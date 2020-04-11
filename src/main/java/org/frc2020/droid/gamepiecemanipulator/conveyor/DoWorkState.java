package org.frc2020.droid.gamepiecemanipulator.conveyor;

import java.lang.FunctionalInterface;
import java.util.List;

public class DoWorkState extends BaseState {

    @FunctionalInterface
    interface PerformFunction
    {
        ConveyorStateStatus stateAction(ConveyorStateAction act) ;
    }

    public DoWorkState(String label, PerformFunction function) {
        function_ = function ;
        label_ = label ;
    }

    public DoWorkState(String name, String label, PerformFunction function) {
        super(name) ;
        function_ = function ;
        label_ = label ;
    }
    
    @Override
    public void addBranchTargets(List<String> targets) {
    }

    @Override
	public ConveyorStateStatus runState(ConveyorStateAction act) {
        return function_.stateAction(act) ;
    }

    @Override
    public String humanReadableName() {
        return "DoWork " + label_ ;
    }       

    private String label_ ;
    private PerformFunction function_ ;
}