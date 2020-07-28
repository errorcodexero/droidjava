package org.xero1425.base.actions;

import edu.wpi.first.wpilibj.Timer ;
import org.xero1425.base.XeroRobot;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class DelayAction extends Action {
    public DelayAction(XeroRobot robot, double delay) {
        super(robot.getMessageLogger());
        delay_ = delay;
    }

    public DelayAction(XeroRobot robot, String delay) throws BadParameterTypeException, MissingParameterException {
        super(robot.getMessageLogger()) ;
        delay_ = robot.getSettingsParser().get(delay).getDouble() ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;
        start_time_ = Timer.getFPGATimestamp() ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;
        if (Timer.getFPGATimestamp() - start_time_ > delay_)
            setDone() ;
    }

    @Override
    public void cancel() {
        super.cancel() ;
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "DelayAction " + Double.toString(delay_) ;
    }

    private double delay_ ;
    private double start_time_ ;
}