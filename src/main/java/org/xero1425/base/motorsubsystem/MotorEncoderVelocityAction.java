package org.xero1425.base.motorsubsystem;

import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.PIDCtrl;

public class MotorEncoderVelocityAction extends MotorAction {
    static private final double MaxPlotDuration = 60.0 ;

    public MotorEncoderVelocityAction(MotorEncoderSubsystem sub, double target)
            throws MissingParameterException, BadParameterTypeException {
        super(sub);

        target_ = target;
        pid_ = new PIDCtrl(sub.getRobot().getSettingsParser(), sub.getName() + ":velocity", false);
        duration_ = Double.MAX_VALUE ;
        plot_id_ = - 1 ;        
    }

    public MotorEncoderVelocityAction(MotorEncoderSubsystem sub, double target, double duration)
            throws MissingParameterException, BadParameterTypeException {
        super(sub);

        target_ = target;
        pid_ = new PIDCtrl(sub.getRobot().getSettingsParser(), sub.getName() + ":velocity", false);
        duration_ = duration ;
        
        if (duration_ <= MaxPlotDuration)
            plot_id_ = sub.initPlot(toString()) ;
        else
            plot_id_ = -1 ;
    }    

    public MotorEncoderVelocityAction(MotorEncoderSubsystem sub, String target)
            throws BadParameterTypeException, MissingParameterException {
        super(sub) ;

        target_ = getSubsystem().getRobot().getSettingsParser().get(target).getDouble() ;
        pid_ = new PIDCtrl(getSubsystem().getRobot().getSettingsParser(), sub.getName() + ":velocity", false);
        duration_ = Double.MAX_VALUE ;
        plot_id_ = - 1 ;
    }

    public MotorEncoderVelocityAction(MotorEncoderSubsystem sub, String target, String duration)
            throws BadParameterTypeException, MissingParameterException {
        super(sub) ;

        target_ = getSubsystem().getRobot().getSettingsParser().get(target).getDouble() ;
        pid_ = new PIDCtrl(getSubsystem().getRobot().getSettingsParser(), sub.getName() + ":velocity", false);
        duration_ = getSubsystem().getRobot().getSettingsParser().get(duration).getDouble() ;

        if (duration_ <= MaxPlotDuration)
            plot_id_ = sub.initPlot(toString()) ;
        else
            plot_id_ = -1 ;
    }    

    public void setTarget(double target) {
        target_ = target ;
    }

    public double getTarget() {
        return target_ ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        pid_.reset() ;
        start_ = getSubsystem().getRobot().getTime() ;

        if (plot_id_ != -1)
            getSubsystem().startPlot(plot_id_, columns_) ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;

        MotorEncoderSubsystem me = (MotorEncoderSubsystem)getSubsystem() ;
        double out = pid_.getOutput(target_, me.getVelocity(), getSubsystem().getRobot().getDeltaTime()) ;
        getSubsystem().setPower(out) ;

        MessageLogger logger = getSubsystem().getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID()) ;
        logger.add("MotorEncoderVelocityAction:") ;
        logger.add("target", target_) ;
        logger.add("actual", me.getVelocity()) ;
        logger.add("output", out) ;
        logger.add("encoder", me.getEncoderRawCount()) ;
        logger.endMessage();

        if (plot_id_ != -1) {
            Double[] data = new Double[columns_.length] ;
            data[0] = getSubsystem().getRobot().getTime() - start_ ;
            data[1] = me.getPosition() ;
            data[2] = me.getVelocity() ;
            data[3] = me.getAcceleration() ;
            getSubsystem().addPlotData(plot_id_, data);
        }

        if (getSubsystem().getRobot().getTime() - start_ > duration_) {
            setDone() ;
            getSubsystem().setPower(0.0);
        }
    }

    @Override
    public void cancel() {
        super.cancel() ;
        getSubsystem().setPower(0.0);
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "MotorEncoderVelocityAction " + Double.toString(target_) ;
    }

    private double target_ ;
    private double duration_ ;
    private double start_ ;
    private PIDCtrl pid_ ;
    private int plot_id_ ;
    private static String [] columns_ = { "time", "pos", "velocity", "accel" }  ;
}