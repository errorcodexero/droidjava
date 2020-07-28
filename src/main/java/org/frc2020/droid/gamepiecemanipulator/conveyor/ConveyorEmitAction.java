package org.frc2020.droid.gamepiecemanipulator.conveyor;

import org.frc2020.droid.gamepiecemanipulator.conveyor.WaitForSensor.SensorEvent;

public class ConveyorEmitAction extends ConveyorStateAction {
    private static final String LoopLabel = "loop" ;
    private static final String DoneLabel = "done" ;
    private static final String NotFiringLastBallLabel = "lastball" ;

    public ConveyorEmitAction(ConveyorSubsystem sub) throws Exception {
        super(sub) ;

        emit_intake_power_ = sub.getRobot().getSettingsParser().get("conveyor:emit:intake_side_power").getDouble() ;
        emit_shooter_power_ = sub.getRobot().getSettingsParser().get("conveyor:emit:shooter_side_power").getDouble() ;
        should_stop_firing_ = false ;

        BaseState[] states = new BaseState[] { 
            new BranchState(LoopLabel, DoneLabel, (ConveyorStateAction act) -> {
                return act.getSubsystem().isEmpty(); }),

            new AssertState(
                "ConveyorEmitAction called with no ball in position; was ConveyorPrepareToEmitAction run?",
                (ConveyorStateAction act) -> {
                    return act.getSubsystem().isStagedForFire();}),

            new DoWorkState("motors to emit power", (ConveyorStateAction act) -> {
                act.getSubsystem().setMotorsPower(emit_intake_power_, emit_shooter_power_) ; 
                return ConveyorStateStatus.NextState ; }),
            
            new DoWorkState("set conveyor state", (ConveyorStateAction act) -> {
                act.getSubsystem().setStagedForCollect(false);
                act.getSubsystem().setCollecting(false);
                return ConveyorStateStatus.NextState ;} ),            

            new WaitForSensor(ConveyorSubsystem.Sensor.D, SensorEvent.IS_LOW),

            new BranchState(NotFiringLastBallLabel, (ConveyorStateAction act) -> {
                return act.getSubsystem().getBallCount() != 1 ;}),

            new DelayState(0.5),

            new DoWorkState("decrement ball count", (ConveyorStateAction act) -> {
                act.getSubsystem().decrementBallCount();
                act.getSubsystem().setStagedForFire(false);
                return ConveyorStateStatus.NextState;
            }),

            new GoToState(DoneLabel),

            new WaitForSensor(NotFiringLastBallLabel, ConveyorSubsystem.Sensor.D, SensorEvent.IS_HIGH),

            new WaitForSensor(ConveyorSubsystem.Sensor.D, SensorEvent.IS_LOW),

            new DoWorkState("decrement ball count", (ConveyorStateAction act) -> {
                act.getSubsystem().decrementBallCount();
                return ConveyorStateStatus.NextState;
            }),

            new BranchState(DoneLabel, (ConveyorStateAction act) -> {
                return should_stop_firing_; }),

            new GoToState(LoopLabel),

            new DoWorkState(DoneLabel, "set motor power zero", (ConveyorStateAction act) -> {
                act.getSubsystem().setMotorsPower(0.0, 0.0) ;
                return ConveyorStateStatus.NextState;
            }),
        } ;

        setStates(states);
    }

    public void stopFiring() {
        should_stop_firing_ = true ;
    }

    @Override
    protected void conveyorActionStarted() {
        should_stop_firing_ = false ;
    }

    @Override
    protected void conveyorActionRunning() {
    }

    @Override
    protected void conveyorActionFinished() {
    }

    public String toString(int indent) {
        return prefix(indent) + "ConveyorEmitAction" ;
    }

    private double emit_intake_power_ ;
    private double emit_shooter_power_ ;
    private boolean should_stop_firing_ ;
}