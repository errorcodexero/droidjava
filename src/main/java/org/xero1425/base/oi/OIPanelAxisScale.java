package org.xero1425.base.oi ;

public class OIPanelAxisScale extends OIPanelItem
{
    public OIPanelAxisScale(int item, final Double[] map) {
        super(item, OIPanelItem.JoystickResourceType.Axis) ;

        map_ = map ;
        value_ = -1 ;
    }

    @Override
    public void setAxisValue(double value) {
        value_ = map_.length;
        for(int i = 0 ; i < map_.length ; i++)
        {
            if (value <= map_[i]) {
                value_ = i ;
                break ;
            }
        }
    }

    @Override
    public int getValue() {
        return value_ ;
    }

    private int value_ ;
    final private Double[] map_ ;
}