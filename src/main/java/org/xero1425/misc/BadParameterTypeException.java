package org.xero1425.misc ;

import java.lang.Exception ;

/**
 * This class is an exception that is thrown when a value of the incorrect type is
 * requested from a SettingsValue class.
 */
public class BadParameterTypeException extends Exception
{
    static final long serialVersionUID = 42 ;
    
    public BadParameterTypeException(SettingsValue.SettingsType expected, SettingsValue.SettingsType got) {
        super("wrong parameter type, expected " + expected.toString() + " got " + got.toString()) ;
        expected_ = expected ;
        got_ = got ;
    }

    public SettingsValue.SettingsType expected() {
        return expected_ ;
    }

    public SettingsValue.SettingsType got() {
        return got_ ;
    }

    private SettingsValue.SettingsType expected_ ;
    private SettingsValue.SettingsType got_ ;
}

