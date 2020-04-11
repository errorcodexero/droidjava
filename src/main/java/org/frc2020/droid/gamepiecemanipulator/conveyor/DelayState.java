package org.frc2020.droid.gamepiecemanipulator.conveyor;

import java.util.List;

public class DelayState extends BaseState {
    public DelayState(double delay) {
        active_ = false ;
        delay_time_ = delay ;
    }

    public DelayState(String label, double delay) {
        super(label) ;
        active_ = false ;
        delay_time_ = delay ;
    }    

    @Override
    public void addBranchTargets(List<String> targets) {
    }

    @Override
    public ConveyorStateStatus runState(ConveyorStateAction act) {
        ConveyorStateStatus st = ConveyorStateStatus.CurrentState ;

        if (!active_) {
            active_ = true ;
            start_time_ = act.getSubsystem().getRobot().getTime() ;
        }
        else {
            if (act.getSubsystem().getRobot().getTime() - start_time_ > delay_time_)
            {
                st = ConveyorStateStatus.NextState ;
                active_ = false ;
            }
        }

        return st ;
    }

    @Override
    public String humanReadableName() {
        return "DelayState " + delay_time_ ;
    }       

    private boolean active_ ;
    private double start_time_ ;
    private double delay_time_ ;
}