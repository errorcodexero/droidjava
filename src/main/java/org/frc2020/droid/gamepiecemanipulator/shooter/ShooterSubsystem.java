package org.frc2020.droid.gamepiecemanipulator.shooter;

import edu.wpi.first.wpilibj.Servo;
import org.xero1425.base.Subsystem;
import org.xero1425.base.motors.MotorController;
import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

public class ShooterSubsystem extends MotorEncoderSubsystem {
    public final static String SubsystemName = "shooter" ;

    public enum HoodPosition {
        Up,
        Down,
        Unknown
    } ;

    public ShooterSubsystem(Subsystem parent, TankDriveSubsystem db) throws Exception {
        super(parent, SubsystemName, false) ;

        setSmartDashboardWhenEnabled(true);

        db_ = db ;

        int index = getRobot().getSettingsParser().get("hw:shooter:hood").getInteger() ;
        hood_servo_ = new Servo(index) ;

        hood_up_pos_ = getRobot().getSettingsParser().get("shooter:hood:up").getDouble() ;
        hood_down_pos_ = getRobot().getSettingsParser().get("shooter:hood:down").getDouble() ;
        hood_change_time_= getRobot().getSettingsParser().get("shooter:hood:change_time").getDouble() ;
        hood_down_speed_= getRobot().getSettingsParser().get("shooter:hood:down_speed").getDouble() ;

        getMotorController().setCurrentLimit(40);
        getMotorController().setNeutralMode(MotorController.NeutralMode.Coast);

        change_time_ = getRobot().getTime() ;
        actual_ = HoodPosition.Unknown ;
        desired_ = HoodPosition.Down ;
    }

    public void setHood(HoodPosition pos) {
        desired_ = pos ;
    }

    public HoodPosition getHood() {
        return actual_ ;
    }

    public boolean isHoodReady() {
        if (getRobot().getTime() - change_time_ > hood_change_time_)
            return true ;

        return false ;
    }

    public boolean isReadyToFire() {
        return ready_to_fire_ ;
    }

    public void setReadyToFire(boolean b) {
        ready_to_fire_ = b ;
    }

    @Override
    public void computeMyState() throws Exception {
        super.computeMyState();

        double rpm = getVelocity() * 60.0 ;
        putDashboard("shooter:rpm", DisplayType.Verbose, rpm);
    }

    @Override
    public void run() throws Exception {
        super.run() ;
        updateHood() ;
    }

    private void setPhysicalHood(HoodPosition pos)
    {
        if (pos == HoodPosition.Down)
        {
            hood_servo_.set(hood_down_pos_) ;
            actual_ = pos  ;
        }
        else if (pos == HoodPosition.Up)
        {
            hood_servo_.set(hood_up_pos_) ;  
            actual_ = pos  ;                      
        }
    }

    private void updateHood() {
        if (db_.getVelocity() > hood_down_speed_)
            setPhysicalHood(HoodPosition.Down) ;
        else if (actual_ != desired_) {
            setPhysicalHood(desired_);
        }
    }

    private HoodPosition desired_ ;
    private HoodPosition actual_ ;
    private double change_time_ ;
    private boolean ready_to_fire_ ;
    private double hood_change_time_ ;
    private double hood_down_speed_ ;
    private double hood_up_pos_ ;
    private double hood_down_pos_ ;
    private Servo hood_servo_ ;
    private TankDriveSubsystem db_ ;
} ;