package org.xero1425.base.motors;

/// \file
/// This file conatins the implementation of the CTREMotorController class.  This class
/// is derived from the MotorController class and supports the CTRE devices including the TalonFX,
/// the TalonSRX, and the VictorSPX.
///

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.hal.SimBoolean;
import edu.wpi.first.hal.SimDevice;
import edu.wpi.first.hal.SimDouble;
import edu.wpi.first.wpilibj.RobotBase;

/// \brief This class is MotorController class that supports the TalonFX, TalonSRX, and the VictorSPX motors.
public class CTREMotorController extends MotorController
{
    public final static String SimDeviceName = "CTREMotorController" ;


    public enum MotorType
    {
        TalonSRX,
        VictorSPX,
        TalonFX
    } ;
    
    public CTREMotorController(String name, int index, MotorType type) {
        super(name) ;

        inverted_ = false ;
        type_ = type ;

        if (RobotBase.isSimulation()) {
            sim_ = SimDevice.create(SimDeviceName, index) ;

            sim_power_ = sim_.createDouble(MotorController.SimPowerParamName, false, 0.0) ;
            sim_encoder_ = sim_.createDouble(MotorController.SimEncoderParamName, false, 0.0) ;
            sim_motor_inverted_ = sim_.createBoolean(MotorController.SimInvertedParamName, false, false) ;
            sim_neutral_mode_ = sim_.createBoolean(MotorController.SimNeutralParamName, false, false) ;
            sim_.createBoolean(MotorController.SimEncoderStoresTicksParamName, true, true) ;
        }
        else {
            sim_ = null ;
            sim_power_ = null ;
            sim_encoder_ = null ;

            switch(type_)
            {
                case TalonSRX:
                    controller_ = new TalonSRX(index) ;
                    break ;

                case TalonFX:
                    controller_ = new TalonFX(index) ;
                    controller_.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 10, 100) ;
                    controller_.setSelectedSensorPosition(0, 0, 0) ;
                    break ;

                case VictorSPX:
                    controller_ = new VictorSPX(index) ;
                    break ;
            }
        }
    }

    public void set(double percent) {
        if (sim_ != null) {
            sim_power_.set(percent) ;
        }
        else {
            controller_.set(ControlMode.PercentOutput, percent) ;
        }
    }

    public void setInverted(boolean inverted) {
        if (sim_ != null) {
            sim_motor_inverted_.set(true) ;
        }
        else {
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
        }
        else {
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
                    controller_.setNeutralMode(com.ctre.phoenix.motorcontrol.NeutralMode.Coast);
                    break ;

                case Brake:
                    controller_.setNeutralMode(com.ctre.phoenix.motorcontrol.NeutralMode.Brake);
                break ;            
            }
        }
    }

    public void follow(MotorController ctrl, boolean invert) throws BadMotorRequestException {
        if (sim_ == null) {
            if (invert)
                throw new BadMotorRequestException(this, "cannot follow another controller inverted") ;

            try {
                CTREMotorController other = (CTREMotorController)ctrl ;
                controller_.follow(other.controller_) ;
            }
            catch(ClassCastException ex)
            {
                throw new BadMotorRequestException(this, "cannot follow a motor that is of another type") ;
            }
        }
    }

    public String getType() {
        String ret = null ;

        switch(type_)
        {
            case TalonSRX:
                ret = "TalonSRX" ;
                break ;

            case TalonFX:
                ret = "TalonFX" ;
                break ;

            case VictorSPX:
                ret = "VictorSPX" ;
                break ;
        }

        return ret ;
    }

    public boolean hasPosition() {
        return type_ == MotorType.TalonFX ;
    }

    public double getPosition() throws BadMotorRequestException {
        int ret = 0 ;

        if (type_ != MotorType.TalonFX)
            throw new BadMotorRequestException(this, "motor does not support getPosition()") ;

        if (sim_ != null) {
            ret = (int)sim_encoder_.getValue().getDouble() ;
        }
        else {
            TalonFX fx = (TalonFX)controller_ ;
            ret = fx.getSelectedSensorPosition() ;
        }
        
        return ret ;
    }

    public void resetEncoder() throws BadMotorRequestException {
        if (type_ != MotorType.TalonFX)
            throw new BadMotorRequestException(this, "motor does not support getPosition()") ;

        if (sim_ != null) {
            sim_encoder_.set(0.0) ;
        }
        else {
            TalonFX fx = (TalonFX)controller_ ;
            fx.setSelectedSensorPosition(0) ;
        }
    }

    public void setCurrentLimit(double limit) throws BadMotorRequestException {
        if (type_ != MotorType.TalonFX)
            throw new BadMotorRequestException(this, "motor does not support getPosition()") ;

        if (sim_ == null) {
            TalonFX fx = (TalonFX)controller_ ;
            SupplyCurrentLimitConfiguration cfg = new SupplyCurrentLimitConfiguration(true, limit, limit, 1) ;
            fx.configSupplyCurrentLimit(cfg) ;
        }
    }      

    private IMotorController controller_ ;
    private boolean inverted_ ;
    private MotorType type_ ;

    private SimDevice sim_ ;
    private SimDouble sim_power_ ;
    private SimDouble sim_encoder_ ;
    private SimBoolean sim_motor_inverted_ ;
    private SimBoolean sim_neutral_mode_ ;
} ;