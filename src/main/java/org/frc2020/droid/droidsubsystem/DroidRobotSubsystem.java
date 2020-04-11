package org.frc2020.droid.droidsubsystem;

import org.frc2020.droid.climber.ClimberSubsystem;
import org.frc2020.droid.droidlimelight.DroidLimeLightSubsystem;
import org.frc2020.droid.droidoi.DroidOISubsystem;
import org.frc2020.droid.gamepiecemanipulator.GamePieceManipulatorSubsystem;
import org.frc2020.droid.targettracker.TargetTrackerSubsystem;
import org.frc2020.droid.turret.TurretSubsystem;
import org.xero1425.base.RobotSubsystem;
import org.xero1425.base.XeroRobot;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

public class DroidRobotSubsystem extends RobotSubsystem {
    public final static String SubsystemName = "droid" ;
    public final static String TankdriveSubsystemName = "tankdrive" ;

    public DroidRobotSubsystem(XeroRobot robot) throws Exception {
        super(robot, SubsystemName) ;

        db_ = new TankDriveSubsystem(this, TankdriveSubsystemName, "tankdrive") ;
        addChild(db_) ;

        climber_ = new ClimberSubsystem(this) ;
        addChild(climber_) ;

        manip_ = new GamePieceManipulatorSubsystem(this, db_) ;
        addChild(manip_) ;

        turret_ = new TurretSubsystem(this) ;
        addChild(turret_) ;

        limelight_ = new DroidLimeLightSubsystem(this) ;
        addChild(limelight_) ;

        tracker_ = new TargetTrackerSubsystem(this, db_, limelight_, turret_) ;
        addChild(tracker_) ;

        oi_ = new DroidOISubsystem(this, db_) ;
        addChild(oi_) ;        
    }

    public TankDriveSubsystem getTankDrive() {
        return db_ ;
    }

    public DroidOISubsystem getOI() {
        return oi_ ;
    }

    public ClimberSubsystem getClimber() {
        return climber_ ;
    }

    public GamePieceManipulatorSubsystem getGamePieceManipulator() {
        return manip_ ;
    }

    public TurretSubsystem getTurret() {
        return turret_ ;
    }

    public DroidLimeLightSubsystem getLimeLight() {
        return limelight_ ;
    }

    public TargetTrackerSubsystem getTracker() {
        return tracker_ ;
    }

    private TankDriveSubsystem db_ ;
    private DroidOISubsystem oi_ ;
    private ClimberSubsystem climber_ ;
    private GamePieceManipulatorSubsystem manip_ ;
    private TurretSubsystem turret_ ;
    private DroidLimeLightSubsystem limelight_ ;
    private TargetTrackerSubsystem tracker_ ;
}