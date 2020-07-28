package org.frc2020.droid.gamepiecemanipulator.conveyor;

import java.util.List ;
import java.util.ArrayList ;
import org.xero1425.base.actions.Action;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;

public abstract class ConveyorStateAction extends Action {
    public ConveyorStateAction(ConveyorSubsystem sub) {
        super(sub.getRobot().getMessageLogger()) ;

        sub_ = sub ;
    }

    public int getCurrentStateIndex() {
        return current_state_ ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;
        current_state_ = 0 ;
        conveyorActionStarted() ;
    }

    @Override
    public void run() {
        runStateMachine() ;
        conveyorActionRunning() ;

        if (isDone())
            conveyorActionFinished();
    }

    @Override
    public void cancel() {
        super.cancel() ;
        sub_.setMotorsPower(0.0, 0.0) ;
    }

    private void runStateMachine() {
        MessageLogger logger = getSubsystem().getRobot().getMessageLogger();
        ConveyorStateStatus status = null ;
        boolean running = true ;

        while (running) {    
            BaseState state = states_[current_state_] ;               
            try {
                status = state.runState(this) ;
            }
            catch(Exception ex) {
                logger.startMessage(MessageType.Error, getSubsystem().getLoggerID()) ;
                logger.add("conveyor state threw exception - ").add(ex.getMessage()) ;
                logger.endMessage();
                status = ConveyorStateStatus.ActionDone ;
            }

            BaseState prev = state ;
            int prev_state = current_state_ ;

            switch(status.getType()) {
                case CurrentState:
                    running = false ;
                    break ;
                case NextState:
                    current_state_++ ;
                    if (current_state_ == states_.length)
                    {
                        logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID()) ;
                        logger.add("marking action done because state machine finished last state") ;
                        logger.endMessage();
                        setDone() ;
                        running = false ;
                    }
                    break ;
                case ActionDone:
                    setDone() ;
                    running = false ;
                    break ;
                case Branch:
                    current_state_ = getStateIndexByName(status.getStateName()) ;
                    if (current_state_ == -1) {
                        logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID()) ;
                        logger.add("branch target ").addQuoted(status.getStateName()) ;
                        logger.add(" not found - marking action as done") ;
                        logger.endMessage();
                        setDone() ;
                    }
                    break ;
            }

            if (current_state_ == -1 || current_state_ == states_.length || prev != states_[current_state_]) {
                logger.startMessage(MessageType.Debug, getSubsystem().getLoggerID()) ;
                logger.add("conveyor state change: ") ;
                logger.add("(").add(toString(0)).add(") ") ;
                logger.addQuoted(prev_state + ": " + prev.humanReadableName()) ;
                logger.add(" -- > ") ;
                if (current_state_ == -1 || current_state_ == states_.length)
                    logger.addQuoted(current_state_ + ": DONE") ;
                else
                    logger.addQuoted(current_state_ + ": " + states_[current_state_].humanReadableName()) ;

                if (isDone())
                    logger.add("  ACTION DONE") ;

                logger.endMessage();
            }
        }
    }

    private int getStateIndexByName(String name) {
        for(int i = 0 ; i < states_.length ; i++) {
            if (states_[i].getName() == name)
                return i ;
        }

        return -1 ;
    }

    protected void setStates(BaseState[] states) throws Exception {
        List<String> targets = new ArrayList<String>() ;

        states_ = states ;

        // Find all branch targets needed
        for(int i = 0 ; i < states_.length ; i++)
            states_[i].addBranchTargets(targets);

        // Now, see which ones exist
        for(int i = 0 ; i < states_.length ; i++) {
            String target = states_[i].getName() ;
            if (target != null && !target.isEmpty())
                targets.remove(target) ;
        }

        // Now, any left in the targets list are missing targets
        if (targets.size() > 0) {
            MessageLogger logger = getSubsystem().getRobot().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("action ").addQuoted(toString()).add(" is missing branch targets:") ;

            boolean first = true ;
            for(String target : targets) {
                if (!first) {
                    logger.add(",") ;
                }
                else {
                    first = false ;
                }
                logger.addQuoted(target) ;
            }
            logger.endMessage() ;
            throw new Exception("invalid ConveyorStateAction - missing target states, check log file") ;
        }
    }

    protected ConveyorSubsystem getSubsystem() {
        return sub_ ;
    }

    protected abstract void conveyorActionStarted() ;
    protected abstract void conveyorActionRunning()  ;
    protected abstract void conveyorActionFinished() ;

    private ConveyorSubsystem sub_ ;
    private int current_state_ ;
    private BaseState[] states_ ;
}
