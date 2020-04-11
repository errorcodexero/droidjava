package org.xero1425.base.motors ;

import java.util.List ;
import java.util.ArrayList ;

public class MotorGroupController extends MotorController
{
    public MotorGroupController(String name) {
        super(name) ;
        motors_ = new ArrayList<MotorController>() ;
    }

    public void addMotor(MotorController ctrl, boolean inverted) throws BadMotorRequestException {
        if (motors_.size() > 0 && !motors_.get(0).getName().equals(ctrl.getName()))
            throw new BadMotorRequestException(this, "cannot add motor to group with existing motors unless the are the same type") ;

        motors_.add(ctrl) ;

        if (motors_.size() > 1)
            ctrl.follow(motors_.get(0), inverted) ;
    }

    public void set(double percent) throws BadMotorRequestException {
        if (motors_.size() == 0)
            throw new BadMotorRequestException(this, "request made to empty MotorGroupController") ;

        motors_.get(0).set(percent) ;
    }

    public void setInverted(boolean inverted)  throws BadMotorRequestException {
        if (motors_.size() == 0)
            throw new BadMotorRequestException(this, "request made to empty MotorGroupController") ;
            
        motors_.get(0).setInverted(inverted);
    }

    public boolean isInverted() throws BadMotorRequestException {
        if (motors_.size() == 0)
            throw new BadMotorRequestException(this, "request made to empty MotorGroupController") ;
            
        return motors_.get(0).isInverted() ;
    }

    public void reapplyInverted()  throws BadMotorRequestException {
        if (motors_.size() == 0)
            throw new BadMotorRequestException(this, "request made to empty MotorGroupController") ;
            
        motors_.get(0).reapplyInverted();        
    }

    public void setNeutralMode(NeutralMode mode) throws BadMotorRequestException {
        if (motors_.size() == 0)
            throw new BadMotorRequestException(this, "request made to empty MotorGroupController") ;

        for(MotorController ctrl : motors_)
            ctrl.setNeutralMode(mode);
    }

    public void follow(MotorController ctrl, boolean invert) throws BadMotorRequestException {
        throw new BadMotorRequestException(this, "a motor group cannot follow other motors") ;
    }

    public String getType() throws BadMotorRequestException {
        if (motors_.size() == 0)
            throw new BadMotorRequestException(this, "request made to empty MotorGroupController") ;

        return motors_.get(0).getType() ;
    }

    public boolean hasPosition() throws BadMotorRequestException{
        if (motors_.size() == 0)
            throw new BadMotorRequestException(this, "request made to empty MotorGroupController") ;

        return motors_.get(0).hasPosition() ;
    }

    public double getPosition() throws BadMotorRequestException {
        if (motors_.size() == 0)
            throw new BadMotorRequestException(this, "request made to empty MotorGroupController") ;

        return motors_.get(0).getPosition() ;  
    }

    public void resetEncoder() throws BadMotorRequestException {
        if (motors_.size() == 0)
            throw new BadMotorRequestException(this, "request made to empty MotorGroupController") ;

        motors_.get(0).resetEncoder();
    }

    public void setCurrentLimit(double limit) throws BadMotorRequestException {
        if (motors_.size() == 0)
            throw new BadMotorRequestException(this, "request made to empty MotorGroupController") ;
                    
        for(MotorController ctrl : motors_)
            ctrl.setCurrentLimit(limit);
    }      

    List<MotorController> motors_ ;
} ;