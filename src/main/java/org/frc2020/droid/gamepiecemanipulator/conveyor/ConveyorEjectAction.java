package org.frc2020.droid.gamepiecemanipulator.conveyor;

public class ConveyorEjectAction extends ConveyorStateAction {
    public ConveyorEjectAction(ConveyorSubsystem sub) throws Exception {
        super(sub) ;

        eject_intake_power_ = sub.getRobot().getSettingsParser().get("conveyor:eject:intake_side_power").getDouble() ;
        eject_shooter_power_ = sub.getRobot().getSettingsParser().get("conveyor:eject:shooter_side_power").getDouble() ;

        BaseState[] states = new BaseState[] { 
            new DoWorkState("motors to eject power", (ConveyorStateAction act) -> {
                act.getSubsystem().setMotorsPower(eject_intake_power_, eject_shooter_power_);
                return ConveyorStateStatus.NextState ;} ),

            new DoWorkState("reset conveyor state", (ConveyorStateAction act) -> {
                act.getSubsystem().setStagedForFire(false);
                act.getSubsystem().setStagedForCollect(false);
                act.getSubsystem().setCollecting(false);
                act.getSubsystem().setBallCount(0) ;
                return ConveyorStateStatus.NextState ;} ),

            new DelayState(3.0),

            new DoWorkState("motors to zero power", (ConveyorStateAction act) -> {
                act.getSubsystem().setMotorsPower(0.0, 0.0);
                return ConveyorStateStatus.ActionDone ;} ),            
        } ;

        setStates(states);
    }

    @Override
    protected void conveyorActionStarted() {
    }

    @Override
    protected void conveyorActionRunning() {
    }

    @Override
    protected void conveyorActionFinished() {
    }

    public String toString(int indent) {
        return prefix(indent) + "ConveyorEjectAction" ;
    }

    private double eject_intake_power_ ;
    private double eject_shooter_power_ ;
}