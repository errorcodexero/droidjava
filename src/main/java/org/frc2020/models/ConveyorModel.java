package org.frc2020.models;

import java.text.DecimalFormat;
import java.util.Arrays;

import edu.wpi.first.hal.sim.mockdata.DIODataJNI;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorSubsystem;
import org.frc2020.droid.gamepiecemanipulator.conveyor.ConveyorSubsystem.Sensor;
import org.xero1425.simulator.engine.SimulationEngine;
import org.xero1425.simulator.engine.SimulationModel;
import org.xero1425.simulator.models.SimMotorController;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;

public class ConveyorModel extends SimulationModel {
    public static final String LogBallPosition = "conveyor_model_balls" ;
    private static final String IntakeModelPropertyName = "intake_model" ;
    private static final String IntakeInstancePropertyName = "intake_instance" ;    
    private static final String SubTableName = "conveyor" ;
    private static final String BallPresentName = "present" ;
    private static final String BallPositionName = "position" ;
    private static final double BallSensorRegion = 2.0 ;
    private static final double NewBallPosition = -4.5 ;

    public ConveyorModel(SimulationEngine engine, String model, String inst) {
        super(engine, model, inst);

        fmt_ = new DecimalFormat("00.000") ;

        ball_position_logger_id_ = engine.getMessageLogger().registerSubsystem(LogBallPosition) ;
    }

    @Override
    public String statusString() {
        return Integer.toString(getBallCount()) ;
    }

    @Override
    public void endCycle() {
        NetworkTable table = NetworkTableInstance.getDefault().getTable(SimulationEngine.NetworkTableName).getSubTable(SubTableName) ;

        for(int i = 0 ; i < balls_.length ; i++) {
            Ball b = balls_[i] ;
            NetworkTable subkey = table.getSubTable(Integer.toString(i + 1)) ;
            subkey.getEntry(BallPresentName).setBoolean(b.isPresent()) ;
            subkey.getEntry(BallPositionName).setNumber(b.getPosition()) ;
        }

        for(int i = Sensor.A.value ; i <= Sensor.D.value ; i++) {
            String name = "Sensor" + (char)('A' + i) ;
            NetworkTable sub = table.getSubTable(name) ;
            sub.getEntry("state").setBoolean(!state_[i]) ;
        }

        table.getEntry("intakepower").setNumber(intake_.getPower()) ;
        table.getEntry("shooterpower").setNumber(shooter_.getPower()) ;

        printConveyor();
    }

