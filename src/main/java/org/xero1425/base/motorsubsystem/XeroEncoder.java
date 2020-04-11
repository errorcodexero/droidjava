package org.xero1425.base.motorsubsystem;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.Encoder;
import org.xero1425.base.XeroRobot;
import org.xero1425.base.motors.BadMotorRequestException;
import org.xero1425.base.motors.MotorController;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.EncoderMapper;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.SettingsParser;

public class XeroEncoder {
    public XeroEncoder(XeroRobot robot, String cname, boolean angular, MotorController ctrl)
            throws BadParameterTypeException, MissingParameterException, EncoderConfigException,
            BadMotorRequestException {
        createEncoder(robot, cname, ctrl);
    }

    private void createEncoder(XeroRobot robot, String cname, MotorController ctrl)
            throws BadParameterTypeException, MissingParameterException, EncoderConfigException,
            BadMotorRequestException {
        createQuadEncoder(robot, cname, ctrl);
        createAnalogEncoder(robot, cname);
        createPWMEncoder(robot, cname);

        if (pwm_ != null && analog_ != null)
            throw new EncoderConfigException("motor '" + cname + "' - both PWM and ANALOG encoders not valid");

        if (pwm_ == null && analog_ == null && quad_ == null && motor_ == null)
            throw new EncoderConfigException("motor '" + cname + "' - must define a QUAD, PWM, MOTOR, or ANALOG encoder");
    }

    public double getRawCount() {
        double result = 0.0 ;

        try {
            if (motor_ != null)
                result = motor_.getPosition() ;
            else if (quad_ != null)
                result = quad_.get() ;
            else if (analog_ != null)
                result = analog_.getVoltage() ;
            else if (pwm_ != null)
                result = pwm_.getPeriod() ;
        }
        catch(Exception ex) {
            result = 0.0 ;
        }

        return result ;
    }

    public double getPosition() {
        double result = 0.0;

        try {
            if (motor_ != null) {
                result = motor_.getPosition() * quad_m_ + quad_b_;
            }
            else if (quad_ != null) {
                result = quad_.get() * quad_m_ + quad_b_ ;
            }
            else if (analog_ != null) {
                result = mapper_.toRobot(analog_.getVoltage()) ;
            }
            else if (pwm_ != null) {
                result = mapper_.toRobot(pwm_.getPeriod()) ;
            }
        } 
        catch (Exception ex) 
        {
            //
            // THis should never happen, but in case it does
            //
            result = 0.0 ;
        }

        return result;
    }

    private double getAbsolutePosition() {
        double result = 0.0;

        if (analog_ != null) {
            result = mapper_.toRobot(analog_.getVoltage()) ;
        }
        else if (pwm_ != null) {
            result = mapper_.toRobot(pwm_.getPeriod()) ;
        }
        return result;        
    }

    public void reset() {
        try {
            if (motor_ != null)
                motor_.resetEncoder();
            else if (quad_ != null)
                quad_.reset() ;

            calibrate() ;
        }
        catch(Exception ex) {

        }
    }

    public void calibrate(double pos) {
        if (quad_ != null)
            quad_.reset() ;
        else if (motor_ != null)
        {
            try {
                motor_.resetEncoder();
            }
            catch(Exception ex) {                
            }
        }

        quad_b_ = pos ;
    }

    public void calibrate() {
        if ((quad_ != null || motor_ != null) && (analog_ != null && pwm_ != null))
        {
            //
            // We have one of QUAD or MOTOR encoder which are relative
            // We have one of ANALOG or PWM encoder which are absolute
            //
            // Use the absolute encoder to calibrate the relative encoder
            //
            calibrate(getAbsolutePosition());
        }
    }

    private void createQuadEncoder(XeroRobot robot, String cname, MotorController ctrl)
            throws BadParameterTypeException, MissingParameterException, EncoderConfigException,
            BadMotorRequestException {
        SettingsParser settings = robot.getSettingsParser() ;

        //
        // First check for a quadrature encoder
        //
        if (settings.isDefined(cname + ":quad:motor")) {
            //
            // The NAME:quad:motor is set,  its required to be true and
            // indicates we are using the encoder in the motor
            //
            motor_ = ctrl ;
            if (!motor_.hasPosition())
                throw new EncoderConfigException("motor '" + cname + "' - motor does not have internal encoder");
        }
        else if (settings.isDefined(cname + ":quad:1") || settings.isDefined(cname + ":quad:2"))
        {
            //
            // The NAME:quad:1 (or 2) is set, so this is a standard external quadrature encoder
            // connect to two digital I/Os.
            //
            int i1 = settings.get(cname + ":quad:1").getInteger() ;
            int i2 = settings.get(cname + ":quad:2").getInteger() ;

            quad_ = new Encoder(i1, i2) ;
        }
        else
        {
            quad_ = null ;
        }

        if (quad_ != null || motor_ != null)
        {
            quad_m_ = settings.get(cname + ":quad:m").getDouble() ;
            quad_b_ = settings.get(cname + ":quad:b").getDouble() ;
        }
    }

    private void createAnalogEncoder(XeroRobot robot, String cname)
            throws BadParameterTypeException, MissingParameterException {
        SettingsParser settings = robot.getSettingsParser() ;

        if (settings.isDefined(cname + ":analog"))
        {
            int a = settings.get(cname+":analog").getInteger() ;
            analog_ = new AnalogInput(a) ;

            double rmin, rmax ;
            double emin, emax ;
            double rc, ec ;

            rmin = settings.get(cname + ":analog:rmin").getDouble() ;
            rmax = settings.get(cname + ":analog:rmax").getDouble() ;
            emin = settings.get(cname + ":analog:emin").getDouble() ;
            emax = settings.get(cname + ":analog:emax").getDouble() ;
            rc = settings.get(cname + ":analog:rc").getDouble() ;
            ec = settings.get(cname + ":analog:ec").getDouble() ;

            mapper_ = new EncoderMapper(rmax, rmin, emax, emin) ;
            mapper_.calibrate(rc, ec) ;
        }
        else
        {
            analog_ = null ;
        }
    }

    private void createPWMEncoder(XeroRobot robot, String cname)
            throws BadParameterTypeException, MissingParameterException {
        SettingsParser settings = robot.getSettingsParser() ;
                
        if (settings.isDefined(cname + ":pwm"))
        {
            int a = settings.get(cname+":pwm").getInteger() ;
            pwm_ = new Counter(a) ;
            pwm_.setSemiPeriodMode(true);

            double rmin, rmax ;
            double emin, emax ;
            double rc, ec ;

            rmin = settings.get(cname + ":analog:rmin").getDouble() ;
            rmax = settings.get(cname + ":analog:rmax").getDouble() ;
            emin = settings.get(cname + ":analog:emin").getDouble() ;
            emax = settings.get(cname + ":analog:emax").getDouble() ;
            rc = settings.get(cname + ":analog:rc").getDouble() ;
            ec = settings.get(cname + ":analog:ec").getDouble() ;

            mapper_ = new EncoderMapper(rmax, rmin, emax, emin) ;
            mapper_.calibrate(rc, ec) ;
        }
        else
        {
            pwm_ = null ;
        }
    }

    private Encoder quad_ ;
    private double quad_m_ ;
    private double quad_b_ ;
    private MotorController motor_ ;
    private AnalogInput analog_ ;
    private Counter pwm_ ;
    private EncoderMapper mapper_ ;
} ;