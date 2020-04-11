package org.frc2020.droid.gamepiecemanipulator.conveyor;

import java.util.List;

public class GoToState extends BaseState {

    public GoToState(String name) {
        branch_name_ = name ;
    }

    public GoToState(String label, String name) {
        super(label) ;
        branch_name_ = name ;
    }    

    @Override
    public void addBranchTargets(List<String> targets) {
        if (!targets.contains(branch_name_))
            targets.add(branch_name_) ;
    }    

    @Override
    public ConveyorStateStatus runState(ConveyorStateAction act) {
        return new ConveyorStateStatus(branch_name_) ;
    }

    @Override
    public String humanReadableName() {
        return "GoToState " + branch_name_ ;
    }   

    private String branch_name_ ;
}