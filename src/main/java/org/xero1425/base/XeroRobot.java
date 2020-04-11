package org.xero1425.base;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.xero1425.simulator.engine.SimulationEngine;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageDestination;
import org.xero1425.misc.MessageDestinationFile;
import org.xero1425.misc.MessageDestinationThumbFile;
import org.xero1425.misc.SettingsParser;
import org.xero1425.misc.SimArgs;
import org.xero1425.misc.XeroPathManager;
import org.xero1425.base.motors.MotorFactory;
import org.xero1425.base.tankdrive.TankDriveFollowPathAction;
import org.xero1425.base.actions.Action;
import org.xero1425.base.controllers.*;

public abstract class XeroRobot extends TimedRobot {
    public static final String LoggerName = "xerorobot" ;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public XeroRobot(final double period) {
        super(period);

        period_ = period;

        final String name = getName();
        robot_paths_ = new RobotPaths(RobotBase.isSimulation(), name);

        // Setup the mesasge logger to log messages
        enableMessageLogger();
        logger_id_ = logger_.registerSubsystem(LoggerName) ;        
        logger_.startMessage(MessageType.Info).add("robot code starting").endMessage();

        if (RobotBase.isSimulation()) {
            String str = getSimulationFileName() ;
            if (str == null) {
                System.out.println("The code is setup to simulate, but the derived robot class did not provide a stimulus file") ;
                System.exit(1) ;
            }
            SimulationEngine.initializeSimulator(this, logger_);
            addRobotSimulationModels() ;
            SimulationEngine.getInstance().initAll(str) ;
        }

        // Get the network MAC address, used to determine comp bot versus practice bot
        getMacAddress();

        // Read the parameters file
        readParamsFile();

        // Read the paths files needed
        paths_ = new XeroPathManager(logger_, robot_paths_.pathsDirectory());
        try {
            loadPathsFile();
        } catch (Exception ex) {
            logger_.startMessage(MessageType.Error) ;
            logger_.add("caught exception reading path files -").add(ex.getMessage()).endMessage();
        }

        // Create the motor factor
        motors_ = new MotorFactory(logger_, settings_);

        // Create the plot manager
        plot_mgr_ = new PlotManager("/XeroPlot");

        // Store the initial time
        last_time_ = getTime();

        automode_ = -1;
    }

    public int getLoggerID() {
        return logger_id_ ;
    }

    protected abstract String getSimulationFileName() ;

    public void setRobotSubsystem(RobotSubsystem sub) {
        robot_subsystem_ = sub;
    }

    public int getLoopCount() {
        return loop_count_ ;
    }

    @Override
    public void robotInit() {
        boolean v;

        logger_.startMessage(MessageType.Info).add("initializing robot").endMessage();

        try {
            v = settings_.get("plotting:enabled").getBoolean();
            if (v == true)
                plot_mgr_.enable(true);
            else
                plot_mgr_.enable(false);
        } catch (Exception ex) {
            //
            // Either the parameter is missing, or is not a boolean. In either
            // case we just turn off plotting
            plot_mgr_.enable(false);
        }

        //
        // initialize the basic hardware
        //
        try {
            hardwareInit();
            if (RobotBase.isSimulation())
                SimulationEngine.getInstance().createModels() ;
        } catch (Exception ex) {
            logger_.startMessage(MessageType.Error);
            logger_.add("exception thrown in hardwareInit() - ").add(ex.getMessage());
            logger_.endMessage();

            robot_subsystem_ = null;
        }

        if (robot_subsystem_ == null) {
            logger_.startMessage(MessageType.Error);
            logger_.add("the robot subsystem was not set in hardwareInit()");
            logger_.endMessage();

            return;
        }

        delta_time_ = period_;
        try {
            robot_subsystem_.computeState();
        } catch (Exception ex) {
            logger_.startMessage(MessageType.Error);
            logger_.add("Exception caught in computeState() - ").add(ex.getMessage());
            logger_.endMessage();
            ;
        }

        try {
            robot_subsystem_.postHWInit();
        } catch (Exception ex) {
            logger_.startMessage(MessageType.Error);
            logger_.add("Exception caught in postHWInit() - ").add(ex.getMessage());
            logger_.endMessage();
        }

        try {
            auto_controller_ = createAutoController();
            if (isSimulation()) {
                checkPaths() ;
            }
        }
        catch(Exception ex) {
            logger_.startMessage(MessageType.Error);
            logger_.add("Exception caught creating automode controller - ").add(ex.getMessage());
            logger_.endMessage();
        }

        try {
            teleop_controller_ = createTeleopController();
        }
        catch(Exception ex) {
            logger_.startMessage(MessageType.Error);
            logger_.add("Exception caught creating teleop controller - ").add(ex.getMessage());
            logger_.endMessage();            
        }
    }

