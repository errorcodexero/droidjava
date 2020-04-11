package org.xero1425.base.oi;

import edu.wpi.first.wpilibj.DriverStation;
import org.xero1425.base.LoopType;
import org.xero1425.base.actions.Action;
import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.actions.SequenceAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.base.tankdrive.TankDrivePowerAction;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.SettingsParser;

public class TankDriveGamepad extends Gamepad {
    public TankDriveGamepad(OISubsystem oi, int index, TankDriveSubsystem drive_) throws Exception {
        super(oi, index);

        DriverStation ds = DriverStation.getInstance();
        if (ds.getStickPOVCount(getIndex()) == 0) {
            MessageLogger logger = oi.getRobot().getMessageLogger();
            logger.startMessage(MessageType.Error);
            logger.add("driver gamepad does not have POV control - nudges are disabled");
            logger.endMessage();
            pov_ = -1;
        } else {
            pov_ = 0;
        }

        if (ds.getStickAxisCount(getIndex()) <= AxisNumber.RIGHTX.value) {
            MessageLogger logger = oi.getRobot().getMessageLogger();
            logger.startMessage(MessageType.Error);
            logger.add("driver gamepad does not have required number of axis, must have six");
            logger.endMessage();
            throw new Exception("invalid gamepad for TankDriveGamepad");
        }

        db_ = drive_;
    }

    @Override
    public void init(LoopType ltype) {
    }

    @Override
    public void createStaticActions() throws BadParameterTypeException, MissingParameterException {
        SettingsParser settings = getSubsystem().getRobot().getSettingsParser();

        default_power_ = settings.get("driver:power:default").getDouble();
        max_power_ = settings.get("driver:power:max").getDouble();
        turn_power_ = settings.get("driver:turn:default").getDouble();
        turn_max_power_ = settings.get("driver:turn:max").getDouble();
        slow_factor_ = settings.get("driver:power:slowby").getDouble();
        zero_level_ = settings.get("driver:zerolevel").getDouble();

        tolerance_ = settings.get("driver:power:tolerance").getDouble();

        double nudge_straight = settings.get("driver:power:nudge_straight").getDouble();
        double nudge_rotate = settings.get("driver:power:nudge_rotate").getDouble();
        double nudge_time = settings.get("driver:nudge_time").getDouble();

        nudge_forward_ = new TankDrivePowerAction(db_, nudge_straight, nudge_straight, nudge_time);
        nudge_backward_ = new TankDrivePowerAction(db_, -nudge_straight, -nudge_straight, nudge_time);
        nudge_clockwise_ = new TankDrivePowerAction(db_, -nudge_rotate, nudge_rotate, nudge_time);
        nudge_counter_clockwise_ = new TankDrivePowerAction(db_, nudge_rotate, -nudge_rotate, nudge_time);
    }

    @Override
    public void computeState() {
        super.computeState();
    }

    @Override
    public void generateActions(SequenceAction seq) {
        if (db_ == null || !isEnabled())
            return ;

        try {

            POVAngle povvalue ;

            DriverStation ds = DriverStation.getInstance() ;
            if (pov_ == -1)
                povvalue = POVAngle.NONE ;
            else
                povvalue = POVAngle.fromInt(ds.getStickPOV(getIndex(), pov_)) ;

            double ly = ds.getStickAxis(getIndex(), AxisNumber.LEFTY.value) ;
            double rx = ds.getStickAxis(getIndex(), AxisNumber.RIGHTX.value) ;

            if (povvalue == POVAngle.LEFT)
                seq.addSubActionPair(db_, nudge_clockwise_, false);
            else if (povvalue == POVAngle.RIGHT)
                seq.addSubActionPair(db_, nudge_counter_clockwise_, false);       
            else if (povvalue == POVAngle.UP)
                seq.addSubActionPair(db_, nudge_forward_, false);  
            else if (povvalue == POVAngle.DOWN)
                seq.addSubActionPair(db_, nudge_backward_, false);                                       
            else {
                double left, right ;

                if (Math.abs(ly) < zero_level_ && Math.abs(rx) < zero_level_) {
                    left = 0.0 ;
                    right = 0.0; 
                }
                else {
                    double boost = ds.getStickAxis(getIndex(), AxisNumber.LTRIGGER.value) ;
                    boolean slow = isLJoyButtonPressed() ;

                    double power = scalePower(-ly, boost, slow) ;
                    double spin = (Math.abs(rx) > 0.01) ? scaleTurn(rx, boost, slow) : 0.0 ;

                    left = power + spin ;
                    right = power - spin ;
                }

                if (Math.abs(left - left_) > tolerance_ || Math.abs(right - right_) > tolerance_)
                {
                    TankDrivePowerAction act = new TankDrivePowerAction(db_, left, right) ;
                    seq.addSubActionPair(db_, act, false);
                    left_ = left ;
                    right_ = right ;
                }
            }
        }
        catch(InvalidActionRequest ex) {
            //
            // This should never happen
            //
        }
    }

    private double scalePower(double axis, double boost, boolean slow) {
        double base = default_power_ + (max_power_ - default_power_) * boost ;
        double slowdown = slow ? default_power_ * slow_factor_ : 0.0 ;
        return axis * (base - slowdown) ;
    }

    private double scaleTurn(double axis, double boost, boolean slow) {
        double base = turn_power_ + (turn_max_power_ - turn_power_) * boost ;
        double slowdown = slow ? turn_power_ * slow_factor_ : 0.0 ;
        return axis * (base - slowdown) ;
    }

    private TankDriveSubsystem db_ ;
    private Action nudge_forward_ ;
    private Action nudge_backward_ ;
    private Action nudge_clockwise_ ;
    private Action nudge_counter_clockwise_ ;
    private int pov_ ;

    private double default_power_ ;
    private double max_power_ ;
    private double turn_power_ ;
    private double turn_max_power_ ;
    private double slow_factor_ ;
    private double zero_level_ ;
    private double tolerance_ ;

    double left_ ;
    double right_ ;
}