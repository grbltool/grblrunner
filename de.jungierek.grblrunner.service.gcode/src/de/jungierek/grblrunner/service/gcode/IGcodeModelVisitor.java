package de.jungierek.grblrunner.service.gcode;

public interface IGcodeModelVisitor {
    
    public void visit ( IGcodeLine gcodeLine );

}
