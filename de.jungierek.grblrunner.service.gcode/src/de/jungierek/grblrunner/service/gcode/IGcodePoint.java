package de.jungierek.grblrunner.service.gcode;

public interface IGcodePoint {
    
    public static final String FORMAT_COORDINATE = "%.3f";
    public static final double EPSILON = 0.0005;
    
    public double getX ();
    public double getY ();
    public double getZ ();
    
    public double [] getCooridnates ();
    
    public IGcodePoint min ( IGcodePoint point );
    public IGcodePoint max ( IGcodePoint point );
    
    public IGcodePoint add ( IGcodePoint point );
    public IGcodePoint sub ( IGcodePoint point );
    public IGcodePoint mult ( double factor );
    
    public IGcodePoint zeroAxis ( char axis );
    public IGcodePoint addAxis ( char axis, IGcodePoint point );

}