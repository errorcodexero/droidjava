package org.xero1425.simulator.models;

import edu.wpi.first.hal.sim.DriverStationSim;
import org.xero1425.simulator.engine.SimulationModel;
import org.xero1425.simulator.engine.SimulationEngine;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;

public class FMSModel extends SimulationModel {
    public FMSModel(final SimulationEngine engine, final String model, final String inst) {
        super(engine, model, inst);

        start_time_ = 0.0 ;
        auto_time_ = 0.0 ;
        teleop_time_ = 0.0 ;
        between_time_ = 0.0 ;
        closing_time_ = 0.0 ;        
    }

    public boolean create() {
        state_ = FMSState.Initializing ;

        if (hasProperty("autonomous")) {
            auto_time_ = setProperty("autonomous", getProperty("autonomous"), auto_time_) ;
        }
        else if (hasProperty("start")) {
            start_time_ = setProperty("start", getProperty("start"), start_time_) ;
        }        
        else if (hasProperty("between")) {
            between_time_ = setProperty("between", getProperty("between"), start_time_) ;
        }     
        else if (hasProperty("teleop")) {
            teleop_time_ = setProperty("teleop", getProperty("teleop"), start_time_) ;
        }

        setCreated();
        return true;
    }

    public void run(final double dt) {
        final DriverStationSim ds = getEngine().getDriverStation() ;

        final double elapsed = getRobotTime() - period_start_time_ ;
        switch(state_)
        {
            case Initializing:
                period_start_time_ = getRobotTime() ;
                state_ = FMSState.Start ;
                break ;

            case Start:
                if (elapsed >= start_time_)
                {
                    ds.setAutonomous(true);
                    ds.setEnabled(true);
                    state_ = FMSState.Auto ;
                    period_start_time_ = getRobotTime() ;
                }
                break ;

            case Auto:
                if (elapsed >= auto_time_)
                {
                    ds.setEnabled(false);
                    state_ = FMSState.Between ;
                    period_start_time_ = getRobotTime() ;
                }            
                break ;

            case Between:
                if (elapsed >= between_time_)
                {
                    ds.setAutonomous(false);
                    ds.setEnabled(true);
                    state_ = FMSState.Teleop ;
                    period_start_time_ = getRobotTime() ;
                }              
                break ;

            case Teleop:
                if (elapsed >= teleop_time_)
                {
                    ds.setEnabled(false);
                    state_ = FMSState.Closing ;
                    period_start_time_ = getRobotTime() ;
                }               
                break ;

            case Closing:
                if (elapsed >= closing_time_)
                {
                    state_ = FMSState.Done ;
                    period_start_time_ = getRobotTime() ;

                    getEngine().exitSimulator();
                }               
                break ;            

            case Done:
                break ;                                              
        }        
    }

    public boolean processEvent(final String name, final SettingsValue value) {
        boolean ret = false;

        if (name.equals("autonomous")) {
            auto_time_ = setProperty(name, value, auto_time_) ;
            ret = true ;
        }
        else if (name.equals("start")) {
            start_time_ = setProperty(name, value, start_time_) ;
            ret = true ;
        }
        else if (name.equals("between")) {
            between_time_ = setProperty(name, value, between_time_) ;
            ret = true ;
        }
        else if (name.equals("teleop")) {
            teleop_time_ = setProperty(name, value, teleop_time_) ;
            ret = true ;
        }
        else if (name.equals("fms")) {
            if (!value.isBoolean()) {
                final MessageLogger logger = getEngine().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("event: model ").addQuoted(getModelName());
                logger.add(" instance ").addQuoted(getInstanceName());
                logger.add(" event name ").addQuoted(name);
                logger.add(" value is not a boolean").endMessage();
            }
            else {
                try {
                    getEngine().getDriverStation().setFmsAttached(value.getBoolean());
                } catch (final BadParameterTypeException e) {
                }
            }
        }

        return ret ;
    }

    private double setProperty(final String name, final SettingsValue v, double ret) {
        if (!v.isDouble()) {
            final MessageLogger logger = getEngine().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" event name ").addQuoted(name);
            logger.add(" value is not a double").endMessage();
        }
        else {
            try {
                ret = v.getDouble();
            } catch (final BadParameterTypeException e) {
            }
        }

        return ret ;
    }

    private enum FMSState {
        Initializing,
        Start,
        Auto,
        Between,
        Teleop,
        Closing,
        Done,
    } ;

    private FMSState state_ ;
    private double period_start_time_ ;
    private double start_time_  ;
    private double auto_time_ ;
    private double teleop_time_ ;
    private double between_time_ ;
    private double closing_time_ ;
}