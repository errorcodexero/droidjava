package org.xero1425.base.actions;

import java.util.List;

import org.xero1425.base.Subsystem;

public class DispatchAction extends ActionGroup {
    public DispatchAction(Subsystem sub, Action act, boolean block) {
        super(sub.getRobot().getMessageLogger());

        sub_ = sub;
        act_ = act;
        block_ = block;
    }

    @Override
    public void start() throws Exception {
        super.start();

        sub_.setAction(act_);
        if (!block_)
            setDone();
    }

    @Override
    public void run() throws Exception {
        super.run();

        if (block_ && act_.isDone()) {
            setDone();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        if (block_) {
            act_.cancel();
            sub_.setAction(null);
        }
    }

    @Override
    public String toString(int indent) {
        String ret = prefix(indent) + "DispatchAction [" ;

        ret += "\n" ;
        ret += spaces(indent + 4) + sub_.getName() + ", " + act_.toString(0);
        if (block_)
            ret += ", BLOCKING";
        else
            ret += ", NONBLOCKING";

        ret += "\n" ;
        ret += spaces(indent) + "]" ;
        return ret;
    }

    private Subsystem sub_;
    private Action act_;
    private boolean block_;

    @Override
    public void getAllChildren(List<Action> output) {
        if (act_ instanceof ActionGroup)
            ((ActionGroup)act_).getAllChildren(output);
            
        output.add(act_) ;
    }
}