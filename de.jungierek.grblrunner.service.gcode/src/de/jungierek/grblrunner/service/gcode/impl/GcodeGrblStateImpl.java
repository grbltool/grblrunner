package de.jungierek.grblrunner.service.gcode.impl;

import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;

public class GcodeGrblStateImpl implements IGcodeGrblState {
    
    private EGrblState state;
    private GcodePointImpl machine; 
    private GcodePointImpl work; 

    public GcodeGrblStateImpl ( EGrblState state, GcodePointImpl machineCoordinates, GcodePointImpl workCoordinates ) {
        
        this.state = state;
        this.machine = machineCoordinates;
        this.work = workCoordinates;
        
    }

    @Override
    public EGrblState getGrblState () {
        
        return state;
        
    }
    
    @Override
    public IGcodePoint getMachineCoordindates () {
        
        return machine;
        
    }

    @Override
    public IGcodePoint getWorkCoordindates () {
        
        return work;
        
    }
    
    @Override
    public String toString () {
        
        return "GcodeState[" + state + ",m:" + machine + ",w:" + work + "]";
        
    }
    
    @Override
    public boolean equals ( Object obj ) {

        if ( obj == null || !(obj instanceof GcodeGrblStateImpl) ) return false;
        if ( obj == this ) return true;
        
        GcodeGrblStateImpl s = (GcodeGrblStateImpl) obj;
        
        return s.state.equals ( this.state ) && s.machine.equals ( this.machine ) && s.work.equals ( this.work );
        
    }

}