    public boolean create() {
        MessageLogger logger = getEngine().getMessageLogger() ;

        intake_ = new SimMotorController(this, "intake") ;
        if (!intake_.createMotor())
            return false ;

        shooter_ = new SimMotorController(this, "shooter") ;
        if (!shooter_.createMotor())
            return false ;

        if (!hasProperty(IntakeModelPropertyName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(IntakeModelPropertyName);
            logger.endMessage();
            return false;
        }

        if (!hasProperty(IntakeInstancePropertyName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(IntakeInstancePropertyName);
            logger.endMessage();
            return false;
        }

        SettingsValue modelprop = getProperty(IntakeModelPropertyName);
        SettingsValue instprop = getProperty(IntakeInstancePropertyName);

        if (!modelprop.isString()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(IntakeModelPropertyName).add(" is not a string");
            logger.endMessage();
            return false;
        }

        if (!modelprop.isString()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(IntakeInstancePropertyName).add(" is not a string");
            logger.endMessage();
            return false;
        }

        try {
            intake_model_ = (IntakeModel) getEngine().findModel(modelprop.getString(), instprop.getString());
        } catch (Exception ex) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" referenced limelight model is not a limelight");
            logger.endMessage();
            return false;
        }

        balls_ = new Ball[ConveyorSubsystem.MAX_BALLS] ;
        for(int i = 0 ; i < balls_.length ; i++) {
            balls_[i] = new Ball(false, 0.0) ;
        }

        sensor_io_ = new int[ConveyorSubsystem.SENSOR_COUNT];
        sensor_pos_ = new double[ConveyorSubsystem.SENSOR_COUNT] ;
        state_ = new boolean[ConveyorSubsystem.SENSOR_COUNT] ;

        try {
            dist_per_second_per_volt_ = getProperty("distance_per_second_per_inch").getDouble() ;
            dist_per_second_stopped_ = getProperty("distance_per_second_stopped").getDouble() ;

            minpos_ = getProperty("minpos").getDouble() ;
            maxpos_ = getProperty("maxpos").getDouble() ;      
            midpos_ = getProperty("midpos").getDouble() ;

            sensor_io_[ConveyorSubsystem.Sensor.A.value] = getProperty("sensorA").getInteger() ;
            sensor_io_[ConveyorSubsystem.Sensor.B.value] = getProperty("sensorB").getInteger() ;
            sensor_io_[ConveyorSubsystem.Sensor.C.value] = getProperty("sensorC").getInteger() ;
            sensor_io_[ConveyorSubsystem.Sensor.D.value] = getProperty("sensorD").getInteger() ;
            
            sensor_pos_[ConveyorSubsystem.Sensor.A.value] = getProperty("positionA").getDouble() ;
            sensor_pos_[ConveyorSubsystem.Sensor.B.value] = getProperty("positionB").getDouble() ;
            sensor_pos_[ConveyorSubsystem.Sensor.C.value] = getProperty("positionC").getDouble() ;
            sensor_pos_[ConveyorSubsystem.Sensor.D.value] = getProperty("positionD").getDouble() ; 
            
            for(int i = 0 ; i < sensor_io_.length ; i++) {
                DIODataJNI.setIsInput(sensor_io_[i], true) ;
                DIODataJNI.setValue(sensor_io_[i], true) ;
            }
        }
        catch(BadParameterTypeException ex) {
        }

        NetworkTable table = NetworkTableInstance.getDefault().getTable(SimulationEngine.NetworkTableName).getSubTable(SubTableName) ;
        table.getEntry("MinConveyorPosition").setNumber(minpos_) ;
        table.getEntry("MaxConveyorPosition").setNumber(maxpos_) ;
        table.getEntry("MidConveyorPosition").setNumber(midpos_) ;

        for(int i = Sensor.A.value ; i <= Sensor.D.value ; i++) {
            String name = "Sensor" + (char)('A' + i) ;
            NetworkTable sub = table.getSubTable(name) ;
            sub.getEntry("position").setNumber(sensor_pos_[i]) ;
            sub.getEntry("state").setBoolean(false) ;
        }

        setCreated();
        return true ;
    }

    public boolean processEvent(String name, SettingsValue value) {
        try {
            if (name.equals("ball")) {
                if (value.isBoolean() && value.getBoolean() == true)
                    insertBallAtIntake() ;
            }
            else if (name.equals("print")) {
                if (value.isBoolean() && value.getBoolean() == true)
                    printBalls() ;
            }
            else if (name.equals("picture")) {
                if (value.isBoolean() && value.getBoolean() == true)
                    printConveyor();
            }            
            else if (name.equals("start3")) {
                if (value.isBoolean() && value.getBoolean() == true)
                    placeStart3() ;
            }
        }
        catch(BadParameterTypeException ex) {
            //
            // Should never happen, we check the types before getting the value
            //
        }
        return true ;
    }

    public void run(double dt) {

        if (isConveyorOff())
            moveBallsStopped(dt) ;
        else
            moveBallsRunning(dt) ;

        setSensors() ;
    }

    private boolean isConveyorOff() {
        return Math.abs(intake_.getPower()) < 0.01 && Math.abs(shooter_.getPower()) < 0.01 ;
    }

    private void moveBallsStopped(double dt) {
        if (intake_model_.isDownAndRunning()) {
            for(int i = balls_.length - 1 ; i >= 0 ; i--) {
                Ball b = balls_[i] ;
                if (b.isPresent() && b.getPosition() < 2.0) {
                    double pos = b.getPosition() + dist_per_second_stopped_ * dt ;
                    b.setPosition(pos) ;
                }
            }
        }
    }

    private void moveBallsRunning(double dt) {
        double dist = intake_.getPower() * dist_per_second_per_volt_ * dt ;
        double dist2 = shooter_.getPower() * dist_per_second_per_volt_ * dt ;

        if (dist > 0)
        {
            for(int i = balls_.length - 1 ; i >= 0 ; i--)
            {
                if (balls_[i].present_)
                {
                    double npos = 0.0 ;

                    if (balls_[i].getPosition() > midpos_)
                        npos = balls_[i].getPosition() + dist2 ;
                    else
                        npos = balls_[i].getPosition() + dist ;

                    balls_[i].setPosition(npos) ;
                }
            }

            int cnt = getBallCount() ;
            if (cnt > 0)
            {
                if (balls_[cnt - 1].getPosition() >= maxpos_ + 4.0)
                    deleteBallFromShooter() ;
            }
        }
        else
        {
            for(int i = 0 ; i < balls_.length ; i++)
            {
                if (balls_[i].present_)
                {
                    double newloc = balls_[i].getPosition() + dist ;
                    balls_[i].setPosition(newloc) ;
                }
            }

            int cnt = getBallCount() ;
            if (cnt > 0)
            {
                if (balls_[0].getPosition() < -7.0)
                    deleteBallFromIntake() ;
            }                
        }
    }

