package de.jungierek.grblrunner.parts.groups;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.parts.Point;
import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeModelVisitor;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.IConstants;
import de.jungierek.grblrunner.tools.IEvents;
import de.jungierek.grblrunner.tools.IPersistenceKeys;
import de.jungierek.grblrunner.tools.IPreferences;
import de.jungierek.grblrunner.tools.PartTools;

public class GcodeViewGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeViewGroup.class );

    private static final String GROUP_NAME = "Gcode View";

    @Inject
    private MApplication application;

    @Inject
    private IGcodeModel model;

    @Inject
    private IGcodeService gcode;

    @Inject
    private PartTools partTools;

    @Inject
    private IEventBroker eventBroker;

    private Rectangle canvasArea; // TODO check for this global var

    private IGcodeGrblState gcodeState;

    private Point canvasShift = new Point ();
    private double rotX = 0.0 * IConstants.ONE_DEGREE;
    private double rotY = 0.0 * IConstants.ONE_DEGREE;
    private double rotZ = 0.0 * IConstants.ONE_DEGREE;
    private double scale = 5.0;

    private final double workAreaMaxX = IPreferences.WORK_AREA_X; // TODO_PREF preferences
    private final double workAreaMaxY = IPreferences.WORK_AREA_Y; // TODO_PREF preferences
    private IGcodePoint [] workAreaPoints;
    private final int workAreaCenterCrossLength = 3; // its only the half length, like a radius
    private IGcodePoint [] workAreaCenterCrossEndPoints;

    private Canvas canvas;

    private volatile boolean viewGrid = IPreferences.INITIAL_VIEW_GRID;
    private volatile boolean viewGcode = IPreferences.INITIAL_VIEW_GCODE;
    private volatile boolean viewAltitude = IPreferences.INITIAL_VIEW_ALTITUDE;
    private volatile boolean viewWorkarea = IPreferences.INITIAL_VIEW_WORKAREA;
    private volatile boolean viewAltitudeLabel;

    private Label scaleLabel;
    private Label pixelShiftLabel;
    private Label rotationLabel;
    private Label mouseCoordinateLabel;

    public void toggleViewGrid () {
        
        this.viewGrid = !this.viewGrid;
        canvas.redraw ();

    }

    public void toggleViewGcode () {

        this.viewGcode = !this.viewGcode;
        canvas.redraw ();

    }

    public void toggleViewAltitude () {

        this.viewAltitude = !this.viewAltitude;
        canvas.redraw ();
        
    }

    public void toggleViewLabel () {
        
        this.viewAltitudeLabel = !this.viewAltitudeLabel;
        canvas.redraw ();
        
    }

    public void toggleViewWorkarea () {

        this.viewWorkarea = !this.viewWorkarea;
        canvas.redraw ();

    }

    public void viewPlaneXY () {

        rotX = 0.0 * IConstants.ONE_DEGREE;
        rotY = 0.0 * IConstants.ONE_DEGREE;
        rotZ = 0.0 * IConstants.ONE_DEGREE;

        canvas.redraw ();

    }

    public void viewPlaneXZ () {

        rotX = 90.0 * IConstants.ONE_DEGREE;
        rotY = 0.0 * IConstants.ONE_DEGREE;
        rotZ = 0.0 * IConstants.ONE_DEGREE;

        canvas.redraw ();

    }

    public void viewPlaneYZ () {

        rotX = 90.0 * IConstants.ONE_DEGREE;
        rotY = 0.0 * IConstants.ONE_DEGREE;
        rotZ = 90.0 * IConstants.ONE_DEGREE;

        canvas.redraw ();

    }

    public void fitToSize () {
        
        final double margin = IPreferences.FIT_TO_SIZE_MARGIN; // TODO Pref

        if ( model.isGcodeProgramLoaded () ) {

            LOG.debug ( "fitToSize: ----------------------------------------------------" );

            IGcodePoint min = model.getMin ();
            IGcodePoint max = model.getMax ();
            if ( !IPreferences.DUMP_PARSED_GCODE_LINE ) {
                min = min.zeroAxis ( 'Z' );
                max = max.zeroAxis ( 'Z' );
            }

            // an now without

            // TODO gcode max/min bei Drehung ermitteln!
            // funktionerit derzeit nur in xy

            Point minPixelZoom1 = gcodeToPixel ( 1.0, min );
            Point maxPixelZoom1 = gcodeToPixel ( 1.0, max );
            Point minPixel = minPixelZoom1; // TODO check all corner points
            Point maxPixel = maxPixelZoom1; // TODO check all corner points

            Rectangle clientArea = canvas.getClientArea ();
            LOG.debug ( "fitToSize: canvas x=" + clientArea.x + " y =" + clientArea.y + " w=" + clientArea.width + " h=" + clientArea.height );
            LOG.debug ( "fitToSize: minPixel=" + minPixel + " maxPixel=" + maxPixel );
            
            double zoomX = (clientArea.width - 2.0 * margin) / (maxPixel.x - minPixel.x);
            double zoomY = (clientArea.height - 2.0 * margin) / (maxPixel.y - minPixel.y);
            double zoom = Math.min ( zoomX, zoomY );
            if ( zoom < 1.0 ) {
                zoom = 1.0;
            }
            scale = Math.floor ( zoom );
            LOG.debug ( "fitToSize: zoom=" + scale + " x=" + zoomX + " y=" + zoomY );
            
            Point minPixelForShift = gcodeToPixel ( scale, min.add ( model.getShift () ) );
            LOG.debug ( "fitToSize: minPixel=" + minPixelForShift );
            canvasShift = new Point ( margin, margin ).sub ( minPixelForShift );
            LOG.debug ( "fitToSize: canvasShift=" + canvasShift );

            canvas.redraw ();

        }
        else {
            eventBroker.send ( IEvents.EVENT_MSG_ERROR, "no gcode program loaded!" );
        }

    }

    @Focus
    public void setFocus () {

        canvas.setFocus ();

    }


    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int partCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_GROUP_COLS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, "", groupCols, 1, true, true );

        final int cols = 9 + 3;
        group.setLayout ( new GridLayout ( cols, true ) );

        canvas = new Canvas ( group, SWT.NO_BACKGROUND );
        canvas.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, cols, 1 ) );
        canvas.setEnabled ( true );

        // detailsLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "...", cols - 1 );

        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "zoom:", 1 );
        scaleLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );

        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "shift:", 1 );
        pixelShiftLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );

        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "rot:", 1 );
        rotationLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );

        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "mouse:", 1 );
        mouseCoordinateLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );

        canvas.addPaintListener ( new Painter () );
        MouseDetector mouseDetector = new MouseDetector ();
        canvas.addMouseListener ( mouseDetector );
        canvas.addMouseTrackListener ( mouseDetector );
        canvas.addMouseMoveListener ( mouseDetector );
        canvas.addMouseWheelListener ( mouseDetector );
        canvas.addKeyListener ( mouseDetector );

        canvas.redraw ();

        // create arrays here, because we need injected model for this
        initWorkAreaPoints ();

        restorePersistedState ();

    }

    private void restorePersistedState () {

        final Map<String, String> persistedState = application.getPersistedState ();

        scale = partTools.parseDouble ( persistedState.get ( IPersistenceKeys.KEY_VIEW_SCALE ), 5.0 );

        double x = partTools.parseDouble ( persistedState.get ( IPersistenceKeys.KEY_VIEW_PIXEL_SHIFT + "X" ), 0.0 );
        double y = partTools.parseDouble ( persistedState.get ( IPersistenceKeys.KEY_VIEW_PIXEL_SHIFT + "Y" ), 0.0 );
        canvasShift = new Point ( x, y );

        rotX = partTools.parseDouble ( persistedState.get ( IPersistenceKeys.KEY_VIEW_ROTATION + "X" ), 0.0 );
        rotY = partTools.parseDouble ( persistedState.get ( IPersistenceKeys.KEY_VIEW_ROTATION + "Y" ), 0.0 );
        rotZ = partTools.parseDouble ( persistedState.get ( IPersistenceKeys.KEY_VIEW_ROTATION + "Z" ), 0.0 );

    }

    private void savePersistedState () {

        final Map<String, String> persistedState = application.getPersistedState ();

        persistedState.put ( IPersistenceKeys.KEY_VIEW_SCALE, String.format ( IConstants.FORMAT_COORDINATE, scale ) );

        persistedState.put ( IPersistenceKeys.KEY_VIEW_PIXEL_SHIFT + "X", String.format ( IConstants.FORMAT_COORDINATE, canvasShift.x ) );
        persistedState.put ( IPersistenceKeys.KEY_VIEW_PIXEL_SHIFT + "Y", String.format ( IConstants.FORMAT_COORDINATE, canvasShift.y ) );

        persistedState.put ( IPersistenceKeys.KEY_VIEW_ROTATION + "X", String.format ( IConstants.FORMAT_COORDINATE, rotX ) );
        persistedState.put ( IPersistenceKeys.KEY_VIEW_ROTATION + "Y", String.format ( IConstants.FORMAT_COORDINATE, rotY ) );
        persistedState.put ( IPersistenceKeys.KEY_VIEW_ROTATION + "Z", String.format ( IConstants.FORMAT_COORDINATE, rotZ ) );

    }

    private void initWorkAreaPoints () {

        /* @formatter:off */
        workAreaPoints = new IGcodePoint [] { 
                gcode.createGcodePoint ( workAreaMaxX, 0.0, 0.0 ), 
                gcode.createGcodePoint ( workAreaMaxX, workAreaMaxY, 0.0 ), 
                gcode.createGcodePoint ( 0.0, workAreaMaxY, 0.0 ), 
                gcode.createGcodePoint ( 0.0, 0.0, 0.0 ),
        };
        
        workAreaCenterCrossEndPoints = new IGcodePoint [] { 
                gcode.createGcodePoint ( workAreaMaxX / 2 - workAreaCenterCrossLength, workAreaMaxY / 2, 0.0 ), 
                gcode.createGcodePoint ( workAreaMaxX / 2 + workAreaCenterCrossLength, workAreaMaxY / 2, 0.0 ), 
                gcode.createGcodePoint ( workAreaMaxX / 2, workAreaMaxY / 2 - workAreaCenterCrossLength, 0.0 ), 
                gcode.createGcodePoint ( workAreaMaxX / 2, workAreaMaxY / 2 + workAreaCenterCrossLength, 0.0 ),
        };
        /* @formatter:on */

    }

    // TOOD may be delete this methode
    @SuppressWarnings("unused")
    private void initPixelShift () {

        LOG.debug ( "initCanvasShift:" );

        Point canvasPoint = gcodeToCanvas ( gcode.createGcodePoint ( workAreaMaxX, workAreaMaxY, 0.0 ) );
        Point pixelPoint = canvasToPixel ( (int) canvasPoint.x, (int) canvasPoint.y );

        canvasShift.x = (canvasArea.width - canvasArea.x - pixelPoint.x) / 2;
        canvasShift.y = (canvasArea.height - canvasArea.y - pixelPoint.y) / 2;

    }

    private Point gcodeToCanvas ( IGcodePoint gcodePoint ) {

        return gcodeToCanvas ( scale, canvasShift, gcodePoint );

    }

    private Point gcodeToCanvas ( double zoom, Point shift, IGcodePoint gcodePoint ) {

        return pixelToCanvas ( gcodeToPixel ( zoom, gcodePoint ), shift );

    }

    private Point gcodeToPixel ( double zoom, IGcodePoint gcodePoint ) {

        double x0 = zoom * gcodePoint.getX ();
        double y0 = zoom * gcodePoint.getY ();
        double z0 = zoom * gcodePoint.getZ ();

        // rotation around z
        double x1 = x0 * Math.cos ( rotZ ) + y0 * Math.sin ( rotZ );
        double y1 = x0 * -Math.sin ( rotZ ) + y0 * Math.cos ( rotZ );
        double z1 = z0;
        // rotation around y
        double x2 = x1 * Math.cos ( rotY ) + z1 * -Math.sin ( rotY );
        double y2 = y1;
        double z2 = x1 * Math.sin ( rotY ) + z1 * Math.cos ( rotY );
        // rotation around x
        double x3 = x2;
        double y3 = y2 * Math.cos ( rotX ) + z2 * Math.sin ( rotX );
        // double z3 = y2 * -Math.sin ( rotX ) + z2 * Math.cos ( rotX );

        return new Point ( x3, y3 );

    }

    // there are many coordinate systems
    // working coordinates (3D, origin at upper, left, front corner)
    // -> machine coordinates (3D, origin at upper, left, front corner)
    // -> pixel coordinates (2D, after scale, shift and rotate, origin at lower, left corner)
    // -> camvas coordinates (2D, pixel coordinates translated to canvas, origin at upper, left corner)

    private Point pixelToCanvas ( Point pixel, Point shift ) {

        return pixelToCanvas ( pixel.x, pixel.y, shift );

    }

    private Point pixelToCanvas ( double x, double y, Point shift ) {

        return new Point ( canvasArea.x + shift.x + x, canvasArea.y + canvasArea.height - (shift.y + y) );

    }

    private Point canvasToPixel ( int x, int y ) {

        // return new Point ( x - canvasArea.x - pixelShift.x, -y + canvasArea.y + canvasArea.height - pixelShift.y );
        return canvasToPixelIncludingGcodeShift ( x, y ).sub ( canvasShift );

    }

    // pixel shift is included
    private Point canvasToPixelIncludingGcodeShift ( int x, int y ) {

        return new Point ( x - canvasArea.x, -y + canvasArea.y + canvasArea.height );

    }

    private class Painter implements PaintListener {

        private Display display;

        @Override
        public void paintControl ( PaintEvent evt ) {

            LOG.trace ( "paintControl: grid=" + viewGrid + " alt=" + viewAltitude );

            canvasArea = canvas.getClientArea ();
            final int w = canvasArea.width;
            final int h = canvasArea.height;

            display = evt.display;

            scaleLabel.setText ( String.format ( "%.1f", scale ) );
            pixelShiftLabel.setText ( "" + canvasShift );
            String rotXs = String.format ( "%.1f", rotX / IConstants.ONE_DEGREE );
            String rotYs = String.format ( "%.1f", rotY / IConstants.ONE_DEGREE );
            String rotZs = String.format ( "%.1f", rotZ / IConstants.ONE_DEGREE );
            rotationLabel.setText ( "[" + rotXs + "," + rotYs + "," + rotZs + "]" );

            // Double Buffering
            // http://www.eclipsezone.com/eclipse/forums/t24142.html
            Image bufferImage = new Image ( display, w, h );
            GC gc = new GC ( bufferImage );

            // gc.setBackground ( display.getSystemColor ( SWT.COLOR_GRAY ) );
            // gc.fillRectangle ( 0, 0, w, h );

            draw3DCross ( gc );
            // drawMidCross ( w, h, evt.display.getSystemColor ( SWT.COLOR_RED ), gc );
            // drawWorkArea ( gc, 'M' ); // TODO_PREF preference
            if ( viewWorkarea ) drawWorkArea ( gc, 'W' );
            drawOrigign ( gc, 'M' );
            drawOrigign ( gc, 'W' );
            if ( viewGrid ) drawGrid ( gc );
            if ( viewGcode ) drawGcode ( gc );
            drawGantry ( gc );

            // Double Buffering
            evt.gc.drawImage ( bufferImage, 0, 0 );
            bufferImage.dispose ();

        }

        private Color getColor ( int color ) {

            return display.getSystemColor ( color );

        }

        private void draw3DCross ( GC gc ) {

            final IGcodePoint origin = gcode.createGcodePoint ( 0.0, 0.0, 0.0 );
            final double factor = 30.0;
            final Point shift = new Point ( 40.0, 40.0 );
            /* @formatter:off */
            final IGcodePoint [] v = new IGcodePoint [] { 
                    gcode.createGcodePoint ( 1.0, 0.0, 0.0 ), 
                    gcode.createGcodePoint ( 0.0, 1.0, 0.0 ), 
                    gcode.createGcodePoint ( 0.0, 0.0, 1.0 ) 
            };
            // TODO_PREF make it static
            final int color [] = new int [] { SWT.COLOR_RED, SWT.COLOR_GREEN, SWT.COLOR_BLUE };
            final String [] axis = new String [] { "x", "y", "z" };
            /* @formatter:on */

            gc.setLineStyle ( SWT.LINE_SOLID );
            gc.setLineWidth ( 1 );

            Point p0 = gcodeToCanvas ( factor, shift, origin );

            for ( int i = 0; i < v.length; i++ ) {
                gc.setForeground ( getColor ( color[i] ) );
                drawLine ( gc, p0, gcodeToCanvas ( factor, shift, v[i] ) );
                Point p = gcodeToCanvas ( 1.3 * factor, shift, v[i] );
                gc.setForeground ( display.getSystemColor ( SWT.COLOR_BLACK ) );
                org.eclipse.swt.graphics.Point extent = gc.textExtent ( axis[i] );
                gc.drawString ( axis[i], (int) p.x - extent.x / 2, (int) p.y - extent.y / 2, true );
            }

        }

        private void drawGrid ( GC gc ) {

            IGcodePoint [][] m = model.getScanMatrix ();
            LOG.trace ( "drawGrid: m=" + m );

            if ( m == null ) return;

            gc.setLineStyle ( SWT.LINE_SOLID );
            gc.setLineWidth ( 1 );
            gc.setForeground ( getColor ( SWT.COLOR_MAGENTA ) );

            IGcodePoint gcodeShift = model.getShift ();

            for ( int i = 0; i < m.length; i++ ) {
                for ( int j = 0; j < m[i].length; j++ ) {

                    IGcodePoint p1 = m[i][j];
                    if ( !viewAltitude ) p1 = p1.zeroAxis ( 'Z' );
                    p1 = p1.add ( gcodeShift );

                    if ( i + 1 < m.length ) {
                        IGcodePoint p2 = m[i + 1][j];
                        if ( !viewAltitude ) p2 = p2.zeroAxis ( 'Z' );
                        drawLine ( gc, p1, p2.add ( gcodeShift ) );
                    }

                    if ( j + 1 < m[i].length ) {
                        IGcodePoint p2 = m[i][j + 1];
                        if ( !viewAltitude ) p2 = p2.zeroAxis ( 'Z' );
                        drawLine ( gc, p1, p2.add ( gcodeShift ) );
                    }

                }
            }

        }

        private void drawGantry ( GC gc ) {

            if ( gcodeState != null ) {

                gc.setLineStyle ( SWT.LINE_SOLID );
                gc.setLineWidth ( 3 );
                gc.setForeground ( getColor ( SWT.COLOR_RED ) );

                Point p = gcodeToCanvas ( gcodeState.getMachineCoordindates () );
                final int r = 2; // radius in pixel TODO_PREF preferences
                gc.drawOval ( (int) p.x - r, (int) p.y - r, 2 * r, 2 * r );

            }

        }

        private void drawGcode ( GC gc ) {
            model.visit ( new IGcodeModelVisitor () {

                @Override
                public void visit ( IGcodeLine gcodeLine ) {

                    EGcodeMode gcodeMode = gcodeLine.getGcodeMode ();

                    Color color;

                    switch ( gcodeMode ) {

                    // line attributes to preference

                        case MOTION_MODE_SEEK:
                            gc.setLineStyle ( SWT.LINE_SOLID );
                            gc.setLineWidth ( 1 );
                            color = getColor ( SWT.COLOR_GRAY );
                            if ( gcodeLine.isProcessed () ) {
                                color = getColor ( SWT.COLOR_GREEN );
                            }
                            gc.setForeground ( color );
                            drawLine ( gc, gcodeLine );
                            break;
                        case MOTION_MODE_LINEAR:
                            gc.setLineStyle ( SWT.LINE_SOLID );
                            gc.setLineWidth ( 1 );
                            color = getColor ( SWT.COLOR_BLUE );
                            if ( gcodeLine.isProcessed () ) {
                                color = getColor ( SWT.COLOR_GREEN );
                            }
                            gc.setForeground ( color );
                            drawLine ( gc, gcodeLine );
                            break;
                        case MOTION_MODE_PROBE:
                            gc.setLineStyle ( SWT.LINE_DASH );
                            gc.setLineWidth ( 1 );
                            gc.setForeground ( getColor ( SWT.COLOR_RED ) );
                            drawLine ( gc, gcodeLine );
                            break;

                        case COMMENT:
                            break;

                        default:
                            // do nothing
                            break;
                    }

                }

            } );
        }

        @SuppressWarnings("unused")
        private void drawMidCross ( GC gc, final int w, final int h, final Color color ) {
            // paint a cross in the mid
            if ( true ) {
                gc.setLineStyle ( SWT.LINE_DASH );
                gc.setLineWidth ( 1 );
                gc.setForeground ( color );
                int r = 10;
                int x = canvasArea.x + w / 2;
                int y = canvasArea.y + h / 2;
                gc.drawLine ( x - r, y, x + r, y );
                gc.drawLine ( x, y - r, x, y + r );
            }
        }

        private void drawOrigign ( GC gc, char type ) {

            final double r = 0.5;

            IGcodePoint zero = gcode.createGcodePoint ( 0.0, 0.0, 0.0 );

            int color = SWT.COLOR_MAGENTA;
            if ( type == 'W' ) {
                zero = zero.add ( model.getShift () );
                color = SWT.COLOR_CYAN;
            }

            IGcodePoint xDist = gcode.createGcodePoint ( r, 0.0, 0.0 );
            IGcodePoint yDist = gcode.createGcodePoint ( 0.0, r, 0.0 );

            Point p1 = gcodeToCanvas ( zero.sub ( xDist ) );
            Point p2 = gcodeToCanvas ( zero.add ( yDist ) );
            Point p3 = gcodeToCanvas ( zero.add ( xDist ) );
            Point p4 = gcodeToCanvas ( zero.sub ( yDist ) );

            gc.setLineStyle ( SWT.LINE_SOLID );
            gc.setLineWidth ( 2 );
            gc.setForeground ( getColor ( color ) );

            drawLine ( gc, p1, p2 );
            drawLine ( gc, p2, p3 );
            drawLine ( gc, p3, p4 );
            drawLine ( gc, p4, p1 );

            if ( type == 'M' ) {
                gc.setLineStyle ( SWT.LINE_DOT );
                gc.setLineWidth ( 1 );
                gc.setForeground ( getColor ( color ) );
                drawLine ( gc, zero, zero.addAxis ( 'Z', model.getShift () ) );
            }

        }

        private void drawWorkArea ( GC gc, char type ) {

            // print work area
            gc.setForeground ( getColor ( SWT.COLOR_DARK_RED ) );
            gc.setLineStyle ( SWT.LINE_DOT );
            gc.setLineWidth ( 1 ); // TODO_PREF prefrences

            IGcodePoint p1 = workAreaPoints[workAreaPoints.length - 1];
            if ( type == 'W' ) {
                // p1 = p1.add ( gcodeModel.getShift () );
                p1 = p1.addAxis ( 'Z', model.getShift () );
            }

            for ( int i = 0; i < workAreaPoints.length; i++ ) {
                IGcodePoint p2 = workAreaPoints[i];
                if ( type == 'W' ) {
                    p2 = p2.addAxis ( 'Z', model.getShift () );
                }
                drawLine ( gc, p1, p2 );
                p1 = p2;
            }

            // cross in the middle of work area
            gc.setLineStyle ( SWT.LINE_DASH );
            gc.setLineWidth ( 1 );
            gc.setForeground ( getColor ( SWT.COLOR_RED ) );
            drawLine ( gc, workAreaCenterCrossEndPoints[0].addAxis ( 'Z', model.getShift () ), workAreaCenterCrossEndPoints[1].addAxis ( 'Z', model.getShift () ) );
            drawLine ( gc, workAreaCenterCrossEndPoints[2].addAxis ( 'Z', model.getShift () ), workAreaCenterCrossEndPoints[3].addAxis ( 'Z', model.getShift () ) );

        }

        private void drawLine ( GC gc, IGcodeLine gcodeLine ) {

            IGcodePoint start = gcodeLine.getStart ();
            IGcodePoint end = gcodeLine.getEnd ();

            if ( start.equals ( end ) ) return;

            if ( viewAltitude && model.isScanDataComplete () ) {
                IGcodePoint [] path = model.interpolateLine ( start, end );
                for ( int i = 0; i < path.length - 1; i++ ) {
                    drawLine ( gc, path[i].add ( model.getShift () ), path[i + 1].add ( model.getShift () ) );
                }
            }
            else {
                // translate to machine coordinates
                drawLine ( gc, start.add ( model.getShift () ), end.add ( model.getShift () ) );
            }

        }

        private void drawLine ( GC gc, IGcodePoint start, IGcodePoint end ) {

            Point p1 = gcodeToCanvas ( start );
            Point p2 = gcodeToCanvas ( end );

            if ( p1.equals ( p2 ) ) return;

            LOG.trace ( "paintControl.drawLine: x1=" + p1.x + " y1=" + p1.y + " x2=" + p2.x + " y2=" + p2.y );
            LOG.trace ( "paintControl.drawLine: start=" + start + " end=" + end );

            drawLine ( gc, p1, p2 );

        }

        private void drawLine ( GC gc, Point p1, Point p2 ) {

            gc.drawLine ( (int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y );

        }

    }

    private class MouseDetector implements MouseListener, MouseMoveListener, MouseTrackListener, MouseWheelListener, KeyListener {

        // @formatter:off
        //          SWT.MODELESS = 0x0
        // Ctrl:    SWT.MOD1 = 0x40000
        // Shift:   SWT.MOD2 = 0x20000
        // Alt:     SWT.MOD3 = 0x10000
        // ?        SWT.MOD4 = 0x0
        // @formatter:on

        @Override
        // MousWheelListener
        public void mouseScrolled ( MouseEvent evt ) {

            LOG.trace ( "mouseScrolled: evt=" + evt );

            scale += evt.count / 10; // TODO_PREF to preferences
            if ( scale < 1.0 ) scale = 1.0;

            savePersistedState ();
            canvas.redraw ();

        }

        @Override
        // MouseTrackListener
        public void mouseEnter ( MouseEvent evt ) {

            LOG.trace ( "mouseEnter: evt=" + evt );

        }

        @Override
        // MouseTrackListener
        public void mouseExit ( MouseEvent evt ) {

            LOG.trace ( "mouseExit: evt=" + evt );

        }

        @Override
        // MouseTrackListener
        public void mouseHover ( MouseEvent evt ) {

            LOG.trace ( "mouseHover: evt=" + evt );

            Canvas canvas = (Canvas) evt.getSource ();
            Rectangle clientArea = canvas.getClientArea ();
            
            final int x = evt.x - clientArea.x;
            final int y = clientArea.height-evt.y - clientArea.y;
            mouseCoordinateLabel.setText ( "[" + x + "," + y + "]" );

            canvas.redraw ();

        }

        @Override
        // MousListener
        public void mouseDoubleClick ( MouseEvent evt ) {

            LOG.trace ( "mouseDoubleClick: evt=" + evt );

            // repos only for left button
            if ( evt.button == 1 && evt.stateMask == 0 ) {

                // Point click = canvasToPixel ( evt.x, evt.y ).add (
                // shiftCanvas );
                Point click = canvasToPixelIncludingGcodeShift ( evt.x, evt.y );
                Point midCanvas = new Point ( canvasArea.width / 2, canvasArea.height / 2 );
                canvasShift.add ( midCanvas.sub ( click ) );

                savePersistedState ();
                canvas.redraw ();

            }
            // right double eliminates shift
            else if ( evt.button == 3 && evt.stateMask == 0 ) {

                canvasShift = new Point ( 0.0, 0.0 );
                // initPixelShift ();

                rotX = 0.0;
                rotY = 0.0;
                rotZ = 0.0;

                scale = 5.0;

                savePersistedState ();
                canvas.redraw ();

            }

        }

        private Point lastPoint = new Point ();
        private boolean move = false;
        private boolean rotate;
        private boolean capture;

        @Override
        // MouseMoveListener
        public void mouseMove ( MouseEvent evt ) {

            LOG.trace ( "mouseMove: evt=" + evt );

            if ( move ) {
                Point p = canvasToPixelIncludingGcodeShift ( evt.x, evt.y );
                lastPoint.sub ( p );
                canvasShift.sub ( lastPoint );
                lastPoint = p;

                canvas.redraw ();
            }
            else if ( rotate ) {
                Point p = canvasToPixelIncludingGcodeShift ( evt.x, evt.y );
                rotX += -(lastPoint.y - p.y) * IConstants.ONE_DEGREE / 4;
                rotZ += -(lastPoint.x - p.x) * IConstants.ONE_DEGREE / 4;
                lastPoint = p;

                canvas.redraw ();
            }
            else if ( capture ) {
                // TODO implement capture
            }

        }

        @Override
        // MousListener
        // left/right mouse button like openSCAD
        public void mouseDown ( MouseEvent evt ) {

            LOG.trace ( "mouseDown: evt=" + evt );

            // String mod = "";
            // if ( (evt.stateMask & SWT.MOD1) != 0 ) mod += "1";
            // if ( (evt.stateMask & SWT.MOD2) != 0 ) mod += "2";
            // if ( (evt.stateMask & SWT.MOD3) != 0 ) mod += "3";
            // if ( (evt.stateMask & SWT.MOD4) != 0 ) mod += "4";
            // System.out.println ( logName () + "mouseDown: stateMask=" + mod );

            // TODO check evt.button
            if ( evt.count == 1 ) {

                switch ( evt.button ) {
                    case 1: // left
                        lastPoint = canvasToPixelIncludingGcodeShift ( evt.x, evt.y );
                        if ( evt.stateMask == 0 ) {
                            rotate = true;
                        }
                        else if ( (evt.stateMask & SWT.MOD1) == 0 ) { // Ctrl
                            capture = true;
                        }
                        break;

                    case 2: // mid
                        break;

                    case 3: // right
                        if ( evt.stateMask == 0 ) {
                            lastPoint = canvasToPixelIncludingGcodeShift ( evt.x, evt.y );
                            move = true;
                        }
                        break;

                    default:
                        break;
                }
            }

        }

        @Override
        // MousListener
        public void mouseUp ( MouseEvent evt ) {

            LOG.trace ( "mouseUp: evt=" + evt );

            move = false;
            rotate = false;
            capture = false;

            savePersistedState ();

        }

        @Override
        public void keyPressed ( KeyEvent evt ) {

            LOG.trace ( "keyPressed: evt=" + evt );

            if ( evt.character == '\0' ) {

                switch ( evt.keyCode ) {
                    case 0x1000001: // up
                        canvasShift.y += 10;
                        savePersistedState ();
                        canvas.redraw ();
                        break;

                    case 0x1000002: // down
                        canvasShift.y -= 10;
                        savePersistedState ();
                        canvas.redraw ();
                        break;

                    case 0x1000003: // left
                        canvasShift.x -= 10;
                        savePersistedState ();
                        canvas.redraw ();
                        break;

                    case 0x1000004: // right
                        canvasShift.x += 10;
                        savePersistedState ();
                        canvas.redraw ();
                        break;

                    default:
                        break;
                }
            }

        }

        @Override
        public void keyReleased ( KeyEvent evt ) {

            LOG.trace ( "keyReleased: evt=" + evt );

        }

    }

    @Inject
    @Optional
    public void stateUpdateNotified ( @UIEventTopic(IEvents.EVENT_GCODE_UPDATE_STATE) IGcodeGrblState state ) {

        // System.out.println ( logName () + "stateUpdateNotified: state=" + state );

        gcodeState = state;
        canvas.redraw ();

    }

    @Inject
    @Optional
    public void playerLoadedNotified ( @UIEventTopic(IEvents.EVENT_GCODE_PLAYER_LOADED) String fileName ) {

        LOG.debug ( "playerLoadedNotified: fileName=" + fileName );

        canvas.redraw ();

    }

    @Inject
    @Optional
    public void redrawScanGridNotified ( @UIEventTopic(IEvents.REDRAW) Integer viewFlags ) {

        LOG.debug ( "redrawScanGridNotified: viewFlags=" + viewFlags );

        canvas.redraw ();

    }

    @Inject
    @Optional
    public void updateCoordSelectOffsetsNotified ( @UIEventTopic(IEvents.EVENT_GCODE_UPDATE_COORD_SELECT_OFFSET) Object dummy ) {

        LOG.debug ( "updateCoordSelectOffsetsNotified" );

        canvas.redraw ();

    }

    @Inject
    @Optional
    public void updateProbeNotified ( @UIEventTopic(IEvents.EVENT_PROBE_UPDATE) IGcodePoint probe ) {

        LOG.trace ( "updateProbeNotified: probe=" + probe );

        if ( gcode.isScanning () ) canvas.redraw ();

    }

}
