package org.frc2020.droid.climber;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PWMSparkMax;
import org.xero1425.base.LoopType;
import org.xero1425.base.Subsystem;
import org.xero1425.base.motors.MotorController;
import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem;

public class ClimberSubsystem extends Subsystem {
    public static final String SubsystemName = "climber" ;

    public ClimberSubsystem(Subsystem parent) throws Exception {
        super(parent, SubsystemName);

        max_height_ = getRobot().getSettingsParser().get("climber:max_height").getDouble();
        lifter_ = new LifterSubsystem(this, "climber:lifter");
        lifter_.getMotorController().setCurrentLimit(40);
        lifter_.getMotorController().setNeutralMode(MotorController.NeutralMode.Brake);
        lifter_.getMotorController().resetEncoder();
        addChild(lifter_);

        int travid = getRobot().getSettingsParser().get("hw:climber:traverser:pwmid").getInteger();
        traverser_ = new PWMSparkMax(travid);
    }

    public void setTraverserPower(double p) {
        traverser_.set(p);
    }

    public double getMaxHeight() {
        return max_height_;
    }

    public boolean isInFieldMode() {
        return field_mode_ ;
    }

    public MotorEncoderSubsystem getLifter() {
        return lifter_ ;
    }

    @Override
    public void init(LoopType ltype) {
        super.init(ltype) ;

        boolean b ;
        try {
            b = getRobot().getSettingsParser().get("climber:force_field_mode").getBoolean() ;
        }
        catch(Exception ex) {
            b = true ;
        }
        
        field_mode_ = DriverStation.getInstance().isFMSAttached() || b ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;
    }

    private MotorEncoderSubsystem lifter_ ;
    private PWMSparkMax traverser_ ;
    private double max_height_ ;
    private boolean field_mode_ ;
}