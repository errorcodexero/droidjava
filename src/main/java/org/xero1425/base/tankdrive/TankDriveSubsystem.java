package org.xero1425.base.tankdrive;

import java.util.Map;
import java.util.HashMap;
import edu.wpi.first.wpilibj.SPI;
import com.kauailabs.navx.frc.AHRS;
import org.xero1425.base.LoopType;
import org.xero1425.base.Subsystem;
import org.xero1425.base.motors.BadMotorRequestException;
import org.xero1425.base.motors.MotorController;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.Speedometer;

public class TankDriveSubsystem extends Subsystem {

    public TankDriveSubsystem(Subsystem parent, String name, String config)
            throws BadParameterTypeException, MissingParameterException {
        super(parent, name);

        MessageLogger logger = getRobot().getMessageLogger();

        dist_l_ = 0.0;
        dist_r_ = 0.0;

        left_inches_per_tick_ = getRobot().getSettingsParser().get("tankdrive:inches_per_tick").getDouble();
        right_inches_per_tick_ = left_inches_per_tick_;

        angular_ = new Speedometer("angles", 2, true);
        left_linear_ = new Speedometer("left", 2, false);
        right_linear_ = new Speedometer("right", 2, false);

        automode_neutral_ = MotorController.NeutralMode.Brake;
        teleop_neutral_ = MotorController.NeutralMode.Brake;
        disabled_neutral_ = MotorController.NeutralMode.Coast;

        navx_ = new AHRS(SPI.Port.kMXP) ;
        double start = getRobot().getTime() ;
        while (getRobot().getTime() - start < 3.0) {
            if (navx_.isConnected())
                break ;
        }

        if (!navx_.isConnected()) {
            logger.startMessage(MessageType.Error);
            logger.add("NavX is not connected - cannot perform tankdrive path following functions");
            logger.endMessage();
            navx_ = null;
        }

        trips_ = new HashMap<String, Double>();

        attachHardware();
    }

    public void startTrip(String name) {
        trips_.put(name, getDistance());
    }

    public double getTripDistance(String name) {
        if (!trips_.containsKey(name))
            return 0.0;

        return trips_.get(name);
    }

    public double getLeftDistance() {
        return dist_l_;
    }

    public double getRightDistance() {
        return dist_r_;
    }

    public double getDistance() {
        return (getLeftDistance() + getRightDistance()) / 2.0;
    }

    public double getLeftVelocity() {
        return left_linear_.getVelocity() ;
    }

    public double getRightVelocity() {
        return right_linear_.getVelocity() ;
    }

    public double getVelocity() {
        return (left_linear_.getVelocity() + right_linear_.getVelocity()) / 2.0 ;
    }

    public double getLeftAcceleration() {
        return left_linear_.getAcceleration() ;
    }

    public double getRightAcceleration() {
        return right_linear_.getAcceleration() ;
    }    

    public double getAcceleration() {
        return (left_linear_.getAcceleration() + right_linear_.getAcceleration()) / 2.0 ;
    }

    public int getLeftTick() {
        return ticks_left_ ;
    }

    public int getRightTick() {
        return ticks_right_ ;
    }

    public double getAngle() {
        return angular_.getDistance() ;
    }

    public double getTotalAngle() {
        return total_angle_ ;
    }

    public void reset() {
        super.reset();

        try {
            left_motors_.setNeutralMode(disabled_neutral_);
            right_motors_.setNeutralMode(disabled_neutral_);
        } catch (Exception ex) {
        }
    }

    public void init(LoopType ltype) {
        super.init(ltype);

        try {
            switch (ltype) {
            case Autonomous:
                left_motors_.setNeutralMode(automode_neutral_);
                right_motors_.setNeutralMode(automode_neutral_);
                break;

            case Teleop:
                left_motors_.setNeutralMode(teleop_neutral_);
                right_motors_.setNeutralMode(teleop_neutral_);
                break;

            case Test:
                left_motors_.setNeutralMode(disabled_neutral_);
                right_motors_.setNeutralMode(disabled_neutral_);
                break;

            case Disabled:
                left_motors_.setNeutralMode(disabled_neutral_);
                right_motors_.setNeutralMode(disabled_neutral_);            
                break ;
            }
        } catch (Exception ex) {
        }
    }

    public void run() throws Exception {
        super.run();
    }

    public void computeMyState() {
        double angle = 0.0;

        try {
            if (left_motors_.hasPosition() && right_motors_.hasPosition()) {
                ticks_left_ = (int)left_motors_.getPosition();
                ticks_right_ = (int)right_motors_.getPosition();
            }
            else {
                //
                // TODO: Support external encoders
                //
            }

            dist_l_ = ticks_left_ * left_inches_per_tick_;
            dist_r_ = ticks_right_ * right_inches_per_tick_;
            if (navx_ != null) {
                angle = -navx_.getYaw();
                angular_.update(getRobot().getDeltaTime(), angle);
            }

            left_linear_.update(getRobot().getDeltaTime(), getLeftDistance());
            right_linear_.update(getRobot().getDeltaTime(), getRightDistance());


            total_angle_ = navx_.getAngle() ;

        } catch (Exception ex) {
            //
            // This should never happen
            //
        }

        putDashboard("dbleft", DisplayType.Verbose, left_linear_.getDistance());
        putDashboard("dbright", DisplayType.Verbose, right_linear_.getDistance());
        putDashboard("dbangle", DisplayType.Verbose, angular_.getDistance());        

        MessageLogger logger = getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getLoggerID()) ;
        logger.add("tankdrive:") ;
        logger.add(" powerl", left_power_).add(" powerr", right_power_) ;
        logger.add(" ticksl", ticks_left_).add(" ticksr ", ticks_right_) ;
        logger.add(" distl", dist_l_).add(" distr", dist_r_) ;
        logger.add(" velocityl", getLeftVelocity()).add(" velocityr", getRightVelocity()) ;
        logger.add(" speed", getVelocity()).add(" angle", getAngle()) ;
        logger.endMessage();
    }

    protected void setPower(double left, double right) {
        left_power_ = left ;
        right_power_ = right ;

        try {
            left_motors_.set(left_power_) ;
            right_motors_.set(right_power_) ;
        }
        catch(BadMotorRequestException ex) {
            MessageLogger logger = getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("subsystem ").addQuoted(getName()).add(": cannot set power -").add(ex.getMessage()).endMessage();
        }
    }

    private void attachHardware() {
        left_motors_ = getRobot().getMotorFactory().createMotor("tankdrive:motors:left", "hw:tankdrive:motors:left") ;
        right_motors_ = getRobot().getMotorFactory().createMotor("tankdrive:motors:right", "hw:tankdrive:motors:right") ;
    }

    private double left_power_ ;
    private double right_power_ ;
    private int ticks_left_ ;
    private int ticks_right_ ;
    private double dist_l_ ;
    private double dist_r_ ;
    private double left_inches_per_tick_ ;
    private double right_inches_per_tick_ ;
    private double total_angle_ ;
    private AHRS navx_ ;
    private MotorController.NeutralMode automode_neutral_ ;
    private MotorController.NeutralMode teleop_neutral_ ;
    private MotorController.NeutralMode disabled_neutral_ ;

    private Speedometer angular_ ;
    private Speedometer left_linear_ ;
    private Speedometer right_linear_ ;

    private MotorController left_motors_ ;
    private MotorController right_motors_ ;

    private Map<String, Double> trips_ ;
} ;