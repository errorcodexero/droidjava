package org.xero1425.base.actions ;

public class InvalidActionRequest extends Exception
{
    static final long serialVersionUID = 42 ;
    
    public InvalidActionRequest(Action act, String msg) {
        super(act.toString() + "- " + msg) ;
        act_ = act ;
    }

    public Action getAction() {
        return act_ ;
    }

    private Action act_ ;
}