    @Override
    public void autonomousInit() {
        if (robot_subsystem_ == null)
            return;

        updateAutoMode();
        logAutoModeState();

        current_controller_ = auto_controller_;
        if (current_controller_ != null)
            current_controller_.init();

        robot_subsystem_.init(LoopType.Autonomous);

        loop_count_ = 0 ;
    }

    @Override
    public void autonomousPeriodic() {
        if (robot_subsystem_ == null)
            return;

        robotLoop(LoopType.Autonomous);

        loop_count_++ ;
    }

    @Override
    public void teleopInit() {
        if (robot_subsystem_ == null)
            return;

        logger_.startMessage(MessageType.Info).add("Staring teleop mode").endMessage();

        current_controller_ = teleop_controller_;
        if (current_controller_ != null)
            current_controller_.init();

        robot_subsystem_.init(LoopType.Teleop);

        loop_count_ = 0 ;
    }

    @Override
    public void teleopPeriodic() {
        if (robot_subsystem_ == null)
            return;

        robotLoop(LoopType.Teleop);

        loop_count_++ ;
    }

    @Override
    public void testInit() {
        if (robot_subsystem_ == null)
            return;

        logger_.startMessage(MessageType.Info).add("Staring teleop mode").endMessage();

        current_controller_ = test_controller_;
        if (current_controller_ != null)
            current_controller_.init();

        robot_subsystem_.init(LoopType.Test);
        
        loop_count_ = 0 ;
    }

    @Override
    public void testPeriodic() {
        if (robot_subsystem_ == null)
            return;

        loop_count_++ ;
    }

    @Override
    public void simulationInit() {
    }

    @Override
    public void simulationPeriodic() {
    }

    @Override
    public void disabledInit() {
        if (robot_subsystem_ == null)
            return;

        current_controller_ = null;
        robot_subsystem_.reset();

        automode_ = -1;
        robot_subsystem_.init(LoopType.Disabled);

        loop_count_ = 0 ;
    }

    @Override
    public void disabledPeriodic() {
        if (robot_subsystem_ == null)
            return;

        double initial_time = getTime();
        delta_time_ = initial_time - last_time_;
        updateAutoMode();
        try {
            robot_subsystem_.computeState();
        } catch (Exception ex) {
            logger_.startMessage(MessageType.Error);
            logger_.add("exception caught in computeState() in disabledPeriodic loop -");
            logger_.add(ex.getMessage());
            logger_.endMessage();
        }

        if (isSimulation()) {
            SimulationEngine engine = SimulationEngine.getInstance() ;
            engine.run(getTime()) ;
        }

        last_time_ = initial_time;
        loop_count_++ ;
    }

    @Override
    public void robotPeriodic() {
        if (robot_subsystem_ == null)
            return;
    }

    public RobotSubsystem getRobotSubsystem() {
        return robot_subsystem_;
    }

    public double getTime() {
        return Timer.getFPGATimestamp();
    }

    public double getDeltaTime() {
        return delta_time_;
    }

    public MessageLogger getMessageLogger() {
        return logger_;
    }

    public SettingsParser getSettingsParser() {
        return settings_;
    }

    public XeroPathManager getPathManager() {
        return paths_;
    }

    public MotorFactory getMotorFactory() {
        return motors_;
    }

    public PlotManager getPlotManager() {
        return plot_mgr_;
    }

    protected void enableMessages() {
    }

    protected void addRobotSimulationModels() {
    }

    public String getName() {
        return "XeroRobot";
    }

    protected void hardwareInit() throws Exception {
        throw new Exception("override this in the derived class");
    }

    protected byte[] getPracticeBotMacAddress() {
        return null;
    }

    protected boolean isPracticeBot() {
        if (mac_addr_ == null)
            return false;

        byte[] addr = getPracticeBotMacAddress();
        if (addr == null)
            return false;

        return mac_addr_.equals(addr);
    }

    protected abstract AutoController createAutoController() throws MissingParameterException, BadParameterTypeException ;
    
    protected TeleopController createTeleopController() throws MissingParameterException, BadParameterTypeException {
        return new TeleopController(this, getName() + "-teleop") ;
    }

