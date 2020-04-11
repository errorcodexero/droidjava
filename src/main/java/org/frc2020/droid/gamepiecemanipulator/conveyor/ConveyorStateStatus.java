package org.frc2020.droid.gamepiecemanipulator.conveyor ;

public class ConveyorStateStatus {
    public enum StateStatusType {
        CurrentState,
        NextState,
        Branch,
        ActionDone
    } ;

    public ConveyorStateStatus(StateStatusType type) {
        type_ = type ;
    }

    public ConveyorStateStatus(String name) {
        type_ = StateStatusType.Branch ;
        name_ = name ;
    }

    public StateStatusType getType() {
        return type_ ;
    }

    public String getStateName() {
        return name_ ;
    }


    public static final ConveyorStateStatus CurrentState = new ConveyorStateStatus(StateStatusType.CurrentState) ;
    public static final ConveyorStateStatus NextState = new ConveyorStateStatus(StateStatusType.NextState) ;
    public static final ConveyorStateStatus ActionDone = new ConveyorStateStatus(StateStatusType.ActionDone) ;    

    private StateStatusType type_ ;
    private String name_ ;
} ;