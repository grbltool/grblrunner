package de.jungierek.grblrunner.service.webcam.impl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamExceptionHandler;

import de.jungierek.grblrunner.service.webcam.IWebcamService;
import de.jungierek.grblrunner.service.webcam.IWebcamServiceReceiver;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class WebcamServiceImpl implements IWebcamService {

    private static final Logger LOG = LoggerFactory.getLogger ( WebcamServiceImpl.class );

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
     * Scheduled executor acting as timer.
     */
    private ScheduledExecutorService executor = null;

    private volatile ImageUpdater updater = null;

    private IWebcamServiceReceiver receiver;

    private Webcam [] cachedWebcams;

    @Inject
    public WebcamServiceImpl () {

        LOG.debug ( "Constructor called" );
        
        detectWebcamsAsync ();

    }

    @PostConstruct
    public void postConstruct () {
        
        LOG.info ( "postConstruct called" );

    }

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
    private class ImageUpdater extends Thread {

        // TODO eliminate executer
        @Override
        public void run () {

            LOG.trace ( "image updater called" );

            if ( !webcam.isOpen () ) return;

            if ( receiver != null ) {
                final BufferedImage image = webcam.getImage ();
                receiver.received ( image );
            }

        }

    }

    @Override
    public void setWebcam ( Webcam webcam ) {

        if ( webcam == null ) { throw new IllegalArgumentException ( "Webcam cannot be null" ); }

        this.webcam = webcam;

        // HACK choose highest resolution
        Dimension [] sizes = webcam.getViewSizes ();
        webcam.setViewSize ( sizes[sizes.length - 1] );

    }

    @Override
    public String getWebcamSize () {

        if ( webcam == null ) return null;

        return dimToText ( webcam.getViewSize () );

    }

    @Override
    public String [] getWebcamSizes () {

        if ( webcam == null ) return null;

        Dimension [] viewSizes = webcam.getViewSizes ();

        String [] result = new String [viewSizes.length];
        for ( int i = 0; i < result.length; i++ ) {
            result[i] = dimToText ( viewSizes[i] );
        }

        return result;

    }

    private String dimToText ( final Dimension dim ) {

        return dim.getWidth () + " x " + dim.getHeight ();

    }

    @Override
    public void setViewSize ( String resolution ) {

        if ( resolution == null ) return;
        if ( webcam == null ) return;

        Dimension [] viewSizes = getWebcam ().getViewSizes ();
        for ( Dimension size : viewSizes ) {
            if ( resolution.equals ( dimToText ( size ) ) ) {
                LOG.info ( "set res at " + size );
                webcam.setViewSize ( size );
            }
        }

    }

    @Override
    public Webcam getWebcam () {
        
        return webcam;
        
    }

    /**
     * Open webcam and start rendering.
     */
    @Override
    public void start () {

        LOG.info ( "Starting panel rendering and trying to open attached webcam" );
        LOG.info ( "res=" + webcam.getViewSize () );

        updater = new ImageUpdater ();
        executor = Executors.newScheduledThreadPool ( 1, THREAD_FACTORY );
        executor.scheduleAtFixedRate ( updater, 0, (long) (1000 / frequency), TimeUnit.MILLISECONDS );
        // executor.scheduleAtFixedRate ( updater, 0, 3L, TimeUnit.SECONDS );

        if ( !webcam.open () ) throw new WebcamException ( "Webcam " + webcam + " not open" );

    }

    /**
     * Stop rendering and close webcam.
     */
    @Override
    public void stop () {

        LOG.info ( "Stopping panel rendering and closing attached webcam" );

        executor.shutdown ();
        updater = null;
        webcam.close ();
        
        receiver.received ( null );

    }

    /**
     * Get rendering frequency in FPS (equivalent to Hz).
     * 
     * @return Rendering frequency
     */
    @Override
    public double getFPS () {
        return frequency;
    }

    /**
     * Set rendering frequency (in Hz or FPS). Minimum frequency is 0.016 (1 frame per minute) and maximum is 25 (25 frames per second).
     * 
     * @param frequency
     *            the frequency
     */
    @Override
    public void setFPS ( double frequency ) {

        if ( frequency > MAX_FREQUENCY ) frequency = MAX_FREQUENCY;
        if ( frequency < MIN_FREQUENCY ) frequency = MIN_FREQUENCY;

        this.frequency = frequency;

    }
    
    @Override
    public void detectWebcams () {

        List<Webcam> webcams = Webcam.getWebcams ();
        cachedWebcams = webcams.toArray ( new Webcam [webcams.size ()] );

    }

    @Override
    public void detectWebcamsAsync () {

        new Thread ( ( ) -> detectWebcams () ).start ();

    }

    @Override
    public Webcam [] getWebcams () {

        return cachedWebcams;

    }

    @Override
    public void setReceiver ( IWebcamServiceReceiver receiver ) {

        this.receiver = receiver;

    }
    
}