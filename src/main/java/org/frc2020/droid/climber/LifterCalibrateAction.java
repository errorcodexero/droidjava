package org.frc2020.droid.climber;

import org.xero1425.base.actions.Action;

public class LifterCalibrateAction extends Action {
    public LifterCalibrateAction(LifterSubsystem sub) throws Exception {
        super(sub.getRobot().getMessageLogger());

        sub_ = sub ;
        holding_power_ = sub.getRobot().getSettingsParser().get("lifter:calibrate:hold_power").getDouble() ;        
        samples_ = sub.getRobot().getSettingsParser().get("lifter:calibrate:samples").getInteger() ;
        encoders_ = new double[samples_] ;
        down_power_ = sub.getRobot().getSettingsParser().get("lifter:calibrate:down_power").getDouble() ;
        if (down_power_ >= 0.0)
            throw new Exception("lifter calibrate down power must be negative") ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        if (sub_.isCalibarated()) {
            state_ = State.Holding ;
        }
        else {
            captured_ = 0 ;
            state_ = State.DownSlowly ;
            sub_.setPower(down_power_) ;
        }
    }

    @Override
    public void run() {
        switch(state_)
        {
            case DownSlowly:
                if (addEncoderPosition(sub_.getPosition())) {
                    sub_.setCalibrated();
                    sub_.reset() ;
                    state_ = State.Holding ;
                }
                break ;
            case Holding:
                sub_.setPower(holding_power_) ;
                break ;
        }
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "LifterCalibrationAction" ;
    }

    private boolean checkForStopped() {
        double vmax = encoders_[0] ;
        double vmin = encoders_[0] ;

        for(int i = 1 ; i < samples_ ; i++)
        {
            if (encoders_[i] < vmin)
                vmin = encoders_[i] ;

            if (encoders_[i] > vmax)
                vmax = encoders_[i] ;
        }

        return vmax - vmin < threshold_ ;        
    }

    private boolean addEncoderPosition(double pos) {
        boolean ret = false ;

        if (captured_ == samples_) {
            for(int i = samples_ - 1 ; i > 0 ; i--)
                encoders_[i] = encoders_[i - 1] ;
            encoders_[0] = pos ;
            ret = checkForStopped() ;
        }
        else {
            encoders_[captured_++] = pos ;
        }

        return ret ;
    }

    private enum State {
        DownSlowly,
        Holding
    } ;

    LifterSubsystem sub_ ;
    private State state_ ;
    double down_power_ ;
    double threshold_ ;
    double holding_power_ ;
    double [] encoders_ ;
    int samples_ ;
    int captured_ ;
}