package org.frc2020.droid.gamepiecemanipulator.conveyor;

import edu.wpi.first.wpilibj.DigitalInput;
import org.xero1425.base.Subsystem;
import org.xero1425.base.motors.BadMotorRequestException;
import org.xero1425.base.motors.MotorController;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.SettingsValue;

public class ConveyorSubsystem extends Subsystem {
    public static final String SubsystemName = "conveyor";
    public static final String SensorLoggerName = "conveyor-sensors";
    public static final int MAX_BALLS = 5;
    public static final int SENSOR_COUNT = Sensor.values().length;
    public static final String SensorSubsystemName = null;

    public ConveyorSubsystem(Subsystem parent) throws BadParameterTypeException, MissingParameterException {
        super(parent, SubsystemName);

        sensor_logger_id_ = getRobot().getMessageLogger().registerSubsystem(SensorLoggerName);

        int num;
        int basech = (int) 'a';

        staged_for_collect_ = false;
        staged_for_fire_ = false;
        collecting_ = false;

        sensors_ = new DigitalInput[SENSOR_COUNT];
        sensor_states_ = new boolean[SENSOR_COUNT];
        prev_sensor_states_ = new boolean[SENSOR_COUNT];

        for (int i = 0; i < SENSOR_COUNT; i++) {
            String name = "hw:conveyor:sensor:" + (char) (basech + i);
            num = getRobot().getSettingsParser().get(name).getInteger();
            sensors_[i] = new DigitalInput(num);
            sensor_states_[i] = false ;
            prev_sensor_states_[i] = false ;
            String sname = Character.toString((char)('A' + i)) ;
            putDashboard(sname, DisplayType.Verbose, sensor_states_[i]);
        }

        intake_motor_ = getRobot().getMotorFactory().createMotor("intake", "hw:conveyor:motors:intake");
        shooter_motor_ = getRobot().getMotorFactory().createMotor("shooter", "hw:conveyor:motors:shooter");
    }

    public int getSensorLoggerID() {
        return sensor_logger_id_ ;
    }

    public SettingsValue getProperty(String name) {
        SettingsValue v = null ;
        
        if (name.equals("ballcount")) {
            v = new SettingsValue(ball_count_) ;
        }
        else if (name.equals("readyToCollect")) {
            v = new SettingsValue(isStagedForCollect()) ;
        }
        else if (name.equals("readyToFire")) {
            v = new SettingsValue(isStagedForFire()) ;
        }

        return v ;
    }

    public boolean isFull() {
        return ball_count_ == MAX_BALLS ;
    }

    public boolean isEmpty() {
        return ball_count_ == 0 ;
    }

    public boolean isStagedForCollect() {
        if (isFull())
            return false ;
            
        return staged_for_collect_ ;
    }

    public void setStagedForCollect(boolean staged) {
        staged_for_collect_ = staged;

        MessageLogger logger = getRobot().getMessageLogger();
        logger.startMessage(MessageType.Debug, getLoggerID());
        logger.add("Conveyor:").add("setStagedForCollect", staged_for_collect_);
        logger.endMessage();
    }

    public boolean isStagedForFire() {
        return staged_for_fire_ ;
    }

    public void setStagedForFire(boolean staged) {
        staged_for_fire_ = staged;

        MessageLogger logger = getRobot().getMessageLogger();
        logger.startMessage(MessageType.Debug, getLoggerID());
        logger.add("Conveyor:").add("setStagedForFire", staged_for_fire_);
        logger.endMessage();
    }

    public void setCollecting(boolean collecting) {
        collecting_ = collecting;

        MessageLogger logger = getRobot().getMessageLogger();
        logger.startMessage(MessageType.Debug, getLoggerID());
        logger.add("Conveyor:").add("collecting", collecting_);
        logger.endMessage();
    }

    @Override
    public void postHWInit() {
        setDefaultAction(new ConveyorStopAction(this));
    }

