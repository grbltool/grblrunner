package de.jungierek.grblrunner.service.webcam;

import com.github.sarxos.webcam.Webcam;

public interface IWebcamService {
    
    public static final double MIN_FREQUENCY = 0.016; // 1 frame per minute
    public static final double MAX_FREQUENCY = 50; // 50 frames per second

    public void detectWebcams ();
    public void detectWebcamsAsync ();

    public Webcam [] getWebcams ();

    public void setWebcam ( Webcam webcam );
    public Webcam getWebcam ();

    public void setViewSize ( String resolution );
    String getWebcamSize ();
    public String [] getWebcamSizes ();

    public void start ();
    public void stop ();

    public void setFPS ( double frequency );
    public double getFPS ();

    public void setReceiver ( IWebcamServiceReceiver receiver );

}
