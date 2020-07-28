package org.frc2020.droid.gamepiecemanipulator;

import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorStopAction;
import org.frc2020.droid.gamepiecemanipulator.intake.CollectOffAction;
import org.xero1425.base.actions.Action;

public class StopCollectAction extends Action {
    public StopCollectAction(GamePieceManipulatorSubsystem gp) throws Exception {
        super(gp.getRobot().getMessageLogger()) ;

        stop_ = new ConveyorStopAction(gp.getConveyor()) ;
        collect_ = new CollectOffAction(gp.getIntake()) ;

        sub_ = gp ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        sub_.getIntake().setAction(collect_, true) ;
        sub_.getConveyor().setAction(stop_, true) ;
    }

    @Override
    public void run() {
        if (!sub_.getIntake().isBusy() && !sub_.getConveyor().isBusy())
            setDone() ;
    }

    @Override
    public void cancel() {
        super.cancel() ;

        if (sub_.getIntake().isBusy())
            sub_.getIntake().cancelAction();

        if (sub_.getConveyor().isBusy())
            sub_.getConveyor().cancelAction();
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "StartCollectAction" ;
    }

    private GamePieceManipulatorSubsystem sub_ ;
    private ConveyorStopAction stop_ ;
    private CollectOffAction collect_ ;
}