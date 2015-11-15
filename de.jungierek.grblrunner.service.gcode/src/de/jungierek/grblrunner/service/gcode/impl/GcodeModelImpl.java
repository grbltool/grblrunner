package de.jungierek.grblrunner.service.gcode.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeModelVisitor;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;

public class GcodeModelImpl implements IGcodeModel {
    
    private static final Logger LOG = LoggerFactory.getLogger ( GcodeModelImpl.class );

    public final static IGcodePoint GCODE_DEFAULT_START_POINT = new GcodePointImpl ( 0.0, 0.0, 0.0 );

    private int nextLineNo = 0;
    private List<GcodeLineImpl> gcodeLines = new ArrayList<GcodeLineImpl> ( 100 );
    
    private GcodePointImpl min;
    private GcodePointImpl max;
    
    private IGcodePoint gcodeSshift = new GcodePointImpl ( 0.0, 0.0, 0.0 );

    private GcodePointImpl matrix [][];
    private int xSteps, ySteps;
    private double xStepWidth, yStepWidth;
    private int numProbePoints;

    private volatile boolean scanDataComplete = false;

    private final static double ONE_DEGREE = Math.PI / 180.0;
    private double rotationAngle = 0;

    @Override
    public void appendGcodeLine ( String line ) {

        gcodeLines.add ( new GcodeLineImpl ( nextLineNo++, line ) );
        
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
    public void clear () {

        nextLineNo = 0;
        // gcodeLines.clear ();
        gcodeLines = new ArrayList<GcodeLineImpl> ( 100 );

        rotationAngle = 0.0;

        disposeProbeData ();
    
    }

    @Override
    public double getRotationAngle () {

        return rotationAngle / ONE_DEGREE;

    }

    @Override
    public int getLineCount () {
        
        return gcodeLines.size ();
        
    }
    
    @Override
    public void visit ( IGcodeModelVisitor visitor ) {
        
        for ( GcodeLineImpl gcodeLine : gcodeLines ) {
            gcodeLine.visit ( visitor );
        }
    
    }

    @Override
    public void resetProcessed () {
        
        visit ( new IGcodeModelVisitor() {
            
            @Override
            public void visit ( IGcodeLine gcodeLine ) {
                gcodeLine.setProcessed ( false );
            }
        } );

    }

    @Override
    public void rotate ( double angle ) {
    
    
        LOG.debug ( "rotate: angle=" + angle );

        if ( this.rotationAngle == angle * ONE_DEGREE ) return;
    
        this.rotationAngle = angle * ONE_DEGREE;
        
        parseGcode (); // reset line vars

        initMinMax ();
    
        visit ( new IGcodeModelVisitor() {
            
            IGcodePoint lastEnd = null;
    
            @Override
            public void visit ( IGcodeLine gcodeLine ) {
    
                gcodeLine.rotate ( rotationAngle, lastEnd );
                if ( gcodeLine.isMotionMode () ) {
                    lastEnd = gcodeLine.getEnd ();
                    handleMinMax ( gcodeLine );
                }
    
            }
            
        } );
    
    }

    @Override
    public void parseGcode () {
        
        initMinMax ();
        
        visit ( new IGcodeModelVisitor() {
            
            IGcodePoint lastEndPoint = GCODE_DEFAULT_START_POINT;
            // EGcodeMode lastMotionMode = EGcodeMode.GCODE_MODE_UNDEF;
            EGcodeMode lastMotionMode = EGcodeMode.MOTION_MODE_SEEK; // TODO change to _LINEAR?
            int lastFeedrate = 0;
            
            @Override
            public void visit ( IGcodeLine gcodeLine ) {
                
                gcodeLine.parseGcode ( lastMotionMode, lastEndPoint, lastFeedrate );

                if ( gcodeLine.isMotionMode () ) {

                    lastMotionMode = gcodeLine.getGcodeMode ();
                    lastEndPoint = gcodeLine.getEnd ();
                    lastFeedrate = gcodeLine.getFeedrate ();

                    handleMinMax ( gcodeLine );

                }

            }
            
        } );

    }

    private void initMinMax () {

        min = new GcodePointImpl ( +999.0, +999.0, +999.0 );
        max = new GcodePointImpl ( -999.0, -999.0, -999.0 );

    }
    
    private void handleMinMax ( IGcodeLine gcodeLine ) {
    
        if ( gcodeLine.isMoveInXY () ) {

            IGcodePoint point = gcodeLine.getEnd ();
    
            LOG.trace ( "parseGcode: min=" + min + " max=" + max + " lastend=" + point );
            min = (GcodePointImpl) min.min ( point );
            max = (GcodePointImpl) max.max ( point );
    
        }
    
    }

    @Override
    public boolean isScanDataComplete () {

        return scanDataComplete;

    }

    @Override
    public boolean isGcodeProgramLoaded () {

        return gcodeLines.size () > 0;

    }

    @Override
    public void setScanDataCompleted () {

        scanDataComplete = true;

    }

    @Override
    public void disposeProbeData () {

        scanDataComplete = false;
        matrix = null;

        // for ( int i = 0; i < matrix.length; i++ ) {
        // for ( int j = 0; j < matrix[i].length; j++ ) {
        // matrix[i][j] = new GcodePointImpl ( matrix[i][j].x, matrix[i][j].y, 0.0 );
        // }
        // }

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

        resetAutolevelScan ();
    }

    @Override
    public void resetAutolevelScan () {

        for ( int i = 0; i < matrix.length; i++ ) {
            double x = min.getX () + i * xStepWidth;
            for ( int j = 0; j < matrix[i].length; j++ ) {
                double y = min.getY () + j * yStepWidth;
                matrix[i][j] = new GcodePointImpl ( x, y, 0.0 );
            }
        }

    }

    @Override
    public void setProbePoint ( IGcodePoint probe ) {

        // TODO extract computing of indizes in separate Methods, see also interpolate
        // Grbl sends data in machine coordinates, transfer to working coordinates
        final GcodePointImpl p = (GcodePointImpl) probe.sub ( gcodeSshift );

        final double distx = p.x - min.x;
        final double ii = distx / this.xStepWidth + EPSILON;
        int i = (int) ii;

        final double disty = p.y - min.y;
        final double jj = disty / this.yStepWidth + EPSILON;
        int j = (int) jj;

        // LOG.debug ( "setProbePoint: dx=" + distx + " dy=" + disty + "  ii=" + ii + " i=" + i + " jj=" + jj + " j=" + j );

        matrix[i][j] = p;

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
    public IGcodePoint [][] getScanMatrix () {

        return matrix;

    }

    @Override
    public void setShift ( IGcodePoint shift ) {

        if ( shift != null ) gcodeSshift = shift;

    }

    @Override
    public IGcodePoint getShift () {

        return gcodeSshift;

    }

    @Override
    public int getNumProbePoints () {

        return numProbePoints;

    }

    private GcodePointImpl interpolate ( IGcodePoint point ) {

        // TODO extract computing of indizes in separate Methods, see also setProbePoint

        GcodePointImpl p = (GcodePointImpl) point;

        double result = p.z; // the interpolated z for the point

        double ii = (p.x - min.x) / xStepWidth + EPSILON;
        int i = (int) ii;
        if ( i < 0 ) i = 0;
        if ( i >= xSteps ) i = xSteps - 1;

        double jj = (p.y - min.y) / yStepWidth + EPSILON;
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
        
        return new GcodePointImpl ( p.x, p.y, result );

    }

    @Override
    public IGcodePoint [] interpolateLine ( IGcodePoint point1, IGcodePoint point2 ) {
        
        ArrayList<IGcodePoint> result = new ArrayList<IGcodePoint> ();

        GcodePointImpl p1 = (GcodePointImpl) point1;
        GcodePointImpl p2 = (GcodePointImpl) point2;
        
        // first point of path
        result.add ( interpolate ( p1 ) );

        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double dz = p2.z - p1.z;

        if ( dx == 0.0 && dy == 0 ) {
            result.add ( interpolate ( p2 ) );
            return result.toArray ( new IGcodePoint [0] );
        }

        double ii1 = (p1.x - min.x) / xStepWidth + EPSILON;
        int i1 = (int) ii1;
        ii1 -= i1;

        double jj1 = (p1.y - min.y) / yStepWidth + EPSILON;
        int j1 = (int) jj1;
        jj1 -= j1;

        double ii2 = (p2.x - min.x) / xStepWidth + EPSILON;
        int i2 = (int) ii2;
        ii2 -= i2;

        double jj2 = (p2.y - min.y) / yStepWidth + EPSILON;
        int j2 = (int) jj2;
        jj2 -= j2;
        
        double llXY = dx * dx + dy * dy;
        double distXY = Math.sqrt ( llXY );

        double llXYZ = llXY + dz * dz;
        double distXYZ = Math.sqrt ( llXYZ );

        dx /= distXY; // cos
        dy /= distXY; // cos
        dz *= distXY / llXYZ; // correction for the slope in z vs. the travel in xy

        if ( Math.abs ( dx ) < 1e-10 ) dx = 0.0;
        if ( Math.abs ( dy ) < 1e-10 ) dy = 0.0;

        // next intersection
        int i = i1;
        if ( dx > 0.0 ) {
            i++;
            if ( ii2 != 0.0 ) i2++;
        }
        else if ( dx < 0.0 ) {
            if ( ii1 == 0.0 ) i--;
            // if ( ii2 != 0.0 ) i2++;
        }
        
        int j = j1;
        if ( dy > 0.0 ) {
            j++;
            if ( jj2 != 0.0 ) j2++;
        }
        else if ( dy < 0.0 ) {
            if ( jj1 == 0.0 ) j--;
            // if ( jj2 != 0.0 ) j2++;
        }

        double x = p1.x;
        double y = p1.y;
        double z = p1.z;

        double xn = x;
        double yn = y;

        double tx = 1e10;
        double ty = 1e10;

        while ( i != i2 || j != j2 ) {

            // HACK delete later
            if ( Math.abs ( i ) + Math.abs ( j ) > 100 ) return null;
            // System.out.println ( "i=" + i + " j=" + j );

            if ( dx != 0.0 ) {
                xn = min.x + i * xStepWidth; // wie matrix [i][j].x
                tx = (xn - p1.x) / dx; // distXY from p1 to pn by moving x
            }

            if ( dy != 0.0 ) {
                yn = min.y + j * yStepWidth;
                ty = (yn - p1.y) / dy; // distXY from p1 to pn by moving y
            }

            if ( Math.abs ( tx ) == Math.abs ( ty ) ) {
                x = xn;
                y = yn;
                z = p1.z + tx * dz;
                if ( dx > 0.0 ) i++;
                else i--;
                if ( dy > 0.0 ) j++;
                else j--;
            }
            else if ( tx < ty ) {
                x = xn;
                y = p1.y + tx * dy;
                z = p1.z + tx * dz;
                if ( dx > 0.0 ) i++;
                else i--;
            }
            else {
                x = p1.x + ty * dx;
                y = yn;
                z = p1.z + ty * dz;
                if ( dy > 0.0 ) j++;
                else j--;
            }
            
            result.add ( interpolate ( new GcodePointImpl ( x, y, z ) ) );

        }

        result.add ( interpolate ( p2 ) );
    
        return result.toArray ( new IGcodePoint [0] );
    
    }

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

    public static void main ( String [] args ) {

        GcodeModelImpl m = new GcodeModelImpl ();
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
            String name = (String) tests [i][j++];
            GcodePointImpl p1 = (GcodePointImpl) tests[i][j++];
            GcodePointImpl p2 = (GcodePointImpl) tests[i][j++];
            m.testCase ( name, p1, p2 );
        }

    }

}

