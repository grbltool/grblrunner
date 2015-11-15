package de.jungierek.grblrunner.service.gcode;


public interface IGcodeModel {
    
    public static final double EPSILON = 0.001;

    public void visit ( IGcodeModelVisitor visitor );

    public boolean isGcodeProgramLoaded ();
    public void clear ();

    public void appendGcodeLine ( String line );

    // TODO change this method to
    // public IGcodePoint getProbeDaataAt ( int ix, int iy );
    @Deprecated
    public IGcodePoint [][] getScanMatrix (); // TODO change to explicite call for every point

    public IGcodePoint getMin ();
    public IGcodePoint getMax ();
    
    @Deprecated
    public int getLineCount ();
    
    public void parseGcode ();

    public void resetProcessed ();
    
    public void setShift ( IGcodePoint shift );
    public IGcodePoint getShift ();

    public void rotate ( double angle );
    double getRotationAngle ();

    // ---------------------------------------------------------------------

    public void disposeProbeData ();
    public void setScanDataCompleted ();
    public boolean isScanDataComplete ();
    void prepareAutolevelScan (); // force creation of new grid
    public void prepareAutolevelScan ( int xSteps, int ySteps );
    public void resetAutolevelScan ();

    public void setProbePoint ( IGcodePoint probe );

    public IGcodePoint [] interpolateLine ( IGcodePoint point1, IGcodePoint point2 );

    public int getXSteps ();
    public int getYSteps ();

    public double getStepWidthX ();
    public double getStepWidthY ();

    public int getNumProbePoints ();

}
