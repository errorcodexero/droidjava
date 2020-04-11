package org.frc2020.droid.automodes;

public class FarSideAuto extends DroidAutoMode {
    public FarSideAuto(DroidAutoController ctrl) throws Exception {
        super(ctrl, "FarSide") ;

        setInitialBallCount(3);

        //
        // Collect the extra two balls
        //
        driveAndCollect("five_ball_auto_collect") ;

        //
        // Drive near the center to fire
        //
        driveAndFire("five_ball_auto_fire", true, 0.0) ;

    }
}