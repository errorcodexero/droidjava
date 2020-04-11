package org.xero1425.base.controllers;

import java.util.List;

import edu.wpi.first.wpilibj.DriverStation;
import org.xero1425.base.XeroRobot;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.SettingsParser;
import org.xero1425.misc.SettingsValue;

public abstract class AutoController extends BaseController {
    private static final String testmode = "auto:testmode:active";

    public AutoController(XeroRobot robot, String name) throws MissingParameterException, BadParameterTypeException {
        super(robot, name) ;

        SettingsParser settings = robot.getSettingsParser() ;
        if (settings.isDefined(testmode)) {
            SettingsValue v = settings.get(testmode) ;
            if (v.isBoolean() && v.getBoolean()) {
                test_mode_ = true ;
            }
        }
    }

    public String getAutomodeName() {
        if (current_automode_ == null)
            return "NONE" ;
        
        return current_automode_.getName() ;
    }

    @Override
    public void init() {
    }

    public abstract List<AutoMode> getAllAutomodes() ;

    @Override
    public void run() {
        if (current_automode_ != null) {
            if (!started_) {
                try {
                    current_automode_.start() ;
                    started_ = true ;
                }
                catch(Exception ex) {
                    MessageLogger logger = getRobot().getMessageLogger();
                    logger.startMessage(MessageType.Error) ;
                    logger.add("expection thrown in start() method of automode - ").add(ex.getMessage()) ;
                    logger.endMessage();
                }
            }
            try {
                current_automode_.run() ;
            }
            catch(Exception ex) {
                MessageLogger logger = getRobot().getMessageLogger();
                logger.startMessage(MessageType.Error) ;
                logger.add("expection thrown in run() method of automode - ").add(ex.getMessage()) ;
                logger.endMessage();            
            }
        }
    }

    public boolean isTestMode() {
        DriverStation ds = DriverStation.getInstance() ;

        if (ds.isFMSAttached())
            return false ;

        return test_mode_ ;
    }

    public void updateAutoMode(int mode, String gamedata) {
        setAutoMode(null) ;
    }

    public String getAutoModeName() {
        if (current_automode_ == null)
            return "NONE" ;

        return current_automode_.getName() ;
    }

    protected void setAutoMode(AutoMode m) {
        current_automode_ = m ;
        started_ = false ;
    }

    private AutoMode current_automode_ ;
    private boolean test_mode_ ;
    private boolean started_ ;
} ;