package de.jungierek.grblrunner.webcam;

//package com.github.sarxos.webcam.addon.swt;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamExceptionHandler;

public class WebcamComposite extends Composite {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger ( WebcamComposite.class );

    /**
     * Minimum FPS frequency.
     */
    public static final double MIN_FREQUENCY = 0.016; // 1 frame per minute

    /**
     * Maximum FPS frequency.
     */
    private static final double MAX_FREQUENCY = 50; // 50 frames per second

    /**
     * Thread factory used by execution service.
     */
    private static final ThreadFactory THREAD_FACTORY = new CompositeThreadFactory ();

    /**
     * Webcam object used to fetch images.
     */
    private Webcam webcam = null;

    /**
     * Frames requesting frequency.
     */
    private double frequency = 5; // FPS

    /**
     * Image currently being displayed.
     */
    private Image image = null;

    /**
     * Repainter is used to fetch images from camera and force panel repaint when image is ready.
     */
    private volatile ImageUpdater updater = null;

    /**
     * Painting is paused.
     */
    private volatile boolean paused = false;

    /**
     * Scheduled executor acting as timer.
     */
    private ScheduledExecutorService executor = null;

    private static final class CompositeThreadFactory implements ThreadFactory {
    
        private static final AtomicInteger number = new AtomicInteger ( 0 );
    
        @Override
        public Thread newThread ( Runnable r ) {
            Thread result = new Thread ( r, String.format ( "webcam-composite-scheduled-executor-%d", number.incrementAndGet () ) );
            result.setUncaughtExceptionHandler ( WebcamExceptionHandler.getInstance () );
            result.setDaemon ( true ); // TODO why this!!!
            return result;
        }
    
    }

    /**
     * Image updater reads images from camera and force panel to be repainted.
     * 
     * @author Bartosz Firyn (SarXos)
     */
    private class ImageUpdater implements Runnable {

        @Override
        public void run () {

            if ( !webcam.isOpen () || paused ) return;

            BufferedImage awtBufferedImage = webcam.getImage ();
            if ( awtBufferedImage != null ) {

                Image newImage = awtToSwtImage ( awtBufferedImage );
                if ( newImage != null ) {
                    if ( image != null && !image.isDisposed () ) image.dispose ();
                    image = newImage;
                    Display.getDefault ().syncExec ( ( ) -> {
                        setBackgroundImage ( image );
                        redraw ();
                    } );
                }

            }

        }

        private Image awtToSwtImage ( BufferedImage awtBufferedImage ) {

            ComponentColorModel awtColorModel = (ComponentColorModel) awtBufferedImage.getColorModel ();
            PaletteData swtPalettedata = new PaletteData ( 0x0000FF, 0x00FF00, 0xFF0000 );
            ImageData swtImageData = new ImageData ( awtBufferedImage.getWidth (), awtBufferedImage.getHeight (), awtColorModel.getPixelSize (), swtPalettedata );

            // this is valid because we are using a 3-byte data model without
            // transparent pixels
            swtImageData.transparentPixel = -1;

            WritableRaster awtWritableRaster = awtBufferedImage.getRaster ();

            int [] rgb = new int [3];

            int x = 0;
            int y = 0;

            for ( x = 0; x < swtImageData.width; x++ ) {
                for ( y = 0; y < swtImageData.height; y++ ) {
                    awtWritableRaster.getPixel ( x, y, rgb );
                    swtImageData.setPixel ( x, y, swtPalettedata.getPixel ( new RGB ( rgb[0], rgb[1], rgb[2] ) ) );
                }
            }

            return new Image ( Display.getDefault (), swtImageData );

        }

    }

    

    // /**
    // * Painter used to draw image in panel.
    // *
    // * @see #setPainter(Painter)
    // * @see #getPainter()
    // */
    // private Painter painter = new DefaultPainter();

