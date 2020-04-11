package org.frc2020.droid.gamepiecemanipulator.conveyor;

import java.util.List;

import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;

public class AssertState extends BaseState {

    @FunctionalInterface
    interface ConditionalFunction
    {
        boolean evaluate(ConveyorStateAction act) ;
    }

    public AssertState(String msg, ConditionalFunction func) {
        message_ = msg ;
        cond_ = func ;
    }

    public AssertState(String label, String msg, ConditionalFunction func) {
        super(label) ;
        message_ = msg ;
        cond_ = func ;
    }    

    @Override
    public void addBranchTargets(List<String> targets) {
    }

    @Override
    public ConveyorStateStatus runState(ConveyorStateAction act) {
        if (!cond_.evaluate(act)) {
            MessageLogger logger = act.getMessageLogger() ;
            logger.startMessage(MessageType.Debug, act.getSubsystem().getLoggerID()) ;
            logger.add("conveyor: ");
            logger.add(message_) ;
            logger.endMessage();
        }
        return ConveyorStateStatus.NextState ;
    }

    @Override
    public String humanReadableName() {
        return "AssertState - " + message_ ;
    }

    private ConditionalFunction cond_ ;
    private String message_ ;
}