package org.xero1425.base.motors;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;

import edu.wpi.first.hal.SimBoolean;
import edu.wpi.first.hal.SimDevice;
import edu.wpi.first.hal.SimDouble;
import edu.wpi.first.wpilibj.RobotBase;

public class SparkMaxMotorController extends MotorController
{
    public final static String SimDeviceNameBrushed = "SparkMaxBrushed" ;
    public final static String SimDeviceNameBrushless = "SparkMaxBrushless" ;
    public final static int TicksPerRevolution = 42 ;

    public SparkMaxMotorController(String name, int index, boolean brushless) {
        super(name) ;

        inverted_ = false ;
        brushless_ = brushless ;

        if (RobotBase.isSimulation()) {
            if (brushless)
                sim_ = SimDevice.create(SimDeviceNameBrushless, index) ;
            else
                sim_ = SimDevice.create(SimDeviceNameBrushed, index) ;

            sim_power_ = sim_.createDouble(MotorController.SimPowerParamName, false, 0.0) ;
            sim_encoder_ = sim_.createDouble(MotorController.SimEncoderParamName, false, 0.0) ;
            sim_motor_inverted_ = sim_.createBoolean(MotorController.SimInvertedParamName, false, false) ;
            sim_neutral_mode_ = sim_.createBoolean(MotorController.SimNeutralParamName, false, false) ;  
            sim_.createBoolean(MotorController.SimEncoderStoresTicksParamName, true, false) ;            
        }
        else {
            if (brushless)
                controller_ = new CANSparkMax(index, CANSparkMax.MotorType.kBrushless) ;
            else
                controller_ = new CANSparkMax(index, CANSparkMax.MotorType.kBrushed) ;

            controller_.restoreFactoryDefaults() ;
            encoder_ = controller_.getEncoder() ;
        }
    }

    public void set(double percent) {
        if (sim_ != null) {
            sim_power_.set(percent) ;
        } else {
            controller_.set(percent) ;
        }
    }

    public void setInverted(boolean inverted) {
        if (sim_ != null) {
            sim_motor_inverted_.set(inverted) ;
        } else {
            controller_.setInverted(inverted);
        }

        inverted_ = inverted ;
    }

    public boolean isInverted() {
        return inverted_ ;
    }    

    public void reapplyInverted() {
        if (sim_ != null) {
            sim_motor_inverted_.set(inverted_) ;
        } else {
            controller_.setInverted(inverted_);
        }
    }

    public void setNeutralMode(NeutralMode mode) throws BadMotorRequestException {
        if (sim_ != null) {
            switch(mode)
            {
                case Coast:
                    sim_neutral_mode_.set(false) ;
                    break ;

                case Brake:
                    sim_neutral_mode_.set(true) ;
                    break ;
            }
        }
        else {
            switch(mode)
            {
                case Coast:
                    controller_.setIdleMode(IdleMode.kCoast) ;
                    break ;

                case Brake:
                    controller_.setIdleMode(IdleMode.kBrake) ;
                break ;
            }
        }
    }

    public void follow(MotorController ctrl, boolean invert) throws BadMotorRequestException {
        if (sim_ == null) {
            try {
                SparkMaxMotorController other = (SparkMaxMotorController)ctrl ;
                controller_.follow(other.controller_, invert) ;
            }
            catch(ClassCastException ex)
            {
                throw new BadMotorRequestException(this, "cannot follow a motor that is of another type") ;
            }
        }
    }

    public String getType() {
        String ret = null ;

        if (brushless_)
        {
            ret = "SparkMax:brushless" ;
        }
        else
        {
            ret = "SparkMax:brushed" ;
        }

        return ret ;
    }

    public boolean hasPosition() {
        return brushless_ ;
    }

    public double getPosition() throws BadMotorRequestException {
        double ret = 0 ;

        if (!brushless_)
            throw new BadMotorRequestException(this, "brushed motor does not support getPosition()") ;

        if (sim_ != null) {
            ret = sim_encoder_.get() * (double)TicksPerRevolution ;
        } else {
            ret = encoder_.getPosition() * TicksPerRevolution ;
        }

        return ret ;
    }

    public void resetEncoder() throws BadMotorRequestException {
        if (!brushless_)
            throw new BadMotorRequestException(this, "brushed motor does not support getPosition()") ;

        if (sim_ != null) {
            sim_encoder_.set(0.0) ;
        }
        else {
            encoder_.setPosition(0.0) ;
        }
    }

    public void setCurrentLimit(double limit) throws BadMotorRequestException {
        if (sim_ == null) {
            controller_.setSmartCurrentLimit((int)limit) ;
        }
    }      

    private CANSparkMax controller_ ;
    private CANEncoder encoder_ ;
    private boolean inverted_ ;
    private boolean brushless_ ;

    private SimDevice sim_ ;
    private SimDouble sim_power_ ;
    private SimDouble sim_encoder_ ;
    private SimBoolean sim_motor_inverted_ ;
    private SimBoolean sim_neutral_mode_ ;
} ;