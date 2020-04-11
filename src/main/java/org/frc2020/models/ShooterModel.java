package org.frc2020.models;

import org.xero1425.simulator.engine.SimulationEngine;
import org.xero1425.simulator.engine.SimulationModel;
import org.xero1425.simulator.models.SimMotorController;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;

public class ShooterModel extends SimulationModel {
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
        double power = motors_.getPower() ;
        double calc_speed = power / kv_ ;
        double delta = calc_speed - current_speed_rpm_ ;

        if (Math.abs(delta) > rpm_change_per_second_ * dt) {
            if (delta < 0.0)
                delta = - rpm_change_per_second_ ;
            else
                delta = rpm_change_per_second_ ;
        }

        current_speed_rpm_ += delta ;
        double deltarev = current_speed_rpm_ * 2.0 / 3.0 /  60.0 * dt ;
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

        motors_.setEncoder(revs_) ;
    }
    
    private SimMotorController motors_ ;
    private double current_speed_rpm_ ;
    private double revs_ ;
    private double kv_ ;
    private double rpm_change_per_second_ ;
}