package de.jungierek.grblrunner.service.gcode.impl;

import de.jungierek.grblrunner.service.gcode.IGcodePoint;

public class GcodePointImpl implements IGcodePoint {
    
    public final double x, y, z;
    
    public GcodePointImpl () {

        x = 0.0;
        y = 0.0;
        z = 0.0;

    }

    public GcodePointImpl ( double [] coord ) {
        
        x = coord [0];
        y = coord [1];
        z = coord [2];
        
    }

    public GcodePointImpl ( double x, double y, double z ) {
        
        this.x = x;
        this.y = y;
        this.z = z;
        
    }

    @Override
    public double getX () {
        
        return x;
        
    }

    @Override
    public double getY () {
        
        return y;
        
    }

    @Override
    public double getZ () {
        
        return z;
        
    }
    
    @Override
    public double [] getCooridnates () {
        
        return new double [] { x, y, z };
        
    }

    private boolean doubleEquals ( double d1, double d2 ) {

        return Math.abs ( d1 - d2 ) < EPSILON;

    }

    @Override
    public boolean equals ( Object obj ) {
        
        if ( obj == null || !(obj instanceof GcodePointImpl) ) return false;
        if ( obj == this ) return true;
        
        GcodePointImpl p = (GcodePointImpl) obj;
        // return p.x == x && p.y == y && p.z == z;
        return doubleEquals ( p.x, x ) && doubleEquals ( p.y, y ) && doubleEquals ( p.z, z );
        
    }

    @Override
    public IGcodePoint min ( IGcodePoint point ) {
        
        if ( point == null ) return clone ();

        double x2 = point.getX ();
        double y2 = point.getY ();
        double z2 = point.getZ ();
        
        return new GcodePointImpl ( x2 < x ? x2 : x, y2 < y ? y2 : y, z2 < z ? z2 : z );
        
    }

    @Override
    public IGcodePoint max ( IGcodePoint point ) {
        
        if ( point == null ) return clone ();
        
        double x2 = point.getX ();
        double y2 = point.getY ();
        double z2 = point.getZ ();

        return new GcodePointImpl ( x2 > x ? x2 : x, y2 > y ? y2 : y, z2 > z ? z2 : z );

    }

    @Override
    public IGcodePoint clone () {
        
        return new GcodePointImpl ( x, y, z );
        
    }

    @Override
    public String toString () {
        
        return "[" + String.format ( FORMAT_COORDINATE, x ) + "," + String.format ( FORMAT_COORDINATE, y ) + "," + String.format ( FORMAT_COORDINATE, z ) + "]";
        
    }
    
    @Override
    public IGcodePoint add ( IGcodePoint point ) {
        
        GcodePointImpl p = (GcodePointImpl) point;
        return new GcodePointImpl ( x + p.x, y + p.y, z + p.z );
        
    }

    @Override
    public IGcodePoint sub ( IGcodePoint point ) {
        
        GcodePointImpl p = (GcodePointImpl) point;
        return new GcodePointImpl ( x - p.x, y - p.y, z - p.z );
        
    }

    @Override
    public IGcodePoint mult ( double factor ) {
        
        return new GcodePointImpl ( factor * x, factor * y, factor * z );
        
    }

    @Override
    public IGcodePoint zeroAxis ( char axis ) {

        GcodePointImpl result = this;

        switch ( axis ) {
            case 'X':
                result = new GcodePointImpl ( 0.0, y, z );
                break;

            case 'Y':
                result = new GcodePointImpl ( x, 0.0, z );
                break;

            case 'Z':
                result = new GcodePointImpl ( x, y, 0.0 );
                break;

            default:
                break;
        }

        return result;

    }

    public IGcodePoint _addAxis ( char axis, IGcodePoint point ) {

        GcodePointImpl result = this;

        switch ( axis ) {
            case 'X':
                result = new GcodePointImpl ( x + point.getX (), y, z );
                break;

            case 'Y':
                result = new GcodePointImpl ( x, y + point.getY (), z );
                break;

            case 'Z':
                result = new GcodePointImpl ( x, y, z + point.getZ () );
                break;

            default:
                break;
        }

        return result;

    }

    @Override
    public IGcodePoint addAxis ( char axis, IGcodePoint point ) {

        double value = 0.0;

        switch ( axis ) {
            case 'X':
                value = point.getX ();
                break;

            case 'Y':
                value = point.getY ();
                break;

            case 'Z':
                value = point.getZ ();
                break;

            default:
                break;
        }

        return addAxis ( axis, value );

    }

    @Override
    public IGcodePoint addAxis ( char axis, double value ) {

        GcodePointImpl result = this;

        switch ( axis ) {
            case 'X':
                result = new GcodePointImpl ( x + value, y, z );
                break;

            case 'Y':
                result = new GcodePointImpl ( x, y + value, z );
                break;

            case 'Z':
                result = new GcodePointImpl ( x, y, z + value );
                break;

            default:
                break;
        }

        return result;

    }

}
