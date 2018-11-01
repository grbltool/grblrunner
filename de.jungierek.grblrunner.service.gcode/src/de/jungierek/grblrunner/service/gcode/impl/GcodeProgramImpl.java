package de.jungierek.grblrunner.service.gcode.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class GcodeProgramImpl implements IGcodeProgram {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeProgramImpl.class );

    public final static IGcodePoint GCODE_DEFAULT_START_POINT = new GcodePointImpl ( 0.0, 0.0, 0.0 );

    // never use field injection, because if a gcode editor is closed this gives a exception (uninject, not resolved ...)
    // @Inject
    private IEventBroker eventBroker;

    private File gcodeFile, probeDataFile;

    private int nextLineNo = 0;
    private List<GcodeLineImpl> gcodeLines = new ArrayList<> ( 100 );

    private GcodePointImpl min;
    private GcodePointImpl max;

    private double durationInMinutes;

    private GcodePointImpl matrix [][];
    private int xSteps = IConstant.INITIAL_XSTEPS;
    private int ySteps = IConstant.INITIAL_YSTEPS;
    private double xStepWidth, yStepWidth;
    private int numProbePoints;

    private volatile boolean scanDataComplete = false;

    private double rotationAngle = 0;

    private boolean playing;
    private boolean scanning;

    boolean optimized = false;
    
    // preference
    private double seekFeedrate;

    private double accelaration; // mm / s^2
    private double timeToFeedrate; // mm / s
    double distanceToFeedrate; // mm

    @Inject
    public void setSeekFeedrate ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.MAX_SEEK_FEEDRATE) int feedrate ) {

        LOG.debug ( "setSeekFeedrate: feedrate=" + feedrate );

        seekFeedrate = feedrate;
        calculateDurationVars ();

    }

    @Inject
    public void setAccelaration ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.ACCELARATION) double accelaration ) {

        LOG.debug ( "setAccelaration: accelaration=" + accelaration );

        this.accelaration = accelaration;
        calculateDurationVars ();

    }

    private void calculateDurationVars () {

        timeToFeedrate = seekFeedrate / 60 / accelaration; // sec

        // s = a/2 *t^2
        // t = sqrt ( s / a/2 ) = sqrt ( 2s / a ) with s = dist/2
        distanceToFeedrate = 0.5 * accelaration * timeToFeedrate * timeToFeedrate; // mm

        LOG.debug ( "calculateDurationVars: seekFeedrate=" + seekFeedrate + "mm/min accelaration=" + accelaration + "mm/s^2 timeToFeedrate=" + timeToFeedrate
                + "s distanceToFeedrate="
                + distanceToFeedrate + "mm" );
    }

    // for test
    public GcodeProgramImpl () {}

    @Inject
    public GcodeProgramImpl ( IEventBroker eventBroker ) {
        
        LOG.trace ( "GcodeProgramImpl: hash=" + Integer.toHexString ( hashCode () ) );
        
        this.eventBroker = eventBroker;

    }

    @Override
    public File getGcodeProgramFile () {

        return gcodeFile;

    }

    @Override
    public File getAutolevelDataFile () {

        return probeDataFile;

    }

    @Override
    public void appendLine ( String line ) {

        gcodeLines.add ( new GcodeLineImpl ( nextLineNo++, line ) );

    }
    
    @Override
    public IGcodeLine [] getAllGcodeLines () {

        return gcodeLines.toArray ( new IGcodeLine [gcodeLines.size ()] );

    }

    @Override
    public IGcodePoint getMin () {

        return min;

    }

    @Override
    public IGcodePoint getMax () {

        return max;

    }

    @Override
    public int getDuration () { // min

        return (int) durationInMinutes;

    }

    @Override
    public void loadGcodeProgram ( File gcodeFile ) {

        this.gcodeFile = gcodeFile;
        String fileName = gcodeFile.getPath ();
        probeDataFile = new File ( fileName.substring ( 0, fileName.lastIndexOf ( '.' ) ) + IConstant.AUTOLEVEL_DATA_FILE_EXTENSION );

        // decouple from UI thread
        new GcodeLoaderThread ( gcodeFile ).start ();

    }

    @Override
    public String getGcodeProgramName () {

        final String name = gcodeFile.getName ();
        return name.substring ( 0, name.lastIndexOf ( '.' ) );

    }

    @Override
    public void clear () {

        nextLineNo = 0;
        // gcodeLines.clear ();
        gcodeLines = new ArrayList<> ( 100 );

        rotationAngle = 0.0;
        optimized = false;

        clearAutolevelData ();

    }

    @Override
    public double getRotationAngle () {

        return rotationAngle / IConstant.ONE_DEGREE;

    }

    @Override
    public int getLineCount () {

        return gcodeLines.size ();

    }

    @Override
    public void resetProcessed () {

        for ( IGcodeLine gcodeLine : getAllGcodeLines () ) {
            gcodeLine.setProcessed ( false );
        }

    }

    // Ein Segemnt beginnt mit einem GO move in xy
    // dann kommt ein G1 z move mit Ziel <=0
    // danach g1 moves in xy
    // letzter move in z nach >0

    // auf jeden fall prefix und suffix segemnt merken

    private class GcodeSegment {

        public final List<GcodeLineImpl> gcodeLines = new ArrayList<> ( 100 );

        public final double x;
        public final double y;

        public GcodeSegment ( double x, double y ) {

            this.x = x;
            this.y = y;

        }
        
        public void append ( GcodeLineImpl line ) {

            gcodeLines.add ( line );

        }

        private void append ( GcodeSegment segment ) {

            this.gcodeLines.addAll ( segment.gcodeLines );

        }

    }

    @Override
    public boolean isOptimized () {

        return optimized;

    }

    @Override
    public void optimize () {

        LOG.debug ( "optimize:" );

        final boolean firstAndLastInSegments = true;

        // 1) divide gcode in segments, each segment ends at the same point as it starts
        List<GcodeSegment> segments = new ArrayList<> ( 100 );
        GcodeSegment segment = new GcodeSegment ( 0.0, 0.0 ); // TODO start to pref
        GcodeSegment firstSegment = null;
        GcodeSegment lastSegment = null;

        for ( GcodeLineImpl line : gcodeLines ) {
            
            LOG.trace ( "optimize: line=" + line );

            if ( line.isMotionModeSeek () ) {

                final double x = line.getEnd ().getX ();
                final double y = line.getEnd ().getY ();

                if ( line.isMoveInZ () && firstSegment != null ) {
                    final double z = line.getEnd ().getZ ();
                    if ( z > 5.0 ) {
                        LOG.trace ( "optimize: last segment z=" + z );
                        // segment.append ( new GcodeLineImpl ( -1, "(-------------------)" ) );
                        segments.add ( segment );
                        segment = new GcodeSegment ( x, y );
                        lastSegment = segment;
                    }
                }
                else {
                    if ( segment.x != x || segment.y != y ) {
                        LOG.trace ( "optimize: segment (" + x + "," + y + ")" );
                        // segment.append ( new GcodeLineImpl ( -1, "(-------------------)" ) );
                        if ( firstSegment == null ) {
                            firstSegment = segment;
                            if ( firstAndLastInSegments ) segments.add ( segment );
                        }
                        else {
                            segments.add ( segment );
                        }
                        segment = new GcodeSegment ( x, y );
                    }
                }
            }

            segment.append ( line );

        }

        if ( firstAndLastInSegments ) segments.add ( segment );

        // algorithm from https://hackaday.io/project/4955-g-code-optimization

        // 2) optimze path
        int cities = segments.size ();
        GcodeSegment [] segmentArray = segments.toArray ( new GcodeSegment [cities] );

        double [][] distance = new double [cities] [cities];
        for ( int i = 0; i < cities; i++ ) {
            for ( int j = 0; j < cities; j++ ) {
                final double dx = segmentArray[i].x - segmentArray[j].x; // no abs necessary
                final double dy = segmentArray[i].y - segmentArray[j].y; // no abs necessary
                final double dist = Math.sqrt ( dx * dx + dy * dy );
                // final double dist = dx * dx + dy * dy;
                distance[i][j] = dist; // TODO sqrt???
            }
        }
        
        int [] path = new int [cities];
        for ( int i = 0; i < cities; i++ ) {
            path[i] = i;
        }

        boolean lookForBetterPath = true;
        while ( lookForBetterPath ) {

            lookForBetterPath = false;

            START: for ( int i = 0; i < cities - 3; i++ ) {
                for ( int j = i + 2; j < cities - 1; j++ ) {
                    // System.out.println ( "i=" + i + " j=" + j );
                    double oldLength = distance[path[i]][path[i + 1]] + distance[path[j]][path[j + 1]];
                    double newLength = distance[path[i]][path[j]] + distance[path[i + 1]][path[j + 1]];
                    if ( newLength < oldLength ) {
                        // reorganize path by inverting the order between i+1 and j
                        for ( int x = 0; x < (j - i) / 2; x++ ) {
                            int temp = path[i + 1 + x];
                            path[i + 1 + x] = path[j - x];
                            path[j - x] = temp;
                        }
                        lookForBetterPath = true;
                        break START;
                    }
                }
            }

        }

        // 3) flatenize all segments
        List<GcodeLineImpl> optimizedGcodeLines = new ArrayList<> ( 100 );
        if ( !firstAndLastInSegments ) optimizedGcodeLines.addAll ( firstSegment.gcodeLines );
        for ( int i = 0; i < cities; i++ ) {
            LOG.trace ( "optimize: path[" + i + "]=" + path[i] );
            optimizedGcodeLines.addAll ( segmentArray[path[i]].gcodeLines );
        }
        if ( !firstAndLastInSegments ) optimizedGcodeLines.addAll ( lastSegment.gcodeLines );
        gcodeLines = optimizedGcodeLines;

        // 4) adjust start end points of every line
        parse ();

        // 5) some administration
        optimized = true;
        eventBroker.send ( IEvent.GCODE_PROGRAM_OPTIMIZED, gcodeFile.getPath () );

    }

    @Override
    public void rotate ( double angle ) {

        LOG.debug ( "rotate: angle=" + angle );

        if ( this.rotationAngle == angle * IConstant.ONE_DEGREE ) return;

        this.rotationAngle = angle * IConstant.ONE_DEGREE;

        parse (); // reset line vars

        initMinMax ();

        IGcodePoint lastEnd = null;
        for ( IGcodeLine gcodeLine : getAllGcodeLines () ) {
            gcodeLine.rotate ( rotationAngle, lastEnd );
            if ( gcodeLine.isMotionMode () ) {
                lastEnd = gcodeLine.getEnd ();
                handleMinMax ( gcodeLine );
            }
        }

    }

    @Override
    public void parse () {

        initMinMax ();

        IGcodePoint lastEndPoint = GCODE_DEFAULT_START_POINT;
        EGcodeMode lastMotionMode = EGcodeMode.MOTION_MODE_SEEK;
        double lastRadius = 0.0;
        int lastFeedrate = 0;

        for ( IGcodeLine gcodeLine : getAllGcodeLines () ) {

            gcodeLine.parseGcode ( lastMotionMode, lastEndPoint, lastRadius, lastFeedrate );

            if ( gcodeLine.isMotionMode () ) {

                lastMotionMode = gcodeLine.getGcodeMode ();
                lastEndPoint = gcodeLine.getEnd ();
                lastFeedrate = gcodeLine.getFeedrate ();

                if ( gcodeLine.isMotionModeArc () ) lastRadius = gcodeLine.getRadius ();

                handleMinMax ( gcodeLine );

            }

        }

    }

    private void initMinMax () {

        min = new GcodePointImpl ( IConstant.PREFERENCE_DOUBLE_MAX, IConstant.PREFERENCE_DOUBLE_MAX, IConstant.PREFERENCE_DOUBLE_MAX );
        max = new GcodePointImpl ( IConstant.PREFERENCE_DOUBLE_MIN, IConstant.PREFERENCE_DOUBLE_MIN, IConstant.PREFERENCE_DOUBLE_MIN );

        durationInMinutes = 0;

    }

    private void handleMinMax ( IGcodeLine gcodeLine ) {

        if ( gcodeLine.isMoveInXY () ) {

            IGcodePoint point = gcodeLine.getEnd ();

            LOG.trace ( "parseGcode: min=" + min + " max=" + max + " lastend=" + point );
            min = (GcodePointImpl) min.min ( point );
            max = (GcodePointImpl) max.max ( point );

        }

        durationInMinutes += computeDuration ( gcodeLine );

    }
    
    private double computeDuration ( IGcodeLine gcodeLine ) {

        IGcodePoint start = gcodeLine.getStart ();
        IGcodePoint end = gcodeLine.getEnd ();

        final double dx = Math.abs ( end.getX () - start.getX () );
        final double dy = Math.abs ( end.getY () - start.getY () );
        final double dz = Math.abs ( end.getZ () - start.getZ () );
        final double dist = Math.sqrt ( dx * dx + dy * dy + dz * dz );

        // double feedrate = IPreferences.MAX_SEEK_FEEDRATE;
        double time = 0.0;
        if ( gcodeLine.isMotionModeLinear () || gcodeLine.isMotionModeArc () ) {
            final double feedrate = gcodeLine.getFeedrate ();
            if ( feedrate != 0 ) time = dist / feedrate;
        }
        else if ( gcodeLine.isMotionModeSeek () ) {

            if ( 2 * distanceToFeedrate < dist ) {
                time = 2 * timeToFeedrate / 60.0 + (dist - 2 * distanceToFeedrate) / seekFeedrate;
            }
            else {
                time = 2 * Math.sqrt ( dist / accelaration ) / 60.0;
            }
            // LOG.info ( "computeDuration: dist=" + dist + " time=" + time + "min" );
            // if ( dist == 12.0 ) LOG.info ( "computeDuration: dist=" + dist + " time=" + 60 * time + "s" );

        }

        // LOG.info ( "computeDuration: l=" + gcodeLine + " s=" + start + " e=" + end + " d=" + dist + " t=" + time );
        return time;

    }

    @Override
    public boolean isLoaded () {

        return gcodeLines.size () > 0;

    }

    @Override
    public void setPlayerStart () {

        this.playing = true;

    }

    @Override
    public void setPlayerStop () {

        this.playing = false;

    }

    @Override
    public boolean isPlaying () {

        return playing;

    }

    @Override
    public void setAutolevelScanCompleted () {
    
        computeAutlevelSegments ();
        scanDataComplete = true;
    
    }

    @Override
    public boolean isAutolevelScanComplete () {
    
        return scanDataComplete;
    
    }

    @Override
    public void loadAutolevelData () {

        // decouple from UI thread
        new ProbeLoaderThread ( probeDataFile ).start ();

    }

    @Override
    public void saveAutolevelData () {

        // gcodeProgram = program;

        if ( !isAutolevelScanComplete () ) return;

        // decouple from UI thread
        new ProbeSaverThread ( probeDataFile ).start ();

    }

    @Override
    public void clearAutolevelData () {

        scanDataComplete = false;
        matrix = null;

        if ( probeDataFile != null ) {
            eventBroker.send ( IEvent.AUTOLEVEL_DATA_CLEARED, probeDataFile.getPath () );
        }

    }

    @Override
    public boolean isAutolevelScanPrepared () {

        return matrix != null;

    }

    @Override
    public void prepareAutolevelScan ( int xSteps, int ySteps ) {

        if ( getLineCount () == 0 ) return;

        LOG.debug ( "prepareAutolevelScan: matrix=" + matrix + " xSteps=" + xSteps + " ySteps=" + ySteps );

        scanDataComplete = false;

        if ( matrix != null && this.xSteps == xSteps && this.ySteps == ySteps ) return;

        this.xSteps = xSteps;
        this.ySteps = ySteps;

        prepareAutolevelScan ();

    }

    @Override
    public void prepareAutolevelScan () {

        xStepWidth = (max.getX () - min.getX ()) / xSteps;
        yStepWidth = (max.getY () - min.getY ()) / ySteps;

        numProbePoints = (xSteps + 1) * (ySteps + 1);

        if ( matrix == null || xSteps != matrix.length || ySteps != matrix[0].length ) {
            matrix = new GcodePointImpl [this.xSteps + 1] [this.ySteps + 1];
        }

        clearZ ();

    }

    @Override
    public void setAutolevelStart () {

        scanning = true;

    }

    @Override
    public void setAutolevelStop () {

        scanning = false;

    }

    @Override
    public boolean isAutolevelScan () {

        return scanning;

    }

    private void clearZ () {

        for ( int i = 0; i < matrix.length; i++ ) {
            double x = min.getX () + i * xStepWidth;
            for ( int j = 0; j < matrix[i].length; j++ ) {
                double y = min.getY () + j * yStepWidth;
                matrix[i][j] = new GcodePointImpl ( x, y, 0.0 );
            }
        }

    }

    // coordinates are in working coordinates!!!
    @Override
    public void setProbePoint ( IGcodePoint probe ) {

        final GcodePointImpl p = (GcodePointImpl) probe;

        final double distx = p.x - min.x + IConstant.EPSILON;
        final double ii = distx / this.xStepWidth;
        int i = (int) ii;

        final double disty = p.y - min.y + IConstant.EPSILON;
        final double jj = disty / this.yStepWidth;
        int j = (int) jj;

        LOG.debug ( "setProbePoint: dx=" + distx + " dy=" + disty + "  ii=" + ii + " i=" + i + " jj=" + jj + " j=" + j );

        matrix[i][j] = p;

    }

    @Override
    public IGcodePoint getProbePointAt ( int ix, int iy ) {

        return matrix[ix][iy];

    }

    @Override
    public int getXSteps () {
        return xSteps;
    }

    @Override
    public int getYSteps () {
        return ySteps;
    }

    @Override
    public double getStepWidthX () {

        return xStepWidth;

    }

    @Override
    public double getStepWidthY () {

        return yStepWidth;

    }

    @Override
    public int getNumProbePoints () {

        return numProbePoints;

    }

    private GcodePointImpl interpolate ( IGcodePoint point ) {

        GcodePointImpl p = (GcodePointImpl) point;

        return new GcodePointImpl ( p.x, p.y, interpolateZ ( point ) );

    }

    private double interpolateZ ( IGcodePoint point ) {

        // TODO extract computing of indizes in separate Methods, see also setProbePoint

        GcodePointImpl p = (GcodePointImpl) point;

        double result = p.z; // the interpolated z for the point

        double ii = (p.x - min.x) / xStepWidth + IConstant.EPSILON;
        int i = (int) ii;
        if ( i < 0 ) i = 0;
        if ( i >= xSteps ) i = xSteps - 1;

        double jj = (p.y - min.y) / yStepWidth + IConstant.EPSILON;
        int j = (int) jj;
        if ( j < 0 ) j = 0;
        if ( j >= ySteps ) j = ySteps - 1;

        double a = ii - i;
        double b = jj - j;
        double a1 = 1.0 - a;
        double b1 = 1.0 - b;

        // LOG.trace ( "xSteps=" + xSteps + " ySteps=" + ySteps + " ii=" + ii + " i=" + i + " jj=" + jj + " j=" + j );
        result += a1 * b1 * matrix[i][j].z;
        result += a1 * b * matrix[i][j + 1].z;
        result += a * b1 * matrix[i + 1][j].z;
        result += a * b * matrix[i + 1][j + 1].z;

        return result;

    }

    private double subDouble ( double d1, double d2 ) {

        double result = d1 - d2;
        if ( Math.abs ( result ) <= IConstant.EPSILON ) {
            result = 0.0;
        }

        return result;

    }

    public IGcodePoint [] interpolateLine ( IGcodePoint point1, IGcodePoint point2 ) {

        ArrayList<IGcodePoint> result = new ArrayList<> ();

        GcodePointImpl p1 = (GcodePointImpl) point1;
        GcodePointImpl p2 = (GcodePointImpl) point2;

        // first point of path
        result.add ( interpolate ( p1 ) );

        final double dx = subDouble ( p2.x, p1.x );
        final double dy = subDouble ( p2.y, p1.y );
        final double dz = subDouble ( p2.z, p1.z );

        if ( dx == 0.0 && dy == 0 ) { // only a z move
            result.add ( interpolate ( p2 ) );
            return result.toArray ( new IGcodePoint [2] );
        }

        double ii1 = (p1.x - min.x + IConstant.EPSILON / 2) / xStepWidth;
        final int i1 = (int) ii1;
        ii1 -= i1;

        double jj1 = (p1.y - min.y + IConstant.EPSILON / 2) / yStepWidth;
        final int j1 = (int) jj1;
        jj1 -= j1;

        double ii2 = (p2.x - min.x + IConstant.EPSILON / 2) / xStepWidth;
        int i2 = (int) ii2;
        ii2 -= i2;

        double jj2 = (p2.y - min.y + IConstant.EPSILON / 2) / yStepWidth;
        int j2 = (int) jj2;
        jj2 -= j2;

        double llXY = dx * dx + dy * dy;
        double distXY = Math.sqrt ( llXY );

        double llXYZ = llXY + dz * dz;
        double distXYZ = Math.sqrt ( llXYZ );

        final double cosX = dx / distXY;
        final double cosY = dy / distXY;
        final double cosZ = dz * distXY / distXYZ; // correction for the slope in z vs. the travel in xy

        // if ( Math.abs ( dx ) < 1e-10 ) dx = 0.0;
        // if ( Math.abs ( dy ) < 1e-10 ) dy = 0.0;
        // if ( Math.abs ( dx ) <= EPSILON ) {cosX = 0.0;
        // if ( Math.abs ( dy ) <= EPSILON ) cosY = 0.0;

        // potential next intersection
        // i and j are the indices of the cross left down from p1
        int i = i1;
        if ( dx > 0.0 ) {
            i++;
            if ( Math.abs ( ii2 ) > IConstant.EPSILON ) i2++;
        }
        else if ( dx < 0.0 ) {
            if ( Math.abs ( ii1 ) <= IConstant.EPSILON ) i--;
        }

        int j = j1;
        if ( dy > 0.0 ) {
            j++;
            if ( Math.abs ( jj2 ) > IConstant.EPSILON ) j2++;
        }
        else if ( dy < 0.0 ) {
            if ( Math.abs ( jj1 ) <= IConstant.EPSILON ) j--;
        }

        double x = p1.x;
        double y = p1.y;
        double z = p1.z;

        double xn = x;
        double yn = y;

        double tx = 1e10;
        double ty = 1e10;

        // @formatter:off
        // if dx == 0 && dy == 0 both areas collapsed, but this condition is returned at beginning
        while ( 
                isInsideArea ( dx, i, i1, i2 ) && isInsideArea ( dy, j, j1, j2 ) 
                || isInsideArea ( dx, i, i1, i2 ) && isInsideCollapsedArea ( dy, j, j1, j2 ) 
                || isInsideCollapsedArea ( dx, i, i1, i2 ) && isInsideArea ( dy, j, j1, j2 ) 
                || isInsideArea ( dx, i, i1, i2 ) && isInsideLastCell ( dy, j, j1, j2 ) 
                || isInsideLastCell ( dx, i, i1, i2 ) && isInsideArea ( dy, j, j1, j2 ) 
              ) {
       // @formatter:on

            // HACK delete later
            if ( IConstant.AUTOLEVEL_ITERATION_LIMIT_CHECK && Math.abs ( i ) + Math.abs ( j ) > IConstant.AUTOLEVEL_ITERATION_LIMIT_COUNT ) {
                LOG.error ( "iteration limit reached dx=" + dx + " i=" + i + " i2=" + i2 + " dy=" + dy + " j=" + j + " j2=" + j2 + " p1=" + p1 + " p2=" + p2 );
                break; // HACK HACK
            }

            if ( dx != 0.0 ) {
                xn = min.x + i * xStepWidth; // like matrix [i][j].x
                tx = (xn - p1.x) / cosX; // distXY from p1 to pn by moving x
            }

            if ( dy != 0.0 ) {
                yn = min.y + j * yStepWidth;
                ty = (yn - p1.y) / cosY; // distXY from p1 to pn by moving y
            }

            if ( Math.abs ( tx ) == Math.abs ( ty ) ) {
                x = xn;
                y = yn;
                z = p1.z + tx * cosZ;
                if ( dx > 0.0 ) i++;
                else if ( dx <= 0.0 ) i--;
                if ( dy > 0.0 ) j++;
                else if ( dy <= 0.0 ) j--;
            }
            else if ( Math.abs ( tx ) < Math.abs ( ty ) ) {
                x = xn;
                y = p1.y + tx * cosY;
                z = p1.z + tx * cosZ;
                if ( dx > 0.0 ) i++;
                else if ( dx < 0.0 ) i--;
            }
            else {
                x = p1.x + ty * cosX;
                y = yn;
                z = p1.z + ty * cosZ;
                if ( dy > 0.0 ) j++;
                else if ( dy < 0.0 ) j--;
            }

            result.add ( interpolate ( new GcodePointImpl ( x, y, z ) ) );

        }

        // add the last point in a path is neccessary, when this point lies inside a grid cell.
        // if the last point lies on a cross, the point is allready in the path
        final GcodePointImpl interpolatedP2 = interpolate ( p2 );
        if ( !result.get ( result.size () - 1 ).equals ( interpolatedP2 ) ) {
            result.add ( interpolatedP2 );
            LOG.trace ( "add last point extra dx=" + dx + " dy=" + dy + " ii2=" + ii2 + " jj2=" + jj2 );
        }

        // if ( !isPathMonotonic ( (GcodePointImpl) point1, (GcodePointImpl) point2, result ) ) {
        //
        // LOG.error ( "interpolateLine: path=" + result );
        // LOG.error ( "interpolateLine: dx=" + dx + " dy=" + dy + " dz=" + dz );
        // LOG.error ( "interpolateLine: i1=" + i1 + " ii1=" + ii1 + " i2=" + i2 + " ii2=" + ii2 );
        //
        // }

        return result.toArray ( new IGcodePoint [result.size ()] );

    }

    private boolean isInsideArea ( double delta, int index, int fromIndex, int toIndex ) {

        // return delta == 0.0 && index != minIndex && index != maxIndex || delta < 0.0 && index > minIndex || delta > 0.0 && index < maxIndex;
        // @formatter:off
        return 
                delta == 0.0 && index != fromIndex && index != toIndex //TODO  may be this condition never fullfilled
                || delta < 0.0 && index > toIndex 
                || delta > 0.0 && index < toIndex;
        // @formatter:on

    }

    private boolean isInsideCollapsedArea ( double delta, int index, int fromIndex, int toIndex ) {

        return delta == 0.0 && index == fromIndex && fromIndex == toIndex;

    }

    private boolean isInsideLastCell ( double delta, int index, int fromIndex, int toIndex ) {

        return delta != 0.0 && index == toIndex;

    }

    @SuppressWarnings("unused")
    private boolean isPathMonotonic ( GcodePointImpl p1, GcodePointImpl p2, ArrayList<IGcodePoint> path ) {
        
        double pathSignum = Math.signum ( p2.x - p1.x );

        for ( IGcodePoint p : path ) {
            final double pointSignum = Math.signum ( p2.x - p.getX () );
            if ( pointSignum != 0.0 && pathSignum != pointSignum ) { return false; }
        }
        
        return true;

    }

    private void sendErrorMessageFromThread ( final String threadName, Exception exc ) {

        final String intialMsg = "Thread " + threadName + " not executed!";

        sendErrorMessage ( intialMsg, exc );

    }

    private void sendErrorMessage ( final String intialMsg, Exception exc ) {

        StringBuilder sb = new StringBuilder ();
        sb.append ( intialMsg );
        sb.append ( "\n\n" );
        sb.append ( "Cause:\n" );
        sb.append ( exc + "\n\n" );

        eventBroker.send ( IEvent.MESSAGE_ERROR, "" + sb );

    }

    // TODO eliminate double impl
    private GcodePointImpl parseCoordinates ( String line, String intro, char closingChar ) {

        return new GcodePointImpl ( parseVector ( line, IConstant.AXIS.length, intro, closingChar ) );

    }

    // TODO eliminate double impl
    private double [] parseVector ( String line, int vectorLength, String intro, char closingChar ) {

        double [] coord = new double [vectorLength];

        int startPos = line.indexOf ( intro ) + intro.length ();
        int endPos = -1;

        for ( int i = 0; i < vectorLength; i++ ) {
            endPos = line.indexOf ( (i < vectorLength - 1 ? "," : "" + closingChar), startPos );
            coord[i] = parseDouble ( 99999.999, line.substring ( startPos, endPos ) );
            startPos = endPos + 1;
        }

        return coord;

    }

    // TODO eliminate double impl
    private double parseDouble ( double defaultValue, String s ) {

        double result = defaultValue;
        try {
            result = Double.parseDouble ( s );
        }
        catch ( NumberFormatException exc ) {}

        return result;

    }

    private void computeAutlevelSegments () {

        LOG.debug ( "computeAutlevelSegments:" );

        for ( IGcodeLine gcodeLine : gcodeLines ) {
            if ( gcodeLine.isMotionMode () ) {
                IGcodePoint [] path = interpolateLine ( gcodeLine.getStart (), gcodeLine.getEnd () );
                gcodeLine.setAutolevelSegmentPath ( path );
            }
        }

    }

    protected class GcodeLoaderThread extends Thread {

        private final static String THREAD_NAME = "gcode-file-loader";

        private File file;

        public GcodeLoaderThread ( File file ) {

            super ( THREAD_NAME + " " + file.getName () );
            this.file = file;

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }

        private boolean isLineEmpty ( String line ) {

            boolean result = true;

            for ( int i = 0; i < line.length (); i++ ) {
                if ( line.charAt ( i ) != ' ' ) {
                    result = false;
                    break;
                }
            }

            return result;

        }

        @Override
        public void run () {

            try ( BufferedReader reader = new BufferedReader ( new FileReader ( file ) ) ) {

                clear ();

                String line;
                while ( (line = reader.readLine ()) != null ) {
                    if ( !isLineEmpty ( line ) ) {
                        appendLine ( line );
                    }
                }
                reader.close ();

                parse ();
                prepareAutolevelScan ( IConstant.INITIAL_XSTEPS, IConstant.INITIAL_YSTEPS );

                eventBroker.send ( IEvent.GCODE_PROGRAM_LOADED, file.getPath () );

            }
            catch ( IOException | RuntimeException exc ) { // including FileNotFoundException
                LOG.error ( "exc=" + exc );
                sendErrorMessageFromThread ( THREAD_NAME, exc );
            }

            LOG.debug ( "stopped" );

        }

    }

    protected class ProbeLoaderThread extends Thread {

        private final static String THREAD_NAME = "probe-file-loader";

        private File file;

        public ProbeLoaderThread ( File file ) {

            super ( THREAD_NAME + " " + file.getName () );
            this.file = file;

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }


        @Override
        public void run () {

            try ( BufferedReader reader = new BufferedReader ( new FileReader ( file ) ) ) {

                String line;
                double [] dim = null;
                GcodePointImpl min = null, max = null;
                double stepWidthX, stepWidthY;

                while ( (line = reader.readLine ()) != null ) {

                    if ( line.startsWith ( "*" ) ) {
                        // igmore comment
                    }
                    else if ( line.startsWith ( "dim" ) ) {
                        dim = parseVector ( line, 2, "=[", ']' );
                        LOG.debug ( "dim_x=" + dim[0] + ", dim_y=" + dim[1] );
                    }
                    else if ( line.startsWith ( "min" ) ) {
                        min = parseCoordinates ( line, "=[", ']' );
                        LOG.debug ( "min=" + min );
                    }
                    else if ( line.startsWith ( "max" ) ) {
                        max = parseCoordinates ( line, "=[", ']' );
                        LOG.debug ( "max=" + max );
                    }
                    else if ( line.startsWith ( "stepWidthX" ) ) {
                        stepWidthX = parseDouble ( 0.0, line.substring ( line.indexOf ( '=' ) + 1 ) );
                        LOG.debug ( "stepWidthX=" + stepWidthX );
                    }
                    else if ( line.startsWith ( "stepWidthY" ) ) {
                        stepWidthY = parseDouble ( 0.0, line.substring ( line.indexOf ( '=' ) + 1 ) );
                        LOG.debug ( "stepWidthY=" + stepWidthY );
                        // check size of gcode area
                        if ( min.zeroAxis ( 'Z' ).equals ( getMin ().zeroAxis ( 'Z' ) ) && max.zeroAxis ( 'Z' ).equals ( getMax ().zeroAxis ( 'Z' ) ) ) {
                            prepareAutolevelScan ( (int) dim[0] - 1, (int) dim[1] - 1 );
                        }
                        else {
                            LOG.error ( "gcode area differs" );
                            eventBroker.send ( IEvent.MESSAGE_ERROR, "GCODE area differs!\nmin1=" + min + " min2=" + getMin () + "\nmax1=" + max + " max2=" + getMax () );
                            return;
                        }
                    }
                    else if ( line.startsWith ( "m(" ) ) {
                        setProbePoint ( parseCoordinates ( line, ")=[", ']' ) );
                    }
                    else if ( line.startsWith ( "end of data" ) ) {
                        setAutolevelScanCompleted ();
                        break;
                        // LOG.debug ( "never be here" );
                    }
                }

                reader.close ();

                eventBroker.send ( IEvent.AUTOLEVEL_DATA_LOADED, file.getPath () );

            }
            catch ( IOException exc ) {
                LOG.error ( "exc=" + exc );
                sendErrorMessageFromThread ( THREAD_NAME, exc );
            }

            LOG.debug ( "stopped" );

        }

    }

    protected class ProbeSaverThread extends Thread {

        private final static String THREAD_NAME = "probe-file-saver";

        private File file;

        public ProbeSaverThread ( File file ) {

            super ( THREAD_NAME + " " + file.getName () );
            this.file = file;

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }

        @Override
        public void run () {

            IGcodePoint min = getMin ();
            IGcodePoint max = getMax ();
            double stepWidthX = getStepWidthX ();
            double stepWidthY = getStepWidthY ();
            int xlength = getXSteps () + 1;
            int ylength = getYSteps () + 1;

            try ( BufferedWriter writer = new BufferedWriter ( new FileWriter ( file ) ); ) {

                writer.write ( "* generated " + new SimpleDateFormat ( "dd.MM.yyyy HH.mm:ss" ).format ( new Date () ) );
                writer.newLine ();
                writer.write ( "* " + file.getPath () );
                writer.newLine ();
                writer.write ( "dim=[" + xlength + "," + ylength + "]" );
                writer.newLine ();
                writer.write ( "min=" + min );
                writer.newLine ();
                writer.write ( "max=" + max );
                writer.newLine ();
                writer.write ( "stepWidthX=" + stepWidthX );
                writer.newLine ();
                writer.write ( "stepWidthY=" + stepWidthY );
                writer.newLine ();

                for ( int i = 0; i < xlength; i++ ) {
                    for ( int j = 0; j < ylength; j++ ) {
                        writer.write ( "m(" + i + "," + j + ")=" + getProbePointAt ( i, j ) );
                        writer.newLine ();
                    }
                }

                writer.write ( "end of data" );
                // writer.newLine ();

                eventBroker.send ( IEvent.AUTOLEVEL_DATA_SAVED, file.getPath () );

            }
            catch ( IOException exc ) {
                LOG.error ( "exc=" + exc );
                sendErrorMessageFromThread ( THREAD_NAME, exc );
            }

            LOG.debug ( "stopped" );

        }

    }

}