    public WebcamComposite ( Composite parent, int style ) {

        super ( parent, style );
        setLayout ( new FillLayout ( SWT.HORIZONTAL ) );
        setSize ( 640, 480 );
        setVisible ( true );
        setBackground ( new Color ( Display.getDefault (), new RGB ( 124, 23, 56 ) ) );

    }

    @Override
    public void dispose () {
    
        if ( image != null ) image.dispose ();
        super.dispose ();
    
    }

    public void setWebcam ( Webcam webcam ) {

        if ( webcam == null ) { throw new IllegalArgumentException ( "Webcam cannot be null" ); }
        this.webcam = webcam;

    }

    /**
     * Open webcam and start rendering.
     */
    public void startCapturing () {

        LOG.debug ( "Starting panel rendering and trying to open attached webcam" );

        updater = new ImageUpdater ();
        executor = Executors.newScheduledThreadPool ( 1, THREAD_FACTORY );
        executor.scheduleAtFixedRate ( updater, 0, (long) (1000 / frequency), TimeUnit.MILLISECONDS );
        if ( !webcam.open () ) throw new WebcamException ( "Webcam " + webcam + " not open" );

    }

    /**
     * Stop rendering and close webcam.
     */
    public void stopCapturing () {

        LOG.debug ( "Stopping panel rendering and closing attached webcam" );

        executor.shutdown ();
        updater = null;
        image = null;

        if ( !webcam.close () ) Display.getDefault ().syncExec ( ( ) -> redraw () );

    }

    /**
     * Pause rendering.
     */
    public void pause () {

        if ( paused ) return;

        LOG.debug ( "Pausing panel rendering" );
        paused = true;

    }

    /**
     * Resume rendering.
     */
    public void resume () {

        if ( !paused ) return;

        LOG.debug ( "Resuming panel rendering" );
        paused = false;

    }

    /**
     * Get rendering frequency in FPS (equivalent to Hz).
     * 
     * @return Rendering frequency
     */
    public double getFPS () {
        return frequency;
    }

    /**
     * Set rendering frequency (in Hz or FPS). Minimum frequency is 0.016 (1 frame per minute) and maximum is 25 (25 frames per second).
     * 
     * @param frequency
     *            the frequency
     */
    public void setFPS ( double frequency ) {

        if ( frequency > MAX_FREQUENCY ) frequency = MAX_FREQUENCY;
        if ( frequency < MIN_FREQUENCY ) frequency = MIN_FREQUENCY;

        this.frequency = frequency;

    }

    public static void main ( String [] args ) throws TimeoutException {
    
        final org.slf4j.Logger log = LoggerFactory.getLogger ( WebcamComposite.class );
        log.trace ( "trace" );
        log.debug ( "debug" );
        log.info ( "info" );
        log.warn ( "warning" );
        log.error ( "error" );
    
        Display display = Display.getDefault ();
        Shell shell = new Shell ( display );
    
        shell.setLayout ( new FillLayout () );
        shell.setText ( "Test" );
        shell.setSize ( 640, 480 );
        shell.setBackground ( new Color ( Display.getDefault (), new RGB ( 65, 120, 45 ) ) );
    
        Webcam webCam;
        final boolean useDefault = false;
        if ( useDefault ) {
            webCam = Webcam.getDefault ();
        }
        else {
            List<Webcam> webcams = Webcam.getDiscoveryService ().getWebcams ( 1000, TimeUnit.MILLISECONDS );
            LOG.debug ( "webcams=" + webcams );
            webCam = webcams.get ( 0 );
        }
        
        WebcamComposite composite = new WebcamComposite ( shell, SWT.EMBEDDED );
        composite.setWebcam ( webCam );
        composite.startCapturing ();
    
        shell.open ();
    
        while ( !shell.isDisposed () ) {
            if ( !display.readAndDispatch () ) {
                display.sleep ();
            }
        }
    
        System.out.println ( "done" );
    
        composite.dispose ();
        display.dispose ();
    }
}