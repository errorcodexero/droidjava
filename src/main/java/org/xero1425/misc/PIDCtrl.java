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

    /// \brief create a new object by taking double-inputs.
    /// Enter all the correct information for the variables
    /// \param p the proportional constant
    /// \param i the integral constant
    /// \param d the derivative constant
    /// \param f the feedforward constant
    /// \param minout the minimum output
    /// \param maxout the maximum output
    /// \param maxint the maximum integral
    /// \param angle if true it is managing an angle between =180 and +180
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

    /// \brief create a new object by reading parameters from the settings parser.
    /// The kv parameter is found by looking up the basename + ":kv".  The ka parameters is
    /// found by looking up the basename + ":ka".  The kp parameter is found by looking up
    /// the basename + ":kp".  The kd parameter is found by looking up the basename + ":kd".
    /// \param settings the settings parser
    /// \param name the basename to use to extract params from the settings parser
    /// \param angle if true it is managing an angle between =180 and +180
    public PIDCtrl(SettingsParser settings, String name, boolean isangle) throws MissingParameterException, BadParameterTypeException {
        init(settings, name) ;
    }

    /// \brief create a new object by reading parameters from the settings parser.
    /// The kv parameter is found by looking up the basename + ":kv".  The ka parameters is
    /// found by looking up the basename + ":ka".  The kp parameter is found by looking up
    /// the basename + ":kp".  The kd parameter is found by looking up the basename + ":kd".
    /// \param settings the settings parser
    /// \param name the basename to use to extract params from the settings parser
    public void init(SettingsParser settings, String name)  throws MissingParameterException, BadParameterTypeException {
        kp_ = settings.get(name + ":kp").getDouble() ;
        ki_ = settings.get(name + ":ki").getDouble() ;
        kd_ = settings.get(name + ":kd").getDouble() ;
        kf_ = settings.get(name + ":kf").getDouble() ;
        kmin_ = settings.get(name + ":min").getDouble() ;
        kmax_ = settings.get(name + ":max").getDouble() ;
        kimax_ = settings.get(name + ":imax").getDouble() ;                                        
    }

    /// \brief get the output by using variables & performing the PID calculations
    /// \param target the target position
    /// \param current the current position
    /// \param dt the difference in time (delta time) since the last robot loop (should be 20 milliseconds)
    /// \returns the output applied to motors/etc. after performing calculations
    public double getOutput(double target, double current, double dt) {
        double error = calcError(target, current) ;
        double pOut = kp_ * error;
        double derivative = 0;

        // dt is difference in time (telta time)
        // "if" statement takes into account whether 1 robot loop has passed since program started
        if (has_last_error_) {
            // takes derivative based on definition of derivative
            derivative = (error - last_error_) / dt ;
        }
        
        // assigns last_error_ to current error
        last_error_ = error;
        has_last_error_ = true;

        // output for derivative * D-constant calculated
        double dOut = kd_ * derivative;
        
        // calculates the integral value by taking a summation of error * difference in time
        integral_ += error * dt ;
        
        // check if integral term is too large small
        if (integral_ > kimax_)
            integral_ = kimax_ ;
        else if (integral_ < -kimax_)
            integral_ = -kimax_ ;
        
        // output fot integral * I-constant calculated 
        double iOut = ki_ * integral_;
        
        // output sum of proportional, integral, and derivative calculations
        // add the feedforward term * target
        double output = pOut + iOut + dOut + kf_ * target ;
    
        // make sure output isn't too big or small
        // if it is, assign it to the min/max outputs
        if (output <= kmin_)
            output = kmin_ ;
        if (output >= kmax_)
            output = kmax_ ;
        
        return output;
    }

    /// \brief resets "has_last_error_" to default and sets the integral summation back to 0
    public void reset() {
        has_last_error_ = false ;
        integral_ = 0.0 ;
    }
    
    /// \brief gets the error between current and target position
    /// \param target the target position
    /// \param current the current position
    /// \returns the error
    private double calcError(double target, double current) {
        double error ;
        
        // check if target is an angle, if so use a math function to get output in angle degrees between +-180
        // else give a "normal" answer of just (target - current)
        if (is_angle_)
            error = XeroMath.normalizeAngleDegrees(target - current) ;
        else
            error = target - current ;
    
        return error ;        
    }
}