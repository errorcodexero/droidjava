package org.frc2020.droid.climber;

import org.xero1425.base.actions.Action;
import org.xero1425.base.motorsubsystem.MotorPowerAction;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class ClimberMoveAction extends Action {
    public ClimberMoveAction(ClimberSubsystem sub, double lift, double trav) {
        super(sub.getRobot().getMessageLogger());

        lift_ = new MotorPowerAction(sub.getLifter(), lift) ;
        trav_ = trav;
        sub_ = sub ;
    }

    public ClimberMoveAction(ClimberSubsystem sub, String lift, String trav)
            throws BadParameterTypeException, MissingParameterException {
        super(sub.getRobot().getMessageLogger());

        double power = sub.getRobot().getSettingsParser().get(lift).getDouble() ;
        lift_ = new MotorPowerAction(sub.getLifter(), power) ;
        trav_ = sub.getRobot().getSettingsParser().get(trav).getDouble() ;
    }    

    public ClimberMoveAction(ClimberSubsystem sub, String lift, double trav)
            throws BadParameterTypeException, MissingParameterException {
        super(sub.getRobot().getMessageLogger());

        double power = sub.getRobot().getSettingsParser().get(lift).getDouble() ;
        lift_ = new MotorPowerAction(sub.getLifter(), power) ;
        trav_ = trav ;
    }      

    public ClimberMoveAction(ClimberSubsystem sub, double lift, String trav)
            throws BadParameterTypeException, MissingParameterException {
        super(sub.getRobot().getMessageLogger());

        lift_ = new MotorPowerAction(sub.getLifter(), lift) ;
        trav_ = sub.getRobot().getSettingsParser().get(trav).getDouble() ;
    }      

    @Override
    public void start() throws Exception {
        super.start() ;
        sub_.setAction(lift_) ;
        sub_.setTraverserPower(trav_);
        setDone() ;
    }

    @Override
    public void run() {
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "ClimberMoveAction lifter=" + lift_.getPower() +
                " traverse=" + trav_;
    }

    private ClimberSubsystem sub_ ;
    private MotorPowerAction lift_ ;
    private double trav_ ;
}