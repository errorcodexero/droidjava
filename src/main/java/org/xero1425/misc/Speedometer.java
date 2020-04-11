package org.xero1425.misc ;

import java.util.List ;
import java.util.ArrayList ;

public class Speedometer
{
    public Speedometer(String name, int samples, boolean angle) {
        angle_ = angle ;
        max_samples_ = samples ;
        distances_ = new ArrayList<Double>() ;
        velocities_ = new ArrayList<Double>() ;
        times_ = new ArrayList<Double>() ;
        accel_ = 0.0 ;
        name_ = name ;
    }

    public String getName() {
        return name_ ;
    }

    public void update(double dtime, double pos) {
        double vel ;

        if (dtime > 1e-4) {
            times_.add(dtime) ;
            if (times_.size() > max_samples_)
                times_.remove(0) ;

            distances_.add(pos) ;
            if (distances_.size() > max_samples_)
                distances_.remove(0) ;

            double total = 0.0 ;
            for(int i = 1 ; i < times_.size() ; i++)
                total += times_.get(i) ;

            if (angle_)
                vel = XeroMath.normalizeAngleDegrees(getDistance() - getOldestDistance()) / total ;
            else
                vel = (getDistance() - getOldestDistance()) / total ;

            velocities_.add(vel) ;
            if (velocities_.size() > max_samples_)
                velocities_.remove(0) ;

            accel_ = (getVelocity() - getOldestVelocity()) / total ;
        }
    }    

    public double getDistance() {
        if (distances_.size() == 0)
            return 0.0 ;

        return distances_.get(distances_.size() - 1) ;
    }

    public double getVelocity() {
        if (velocities_.size() == 0)
            return 0.0 ;

        return velocities_.get(velocities_.size() - 1) ;        
    }

    public double getAcceleration() {
        return accel_ ;
    }

    public double getOldestDistance()  {
        return distances_.get(0) ;
    }

    public double getOldestVelocity() {
        return velocities_.get(0) ;
    }    

    private boolean angle_ ;
    private int max_samples_ ;
    private List<Double> distances_ ;
    private List<Double> velocities_ ;
    private List<Double> times_ ;
    private double accel_ ;
    private String name_ ;
}