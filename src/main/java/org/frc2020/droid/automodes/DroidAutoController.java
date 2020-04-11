package org.frc2020.droid.automodes;

import java.util.ArrayList;
import java.util.List;

import org.frc2020.droid.Droid;
import org.xero1425.base.controllers.AutoController;
import org.xero1425.base.controllers.AutoMode;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;

public class DroidAutoController extends AutoController {
    public DroidAutoController(Droid robot) throws MissingParameterException, BadParameterTypeException {
        super(robot, "droid-auto");

        MessageLogger logger = getRobot().getMessageLogger() ;

        try {
            near_side_eight_ = new NearSideEightAuto(this);
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'NearSideEightAuto', exception caught") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }

        try {
            near_side_six_ = new NearSideSixAuto(this) ;
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'NearSideSixAuto', exception caught") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }
        
        try {
            near_side_ten_ = new NearSideTenAuto(this) ;
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'NearSideTenAuto', exception caught") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }
        
        try {
            middle_three_  = new MiddleAuto(this) ;
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'MiddleAuto', exception caught") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }
        
        try {
            far_side_five_ = new FarSideAuto(this) ;
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'FarSideAuto', exception caught") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }        
    }

    public List<AutoMode> getAllAutomodes() {
        List<AutoMode> modes = new ArrayList<AutoMode>() ;
        modes.add(near_side_eight_) ;
        modes.add(near_side_six_) ;
        modes.add(near_side_ten_) ;
        modes.add(middle_three_) ;
        modes.add(far_side_five_) ;
        return modes ;
    }

    public void updateAutoMode(int mode, String gamedata) {

        try {
            if (isTestMode()) {
                setAutoMode(new DroidTestAutoMode(this));
            }
            else {
                switch(mode) {
                    case 0:
                        setAutoMode(near_side_eight_) ;
                        break ;

                    case 1:
                        setAutoMode(near_side_six_) ;
                        break ;

                    case 2:
                        setAutoMode(near_side_ten_) ;
                        break ;                        

                    case 3:
                        setAutoMode(middle_three_) ;
                        break ;

                    case 4:
                        setAutoMode(far_side_five_) ;
                        break ;
                }
            }
        }
        catch(Exception ex) {
            setAutoMode(null);
        }
    }

    private AutoMode near_side_eight_ ;
    private AutoMode near_side_six_ ;
    private AutoMode near_side_ten_ ;
    private AutoMode middle_three_ ;
    private AutoMode far_side_five_ ;      
}
