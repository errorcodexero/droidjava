package org.xero1425.base.motorsubsystem ;

import org.xero1425.base.actions.Action;

public abstract class MotorAction extends Action
{
    public MotorAction(final MotorSubsystem drive) {
        super(drive.getRobot().getMessageLogger());
        motor_subsystem_ = drive;
    }

    public MotorSubsystem getSubsystem() {
        return motor_subsystem_;
    }

    private final MotorSubsystem motor_subsystem_;
}