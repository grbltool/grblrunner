package de.jungierek.grblrunner.service.gcode.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;

public class GcodeGrblStateImpl implements IGcodeGrblState {
    
    private static final Logger LOG = LoggerFactory.getLogger ( GcodeGrblStateImpl.class );

    private EGrblState state;
    private GcodePointImpl machine; 
    private GcodePointImpl work;
    
    private int plannerBufferSize;
    private int rxBufferSize;

    private double feedRate;
    private double spindleSpeed;

    private String pinState;

    public GcodeGrblStateImpl ( EGrblState state, GcodePointImpl machineCoordinates, GcodePointImpl workCoordinates ) {
        
        this.state = state;
        this.machine = machineCoordinates;
        this.work = workCoordinates;
        
    }

    @Deprecated
    public GcodeGrblStateImpl ( EGrblState state, GcodePointImpl machineCoordinates, GcodePointImpl workCoordinates, int plannerBufferSize, int rxBufferSize ) {

        this ( state, machineCoordinates, workCoordinates );

        this.plannerBufferSize = plannerBufferSize;
        this.rxBufferSize = rxBufferSize;

    }

    @Override
    public void setAvailablePlannerBufferSize ( int size ) {

        this.plannerBufferSize = size;

    }

    @Override
    public void setAvailableRxBufferSize ( int size ) {

        this.rxBufferSize = size;

    }

    @Override
    public void setFeedRate ( double feedRate ) {

        this.feedRate = feedRate;

    }

    @Override
    public void setSpindleSpeed ( double spindleSpeed ) {

        this.spindleSpeed = spindleSpeed;

    }

    @Override
    public void setPinState ( String state ) {

        this.pinState = state;

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
    public int getAvailablePlannerBufferSize () {

        return plannerBufferSize;

    }
    
    @Override
    public int getAvailableRxBufferSize () {

        return rxBufferSize;

    }
    
    @Override
    public double getFeedRate () {

        return feedRate;

    }

    @Override
    public double getSpindleSpeed () {

        return spindleSpeed;

    }

    @Override
    public String getPinState () {

        return pinState;

    }

    @Override
    public String toString () {
        
        return "GcodeState[" + state + ",m:" + machine + ",w:" + work + ",f:" + feedRate + ",s:" + spindleSpeed + (pinState == null ? "" : ",p:" + pinState) + "]";
        
    }
    
    @Override
    public boolean equals ( Object obj ) {

        if ( obj == null || !(obj instanceof GcodeGrblStateImpl) ) return false;
        if ( obj == this ) return true;
        
        GcodeGrblStateImpl s = (GcodeGrblStateImpl) obj;
        
        boolean result = s.state.equals ( this.state );
        result = result && s.machine.equals ( this.machine );
        result = result && s.work.equals ( this.work );
        result = result && s.rxBufferSize == this.rxBufferSize;
        result = result && s.plannerBufferSize == this.plannerBufferSize;
        result = result && s.feedRate == this.feedRate;
        result = result && s.spindleSpeed == this.spindleSpeed;
        result = result && (s.pinState == null ? true : s.pinState.equals ( this.pinState ));

        return result;
        
    }

}
