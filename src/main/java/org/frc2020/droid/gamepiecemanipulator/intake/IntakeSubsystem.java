package org.frc2020.droid.gamepiecemanipulator.intake;

import org.xero1425.base.Subsystem;
import org.xero1425.base.motors.BadMotorRequestException;
import org.xero1425.base.motors.MotorController;
import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem;

public class IntakeSubsystem extends MotorEncoderSubsystem {
    public static final String SubsystemName = "intake" ;

    public IntakeSubsystem(Subsystem parent) throws Exception {
        super(parent, SubsystemName, false);

        collector_ = getRobot().getMotorFactory().createMotor("intake-collector", "hw:intake:collect:motor");
    }

    protected void setCollectorPower(double p) throws BadMotorRequestException {
        collector_.set(p) ;
    }

    private MotorController collector_ ;
}