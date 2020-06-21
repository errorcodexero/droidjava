package org.xero1425.base;

import java.util.List;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.xero1425.base.actions.Action;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.SettingsParser;
import org.xero1425.misc.SettingsValue;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/// \brief the base class for all subsystems that make up the robot.
/// The subsystem class manages the interaction of subsytems with the robot control loop for each
/// subsystem.  A subsystem goes through a specific lifecycle.
/// <table>
/// <caption id="multi_row">Subsystemn lifecycle</caption>
/// <tr><th>State<th>Purpose        <th>Column 3
/// <tr><td rowspan="2">cell row=1+2,col=1<td>cell row=1,col=2<td>cell row=1,col=3
/// <tr><td rowspan="2">cell row=2+3,col=2                    <td>cell row=2,col=3
/// <tr><td>cell row=3,col=1                                  <td rowspan="2">cell row=3+4,col=3
/// <tr><td colspan="2">cell row=4,col=1+2
/// <tr><td>cell row=5,col=1              <td colspan="2">cell row=5,col=2+3
/// <tr><td colspan="2" rowspan="2">cell row=6+7,col=1+2      <td>cell row=6,col=3
/// </table>
///
public class Subsystem {
    //
    // The current action assigned to this subsystme
    //
    private Action action_;

    //
    // The default action to run when no action is assigned
    //
    private Action default_action_;

    //
    // THe parent subsystem of this subsystem
    //
    private final Subsystem parent_;

    //
    // The name of this subsystem
    //
    private final String name_;

    //
    // The children subsystem of this subsystem
    //
    private final List<Subsystem> children_;

    //
    // The main robot object
    //
    private XeroRobot robot_;

    //
    // The ID for logging subsystem related messages to the log file
    //
    private final int logger_id_ ;

    //
    // If true, the default action actually finished (we don't really want this to happen)
    //
    private boolean finished_default_ ;

    //
    // The total time spent running code in this subsystem.  This captures the compute state
    // run time since this is the method that takes a lot of time in time critical subsystem code.
    //
    private double total_time_ ;

    //
    // The number of times this subsystem has been run
    //
    private int total_cnt_ ;

    //
    // The maximum time spent on any one robot loop in the code for this subsystem
    //
    private double max_time_ ;

    //
    // The minimum time spend on any one robot loop in the code for this subsystem
    //
    private double min_time_ ;

    //
    // The output format for logging information about the subsystem run times
    //
    private DecimalFormat fmt_ ;

    //
    // If true, this subsystem logs much information
    //
    private boolean verbose_ ;

    /// \brief used to give the display type for a value
    public enum DisplayType {
        Always,                 ///< Always display this value
        Verbose,                ///< Display this value when running
        Disabled,               ///< Display only when the robot is disabled
    } ;

    /// \brief Create a new subsystem
    /// \param parent the parent for the current subsystem
    /// \param name the name of the current subsystem
    public Subsystem(final Subsystem parent, final String name) {
        parent_ = parent;
        name_ = name;
        children_ = new ArrayList<Subsystem>();
        action_ = null;
        default_action_ = null;
        if (parent != null) {
            robot_ = parent.getRobot();
            logger_id_ = getRobot().getMessageLogger().registerSubsystem(name);
        } else {
            logger_id_ = -1;
        }

        finished_default_ = false;
        total_time_ = 0.0;
        total_cnt_ = 0;
        min_time_ = Double.MAX_VALUE;
        max_time_ = 0.0;
        fmt_ = new DecimalFormat("00.000");
        verbose_ = false;

        if (getRobot() != null) {
            String pname = name_ + ":verbose";
            SettingsParser p = getRobot().getSettingsParser();
            try {
                if (p.isDefined(pname) && p.get(pname).isBoolean() && p.get(pname).getBoolean())
                    verbose_ = true;
            } catch (MissingParameterException e) {
                // Should never happen
                verbose_ = false ;
            } catch (BadParameterTypeException e) {
                // Should never happen
                verbose_ = false ;
            }
        }
    }

    /// \brief returns a property for the simulation system.
    /// This base class always returns null.  This method is expected to be overridden
    /// in a derived class to return subsystem specific properties.
    /// \param name the name of the property to return
    /// \returns always returns null
    public SettingsValue getProperty(String name) {
        return null ;
    }

