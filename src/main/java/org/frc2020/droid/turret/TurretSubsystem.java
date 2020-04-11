package org.frc2020.droid.turret;

import org.xero1425.base.Subsystem;
import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem;

public class TurretSubsystem extends MotorEncoderSubsystem {
    public final static String SubsystemName = "turret" ;

    public TurretSubsystem(Subsystem parent) throws Exception {
        super(parent, SubsystemName, false) ;

        min_safe_angle_ = getRobot().getSettingsParser().get("turret:min").getDouble() ;
        max_safe_angle_ = getRobot().getSettingsParser().get("turret:max").getDouble() ;
        is_ready_to_fire_ = false ;
    }

    public double getMinSafeAngle() {
        return min_safe_angle_ ;
    }

    public double getMaxSafeAngle() {
        return max_safe_angle_ ;
    }

    public boolean isReadyToFire() {
        return is_ready_to_fire_ ;
    }

    public void setReadyToFire(boolean b) {
        is_ready_to_fire_ = b ;
    }

    @Override
    public void computeMyState() throws Exception {
        super.computeMyState();
    }

    @Override
    protected void setPower(double p) {
        if (p < 0 && getPosition() < getMinSafeAngle())
            p = 0 ;
        else if (p > 0 && getPosition() > getMaxSafeAngle())
            p = 0 ;

        super.setPower(p) ;
    }

    private double min_safe_angle_ ;
    private double max_safe_angle_ ;
    private boolean is_ready_to_fire_ ;
}

