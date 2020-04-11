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

public class Subsystem {
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

    public SettingsValue getProperty(String name_) {
        return null ;
    }

    public Subsystem getSubsystemByName(String name) {
        if (name_.equals(name))
            return this ;

        return getChildByName(name) ;
    }

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

    public String getName() {
        return name_;
    }

    public Subsystem getParent() {
        return parent_;
    }

    public XeroRobot getRobot() {
        return robot_;
    }

    public int getLoggerID() {
        return logger_id_ ;
    }

    public void addChild(final Subsystem sub) throws Exception {
        children_.add(sub);
    }

    public void init(LoopType ltype) {
        for(Subsystem sub : children_)
            sub.init(ltype);        
    }

    public void reset() {
        for(Subsystem sub : children_)
            sub.reset() ; 
    }

    public void selfTest() {
    }

    public void postHWInit() throws Exception {
        for(Subsystem sub : children_)
            sub.postHWInit();
    }
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

    public boolean setAction(final Action act) {
        return setAction(act, false) ;
    }

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

    public Action getAction() {
        return action_;
    }

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

    public Action getDefaultAction() {
        return default_action_;
    }

    public void cancelAction() {
        cancelAction(true) ;
    }

    public enum DisplayType {
        Always,                 // Always display this value
        Verbose,                 // Display this value when running
        Disabled,               // Display only when the robot is disabled
    } ;

    public void putDashboard(String name, DisplayType dtype, boolean value) {
        if (shouldDisplay(dtype))
            SmartDashboard.putBoolean(name, value) ;
    }

    public void putDashboard(String name, DisplayType dtype, double value) {
        if (shouldDisplay(dtype))
            SmartDashboard.putNumber(name, value) ;
    }

    public void putDashboard(String name, DisplayType dtype, int value) {
        if (shouldDisplay(dtype))
            SmartDashboard.putNumber(name, value) ;
    }

    public void putDashboard(String name, DisplayType dtype, String value) {
        if (shouldDisplay(dtype))
            SmartDashboard.putString(name, value) ;        
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

    protected void setRobot(XeroRobot robot) {
        robot_ = robot ;
    }

    protected void computeMyState() throws Exception {        
    }

    private void cancelActionPlusChildren() {
        if (action_ != null && !action_.isDone())
            cancelAction(false);

        for(Subsystem child: children_)
            child.cancelActionPlusChildren();
    }

    private Action action_;
    private Action default_action_;
    private final Subsystem parent_;
    private final String name_;
    private final List<Subsystem> children_;
    private XeroRobot robot_;
    private final int logger_id_ ;
    private boolean finished_default_ ;
    private double total_time_ ;
    private int total_cnt_ ;
    private double max_time_ ;
    private double min_time_ ;
    private DecimalFormat fmt_ ;
    private boolean verbose_ ;
}