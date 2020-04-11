package org.xero1425.base.oi ;

public class OIPanelAxisSwitch extends OIPanelItem
{
    public OIPanelAxisSwitch(int item, int count) {
        super(item, OIPanelItem.JoystickResourceType.Axis) ;

        count_ = count ;
    }

    int getCount() {
        return count_ ;
    }

    @Override
    public void setAxisValue(double value) {
        double minvalue = -1.0 ;
        double maxvalue = 1.0 ;
        double range = maxvalue - minvalue ;
        double slice = range / count_ ;

        double v = minvalue + slice ;
        value_ = count_ - 1 ;
        for(int i = 0 ; i < count_ ; i++) {
            if (value < v) {
                value_ = i ;
                break ;
            }
            v += slice ;
        }
    }

    @Override
    public int getValue() {
        return value_ ;
    }

    private int value_ ;
    private int count_ ;
}