package de.jungierek.grblrunner.service.webcam;

import java.awt.image.BufferedImage;

public interface IWebcamServiceReceiver {
    
    public void received ( BufferedImage image );

}
