package org.xero1425.misc ;

/// \file

/// \brief This class implements a classic PIDF controller
/// More information about this can be found @https://www.xerosw.org/dokuwiki/doku.php?id=software:followers
public class PIDCtrl
{
    //
    // The P constant
    //
    private double kp_ ;
    private double ki_ ;
    private double kd_ ;
    private double kf_ ;
    private double kmin_ ;
    private double kmax_;
    private double kimax_;

    private boolean is_angle_;

    private boolean has_last_error_;
    private double last_error_;

    private double integral_;

    public PIDCtrl(boolean isangle) {
        kp_ = 0 ;
        ki_ = 0 ;
        kd_ = 0 ;
        kf_ = 0 ;
        kmin_ = 0 ;
        kmax_ = 0 ;
        kimax_ = 0 ;

        is_angle_ = isangle ;
    }

    public PIDCtrl(double p, double i, double d, double f, double minout, double maxout, double maxint, boolean isangle) {
        kp_ = p ;
        ki_ = i ;
        kd_ = d ;
        kf_ = f ;
        kmin_ = minout ;
        kmax_ = maxout ;
        kimax_ = maxint ;

        is_angle_ = isangle ;
    }

    public PIDCtrl(SettingsParser settings, String name, boolean isangle) throws MissingParameterException, BadParameterTypeException {
        init(settings, name) ;
    }

    public void init(SettingsParser settings, String name)  throws MissingParameterException, BadParameterTypeException {
        kp_ = settings.get(name + ":kp").getDouble() ;
        ki_ = settings.get(name + ":ki").getDouble() ;
        kd_ = settings.get(name + ":kd").getDouble() ;
        kf_ = settings.get(name + ":kf").getDouble() ;
        kmin_ = settings.get(name + ":min").getDouble() ;
        kmax_ = settings.get(name + ":max").getDouble() ;
        kimax_ = settings.get(name + ":imax").getDouble() ;                                        
    }

    public double getOutput(double target, double current, double dt) {
        double error = calcError(target, current) ;
        double pOut = kp_ * error;
        double derivative = 0;

        if (has_last_error_) {
            derivative = (error - last_error_) / dt ;
        }

        last_error_ = error;
        has_last_error_ = true;
        double dOut = kd_ * derivative;
        
        integral_ += error * dt ;
        
        if (integral_ > kimax_)
            integral_ = kimax_ ;
        else if (integral_ < kimax_)
            integral_ = -kimax_ ;
        
        double iOut = ki_ * integral_;
    
        double output = pOut + iOut + dOut + kf_ * target ;
    
        if (output <= kmin_)
            output = kmin_ ;
        
        if (output >= kmax_)
            output = kmax_ ;
        
        return output;
    }

    public void reset() {
        has_last_error_ = false ;
        integral_ = 0.0 ;
    }

    private double calcError(double target, double current) {
        double error ;
        
        if (is_angle_)
            error = XeroMath.normalizeAngleDegrees(target - current) ;
        else
            error = target - current ;
    
        return error ;        
    }


} ;