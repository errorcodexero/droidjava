package org.xero1425.base.oi ;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController ;
import edu.wpi.first.wpilibj.DriverStation ;

public abstract class Gamepad extends HIDDevice
{
    public Gamepad(OISubsystem oi, int index) {
        super(oi, index) ;

        controller_ = new XboxController(index) ;
        rumbling_ = false ;
    }

    public void rumble(boolean left_side, double amount, double duration) {
        GenericHID.RumbleType rtype = GenericHID.RumbleType.kRightRumble ;
        if (left_side)
            rtype = GenericHID.RumbleType.kLeftRumble ;

        controller_.setRumble(rtype, amount);
        start_ = getSubsystem().getRobot().getTime() ;
        duration_ = duration ;
        rumbling_ = true ;
    }

    @Override
    public void computeState() {
        if (rumbling_ && getSubsystem().getRobot().getTime() - start_ > duration_)
        {
            controller_.setRumble(GenericHID.RumbleType.kLeftRumble, 0.0) ;
            controller_.setRumble(GenericHID.RumbleType.kRightRumble, 0.0) ;
            rumbling_ = false ;
        }
    }
    
    public boolean isRTriggerPressed() {
        DriverStation ds = DriverStation.getInstance() ;
        return ds.getStickAxis(getIndex(), AxisNumber.RTRIGGER.value) > 0.5 ;
    }

    public boolean isLTriggerPressed() {
        DriverStation ds = DriverStation.getInstance() ;
        return ds.getStickAxis(getIndex(), AxisNumber.LTRIGGER.value) > 0.5 ;
    }    

    public boolean isAPressed() {
        DriverStation ds = DriverStation.getInstance() ;
        return ds.getStickButton(getIndex(), ButtonNumber.A.value) ;
    }


    public boolean isBPressed() {
        DriverStation ds = DriverStation.getInstance() ;
        return ds.getStickButton(getIndex(), ButtonNumber.B.value) ;
    }


    public boolean isXPressed() {
        DriverStation ds = DriverStation.getInstance() ;
        return ds.getStickButton(getIndex(), ButtonNumber.X.value) ;
    }


    public boolean isYPressed() {
        DriverStation ds = DriverStation.getInstance() ;
        return ds.getStickButton(getIndex(), ButtonNumber.Y.value) ;
    }


    public boolean isLJoyButtonPressed() {
        DriverStation ds = DriverStation.getInstance() ;
        return ds.getStickButton(getIndex(), ButtonNumber.L_JOY.value) ;
    }

    public boolean isRJoyButtonPressed() {
        DriverStation ds = DriverStation.getInstance() ;
        return ds.getStickButton(getIndex(), ButtonNumber.R_JOY.value) ;
    }

    public POVAngle getPOVAngle() {
        DriverStation ds = DriverStation.getInstance() ;
        int povval = ds.getStickPOV(getIndex(), 0) ;
        return POVAngle.fromInt(povval) ;
    }

    protected enum AxisNumber {
        LEFTX(0),              ///< Left X axis
        LEFTY(1),              ///< Left Y axis
        LTRIGGER(2),           ///< Left Trigger Axis
        RTRIGGER(3),           ///< Right Trigger Axis
        RIGHTX(4),             ///< Right X axis
        RIGHTY(5) ;            ///< Right Y axis

        public final int value ;
        private AxisNumber(int value) {
            this.value = value ;
        }
    } ;

    /// \brief buttons on the gamepad
    protected enum ButtonNumber {
        A(1),                  ///< A button
        B(2),                  ///< B button
        X(3),                  ///< X button
        Y(4),                  ///< Y button
        LB(5),                 ///< Left back button
        RB(6),                 ///< Right back button
        BACK(7),               ///< Back button
        START(8),              ///< Start button
        L_JOY(9),              ///< Left joystick button
        R_JOY(10);               ///< Right joystick button

        public final int value ;
        private ButtonNumber(int value) {
            this.value = value ;
        }        
    } ;

    /// \brief POV angles
    protected enum POVAngle {
        UP(0),                 ///< Up, 0 degrees
        UPRIGHT(45),           ///< UpRight, 45 degrees
        RIGHT(90),             ///< Right, 90 degrees
        DOWNRIGHT(135),        ///< DownRight, 135 degrees
        DOWN(180),               ///< Down, 180 degrees
        DOWNLEFT(225),         ///< DownLeft, 225 degrees
        LEFT(270),             ///< Left, 270 degrees
        UPLEFT(315),           ///< UpLeft, 315 degrees
        NONE(-1) ;             ///< Not pressed in any direction

        public final int value ;

        private POVAngle(int value) {
            this.value = value ;
        }

        public static POVAngle fromInt(int id) {
            POVAngle[] As = POVAngle.values() ;
            for(int i = 0 ; i < As.length ; i++) {
                if (As[i].value == id)
                    return As[i] ;
            }

            return NONE ;
        }
    } ;    

    private XboxController controller_ ;
    private double start_ ;
    private double duration_ ;
    private boolean rumbling_ ;
}