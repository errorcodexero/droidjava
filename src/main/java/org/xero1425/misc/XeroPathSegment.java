package org.xero1425.misc ;

public class XeroPathSegment
{
    public XeroPathSegment(double t, double x, double y, double pos, double vel, double accel, double jerk, double heading) {
        time_ = t ;
        x_ = x ;
        y_ = y ;
        pos_ = pos ;
        vel_ = vel ;
        accel_ = accel ;
        jerk_ = jerk ;
        heading_ = heading ;
    }

    public XeroPathSegment(Double[] data) {
        time_ = data[0] ;
        x_ = data[1] ;
        y_ = data[2] ;
        pos_ = data[3] ;
        vel_ = data[4] ;
        accel_ = data[5] ;
        jerk_ = data[6] ;
        heading_ = data[7] ;
    }

    public double getTime() {
        return time_ ;
    }

    public double getX() {
        return x_ ;
    }

    public double getY() {
        return y_ ;
    }

    public double getPosition() {
        return pos_ ;
    }

    public double getVelocity() {
        return vel_ ;
    }

    public double getAccel() {
        return accel_ ;
    }

    public double getJerk() {
        return jerk_ ;
    }

    public double getHeading() {
        return heading_ ;
    }

    private double time_ ;
    private double x_ ;
    private double y_ ;
    private double pos_ ;
    private double vel_ ;
    private double accel_ ;
    private double jerk_ ;
    private double heading_ ;
}