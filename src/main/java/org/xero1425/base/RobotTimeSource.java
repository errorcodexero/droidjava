package org.xero1425.base ;

import edu.wpi.first.wpilibj.Timer ;
import org.xero1425.misc.MessageTimeSource; 

public class RobotTimeSource implements MessageTimeSource
{
    public RobotTimeSource() {
    }

    public double getTime() {
        return Timer.getFPGATimestamp() ;
    }
}