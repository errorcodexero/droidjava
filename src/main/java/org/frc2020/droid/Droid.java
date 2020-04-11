/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.frc2020.droid;

import edu.wpi.first.wpilibj.RobotBase;
import org.frc2020.droid.automodes.DroidAutoController;
import org.frc2020.droid.droidsubsystem.DroidRobotSubsystem;
import org.xero1425.simulator.engine.ModelFactory;
import org.xero1425.simulator.engine.SimulationEngine;
import org.xero1425.base.XeroRobot;
import org.xero1425.base.actions.Action;
import org.xero1425.base.alarms.AlarmSubsystem;
import org.xero1425.base.controllers.AutoController;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MissingParameterException;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Droid extends XeroRobot {
    static private byte[] practice_bot_mac_addr = new byte[] { 0x00, -128, 0x2F, 0x17, -119, -111 };

    Droid() {
        super(0.02, true);
    }

    public String getSimulationFileName() {
        String ret = Main.getSimFile() ;
        if (ret != null)
            return ret ;

        return "disabled" ;
    }

    protected void addRobotSimulationModels() {
        ModelFactory factory = SimulationEngine.getInstance().getModelFactory() ;
        factory.registerModel("conveyor", "org.frc2020.models.ConveyorModel");
        factory.registerModel("droidoi", "org.frc2020.models.DroidOIModel");
        factory.registerModel("intake", "org.frc2020.models.IntakeModel");
        factory.registerModel("shooter", "org.frc2020.models.ShooterModel");
        factory.registerModel("droid_limelight", "org.frc2020.models.DroidLimelightModel") ;
        factory.registerModel("turret", "org.frc2020.models.TurretModel") ;
    }

    public String getName() {
        return "droid";
    }

    protected AutoController createAutoController() throws MissingParameterException, BadParameterTypeException {
        return new DroidAutoController(this) ;
    }

    protected byte[] getPracticeBotMacAddress() {
        return practice_bot_mac_addr ;
    }

    protected void hardwareInit() throws Exception {
        DroidRobotSubsystem robotsub = new DroidRobotSubsystem(this) ;
        setRobotSubsystem(robotsub);

        AlarmSubsystem alarms = robotsub.getAlarms() ;
        alarms.addEntry(30.0, new ClimbAlarm(robotsub.getOI().getGamepad()));
    }

    protected void enableMessages() {
        MessageLogger logger = getMessageLogger() ;

        // logger.enableLogging(Action.LoggerName) ;
        // logger.enableLogging(XeroPathManager.LoggerName) ;
        // logger.enableLogging(SettingsParser.LoggerName) ;
        // logger.enableLogging(DroidOISubsystem.SubsystemName) ;
        // logger.enableLogging(ConveyorSubsystem.SubsystemName) ;
        // logger.enableLogging(ConveyorSubsystem.SensorLoggerName) ;        
        // logger.enableLogging(GamePieceManipulatorSubsystem.SubsystemName) ;
        // logger.enableLogging(IntakeSubsystem.SubsystemName) ;
        // logger.enableLogging(ShooterSubsystem.SubsystemName) ;
        // logger.enableLogging(ClimberSubsystem.SubsystemName) ;
        // logger.enableLogging(BlinkySubsystem.SubsystemName) ;
        // logger.enableLogging(DroidLimeLightSubsystem.LoggerSubsystemNameName) ;
        // logger.enableLogging(DroidRobotSubsystem.TankdriveLoggerName) ;
        // logger.enableLogging(TargetTrackerSubsystem.SubsystemName) ;
        // logger.enableLogging(TurretSubsystem.SubsystemName) ;
        // logger.enableLogging(XeroPathManager.LoggerName) ;
        // logger.enableLogging(SettingsParser.LoggerName) ;
        // logger.enableLogging(XeroRobot.LoggerName) ;

        logger.enableLogging(Action.LoggerName) ;

        if (RobotBase.isSimulation()) {
            // logger.enableLogging(SimulationEngine.LoggerName) ;
            // logger.enableLogging("tankdrive_model") ;
            // logger.enableLogging("conveyor_model") ;
            // logger.enableLogging(ConveyorModel.LogBallPosition) ;
            // logger.enableLogging("shooter_model") ;
        }
    }

    protected void loadPathsFile() throws Exception {
        super.loadPathsFile(); ;
    }
}
