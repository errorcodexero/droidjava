package org.xero1425.simulator.engine;

import java.util.ArrayList;
import java.util.List;
import edu.wpi.first.hal.sim.DriverStationSim;
import edu.wpi.first.hal.sim.SimHooks;
import edu.wpi.first.wpilibj.DriverStation;
import org.xero1425.base.XeroRobot;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;

public class SimulationEngine {
    public static final String LoggerName = "simulator" ;
    public static final String NetworkTableName = "XeroSim" ;

    private SimulationEngine(XeroRobot robot, MessageLogger logger) {
        logger_ = logger ;
        robot_ = robot ;
        logger_id_ = logger_.registerSubsystem(LoggerName);

        models_ = new ModelManager(this);
        events_ = new EventsManager(this);

        active_models_ = new ArrayList<SimulationModel>() ;

        failed_count_ = 0 ;
        passed_count_ = 0 ;
        delta_t_ = robot.getPeriod() / 10 ;
    }

    static public SimulationEngine getInstance() {
        return the_one_;
    }

    static public void initializeSimulator(XeroRobot robot, MessageLogger logger) {
        DriverStation.getInstance();
        the_one_ = new SimulationEngine(robot, logger);
    }

    public void addAssertError() {
        failed_count_++ ;
    }

    public void addAssertPassed() {
        passed_count_++ ;
    }

    public void exitSimulator() {
        int code = 0 ;

        if (failed_count_ == 0)
        {
            logger_.startMessage(MessageType.Info).add("Simulation completed normally").endMessage();
            logger_.startMessage(MessageType.Info).add("    ").add(passed_count_).add(" asserts passed") ;
        }
        else
        {
            code = 1 ;
            logger_.startMessage(MessageType.Info).add("Simulation complete with " + failed_count_ + " errors").endMessage();
            logger_.startMessage(MessageType.Info).add("    ").add(passed_count_).add(" asserts passed").endMessage();
            logger_.startMessage(MessageType.Info).add("    ").add(failed_count_).add(" asserts failed").endMessage();            
        }

        java.lang.System.exit(code) ;
    }

    public ModelFactory getModelFactory() {
        return models_.getFactory() ;
    }

    public void createModels() {
        for(SimulationModel model : active_models_) {
            if (!model.isCreated()) {
                logger_.startMessage(MessageType.Debug, logger_id_) ;
                logger_.add("late model creation - model ").addQuoted(model.getModelName()) ;
                logger_.add(" instance ").addQuoted(model.getInstanceName()).endMessage();   
                try {             
                    model.create() ;
                }
                catch(Exception ex) {
                    
                }
            }
        }
    }

    public DriverStationSim getDriverStation() {
        return dssim_ ;
    }

    public void addModel(SimulationModel model) {
        if (model.hasProperty("create_early")) {
            logger_.startMessage(MessageType.Debug, logger_id_) ;
            logger_.add("create model early call, model ").addQuoted(model.getModelName()) ;
            logger_.add(" instance ").addQuoted(model.getInstanceName()).endMessage();
            model.create() ;
        }
        active_models_.add(model) ;
        dssim_.notifyNewData();
    }

    //
    // This is the amount of time to run forward to have the simulation
    // models catch up with the simulation
    //
    public void run(double t) {
        double sofar = 0 ;
        SimHooks.pauseTiming();
        for(SimulationModel model : active_models_)
            model.startCycle();

        while (sofar < t) {
            double dt = t - sofar ;
            if (dt > delta_t_)
                dt = delta_t_ ;
            processEvents() ;           
            runModels(dt) ;
            dssim_.notifyNewData() ;
            sofar += dt ;
        }
        for(SimulationModel model : active_models_)
            model.endCycle();        
        SimHooks.resumeTiming();
    }

    public MessageLogger getMessageLogger() {
        return logger_ ;
    }

    public XeroRobot getRobot() {
        return robot_ ;
    }

    public double getSimulationTime() {
        return robot_.getTime() ;
    }

    public SimulationModel findModel(String model, String inst) {
        for(SimulationModel m: active_models_) {
            if (m.getModelName().equals(model) && m.getInstanceName().equals(inst))
                return m ;
        }

        return null ;
    }

    private void processEvents() {
        while (events_.size() > 0) {
            SimulationEvent ev = events_.getFirstEvent() ;
            if (ev.getTime() > getRobot().getTime())
                break ;

            logger_.startMessage(MessageType.Debug, logger_id_) ;
            logger_.add("processing event ").addQuoted(ev.toString()) ;
            logger_.endMessage();
            ev.run(this) ;
            events_.removeFirstEvent();
        }
    }

    private void runModels(double dt) {
        for(SimulationModel m : active_models_) {
            if (m.isCreated())
                m.run(dt) ;
            else {
                logger_.startMessage(MessageType.Error) ;
                logger_.add("did not run model ").addQuoted(m.getModelName()) ;
                logger_.add(" instance ").addQuoted(m.getInstanceName()) ;
                logger_.add(" - model not created").endMessage(); 
            }
        }
    }    

    private void readModelFile(String file) {
        models_.readModelFile(file) ;
    }

    private void readEventsFile(String file) {
        events_.readEventsFile(file) ;
    }
    
    public void initAll(String simfile) {
        if (dssim_ == null)
            dssim_ = new DriverStationSim() ;

        readModelFile("src/sim/robot.json") ;
        readEventsFile("src/sim/sims/" + simfile + ".json") ;
    }

    private static SimulationEngine the_one_ = null ;
    
    private DriverStationSim dssim_ = null ;
    private MessageLogger logger_ ;
    private XeroRobot robot_ ;
    private int logger_id_ ;

    private ModelManager models_ ;
    private EventsManager events_ ;

    private double delta_t_ ;

    private List<SimulationModel> active_models_ ;
    private int failed_count_ ;
    private int passed_count_ ;
}