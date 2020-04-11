package org.xero1425.base.oi;

import java.util.Map;

import edu.wpi.first.wpilibj.DriverStation;

import java.util.HashMap;
import org.xero1425.base.LoopType;
import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.actions.SequenceAction;

public class OIPanel extends HIDDevice
{
    public OIPanel(OISubsystem sub, int index) {
        super(sub, index) ;

        items_ = new HashMap<Integer, OIPanelItem>() ;
        next_handle_ = 1 ;
    }

    public int mapButton(int itemno, OIPanelButton.ButtonType type) {
        OIPanelButton button = new OIPanelButton(itemno, type) ;
        int handle = next_handle_++ ;
        items_.put(handle, button) ;
        return handle ;
    }

    public int mapAxisScale(int axisno, Double[] mapping) {
        OIPanelAxisScale scale = new OIPanelAxisScale(axisno, mapping) ;
        int handle = next_handle_++ ;
        items_.put(handle, scale) ;
        return handle ;        
    }

    public int mapAxisSwitch(int axisno, int positions) {
        OIPanelAxisSwitch sw = new OIPanelAxisSwitch(axisno, positions) ;
        int handle = next_handle_++ ;
        items_.put(handle, sw) ;
        return handle ;   
    }

    public int getValue(int handle) {
        if (!items_.containsKey(handle))
            return -1 ;
        
        return items_.get(handle).getValue() ;
    }

    @Override
    public void createStaticActions() throws Exception {
    }

    @Override
    public void init(LoopType ltype) {

    }

    @Override
    public void computeState() throws Exception {
        DriverStation ds = DriverStation.getInstance() ;
        
        for(OIPanelItem item : items_.values()) {
            if (item.getDashboardType() == OIPanelItem.JoystickResourceType.Button)
            {
                boolean v = ds.getStickButton(getIndex(), item.getItemNumber()) ;
                item.setButtonValue(v);
            }
            else
            {
                double v = ds.getStickAxis(getIndex(), item.getItemNumber()) ;
                item.setAxisValue(v) ;
            }
        }
    }

    @Override
    public void generateActions(SequenceAction seq) throws InvalidActionRequest {
    }

    int next_handle_ ;
    Map<Integer, OIPanelItem> items_ ;
} ;