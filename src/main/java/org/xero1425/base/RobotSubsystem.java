package org.xero1425.base;

import org.xero1425.base.alarms.AlarmSubsystem;
import org.xero1425.base.oi.OISubsystem;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

public class RobotSubsystem extends Subsystem
{
    public RobotSubsystem(XeroRobot robot, String name) throws Exception {
        super(robot, name) ;

        oi_ = null ;
        db_ = null ;
        alarms_ = new AlarmSubsystem(this) ;
        addChild(alarms_) ;
    }

    public void addChild(final Subsystem child) throws Exception {
        super.addChild(child) ;

        if (child.getClass().isInstance(OISubsystem.class)) {
            if (oi_ != null)
                throw new Exception("multiple OI subsystems added to robot subsystem") ;

            oi_ = (OISubsystem)child ;
        }
        else if (child.getClass().isInstance(TankDriveSubsystem.class)) {
            if (db_ != null)
                throw new Exception("multiple drivebase subsystems added to robot subsystem") ;

            db_ = (TankDriveSubsystem)child ;
        }
    }

    public OISubsystem getOI() {
        return oi_ ;
    }

    public TankDriveSubsystem getDB() {
        return db_ ;
    }

    public AlarmSubsystem getAlarms() {
        return alarms_ ;
    }

    private OISubsystem oi_ ;
    private TankDriveSubsystem db_ ;
    private AlarmSubsystem alarms_;
} ;