    @Override
    public void computeMyState() throws Exception {
        MessageLogger logger = getRobot().getMessageLogger();

        for (int i = 0; i < SENSOR_COUNT; i++) {
            prev_sensor_states_[i] = sensor_states_[i] ;
            sensor_states_[i] = !sensors_[i].get();

            if (prev_sensor_states_[i] != sensor_states_[i]) {
                Sensor s = Sensor.fromInt(i);
                logger.startMessage(MessageType.Debug, getLoggerID());
                logger.add("Conveyor:").add("sensor ").add(s.toString());
                logger.add(" transitioned to ").add(sensor_states_[i]);
                logger.endMessage();
                putDashboard(s.toString(), DisplayType.Verbose, sensor_states_[i]);
            }
        }

        logger.startMessage(MessageType.Debug, getSensorLoggerID()) ;
        logger.add("sensors") ;
        for(int i = 0 ; i < SENSOR_COUNT ; i++) {
            logger.add(" [ ").add(Sensor.fromInt(i).toString()) ;
            logger.add(" ").add(prev_sensor_states_[i]) ;
            logger.add(" ").add(sensor_states_[i]) ;
            logger.add(" ").add(didSensorLowToHigh(Sensor.fromInt(i))) ;
            logger.add(" ").add(didSensorHighToLow(Sensor.fromInt(i))) ;            
            logger.add("]") ;
        }
        logger.endMessage();

        if (prev_sensor_states_[Sensor.D.value] == true && sensor_states_[Sensor.D.value] == false) {
            staged_for_fire_ = true ;
            logger.startMessage(MessageType.Debug, getLoggerID()) ;
            logger.add("setting iStagedForFire() to true in subsystem") ;
            logger.endMessage(); ;
        }

        putDashboard("staged-fire", DisplayType.Verbose, staged_for_fire_) ;
        putDashboard("staged-collect", DisplayType.Verbose, staged_for_collect_);
        putDashboard("ballcount", DisplayType.Always, ball_count_);
    }

    public boolean isCollecting() {
        return collecting_ ;
    }

    public int getBallCount() {
        return ball_count_ ;
    }

    protected void setBallCount(int n) {
        ball_count_ = n ;
    }

    protected void incrementBallCount() {
        ball_count_++ ;
    }

    protected void decrementBallCount() {
        ball_count_-- ;
    }    

    public boolean getSensorState(Sensor s) {
        return sensor_states_[s.value] ;
    }

    public boolean didSensorLowToHigh(Sensor s) {
        return !prev_sensor_states_[s.value] && sensor_states_[s.value] ;
    }

    public boolean didSensorHighToLow(Sensor s) {
        return prev_sensor_states_[s.value] && !sensor_states_[s.value] ;
    }

    protected void setMotorsPower(double intake, double shooter) {
        try {
            intake_motor_.set(intake) ;
            shooter_motor_.set(shooter) ;
        }
        catch(BadMotorRequestException ex) {
        }
    }

    public enum Sensor {
        A(0),
        B(1),
        C(2),
        D(3) ;

        public final int value ;

        private Sensor(int value) {
            this.value = value ;
        }

        public static Sensor fromInt(int id) throws Exception {
            Sensor[] As = Sensor.values() ;
            for(int i = 0 ; i < As.length ; i++) {
                if (As[i].value == id)
                    return As[i] ;
            }
            throw new Exception("invalid integer in Sensor.fromInt") ;
        }
    }     

    private int ball_count_ ;
    private boolean staged_for_collect_ ;
    private boolean staged_for_fire_ ;
    private boolean collecting_ ;
    private DigitalInput [] sensors_ ;
    private boolean [] sensor_states_ ;
    private boolean [] prev_sensor_states_ ;
    private MotorController intake_motor_ ;
    private MotorController shooter_motor_ ;
    private int sensor_logger_id_ ;
} ;