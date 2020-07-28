package org.frc2020.droid.gamepiecemanipulator.conveyor;

import org.xero1425.base.actions.Action;

public class ConveyorStopAction extends Action {
    public ConveyorStopAction(ConveyorSubsystem sub) {
        super(sub.getRobot().getMessageLogger()) ;
        
        sub_ = sub ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        count_++ ;

        if (count_ == 3) {
            System.out.println("done") ;
        }
        sub_.setMotorsPower(0.0, 0.0) ;
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
        return spaces(indent) + "ConveyorStopAction" ;
    }

    ConveyorSubsystem sub_ ;
    static int count_ = 0 ;
} ;