package org.frc2020.droid.gamepiecemanipulator.conveyor;

import org.xero1425.base.actions.Action;

public class ConveyorSetBallCountAction extends Action {
    public ConveyorSetBallCountAction(ConveyorSubsystem sub, int count) {
        super(sub.getRobot().getMessageLogger()) ;
        
        sub_ = sub ;
        count_ = count ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;
        sub_.setBallCount(count_);
        setDone() ;
    }

    @Override
    public void run() {
    }

    @Override
    public void cancel() {
        super.cancel() ;
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "ConveyorSetBallCountAction " + count_ ;
    }

    ConveyorSubsystem sub_ ;
    int count_ ;
} ;