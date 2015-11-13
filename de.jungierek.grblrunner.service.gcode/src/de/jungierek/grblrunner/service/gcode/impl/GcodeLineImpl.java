package de.jungierek.grblrunner.service.gcode.impl;

import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeModelVisitor;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;

public class GcodeLineImpl implements IGcodeLine {

    private final int lineNo;
    private final String line;
    private EGcodeMode mode;
    private IGcodePoint start;
    private IGcodePoint end;
    private int feedrate;
    private boolean processed;

    public GcodeLineImpl ( int lineNo, String line ) {

        this.line = line;
        this.lineNo = lineNo;

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
    public boolean isMoveInXY () {

        return start != null && end != null && (start.getX () != end.getX () || start.getY () != end.getY ());

    }

    @Override
    public boolean isMotionMode () {

        // return mode == EGcodeMode.MOTION_MODE_SEEK || mode == EGcodeMode.MOTION_MODE_LINEAR;
        return mode.isMotionMode ();

    }

    private void parseMotionLine ( IGcodePoint lastEnd, int last_feedrate ) {

        this.start = lastEnd;
    
        double x = scanAxisCoordinate ( 'X', lastEnd.getX () );
        double y = scanAxisCoordinate ( 'Y', lastEnd.getY () );
        double z = scanAxisCoordinate ( 'Z', lastEnd.getZ () );
        this.end = new GcodePointImpl ( x, y, z );
    
        feedrate = (int) scanAxisCoordinate ( 'F', last_feedrate );

    }

    private double scanAxisCoordinate ( char axis, double coordinate ) {
    
        int pos = line.indexOf ( axis );
        if ( pos == -1 ) return coordinate;
        int end = pos;
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
    public void parseGcode ( EGcodeMode lastMotionMode , IGcodePoint lastEnd , int lastFeedrate  ) {

        if ( lastEnd == null ) return;

        this.mode = EGcodeMode.identify ( line );

        if ( this.mode.isMotionMode () ) {
            parseMotionLine ( lastEnd, lastFeedrate );
        }
        else if ( this.mode == EGcodeMode.GCODE_MODE_UNDEF ) {
            // HACK there is a gap: when in a line is only mess without commented out, then this is an error
            // then there is a 0-move, because last point is the target
            this.mode = lastMotionMode;
            parseMotionLine ( lastEnd, lastFeedrate );
        }

    }

    @Override
    public void rotate ( double angle, IGcodePoint lastEnd ) {

        // only rotate end point
        // a end point from the last gcode line is the same reference as the current start
        // first startpoint is (0,0,0)

        if ( isMotionMode () ) {

            if ( lastEnd != null ) start = lastEnd;

            double x0 = end.getX ();
            double y0 = end.getY ();

            double x1 = x0 * Math.cos ( angle ) + y0 * Math.sin ( angle );
            double y1 = x0 * -Math.sin ( angle ) + y0 * Math.cos ( angle );

            end = new GcodePointImpl ( x1, y1, end.getZ () );

        }

    }

    @Override
    public void visit ( IGcodeModelVisitor visitor ) {
    
        visitor.visit ( this );
    
    }

    @Override
    public String toString () {
        
        String result = "[" + lineNo + ": " + mode;
        if ( isMotionMode () ) {
            result += " " + start + " -> " + end + " " + feedrate + " mm/min";
        }
        else if ( mode == EGcodeMode.COMMENT ) {
            result += line;
        }

        result += "]";

        return result;
        
    }

}
