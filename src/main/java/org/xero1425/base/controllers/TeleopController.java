package org.xero1425.base.controllers;

import org.xero1425.base.XeroRobot;
import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.actions.SequenceAction;
import org.xero1425.base.oi.OISubsystem;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;

public class TeleopController extends BaseController
{
    public TeleopController(XeroRobot robot, String name) {
        super(robot, name) ;

        sequence_ = new SequenceAction(robot.getMessageLogger());
    }

    @Override
    public void init() {
    }

    @Override 
    public void run() {
        OISubsystem oi = getRobot().getRobotSubsystem().getOI() ;

        sequence_.clear() ;
        try {
            oi.generateActions(sequence_) ;
        }
        catch(InvalidActionRequest ex) {
            MessageLogger logger = getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("Error generating actions in teleop - ") ;
            logger.add(ex.getMessage()) ;
            logger.endMessage(); 
        }

        if (sequence_.getChildren().size() > 0)
        {
            try {
                sequence_.start() ;
            }
            catch(Exception ex) {
                MessageLogger logger = getRobot().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("Error starting actions in teleop sequence - ") ;
                logger.add(ex.getMessage()) ;
                logger.endMessage(); 
            }

            try {
                sequence_.run() ;
            }
            catch(Exception ex) {
                MessageLogger logger = getRobot().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("Error running actions in teleop sequence - ") ;
                logger.add(ex.getMessage()) ;
                logger.endMessage(); 
            }
        }
    }

    private SequenceAction sequence_ ;
} ;
