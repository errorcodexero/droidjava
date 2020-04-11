package org.frc2020.droid.gamepiecemanipulator.intake ;

import org.xero1425.base.motorsubsystem.MotorEncoderGotoAction;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class CollectOnAction extends MotorEncoderGotoAction {
    public CollectOnAction(IntakeSubsystem sub) throws BadParameterTypeException, MissingParameterException {
        super(sub, "intake:arm:collecton:pos", true);

        sub_ = sub;
        collect_power_ = sub.getRobot().getSettingsParser().get("intake:collector:motor:power").getDouble();
    }

    @Override
    public void start() throws Exception {
        super.start();

        sub_.setCollectorPower(collect_power_);
    }

    @Override
    public void run() throws Exception {
        super.run();
    }

    @Override
    public void cancel() {
        super.cancel() ;
        try {
            sub_.setCollectorPower(0.0);
        }
        catch(Exception ex) {            
        }
    }

    IntakeSubsystem sub_ ;
    private double collect_power_ ;
}