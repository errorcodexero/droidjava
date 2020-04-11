package org.xero1425.base.tankdrive ;

import org.xero1425.base.actions.Action;

public abstract class TankDriveAction extends Action
{
    public TankDriveAction(TankDriveSubsystem drive) {
        super(drive.getRobot().getMessageLogger()) ;
        tankdrive_ = drive ;
    }

    public TankDriveSubsystem getSubsystem() {
        return tankdrive_ ;
    }

    private TankDriveSubsystem tankdrive_ ;
}