    private void setSensors() {
        // Update sensors
        state_[0] = true ;
        state_[1] = true ;
        state_[2] = true ;
        state_[3] = true ;

        for (int i = 0; i < balls_.length; i++) {
            if (!balls_[i].present_) 
                continue;

            double pos = balls_[i].getPosition();

            if (pos > sensor_pos_[Sensor.A.value] - BallSensorRegion * 2.0 && pos < sensor_pos_[Sensor.A.value] + BallSensorRegion * 2.0)
                state_[0] = false ;

            if (pos > sensor_pos_[Sensor.B.value] - BallSensorRegion && pos < sensor_pos_[Sensor.B.value] + BallSensorRegion)
                state_[1]  = false ;

            if (pos > sensor_pos_[Sensor.C.value] - BallSensorRegion && pos < sensor_pos_[Sensor.C.value] + BallSensorRegion)
                state_[2]  = false ;

            if (pos > sensor_pos_[Sensor.D.value] - BallSensorRegion && pos < sensor_pos_[Sensor.D.value] + BallSensorRegion)
                state_[3]  = false ;
        }

        DIODataJNI.setValue(sensor_io_[Sensor.A.value], state_[0]) ;
        DIODataJNI.setValue(sensor_io_[Sensor.B.value], state_[1]) ;
        DIODataJNI.setValue(sensor_io_[Sensor.C.value], state_[2]) ;
        DIODataJNI.setValue(sensor_io_[Sensor.D.value], state_[3]) ;
    }
    
    private class Ball {
        public Ball(boolean present, double pos) {
            present_ = present ;
            pos_ = pos ;
        }

        public boolean isPresent() {
            return present_ ;
        }

        public void setPresent(boolean b) {
            present_ = b ;
        }

        public double getPosition() {
            return pos_ ;
        }

        public void setPosition(double v) {
            pos_ = v ;
        }

        private boolean present_ ;
        private double pos_ ;
    }

    private int getBallCount() {
        int ret = 0 ;

        for(int i = 0 ; i < balls_.length ; i++) {
            if (balls_[i].isPresent())
                ret++ ;
        }

        return ret ;
    }

    private void printBalls() {
        MessageLogger logger = getEngine().getMessageLogger() ;
        logger.startMessage(MessageType.Info) ;
        logger.add("Conveyor:") ;
        for(Ball b : balls_) {
            logger.add("    ") ;
            if (b.isPresent()) {
                logger.add("(*)") ;
            }
            else {
                logger.add("( )") ;
            }
            logger.add(" ").add(fmt_.format(b.getPosition())) ;
        }
        logger.endMessage();
    }

    private void outputConveyorEdge(int length) {
        MessageLogger logger = getEngine().getMessageLogger() ;

        char [] chars = new char[length] ;
        Arrays.fill(chars, '=') ;
        for(double d : new double[]{ 0.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0 }) {
            int index = (int)((d - minpos_) / (maxpos_ - minpos_) * length) ;
            if (index >= 0 && index < chars.length)
                chars[index] = '|' ;
        }

        String line = String.valueOf(chars) ;
        logger.startMessage(MessageType.Info, ball_position_logger_id_).add(line).endMessage() ;
    }

    private void outputSensors(int length) {
        int index ;

        MessageLogger logger = getEngine().getMessageLogger() ;

        char [] chars = new char[length] ;
        Arrays.fill(chars, '=') ;

        index = (int)((sensor_pos_[0] - minpos_) / (maxpos_ - minpos_) * length) ;
        if (index >= 0 && index < chars.length)
            chars[index] = 'A' ;

        index = (int)((sensor_pos_[1] - minpos_) / (maxpos_ - minpos_) * length) ;
        if (index >= 0 && index < chars.length)
            chars[index] = 'B' ;
                
        index = (int)((sensor_pos_[2] - minpos_) / (maxpos_ - minpos_) * length) ;
        if (index >= 0 && index < chars.length)
            chars[index] = 'C' ;
                    
        index = (int)((sensor_pos_[3] - minpos_) / (maxpos_ - minpos_) * length) ;
        if (index >= 0 && index < chars.length)
            chars[index] = 'D' ;                    

        String line = String.valueOf(chars) ;
        logger.startMessage(MessageType.Info, ball_position_logger_id_).add(line).endMessage() ;        
    }

