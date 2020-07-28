package org.frc2020.droid.gamepiecemanipulator;

import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorReceiveAction;
import org.frc2020.droid.gamepiecemanipulator.intake.CollectOnAction;
import org.xero1425.base.actions.Action;

public class StartCollectAction extends Action {
    public StartCollectAction(GamePieceManipulatorSubsystem gp) throws Exception {
        super(gp.getRobot().getMessageLogger()) ;

        receive_ = new ConveyorReceiveAction(gp.getConveyor()) ;
        collect_ = new CollectOnAction(gp.getIntake()) ;

        sub_ = gp ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        sub_.getIntake().setAction(collect_, true) ;
        sub_.getConveyor().setAction(receive_, true) ;
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
    private ConveyorReceiveAction receive_ ;
    private CollectOnAction collect_ ;
}