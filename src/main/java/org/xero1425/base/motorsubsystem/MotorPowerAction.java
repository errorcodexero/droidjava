package org.xero1425.base.motorsubsystem ;

import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class MotorPowerAction extends MotorAction {
    public MotorPowerAction(MotorSubsystem motor, double power) {
        super(motor);

        power_ = power;
        timed_ = false;
    }

    public MotorPowerAction(MotorSubsystem motor, String power)
            throws BadParameterTypeException, MissingParameterException {
        super(motor);

        power_ = motor.getRobot().getSettingsParser().get(power).getDouble();
        timed_ = false;
    }

    public MotorPowerAction(MotorSubsystem motor, double power, double duration) {
        super(motor);

        power_ = power;
        duration_ = duration;
        timed_ = true;
    }

    public MotorPowerAction(MotorSubsystem motor, String power, String duration)
            throws BadParameterTypeException, MissingParameterException {

        super(motor);
        power_ = motor.getRobot().getSettingsParser().get(power).getDouble();
        duration_ = motor.getRobot().getSettingsParser().get(duration).getDouble();
        timed_ = true;
    }

    public double getPower() {
        return power_ ;
    }

    public double getDuration() {
        return duration_ ;
    }

    public boolean isTimed() {
        return timed_ ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        try {
            getSubsystem().setPower(power_) ;
            if (timed_)
                start_ = getSubsystem().getRobot().getTime() ;
            else
                setDone() ;
        }
        catch(Exception ex) {
        }
    }

    @Override
    public void run() {
        if (timed_) {
            if (getSubsystem().getRobot().getTime() - start_ > duration_)
            {
                getSubsystem().setPower(0.0) ;
                setDone() ;
            }
        }
    }

    @Override
    public void cancel() {
        super.cancel() ;
        getSubsystem().setPower(0.0) ;
    }

    public String toString(int indent) {
        String ret = prefix(indent) + "MotorPowerAction" ;
        ret += " power=" + Double.toString(power_) ;
        if (timed_)
            ret += " duration=" + Double.toString(duration_) ;

        return ret ;
    }

    private double power_ ;
    private double start_ ;
    private double duration_ ;
    private boolean timed_ ;
} ;