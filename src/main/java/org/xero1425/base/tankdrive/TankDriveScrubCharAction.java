package org.xero1425.base.tankdrive;

import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;

public class TankDriveScrubCharAction extends TankDriveAction {
    public TankDriveScrubCharAction(final TankDriveSubsystem drive, final double power, final double total) {
        super(drive);
        power_ = power;
        total_ = total;

        plot_id_ = drive.initPlot("tankdrivescrub");
    }

    @Override
    public void start() throws Exception {
        super.start();

        getSubsystem().setPower(-power_, power_);
        start_ = getSubsystem().getRobot().getTime();
        start_angle_ = getSubsystem().getTotalAngle();
        getSubsystem().startPlot(plot_id_, plot_columns_);
    }

    @Override
    public void run() {

        final double angle = getSubsystem().getTotalAngle() - start_angle_;
        final double distl = getSubsystem().getLeftDistance();
        final double distr = getSubsystem().getRightDistance();

        if (angle > total_) {
            final MessageLogger logger = getSubsystem().getRobot().getMessageLogger();
            setDone();
            getSubsystem().setPower(0.0, 0.0);

            final double avgc = (distl - distr) / 2.0;
            final double revs = angle / 360.0;
            final double effr = avgc / (Math.PI * revs);

            logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID());
            logger.add("Total Angle (NaVX) ").add(angle);
            logger.add(", left ").add(distl);
            logger.add(", right ").add(distr);
            logger.add(", effective Width ").add(effr * 2.0);
            logger.endMessage();
        } else {
            final Double[] data = new Double[7];
            data[0] = getSubsystem().getRobot().getTime() - start_;
            data[1] = getSubsystem().getAngle();
            data[4] = (double) getSubsystem().getLeftTick();
            data[5] = (double) getSubsystem().getRightTick();
            data[6] = power_;
            getSubsystem().addPlotData(plot_id_, data);
        }
    }

    @Override
    public void cancel() {
        super.cancel();

        getSubsystem().setPower(0.0, 0.0);
        getSubsystem().endPlot(plot_id_);
    }

    public String toString(int indent) {
        String ret = prefix(indent) + "TankDriveScrubCharAction";
        ret += " power=" + Double.toString(power_);
        ret += " angle=" + Double.toString(total_);

        return ret;
    }

    private final double power_;
    private double start_;
    private double start_angle_;
    private final double total_;
    private final int plot_id_;
    private static String [] plot_columns_ = { "time", "angle", "lticks", "rticks", "power" } ;
} ;