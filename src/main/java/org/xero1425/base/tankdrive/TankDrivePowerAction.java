package org.xero1425.base.tankdrive ;

import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class TankDrivePowerAction extends TankDriveAction {
    public TankDrivePowerAction(TankDriveSubsystem drive, double left, double right) {
        super(drive);

        left_ = left ;
        right_ = right ;
        timed_ = false;
    }

    public TankDrivePowerAction(TankDriveSubsystem drive, String left, String right)
            throws BadParameterTypeException, MissingParameterException {
        super(drive);

        left_ = drive.getRobot().getSettingsParser().get(left).getDouble();
        right_ = drive.getRobot().getSettingsParser().get(right).getDouble() ;
        timed_ = false;
    }

    public TankDrivePowerAction(TankDriveSubsystem drive, double left, double right, double duration) {
        super(drive);

        left_ = left ;
        right_ = right ;
        duration_ = duration;
        timed_ = true;
        plot_id_ = drive.initPlot("tankdrivepower");
    }

    public TankDrivePowerAction(TankDriveSubsystem drive, String left, String right, String duration)
            throws BadParameterTypeException, MissingParameterException {

        super(drive);
        left_ = drive.getRobot().getSettingsParser().get(left).getDouble();
        right_ = drive.getRobot().getSettingsParser().get(right).getDouble();
        duration_ = drive.getRobot().getSettingsParser().get(duration).getDouble();
        timed_ = true;
        plot_id_ = drive.initPlot("tankdrivepower");        
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        try {
            getSubsystem().setPower(left_, right_) ;
            if (timed_)
                start_ = getSubsystem().getRobot().getTime() ;
            else
                setDone() ;
        }
        catch(Exception ex) {
        }

        if (timed_)
            getSubsystem().startPlot(plot_id_, plot_columns_) ;
    }

    @Override
    public void run() {
        if (timed_) {
            if (getSubsystem().getRobot().getTime() - start_ > duration_)
            {
                try 
                {
                    getSubsystem().setPower(0.0, 0.0) ;
                }
                catch(Exception ex)
                {
                }
                setDone() ;
                getSubsystem().endPlot(plot_id_) ;
            }

            Double[] data = new Double[plot_columns_.length] ;
            data[0] = getSubsystem().getRobot().getTime() - start_ ;
            data[1] = getSubsystem().getDistance() ;
            data[2] = getSubsystem().getVelocity() ;
            data[3] = getSubsystem().getAcceleration() ;
            data[4] = (double)getSubsystem().getLeftTick() ;
            data[5] = (double)getSubsystem().getRightTick() ;
            data[6] = left_ ;
            data[7] = right_ ;
            getSubsystem().addPlotData(plot_id_, data);
        }
    }

    @Override
    public void cancel() {
        super.cancel() ;

        try {
            getSubsystem().setPower(0.0, 0.0) ;
        }
        catch(Exception ex)
        {
        }        
    }

    public String toString(int indent) {
        String ret = prefix(indent) + "TankDrivePowerAction" ;
        ret += " left=" + Double.toString(left_) ;
        ret += " right=" + Double.toString(right_) ;
        if (timed_)
            ret += " duration=" + Double.toString(duration_) ;

        return ret ;
    }

    private double left_ ;
    private double right_ ;
    private double start_ ;
    private double duration_ ;
    private boolean timed_ ;
    private int plot_id_ ;
    private static String [] plot_columns_ = { "time", "dist", "velocity", "acceleration", "lticks", "rticks", "left", "right" } ;
} ;