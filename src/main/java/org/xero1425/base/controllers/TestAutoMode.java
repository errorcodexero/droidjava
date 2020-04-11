package org.xero1425.base.controllers ;

import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.SettingsParser;

public class TestAutoMode extends AutoMode {
    static private final String Which = "auto:testmode:which";
    static private final String Power = "auto:testmode:power";
    static private final String Duration = "auto:testmode:duration";
    static private final String Distance = "auto:testmode:distance";
    static private final String Name = "auto:testmode:name";

    public TestAutoMode(AutoController ctrl, String name) throws BadParameterTypeException, MissingParameterException {
        super(ctrl, name) ;

        SettingsParser parser = ctrl.getRobot().getSettingsParser() ;
        which_ = parser.get(Which).getInteger() ;
        power_ = parser.get(Power).getDouble() ;
        duration_ = parser.get(Duration).getDouble() ;
        position_ = parser.get(Distance).getDouble() ;
        name_ = parser.get(Name).getString() ;
    }

    protected int getTestNumber() {
        return which_;
    }

    protected double getPower() {
        return power_ ;
    }

    protected double getDuration() {
        return duration_ ;
    }

    protected double getPosition() {
        return position_ ;
    }

    protected String getNameParam() {
        return name_ ;
    }

    private int which_ ;
    private double power_ ;
    private double duration_ ;
    private double position_ ;
    private String name_ ;
}