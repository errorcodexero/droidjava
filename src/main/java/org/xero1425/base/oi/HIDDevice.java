package org.xero1425.base.oi;

import org.xero1425.base.LoopType;
import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.actions.SequenceAction;

public abstract class HIDDevice
{
    public HIDDevice(OISubsystem sub, int index) {
        sub_ = sub ;
        index_ = index ;
        enabled_ = true ;
    }

    public OISubsystem getSubsystem() {
        return sub_ ;
    }

    public int getIndex() {
        return index_ ;
    }

    public abstract void init(LoopType ltype) ;
    public abstract void generateActions(SequenceAction seq) throws InvalidActionRequest ;
    public abstract void createStaticActions() throws Exception ;
    public abstract void computeState() throws Exception ;
    public int getAutoModeSelector() {
        return -1 ;
    }

    public void enable() {
        enabled_ = true ;
    }

    public void disable() {
        enabled_ = false ;
    }

    public boolean isEnabled() {
        return enabled_ ;
    }

    private boolean enabled_ ;
    private OISubsystem sub_ ;
    private int index_ ;
}