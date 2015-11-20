package de.jungierek.grblrunner.parts;

public class Point {

    public double x;
    public double y;

    public Point () {

        this.x = 0.0;
        this.y = 0.0;

    }

    public Point ( double x, double y ) {

        this.x = (int) x;
        this.y = (int) y;

    }

    public Point add ( Point p ) {
        
        return add ( p.x, p.y );
        
    }

    public Point add ( double x, double y ) {
        
        this.x += x;
        this.y += y;
        return this;
        
    }

    public Point sub ( Point p ) {
        
        return sub ( p.x, p.y );
        
    }

    public Point sub ( double x, double y ) {
        
        this.x -= x;
        this.y -= y;

        return this;
        
    }

    public Point mult ( double scalar ) {

        this.x *= scalar;
        this.y *= scalar;

        return this;

    }

    public Point max ( Point p ) {

        return new Point ( Math.max ( x, p.x ), Math.max ( y, p.y ) );

    }

    public Point min ( Point p ) {

        return new Point ( Math.min ( x, p.x ), Math.min ( y, p.y ) );

    }

    @Override
    public boolean equals ( Object obj ) {

        if ( obj == null || !(obj instanceof Point) ) return false;

        Point p = (Point) obj;
        return x == p.x && y == p.y;

    }

    @Override
    public String toString () {

        return "[" + x + "," + y + "]";

    };

    @Override
    protected Point clone () {
        
        return new Point ( this.x, this.y );

    }

}