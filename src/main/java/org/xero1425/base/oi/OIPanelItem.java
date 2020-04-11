package org.xero1425.base.oi ;

public abstract class OIPanelItem {
    public enum JoystickResourceType
    {
        Axis,
        Button
    } ;

    public OIPanelItem(int itemno, JoystickResourceType type) {
        item_number_ = itemno ;
        type_ = type ;
    }

    public int getItemNumber() {
        return item_number_ ;
    }

    public JoystickResourceType getDashboardType() {
        return type_ ;
    }

    public void setButtonValue(boolean value) throws Exception {
        throw new Exception("must override this for button based items") ;
    }

    public void setAxisValue(double value) throws Exception {
        throw new Exception("must override this for axis based items") ;
    }

    public abstract int getValue() ;

    private JoystickResourceType type_ ;
    private int item_number_ ;
} ;