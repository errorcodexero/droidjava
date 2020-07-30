package org.xero1425.base.actions ;

import java.util.List;
import java.util.ArrayList;
import org.xero1425.base.Subsystem;
import org.xero1425.misc.MessageLogger;

public class SequenceAction extends ActionGroup
{
    public SequenceAction(MessageLogger logger) {
        super(logger) ;

        actions_ = new ArrayList<Action>() ;
        running_ = false ;
    }

    @Override
    public void getAllChildren(List<Action> output) {
        for(Action a : actions_) {
            if (a instanceof ActionGroup)
                ((ActionGroup)a).getAllChildren(output) ;
            output.add(a) ;
        }
    }      

    public void clear() {
        actions_.clear() ;
        running_ = false ;
    }

    public void addAction(Action act) throws InvalidActionRequest {
        if (running_)
            throw new InvalidActionRequest(this, 
                    "cannot add actions to sequence after it has been started") ;

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
        index_ = -1 ;

        startNextAction() ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;

        while (!isDone()) {
            if (actions_.get(index_).isDone())
            {
                startNextAction();
                if (isDone())
                    break ;
            }
            else
            {
                actions_.get(index_).run() ;
                if (actions_.get(index_).isDone())
                {
                    startNextAction();
                    if (isDone())
                        break ;
                }
                else
                {
                    break ;
                }
            }
        }
    }

    @Override
    public void cancel() {
        super.cancel() ;

        if (index_ >= 0 && index_ <= actions_.size())
            actions_.get(index_).cancel() ;
    }

    @Override
    public String toString(int indent) {
        String ret = prefix(indent) + "Sequence [" ;

        for(Action act : actions_)
        {
            ret += "\n" ;
            ret += act.toString(indent + 4) ;
        }
        ret += "\n" ;
        ret += spaces(indent) + "]" ;
        return ret ;
    }      

    private void startNextAction() throws Exception {
        index_++ ;
        if (index_ < actions_.size())
        {
            actions_.get(index_).start() ;
        }
        else
        {
            setDone() ;
        }
    }

    private int index_ ;
    private boolean running_ ;
    private List<Action> actions_ ;
} ;