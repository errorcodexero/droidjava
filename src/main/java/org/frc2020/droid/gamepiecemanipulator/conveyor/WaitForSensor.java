package org.frc2020.droid.gamepiecemanipulator.conveyor;

import java.util.List;

import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;

public class WaitForSensor extends BaseState {
    public enum SensorEvent {
        IS_LOW,
        IS_HIGH,
        LOW_TO_HIGH,
        HIGH_TO_LOW
    } ;

    public WaitForSensor(ConveyorSubsystem.Sensor s, SensorEvent ev) {
        sensor_ = s ;
        event_ = ev ;
        timeout_duration_ = Double.MAX_VALUE ;
    }

    public WaitForSensor(ConveyorSubsystem.Sensor s, SensorEvent ev, String label, double timeout) {
        sensor_ = s ;
        event_ = ev ;
        timeout_duration_ = timeout ;
    }    

    public WaitForSensor(String label, ConveyorSubsystem.Sensor s, SensorEvent ev) {
        super(label) ;
        sensor_ = s ;
        event_ = ev ;
        timeout_duration_ = Double.MAX_VALUE ;
    }

    public WaitForSensor(String label, ConveyorSubsystem.Sensor s, SensorEvent ev, String timeoutlabel, double timeout) {
        super(label) ;
        sensor_ = s ;
        event_ = ev ;
        timeout_label_ = timeoutlabel ;
        timeout_duration_ = timeout ;
    }       

    @Override
    public void addBranchTargets(List<String> targets) {
        if (timeout_label_ != null && !targets.contains(timeout_label_))
            targets.add(timeout_label_) ;
    }        

    @Override
    public ConveyorStateStatus runState(ConveyorStateAction act) {
        ConveyorStateStatus st = ConveyorStateStatus.CurrentState ;

        if (!active_) {
            active_ = true ;
            start_ = act.getSubsystem().getRobot().getTime() ;
        }
        else {
            if (act.getSubsystem().getRobot().getTime() - start_ > timeout_duration_)
            {
                MessageLogger logger = act.getSubsystem().getRobot().getMessageLogger() ;
                logger.startMessage(MessageType.Debug, act.getSubsystem().getLoggerID()) ;
                logger.add("state ").addQuoted(toString()).add(" timed out after ") ;
                logger.add(timeout_duration_).add(" seconds").endMessage() ;
                st = new ConveyorStateStatus(timeout_label_) ;
            }
        }

        if (st.getType() == ConveyorStateStatus.StateStatusType.CurrentState)
        {
            switch(event_) {
                case IS_LOW:
                    if (!act.getSubsystem().getSensorState(sensor_))
                        st = ConveyorStateStatus.NextState ;
                    break ;
                case IS_HIGH:
                    if (act.getSubsystem().getSensorState(sensor_))
                        st = ConveyorStateStatus.NextState ;
                    break ;
                case LOW_TO_HIGH:
                    if (act.getSubsystem().didSensorLowToHigh(sensor_))
                        st = ConveyorStateStatus.NextState ;
                    break ;
                case HIGH_TO_LOW:
                    if (act.getSubsystem().didSensorHighToLow(sensor_))
                        st = ConveyorStateStatus.NextState ;
                    break ;                                
            }
        }

        if (st.getType() == ConveyorStateStatus.StateStatusType.NextState) {
            active_ = false ;
        }

        return st ;
    }

    @Override
    public String humanReadableName() {
        String ret = "WaitForSensor " + sensor_.toString() + " " + event_.toString() ;
        return ret ;
    }   

    private ConveyorSubsystem.Sensor sensor_ ;
    private SensorEvent event_ ;
    private String timeout_label_ ;
    private double timeout_duration_ ;
    private double start_ ;
    private boolean active_ ;
}