    private void printConveyor() {
        String line ;
        int length = 120 ;
        double intake_power = intake_.getPower() ;
        double shooter_power = shooter_.getPower() ;

        MessageLogger logger = getEngine().getMessageLogger() ;

        outputConveyorEdge(length);

        char [] chars = new char[length] ;
        Arrays.fill(chars, ' ') ;
        for(Ball b : balls_) {
            if (b.isPresent()) {
                int index = (int)((b.getPosition() - minpos_) / (maxpos_ - minpos_) * length) ;
                for(int i = index - 7  ; i < index + 7 ; i++) {
                    if (i >= 0 && i < chars.length) {
                        if (i >= index - 2 && i <= index + 2)
                            chars[i] = '+' ;
                        else
                            chars[i] = '*' ;                        
                    }
                }
            }
        }
        line = String.valueOf(chars) ;
        logger.startMessage(MessageType.Info, ball_position_logger_id_).add(line).endMessage() ;
        
        outputSensors(length) ;

        logger.startMessage(MessageType.Info, ball_position_logger_id_) ;
        for(Ball b : balls_) {
            if (b.isPresent())
                logger.add("  ").add(b.getPosition()) ;
        }
        logger.endMessage();

        logger.startMessage(MessageType.Info, ball_position_logger_id_) ;
        logger.add("intake", intake_power) ;
        logger.add("shooter", shooter_power) ;
        logger.add("A", !state_[0]) ;
        logger.add("B", !state_[1]) ;
        logger.add("C", !state_[2]) ;
        logger.add("D", !state_[3]) ;                        
        logger.endMessage() ;

        line = new String("======================================================================================") ;
        logger.startMessage(MessageType.Info, ball_position_logger_id_).add(line).endMessage() ;
    }

    private void placeStart3() {
        for(int i = 0 ; i < balls_.length ; i++) {
            balls_[i].setPresent(false);
        }

        balls_[0].setPresent(true) ;
        balls_[0].setPosition(3.5);

        balls_[1].setPresent(true) ;
        balls_[1].setPosition(10.5);
        
        balls_[2].setPresent(true) ;
        balls_[2].setPosition(17.5);        
    }

    private void insertBallAtIntake() {
        if (getBallCount() < balls_.length)
        {
            int i = balls_.length - 1 ;
            do
            {
                balls_[i].setPosition(balls_[i - 1].getPosition());
                balls_[i].setPresent(balls_[i-1].isPresent());

            } while (--i > 0) ;

            balls_[0].setPresent(true) ;
            balls_[0].setPosition(NewBallPosition);
        }          
    }

    private void deleteBallFromShooter() {
        int i = balls_.length - 1 ;

        while (i >= 0)
        {
            if (balls_[i].present_ && balls_[i].getPosition() > 31.0)
            {
                MessageLogger logger = getEngine().getMessageLogger() ;
                logger.startMessage(MessageType.Debug, getLoggerID()) ;
                logger.add("ball exited via shooter").endMessage();
                balls_[i].setPresent(false) ;
            }
            i-- ;
        }
    }

    private void deleteBallFromIntake() {
        int i = 0 ; 

        while (i < balls_.length - 1)
        {
            balls_[i].setPosition(balls_[i].getPosition()) ;
            balls_[i].setPresent(balls_[i].isPresent()) ;
            i++ ;
        }
        MessageLogger logger = getEngine().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, getLoggerID()) ;
        logger.add("ball exited via intake").endMessage();
        balls_[balls_.length - 1].setPresent(false);
    }

    private SimMotorController intake_ ;
    private SimMotorController shooter_ ;

    private int [] sensor_io_ ;
    private double [] sensor_pos_ ;

    private Ball [] balls_ ;

    private double dist_per_second_per_volt_ ;
    private double dist_per_second_stopped_ ;

    private DecimalFormat fmt_ ;

    private int ball_position_logger_id_ ;
    private boolean [] state_ ;

    private double minpos_ ;
    private double maxpos_ ;
    private double midpos_ ;

    private IntakeModel intake_model_ ;
}