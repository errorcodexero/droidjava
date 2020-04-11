package org.xero1425.misc ;

import java.lang.Exception ;

public class MissingParameterException extends Exception
{
    static final long serialVersionUID = 42 ;
    
    public MissingParameterException(String param) {
        super("missing parameter '" + param + "'") ;
        param_ = param ;
    }

    public String getParameter() {
        return param_ ;
    }

    private String param_ ;
}

