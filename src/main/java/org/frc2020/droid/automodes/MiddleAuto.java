package org.frc2020.droid.automodes;

import org.xero1425.base.tankdrive.TankDriveFollowPathAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

public class MiddleAuto extends DroidAutoMode {
    public MiddleAuto(DroidAutoController ctrl) throws Exception {
        super(ctrl, "MiddleAuto") ;

        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ;        

        setInitialBallCount(3);

        //
        // Fire the three balls (note path = null means not driving)
        //
        driveAndFire(null, true, 0.0) ;

        //
        // Move forward a short distance to get off the initiation line
        //
        addSubActionPair(db, new TankDriveFollowPathAction(db, "three_ball_auto_fire", true), true);
    }
}