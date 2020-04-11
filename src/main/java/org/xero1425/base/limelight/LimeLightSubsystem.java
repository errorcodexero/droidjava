package org.xero1425.base.limelight;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.xero1425.base.Subsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.SettingsValue;

public class LimeLightSubsystem extends Subsystem {
    public final static String LimeLightTableName = "limelight";
    public final static String CamModeKeyName = "camMode" ;
    public final static String LedModeKeyName = "ledMode" ;
    public final static String PipelineKeyName = "pipeline" ;

    public LimeLightSubsystem(Subsystem parent, String name) throws BadParameterTypeException, MissingParameterException {
        super(parent, name) ;

        led_mode_ = LedMode.Invalid ;
        cam_mode_ = CamMode.Invalid ;
        pipeline_ = -1 ;

        camera_latency_ = getRobot().getSettingsParser().get("limelight:camera_latency").getDouble() ;
        network_latency_ = getRobot().getSettingsParser().get("limelight:network_latency").getDouble() ;

        nt_ = NetworkTableInstance.getDefault().getTable(LimeLightTableName) ;

        setLedMode(LedMode.ForceOff);
        setCamMode(CamMode.VisionProcessing) ;
        setPipeline(0);
    }

    public enum CamMode
    {
        VisionProcessing,
        DriverCamera,
        Invalid ;
    } ;

    public enum LedMode
    {
        UseLED,
        ForceOff,
        ForceBlink,
        ForceOn,
        Invalid
    } ;

    @Override
    public SettingsValue getProperty(String name) {
        SettingsValue v = null ;

        if (name.equals("tv")) {
            v = new SettingsValue(tv_) ;
        }
        else if (name.equals("tx")) {
            v = new SettingsValue(tx_) ;
        }
        else if (name.equals("ty")) {
            v = new SettingsValue(ty_) ;
        }

        return v ;
    }

    @Override
    public void computeMyState() {
        if (cam_mode_ == CamMode.VisionProcessing)
        {
            if (nt_.containsKey("tv"))
            {
                connected_ = true ;
                if (nt_.getEntry("tv").getNumber(0.0).doubleValue() < 0.01)
                {
                    tv_ = false ;
                }
                else
                {
                    tv_ = true ;
                    tx_ = nt_.getEntry("tx").getNumber(0.0).doubleValue() ;
                    ty_ = nt_.getEntry("ty").getNumber(0.0).doubleValue() ;
                    ta_ = nt_.getEntry("ta").getNumber(0.0).doubleValue() ;
                    total_latency_ = nt_.getEntry("tl").getNumber(0.010).doubleValue() + camera_latency_ + network_latency_ ;
                }
            }
            else
            {
                connected_ = false ;
                if (getRobot().getTime() > 4.0)
                {
                    MessageLogger logger = getRobot().getMessageLogger() ;
                    logger.startMessage(MessageType.Error) ;
                    logger.add("did not detect limelight (after 4 seconds)") ;
                    logger.endMessage() ;
                }
            }
        }
        else
        {
            tv_ = false ;
        }
    }

    @Override
    public void run() {
    }

    public boolean isLimeLightConnected() {
        return connected_ ;
    }

    public boolean isTargetDetected() {
        return tv_ ;
    }

    public double getTX() {
        return tx_ ;
    }

    public double getTY() {
        return ty_ ;
    }

    public double getTA() {
        return ta_ ;
    }

    public void setCamMode(CamMode mode) {
        if (cam_mode_ != mode)
        {
            switch(mode) {
                case VisionProcessing:
                    nt_.getEntry(CamModeKeyName).setNumber(0) ;
                    break ;
                case DriverCamera:
                    nt_.getEntry(CamModeKeyName).setNumber(1) ;
                    break ;
                case Invalid:
                    break ;
            }

            cam_mode_ = mode ;
        }
    }

    public CamMode getCamMode() {
        return cam_mode_ ;
    }

    public void setLedMode(LedMode mode) {
        if (led_mode_ != mode)
        {
            switch(mode)
            {
                case UseLED:
                    nt_.getEntry(LedModeKeyName).setNumber(0) ;
                    break ;
                case ForceOff:
                    nt_.getEntry(LedModeKeyName).setNumber(1) ;
                    break ;
                case ForceBlink:
                    nt_.getEntry(LedModeKeyName).setNumber(2) ;
                    break ;
                case ForceOn:
                    nt_.getEntry(LedModeKeyName).setNumber(3) ;
                    break ;
                case Invalid:
                    break ;                                                                                
            }

            led_mode_ = mode ;
        }
    }

    public LedMode getLedMode() {
        return led_mode_ ;
    }

    public void setPipeline(int which) {
        if (which != pipeline_)
        {
            nt_.getEntry(PipelineKeyName).setNumber(which) ;
            pipeline_ = which ;
        }
    }

    public int getPipeline() {
        return pipeline_ ;
    }

    public double getTotalLatency() {
        return total_latency_ ;
    }

    @Override
    public String toString() {
        return "limelight" ;
    }

    private CamMode cam_mode_ ;
    private LedMode led_mode_ ;
    private int pipeline_ ;
    private boolean tv_ ;
    private boolean connected_ ;
    private double tx_ ;
    private double ty_ ;
    private double ta_ ;
    private double total_latency_ ;
    private double camera_latency_ ;
    private double network_latency_ ;
    private NetworkTable nt_ ;
} ;