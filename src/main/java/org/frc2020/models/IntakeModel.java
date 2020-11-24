package org.frc2020.models;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.xero1425.simulator.engine.SimulationEngine;
import org.xero1425.simulator.engine.SimulationModel;
import org.xero1425.simulator.models.SimMotorController;
import org.xero1425.base.motors.SparkMaxMotorController;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;

public class IntakeModel extends SimulationModel {
    private static final String SubTableName = "intake" ;

    public IntakeModel(SimulationEngine engine, String model, String inst) {
        super(engine, model, inst);
    }

    public boolean create() {
        spin_ = new SimMotorController(this, "spin") ;
        if (!spin_.createMotor())
            return false ;

        updown_ = new SimMotorController(this, "updown") ;
        if (!updown_.createMotor())
            return false ;

        try {
            ticks_per_second_per_volt_ = getProperty("ticks_per_second_per_volt").getDouble();
        } catch (BadParameterTypeException e) {
            MessageLogger logger = getEngine().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create model ").addQuoted(getModelName()).add(" instance ").addQuoted(getInstanceName()) ;
            logger.add(" - missing parameter ").addQuoted("ticks_per_second_per_volt").endMessage();
            return false ;
        }
        ticks_ = 0 ;

        setCreated();
        return true ;
    }

    public boolean processEvent(String name, SettingsValue value) {
        return false ;
    }

    public void run(double dt) {
        double dist = updown_.getPower() * ticks_per_second_per_volt_ * dt ;
        ticks_ += dist ;
        if (ticks_ < -100)
            ticks_ = -100 ;
        else if (ticks_ > 1600)
            ticks_ = 1600 ;
        updown_.setEncoder(ticks_ / (double)SparkMaxMotorController.TicksPerRevolution) ;

        NetworkTable table = NetworkTableInstance.getDefault().getTable(SimulationEngine.NetworkTableName).getSubTable(SubTableName) ;
        table.getEntry("angle").setNumber(ticks_) ;
        table.getEntry("speed").setNumber(spin_.getPower()) ;
    }

    public boolean isDownAndRunning() {
        return ticks_ > 1400 && spin_.getPower() > 0.3 ;
    }
    
    private SimMotorController spin_ ;
    private SimMotorController updown_ ;
    private double ticks_per_second_per_volt_ ;
    private double ticks_  ;
}