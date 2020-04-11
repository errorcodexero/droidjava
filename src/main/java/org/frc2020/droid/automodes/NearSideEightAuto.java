package org.frc2020.droid.automodes;

public class NearSideEightAuto extends DroidAutoMode {
    public NearSideEightAuto(DroidAutoController ctrl) throws Exception {
        super(ctrl, "NearSideEight") ;


        setInitialBallCount(3);

        //
        // The first drive and fire
        //
        driveAndFire("eight_ball_auto_fire", false, 20.0) ;

        //
        // Now, collect new balls
        //
        driveAndCollect("eight_ball_auto_collect") ;

        //
        // Now fire the new set of balls
        //
        driveAndFire("eight_ball_auto_fire2", true, 0.0) ;
    }
}