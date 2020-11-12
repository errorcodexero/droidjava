package org.xero1425.base.motorsubsystem;

import org.xero1425.base.XeroRobot;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.PIDACtrl;
import org.xero1425.misc.SettingsParser;
import org.xero1425.misc.TrapezoidalProfile;
import org.xero1425.misc.XeroMath;

public class MotorEncoderGotoAction extends MotorAction {
    public MotorEncoderGotoAction(MotorEncoderSubsystem sub, double target, boolean addhold)
            throws BadParameterTypeException, MissingParameterException {
        super(sub) ;
        target_ = target ;
        addhold_ = addhold ;

        SettingsParser settings = sub.getRobot().getSettingsParser() ;
        profile_ = new TrapezoidalProfile(settings, sub.getName() + ":goto") ;
        plot_id_ = sub.initPlot(sub.getName() + "-" + toString(0)) ;
    }

    public MotorEncoderGotoAction(MotorEncoderSubsystem sub, String target, boolean addhold)
            throws BadParameterTypeException, MissingParameterException {
        super(sub) ;
        target_ = getSubsystem().getRobot().getSettingsParser().get(target).getDouble() ;

        SettingsParser settings = sub.getRobot().getSettingsParser() ;
        profile_ = new TrapezoidalProfile(settings, sub.getName() + ":goto") ;
        plot_id_ = sub.initPlot(sub.getName() + "-" + toString(0)) ;        
    }

    public void start() throws Exception {
        super.start() ;
        setTarget() ;
        getSubsystem().startPlot(plot_id_, plot_columns_) ;
    }

    public void run() throws Exception {
        super.run() ;

        MotorEncoderSubsystem sub = (MotorEncoderSubsystem)getSubsystem() ;
        XeroRobot robot = sub.getRobot() ;

        double dt = robot.getDeltaTime() ;
        double elapsed = robot.getTime() - start_time_ ;
        double position = sub.getPosition() ;
        double traveled = normalizePosition(sub, position - start_position_) ;

        if (elapsed > profile_.getTotalTime())
        {
            setDone() ;
            sub.setPower(0.0) ;
            sub.endPlot(plot_id_);
        }
        else
        {
            double targetDist = profile_.getDistance(elapsed) ;
            double targetVel = profile_.getVelocity(elapsed) ;
            double targetAcc = profile_.getAccel(elapsed) ;
            double out = ctrl_.getOutput(targetAcc, targetVel, targetDist, traveled, dt) ;
            sub.setPower(out) ;

            Double[] data = new Double[plot_columns_.length] ;
            data[0] = elapsed ;
            data[1] = start_position_ + targetDist ;
            data[2] = position ;
            data[3] = targetVel ;
            data[4] = sub.getVelocity() ;
            data[5] = out ;
            sub.addPlotData(plot_id_, data);
        }
    }

    public void cancel() {
        super.cancel() ;
        getSubsystem().setPower(0.0) ;
    }

    public String toString(int indent) {
        return prefix(indent) + "MotorEncoderGotoAction " + getSubsystem().getName() + " " + Double.toString(target_) ;
    }

    private double normalizePosition(MotorEncoderSubsystem me, double pos) {
        if (me.isAngular())
            return XeroMath.normalizeAngleDegrees(pos) ;
        
        return pos ;
    }

    private void setTarget() throws BadParameterTypeException, MissingParameterException {
        MotorEncoderSubsystem sub = (MotorEncoderSubsystem)getSubsystem() ;
        if (addhold_)
            sub.setDefaultAction(new MotorEncoderHoldAction(sub, target_)) ;

        double dist = normalizePosition(sub, target_ - sub.getPosition()) ;
        if (Math.abs(dist) < threshold_)
        {
            setDone() ;
        }
        else
        {
            String config = sub.getName() + ":follower" ;
            SettingsParser settings = sub.getRobot().getSettingsParser() ;
            if (dist < 0)
                ctrl_ = new PIDACtrl(settings, config + ":down", sub.isAngular());
            else
                ctrl_ = new PIDACtrl(settings, config + ":up", sub.isAngular()) ;

            profile_.update(dist, 0, 0) ;
            start_time_ = sub.getRobot().getTime() ;
            start_position_ = sub.getPosition() ;
        }
    }

    private double threshold_ ;
    private double target_ ;
    private double start_time_ ;
    private double start_position_ ;
    PIDACtrl ctrl_ ;
    TrapezoidalProfile profile_ ;
    boolean addhold_ ;

    int plot_id_ ;
    static final String [] plot_columns_ = { "time", "tpos", "apos", "tvel", "avel", "out" } ;
}

