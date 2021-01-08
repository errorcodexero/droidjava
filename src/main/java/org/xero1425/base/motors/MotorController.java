package org.xero1425.base.motors ;


public abstract class MotorController
{
    public final static String SimPowerParamName = "Power" ;
    public final static String SimEncoderParamName = "Encoder" ;
    public final static String SimEncoderStoresTicksParamName = "StoresTicks" ;
    public final static String SimInvertedParamName = "Inverted" ;
    public final static String SimNeutralParamName = "Neutral" ;

    public enum NeutralMode { 
        Coast,
        Brake
    } ;

    MotorController(String name) {
        name_ = name ;
    }

    public String getName() {
        return name_  ;
    }

    public abstract void set(double percent)  throws BadMotorRequestException;
    public abstract void setInverted(boolean inverted)  throws BadMotorRequestException;
    public abstract boolean isInverted() throws BadMotorRequestException ;    
    public abstract void reapplyInverted() throws BadMotorRequestException;
    public abstract void setNeutralMode(NeutralMode mode) throws BadMotorRequestException;
    public abstract void follow(MotorController ctrl, boolean invert) throws BadMotorRequestException;
    public abstract String getType()  throws BadMotorRequestException;
    
    public boolean hasPosition() throws BadMotorRequestException {
        return false ;
    }

    public double getPosition() throws BadMotorRequestException {
        throw new BadMotorRequestException(this, "motor does not support getPosition()") ;
    }

    public void resetEncoder() throws BadMotorRequestException {
        throw new BadMotorRequestException(this, "motor does not support resetEncoder()") ;        
    }

    public void setCurrentLimit(double limit) throws BadMotorRequestException {
        throw new BadMotorRequestException(this, "motor does not support setCurrentLimit()") ;        
    }

    private String name_ ;
}