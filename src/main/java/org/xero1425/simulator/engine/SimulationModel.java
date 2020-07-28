package org.xero1425.simulator.engine;

import java.util.HashMap;
import java.util.Map;

import org.xero1425.misc.SettingsValue;

public abstract class SimulationModel {
    public SimulationModel(SimulationEngine engine, String model, String instance) {
        engine_ = engine ;
        model_ = model ;
        instance_ = instance ;
        created_ = false ;

        props_ = new HashMap<String, SettingsValue>() ;

        logger_id_ = engine.getMessageLogger().registerSubsystem(model + "_model") ;
    }

    public String statusString() {
        return "" ;
    }

    public String getModelName() {
        return model_ ;
    }

    public String getInstanceName() {
        return instance_ ;
    }

    public abstract boolean create() ;
    public abstract void run(double dt) ;
    public abstract boolean processEvent(String name, SettingsValue value) ;
    public void startCycle()  {
    }

    public void endCycle() {
    }

    public boolean hasProperty(String name) {
        return props_.containsKey(name) ;
    }

    public void setProperty(String name, SettingsValue value) {
        props_.put(name, value) ;
    }

    public SettingsValue getProperty(String name) {
        return props_.get(name) ;
    }

    public double getRobotTime() {
        return engine_.getRobot().getTime() ;
    }

    public SimulationEngine getEngine() {
        return engine_ ;
    }
    
    public boolean isCreated() {
        return created_ ;
    }

    protected void setCreated() {
        created_ = true ;
    }

    protected int getLoggerID() {
        return logger_id_ ;
    }

    private SimulationEngine engine_ ;
    private String model_ ;
    private String instance_ ;
    private Map<String, SettingsValue> props_ ;
    private boolean created_ ;
    private int logger_id_ ;
}