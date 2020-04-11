package org.frc2020.droid.climber;

import org.xero1425.base.Subsystem;
import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem;

public class LifterSubsystem extends MotorEncoderSubsystem {
    public LifterSubsystem(Subsystem parent, String name) throws Exception {
        super(parent, name, false);

        low_power_limit_ = getRobot().getSettingsParser().get("climber:low_power_limit").getDouble() ;
        low_power_height_ = getRobot().getSettingsParser().get("climber:low_power_height").getDouble() ;     
        calibrated_ = false ;
    }

    public boolean isCalibarated() {
        return calibrated_ ;
    }

    protected void setCalibrated() {
        calibrated_ = true ;
    }

    protected void setPower(double power) {
        ClimberSubsystem climber = (ClimberSubsystem)getParent() ;

        if (power < 0.0) {
            //
            // We are going down
            //
            if (climber.isInFieldMode())
            {
                if (getPosition() < 0)
                {
                    //
                    // We are at the bottom, do not go any further
                    //
                    power = 0 ;
                }
                else if (getPosition() < low_power_height_)
                {
                    if (power < low_power_limit_)
                        power = low_power_limit_ ;
                }
            }
            else
            {
                //
                // We are in the PIT mode here
                //
                if (power < low_power_limit_)
                    power = low_power_limit_ ;
            }
        }
        else
        {
            //
            // We are going up
            //
            if (getPosition() > climber.getMaxHeight())
                power = 0 ;
        }

        super.setPower(power) ;
    }

    private double low_power_height_ ;
    private double low_power_limit_ ;
    private boolean calibrated_ ;
}