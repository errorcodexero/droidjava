package org.frc2020.droid.automodes;

public class NearSideSixAuto extends DroidAutoMode {
    public NearSideSixAuto(DroidAutoController ctrl) throws Exception {
        super(ctrl, "NearSideSix") ;

        setInitialBallCount(3);

        //
        // The first drive and fire
        //
        driveAndFire("six_ball_auto_fire", false, 20.0) ;

        //
        // Now, collect new balls
        //
        driveAndCollect("six_ball_auto_collect") ;

        //
        // Now fire the new set of balls
        //
        driveAndFire("six_ball_auto_fire2", true, 0.0) ;
    }
}