    /// \brief returns a subsystem by name.
    /// If this subsystem has the requested name, this subsystem is returned.  Otherwise
    /// all of the children for the current subsystem are searched recursively until a
    /// subsystem with the name given is found.
    /// \param name the name of the subsystem desired
    /// \returns the subsystem with the given name, or null if it does not exist within this subsystem
    public Subsystem getSubsystemByName(String name) {
        if (name_.equals(name))
            return this ;

        return getChildByName(name) ;
    }

    /// \brief get a child subsystem by name
    /// All child subsystem are searched recursively until a subsystem with the name given
    /// is found.  If a subsystem with the given name is not found, null is returned
    /// \param name the name of the subsystem of interest
    /// \returns the subsystem with the given nama, or null if a subsystem with the name does not exist
    public Subsystem getChildByName(String name) {
        for(Subsystem child: children_) {
            Subsystem ret = child.getChildByName(name) ;
            if (ret != null)
                return ret ;

            if (child.getName().equals(name))
                return child ;
        }

        return null ;
    }

    /// \brief returns the name of the current subsystem
    /// \returns the name of the current subsystem
    public String getName() {
        return name_;
    }

    /// \brief returns the parent of the subsystem
    /// \returns the parent of the subsystem
    public Subsystem getParent() {
        return parent_;
    }

    /// \brief returns a reference to the robot object
    /// \returns a reference to the robot object
    public XeroRobot getRobot() {
        return robot_;
    }

    /// \brief return the logger ID for message assocaited with this subsystem
    /// \returns the logger iD for message associated with this subsystem
    public int getLoggerID() {
        return logger_id_ ;
    }

    /// \brief add a child subsystem to this current subystem
    /// \param sub the subsystem to add as a child
    public void addChild(final Subsystem sub) throws Exception {
        children_.add(sub);
    }

    /// \brief initialize the subsystem. 
    /// This is called at the start of the robot loop of each type.  In other words it is
    /// called at the start of autonomous (LoopType.Autonomous), teleop (LoopType.Teleop),
    /// and disabled (LoopType.Disabled).
    /// \param ltype the loop type we are running
    public void init(LoopType ltype) {
        for(Subsystem sub : children_)
            sub.init(ltype);        
    }

    /// \brief This method is called when the robot enters the disabled state.
    /// It is used to reset the hardware of the robot.  Keep in mind the robot enters
    /// the disabled state at the start of robot execution, between autonomous and
    /// teleop, and after teleop is complete.  This method can be used to reset hardware
    /// actions.  For instance, there was a turret rotating at the end of auto, this method
    /// could be used to reset the motors to stopped for the turret so they did not start
    /// up immediately when teleop begins.
    public void reset() {
        for(Subsystem sub : children_)
            sub.reset() ; 
    }

    /// \brief reservered for future use
    public void selfTest() {
    }

    /// \brief this method is called during hardware initialization.
    /// It is called after all of the subsystms are constructure and after computeState() has been
    /// called once so that the internal state of each subsystem is valid.
    public void postHWInit() throws Exception {
        for(Subsystem sub : children_)
            sub.postHWInit();
    }

    /// \brief this method computes the current state of the subsystem.
    /// This method is called when the robot is disabled, in auto mode, or in teleop mode.  This
    /// method calls subsystem specific computeMyState() which should be implemented by any
    /// derived class.  This specific implementation keeps track of execution time of the
    /// subsystem state computations as this is the most CPU intensive operation of the robot.
    /// Note, this method also catches all exceptions from the computeMyState() method keeping
    /// the exception from propogating up and crashing the robot code.
    ///
    public void computeState() {
        try {
            double start = getRobot().getTime() ;
            computeMyState() ;
            double elapsed = getRobot().getTime() - start ;
            total_time_ += elapsed ;
            total_cnt_++ ;

            min_time_ = Math.min(min_time_, elapsed) ;
            max_time_ = Math.max(max_time_, elapsed) ;

            if (total_cnt_ > 0 && (total_cnt_ % 500) == 0) {
                MessageLogger logger = getRobot().getMessageLogger() ;
                logger.startMessage(MessageType.Debug, getRobot().getLoggerID()) ;
                logger.add("subsystem ").addQuoted(getName()) ;
                logger.add("count", total_cnt_) ;
                logger.add("min", fmt_.format(min_time_ * 1000)) ;
                logger.add("average", fmt_.format(total_time_ / total_cnt_ * 1000)) ;
                logger.add("max", fmt_.format(max_time_ * 1000)) ;
                logger.endMessage();
            }
        }
        catch(Exception ex) {
            MessageLogger logger = getRobot().getMessageLogger() ;            
            logger.startMessage(MessageType.Error) ;
            logger.add("subsystem ").addQuoted(getName()) ;
            logger.add(" threw exception in computeMyState() - ").add(ex.getMessage()) ;
            logger.endMessage();
        }

        for(Subsystem sub : children_) {
            sub.computeState();
        }
    }

