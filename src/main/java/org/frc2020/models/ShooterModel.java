package org.frc2020.models;

import org.xero1425.simulator.engine.SimulationEngine;
import org.xero1425.simulator.engine.SimulationModel;
import org.xero1425.simulator.models.SimMotorController;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;

public class ShooterModel extends SimulationModel {
    private static final String SubTableName = "shooter" ;

    public ShooterModel(SimulationEngine engine, String model, String inst) {
        super(engine, model, inst);
    }

    public boolean create() {
        motors_ = new SimMotorController(this, "shooter") ;
        if (!motors_.createMotor())
            return false ;

        try {
            kv_ = getProperty("kv").getDouble();
        } catch (BadParameterTypeException e) {
            MessageLogger logger = getEngine().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create model ").addQuoted(getModelName()).add(" instance ").addQuoted(getInstanceName()) ;
            logger.add(" - missing parameter ").addQuoted("kv").endMessage();
            return false ;
        }
        try {
            rpm_change_per_second_ = getProperty("change").getDouble();
        } catch (BadParameterTypeException e) {
            MessageLogger logger = getEngine().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create model ").addQuoted(getModelName()).add(" instance ").addQuoted(getInstanceName()) ;
            logger.add(" - missing parameter ").addQuoted("change").endMessage();
            return false ;
        }
        
        revs_ = 0.0 ;

        setCreated();
        return true ;
    }

    public boolean processEvent(String name, SettingsValue value) {
        return false ;
    }

    public void run(double dt) {
        if (getEngine().getSimulationTime() > 9.25) {
            MessageLogger logger = getEngine().getMessageLogger() ;
            logger.startMessage(MessageType.Debug, getLoggerID()) ;
            logger.add("shooter model:") ;
            logger.endMessage();
        }

        //
        // Get the power from the motor
        //
        double power = motors_.getPower() ;

        //
        // Calculate the speed in RPM
        //
        double calc_speed = power / kv_ ;

        //
        // Calculate the delta speed between the desired speed and the current speed
        //
        double delta = calc_speed - current_speed_rpm_ ;

        //
        // Move as much as we can to the desired speed.
        //
        if (Math.abs(delta) > rpm_change_per_second_ * dt) {
            if (delta < 0.0)
                delta = - rpm_change_per_second_ ;
            else
                delta = rpm_change_per_second_ ;
        }

        //
        // Add the delta to the current speed so our new current speed is as close
        // as we can be to the speed that the motor power should drive
        //
        current_speed_rpm_ += delta ;

        //
        // Calculate the incremental number of revolutions (usually a fraction) that have occured 
        // since the last simulation loop.  Assume the new speed for the motor
        //
        double deltarev = current_speed_rpm_ / 60.0 * dt ;

        //
        // Add the incremental number of revolutions to the total traveled so far
        //
        revs_ += deltarev ;

        MessageLogger logger = getEngine().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getLoggerID()) ;
        logger.add("shooter model:") ;
        logger.add("dt", dt) ;
        logger.add("speed", current_speed_rpm_) ;        
        logger.add("power", power) ;
        logger.add("calc", calc_speed) ;
        logger.add("delta", deltarev) ;
        logger.add("revs", revs_) ;
        logger.endMessage();

        motors_.setEncoder(revs_ * 42) ;

        NetworkTable table = NetworkTableInstance.getDefault().getTable(SimulationEngine.NetworkTableName).getSubTable(SubTableName) ;
        table.getEntry("speed").setNumber(current_speed_rpm_) ;
    }
    
    private SimMotorController motors_ ;
    private double current_speed_rpm_ ;
    private double revs_ ;
    private double kv_ ;
    private double rpm_change_per_second_ ;
}