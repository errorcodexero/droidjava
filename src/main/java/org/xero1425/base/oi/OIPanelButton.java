package org.xero1425.base.oi ;

public class OIPanelButton extends OIPanelItem
{
    public enum ButtonType {
        Level,
        LevelInv,
        LowToHigh,
        HighToLow
    } ;

    public OIPanelButton(int item, ButtonType type) {
        super(item, OIPanelItem.JoystickResourceType.Button) ;
        type_ = type ;
        value_ = 0 ;
        prev_ = false ;
    }

    public ButtonType getType() {
        return type_ ;
    }

    @Override
    public void setButtonValue(boolean value) {
        switch(type_)
        {
            case Level:
                value_ = value ? 1 : 0 ;
                break ;

            case LevelInv:
                value_ = value ? 0 : 1 ;
                break ;

            case LowToHigh:
                if (!prev_ && value)
                    value_ = 1 ;
                else
                    value_ = 0 ;
                break ;
                
            case HighToLow:
                if (prev_ && !value)
                    value_ = 1 ;
                else
                    value_ = 0 ;
                break ;
        }

        prev_ = value ;
    }

    @Override
    public int getValue() {
        return value_ ;
    }

    int value_ ;
    boolean prev_ ;
    ButtonType type_ ;
}