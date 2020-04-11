package org.frc2020.droid.droidoi;

import org.xero1425.base.Subsystem;
import org.xero1425.base.oi.OISubsystem;
import org.xero1425.base.oi.TankDriveGamepad;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;

public class DroidOISubsystem extends OISubsystem {
    public final static String SubsystemName = "droidoi";
    private final static String DriverGamepadHIDIndexName = "hw:driverstation:hid:driver";
    private final static String OIHIDIndexName = "hw:driverstation:hid:oi";

    public DroidOISubsystem(Subsystem parent, TankDriveSubsystem db) {
        super(parent, SubsystemName);

        MessageLogger logger = getRobot().getMessageLogger() ;
        int index;

        try {
            index = getRobot().getSettingsParser().get(DriverGamepadHIDIndexName).getInteger();
        } catch (BadParameterTypeException e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("parameter ").addQuoted(DriverGamepadHIDIndexName) ;
            logger.add(" exists, but is not an integer").endMessage();
            index = -1 ;
        } catch (MissingParameterException e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("parameter ").addQuoted(DriverGamepadHIDIndexName) ;
            logger.add(" does not exist").endMessage();
            index = -1 ;            
        }
        
        if (index != -1) {
            try {
                gp_ = new TankDriveGamepad(this, index, db) ;
                addHIDDevice(gp_);
            }
            catch(Exception ex) {
                logger.startMessage(MessageType.Error) ;
                logger.add("driver gamepad HID device was not created ").endMessage();
            }
        }

        try {
            index = getRobot().getSettingsParser().get(OIHIDIndexName).getInteger() ;
        } catch (BadParameterTypeException e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("parameter ").addQuoted(OIHIDIndexName) ;
            logger.add(" exists, but is not an integer").endMessage();
            index = -1 ;
        } catch (MissingParameterException e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("parameter ").addQuoted(OIHIDIndexName) ;
            logger.add(" does not exist").endMessage();
            index = -1 ;            
        }

        if (index != -1) {
            try {
                oi_ = new DroidOIDevice(this, index, gp_) ;
                addHIDDevice(oi_) ;
            }
            catch(Exception ex) {
                logger.startMessage(MessageType.Error) ;
                logger.add("OI HID device was not created ").endMessage();
            }
        }
    }

    public TankDriveGamepad getGamepad() {
        return gp_ ;
    }

    private TankDriveGamepad gp_ ;
    private DroidOIDevice oi_ ;
}