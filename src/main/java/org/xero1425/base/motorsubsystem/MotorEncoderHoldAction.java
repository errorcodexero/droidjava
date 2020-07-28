package org.xero1425.base.motorsubsystem;

import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.PIDCtrl;

public class MotorEncoderHoldAction extends MotorAction {
    public MotorEncoderHoldAction(MotorEncoderSubsystem subsystem) {
        super(subsystem);
        has_explicit_target_ = false;
    }

    public MotorEncoderHoldAction(MotorEncoderSubsystem subsystem, double target) {
        super(subsystem);
        has_explicit_target_ = true;
        target_ = target;
    }

    public MotorEncoderHoldAction(MotorEncoderSubsystem subsystem, String target)
            throws BadParameterTypeException, MissingParameterException {
        super(subsystem) ;
        has_explicit_target_ = true ;
        target_ = subsystem.getRobot().getSettingsParser().get(target).getDouble() ;
    }      

    public double getTarget() {
        return target_ ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        MotorEncoderSubsystem me = (MotorEncoderSubsystem)getSubsystem();
        if (!has_explicit_target_)
            target_ = me.getPosition() ;

        pid_ = new PIDCtrl(me.getRobot().getSettingsParser(), me.getName() + ":hold", me.isAngular()) ;
    }

    @Override
    public void run() {
        MotorEncoderSubsystem me = (MotorEncoderSubsystem)getSubsystem();
        double out = pid_.getOutput(target_, me.getPosition(), me.getRobot().getDeltaTime()) ;
        me.setPower(out) ;
    }

    @Override
    public void cancel() {
        super.cancel() ;
        getSubsystem().setPower(0.0) ;
    }

    @Override
    public String toString(int indent) {
        String ret = prefix(indent) + "MotorEncoderHoldAction" ;
        if (has_explicit_target_)
            ret += "-explicit" ;
        else
            ret += "-implicit" ;
        ret += " target=" + Double.toString(target_) ;
        return ret ;
    }

    private boolean has_explicit_target_ ;
    private double target_ ;
    private PIDCtrl pid_ ;
} ;