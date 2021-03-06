package de.jungierek.grblrunner.service.gcode.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;

public class GcodeLineImpl implements IGcodeLine {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeLineImpl.class );

    private final int lineNo;
    private final String line;
    private EGcodeMode mode;
    private IGcodePoint start;
    private IGcodePoint end;
    private double radius;
    private int feedrate;
    private boolean processed;

    private IGcodePoint [] autolevelSegmentPath = null;

    public GcodeLineImpl ( int lineNo, String line ) {

        this.line = line;
        this.lineNo = lineNo;

    }

    @Override
    public void setAutolevelSegmentPath ( IGcodePoint [] path ) {

        this.autolevelSegmentPath = path;

    }

    @Override
    public IGcodePoint [] getAutoevelSegmentPath () {

        return autolevelSegmentPath;

    }

    @Override
    public int getLineNo () {

        return lineNo;

    }

    @Override
    public String getLine () {

        return line;

    }

    @Override
    public EGcodeMode getGcodeMode () {

        return mode;

    };

    @Override
    public IGcodePoint getStart () {

        return start;

    }

    @Override
    public IGcodePoint getEnd () {

        return end;

    }

    @Override
    public double getRadius () {

        return radius;

    }

    @Override
    public int getFeedrate () {

        return feedrate;

    }

    @Override
    public boolean isProcessed () {

        return processed;

    }

    @Override
    public void setProcessed ( boolean processed ) {

        this.processed = processed;

    }

    @Override
    public boolean isMoveInXYZ () {

        return isMoveInXY () || isMoveInZ ();

    }

    @Override
    public boolean isMoveInXY () {

        return isMoveInX () || isMoveInY ();

    }

    // from GcodePointImpl
    private boolean doubleEquals ( double d1, double d2 ) {

        return Math.abs ( d1 - d2 ) < IGcodePoint.EPSILON;

    }

    @Override
    public boolean isMoveInX () {

        return start != null && end != null && !doubleEquals ( start.getX (), end.getX () );

    }

    @Override
    public boolean isMoveInY () {

        return start != null && end != null && !doubleEquals ( start.getY (), end.getY () );

    }

    @Override
    public boolean isMoveInZ () {

        return start != null && end != null && !doubleEquals ( start.getZ (), end.getZ () );

    }

    @Override
    public boolean isMotionMode () {

        return mode != null && mode.isMotionMode ();

    }

    @Override
    public boolean isMotionModeSeek () {

        return mode != null && mode.isMotionModeSeek ();

    }

    @Override
    public boolean isMotionModeLinear () {

        return mode != null && mode.isMotionModeLinear ();

    }

    @Override
    public boolean isMotionModeArc () {

        return mode != null && mode.isMotionModeArc ();

    }

    private void parseMotionArc ( IGcodePoint lastEnd, double lastRadius, int last_feedrate ) {

        parseMotionLine ( lastEnd, last_feedrate );

        if ( hasAxisCoordinate ( 'R' ) ) {
            radius = scanAxisCoordinate ( 'R', lastRadius );
        }
        else {
            final double i = scanAxisCoordinate ( 'I', 0 );
            final double j = scanAxisCoordinate ( 'J', 0 );
            radius = Math.sqrt ( i * i + j * j );
            LOG.debug ( "parseMotionArc: i=" + i + " j=" + j + " radius=" + radius );
        }

    }

    private void parseMotionLine ( IGcodePoint lastEnd, int last_feedrate ) {

        this.start = lastEnd;
    
        double x = scanAxisCoordinate ( 'X', lastEnd.getX () );
        double y = scanAxisCoordinate ( 'Y', lastEnd.getY () );
        double z = scanAxisCoordinate ( 'Z', lastEnd.getZ () );
        this.end = new GcodePointImpl ( x, y, z );
    
        feedrate = (int) scanAxisCoordinate ( 'F', last_feedrate );

    }

    private boolean hasAxisCoordinate ( char axis ) {

        return line.indexOf ( axis ) > -1;

    }

    private double scanAxisCoordinate ( char axis, double coordinate ) {
    
        int pos = line.indexOf ( axis );
        if ( pos == -1 ) return coordinate;
        int end = pos;
        while ( line.charAt ( end + 1 ) == ' ' ) {
            end++; // skip leading space
        }
        for ( int i = end + 1; i < line.length (); i++ ) {
            char c = line.charAt ( i );
            // TODO Vorzeichen nur ganz vorne
            if ( c >= '0' && c <= '9' || c == '.' || c == '+' || c == '-' ) end = i;
            else break;
        }
    
        if ( end == pos ) return coordinate;
    
        return Double.parseDouble ( line.substring ( pos + 1, end + 1 ) );
    
    }

    // start ist Referenz, end ist ein neues Objekt
    @Override
    public void parseGcode ( EGcodeMode lastMotionMode, IGcodePoint lastEnd, double lastRadius, int lastFeedrate ) {

        if ( lastEnd == null ) return;

        mode = EGcodeMode.identify ( line );

        if ( mode.isMotionMode () ) {
            if ( mode.isMotionModeArc () ) {
                parseMotionArc ( lastEnd, lastRadius, lastFeedrate );
            }
            else {
                parseMotionLine ( lastEnd, lastFeedrate );
            }
        }
        else if ( mode == EGcodeMode.GCODE_MODE_UNDEF ) {
            // HACK there is a gap: when in a line is only mess without commented out, then this should be an error
            // but there is a 0-move, when lastMotionMode is a motion mode with last point as target
            mode = lastMotionMode;
            if ( mode.isMotionMode () ) {
                if ( mode.isMotionModeArc () ) {
                    parseMotionArc ( lastEnd, lastRadius, lastFeedrate );
                }
                else {
                    parseMotionLine ( lastEnd, lastFeedrate );
                }
            }
        }

    }

    @Override
    public void rotate ( double angle, IGcodePoint lastEnd ) {

        // only rotate end point
        // a end point from the last gcode line is the same reference as the current start
        // first startpoint is (0,0,0)

        if ( isMotionMode () ) {
            if ( lastEnd != null ) start = lastEnd;
            end = end.rotate ( 'Z', angle );
        }

    }

    @Override
    public String toString () {
        
        String result = "[" + lineNo + ": " + mode;
        if ( isMotionMode () ) {
            result += " " + start + " -> " + end;
            if ( isMotionModeArc () ) result += " r=" + radius;
            result += " " + feedrate + " mm/min";
        }
        else if ( mode == EGcodeMode.COMMENT ) {
            result += line;
        }

        result += "]";

        return result;
        
    }

}
