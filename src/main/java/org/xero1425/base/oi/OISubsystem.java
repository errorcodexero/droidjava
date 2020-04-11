package org.xero1425.base.oi;

import java.util.List;
import java.util.ArrayList;

import org.xero1425.base.LoopType;
import org.xero1425.base.Subsystem;
import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.actions.SequenceAction;

public class OISubsystem extends Subsystem {
    public OISubsystem(Subsystem parent, String name) {
        super(parent, name);

        devices_ = new ArrayList<HIDDevice>();
    }

    @Override
    public void init(LoopType ltype) {
        for (HIDDevice dev : devices_)
            dev.init(ltype);
    }

    @Override
    public void computeMyState() throws Exception {
        for (HIDDevice dev : devices_) {
            if (dev.isEnabled())
                dev.computeState();
        }
    }

    public void generateActions(SequenceAction seq) throws InvalidActionRequest {
        for(HIDDevice dev : devices_)
            dev.generateActions(seq) ;
    }

    @Override
    public void run() {
    }

    @Override
    public void postHWInit() throws Exception {
        //
        // This is done here because we are guarenteed to have created
        // all subsystems and established their parent child relationship
        //
        for(HIDDevice dev : devices_)
            dev.createStaticActions();
    }

    public int getAutoModeSelector() {
        for(HIDDevice dev : devices_)
        {
            int mode = dev.getAutoModeSelector() ;
            if (mode != -1)
                return mode ;
        }

        return -1 ;
    }

    protected void addHIDDevice(HIDDevice dev) {
        devices_.add(dev) ;
    }

    private List<HIDDevice> devices_ ;
} ;