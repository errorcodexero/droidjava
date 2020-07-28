package org.frc2020.droid.turret;

import org.frc2020.droid.droidlimelight.DroidLimeLightSubsystem;
import org.frc2020.droid.targettracker.TargetTrackerSubsystem;
import org.xero1425.base.limelight.LimeLightSubsystem.LedMode;
import org.xero1425.base.motorsubsystem.MotorAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.PIDCtrl;

public class FollowTargetAction extends MotorAction {
    public FollowTargetAction(TurretSubsystem sub, DroidLimeLightSubsystem ll, TankDriveSubsystem db, TargetTrackerSubsystem tracker)
            throws BadParameterTypeException, MissingParameterException {
        super(sub);

        db_ = db ;
        ll_ = ll;
        sub_ = sub ;
        tracker_ = tracker ;
        threshold_ = sub.getRobot().getSettingsParser().get("turret:fire_threshold").getDouble() ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;
        pid_ = new PIDCtrl(getSubsystem().getRobot().getSettingsParser(), "turret:follow", false);
        ll_.setLedMode(LedMode.ForceOn);
    }

    @Override
    public void run() {
        double error = tracker_.getTurretError() ;
        double out = pid_.getOutput(0, error, sub_.getRobot().getDeltaTime()) ;
        sub_.setPower(out) ;

        boolean ready = Math.abs(error) < threshold_ ;
        sub_.setReadyToFire(ready) ;

        MessageLogger logger = sub_.getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, sub_.getLoggerID()) ;
        logger.add("FollowTargetAction:") ;
        logger.add(" error", error) ;
        logger.add(" output", out) ;
        logger.add(" ready", ready) ;
        logger.endMessage();
    }

    @Override
    public void cancel() {
        super.cancel() ;
        ll_.setLedMode(LedMode.ForceOff);
        sub_.setPower(0.0) ;
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "FollowTargetAction" ;
    }

    double threshold_ ;
    private PIDCtrl pid_ ;
    TurretSubsystem sub_ ;
    DroidLimeLightSubsystem ll_ ;
    TankDriveSubsystem db_ ;
    TargetTrackerSubsystem tracker_ ;
}