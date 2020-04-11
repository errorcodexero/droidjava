package org.frc2020.droid.droidlimelight;

import org.xero1425.base.Subsystem;
import org.xero1425.base.limelight.LimeLightSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class DroidLimeLightSubsystem extends LimeLightSubsystem {
    public static final String SubsystemName = "limelight" ;

    public DroidLimeLightSubsystem(Subsystem parent) throws BadParameterTypeException, MissingParameterException {
        super(parent, SubsystemName) ;

        camera_angle_ = getRobot().getSettingsParser().get("droidvision:camera_angle").getDouble() ;
        camera_height_ = getRobot().getSettingsParser().get("droidvision:camera_height").getDouble() ;
        target_height_ = getRobot().getSettingsParser().get("droidvision:target_height").getDouble() ;
        distance_ = 0 ;
        sample_time_ = 0 ;

        setCamMode(CamMode.VisionProcessing) ;
        setLedMode(LedMode.ForceOff) ;
    }

    @Override
    public void computeMyState() {
        super.computeMyState();
        
        if (isLimeLightConnected() && isTargetDetected())
        {
            distance_ = (target_height_ - camera_height_) / Math.tan(Math.toRadians(camera_angle_ + getTY())) ;
            yaw_ = getTX() ;
            sample_time_ = getRobot().getTime() - getTotalLatency() ;

            putDashboard("vision-distance", DisplayType.Verbose, distance_);
            putDashboard("vision-yaw", DisplayType.Verbose, yaw_) ;
        }
    }

    public double getDistance() {
        return distance_ ;
    }

    public double getYaw() {
        return yaw_ ;
    }

    public double getSampleTime() {
        return sample_time_ ;
    }

    private double distance_ ;
    private double yaw_ ;
    private double sample_time_;
    private double camera_angle_ ;
    private double camera_height_ ;
    private double target_height_ ;
}