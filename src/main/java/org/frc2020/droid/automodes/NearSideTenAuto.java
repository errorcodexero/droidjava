package org.frc2020.droid.automodes;

public class NearSideTenAuto extends DroidAutoMode {
    public NearSideTenAuto(DroidAutoController ctrl) throws Exception {
        super(ctrl, "NearSideTen") ;

        setInitialBallCount(3);

        // Collect two from the middle zone
        driveAndCollect("ten_ball_auto_collect1") ;

        // Fire the initial five
        driveAndFire("ten_ball_auto_fire1", true, 20.0) ;

        // Collect five more down the trench
        driveAndCollect("ten_ball_auto_collect2") ;     
        
        // Fire the next five
        driveAndFire("ten_ball_auto_fire2", true, 20.0) ;        
    }
}