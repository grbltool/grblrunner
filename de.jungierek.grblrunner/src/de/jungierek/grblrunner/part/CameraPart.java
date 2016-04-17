package de.jungierek.grblrunner.part;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.jungierek.grblrunner.service.webcam.IWebcamService;
import de.jungierek.grblrunner.service.webcam.IWebcamServiceReceiver;

public class CameraPart implements PaintListener, IWebcamServiceReceiver {

    private static final Logger LOG = LoggerFactory.getLogger ( CameraPart.class );

    @Inject
    private IWebcamService webcamService;

    @Inject
    private Display display;

    private Canvas webcamCanvas;
    private BufferedImage webcamImage;

    @PostConstruct
    private void createGui ( Composite parent ) {
        
        // for ( Webcam wc : Webcam.getWebcams () ) {
        // System.out.println ( "wc=" + wc );
        // }
        
        Webcam webcam = Webcam.getDefault ();
        LOG.debug ( "createGui: webcam=" + webcam );
        if ( webcam == null ) LOG.warn ( "createGui: No webcam detected" );

        int cols = 1;
        parent.setLayout ( new GridLayout ( cols, true ) ); // equal width column

        webcamCanvas = new Canvas ( parent, SWT.NO_BACKGROUND );
        webcamCanvas.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, cols, 1 ) );

        webcamCanvas.addPaintListener ( this );
        webcamService.setReceiver ( this );

        setControlsEnabled ( true );

    }

    private void setControlsEnabled ( boolean enabled ) {

        if ( webcamCanvas != null ) webcamCanvas.setEnabled ( enabled );

    }

    @Override
    public void paintControl ( PaintEvent paintEvent ) {

        LOG.trace ( "paintControl:" );

        Rectangle canvasArea = webcamCanvas.getClientArea ();

        Image bufferImage = new Image ( paintEvent.display, canvasArea.width, canvasArea.height );
        GC gc = new GC ( bufferImage );

        if ( webcamImage == null ) {
            // noop blank canvas
        }
        else {

            Image camImage = awtToSwtImage ( webcamImage );
            if ( camImage != null ) {
                // center cam
                final Rectangle camBounds = camImage.getBounds ();
                final int x = canvasArea.x + (canvasArea.width - camBounds.width) / 2;
                final int y = canvasArea.y + (canvasArea.height - camBounds.height) / 2;
                // copy cam image to double buffer
                gc.drawImage ( camImage, x, y );
                camImage.dispose ();
            }

            // TODO_PREF Preference
            drawCross ( gc, 58, -60, canvasArea, paintEvent.display.getSystemColor ( SWT.COLOR_RED ) );
            // drawWeb ( gc, 30, canvasArea, paintEvent.display.getSystemColor ( SWT.COLOR_RED ) );

        }

        // Double Buffering
        paintEvent.gc.drawImage ( bufferImage, 0, 0 );
        bufferImage.dispose ();

    }

    private void drawWeb ( final GC gc, int step, final Rectangle canvasArea, final Color color ) {

        gc.setLineStyle ( SWT.LINE_SOLID );
        gc.setLineWidth ( 1 );
        gc.setForeground ( color );

        for ( int x = canvasArea.x; x < canvasArea.width; x += step ) {
            gc.drawLine ( x, canvasArea.y, x, canvasArea.y + canvasArea.height );
        }

        for ( int y = canvasArea.y; y < canvasArea.height; y += step ) {
            gc.drawLine ( canvasArea.x, y, canvasArea.x + canvasArea.width, y );
        }
    }

    private void drawCross ( final GC gc, final int dx, final int dy, final Rectangle canvasArea, final Color color ) {

        if ( true ) {
            gc.setLineStyle ( SWT.LINE_SOLID );
            gc.setLineWidth ( 1 );
            gc.setForeground ( color );
            int r = 20;
            int x = canvasArea.x + canvasArea.width / 2 + dx;
            int y = canvasArea.y + canvasArea.height / 2 + dy;
            gc.drawLine ( x - 2 * r, y, x + 2 * r, y );
            gc.drawLine ( x, y - 2 * r, x, y + 2 * r );
            gc.drawOval ( x - r, y - r, 2 * r, 2 * r );
        }

    }

    // if ( size == null ) {
    // Dimension resolution = webcam.getViewSize ();
    // setSize ( resolution.width, resolution.height );
    // }

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

        return new Image ( display, swtImageData );

    }

    @Override
    public void received ( BufferedImage image ) {

        LOG.trace ( "received called" );

        webcamImage = image;
        display.syncExec ( ( ) -> {
            if ( !webcamCanvas.isDisposed () ) webcamCanvas.redraw ();
        } );

    }

}
