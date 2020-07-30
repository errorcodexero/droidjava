package org.xero1425.base.actions;

import java.util.List;
import java.util.ArrayList;

import org.xero1425.base.Subsystem;
import org.xero1425.misc.MessageLogger;

public class ParallelAction extends ActionGroup
{
    public ParallelAction(MessageLogger logger) {
        super(logger) ;

        actions_ = new ArrayList<Action>() ;
        running_ = false ;
    }

    public void addAction(Action act) throws InvalidActionRequest {
        if (running_)
            throw new InvalidActionRequest(this, 
                    "cannot add actions to parallel after it has been started") ;

        actions_.add(act) ;
    }

    public void addSubActionPair(Subsystem sub, Action act, boolean block) throws InvalidActionRequest {
        DispatchAction d = new DispatchAction(sub, act, block) ;
        addAction(d) ;
    }

    public List<Action> getChildren() {
        return actions_ ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;
        running_ = true ;

        for(Action act : actions_)
            act.start() ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;

        boolean done = true ;
        for(Action act : actions_)
        {
            if (!act.isDone()) {
                act.run() ;
                if (!act.isDone())
                    done = false ;
            }
        }

        if (done)
            setDone() ;
    }

    @Override
    public void cancel() {
        super.cancel() ;

        for(Action act : actions_) {
            if (!act.isDone())
                act.cancel() ;
        }
    }

    @Override
    public String toString(int indent) {
        String ret = prefix(indent) + "ParallelAction [" ;

        for(Action act : actions_)
        {
            ret += "\n" ;
            ret += act.toString(indent + 4) ;
        }
        ret += "\n" ;
        ret += spaces(indent) + "]" ;
        return ret ;
    }    

    @Override
    public void getAllChildren(List<Action> output) {
        for(Action a : actions_) {
            if (a instanceof ActionGroup)
                ((ActionGroup)a).getAllChildren(output) ;
            output.add(a) ;
        }
    }    

    private List<Action> actions_ ;
    private boolean running_ ;

} ;