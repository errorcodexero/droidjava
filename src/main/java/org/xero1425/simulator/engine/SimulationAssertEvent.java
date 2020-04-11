package org.xero1425.simulator.engine;

import org.xero1425.base.Subsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;

public class SimulationAssertEvent extends SimulationEvent {
    public SimulationAssertEvent(double t, String subsystem, String name, SettingsValue v) {
        super(t);

        subsystem_ = subsystem;
        name_ = name;
        value_ = v;

        tolerance_ = 1e-9 ;
    }

    public void run(SimulationEngine engine) {
        Subsystem sub = engine.getRobot().getRobotSubsystem().getSubsystemByName(subsystem_);
        if (sub == null) {
            MessageLogger logger = engine.getMessageLogger();
            logger.startMessage(MessageType.Error);
            logger.add("AssertFailed: ");
            logger.add("subsystem", subsystem_);
            logger.add(" - does not exist in the robot");
            logger.endMessage();
            engine.addAssertError();
        } else {
            MessageLogger logger = engine.getMessageLogger();
            SettingsValue v = sub.getProperty(name_);
            if (v == null) {
                logger.startMessage(MessageType.Error);
                logger.add("AssertFailed: ");
                logger.add("subsystem", subsystem_);
                logger.add(" property ", name_);
                logger.add(" - subsystem did not contain the given property");
                logger.endMessage();
                engine.addAssertError();
            } else {
                boolean pass = false;

                if (v.isDouble()) {
                    try {
                        pass = Math.abs(v.getDouble() - value_.getDouble()) < tolerance_;
                    } catch (BadParameterTypeException e) {
                        // Should never happen
                        pass = false ;
                    }
                }
                else {
                    pass = v.equals(value_) ;
                }

                if (!pass) {
                    logger.startMessage(MessageType.Error) ;
                    logger.add("AssertFailed: ") ;
                    logger.add("subsystem", subsystem_) ;
                    logger.add(" property ", name_) ;
                    logger.add(" expected ").addQuoted(value_.toString()) ;
                    logger.add(" got ").addQuoted(v.toString()) ;
                    logger.endMessage();
                    engine.addAssertError();                
                }
                else {
                    logger.startMessage(MessageType.Info) ;
                    logger.add("AssertPassed: ") ;
                    logger.add("subsystem", subsystem_) ;
                    logger.add(" property ", name_) ;
                    logger.add(" value ").addQuoted(value_.toString()) ;
                    logger.endMessage();
                    engine.addAssertPassed() ;
                }
            }
        }
    }

    public String toString() {
        return "SimulationAssertEvent" ;
    }

    public void setTolerance(double v) {
        tolerance_ = v ;
    }

    private String subsystem_ ;
    private String name_ ;
    private SettingsValue value_ ;
    private double tolerance_ ;
}