package org.xero1425.base.motors ;

import java.lang.Exception ;

public class BadMotorRequestException extends Exception
{
    static final long serialVersionUID = 42 ;

    public BadMotorRequestException(MotorController motor, String msg) {
        super("motor '" + motor.getName() + "' - " + msg) ;

        motor_ = motor ;
    }

    public MotorController getMotor() {
        return motor_ ;
    }

    private MotorController motor_ ;
}
