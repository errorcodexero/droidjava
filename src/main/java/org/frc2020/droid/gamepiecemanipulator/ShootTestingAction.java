package org.frc2020.droid.gamepiecemanipulator;

import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorEmitAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToEmitAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToReceiveAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorReceiveAction;
import org.frc2020.droid.gamepiecemanipulator.shooter.ShooterSubsystem;
import org.frc2020.droid.gamepiecemanipulator.shooter.ShooterVelocityAction;
import edu.wpi.first.wpilibj.shuffleboard.* ;
import org.xero1425.base.actions.Action;

public class ShootTestingAction extends Action {
    public ShootTestingAction(GamePieceManipulatorSubsystem gp, ShooterSubsystem.HoodPosition pos)
            throws Exception {
        super(gp.getRobot().getMessageLogger()) ;

        sub_ = gp ;

        widget_ = makeWidget() ;
        fire_ = new ShooterVelocityAction(gp.getShooter(), 0, pos) ;
        prepare_receive_ = new ConveyorPrepareToReceiveAction(gp.getConveyor()) ;
        receive_ = new ConveyorReceiveAction(gp.getConveyor()) ;
        prepare_emit_ = new ConveyorPrepareToEmitAction(gp.getConveyor()) ;
        emit_ = new ConveyorEmitAction(gp.getConveyor()) ;

        shoot_delay_ = gp.getRobot().getSettingsParser().get("shoottest:shoot_delay").getDouble() ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        state_ = State.WaitPrepareReceive ;
        fire_.setTarget(0.0);
        sub_.getShooter().setAction(fire_, true) ;
        sub_.getConveyor().setAction(prepare_receive_, true) ;
    }

    @Override
    public void run() {
        double current = sub_.getShooter().getVelocity() ;
        double target = widget_.getEntry().getDouble(current) ;

        if (Math.abs(current - target) < 5) {
            fire_.setTarget(target) ;
        }

        switch(state_) {
            case WaitPrepareReceive:
                if (!sub_.getConveyor().isBusy()) {
                    state_ = State.WaitReceive ;
                    sub_.getConveyor().setAction(receive_, true) ;
                }
                break ;

            case WaitReceive:
                if (!sub_.getConveyor().isBusy()) {
                    state_ = State.WaitPrepareShoot ;
                    sub_.getConveyor().setAction(prepare_emit_, true) ;
                }
                break ;

            case WaitPrepareShoot:
                if (!sub_.getConveyor().isBusy()) {
                    state_ = State.WaitShootDelay ;
                    start_ = sub_.getRobot().getTime() ;
                }
                break ;

            case WaitShootDelay:
                if (sub_.getRobot().getTime() - start_ > shoot_delay_) {
                    state_ = State.WaitShoot ;
                    sub_.getConveyor().setAction(emit_, true) ;
                }
                break ;

            case WaitShoot:
                if (!sub_.getConveyor().isBusy()) {
                    state_ = State.WaitPrepareReceive ;
                    sub_.getConveyor().setAction(prepare_receive_, true) ;
                }
                break ;            
        }
    }

    @Override
    public void cancel() {
        super.cancel() ;
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "ShootTestingAction" ;
    }

    private SimpleWidget makeWidget() {
        return Shuffleboard.getTab("ShootTest").add("Velocity", 0.0).withWidget(BuiltInWidgets.kTextView) ;
    }

    private enum State {
        WaitPrepareReceive,
        WaitReceive,
        WaitPrepareShoot,
        WaitShootDelay,
        WaitShoot,        
    } ;

    private GamePieceManipulatorSubsystem sub_ ;
    private State state_ ;
    private ShooterVelocityAction fire_ ;
    private ConveyorPrepareToReceiveAction prepare_receive_ ;
    private ConveyorPrepareToEmitAction prepare_emit_ ;
    private ConveyorReceiveAction receive_ ;
    private ConveyorEmitAction emit_ ;
    private double shoot_delay_ ;
    private double start_ ;
    private SimpleWidget widget_ ;
}