package org.xero1425.misc ;

public class PIDACtrl
{
    public PIDACtrl(SettingsParser settings, String name, boolean angle) 
                    throws BadParameterTypeException, MissingParameterException {
        kv_ = settings.get(name + ":kv").getDouble() ;
        ka_ = settings.get(name + ":ka").getDouble() ;
        kp_ = settings.get(name + ":kp").getDouble() ;
        kd_ = settings.get(name + ":kd").getDouble() ;
        angle_ = angle ;
    }

    public PIDACtrl(double kv, double ka, double kp, double kd, boolean angle) {
        kv_ = kv ;
        ka_ = ka ;
        kp_ = kp ;
        kd_ = kd ;
        angle_ = angle ;
    }

    public double getOutput(double a, double v, double dtarget, double dactual, double dt) {
        double current_error ;
            
        if (angle_)
            current_error = XeroMath.normalizeAngleDegrees(dtarget - dactual) ;
        else
            current_error = dtarget - dactual ;

        vpart_ = v * kv_ ;
        apart_ = a * ka_ ;
        ppart_ = current_error * kp_ ;
        dpart_ = ((current_error - last_error_) / dt -v) * kd_ ;

        double output = vpart_ + apart_ + ppart_ + dpart_ ;
        last_error_ = current_error ;
        return output ;
    }

    /// \brief returns the V portion of the output value
    /// \returns the V portion of the output value
    public double getVPart() {
        return vpart_ ;
    }

    /// \brief returns the A portion of the output value
    /// \returns the A portion of the output value
    public double getAPart() {
        return apart_ ;
    }

    /// \brief returns the P portion of the output value
    /// \returns the P portion of the output value
    public double getPPart() {
        return ppart_ ;
    }

    /// \brief returns the D portion of the output value
    /// \returns the D portion of the output value
    public double getDPart() {
        return dpart_ ;
    }

    /// \brief returns the last error value
    /// \returns the last error value
    public double getLastError() {
        return last_error_ ;
    }    

    private double ka_ ;
    private double kv_ ;
    private double kp_ ;
    private double kd_ ;
    private boolean angle_ ;

    private double last_error_ ;
    private double vpart_ ;
    private double apart_ ;
    private double ppart_ ;
    private double dpart_ ;
} ;
