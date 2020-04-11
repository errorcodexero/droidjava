package org.frc2020.droid.gamepiecemanipulator;

import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorSubsystem;
import org.frc2020.droid.gamepiecemanipulator.intake.IntakeSubsystem;
import org.frc2020.droid.gamepiecemanipulator.shooter.ShooterSubsystem;
import org.xero1425.base.Subsystem;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

public class GamePieceManipulatorSubsystem extends Subsystem {
    public static final String SubsystemName = "gamepiecemanipulator" ;
    public GamePieceManipulatorSubsystem(Subsystem parent, TankDriveSubsystem db) throws Exception {
        super(parent, SubsystemName) ;

        conveyor_ = new ConveyorSubsystem(this) ;
        addChild(conveyor_) ;

        shooter_ = new ShooterSubsystem(this, db) ;
        addChild(shooter_) ;

        intake_ = new IntakeSubsystem(this) ;
        addChild(intake_) ;
    }

    public ConveyorSubsystem getConveyor() {
        return conveyor_ ;
    }

    public ShooterSubsystem getShooter() {
        return shooter_ ;
    }

    public IntakeSubsystem getIntake() {
        return intake_ ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;
    }

    private ConveyorSubsystem conveyor_ ;
    private ShooterSubsystem shooter_ ;
    private IntakeSubsystem intake_ ;
}