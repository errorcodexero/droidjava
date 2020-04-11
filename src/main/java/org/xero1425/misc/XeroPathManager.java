package org.xero1425.misc;

import java.util.Map;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser ;
import org.apache.commons.csv.CSVRecord ;

public class XeroPathManager
{
    static final public String LoggerName = "pathmanager" ;

    public XeroPathManager(MessageLogger logger, String basedir) {
        basedir_ = basedir ;
        paths_ = new HashMap<String, XeroPath>() ;
        logger_id_ = logger.registerSubsystem(LoggerName) ;
    }

    public String getBaseDir() {
        return basedir_ ;
    }

    public boolean loadPath(String name) {
        String filename = null ;
        Reader lrdr, rrdr ;

        try {
            filename = basedir_ + "/" + name + left_ext_ ;
            lrdr = Files.newBufferedReader(Paths.get(filename)) ;
        }
        catch(Exception ex) {
            logger_.startMessage(MessageType.Error) ;
            logger_.add("cannot load path file (left) '").add(filename).add("' - ").add(ex.getMessage()) ;
            logger_.endMessage();
            return false ;
        }

        try {
            filename = basedir_ + "/" + name + right_ext_ ;
            rrdr = Files.newBufferedReader(Paths.get(filename)) ;
        }
        catch(Exception ex) {
            try {
                lrdr.close() ;
            }
            catch(Exception ex2) {
            }
            logger_.startMessage(MessageType.Error) ;
            logger_.add("cannot load path file (right)'").add(filename).add("' - ").add(ex.getMessage()) ;
            logger_.endMessage();
            return false ;
        }        

        CSVParser lparser = null ;
        CSVParser rparser = null ;

        try {
            lparser = new CSVParser(lrdr, CSVFormat.DEFAULT) ;
            rparser = new CSVParser(rrdr, CSVFormat.DEFAULT) ;
        }
        catch(Exception ex) {
            try {
                if (lparser != null)
                    lparser.close() ;
                else
                    lrdr.close() ;

                if (rparser != null)
                    rparser.close() ;
                else
                    rrdr.close() ;
            }
            catch(Exception ex2) {                
            }

            logger_.startMessage(MessageType.Error) ;
            logger_.add("cannot load path '").add(name).add("' - ").add(ex.getMessage()) ;
            logger_.endMessage();                
        }

        Iterator<CSVRecord> liter = lparser.iterator() ;
        Iterator<CSVRecord> riter = rparser.iterator() ;
        XeroPath path = new XeroPath(name) ;
        boolean first = true ;

        while (liter.hasNext() && riter.hasNext())
        {
            CSVRecord lrec = liter.next() ;
            CSVRecord rrec = riter.next() ;

            if (first)
            {
                first = false ;
                continue ;
            }

            if (lrec.size() != 8)
            {
                logger_.startMessage(MessageType.Error) ;
                logger_.add("cannot load path '").add(name) ;
                logger_.add("' - left file contains invalid number of columns, line") ;
                logger_.add(lrec.getRecordNumber()) ;
                logger_.endMessage();   
                return false ;
            }

            if (rrec.size() != 8)
            {
                logger_.startMessage(MessageType.Error) ;
                logger_.add("cannot load path '").add(name) ;
                logger_.add("' - right file contains invalid number of columns, line") ;
                logger_.add(lrec.getRecordNumber()) ;
                logger_.endMessage();   
                return false ;
            }

            XeroPathSegment lseg, rseg ;
            try {
                lseg = parseCSVRecord(lrec) ;
            }
            catch(Exception ex) {
                logger_.startMessage(MessageType.Error) ;
                logger_.add("cannot load path '").add(name) ;
                logger_.add("' - left file contains invalid floating point number, line") ;
                logger_.add(lrec.getRecordNumber()) ;
                logger_.endMessage();   
                return false ;
            }

            try {
                rseg = parseCSVRecord(lrec) ;
            }
            catch(Exception ex) {
                logger_.startMessage(MessageType.Error) ;
                logger_.add("cannot load path '").add(name) ;
                logger_.add("' - right file contains invalid floating point number, line") ;
                logger_.add(lrec.getRecordNumber()) ;
                logger_.endMessage();   
                return false ;
            }

            path.addPathSegment(lseg, rseg);
        }

        if (liter.hasNext() || riter.hasNext())
        {
            logger_.startMessage(MessageType.Error) ;
            logger_.add("cannot load path '").add(name) ;
            logger_.add("' - left and right files contains differing number of segments") ;
            logger_.endMessage();   
            return false ;
        }

        paths_.put(name, path) ;
        return true ;
    }

    public boolean hadPath(String name) {
        return paths_.containsKey(name) ;
    }

    public XeroPath getPath(String name) throws MissingPathException {
        XeroPath p = paths_.get(name) ;
        if (p == null)
            throw new MissingPathException(name) ;

        return p ;
    }

    public boolean hasPath(String name) {
        return paths_.containsKey(name) ;
    }

    public void setExtensions(String left, String right) {
        left_ext_ = left ;
        right_ext_ = right ;
    }

    private XeroPathSegment parseCSVRecord(CSVRecord r) throws NumberFormatException {
        double time, x, y, pos, vel, accel, jerk, heading ;

        time = Double.parseDouble(r.get(0)) ;
        x = Double.parseDouble(r.get(1)) ;
        y = Double.parseDouble(r.get(2)) ;
        pos = Double.parseDouble(r.get(3)) ;
        vel = Double.parseDouble(r.get(4)) ;
        accel = Double.parseDouble(r.get(5)) ;
        jerk = Double.parseDouble(r.get(6)) ;
        heading = Double.parseDouble(r.get(7)) ;

        return new XeroPathSegment(time, x, y, pos, vel, accel, jerk, heading) ;
    }

    MessageLogger logger_ ;
    int logger_id_ ;
    private Map<String, XeroPath> paths_ ;
    private String basedir_ ;
    private String left_ext_ ;
    private String right_ext_ ;
}