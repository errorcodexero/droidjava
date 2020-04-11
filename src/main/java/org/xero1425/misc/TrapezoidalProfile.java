package org.xero1425.misc ;

public class TrapezoidalProfile {
    public TrapezoidalProfile(double accel, double decel, double maxv) {
        maxa_ = accel ;
        maxd_ = decel ;
        maxv_ = maxv ;
    }

    public TrapezoidalProfile(SettingsParser settings, String name) throws BadParameterTypeException, MissingParameterException {
        maxa_ = settings.get(name + ":maxa").getDouble() ;
        maxd_ = settings.get(name + ":maxd").getDouble() ;
        maxv_ = settings.get(name + ":maxv").getDouble() ;
    }

    public void update(double dist, double start_velocity, double end_velocity) {
        start_velocity_ = Math.abs(start_velocity) ;
        end_velocity_ = Math.abs(end_velocity) ;

        isneg_ = (dist < 0) ;
        distance_ = Math.abs(dist) ;

        ta_ = (maxv_ - start_velocity_) / maxa_ ;

        td_ = (end_velocity_ - maxv_) / maxd_ ;
        double da = start_velocity * ta_ + 0.5 * maxa_ * ta_ * ta_ ;
        double dd = maxv_ * td_ + 0.5 * maxd_ * td_ * td_ ;
        tc_ = (distance_ - da - dd) / maxv_ ;
        type_ = "trapezoid" ;

        if (td_ < 0.0 || da + dd > distance_) {
            //
            // We don't have time to get to the cruising velocity
            //
            double num = (2.0 * distance_ * maxa_ * maxd_ + maxd_ * start_velocity_ * start_velocity_ - maxa_ * end_velocity_ * end_velocity_) / (maxd_ - maxa_) ;
            boolean decel_only = false ;
            if (num < 0)
                decel_only = true ;
            else
                actual_max_velocity_ = Math.sqrt(num) ;


            if (decel_only || actual_max_velocity_ < start_velocity_) {
                // 
                // Just decelerate down to the end
                //
                ta_ = 0 ;
                tc_ = 0 ;
                td_ = (end_velocity - start_velocity_) / maxd_ ;
                actual_max_velocity_ = start_velocity_ ;
                type_ = "line" ;
            }
            else {
                //
                // Can't get to max velocity but can accelerate some
                // before decelerating
                //
                actual_max_velocity_ = Math.sqrt(num) ;
                ta_ = (actual_max_velocity_ -start_velocity_)/ maxa_ ;
                td_ = (end_velocity_ - actual_max_velocity_) / maxd_ ;
                tc_ = 0 ;
                type_ = "pyramid" ;
            }
        }
        else {
            //
            // Ok, now figure out the crusing time
            //
            actual_max_velocity_ = maxv_ ;                
            tc_ = (distance_ - da - dd) / maxv_ ;
        }
    }

    public double getAccel(double t) {
        double ret ;

        if (t < 0)
            ret = 0 ;
        else if (t < ta_)
            ret = maxa_ ;
        else if (t < ta_ + tc_)
            ret = 0.0 ;
        else if (t < ta_ + tc_ + td_)
            ret = maxd_ ;
        else
            ret = 0.0 ;

        return isneg_ ? -ret : ret ;
    }

    public double getVelocity(double t) {
        
        double ret ;
        if (t < 0.0) {
            ret = start_velocity_ ;
        }
        else if (t < ta_) {
            ret = start_velocity_ + t * maxa_ ;
        }   
        else if (t < ta_ + tc_) {
            ret = actual_max_velocity_ ;
        }   
        else if (t < ta_ + tc_ + td_) {
            double dt = (t - ta_ - tc_) ;
            ret = actual_max_velocity_ + dt * maxd_ ;
        }
        else {
            ret = end_velocity_ ;
        }

        return isneg_ ? -ret : ret ;
    }

