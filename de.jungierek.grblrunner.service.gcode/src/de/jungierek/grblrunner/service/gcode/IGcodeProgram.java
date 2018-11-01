package de.jungierek.grblrunner.service.gcode;

import java.io.File;

public interface IGcodeProgram {

    IGcodeLine [] getAllGcodeLines ();

    public File getGcodeProgramFile ();
    public String getGcodeProgramName ();
    public void loadGcodeProgram ( File gcodeFile );
    public boolean isLoaded ();

    public void setPlayerStart ();
    public void setPlayerStop ();

    public boolean isPlaying ();

    public void clear ();

    public void appendLine ( String line );
    public int getLineCount ();

    public IGcodePoint getMin ();
    public IGcodePoint getMax ();

    public int getDuration ();

    public void parse ();

    public void resetProcessed ();

    public void rotate ( double angle );
    double getRotationAngle ();

    void optimize ();
    boolean isOptimized ();

    // ---------------------------------------------------------------------

    public File getAutolevelDataFile ();

    public void loadAutolevelData ();
    public void saveAutolevelData ();
    public void clearAutolevelData ();

    void prepareAutolevelScan (); // force creation of new grid
    public void prepareAutolevelScan ( int xSteps, int ySteps );
    boolean isAutolevelScanPrepared ();

    void setAutolevelStart ();
    void setAutolevelStop ();

    boolean isAutolevelScan ();

    public int getXSteps ();
    public int getYSteps ();
    public double getStepWidthX ();
    public double getStepWidthY ();
    public int getNumProbePoints ();

    public void setAutolevelScanCompleted ();
    public boolean isAutolevelScanComplete ();

    public void setProbePoint ( IGcodePoint probe );
    public IGcodePoint getProbePointAt ( int ix, int iy );

}