    private void robotLoop(LoopType ltype) {
        double initial_time = getTime() ;
        delta_time_ = initial_time - last_time_ ;

        if (isSimulation() && delta_time_ < 0.005) {
            //
            // When we run the simulation engine, we pause the time for the robot while we evaluate the
            // models.  I have noticed that this causes the periodic functions to be called back to back with
            // less than a few hundred micro-seconds of robot time between the calls.  This conditional obviously
            // is only valid during simulations and prevents robot loops with very small delta time values from
            // being processed.
            //
            return ;
        }

        logger_.startMessage(MessageType.Debug, logger_id_) ;
        logger_.add("xerorobot: starting loop,") ;
        logger_.add("time", initial_time) ;
        logger_.add("delta", delta_time_) ;
        logger_.endMessage() ;

        if (isSimulation()) {
            SimulationEngine engine = SimulationEngine.getInstance() ;
            engine.run(delta_time_) ;
        }        

        try {
            robot_subsystem_.computeState();
        }
        catch(Exception ex) {
            logger_.startMessage(MessageType.Error) ;
            logger_.add("exception caught in computeState() in robot loop -") ;
            logger_.add(ex.getMessage()) ;
            logger_.endMessage();
        }

        if (current_controller_ != null)
            current_controller_.run() ;

        try {
            robot_subsystem_.run();
        }
        catch(Exception ex) {
            logger_.startMessage(MessageType.Error) ;
            logger_.add("exception caught in run() in robot loop -") ;
            logger_.add(ex.getMessage()) ;
            logger_.endMessage();            
        }


        last_time_ = initial_time ;
    }

    private void logAutoModeState() {
        DriverStation ds = DriverStation.getInstance() ;

        logger_.startMessage(MessageType.Info) ;
        logger_.add("Entering Autonomous Mode").endMessage();

        logger_.startMessage(MessageType.Info) ;
        logger_.add("    Automode Number: ").add(automode_).endMessage();

        logger_.startMessage(MessageType.Info) ;
        logger_.add("    Automode Name: ").add(auto_controller_.getAutomodeName()).endMessage();


        String str = "undefined" ;
        if (ds.getAlliance() == Alliance.Red)
            str = "RED" ;
        else if (ds.getAlliance() == Alliance.Blue)
            str = "BLUE" ;
        logger_.startMessage(MessageType.Info) ;
        logger_.add("    Alliance: ").add(str).endMessage();

        logger_.startMessage(MessageType.Info) ;
        logger_.add("    Location: ").add(ds.getLocation()).endMessage();

        logger_.startMessage(MessageType.Info) ;
        logger_.add("    Location: ").add(game_data_).endMessage();   
        
        logger_.startMessage(MessageType.Info) ;
        logger_.add("    EventName: ").add(ds.getEventName()).endMessage();

        str = "invalid" ;
        switch(ds.getMatchType()) {
            case None:
                str = "NONE" ;
                break ;
            case Elimination:
                str = "ELIMINATION" ;
                break ;
            case Qualification:
                str = "QUALIFICATION" ;
                break ;
            case Practice:
                str = "PRACTICE" ;
                break ;
        }
        logger_.startMessage(MessageType.Info) ;
        logger_.add("    MatchType: ").add(str).endMessage();

        logger_.startMessage(MessageType.Info) ;
        logger_.add("    MatchNumber: ").add(ds.getMatchNumber()).endMessage();     
        
        logger_.startMessage(MessageType.Info) ;
        logger_.add("    MatchTime: ").add(ds.getMatchTime()).endMessage();

        str = "NO" ;
        if (ds.isFMSAttached())
            str = "YES" ;
            logger_.startMessage(MessageType.Info) ;
            logger_.add("    FMS Attached: ").add(str).endMessage();
    }

    private void displayAutoModeState() {
        SmartDashboard.putNumber("AutoModeNumber", automode_) ;
        SmartDashboard.putString("AutoModeName", auto_controller_.getAutoModeName()) ;
    }

    protected int getAutoModeSelection() {
        return robot_subsystem_.getOI().getAutoModeSelector() ;
    }

    private void updateAutoMode() {
        if (auto_controller_ != null && robot_subsystem_.getOI() != null) {
            DriverStation ds = DriverStation.getInstance() ;
            String msg = ds.getGameSpecificMessage() ;

            int sel = getAutoModeSelection() ;
            if (sel != automode_ || msg.equals(game_data_) || ds.isFMSAttached() != fms_connection_ || auto_controller_.isTestMode())
            {
                automode_ = sel ;
                game_data_ = msg ;
                fms_connection_ = ds.isFMSAttached() ;
                auto_controller_.updateAutoMode(automode_, game_data_) ;
                displayAutoModeState() ;
            }
        }
    }

    private void enableMessageLogger() {
        String logfile = SimArgs.LogFileName ;
        MessageDestination dest ;

        logger_ = new MessageLogger();
        logger_.setTimeSource(new RobotTimeSource());

        if (logfile != null) {
            dest = new MessageDestinationFile(logfile) ;
        }
        else {
            dest = new MessageDestinationThumbFile(robot_paths_.logFileDirectory(), 250);
        }
        logger_.addDestination(dest);
        enableMessages();
    }

