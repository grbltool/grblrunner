package de.jungierek.grblrunner.service.gcode;



public interface IGcodeLine {
    
    public String getLine ();
    
    public int getLineNo ();
    
    public IGcodePoint getStart ();
    public IGcodePoint getEnd ();
    public EGcodeMode getGcodeMode ();
    public double getRadius ();
    public int getFeedrate ();

    public boolean isProcessed ();
    public void setProcessed ( boolean processed );

    public boolean isMotionMode ();
    public boolean isMoveInXYZ ();
    public boolean isMoveInXY ();
    public boolean isMoveInX ();
    public boolean isMoveInY ();
    public boolean isMoveInZ ();
    boolean isArcMode ();

    void parseGcode ( EGcodeMode lastMotionMode, IGcodePoint lastEnd, double lastRadius, int lastFeedrate );

    public void rotate ( double rotationAngle, IGcodePoint lastEnd );

}