    /// \brief this method is called in robot loop to set any hardware outputs
    /// This method is called in auto mode and teleop mode.  It takes the current state of the
    /// robot combined with the action assigned to the subsystem and determines the outputs
    /// required for actuators.
    ///
    public void run() throws Exception {
        if (action_ != null)
        {
            try {
                if (!action_.isDone()) {
                    action_.run() ;
                    if (action_.isDone()) {
                        if (action_ == default_action_)
                            finished_default_ = true ;
                        action_ = null ;
                    }
                }
            }
            catch(Exception ex) {
                MessageLogger logger = getRobot().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("action ").addQuoted(action_.toString()) ;
                logger.add(" threw exception during run() - ").add(ex.getMessage()) ;
                logger.endMessage();
            }
        }

        //
        // If the current action is done, but there is a default action, and we
        // did not just complete the default action, then start the default action
        //
        if (action_ == null && default_action_ != null && !finished_default_)
        {
            try {
                cancelActionPlusChildren();
                action_ = default_action_ ;                
                action_.start() ;
                if (action_.isDone())
                    finished_default_ = true ;
            }
            catch(Exception ex) {
                MessageLogger logger = getRobot().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("action ").addQuoted(action_.toString()) ;
                logger.add(" threw exception during start() - ").add(ex.getMessage()) ;
                logger.endMessage();
                action_ = null ;
            }
        }

        for(Subsystem sub : children_)
            sub.run();        
    }

    /// \brief set the current action for the subsystem
    /// \param act the action to set
    /// \returns true if the action was accepted, false if not
    public boolean setAction(final Action act) {
        return setAction(act, false) ;
    }

    /// \brief set the current action for the subsystem
    /// \param act the action to set
    /// \param parent_busy_ok ok to assign action if a parent is busy
    /// \returns true if the action was accepted, false if not
    public boolean setAction(final Action act, boolean parent_busy_ok) {
        if (act == default_action_ && act != null) {
            MessageLogger logger = getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("action ").addQuoted(act.toString()) ;
            logger.add(" is being assigned in setAction() but is the default action") ;
            logger.endMessage();

            //
            // Trying to setAction with the same action that is already the
            // default action
            //
            return false ;
        }

        if (!parent_busy_ok && parent_.isAnyParentBusy())
        {
            MessageLogger logger = getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("action ").addQuoted(act.toString()) ;
            logger.add(" cannot be assigned because a parent is busy") ;
            logger.endMessage();            
            
            //
            // If we are busy the new action overrides the current action.  If any
            // parent is busy, we fail unless the parent_busy_ok flag is set.
            //
            return false ;
        }

        finished_default_ = false ;

        //
        // Cancel any current action for this subsystem or any action for any children
        // of this subsystem.
        //
        cancelActionPlusChildren();

        action_ = act ;
        try {
            //
            // Now start the new current action
            //
            if (action_ != null)
                action_.start() ;
        }
        catch(Exception ex) {
            MessageLogger logger = getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("action ").addQuoted(action_.toString()) ;
            logger.add(" threw exception during start() - ").add(ex.getMessage()) ;
            logger.endMessage();
            action_ = null ;
        }

        return true ;
    }

    /// \brief returns the currently assigned action
    /// \returns the currently assigned action
    public Action getAction() {
        return action_;
    }

    /// \brief set the default action for the subsystem
    /// \param act the action to assign to the subsystem
    public void setDefaultAction(final Action act) {
        finished_default_ = false ;

        if (action_ != null && action_ == default_action_)
        {
            //
            // The running action is the default action, since we are 
            // replacing the default action, cancel this current action so that
            // we can start the new default action.
            //
            cancelActionPlusChildren();
            action_ = null ;
        }

        //
        // Store the default action
        //
        default_action_ = act ;
        if (action_ == null)
        {
            //
            // If nothing is running now, start the default action
            //
            try {
                cancelActionPlusChildren();
                action_ = default_action_ ;                
                action_.start() ;
                if (action_.isDone())
                    finished_default_ = true ;
            }
            catch(Exception ex) {
                MessageLogger logger = getRobot().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("action ").addQuoted(action_.toString()) ;
                logger.add(" threw exception during start() - ").add(ex.getMessage()) ;
                logger.endMessage();
            }
        }
    }

