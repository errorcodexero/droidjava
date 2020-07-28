package org.frc2020.droid.gamepiecemanipulator.conveyor;

import org.frc2020.droid.gamepiecemanipulator.conveyor.WaitForSensor.SensorEvent;

public class ConveyorPrepareToReceiveAction extends ConveyorStateAction {
    private static final String DoneLabel = "done" ;
    private static final String SetStagedLabel = "setstaged" ;

    public ConveyorPrepareToReceiveAction(ConveyorSubsystem sub) throws Exception {
        super(sub) ;

        prepare_receive_intake_to_intake_power_ = sub.getRobot().getSettingsParser().get("conveyor:prepare_receive:to_intake:intake_side_power").getDouble() ;
        prepare_receive_shooter_to_intake_power_ = sub.getRobot().getSettingsParser().get("conveyor:prepare_receive:to_intake:shooter_side_power").getDouble() ;
        prepare_receive_intake_to_shooter_power_ = sub.getRobot().getSettingsParser().get("conveyor:prepare_receive:to_shooter:intake_side_power").getDouble() ;
        prepare_receive_shooter_to_shooter_power_ = sub.getRobot().getSettingsParser().get("conveyor:prepare_receive:to_shooter:shooter_side_power").getDouble() ;        

        BaseState[] states = new BaseState[] {
            new BranchState(DoneLabel, (ConveyorStateAction act) -> {
                return act.getSubsystem().isFull(); }),

            new BranchState(SetStagedLabel, (ConveyorStateAction act) -> {
                return act.getSubsystem().isEmpty(); }),

                new BranchState(SetStagedLabel, (ConveyorStateAction act) -> {
                    return act.getSubsystem().isStagedForCollect(); }),                

            new DoWorkState("set motors prepare receive power forward", (ConveyorStateAction act) -> {
                act.getSubsystem().setMotorsPower(prepare_receive_intake_to_intake_power_, prepare_receive_shooter_to_intake_power_) ;
                return ConveyorStateStatus.NextState; }),

            new WaitForSensor(ConveyorSubsystem.Sensor.A, SensorEvent.IS_HIGH),

            new DoWorkState("set motor prepare receive power reverse", (ConveyorStateAction act) -> {
                act.getSubsystem().setMotorsPower(prepare_receive_intake_to_shooter_power_, prepare_receive_shooter_to_shooter_power_) ;
                return ConveyorStateStatus.NextState; }),  
                
            new WaitForSensor(ConveyorSubsystem.Sensor.A, SensorEvent.IS_LOW),

            new WaitForSensor(ConveyorSubsystem.Sensor.B, SensorEvent.IS_HIGH),            

            new DoWorkState(SetStagedLabel, "set conveyor state staged", (ConveyorStateAction act) -> {
                act.getSubsystem().setStagedForCollect(true);
                act.getSubsystem().setStagedForFire(false);
                act.getSubsystem().setCollecting(false);
                return ConveyorStateStatus.NextState; }),            
                
            new DoWorkState(DoneLabel, "set motor power zero", (ConveyorStateAction act) -> {
                act.getSubsystem().setMotorsPower(0.0, 0.0) ;
                return ConveyorStateStatus.NextState; }),                
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
        return prefix(indent) + "ConveyorPrepareToReceive" ;
    }

    private double prepare_receive_intake_to_intake_power_ ;
    private double prepare_receive_shooter_to_intake_power_ ;
    private double prepare_receive_intake_to_shooter_power_ ;
    private double prepare_receive_shooter_to_shooter_power_ ;    
}