    private void readParamsFile() {
        settings_ = new SettingsParser(logger_);

        String bot ;
        if (isPracticeBot())
            bot = "PRACTICE" ;
        else
            bot = "COMPETITION" ;

        settings_.addDefine(bot) ;
        logger_.startMessage(MessageType.Info).add("reading params for bot ").addQuoted(bot).endMessage() ;

        if (!settings_.readFile(robot_paths_.deployDirectory() + getName() + ".dat")) {
            logger_.startMessage(MessageType.Error).add("error reading parameters file").endMessage();
        }
    }

    protected void loadPathsFile() throws Exception {
        XeroPathManager mgr = getPathManager() ;
        mgr.setExtensions("_left.csv", "_right.csv");

        try (Stream<Path> walk = Files.walk(Paths.get(mgr.getBaseDir()))) {

            List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith("_main.csv")).collect(Collectors.toList());
            for(String name : result) {
                int index = name.lastIndexOf(File.separator) ;
                if (index != -1) {
                    name = name.substring(index + 1) ;
                    name = name.substring(0, name.length() - 9) ;
                    mgr.loadPath(name) ;
                }
            }
        }
        catch(IOException ex) {
        }
    }        

    private void getMacAddress() {
        Enumeration<NetworkInterface> netlist ;
        mac_addr_ = null ;

        try {
            netlist = NetworkInterface.getNetworkInterfaces() ;
            while (netlist.hasMoreElements())
            {
                NetworkInterface ni = netlist.nextElement() ;
                String name = ni.getName() ;
                if (name.equals("lo"))
                    continue ;
                    
                mac_addr_ = ni.getHardwareAddress() ;
                break ;
            }
        }
        catch(Exception ex)
        {
            mac_addr_ = null ;
        }

        logger_.startMessage(MessageType.Info).add("Mac Address: ") ;
        if (mac_addr_ == null)
            logger_.add("NONE") ;
        else
        {
            for(int j = 0 ; j < mac_addr_.length ; j++)
            {
                int v = mac_addr_[j] & 0xFF;
                if (j != 0)
                    logger_.add(':') ;
                logger_.add(HEX_ARRAY[v >>> 4]) ;
                logger_.add(HEX_ARRAY[v & 0x0F]) ;
            }
        }
        logger_.endMessage();
    }

    private void checkPaths() {
        boolean valid = true ;
        List<Action> actions = new ArrayList<Action>() ;
        List<AutoMode> modes = auto_controller_.getAllAutomodes();
        for(AutoMode mode : modes) {
            logger_.startMessage(MessageType.Debug, logger_id_) ;
            logger_.add("processing automode ").addQuoted(mode.getName()).endMessage();
            actions.clear(); ;
            mode.getAllChildren(actions);
            for(Action act : actions) {
                if (act instanceof TankDriveFollowPathAction) {
                    TankDriveFollowPathAction pa = (TankDriveFollowPathAction)act ;
                    logger_.startMessage(MessageType.Debug, logger_id_) ;
                    logger_.add("    processing path ").addQuoted(pa.getPathName()).endMessage();

                    if (!paths_.hasPath(pa.getPathName())) {
                        logger_.startMessage(MessageType.Error) ;
                        logger_.add("automode ").addQuoted(mode.getName()) ;
                        logger_.add(" requires path ").addQuoted(pa.getPathName()) ;
                        logger_.add(" which is missing from the paths directory") ;
                        logger_.endMessage();
                        valid = false ;
                    }
                }
            }
            logger_.startMessage(MessageType.Debug, logger_id_) ;
            logger_.add("  contained ").add(actions.size()).add(" actions total").endMessage(); 
        }

        if (!valid) {
            logger_.startMessage(MessageType.Fatal).add("some required paths are missing").endMessage();
        }
    }

    private final RobotPaths robot_paths_;
    private final double period_ ;
    private double delta_time_ ;
    private MessageLogger logger_ ;
    private SettingsParser settings_ ;
    private PlotManager plot_mgr_ ;
    private XeroPathManager paths_ ;
    private MotorFactory motors_ ;
    private double last_time_;
    private byte[] mac_addr_ ;
    private RobotSubsystem robot_subsystem_ ;

    private int automode_ ;
    private String game_data_ ;
    private boolean fms_connection_ ;
    private int loop_count_ ;
    private int logger_id_ ;

    private BaseController current_controller_ ;
    private AutoController auto_controller_ ;
    private TeleopController teleop_controller_ ;
    private TestController test_controller_ ;
}
