package org.xero1425.simulator.models;

import org.xero1425.simulator.engine.SimulationModel;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import org.xero1425.simulator.engine.SimulationEngine;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;
import org.xero1425.misc.XeroMath;

public class TankDriveModel extends SimulationModel {
    private final static String SubTableName = "tankdrive" ;
    private final static String TankDriveXPos = "xpos" ;
    private final static String TankDriveYPos = "ypos" ;
    private final static String TankDriveAngle = "angle" ;
    private final static String TankDriveText = "text" ;
    private final static String TextProviderModel = "text_provider_model" ;
    private final static String TextProviderInst = "text_provider_instance" ;

    public TankDriveModel(SimulationEngine engine, String model, String inst) {
        super(engine, model, inst);

        navx_ = null;
        text_provider_ = null ;

        xpos_ = 0.0 ;
        ypos_ = 0.0 ;
        angle_ = 0.0 ;
    }
    
    @Override
    public void endCycle() {

        MessageLogger logger = getEngine().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getLoggerID()) ;
        logger.add("tankdrive") ;
        logger.add(" ").add(xpos_) ;
        logger.add(" ").add(ypos_) ;
        logger.add(" ").add(angle_) ;
        logger.endMessage() ;

        NetworkTable table_ = NetworkTableInstance.getDefault().getTable(SimulationEngine.NetworkTableName).getSubTable(SubTableName) ;
        table_.getEntry(TankDriveXPos).setNumber(xpos_) ;
        table_.getEntry(TankDriveYPos).setNumber(ypos_) ;
        table_.getEntry(TankDriveAngle).setNumber(XeroMath.rad2deg(angle_)) ;
        if (text_provider_ != null)
            table_.getEntry(TankDriveText).setString(text_provider_.provideText()) ;
    }

    public boolean create() {
        MessageLogger logger = getEngine().getMessageLogger() ;

        if (hasProperty(TextProviderModel) && hasProperty(TextProviderInst)) {
            SettingsValue modelprop = getProperty(TextProviderModel) ;
            SettingsValue instprop = getProperty(TextProviderInst) ;

            if (!modelprop.isString()) {
                logger.startMessage(MessageType.Error);
                logger.add("event: model ").addQuoted(getModelName());
                logger.add(" instance ").addQuoted(getInstanceName());
                logger.add(" property ").addQuoted(TextProviderModel).add(" is not a string") ;
                logger.endMessage();
                modelprop = null ;
            }

            if (!instprop.isString()) {
                logger.startMessage(MessageType.Error);
                logger.add("event: model ").addQuoted(getModelName());
                logger.add(" instance ").addQuoted(getInstanceName());
                logger.add(" property ").addQuoted(TextProviderInst).add(" is not a string") ;
                logger.endMessage();
                instprop = null ;
            }        

            if (modelprop != null && instprop != null) {
                try {
                    text_provider_ = getEngine().findModel(modelprop.getString(), instprop.getString());
                } catch (BadParameterTypeException e) {
                }

                if (text_provider_ == null) {
                    logger.startMessage(MessageType.Error);
                    logger.add("event: model ").addQuoted(getModelName());
                    logger.add(" instance ").addQuoted(getInstanceName());
                    logger.add(" the reference model for text provider does not exist") ;
                    logger.endMessage();                    
                }
            }
        }

        left_ = new SimMotorController(this, "left");
        if (!left_.createMotor())
            return false ;

        right_ = new SimMotorController(this, "right");
        if (!right_.createMotor())
            return false ;

        if (hasProperty("navx:model") && getProperty("navx:model").isString() && hasProperty("navx:instance")
                && getProperty("navx:instance").isString()) {

            String navx_model = null ;
            String navx_inst = null ;

            try {
                navx_model = getProperty("navx:model").getString();
                navx_inst = getProperty("navx:instance").getString() ;                
            } catch (BadParameterTypeException e) {
            }

            SimulationModel model = getEngine().findModel(navx_model, navx_inst) ;
            if (model != null && (model instanceof NavXModel))
                navx_ = (NavXModel)model ;
        }

        if (hasProperty("left:motor:inverted"))
        {
            SettingsValue v = getProperty("left:motor:interted") ;
            try {
                if (v.isBoolean() && v.getBoolean())
                    left_motor_mult_ = -1.0;
            } catch (BadParameterTypeException e) {
            }
        }

        if (hasProperty("right:motor:inverted"))
        {
            SettingsValue v = getProperty("right:motor:inverted") ;
            try {
                if (v.isBoolean() && v.getBoolean())
                    right_motor_mult_ = -1.0;
            } catch (BadParameterTypeException e) {
            }
        }        

        if (hasProperty("left:encoder:inverted"))
        {
            SettingsValue v = getProperty("left:encoder:inverted") ;
            try {
                if (v.isBoolean() && v.getBoolean())
                    left_encoder_mult_ = -1;
            } catch (BadParameterTypeException e) {
            }
        }        

        if (hasProperty("right:encoder:inverted"))
        {
            SettingsValue v = getProperty("right:encoder:inverted") ;
            try {
                if (v.isBoolean() && v.getBoolean())
                    right_encoder_mult_ = -1;
            } catch (BadParameterTypeException e) {
            }
        }         
        
        try {
            diameter_ = getProperty("diameter").getDouble();
            width_ = getProperty("width").getDouble() ;
            length_ = getProperty("length").getDouble() ;
            scrub_ = getProperty("scrub").getDouble() ;
            ticks_per_rev_ = getProperty("ticks_per_rev").getDouble() ;
            max_velocity_ = getProperty("maxvelocity").getDouble() ;
            max_accel_ = getProperty("maxacceleration").getDouble() ;
        } catch (BadParameterTypeException e) {
            return false ;
        }

        double circum = diameter_ * Math.PI ;
        left_rps_per_power_per_time_ = max_velocity_ / circum ;
        right_rps_per_power_per_time_ = max_velocity_ / circum ;

        max_change_ = max_accel_ / circum ;

        left_motor_mult_ = 1.0 ;
        right_motor_mult_ = 1.0 ;
        current_left_rps_ = 0.0 ;
        current_right_rps_ = 0.0 ;
        left_pos_ = 0.0 ;
        right_pos_ = 0.0 ;
        angle_ = 0.0 ;
        last_angle_ = 0.0 ;
        total_angle_ = 0.0 ;

        left_encoder_mult_ = 1 ;
        right_encoder_mult_ = 1 ;

        setCreated();
        return true ;
    }

    public Pose2d getPose() {
        return new Pose2d(xpos_, ypos_, new Rotation2d(angle_)) ;
    }

    public double getXPos() {
        return xpos_ ;
    }

    public double getYPos() {
        return ypos_ ;
    }

    public double getAngle() {
        return XeroMath.rad2deg(angle_) ;
    }

    public double getSpeed() {
        return speed_ ;
    }

    public double getWidth() {
        return width_ ;
    }

    public double getLength() {
        return length_ ;
    }    

    public void run(double dt) {

        double leftpower = left_.getPower() ;
        double rightpower = right_.getPower() ;

        double desired_left_rps = left_rps_per_power_per_time_ * leftpower * left_motor_mult_ ;
        double desired_right_rps = right_rps_per_power_per_time_ * rightpower * right_motor_mult_ ;

        current_left_rps_ = capVelocity(current_left_rps_, desired_left_rps) ;
        current_right_rps_ = capVelocity(current_right_rps_, desired_right_rps) ;

        double dleft = current_left_rps_ * dt * diameter_ * Math.PI ;
        double dright = current_right_rps_ * dt * diameter_ * Math.PI ;

        left_pos_ += dleft ;
        right_pos_ += dright ;

        double lrevs = left_pos_ / (Math.PI * diameter_) ;
        double rrevs = right_pos_ / (Math.PI * diameter_) ;
        double dv = (dright - dleft) / 2 * scrub_ ;
        angle_ = XeroMath.normalizeAngleRadians(angle_ + (dv * 2.0) / width_) ;
        updatePosition(dleft, dright, angle_) ;

        double distsq = (xpos_ - last_xpos_) * (xpos_ - last_xpos_) + (ypos_ - last_ypos_) * (ypos_ - last_ypos_) ;
        double dist = Math.sqrt(distsq) ;
        speed_ = dist / dt ;

        last_xpos_ = xpos_ ;
        last_ypos_ = ypos_ ;

        left_enc_value_ = (int)(lrevs * ticks_per_rev_ * left_encoder_mult_) ;
        right_enc_value_ = (int)(rrevs * ticks_per_rev_ * right_encoder_mult_) ;

        if (left_.usesTicks()) {
            left_.setEncoder(left_enc_value_);
            right_.setEncoder(right_enc_value_);
        }
        else {
            left_.setEncoder(lrevs * left_encoder_mult_);
            right_.setEncoder(rrevs * right_encoder_mult_) ;
        }

        double deg = XeroMath.normalizeAngleDegrees(-XeroMath.rad2deg(angle_)) ;
        if (navx_ != null) {
            navx_.setYaw(deg);
            navx_.setTotalAngle(XeroMath.rad2deg(total_angle_));
        }
    }

    public boolean processEvent(String name, SettingsValue value) {
        if (name.equals("xpos")) {
            if (!value.isDouble()) {
                MessageLogger logger = getEngine().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("event: model ").addQuoted(getModelName());
                logger.add(" instance ").addQuoted(getInstanceName());
                logger.add(" event name ").addQuoted(name);
                logger.add(" value is not a double").endMessage();
                return true ;
            }

            try {
                xpos_ = value.getDouble();
            } catch (BadParameterTypeException e) {
            }
        }
        else if (name.equals("ypos")) {
            if (!value.isDouble()) {
                MessageLogger logger = getEngine().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("event: model ").addQuoted(getModelName());
                logger.add(" instance ").addQuoted(getInstanceName());
                logger.add(" event name ").addQuoted(name);
                logger.add(" value is not a double").endMessage();
                return true ;
            }

            try {
                ypos_ = value.getDouble();
            } catch (BadParameterTypeException e) {
            }
        }  
        else if (name.equals("angle")) {
            if (!value.isDouble()) {
                MessageLogger logger = getEngine().getMessageLogger() ;
                logger.startMessage(MessageType.Error) ;
                logger.add("event: model ").addQuoted(getModelName());
                logger.add(" instance ").addQuoted(getInstanceName());
                logger.add(" event name ").addQuoted(name);
                logger.add(" value is not a double").endMessage();
                return true ;
            }

            try {
                angle_ = XeroMath.deg2rad(value.getDouble()) ;
            } catch (BadParameterTypeException e) {
            }
        }               
        return true ;
    }

    private void updatePosition(double dleft, double dright, double angle) {
        if (Math.abs(dleft - dright) < 1e-6) {
            xpos_ += dleft * Math.cos(angle) ;
            ypos_ += dright * Math.sin(angle) ;
        }
        else {
            double r = width_ * (dleft + dright) / (2 * (dright - dleft)) ;
            double wd = (dright - dleft) / width_ ;
            xpos_ = xpos_ + r * Math.sin(wd + angle) - r * Math.sin(angle) ;
            ypos_ = ypos_ - r * Math.cos(wd + angle) + r * Math.cos(angle) ;
        }

        double dangle = XeroMath.normalizeAngleRadians(angle_ - last_angle_) ;
        total_angle_ += dangle ;
    }

    private double capVelocity(double prev, double target) {
        double ret = 0.0 ;

        if (target > prev) {
            if (target > prev + max_change_)
                ret = prev +  max_change_ ;
            else
                ret = target ;
        } else {
            if (target < prev - max_change_)
                ret = prev - max_change_ ;
            else
                ret = target ;
        }

        return ret ;
    }

    private SimulationModel text_provider_ ;

    private SimMotorController left_ ;
    private SimMotorController right_ ;
    private NavXModel navx_ ;

    private double diameter_ ;
    private double width_ ;
    private double length_ ;
    private double scrub_ ;
    private double ticks_per_rev_ ;
    private double max_velocity_ ;
    private double max_accel_ ;

    private double left_motor_mult_ ;
    private double right_motor_mult_ ;
    
    private double current_left_rps_ ;
    private double current_right_rps_ ;
    private double left_pos_ ;
    private double right_pos_ ;
    private double angle_ ;
    private double last_angle_ ;
    private double total_angle_ ;
    private double speed_ ;

    private int left_enc_value_ ;
    private int right_enc_value_ ;
    private int left_encoder_mult_ ;
    private int right_encoder_mult_ ;

    private double left_rps_per_power_per_time_ ;
    private double right_rps_per_power_per_time_ ;
    private double max_change_ ;

    private double xpos_ ;
    private double ypos_ ;
    private double last_xpos_ ;
    private double last_ypos_ ;
}