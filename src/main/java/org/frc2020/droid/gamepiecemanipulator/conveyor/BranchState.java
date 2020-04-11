package org.frc2020.droid.gamepiecemanipulator.conveyor ;

import java.util.List;

public class BranchState extends BaseState {

    @FunctionalInterface
    interface ConditionalFunction
    {
        boolean evaluate(ConveyorStateAction act) ;
    }

    public BranchState(String name, ConditionalFunction func) {
        branch_name_ = name ;
        cond_ = func ;
    }

    public BranchState(String label, String name, ConditionalFunction func) {
        super(label) ;
        branch_name_ = name ;
        cond_ = func ;
    }    

    @Override
    public void addBranchTargets(List<String> targets) {
        if (!targets.contains(branch_name_))
            targets.add(branch_name_) ;
    }

    @Override
    public ConveyorStateStatus runState(ConveyorStateAction act) {
        if (cond_.evaluate(act))
            return new ConveyorStateStatus(branch_name_) ;
        
        return ConveyorStateStatus.NextState ;
    }

    @Override
    public String humanReadableName() {
        return "BranchState " + branch_name_ ;
    }    

    private ConditionalFunction cond_ ;
    private String branch_name_ ;
}