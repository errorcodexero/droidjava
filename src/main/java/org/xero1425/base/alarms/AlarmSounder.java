package org.xero1425.base.alarms ;

public abstract class AlarmSounder {
    public AlarmSounder(String name) {
        name_ = name ;
    }

    public String getName() {
        return name_ ;
    }

    public abstract void signalAlarm() ;

    private String name_ ;
}