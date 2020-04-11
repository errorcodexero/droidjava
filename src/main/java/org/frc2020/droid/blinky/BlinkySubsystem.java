package org.frc2020.droid.blinky;

import edu.wpi.first.wpilibj.Relay;
import org.xero1425.base.Subsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class BlinkySubsystem extends Subsystem {
    public static final String SubsystemName = "blinky" ;

    public BlinkySubsystem(Subsystem parent) throws BadParameterTypeException, MissingParameterException {
        super(parent, SubsystemName) ;

        int rnum ;

        rnum = getRobot().getSettingsParser().get("hw:blinky:r0").getInteger() ;
        r0_ = new Relay(rnum) ;

        rnum = getRobot().getSettingsParser().get("hw:blinky:r1").getInteger() ;
        r1_ = new Relay(rnum) ;
    }

    @Override
    public void computeMyState() throws Exception {
        super.computeMyState();
    }

    @Override
    public void run() throws Exception {
        super.run() ;
    }

    public void setPattern(int pattern) {
        switch(pattern & 3)
        {
            case 0:
                r0_.set(Relay.Value.kOff) ;
                break ;

            case 1:
                r0_.set(Relay.Value.kForward) ;
                break ;

            case 2:
                r0_.set(Relay.Value.kReverse) ;
                break ;

            case 3:
                r0_.set(Relay.Value.kOn) ;
                break ;
        }

        switch((pattern >> 2) & 3)
        {
            case 0:
                r1_.set(Relay.Value.kOff) ;
                break ;

            case 1:
                r1_.set(Relay.Value.kForward) ;
                break ;

            case 2:
                r1_.set(Relay.Value.kReverse) ;
                break ;

            case 3:
                r1_.set(Relay.Value.kOn) ;
                break ;
        }        
    }

    private Relay r0_ ;
    private Relay r1_ ;
}