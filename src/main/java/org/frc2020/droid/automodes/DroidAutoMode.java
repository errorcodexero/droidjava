package org.frc2020.droid.automodes;

import org.frc2020.droid.droidlimelight.DroidLimeLightSubsystem;
import org.frc2020.droid.droidsubsystem.DroidRobotSubsystem;
import org.frc2020.droid.gamepiecemanipulator.FireAction;
import org.frc2020.droid.gamepiecemanipulator.GamePieceManipulatorSubsystem;
import org.frc2020.droid.gamepiecemanipulator.StartCollectAction;
import org.frc2020.droid.gamepiecemanipulator.StopCollectAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToEmitAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToReceiveAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorSetBallCountAction;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorSubsystem;
import org.frc2020.droid.gamepiecemanipulator.shooter.ShooterSubsystem;
import org.frc2020.droid.gamepiecemanipulator.shooter.ShooterVelocityAction;
import org.frc2020.droid.gamepiecemanipulator.shooter.ShooterSubsystem.HoodPosition;
import org.frc2020.droid.targettracker.TargetTrackerSubsystem;
import org.frc2020.droid.turret.FollowTargetAction;
import org.frc2020.droid.turret.TurretSubsystem;
import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.actions.ParallelAction;
import org.xero1425.base.actions.SequenceAction;
import org.xero1425.base.controllers.AutoMode;
import org.xero1425.base.motorsubsystem.MotorEncoderGotoAction;
import org.xero1425.base.tankdrive.TankDriveFollowPathAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class DroidAutoMode extends AutoMode {
    public DroidAutoMode(DroidAutoController ctrl, String name) {
        super(ctrl, name);
    }

    protected DroidRobotSubsystem getDroidSubsystem() {
        return (DroidRobotSubsystem) getAutoController().getRobot().getRobotSubsystem();
    }

    protected void setInitialBallCount(int count) throws InvalidActionRequest {
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor();
        addSubActionPair(conveyor, new ConveyorSetBallCountAction(conveyor, count), false);
    }

    protected void driveAndFire(String path, boolean reverse, double angle) throws Exception {
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor() ; 
        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ;
        ShooterSubsystem shooter = getDroidSubsystem().getGamePieceManipulator().getShooter() ;
        TurretSubsystem turret = getDroidSubsystem().getTurret() ;
        TargetTrackerSubsystem tracker = getDroidSubsystem().getTracker() ;
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator() ;
        ParallelAction parallel ;

        parallel = new ParallelAction(getAutoController().getRobot().getMessageLogger()) ;
        parallel.addAction(setTurretToTrack(angle)) ;

        parallel.addSubActionPair(conveyor, new ConveyorPrepareToEmitAction(conveyor), false);
        if (path != null)
            parallel.addSubActionPair(db, new TankDriveFollowPathAction(db, path, reverse), true);
        parallel.addSubActionPair(shooter, new ShooterVelocityAction(shooter, 4150, HoodPosition.Down), false);

        addAction(parallel);
        addSubActionPair(gp, new FireAction(gp, tracker, turret, db), true);
    }

    protected void driveAndCollect(String path) throws Exception {
        ConveyorSubsystem conveyor = getDroidSubsystem().getGamePieceManipulator().getConveyor() ; 
        GamePieceManipulatorSubsystem gp = getDroidSubsystem().getGamePieceManipulator() ;
        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ;
        ParallelAction parallel ;
        SequenceAction series ;

        parallel = new ParallelAction(getAutoController().getRobot().getMessageLogger()) ;

        parallel.addSubActionPair(db, new TankDriveFollowPathAction(db, path, false), true);

        series = new SequenceAction(getAutoController().getRobot().getMessageLogger()) ;
        series.addSubActionPair(conveyor, new ConveyorPrepareToReceiveAction(conveyor), true);
        series.addSubActionPair(gp, new StartCollectAction(gp), false);
        parallel.addAction(series) ;
        addAction(parallel) ;

        addSubActionPair(gp, new StopCollectAction(gp), true);
    }

    private SequenceAction setTurretToTrack(double angle) throws InvalidActionRequest, BadParameterTypeException, MissingParameterException {
        TurretSubsystem turret = getDroidSubsystem().getTurret() ;
        DroidLimeLightSubsystem ll = getDroidSubsystem().getLimeLight() ;
        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ;
        TargetTrackerSubsystem tracker = getDroidSubsystem().getTracker() ;
        
        SequenceAction seq = new SequenceAction(getAutoController().getRobot().getMessageLogger()) ;
        seq.addSubActionPair(turret, new MotorEncoderGotoAction(turret, angle, false), true) ;
        seq.addSubActionPair(turret, new FollowTargetAction(turret, ll, db, tracker), false);

        return seq ;
    }    
}