    public double getDistance(double t) {
        double ret ;

        if (t < 0.0) {
            ret = 0.0 ;
        }
        else if (t < ta_) {
            ret = start_velocity_ * t + 0.5 * t * t * maxa_ ;
        }   
        else if (t < ta_ + tc_) {
            ret = start_velocity_ * ta_ + 0.5 * ta_ * ta_ * maxa_ ;
            ret += (t - ta_) * actual_max_velocity_ ;
        }   
        else if (t < ta_ + tc_ + td_) {
            double dt = t - ta_ - tc_ ;
            ret = start_velocity_ * ta_ + 0.5 * ta_ * ta_ * maxa_ ;
            ret += tc_ * actual_max_velocity_ ;
            ret += actual_max_velocity_ * dt + 0.5 * dt * dt * maxd_ ;
        }
        else {
            ret = distance_ ;
        }

        return isneg_ ? -ret : ret ;
    }

    public String toString() {
        String ret = "[" + type_ ;
        ret += ", sv " + Double.toString(start_velocity_) ;
        ret += ", mv " + Double.toString(actual_max_velocity_) ;
        ret += ", ev " + Double.toString(end_velocity_) ;
        ret += ", ta " + Double.toString(ta_) ;
        ret += ", tc " + Double.toString(tc_) ;
        ret += ", td " + Double.toString(td_) ;                        
        ret += "]" ;

        return ret ;
    }

    public double getTimeAccel() {
        return ta_ ;
    }

    public double getTimeCruise() {
        return tc_ ;
    }

    public double getTimeDecel() {
        return td_ ;
    }

    public double getTotalTime() {
        return ta_ + tc_ + td_ ;
    }

    public double getActualMaxVelocity() {
        if (isneg_)
            return -actual_max_velocity_ ;

        return actual_max_velocity_ ;
    }

    public double getTimeForDistance(double dist) throws Exception {
        double ret ;
        double sign = isneg_ ? -1.0 : 1.0 ;
        Double [] roots ;

        if (isneg_)
            dist = -dist ;

        if (dist < sign * getDistance(ta_)) {
            roots = XeroMath.quadratic(0.5 * maxa_, start_velocity_, -dist) ;
            ret = pickRoot(roots) ;
        }
        else if (dist < sign * getDistance(ta_ + tc_)) {
            dist -= sign * getDistance(ta_) ;
            ret = ta_ + dist / actual_max_velocity_ ;
        }
        else if (dist < sign * getDistance(ta_ + tc_ + td_)) {
            dist -= sign * getDistance(ta_ + tc_) ;
            roots = XeroMath.quadratic(0.5 * maxd_, actual_max_velocity_, -dist) ;
            ret = pickRoot(roots) + ta_ + tc_ ;
        }
        else {
            ret = ta_ + tc_ + td_ ;
        }

        return ret ;
    }

    public double getStartVelocity() {
        return start_velocity_ ;
    }

    public double getEndVelocity() {
        return end_velocity_ ;
    }

    private double pickRoot(Double [] roots) throws Exception {
        double ret = 0.0 ;

        if (roots.length == 0)
            throw new Exception("no real roots for equation") ;

        if (roots.length == 1)
        {
            if (roots[0] < 0.0)
                throw new Exception("all real roots are negative") ;

            ret = roots[0] ;
        }
        else 
        {
            if (roots[0] < 0.0 && roots[1] < 0.0)
                throw new Exception("all real roots are negative") ;

            if (roots[0] < 0.0)
                ret = roots[1] ;
            else if (roots[1] < 0.0)
                ret = roots[0] ;
            else if (roots[0] < roots[1])
                ret =  roots[0] ;
            else
                ret = roots[1] ;
        }

        return ret ;
    }

    private double maxa_ ;
    private double maxd_ ;
    private double maxv_ ;

    private double ta_ ;
    private double tc_ ;
    private double td_ ;

    private double start_velocity_ ;
    private double end_velocity_ ;
    private double distance_ ;

    private boolean isneg_ ;
    private double actual_max_velocity_ ;

    private String type_ ;
}