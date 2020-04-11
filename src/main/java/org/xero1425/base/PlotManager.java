package org.xero1425.base ;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableEntry ;
import java.util.Map ;
import java.util.HashMap ;

class PlotManager
{
    static private String CompleteEntry = "complete" ;
    static private String PointsEntry = "points" ;
    static private String ColumnsEntry = "columns" ;
    static private String DataEntry = "data" ;

    protected PlotManager(String key)
    {
        plots_ = new HashMap<Integer, PlotInfo>() ;
        next_plot_id_ = 0 ;
        plot_table_ = key ;
        enabled_ = false ;
    }

    public void enable(boolean b) {
        enabled_ = b ;
    }

    int initPlot(String name)
    {
        if (!enabled_ || DriverStation.getInstance().isFMSAttached())
            return -1 ;

        for(int key : plots_.keySet())
        {
            if (plots_.get(key).name_ == name)
                return key ;
        }

        PlotInfo info = new PlotInfo(name, next_plot_id_++) ;
        plots_.put(info.index_, info) ;

        return info.index_ ;
    }

    void startPlot(int id, String[] cols)
    {
        if (!enabled_ || DriverStation.getInstance().isFMSAttached() || !plots_.containsKey(id))
            return ;
        
        PlotInfo info = plots_.get(id) ;
        info.cols_ = cols.length ;
        info.index_ = 0 ;

        NetworkTableInstance inst = NetworkTableInstance.getDefault() ;
        NetworkTable table = inst.getTable(getKeyForPlot(id)) ;
        NetworkTableEntry entry ;
        
        entry = table.getEntry(ColumnsEntry) ;
        entry.setStringArray(cols) ;

        entry = table.getEntry(PointsEntry) ;
        entry.setNumber(0) ;

        entry = table.getEntry(CompleteEntry) ;
        entry.setBoolean(false) ;

        table.delete(DataEntry) ;
    }

    void addPlotData(int id, Double[] data)
    {
        if (!enabled_ || DriverStation.getInstance().isFMSAttached() || !plots_.containsKey(id))
            return ;
            
        PlotInfo info = plots_.get(id) ;
        if (data.length == info.cols_)
        {
            NetworkTableInstance inst = NetworkTableInstance.getDefault() ;
            NetworkTable table = inst.getTable(getKeyForPlot(id)) ;
            NetworkTableEntry entry = table.getEntry(DataEntry + "/" + Integer.toString(info.index_)) ;
            entry.setNumberArray(data) ;
            entry = table.getEntry(PointsEntry) ;
            info.index_++ ;
            entry.setNumber(info.index_) ;
        }
    }

    void endPlot(int id)
    {
        if (!enabled_ || DriverStation.getInstance().isFMSAttached() || !plots_.containsKey(id))
            return ;
            
        NetworkTableInstance inst = NetworkTableInstance.getDefault() ;
        NetworkTable table = inst.getTable(getKeyForPlot(id)) ;
        NetworkTableEntry entry = table.getEntry(CompleteEntry) ;
        entry.setBoolean(true) ;
    }

    private String getKeyForPlot(int id)
    {
        if (!plots_.containsKey(id))
            return null ;

        PlotInfo info = plots_.get(id) ;
        return plot_table_ + "/" + info.name_ ;
    }

    private class PlotInfo
    {
        PlotInfo(String name, int index) {
            name_ = name ;
            index_ = index ;
        }
        public String name_ ;
        public int cols_ ;
        public int index_ ;
    } ;

    int next_plot_id_ ;
    String plot_table_ ;
    Map<Integer, PlotInfo> plots_ ;
    boolean enabled_ ;
} ;