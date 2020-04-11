package org.xero1425.base.motorsubsystem;

import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class MotorEncoderPowerAction extends MotorPowerAction
{
    public MotorEncoderPowerAction(MotorEncoderSubsystem motor, double power) {
        super(motor, power);
        plot_id_ = -1 ;
    }

    public MotorEncoderPowerAction(MotorEncoderSubsystem motor, String power)
            throws BadParameterTypeException, MissingParameterException {
        super(motor, power);
        plot_id_ = -1 ;
    }

    public MotorEncoderPowerAction(MotorEncoderSubsystem motor, double power, double duration) {
        super(motor, power, duration);
        plot_id_ = motor.initPlot(toString()) ;
    }

    public MotorEncoderPowerAction(MotorEncoderSubsystem motor, String power, String duration)
            throws BadParameterTypeException, MissingParameterException {

        super(motor, power, duration);
        plot_id_ = motor.initPlot(toString()) ;        
    }

    @Override
    public String toString() {
        String ret ;

        ret = "MotorEncoderPowerAction " + getSubsystem().getName() ;
        if (isTimed())
            ret += " " + getPower() + " " + getDuration() ;

        return ret ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        start_ = getSubsystem().getRobot().getTime() ;
        getSubsystem().startPlot(plot_id_, plot_columns_);
    }

    @Override
    public void run() {
        Double [] data = new Double[plot_columns_.length] ;
        data[0] = getSubsystem().getRobot().getTime() - start_ ;
        data[1] = ((MotorEncoderSubsystem)(getSubsystem())).getPosition() ;
        data[2] = ((MotorEncoderSubsystem)(getSubsystem())).getVelocity() ;
        data[3] = ((MotorEncoderSubsystem)(getSubsystem())).getAcceleration() ;
        data[4] = getSubsystem().getPower() ;
        data[5] = ((MotorEncoderSubsystem)(getSubsystem())).getEncoderRawCount() ;
        getSubsystem().addPlotData(plot_id_, data);
        
        if (isDone())
            getSubsystem().endPlot(plot_id_) ;

        super.run() ;

        // int cnt = getSubsystem().getRobot().getLoopCount() ;
        // System.out.println("Data: " + cnt + " " + data[0] + " " + data[1] + " " + data[5]) ;
    }

    @Override
    public void cancel() {
        super.cancel() ;
        getSubsystem().endPlot(plot_id_) ;        
    }

    private double start_ ;
    private int plot_id_ ;
    private static String[] plot_columns_ = {"time","pos","vel","accel","out","encoder" } ;
}