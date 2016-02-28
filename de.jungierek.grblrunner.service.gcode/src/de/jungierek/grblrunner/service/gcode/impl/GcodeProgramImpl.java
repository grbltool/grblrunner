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

import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPreferences;
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
    private List<GcodeLineImpl> gcodeLines = new ArrayList<GcodeLineImpl> ( 100 );

    private GcodePointImpl min;
    private GcodePointImpl max;

    private double durationInMinutes;

    private GcodePointImpl matrix [][];
    private int xSteps = IPreferences.INITIAL_XSTEPS;
    private int ySteps = IPreferences.INITIAL_YSTEPS;
    private double xStepWidth, yStepWidth;
    private int numProbePoints;

    private volatile boolean scanDataComplete = false;

    private double rotationAngle = 0;

    private boolean playing;
    private boolean scanning;

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

        return gcodeLines.toArray ( new IGcodeLine [0] );

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
        probeDataFile = new File ( fileName.substring ( 0, fileName.lastIndexOf ( '.' ) ) + IPreferences.AUTOLEVEL_DATA_FILE_EXTENSION );

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
        gcodeLines = new ArrayList<GcodeLineImpl> ( 100 );

        rotationAngle = 0.0;

        clearAutolevelData ();

    }

    @Override
    public double getRotationAngle () {

        return rotationAngle / IConstants.ONE_DEGREE;

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

    @Override
    public void rotate ( double angle ) {

        LOG.debug ( "rotate: angle=" + angle );

        if ( this.rotationAngle == angle * IConstants.ONE_DEGREE ) return;

        this.rotationAngle = angle * IConstants.ONE_DEGREE;

        parse (); // reset line vars

        initMinMax ();

        IGcodePoint lastEnd = null;
        for ( IGcodeLine gcodeLine : getAllGcodeLines () ) {
            gcodeLine.rotate ( rotationAngle, lastEnd );
            if ( gcodeLine.isMotionMode () || gcodeLine.isArcMode () ) {
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

                handleMinMax ( gcodeLine );

            }
            else if ( gcodeLine.isArcMode () ) {

                lastMotionMode = gcodeLine.getGcodeMode ();
                lastEndPoint = gcodeLine.getEnd ();
                lastRadius = gcodeLine.getRadius ();
                lastFeedrate = gcodeLine.getFeedrate ();

                handleMinMax ( gcodeLine );

            }

        }

    }

    private void initMinMax () {

        min = new GcodePointImpl ( +999.0, +999.0, +999.0 );
        max = new GcodePointImpl ( -999.0, -999.0, -999.0 );
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
        double feedrate = IPreferences.AVG_SEEK_FEEDRATE;
        if ( gcodeLine.getGcodeMode () == EGcodeMode.MOTION_MODE_LINEAR || gcodeLine.isArcMode () ) {
            feedrate = gcodeLine.getFeedrate ();
        }
        double time = 0.0;
        if ( feedrate != 0 ) time = dist / feedrate;

        // LOG.info ( "computeDuration: l=" + gcodeLine + " s=" + start + " e=" + end + " d=" + dist + " t=" + time );
        return time;

    }

    @Override
    public boolean isAutolevelScanComplete () {

        return scanDataComplete;

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

        scanDataComplete = true;

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
            eventBroker.send ( IEvents.AUTOLEVEL_DATA_CLEARED, probeDataFile.getPath () );
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

        final double distx = p.x - min.x;
        final double ii = distx / this.xStepWidth + IConstants.EPSILON;
        int i = (int) ii;

        final double disty = p.y - min.y;
        final double jj = disty / this.yStepWidth + IConstants.EPSILON;
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

        double ii = (p.x - min.x) / xStepWidth + IConstants.EPSILON;
        int i = (int) ii;
        if ( i < 0 ) i = 0;
        if ( i >= xSteps ) i = xSteps - 1;

        double jj = (p.y - min.y) / yStepWidth + IConstants.EPSILON;
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
        if ( Math.abs ( result ) <= IConstants.EPSILON ) {
            result = 0.0;
        }

        return result;

    }

    @Override
    public IGcodePoint [] interpolateLine ( IGcodePoint point1, IGcodePoint point2 ) {

        ArrayList<IGcodePoint> result = new ArrayList<IGcodePoint> ();

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

        double ii1 = (p1.x - min.x + IConstants.EPSILON) / xStepWidth;
        final int i1 = (int) ii1;
        ii1 -= i1;

        double jj1 = (p1.y - min.y + IConstants.EPSILON) / yStepWidth;
        final int j1 = (int) jj1;
        jj1 -= j1;

        double ii2 = (p2.x - min.x + IConstants.EPSILON) / xStepWidth;
        int i2 = (int) ii2;
        ii2 -= i2;

        double jj2 = (p2.y - min.y + IConstants.EPSILON) / yStepWidth;
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
            if ( Math.abs ( ii2 ) > IConstants.EPSILON ) i2++;
        }
        else if ( dx < 0.0 ) {
            if ( Math.abs ( ii1 ) <= IConstants.EPSILON ) i--;
        }

        int j = j1;
        if ( dy > 0.0 ) {
            j++;
            if ( Math.abs ( jj2 ) > IConstants.EPSILON ) j2++;
        }
        else if ( dy < 0.0 ) {
            if ( Math.abs ( jj1 ) <= IConstants.EPSILON ) j--;
        }

        double x = p1.x;
        double y = p1.y;
        double z = p1.z;

        double xn = x;
        double yn = y;

        double tx = 1e10;
        double ty = 1e10;

        while ( isInsideArea ( dx, i, i1, i2 ) && isInsideArea ( dy, j, j1, j2 ) ) {

            // HACK delete later
            // if ( Math.abs ( i ) + Math.abs ( j ) > 100 ) return null;
            if ( Math.abs ( i ) + Math.abs ( j ) > 100 ) {
                System.out.println ( "dx=" + dx + " i=" + i + " i2=" + i2 + " dy=" + dy + " j=" + j + " j2=" + j2 + " p1=" + p1 + " p2=" + p2 );
                break; // HACK HACK
            }

            if ( dx != 0.0 ) {
                xn = min.x + i * xStepWidth; // wie matrix [i][j].x
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

        result.add ( interpolate ( p2 ) );

        // if ( !isPathMonotonic ( (GcodePointImpl) point1, (GcodePointImpl) point2, result ) ) {
        //
        // LOG.error ( "interpolateLine: path=" + result );
        // LOG.error ( "interpolateLine: dx=" + dx + " dy=" + dy + " dz=" + dz );
        // LOG.error ( "interpolateLine: i1=" + i1 + " ii1=" + ii1 + " i2=" + i2 + " ii2=" + ii2 );
        //
        // }

        return result.toArray ( new IGcodePoint [result.size ()] );

    }

    private boolean isInsideArea ( double delta, int index, int minIndex, int maxIndex ) {

        return delta == 0.0 && index != minIndex && index != maxIndex || delta < 0.0 && index > minIndex || delta > 0.0 && index < maxIndex;

    }

    @SuppressWarnings("unused")
    private boolean isPathMonotonic ( GcodePointImpl p1, GcodePointImpl p2, ArrayList<IGcodePoint> path ) {
        
        double pathSignum = Math.signum ( p2.x - p1.x );

        for ( IGcodePoint p : path ) {
            final double pointSignum = Math.signum ( p2.x - p.getX () );
            if ( pointSignum != 0.0 && pathSignum != pointSignum ) {
 return false;
            }
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

        eventBroker.send ( IEvents.MESSAGE_ERROR, "" + sb );

    }

    // TODO eliminate double impl
    private GcodePointImpl parseCoordinates ( String line, String intro, char closingChar ) {

        return new GcodePointImpl ( parseVector ( line, IConstants.AXIS.length, intro, closingChar ) );

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
                prepareAutolevelScan ( IPreferences.INITIAL_XSTEPS, IPreferences.INITIAL_YSTEPS );

                eventBroker.send ( IEvents.GCODE_PROGRAM_LOADED, file.getPath () );

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
                            eventBroker.send ( IEvents.MESSAGE_ERROR, "GCODE area differs!\nmin1=" + min + " min2=" + getMin () + "\nmax1=" + max + " max2=" + getMax () );
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

                eventBroker.send ( IEvents.AUTOLEVEL_DATA_LOADED, file.getPath () );

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

                eventBroker.send ( IEvents.AUTOLEVEL_DATA_SAVED, file.getPath () );

            }
            catch ( IOException exc ) {
                LOG.error ( "exc=" + exc );
                sendErrorMessageFromThread ( THREAD_NAME, exc );
            }

            LOG.debug ( "stopped" );

        }

    }

    // ====================================================================================================================================

    public void initTest () {

        GcodePointImpl p00 = new GcodePointImpl ( 0.0, 0.0, 1.0 );
        GcodePointImpl p01 = new GcodePointImpl ( 0.0, 1.0, 0.75 );
        GcodePointImpl p02 = new GcodePointImpl ( 0.0, 2.0, 0.5 );
        GcodePointImpl p03 = new GcodePointImpl ( 0.0, 3.0, 0.25 );
        GcodePointImpl p04 = new GcodePointImpl ( 0.0, 4.0, 0.0 );

        GcodePointImpl p10 = new GcodePointImpl ( 1.0, 0.0, 0.75 );
        GcodePointImpl p11 = new GcodePointImpl ( 1.0, 1.0, 0.625 );
        GcodePointImpl p12 = new GcodePointImpl ( 1.0, 2.0, 0.5 );
        GcodePointImpl p13 = new GcodePointImpl ( 1.0, 3.0, 0.375 );
        GcodePointImpl p14 = new GcodePointImpl ( 1.0, 4.0, 0.25 );

        GcodePointImpl p20 = new GcodePointImpl ( 2.0, 0.0, 0.5 );
        GcodePointImpl p21 = new GcodePointImpl ( 2.0, 1.0, 0.5 );
        GcodePointImpl p22 = new GcodePointImpl ( 2.0, 2.0, 0.5 );
        GcodePointImpl p23 = new GcodePointImpl ( 2.0, 3.0, 0.5 );
        GcodePointImpl p24 = new GcodePointImpl ( 2.0, 4.0, 0.5 );

        GcodePointImpl p30 = new GcodePointImpl ( 3.0, 0.0, 0.25 );
        GcodePointImpl p31 = new GcodePointImpl ( 3.0, 1.0, 0.375 );
        GcodePointImpl p32 = new GcodePointImpl ( 3.0, 2.0, 0.5 );
        GcodePointImpl p33 = new GcodePointImpl ( 3.0, 3.0, 0.625 );
        GcodePointImpl p34 = new GcodePointImpl ( 3.0, 4.0, 0.75 );

        GcodePointImpl p40 = new GcodePointImpl ( 4.0, 0.0, 0.0 );
        GcodePointImpl p41 = new GcodePointImpl ( 4.0, 1.0, 0.25 );
        GcodePointImpl p42 = new GcodePointImpl ( 4.0, 2.0, 0.5 );
        GcodePointImpl p43 = new GcodePointImpl ( 4.0, 3.0, 0.75 );
        GcodePointImpl p44 = new GcodePointImpl ( 4.0, 4.0, 1.0 );

        /* @formatter:off */
        matrix = new GcodePointImpl [] [] { 
                { p00, p01, p02, p03, p04 }, 
                { p10, p11, p12, p13, p14 }, 
                { p20, p21, p22, p23, p24 }, 
                { p30, p31, p32, p33, p34 }, 
                { p40, p41, p42, p43, p44 }, 
        };
        /* @formatter:on */

        xSteps = 4;
        ySteps = 4;

        min = new GcodePointImpl ( 0.0, 0.0, 0.0 );
        max = new GcodePointImpl ( 4.0, 4.0, 0.0 );

        xStepWidth = (max.x - min.x) / xSteps;
        yStepWidth = (max.y - min.y) / ySteps;

    }

    public void testCase ( String name, GcodePointImpl p1, GcodePointImpl p2 ) {

        IGcodePoint [] path = interpolateLine ( p1, p2 );

        System.out.println ( name + ": len=" + path.length + " p1=" + p1 + " p2=" + p2 );
        for ( int i = 0; i < path.length; i++ ) {
            System.out.println ( "i=" + i + " p=" + path[i] );
        }
        System.out.println ( "-------------------------" );

    }

    public static void _main ( String [] args ) {

        GcodeProgramImpl m = new GcodeProgramImpl ();
        m.initTest ();

        // for ( int i = 0; i < m.matrix.length; i++ ) {
        // for ( int j = 0; j < m.matrix[i].length; j++ ) {
        // System.out.println ( "i=" + i + " j=" + j + " z=" + m.matrix[i][j].z );
        // }
        // }

        // GcodePointImpl p = new GcodePointImpl ( 0.5, 0.5, 0.0 );
        // System.out.println ( "z=" + m.interpolate ( p ).z );

        /* @formatter:off */
        Object tests [][] = new Object [] [] {
                
                { "straight right aligned",          new GcodePointImpl ( 0.0, 0.0, 0.0 ), new GcodePointImpl ( 4.0, 0.0, 0.0 ) },
                { "straight up aligned",          new GcodePointImpl ( 0.0, 0.0, 0.0 ), new GcodePointImpl ( 0.0, 4.0, 0.0 ) },
                { "straight left aligned",          new GcodePointImpl ( 4.0, 0.0, 0.0 ), new GcodePointImpl ( 0.0, 0.0, 0.0 ) },
                { "straight down aligned",          new GcodePointImpl ( 0.0, 4.0, 0.0 ), new GcodePointImpl ( 0.0, 0.0, 0.0 ) },

                { "straight right not aligned",          new GcodePointImpl ( 0.5, 0.5, 0.0 ), new GcodePointImpl ( 3.5, 0.5, 0.0 ) },
                { "straight up not aligned",          new GcodePointImpl ( 0.5, 0.5, 0.0 ), new GcodePointImpl ( 0.5, 3.5, 0.0 ) },
                { "straight left not aligned",          new GcodePointImpl ( 3.5, 0.5, 0.0 ), new GcodePointImpl ( 0.5, 0.5, 0.0 ) },
                { "straight down not aligned",          new GcodePointImpl ( 0.5, 3.5, 0.0 ), new GcodePointImpl ( 0.5, 0.5, 0.0 ) },

                { "diagnoal right up aligned",          new GcodePointImpl ( 0.0, 0.0, 0.0 ), new GcodePointImpl ( 4.0, 4.0, 0.0 ) },
                { "diagnoal right up not aligned",      new GcodePointImpl ( 0.5, 0.5, 0.0 ), new GcodePointImpl ( 3.5, 3.5, 0.0 ) },

                { "diagnoal left up aligned",          new GcodePointImpl ( 4.0, 0.0, 0.0 ), new GcodePointImpl ( 0.0, 4.0, 0.0 ) },
                { "diagnoal left up not aligned",      new GcodePointImpl ( 3.5, 0.5, 0.0 ), new GcodePointImpl ( 0.5, 3.5, 0.0 ) },

                { "diagnoal right down aligned",          new GcodePointImpl ( 0.0, 4.0, 0.0 ), new GcodePointImpl ( 4.0, 0.0, 0.0 ) },
                { "diagnoal right down not aligned",      new GcodePointImpl ( 0.5, 3.5, 0.0 ), new GcodePointImpl ( 3.5, 0.5, 0.0 ) },

                { "diagnoal left down aligned",          new GcodePointImpl ( 4.0, 4.0, 0.0 ), new GcodePointImpl ( 0.0, 0.0, 0.0 ) },
                { "diagnoal left down not aligned",      new GcodePointImpl ( 3.5, 3.5, 0.0 ), new GcodePointImpl ( 0.5, 0.5, 0.0 ) },

                { "no crossing",      new GcodePointImpl ( 0.3, 0.5, 0.0 ), new GcodePointImpl ( 0.5, 0.7, 0.0 ) },
                { "irregular",      new GcodePointImpl ( 0.3, 0.5, 0.0 ), new GcodePointImpl ( 2.15, 1.78, 0.0 ) },

        };
        /* @formatter:on */

        for ( int i = 0; i < tests.length; i++ ) {
            int j = 0;
            String name = (String) tests[i][j++];
            GcodePointImpl p1 = (GcodePointImpl) tests[i][j++];
            GcodePointImpl p2 = (GcodePointImpl) tests[i][j++];
            m.testCase ( name, p1, p2 );
        }

    }


}
