package org.frc2020.droid.targettracker;

import java.util.ArrayList;
import java.util.List;

import org.frc2020.droid.droidlimelight.DroidLimeLightSubsystem;
import org.frc2020.droid.turret.TurretSubsystem;
import org.xero1425.base.Subsystem;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;

//
// The purpose of the tracker class is to generate two things.  It generates
// an error value for the TURRET pid controller and it generates the distance
// from the robot camera to the target.  The target tracker returns a boolean
// that indicates if the target tracker is ready to fire.  This goes from 
// false to true once the drivebase is stopped, the target is seen, and a value
// for angle and distacne has been locked in.
//
public class TargetTrackerSubsystem extends Subsystem {
    public static final String SubsystemName = "targettracker" ;

    public TargetTrackerSubsystem(Subsystem parent, TankDriveSubsystem db, 
                    DroidLimeLightSubsystem ll, TurretSubsystem turret) throws BadParameterTypeException, MissingParameterException {
        super(parent, SubsystemName) ;

        ll_ = ll ;
        db_ = db ;
        turret_ = turret ;

        camera_offset_angle_ = getRobot().getSettingsParser().get("targettracker:camera_offset_angle").getDouble() ;
        db_velocity_threshold_ = getRobot().getSettingsParser().get("targettracker:fire:max_drivebase_velocity").getDouble() ;

        distances_ = new ArrayList<Double>() ;
        angles_ = new ArrayList<Double>() ;

        samples_ = 5 ;
        max_sample_age_ = 5.0 ;
        is_ready_ = false ;
        locked_ = false ;
    }

    public DroidLimeLightSubsystem getLimeLight() {
        return ll_ ;
    }

    @Override
    public void computeMyState() {
        MessageLogger logger = getRobot().getMessageLogger() ;

        if (db_.getVelocity() > db_velocity_threshold_) {
            //
            // The drivebase is moving, we cannot be locked with the
            // drivebase moving.  Clear out any history and wait for the
            // drivebase to stop.
            //
            logger.startMessage(MessageType.Debug, getLoggerID()) ;
            logger.add("targettracker: drivebase moving, target tracker history cleared").endMessage();

            distances_.clear() ;
            angles_.clear() ;
            is_ready_ = false ;
            locked_ = false ;
        }
        else {
            //
            // We are stopped, so see if we can grab a new sample.  We keep grabbing samples even while
            // we are locked.  These new samples are not used to change the shot, be we have them in case
            // we need them.
            //
            if (ll_.isTargetDetected()) {
                double dist = ll_.getDistance() ;

                // If you are rotated a positive angle away from the target on the
                // turret, the yaw from the limelight will be a negative angle, so we need
                // to reverse the angle of the yaw
                double yaw = -ll_.getYaw() ;

                distances_.add(dist) ;
                if (distances_.size() > samples_)
                    distances_.remove(0) ;

                angles_.add(yaw + calculateDesiredCameraOffset()) ;
                if (angles_.size() > samples_)
                    angles_.remove(0) ;

                most_recent_sample_ = getRobot().getTime() ;
                
                logger.startMessage(MessageType.Debug, getLoggerID()) ;
                logger.add("targettracker: adding sample") ;
                logger.add(" dist", dist) ;
                logger.add(" angle", yaw) ;
                logger.endMessage();
            }

            //
            // Now, based on the number of samples and their age, we see if we are
            // locked into a target
            //
            if (getRobot().getTime() - most_recent_sample_ > max_sample_age_ || distances_.size() < samples_) {
                //
                // Either I have not acquired enough samples yet, or my last sample is old enough that I
                // am pretty sure I don't have a target.  The idea here is that I may lose the target for short
                // amount of times while balls fly in front of the camera, but the target will come back in a 
                // timely fashion.
                //
                logger.startMessage(MessageType.Debug, getLoggerID()) ;
                logger.add("targettracker: not ready") ;
                logger.add(" age", (getRobot().getTime() - most_recent_sample_)) ;
                logger.add(" maxage", max_sample_age_) ;
                logger.add(" actual_samples", distances_.size()) ;
                logger.add(" required_samples", samples_) ;
                logger.endMessage();
                is_ready_ = false ;
            }
            else {
                //
                // Ok, I have enough samples and they are recent enough that the tracker has a
                // real target.  It does not mean that we are aimed at the target, that is up to the
                // turret to do, we just have a target we can aim at.
                //
                is_ready_ = true ;

                if (locked_ || turret_.isReadyToFire()) {
                    //
                    // If the turret has locked into a target, we stop moving the
                    // desired angle based on new data.
                    //
                    locked_ = true ;
                }
                else {
                    //
                    // I am not currently locked in, so I take the good data I have an lock it
                    // in for the duration of the shot.
                    //
                    logger.startMessage(MessageType.Debug, getLoggerID()) ;
                    logger.add("targettracker: ready") ;
                    logger.add(" age", (getRobot().getTime() - most_recent_sample_)) ;
                    logger.add(" maxage", max_sample_age_) ;
                    logger.add(" actual_samples", distances_.size()) ;
                    logger.add(" required_samples", samples_) ;
                    logger.endMessage();

                    distance_ = calcDistances(distances_) ;
                    error_ = calcError(angles_) ;
                }
            }
        }
    }

    public boolean isReadyToFire() {
        return is_ready_ ;
    }

    public double getDistance() {
        return distance_ ;
    }

    public double getTurretError() {
        return error_ ;
    }

    private double calculateDesiredCameraOffset() {
        return camera_offset_angle_ ;
    }

    private double calcDistances(List<Double> dists) {
        double dist = 0.0 ;
        for(Double d : dists)
            dist += d ;

        return dist / (double)dists.size() ;
    }

    private double calcError(List<Double> errors) {
        double error = 0.0 ;
        for(Double d : errors)
            error += d ;
        
            return error / (double)errors.size() ;
    }

    private double camera_offset_angle_ ;
    private double db_velocity_threshold_ ;
    private DroidLimeLightSubsystem ll_ ;
    private TankDriveSubsystem db_ ;
    private TurretSubsystem turret_ ;

    private double most_recent_sample_ ;
    private int samples_ ;
    private List<Double> distances_ ;
    private List<Double> angles_ ;
    private double max_sample_age_ ;
    private boolean is_ready_ ;
    private boolean locked_ ;

    private double distance_ ;
    private double error_ ;


}
