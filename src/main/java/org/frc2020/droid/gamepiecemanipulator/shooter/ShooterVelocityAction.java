package org.frc2020.droid.gamepiecemanipulator.shooter;

import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem;
import org.xero1425.base.motorsubsystem.MotorEncoderVelocityAction;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.XeroMath;

public class ShooterVelocityAction extends MotorEncoderVelocityAction {
    public ShooterVelocityAction(ShooterSubsystem shooter, double target, ShooterSubsystem.HoodPosition pos)
            throws BadParameterTypeException, MissingParameterException {
        super(shooter, target) ;

        ready_percent_ = shooter.getRobot().getSettingsParser().get("shooter:velocity:ready_margin_percent").getDouble() ;
        pos_ = pos ;
        sub_ = shooter ;
    }

    @Override
    public void setTarget(double target) {
        super.setTarget(target) ;
        updateReadyToFire() ;
    }

    public void setHoodPosition(ShooterSubsystem.HoodPosition pos) {
        pos_ = pos ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;
        if (sub_.getHood() != pos_)
            sub_.setHood(pos_);

        updateReadyToFire() ;

        MotorEncoderSubsystem me = (MotorEncoderSubsystem)getSubsystem() ;
        MessageLogger logger = getSubsystem().getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID()) ;
        logger.add("shooter velocity:") ;
        logger.add("target", getTarget()) ;
        logger.add("actual", me.getVelocity()) ;
        logger.endMessage();
    }

    @Override
    public void cancel() {
        super.cancel() ;
    }

    @Override
    public String toString() {
        String ret = "ShooterVelocityAction" ;
        ret += " target = " +  Double.toString(getTarget()) ;
        return ret ;
    }

    private void updateReadyToFire() {
        if (getTarget() > 0.01 && XeroMath.equalWithinPercentMargin(getTarget(), sub_.getVelocity(), ready_percent_))
            sub_.setReadyToFire(true);
        else
            sub_.setReadyToFire(false);
    }

    ShooterSubsystem sub_ ;
    private double ready_percent_ ;
    private ShooterSubsystem.HoodPosition pos_ ;
}