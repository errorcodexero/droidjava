package org.xero1425.base.motors ;

import java.util.Map ;

import java.util.HashMap ;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType ;
import org.xero1425.misc.SettingsParser;
import org.xero1425.misc.SettingsValue;
import org.xero1425.misc.SettingsValue.SettingsType;

public class MotorFactory
{
    public MotorFactory(MessageLogger logger, SettingsParser settings) {
        logger_ = logger ;
        settings_ = settings;
        motors_ = new HashMap<Integer, MotorController>() ;
    }

    private void errorMessage(String id, String msg) {
        logger_.startMessage(MessageType.Error) ;
        logger_.add("error creating motor '") ;
        logger_.add(id) ;
        logger_.add("' - ").add(msg) ;
        logger_.endMessage();
    }

    public MotorController createMotor(String name, String id) {
        MotorController ret = null ;

        try {
            ret = createSingleMotor(name, id) ;
            if (ret != null)
                return ret ;

            MotorController.NeutralMode groupmode = getNeutralMode(id) ;
            boolean groupinverted = isInverted(id) ;
            boolean leaderinverted = false ;
            int currentIndex = 1 ;
            MotorGroupController group = new MotorGroupController(name) ;
            ret = group ;

            while (true)
            {
                String motorid = id + ":" + Integer.toString(currentIndex) ;
                MotorController single = createSingleMotor(name + ":" + Integer.toString(currentIndex), motorid) ;
                if (single != null)
                {
                    boolean v = single.isInverted() ;

                    if (currentIndex == 1) {
                        leaderinverted = v ;
                        if (groupinverted)
                            v = !v ;
                        single.setInverted(v);
                    } else if (leaderinverted) {
                        v = !v ;
                    }

                    if (groupmode != null)
                    {
                        single.setNeutralMode(groupmode);
                    }

                    group.addMotor(single, v) ;
                    
                    currentIndex++ ;
                }
                else
                {
                    if (currentIndex == 1)
                    {
                        errorMessage(id, "no motors found that match this id") ;
                        return null ;
                    }
                    break ;
                }
            }

        }
        catch(Exception ex) {
            ret = null ;
        }

        return ret ;
    }

    private MotorController createSingleMotor(String name, String id) throws BadParameterTypeException, BadMotorRequestException {
        String idparam = id + ":type" ;
        String canparam = id + ":canid" ;

        boolean hasid = settings_.isDefined(canparam) && settings_.getOrNull(canparam).isInteger() ;
        boolean hastype = settings_.isDefined(idparam) && settings_.getOrNull(idparam).isString() ;

        if (hastype && !hasid)
        {
            errorMessage(id, "missing motor id, cannot create motor") ;
            return null ;
        }

        if (hasid && !hastype) 
        {
            errorMessage(id, "missing motor type, cannot create motor") ;
            return null ;            
        }

        if (!hasid || !hastype)
            return null ;

        int canid = settings_.getOrNull(canparam).getInteger() ;
        if (motors_.containsKey(canid))
        {
            MotorController dup = motors_.get(canid) ;
            errorMessage(id, "cannot create motor, can id is already in use '" + dup.getName() + "'") ;
            return null ;
        }

        String type = settings_.getOrNull(idparam).getString() ;
        MotorController ctrl = null ;

        if (type.equals("talon_srx"))
        {
            ctrl = new CTREMotorController(type, canid, CTREMotorController.MotorType.TalonSRX) ;
        }
        else if (type.equals("talon_fx"))
        {
            ctrl = new CTREMotorController(type, canid, CTREMotorController.MotorType.TalonFX) ;
        }
        else if (type.equals("victor_spx"))
        {
            ctrl = new CTREMotorController(type, canid, CTREMotorController.MotorType.VictorSPX) ;
        }
        else if (type.equals("sparkmax_brushless"))
        {
            ctrl = new SparkMaxMotorController(type, canid, true) ;
        }
        else if (type.equals("sparmmax_brushed"))
        {
            ctrl = new SparkMaxMotorController(type, canid, false) ;
        }
        else
        {
            errorMessage(id, "motor type '" + type + "' is not a valid motor type") ;
            return null ;
        }

        ctrl.setInverted(isInverted(id)) ;
        MotorController.NeutralMode nm = getNeutralMode(id) ;
        if (nm != null)
            ctrl.setNeutralMode(nm) ;

        return ctrl ;
    }

    private MotorController.NeutralMode getNeutralMode(String id) throws BadParameterTypeException {
        SettingsValue v ;
        String pname = id + ":neutral_mode" ;
        
        v = settings_.getOrNull(pname) ;
        if (v == null)
            return null ;

        if (!v.isString()) {
            logger_.startMessage(MessageType.Error).add("parameter '").add(pname).add("'") ;
            logger_.add(" - does not have boolean type") ;
            throw new BadParameterTypeException(SettingsType.String, v.getType()) ;
        }

        MotorController.NeutralMode mode ;
        if (v.getString().equals("brake"))
        {
            mode = MotorController.NeutralMode.Brake ;
        }
        else if (v.getString().equals("coast"))
        {
            mode = MotorController.NeutralMode.Coast ;
        }
        else 
        {
            logger_.startMessage(MessageType.Warning).add("parameter '").add(pname).add("'") ;
            logger_.add(" - is string but is not 'brake' or 'coast'") ;
            mode = null ;
        }

        return mode ;
    }

    private boolean isInverted(String id) throws BadParameterTypeException {
        SettingsValue v ;
        String pname = id + ":inverted" ;
        
        v = settings_.getOrNull(pname) ;
        if (v == null)
            return false ;

        if (!v.isBoolean()) {
            logger_.startMessage(MessageType.Error).add("parameter '").add(pname).add("'") ;
            logger_.add(" - does not have boolean type") ;
            throw new BadParameterTypeException(SettingsType.Boolean, v.getType()) ;
        }

        return v.getBoolean() ;
    }

    private MessageLogger logger_ ;
    private SettingsParser settings_ ;
    private Map<Integer, MotorController> motors_ ;
} ;
