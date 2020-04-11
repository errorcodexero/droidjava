package org.xero1425.misc ;

public class EncoderMapper
{
    public EncoderMapper(double rmax, double rmin, double emax, double emin) {
        rmax_ = rmax ;
        rmin_ = rmin ;
        emax_ = emax ;
        emin_ = emin ;
        kEncoder2Robot_ = (rmax - rmin) / (emax - emin) ;   
    }

    public void calibrate(double robot, double encoder) {
        ec_ = encoder ;
        rc_ = robot ;
    }

    public double toRobot(double encoder) {
        double ret ;
        double offset; 

        encoder = clamp(encoder, emax_, emin_) ;
        offset = normalize(ec_ - (rc_ - rmin_) / kEncoder2Robot_, emax_, emin_) ;
        ret = normalize((encoder - offset) * kEncoder2Robot_ + rmin_, rmax_, rmin_) ;
        
        return ret ;
    }

    public double toEncoder(double robot) {
        double ret ;
        double offset ;

        robot = clamp(robot, rmax_, rmin_) ;
        offset = normalize(ec_ - (rc_ - rmin_) / kEncoder2Robot_,  emax_, emin_) ;
        ret = normalize(offset + (robot - rmin_) / kEncoder2Robot_,  emax_, emin_) ;
        
        return ret ;
    }    

    private double normalize(double value, double vmax, double vmin) {
        if (vmax < vmin)
        {
            double temp = vmax ;
            vmax = vmin ;
            vmin = temp ;
        }

        while (value < vmin)
        {
            value += (vmax - vmin) ;
        }

        while (value > vmax)
        {
            value -= (vmax - vmin) ;
        }

        return value ;
    }

    private double clamp(double value, double vmax, double vmin) {
        if (vmax < vmin)
        {
            double temp = vmax ;
            vmax = vmin ;
            vmin = temp ;
        }

        if (value > vmax)
            value = vmax ;
        else if (value < vmin)
            value = vmin ;

        return value ;
    }    

    private double kEncoder2Robot_ ;
    private double rmax_ ;
    private double rmin_ ;
    private double rc_ ;    
    private double emax_ ;
    private double emin_ ;
    private double ec_ ;
} ;
