package org.xero1425.base.motorsubsystem;

import org.xero1425.base.Subsystem;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.Speedometer;

public class MotorEncoderSubsystem extends MotorSubsystem
{
    public MotorEncoderSubsystem(Subsystem parent, String name, boolean angle) throws Exception {
        super(parent, name) ;

        smart_dashboard_when_enabled_ = false ;
        speedometer_ = new Speedometer(name, 2, angle) ;
        angular_ = angle ;

        String encname = "hw:" + name + ":encoder" ;
        encoder_ = new XeroEncoder(parent.getRobot(), encname, angle, getMotorController()) ;
    }

    public boolean isAngular() {
        return angular_ ;
    }

    public double getPosition() {
        return speedometer_.getDistance() ;
    }

    public double getVelocity() {
        return speedometer_.getVelocity() ;
    }

    public double getAcceleration() {
        return speedometer_.getAcceleration() ;
    }

    public void calibrate() {
        encoder_.calibrate(); 
    }

    public void calibrate(double pos) {
        encoder_.calibrate(pos) ;
    }

    public void reset() {
        super.reset() ;
        encoder_.reset() ;
    }

    public double getEncoderRawCount() {
        return encoder_.getRawCount() ;
    }

    @Override
    public void computeMyState() throws Exception {
        super.computeMyState();

        double pos = encoder_.getPosition() ;
        speedometer_.update(getRobot().getDeltaTime(), pos) ;

        putDashboard(getName() + ":pos", DisplayType.Verbose, speedometer_.getDistance()) ;
        putDashboard(getName() + ":vel", DisplayType.Verbose, speedometer_.getVelocity()) ;

        MessageLogger logger = getRobot().getMessageLogger()  ;
        logger.startMessage(MessageType.Debug, getLoggerID()) ;
        logger.add(getName()) ;
        logger.add("power", getPower()) ;
        logger.add("pos", pos) ;
        logger.add("velocity", speedometer_.getVelocity());
        logger.add("accel", speedometer_.getAcceleration()) ;
        logger.endMessage();
    }

    @Override
    public void run() throws Exception {
        super.run() ;
    }

    @Override
    public void postHWInit() throws Exception {
        super.postHWInit();
    }

    public void setSmartDashboardWhenEnabled(boolean b) {
        smart_dashboard_when_enabled_ = b ;
    }

    public boolean isSmartDashboardWhenEnabled() {
        return smart_dashboard_when_enabled_ ;
    }

    private Speedometer speedometer_ ;
    private XeroEncoder encoder_ ;
    private boolean smart_dashboard_when_enabled_ ;
    private boolean angular_ ;
}