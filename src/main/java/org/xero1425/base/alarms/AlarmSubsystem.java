package org.xero1425.base.alarms;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.DriverStation;

import java.util.* ;

import org.xero1425.base.Subsystem;

public class AlarmSubsystem extends Subsystem {
    public AlarmSubsystem(Subsystem parent) {
        super(parent, "alarms") ;

        entries_ = new ArrayList<AlarmEntry>() ;
    }

    @Override
    public void run() {
        DriverStation ds = DriverStation.getInstance() ;
        if (ds.isFMSAttached()) {
            double remaining = ds.getMatchTime() ;

            while (!entries_.isEmpty())
            {
                if (remaining < entries_.get(0).getTime())
                {
                    entries_.get(0).getSounder().signalAlarm();
                    entries_.remove(0) ;
                }
                else 
                {
                    break ;
                }
            }
        }
    }

    public void addEntry(double time, AlarmSounder sounder) {
        AlarmEntry entry = new AlarmEntry(time, sounder) ;
        entries_.add(entry) ;

        Collections.sort(entries_, new SortByTime()) ;
    }

    private class AlarmEntry {
        public AlarmEntry(double time, AlarmSounder sounder) {
            time_ = time ;
            sounder_ = sounder ;
        }

        public double getTime() {
            return time_ ;
        }

        public AlarmSounder getSounder() {
            return sounder_ ;
        }

        private double time_ ;
        private AlarmSounder sounder_ ;
    } ;

    private class SortByTime implements Comparator<AlarmEntry> {
        public int compare(AlarmEntry a, AlarmEntry b) {
            double diff  = a.getTime() - b.getTime() ;
            if (diff < 0.0)
                return -1 ;
            else if (diff > 0.0)
                return 1 ;

            return 0 ;
        }
    }

    private List<AlarmEntry> entries_ ;
}