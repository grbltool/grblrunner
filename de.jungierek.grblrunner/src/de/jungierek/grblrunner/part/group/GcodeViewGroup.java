package de.jungierek.grblrunner.part.group;

import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.resource.StringConverter;
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

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPersistenceKey;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tool.GuiFactory;
import de.jungierek.grblrunner.tool.Toolbox;

@SuppressWarnings("restriction")
public class GcodeViewGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeViewGroup.class );

    private static final String GROUP_NAME = "Gcode View";

    @Inject
    private MApplication application;

    @Inject
    private IGcodeService gcodeService;

    @Inject
    private Toolbox toolbox;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private Display display;

    private IGcodeProgram gcodeProgram;
    private IGcodeProgram overlayGcodeProgram;

    private Rectangle canvasArea;

    private IGcodeGrblState gcodeState;

    private Point canvasShift = new Point ();
    private double rotX = 0.0 * IConstant.ONE_DEGREE;
    private double rotY = 0.0 * IConstant.ONE_DEGREE;
    private double rotZ = 0.0 * IConstant.ONE_DEGREE;
    private double scale = IConstant.INITIAL_SCALE;

    private double sinRotX = Math.sin ( rotX );
    private double cosRotX = Math.cos ( rotX );
    private double sinRotY = Math.sin ( rotY );
    private double cosRotY = Math.cos ( rotY );
    private double sinRotZ = Math.sin ( rotZ );
    private double cosRotZ = Math.cos ( rotZ );

    // preferences
    private double workAreaMaxX; // set from preferences
    private double workAreaMaxY; // set from preferences
    private boolean isWorkAreaOrigin0x0; // set from preferences

    private double fitToSizeMargin; // set from preferences

    private Color autolevelGridColor; // set from preferences
    private Color gantryColor; // set from preferences
    private Color gcodeProcessedColor; // set from preferences
    private Color gcodeMotionModeSeekColor; // set from preferences
    private Color gcodeMotionModeLinearColor; // set from preferences
    private Color gcodeMotionModeArcColor; // set from preferences
    private Color gcodeMotionModeProbeColor; // set from preferences
    private Color machineOriginColor; // set from preferences
    private Color workOriginColor; // set from preferences
    private Color workareaBorderColor; // set from preferences
    private Color workareaMidcrossColor; // set from preferences

    private boolean fitToSizeWithZ; // set from preferences

    private Color overlayGcodeMotionModeSeekColor; // set from preferences
    private Color overlayGcodeMotionModeLinearColor; // set from preferences
    private Color overlayGcodeMotionModeArcColor; // set from preferences

    private IGcodePoint [] workAreaPoints;
    private final int workAreaCenterCrossLength = 3; // its only the half length, like a radius
    private IGcodePoint [] workAreaCenterCrossEndPoints;

    private Canvas canvas;
    private GC canvasGC;

    private volatile boolean viewGrid = IConstant.INITIAL_VIEW_GRID;
    private volatile boolean viewGcode = IConstant.INITIAL_VIEW_GCODE;
    private volatile boolean viewAltitude = IConstant.INITIAL_VIEW_ALTITUDE;
    private volatile boolean viewWorkarea = IConstant.INITIAL_VIEW_WORKAREA;
    private volatile boolean viewAltitudeLabel;

    private Label scaleLabel;
    private Label pixelShiftLabel;
    private Label rotationLabel;
    private Label mouseCoordinateLabel;

    private Image gcodeImage;
    private GC gcodeImageGC;

    private Image gantryImage = new Image ( display, 2 * (IConstant.GANTRY_LINE_WIDTH + IConstant.GANTRY_RADIUS) + 1, 2 * (IConstant.GANTRY_LINE_WIDTH + IConstant.GANTRY_RADIUS) + 1 );

    @Inject
    public void setGcodeProgram ( @Optional @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram program ) {

        LOG.debug ( "setGcodeProgram: program=" + program );

        gcodeProgram = program;
        redraw ();

    }

    public void setOverlayGcodeProgram ( IGcodeProgram program ) {

        LOG.debug ( "setOverlayGcodeProgram: program=" + program );

        overlayGcodeProgram = program;
        redraw ();

    }

    public void setInitialViewFlags ( boolean viewGrid, boolean viewGcode, boolean viewAltitude, boolean viewWorkArea ) {
        
        this.viewGrid = viewGrid;
        this.viewGcode = viewGcode;
        this.viewAltitude = viewAltitude;
        this.viewWorkarea = viewWorkArea;

    }

    public void toggleViewGrid () {

        this.viewGrid = !this.viewGrid;
        redraw ();

    }

    public void toggleViewGcode () {

        this.viewGcode = !this.viewGcode;
        redraw ();

    }

    public void toggleViewAltitude () {

        this.viewAltitude = !this.viewAltitude;
        redraw ();

    }

    public void toggleViewLabel () {

        this.viewAltitudeLabel = !this.viewAltitudeLabel;
        redraw ();

    }

    public void toggleViewWorkarea () {

        this.viewWorkarea = !this.viewWorkarea;
        redraw ();

    }

    public void viewPlaneXY () {

        rotX = 0.0 * IConstant.ONE_DEGREE;
        rotY = 0.0 * IConstant.ONE_DEGREE;
        rotZ = 0.0 * IConstant.ONE_DEGREE;

        redraw ();

    }

    public void viewPlaneXZ () {

        rotX = 90.0 * IConstant.ONE_DEGREE;
        rotY = 0.0 * IConstant.ONE_DEGREE;
        rotZ = 0.0 * IConstant.ONE_DEGREE;

        redraw ();

    }

    public void viewPlaneYZ () {

        rotX = 90.0 * IConstant.ONE_DEGREE;
        rotY = 0.0 * IConstant.ONE_DEGREE;
        rotZ = 90.0 * IConstant.ONE_DEGREE;

        redraw ();

    }

    public void viewIso () {

        rotX = 60.0 * IConstant.ONE_DEGREE;
        rotY = 0.0 * IConstant.ONE_DEGREE;
        rotZ = -35.264 * IConstant.ONE_DEGREE;

        redraw ();

    }

    public void fitToSize () {

        if ( gcodeProgram == null ) return;

        if ( gcodeProgram.isLoaded () ) {

            LOG.debug ( "fitToSize: ----------------------------------------------------" );

            IGcodePoint min = gcodeProgram.getMin ();
            IGcodePoint max = gcodeProgram.getMax ();
            LOG.debug ( "fitToSize 1: min=" + min + " max=" + max );

            if ( !fitToSizeWithZ ) {
                min = min.zeroAxis ( 'Z' );
                max = max.zeroAxis ( 'Z' );
                LOG.debug ( "fitToSize 2: min=" + min + " max=" + max );
            }

            // all 4 corners in gcode
            IGcodePoint p00 = min;
            IGcodePoint p10 = gcodeService.createGcodePoint ( max.getX (), min.getY (), 0.0 ); // TODO interpolate z
            IGcodePoint p01 = gcodeService.createGcodePoint ( min.getX (), max.getY (), 0.0 ); // TODO interpolate z
            IGcodePoint p11 = max;
            LOG.debug ( "fitToSize 3: p00=" + p00 + " p10=" + p10 + " p01=" + p01 + " p11=" + p11 );

            // transform to pixel coordinates, no zoom but with rotation
            Point p00Pixel = gcodeToPixel ( 1.0, p00 );
            Point p10Pixel = gcodeToPixel ( 1.0, p10 );
            Point p01Pixel = gcodeToPixel ( 1.0, p01 );
            Point p11Pixel = gcodeToPixel ( 1.0, p11 );
            LOG.debug ( "fitToSize 4: p00Pixel=" + p00Pixel + " p10Pixel=" + p10Pixel + " p01Pixel=" + p01Pixel + " p11Pixel=" + p11Pixel );

            // determine pixel area
            final Point minPixel = p00Pixel.min ( p10Pixel.min ( p01Pixel.min ( p11Pixel ) ) );
            final Point maxPixel = p00Pixel.max ( p10Pixel.max ( p01Pixel.max ( p11Pixel ) ) );
            LOG.debug ( "fitToSize 5: min=" + minPixel + " max=" + maxPixel );

            // compute zoom
            Rectangle clientArea = canvas.getClientArea ();
            LOG.debug ( "fitToSize: canvas x=" + clientArea.x + " y =" + clientArea.y + " w=" + clientArea.width + " h=" + clientArea.height );
            LOG.debug ( "fitToSize: minPixel=" + minPixel + " maxPixel=" + maxPixel );
            double zoomX = (clientArea.width - clientArea.x - 2.0 * fitToSizeMargin) / (maxPixel.x - minPixel.x);
            double zoomY = (clientArea.height - clientArea.y - 2.0 * fitToSizeMargin) / (maxPixel.y - minPixel.y);
            double zoom = Math.min ( zoomX, zoomY );
            if ( zoom < 1.0 ) {
                zoom = 1.0;
            }
            scale = Math.floor ( zoom );
            LOG.debug ( "fitToSize: zoom=" + scale + " x=" + zoomX + " y=" + zoomY );

            // compute scale
            final IGcodePoint fixtureShift = gcodeService.getFixtureShift ();
            p00Pixel = gcodeToPixel ( p00.add ( fixtureShift ) );
            p10Pixel = gcodeToPixel ( p10.add ( fixtureShift ) );
            p01Pixel = gcodeToPixel ( p01.add ( fixtureShift ) );
            p11Pixel = gcodeToPixel ( p11.add ( fixtureShift ) );
            LOG.debug ( "fitToSize: p00Pixel=" + p00Pixel + " p10Pixel=" + p10Pixel + " p01Pixel=" + p01Pixel + " p11Pixel=" + p11Pixel );

            final Point pixelShift = p00Pixel.min ( p10Pixel.min ( p01Pixel.min ( p11Pixel ) ) );
            LOG.debug ( "fitToSize: pixelShift=" + pixelShift );

            canvasShift = new Point ( fitToSizeMargin, fitToSizeMargin ).sub ( pixelShift );
            LOG.debug ( "fitToSize: canvasShift=" + canvasShift );

            redraw ();

        }
        else {
            eventBroker.send ( IEvent.MESSAGE_ERROR, "no gcode program loaded!" );
        }

    }

    @Inject
    public void setWorkAreaX ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.WORK_AREA_MAX_X) double maxX ) {

        workAreaMaxX = maxX;
        initWorkAreaPoints ();
        redraw ();

    }

    @Inject
    public void setWorkAreaY ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.WORK_AREA_MAX_Y) double maxY ) {

        workAreaMaxY = maxY;
        initWorkAreaPoints ();
        redraw ();

    }
    
    @Inject
    public void setWorkAreaOriginAt0x0 ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.WORK_AREA_ORGIN_0x0) boolean flag ) {

        isWorkAreaOrigin0x0 = flag;
        initWorkAreaPoints ();
        redraw ();

    }

    @Inject
    public void setFitToSizeMargin ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.FIT_TO_SIZE_MARGIN) double margin ) {

        fitToSizeMargin = margin;
        fitToSize ();

    }
    
    @Inject
    public void setAutolevelGridColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_AUTOLEVEL_GRID) String rgbText ) {

        autolevelGridColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setGantryColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_GANTRY) String rgbText ) {

        gantryColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setMachineOriginColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_MACHINE_ORIGIN) String rgbText ) {

        machineOriginColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setWorkOriginColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_WORK_ORIGIN) String rgbText ) {

        workOriginColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setWorkareaBorderColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_WORKAREA_BORDER) String rgbText ) {

        workareaBorderColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setWorkareaMidcrossColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_WORKAREA_MIDCROSS) String rgbText ) {

        workareaMidcrossColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setMotionModeSeekColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_SEEK) String rgbText ) {

        gcodeMotionModeSeekColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setMotionModeLinearColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_LINEAR) String rgbText ) {

        gcodeMotionModeLinearColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setMotionModeArcColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_ARC) String rgbText ) {

        gcodeMotionModeArcColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setMotionModeProbeColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_PROBE) String rgbText ) {

        gcodeMotionModeProbeColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setGcodeProcessedColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_PROCESSED) String rgbText ) {

        gcodeProcessedColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setFitToSizeWithZ ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.FIT_TO_SIZE_WITH_Z) boolean flag ) {

        fitToSizeWithZ = flag;
        redraw ();

    }

    @Inject
    public void setOverlayGcodeMotionModeSeekColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_OVERLAY_SEEK) String rgbText ) {

        overlayGcodeMotionModeSeekColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setOverlayGcodeMotionModeLinearColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_OVERLAY_LINEAR) String rgbText ) {

        overlayGcodeMotionModeLinearColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @Inject
    public void setOverlayGcodeMotionModeArcColor ( Display display, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COLOR_OVERLAY_ARC) String rgbText ) {

        overlayGcodeMotionModeArcColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        redraw ();

    }

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true, true );

        final int cols = 12;
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

        canvasGC = new GC ( canvas );

        // create arrays here, because we need injected gcode service for this
        initWorkAreaPoints ();

        restorePersistedState ();

        redraw ();

    }

    private void redraw () {

        if ( canvas != null && !canvas.isDisposed () ) canvas.redraw ();

    }

    private void restorePersistedState () {

        final Map<String, String> persistedState = application.getPersistedState ();

        scale = toolbox.parseDouble ( persistedState.get ( IPersistenceKey.VIEW_SCALE ), 5.0 );

        double x = toolbox.parseDouble ( persistedState.get ( IPersistenceKey.VIEW_PIXEL_SHIFT + "X" ), 0.0 );
        double y = toolbox.parseDouble ( persistedState.get ( IPersistenceKey.VIEW_PIXEL_SHIFT + "Y" ), 0.0 );
        canvasShift = new Point ( x, y );

        rotX = toolbox.parseDouble ( persistedState.get ( IPersistenceKey.VIEW_ROTATION + "X" ), 0.0 );
        rotY = toolbox.parseDouble ( persistedState.get ( IPersistenceKey.VIEW_ROTATION + "Y" ), 0.0 );
        rotZ = toolbox.parseDouble ( persistedState.get ( IPersistenceKey.VIEW_ROTATION + "Z" ), 0.0 );

    }

    public void savePersistedState () {

        final Map<String, String> persistedState = application.getPersistedState ();

        persistedState.put ( IPersistenceKey.VIEW_SCALE, String.format ( IConstant.FORMAT_COORDINATE, scale ) );

        persistedState.put ( IPersistenceKey.VIEW_PIXEL_SHIFT + "X", String.format ( IConstant.FORMAT_COORDINATE, canvasShift.x ) );
        persistedState.put ( IPersistenceKey.VIEW_PIXEL_SHIFT + "Y", String.format ( IConstant.FORMAT_COORDINATE, canvasShift.y ) );

        persistedState.put ( IPersistenceKey.VIEW_ROTATION + "X", String.format ( IConstant.FORMAT_COORDINATE, rotX ) );
        persistedState.put ( IPersistenceKey.VIEW_ROTATION + "Y", String.format ( IConstant.FORMAT_COORDINATE, rotY ) );
        persistedState.put ( IPersistenceKey.VIEW_ROTATION + "Z", String.format ( IConstant.FORMAT_COORDINATE, rotZ ) );

    }

    private void initWorkAreaPoints () {

        if ( isWorkAreaOrigin0x0 ) {
            /* @formatter:off */
            workAreaPoints = new IGcodePoint [] { 
                    gcodeService.createGcodePoint ( workAreaMaxX, 0.0, 0.0 ), 
                    gcodeService.createGcodePoint ( workAreaMaxX, workAreaMaxY, 0.0 ), 
                    gcodeService.createGcodePoint ( 0.0, workAreaMaxY, 0.0 ), 
                    gcodeService.createGcodePoint ( 0.0, 0.0, 0.0 ),
            };
            /* @formatter:on */
        }
        else {
            /* @formatter:off */
            workAreaPoints = new IGcodePoint [] { 
                    gcodeService.createGcodePoint ( -workAreaMaxX, 0.0, 0.0 ), 
                    gcodeService.createGcodePoint ( -workAreaMaxX, -workAreaMaxY, 0.0 ), 
                    gcodeService.createGcodePoint ( 0.0, -workAreaMaxY, 0.0 ), 
                    gcodeService.createGcodePoint ( 0.0, 0.0, 0.0 ),
            };
            /* @formatter:on */
        }

        /* @formatter:off */
        workAreaCenterCrossEndPoints = new IGcodePoint [] { 
                gcodeService.createGcodePoint ( workAreaMaxX / 2 - workAreaCenterCrossLength, workAreaMaxY / 2, 0.0 ), 
                gcodeService.createGcodePoint ( workAreaMaxX / 2 + workAreaCenterCrossLength, workAreaMaxY / 2, 0.0 ), 
                gcodeService.createGcodePoint ( workAreaMaxX / 2, workAreaMaxY / 2 - workAreaCenterCrossLength, 0.0 ), 
                gcodeService.createGcodePoint ( workAreaMaxX / 2, workAreaMaxY / 2 + workAreaCenterCrossLength, 0.0 ),
        };
        /* @formatter:on */

    }

    private Point gcodeToCanvas ( IGcodePoint gcodePoint ) {

        return gcodeToCanvas ( scale, canvasShift, gcodePoint );

    }

    private Point gcodeToCanvas ( double zoom, Point shift, IGcodePoint gcodePoint ) {

        return pixelToCanvas ( gcodeToPixel ( zoom, gcodePoint ), shift );

    }

    private Point gcodeToPixel ( IGcodePoint gcodePoint ) {

        return gcodeToPixel ( scale, gcodePoint );

    }

    private Point gcodeToPixel ( double zoom, IGcodePoint gcodePoint ) {

        double x0 = zoom * gcodePoint.getX ();
        double y0 = zoom * gcodePoint.getY ();
        double z0 = zoom * gcodePoint.getZ ();

        // rotation around z
        double x1 = x0 * cosRotZ + y0 * sinRotZ;
        double y1 = x0 * -sinRotZ + y0 * cosRotZ;
        double z1 = z0;
        // rotation around y
        double x2 = x1 * cosRotY + z1 * -sinRotY;
        double y2 = y1;
        double z2 = x1 * sinRotY + z1 * cosRotY;
        // rotation around x
        double x3 = x2;
        double y3 = y2 * cosRotX + z2 * sinRotX;
        // double z3 = y2 * -sinRotX + z2 * cosRotX;

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

    @SuppressWarnings("unused")
    private Point canvasToPixel ( int x, int y ) {

        // return new Point ( x - canvasArea.x - pixelShift.x, -y + canvasArea.y + canvasArea.height - pixelShift.y );
        return canvasToPixelIncludingGcodeShift ( x, y ).sub ( canvasShift );

    }

    // pixel shift is included
    private Point canvasToPixelIncludingGcodeShift ( int x, int y ) {

        return new Point ( x - canvasArea.x, -y + canvasArea.y + canvasArea.height );

    }

    private void calculateRots () {
        
        sinRotX = Math.sin ( rotX );
        cosRotX = Math.cos ( rotX );
        sinRotY = Math.sin ( rotY );
        cosRotY = Math.cos ( rotY );
        sinRotZ = Math.sin ( rotZ );
        cosRotZ = Math.cos ( rotZ );

    }

    private void drawGcode ( GC gc, IGcodeProgram program ) {
    
        drawGcode ( gc, program, false );
    
    }

    private void drawGcode ( GC gc, IGcodeProgram program, boolean isOverlay ) {
    
        for ( IGcodeLine gcodeLine : program.getAllGcodeLines () ) {
    
            EGcodeMode gcodeMode = gcodeLine.getGcodeMode ();
    
            if ( gcodeMode != null ) {
    
                switch ( gcodeMode ) {
    
                    case MOTION_MODE_SEEK:
                        gc.setLineStyle ( SWT.LINE_SOLID );
                        gc.setLineWidth ( 1 );
                        gc.setForeground ( gcodeLine.isProcessed () ? gcodeProcessedColor : isOverlay ? overlayGcodeMotionModeSeekColor : gcodeMotionModeSeekColor );
                        drawLine ( gc, gcodeLine );
                        break;
                    case MOTION_MODE_LINEAR:
                        gc.setLineStyle ( SWT.LINE_SOLID );
                        gc.setLineWidth ( 1 );
                        gc.setForeground ( gcodeLine.isProcessed () ? gcodeProcessedColor : isOverlay ? overlayGcodeMotionModeLinearColor : gcodeMotionModeLinearColor );
                        drawLine ( gc, gcodeLine );
                        break;
                    case MOTION_MODE_CW_ARC:
                        gc.setLineStyle ( SWT.LINE_SOLID );
                        gc.setLineWidth ( 1 );
                        gc.setForeground ( gcodeLine.isProcessed () ? gcodeProcessedColor : isOverlay ? overlayGcodeMotionModeArcColor : gcodeMotionModeArcColor );
                        drawCircle ( gc, +1, gcodeLine );
                        break;
                    case MOTION_MODE_CCW_ARC:
                        gc.setLineStyle ( SWT.LINE_SOLID );
                        gc.setLineWidth ( 1 );
                        gc.setForeground ( gcodeLine.isProcessed () ? gcodeProcessedColor : isOverlay ? overlayGcodeMotionModeArcColor : gcodeMotionModeArcColor );
                        drawCircle ( gc, -1, gcodeLine );
                        break;
                    case MOTION_MODE_PROBE:
                        gc.setLineStyle ( SWT.LINE_DASH );
                        gc.setLineWidth ( 1 );
                        gc.setForeground ( gcodeMotionModeProbeColor );
                        drawLine ( gc, gcodeLine );
                        break;
    
                    case COMMENT:
                        break;
    
                    default:
                        // do nothing
                        break;
                }
    
            }
    
        }
    
    }

    private void drawLine ( GC gc, IGcodeLine gcodeLine ) {
    
        IGcodePoint start = gcodeLine.getStart ();
        IGcodePoint end = gcodeLine.getEnd ();
    
        if ( start.equals ( end ) ) return;
    
        if ( viewAltitude && gcodeProgram.isAutolevelScanComplete () ) {
            IGcodePoint [] path = gcodeLine.getAutoevelSegmentPath ();
            if ( path != null ) {
                for ( int i = 0; i < path.length - 1; i++ ) {
                    drawLine ( gc, path [i].add ( gcodeService.getFixtureShift () ), path [i + 1].add ( gcodeService.getFixtureShift () ) );
                }
            }
        }
        else {
            // translate to machine coordinates
            drawLine ( gc, start.add ( gcodeService.getFixtureShift () ), end.add ( gcodeService.getFixtureShift () ) );
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

    private void drawGantry ( GC gc ) {
    
        if ( gcodeState != null ) {
    
            gc.setLineStyle ( SWT.LINE_SOLID );
            gc.setLineWidth ( IConstant.GANTRY_LINE_WIDTH );
            gc.setForeground ( gantryColor );
    
            Point p = gcodeToCanvas ( gcodeState.getMachineCoordindates () );
            // The oval covers an area that is width + 1 pixels wide and height + 1 pixels tall.
            gc.drawOval ( (int) p.x - IConstant.GANTRY_RADIUS, (int) p.y - IConstant.GANTRY_RADIUS, 2 * IConstant.GANTRY_RADIUS, 2 * IConstant.GANTRY_RADIUS );
    
        }
    
    }

    private void drawGrid ( GC gc ) {
    
        LOG.trace ( "drawGrid:" );
    
        if ( !gcodeProgram.isAutolevelScanPrepared () ) return;
    
        gc.setLineStyle ( SWT.LINE_SOLID );
        gc.setLineWidth ( 1 );
        gc.setForeground ( autolevelGridColor );
    
        IGcodePoint gcodeShift = gcodeService.getFixtureShift ();
    
        int xlength = gcodeProgram.getXSteps () + 1;
        int ylength = gcodeProgram.getYSteps () + 1;
    
        for ( int i = 0; i < xlength; i++ ) {
            for ( int j = 0; j < ylength; j++ ) {
    
                IGcodePoint p1 = gcodeProgram.getProbePointAt ( i, j );
                if ( !viewAltitude ) p1 = p1.zeroAxis ( 'Z' );
                p1 = p1.add ( gcodeShift );
    
                if ( i + 1 < xlength ) {
                    IGcodePoint p2 = gcodeProgram.getProbePointAt ( i + 1, j );
                    if ( !viewAltitude ) p2 = p2.zeroAxis ( 'Z' );
                    drawLine ( gc, p1, p2.add ( gcodeShift ) );
                }
    
                if ( j + 1 < ylength ) {
                    IGcodePoint p2 = gcodeProgram.getProbePointAt ( i, j + 1 );
                    if ( !viewAltitude ) p2 = p2.zeroAxis ( 'Z' );
                    drawLine ( gc, p1, p2.add ( gcodeShift ) );
                }
    
            }
        }
    
    }

    private void drawOrigign ( GC gc, char type ) {
    
        final double r = 0.5;
    
        IGcodePoint zero = gcodeService.createGcodePoint ( 0.0, 0.0, 0.0 );
    
        Color color = machineOriginColor;
        if ( type == 'W' ) {
            zero = zero.add ( gcodeService.getFixtureShift () );
            color = workOriginColor;
        }
    
        IGcodePoint xDist = gcodeService.createGcodePoint ( r, 0.0, 0.0 );
        IGcodePoint yDist = gcodeService.createGcodePoint ( 0.0, r, 0.0 );
    
        Point p1 = gcodeToCanvas ( zero.sub ( xDist ) );
        Point p2 = gcodeToCanvas ( zero.add ( yDist ) );
        Point p3 = gcodeToCanvas ( zero.add ( xDist ) );
        Point p4 = gcodeToCanvas ( zero.sub ( yDist ) );
    
        gc.setLineStyle ( SWT.LINE_SOLID );
        gc.setLineWidth ( 2 );
        gc.setForeground ( color  );
    
        drawLine ( gc, p1, p2 );
        drawLine ( gc, p2, p3 );
        drawLine ( gc, p3, p4 );
        drawLine ( gc, p4, p1 );
    
        if ( type == 'M' ) {
            gc.setLineStyle ( SWT.LINE_DOT );
            gc.setLineWidth ( 1 );
            gc.setForeground ( color  );
            drawLine ( gc, zero, zero.addAxis ( 'Z', gcodeService.getFixtureShift () ) );
        }
    
    }

    private void drawWorkArea ( GC gc, char type ) {
    
        // draw work area
        gc.setLineStyle ( SWT.LINE_DOT );
        gc.setLineWidth ( 1 );
        gc.setForeground ( workareaBorderColor );
    
        IGcodePoint p1 = workAreaPoints[workAreaPoints.length - 1];
        if ( type == 'W' ) p1 = p1.addAxis ( 'Z', gcodeService.getFixtureShift () );
    
        for ( int i = 0; i < workAreaPoints.length; i++ ) {
            IGcodePoint p2 = workAreaPoints[i];
            if ( type == 'W' ) p2 = p2.addAxis ( 'Z', gcodeService.getFixtureShift () );
            drawLine ( gc, p1, p2 );
            p1 = p2;
        }
    
        // cross in the middle of work area
        gc.setLineStyle ( SWT.LINE_DASH );
        gc.setLineWidth ( 1 );
        gc.setForeground ( workareaMidcrossColor );
        drawLine ( gc, workAreaCenterCrossEndPoints[0].addAxis ( 'Z', gcodeService.getFixtureShift () ),
                workAreaCenterCrossEndPoints[1].addAxis ( 'Z', gcodeService.getFixtureShift () ) );
        drawLine ( gc, workAreaCenterCrossEndPoints[2].addAxis ( 'Z', gcodeService.getFixtureShift () ),
                workAreaCenterCrossEndPoints[3].addAxis ( 'Z', gcodeService.getFixtureShift () ) );
    
    }

    // all in plane xy
    private void drawCircle ( GC gc, double mult, IGcodeLine gcodeLine ) {
    
        // LOG.debug ( "drawCircle: gcodeLine=" + gcodeLine + " rotationAngle=" + gcodeProgram.getRotationAngle () );
    
        IGcodePoint start = gcodeLine.getStart ().add ( gcodeService.getFixtureShift () );
        IGcodePoint end = gcodeLine.getEnd ().add ( gcodeService.getFixtureShift () );
        double r = gcodeLine.getRadius ();
    
        if ( start.equals ( end ) ) return; // TODO this is may be wrong, this is a full circle
    
        // from grbl code
        final double x = end.getX () - start.getX ();
        final double y = end.getY () - start.getY ();
    
        final double dd = x * x + y * y;
        final double d = Math.sqrt ( dd );
        double hh = r * r - dd / 4;
        if ( hh < 0 ) hh = 0.0;
        double h = Math.sqrt ( hh );
    
        // G2, bei G3 + und - vor h alternieren
        double i = start.getX () + x / 2 + mult * h / d * y;
        double j = start.getY () + y / 2 - mult * h / d * x;
        // LOG.debug ( "drawCircle: dd=" + dd + " d=" + d + " hh=" + hh + " h=" + h + " i=" + i + " j=" + j );
        IGcodePoint center = gcodeService.createGcodePoint ( i, j, 0.0 );
    
        double startAngle = computeAngle ( start, center, r );
        double endAngle = computeAngle ( end, center, r );
        double arcAngle = endAngle - startAngle;
        if ( arcAngle < -180 ) arcAngle += 360;
        else if ( Math.abs ( mult * arcAngle - 180 ) == 0.0 ) arcAngle = -arcAngle;
        else if ( arcAngle > 180 ) arcAngle -= 360;
        // LOG.debug ( "drawCircle: startAngle=" + startAngle + " endAngle=" + endAngle + " arcAngle=" + arcAngle );
    
        // rotate all points by z angle
        // the 4 tangential points: north, west, south, east
        IGcodePoint north = gcodeService.createGcodePoint ( i, j + r, start.getZ () ).sub ( center ).rotate ( 'Z', -rotZ ).add ( center );
        IGcodePoint west = gcodeService.createGcodePoint ( i - r, j, start.getZ () ).sub ( center ).rotate ( 'Z', -rotZ ).add ( center );
        IGcodePoint south = gcodeService.createGcodePoint ( i, j - r, start.getZ () ).sub ( center ).rotate ( 'Z', -rotZ ).add ( center );
        IGcodePoint east = gcodeService.createGcodePoint ( i + r, j, start.getZ () ).sub ( center ).rotate ( 'Z', -rotZ ).add ( center );
    
        drawCircle ( gc, north, west, south, east, (int) (startAngle - (rotZ / IConstant.ONE_DEGREE)), (int) arcAngle );
        // LOG.debug ( "drawCircle: ====================================" );
    
    }

    private void drawCircle ( GC gc, IGcodePoint pointN, IGcodePoint pointW, IGcodePoint pointS, IGcodePoint pointE, int startAngle, int arcAngle ) {
    
        // LOG.debug ( "drawCircle: north=" + pointN + " west=" + pointW + " south=" + pointS + " east=" + pointE );
    
        Point pN = gcodeToCanvas ( pointN );
        Point pW = gcodeToCanvas ( pointW );
        Point pS = gcodeToCanvas ( pointS );
        Point pE = gcodeToCanvas ( pointE );
    
        // The resulting arc begins at startAngle and extends for arcAngle degrees, using the current color. Angles are interpreted such that 0 degrees is at the 3 o'clock
        // position. A positive value indicates a counter-clockwise rotation while a negative value indicates a clockwise rotation.
        // The center of the arc is the center of the rectangle whose origin is (x, y) and whose size is specified by the width and height arguments.
        // The resulting arc covers an area width + 1 pixels wide by height + 1 pixels tall.
    
        int w = (int) pE.x - (int) pW.x;
        int h = (int) pS.y - (int) pN.y;
        int x = (int) pN.x - w / 2;
        int y = (int) pW.y - h / 2;
    
        // LOG.debug ( "drawCircle: x=" + x + " y=" + y + " h=" + h + " w=" + w );
    
        gc.drawArc ( x, y, w, h, startAngle, arcAngle );
    
    }

    private double computeAngle ( IGcodePoint p, IGcodePoint center, double radius ) {
    
        final double PRECISION = 1000.0;

        double cosAngle = (p.getX () - center.getX ()) / radius;
        double angle = Math.acos ( cosAngle ) / IConstant.ONE_DEGREE;
    
        if ( p.getY () < center.getY () ) {
            angle = 360 - angle;
        }
    
        return ((int) (angle * PRECISION + 0.5)) / PRECISION; // HACK round to 3 decimals
    
    }

    private class Painter implements PaintListener {

        @Override
        public void paintControl ( PaintEvent evt ) {

            LOG.trace ( "paintControl: gcode=" + viewGcode + " grid=" + viewGrid + " alt=" + viewAltitude );

            canvasArea = canvas.getClientArea ();
            final int w = canvasArea.width;
            final int h = canvasArea.height;

            calculateRots ();

            scaleLabel.setText ( String.format ( IConstant.FORMAT_SCALE, scale ) );
            pixelShiftLabel.setText ( "" + canvasShift );
            String rotXs = String.format ( IConstant.FORMAT_SCALE, rotX / IConstant.ONE_DEGREE );
            String rotYs = String.format ( IConstant.FORMAT_SCALE, rotY / IConstant.ONE_DEGREE );
            String rotZs = String.format ( IConstant.FORMAT_SCALE, rotZ / IConstant.ONE_DEGREE );
            rotationLabel.setText ( "[" + rotXs + "," + rotYs + "," + rotZs + "]" );

            // Double Buffering
            // http://www.eclipsezone.com/eclipse/forums/t24142.html
            Image bufferImage = new Image ( display, w, h );
            GC gc = new GC ( bufferImage );

            drawCooridnateSystemArrows ( gc );
            // drawWorkArea ( gc, 'M' ); // TODO to decide: control this by a preference?
            if ( viewWorkarea ) drawWorkArea ( gc, 'W' );
            drawOrigign ( gc, 'M' );
            drawOrigign ( gc, 'W' );
            if ( gcodeProgram != null && viewGrid ) drawGrid ( gc );
            if ( gcodeProgram != null && viewGcode ) {
                drawGcode ( gc, gcodeProgram );
                if ( overlayGcodeProgram != null && gcodeProgram != overlayGcodeProgram ) {
                    drawGcode ( gc, overlayGcodeProgram, true );
                }
            }

            // Double Buffering
            evt.gc.drawImage ( bufferImage, 0, 0 );
            // draw gantry only on canvas
            drawGantry ( evt.gc );

            Image oldGcodeImage = gcodeImage;
            GC oldGcodeImageGC = gcodeImageGC;
            gcodeImage = bufferImage;
            gcodeImageGC = gc;
            if ( oldGcodeImage != null ) oldGcodeImage.dispose ();
            if ( oldGcodeImageGC != null ) oldGcodeImageGC.dispose ();

        }

        private void drawCooridnateSystemArrows ( GC gc ) {

            final IGcodePoint origin = gcodeService.createGcodePoint ( 0.0, 0.0, 0.0 );
            final double factor = 30.0;
            final Point shift = new Point ( 40.0, 40.0 );
            /* @formatter:off */
            final IGcodePoint [] v = new IGcodePoint [] { 
                    gcodeService.createGcodePoint ( 1.0, 0.0, 0.0 ), 
                    gcodeService.createGcodePoint ( 0.0, 1.0, 0.0 ), 
                    gcodeService.createGcodePoint ( 0.0, 0.0, 1.0 ) 
            };
            // TODO_PREF make it static
            /* @formatter:on */

            gc.setLineStyle ( SWT.LINE_SOLID );
            gc.setLineWidth ( 1 );

            Point p0 = gcodeToCanvas ( factor, shift, origin );

            for ( int i = 0; i < v.length; i++ ) {
                gc.setForeground ( display.getSystemColor ( IConstant.COORDINATE_SYSTEM_ARROW_COLORS[i + 1] ) );
                drawLine ( gc, p0, gcodeToCanvas ( factor, shift, v[i] ) );
                Point p = gcodeToCanvas ( 1.3 * factor, shift, v[i] );
                gc.setForeground ( display.getSystemColor ( IConstant.COORDINATE_SYSTEM_ARROW_COLORS[0] ) );
                org.eclipse.swt.graphics.Point extent = gc.textExtent ( IConstant.AXIS[i] );
                gc.drawString ( IConstant.AXIS [i], (int) p.x - extent.x / 2, (int) p.y - extent.y / 2, true );
            }

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

            scale += evt.count / 1; // TODO_PREF to preferences
            if ( scale < 1.0 ) scale = 1.0;

            // savePersistedState ();
            redraw ();

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
            // final int y = clientArea.height - evt.y - clientArea.y;
            final int y = evt.y - clientArea.y;
            mouseCoordinateLabel.setText ( "[" + x + "," + y + "]" );

            redraw ();

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

                // savePersistedState ();
                redraw ();

            }
            // right double eliminates shift
            else if ( evt.button == 3 && evt.stateMask == 0 ) {

                canvasShift = new Point ( 0.0, 0.0 );
                // initPixelShift ();

                rotX = 0.0;
                rotY = 0.0;
                rotZ = 0.0;

                scale = 5.0;

                // savePersistedState ();
                redraw ();

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

                redraw ();
            }
            else if ( rotate ) {
                Point p = canvasToPixelIncludingGcodeShift ( evt.x, evt.y );
                rotX += -(lastPoint.y - p.y) * IConstant.ONE_DEGREE / 4;
                rotZ += -(lastPoint.x - p.x) * IConstant.ONE_DEGREE / 4;
                lastPoint = p;

                redraw ();
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

            // savePersistedState ();

        }

        @Override
        public void keyPressed ( KeyEvent evt ) {

            LOG.trace ( "keyPressed: evt=" + evt );

            if ( evt.character == '\0' ) {

                switch ( evt.keyCode ) {
                    case 0x1000001: // up
                        canvasShift.y += 10;
                        // savePersistedState ();
                        redraw ();
                        break;

                    case 0x1000002: // down
                        canvasShift.y -= 10;
                        // savePersistedState ();
                        redraw ();
                        break;

                    case 0x1000003: // left
                        canvasShift.x -= 10;
                        // savePersistedState ();
                        redraw ();
                        break;

                    case 0x1000004: // right
                        canvasShift.x += 10;
                        // savePersistedState ();
                        redraw ();
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
    public void stateUpdateNotified ( @UIEventTopic(IEvent.UPDATE_STATE) IGcodeGrblState state ) {

        LOG.debug ( "stateUpdateNotified: state=" + state );
        
        if ( gcodeState != null && gcodeImage != null ) {

            // restore gsntry area on old position
            Point p = gcodeToCanvas ( gcodeState.getMachineCoordindates () );
            final int x = (int) p.x - IConstant.GANTRY_LINE_WIDTH - IConstant.GANTRY_RADIUS;
            final int y = (int) p.y - IConstant.GANTRY_LINE_WIDTH - IConstant.GANTRY_RADIUS;

            gcodeImageGC.copyArea ( gantryImage, x, y );
            canvasGC.drawImage ( gantryImage, x, y );

        }

        gcodeState = state;

        // draw gantry on new position
        drawGantry ( canvasGC );

    }

    @Inject
    @Optional
    public void playerLineNotified ( @UIEventTopic(IEvent.PLAYER_LINE) IGcodeLine gcodeLine ) {

        LOG.trace ( "playerLineNotified: gcodeLine=" + gcodeLine );

        // draw into gcodeImage and then transfer to canvas
        if ( gcodeLine.isMotionMode () ) {

            gcodeImageGC.setLineStyle ( SWT.LINE_SOLID );
            gcodeImageGC.setLineWidth ( 1 );
            gcodeImageGC.setForeground ( gcodeProcessedColor );
            drawLine ( gcodeImageGC, gcodeLine );

            canvasGC.setLineStyle ( SWT.LINE_SOLID );
            canvasGC.setLineWidth ( 1 );
            canvasGC.setForeground ( gcodeProcessedColor );
            drawLine ( canvasGC, gcodeLine );

        }

    }

    @Inject
    @Optional
    public void programLoadedNotified ( @UIEventTopic(IEvent.GCODE_PROGRAM_LOADED) String fileName ) {

        LOG.debug ( "programLoadedNotified: fileName=" + fileName );

        redraw ();

    }

    @Inject
    @Optional
    public void programOptimizedNotified ( @UIEventTopic(IEvent.GCODE_PROGRAM_OPTIMIZED) String fileName ) {

        LOG.debug ( "programOptimizedNotified: fileName=" + fileName );

        redraw ();

    }

    @Inject
    @Optional
    public void redrawNotified ( @UIEventTopic(IEvent.REDRAW) Object dummy ) {

        LOG.debug ( "redrawScanGridNotified: dummy=" + dummy );

        redraw ();

    }

    @Inject
    @Optional
    public void updateCoordSelectOffsetsNotified ( @UIEventTopic(IEvent.UPDATE_FIXTURE_OFFSET) IGcodePoint fixtureOffset ) {

        LOG.debug ( "updateCoordSelectOffsetsNotified: fixtureOffset=" + fixtureOffset );

        redraw ();

    }

    @Inject
    @Optional
    public void updateProbeNotified ( @UIEventTopic(IEvent.AUTOLEVEL_UPDATE) IGcodePoint probe ) {

        LOG.trace ( "updateProbeNotified: probe=" + probe );

        if ( gcodeService.isAutolevelScan () ) redraw ();

    }

    public Image getGcodeImage () {

        return gcodeImage;

    }

}