    /// \brief return the default action for the subsystem
    /// \returns the default action for the subsystem
    public Action getDefaultAction() {
        return default_action_;
    }

    /// \brief cancel the currently assigned action.
    /// Cancel requires that the action end immediately prior to this call returning
    public void cancelAction() {
        cancelAction(true) ;
    }

    /// \brief put a value on the driver station dashboard
    /// \param name name of the value to display
    /// \param dtype indicates when the value should be displayed
    /// \value the value to display
    public void putDashboard(String name, DisplayType dtype, boolean value) {
        if (shouldDisplay(dtype))
            SmartDashboard.putBoolean(name, value) ;
    }

    /// \brief put a value on the driver station dashboard
    /// \param name name of the value to display
    /// \param dtype indicates when the value should be displayed
    /// \value the value to display
    public void putDashboard(String name, DisplayType dtype, double value) {
        if (shouldDisplay(dtype))
            SmartDashboard.putNumber(name, value) ;
    }

    /// \brief put a value on the driver station dashboard
    /// \param name name of the value to display
    /// \param dtype indicates when the value should be displayed
    /// \value the value to display
    public void putDashboard(String name, DisplayType dtype, int value) {
        if (shouldDisplay(dtype))
            SmartDashboard.putNumber(name, value) ;
    }

    /// \brief put a value on the driver station dashboard
    /// \param name name of the value to display
    /// \param dtype indicates when the value should be displayed
    /// \value the value to display    
    public void putDashboard(String name, DisplayType dtype, String value) {
        if (shouldDisplay(dtype))
            SmartDashboard.putString(name, value) ;        
    }

    
    public boolean isBusy() {
        return action_ != null && !action_.isDone() && action_ != default_action_ ;
    }

    public boolean isBusyOrParentBusy() {
        return isBusy() || parent_.isBusy() ;
    }

    public boolean isAnyParentBusy() {
        return isBusy() || (parent_ != null && parent_.isAnyParentBusy()) ;
    }

    public boolean isBusyOrChildBusy() {
        if (isBusy())
            return true ;

        for(Subsystem child : children_)
        {
            if (child.isBusyOrChildBusy())
                return true ;
        }

        return false ;
    }

    public int initPlot(String name) {
        return getRobot().getPlotManager().initPlot(name) ;
    }

    public void startPlot(int id, String[] cols) {
        getRobot().getPlotManager().startPlot(id, cols) ;
    }

    public void addPlotData(int id, Double[] data) {
        getRobot().getPlotManager().addPlotData(id, data) ;
    }

    public void endPlot(int id) {
        getRobot().getPlotManager().endPlot(id) ;
    }

    /// \brief set the robot object for the subsystem.
    /// \param robot the robot object for the main robot class
    protected void setRobot(XeroRobot robot) {
        robot_ = robot ;
    }

    /// \brief stub version of the computeMyState method.
    /// Should be implemented by a derived class
    protected void computeMyState() throws Exception {        
    }

    private boolean shouldDisplay(DisplayType dtype) {
        boolean ret = false ;

        switch(dtype) {
            case Always:
                ret = true ;
                break ;

            case Verbose:
                if (verbose_ || getRobot().isDisabled())
                    ret = true ;
                break ;

            case Disabled:
                if (getRobot().isDisabled())
                    ret = true ;
                break ;
        }

        return ret ;
    }

    private void cancelAction(boolean start_default) {
        if (action_ == null)
        {
            MessageLogger logger = getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Debug, getLoggerID()) ;
            logger.add("Subsystem cancelAction() called with no active action").endMessage();
            return ;
        }

        action_.cancel() ;
        if (default_action_ != null && action_ != default_action_)
        {
            action_ = default_action_ ;
            try {
                action_.start() ;
                if (action_.isDone())
                    finished_default_ = true ;
            }
            catch(Exception ex) {
                MessageLogger logger = getRobot().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("action ").addQuoted(action_.toString()) ;
                logger.add(" threw exception during start() - ").add(ex.getMessage()) ;
                logger.endMessage();
            }
        }
        else
        {
            action_ = null ;
        }
    }

    private void cancelActionPlusChildren() {
        if (action_ != null && !action_.isDone())
            cancelAction(false);

        for(Subsystem child: children_)
            child.cancelActionPlusChildren();
    }

}