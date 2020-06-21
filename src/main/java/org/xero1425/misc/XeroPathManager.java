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

/// \file

/// \brief This class loads an projects all of the paths stored in path files.
/// A path is loaded given a name.  The paths directory is searched for a set of
/// files that have the given name as the base name.  These files are loaded and
/// the path can be retreived using the name provided at any future time.
///
public class XeroPathManager
{
    //
    // The message logger, for logging path manager related messages to the log file
    //
    MessageLogger logger_ ;

    //
    // The message ID for the path logger
    //
    int logger_id_ ;

    //
    // The set of paths loaded into the path manager
    //
    private Map<String, XeroPath> paths_ ;

    //
    // The base directory for finding path files
    //
    private String basedir_ ;

    //
    // The extension to append to the path name to get the data for the
    // left side of the path.
    //
    private String left_ext_ ;

    //
    // The extension to append to the path name to get the data for the
    // right side of the path.
    //
    private String right_ext_ ;

    //
    // The name of the messages for the logger
    //
    static final private String LoggerName = "pathmanager" ;

    /// \brief create the path manager
    /// \param logger the message logger
    /// \param basedir the base directory where all paths are found
    public XeroPathManager(MessageLogger logger, String basedir) {
        basedir_ = basedir ;
        paths_ = new HashMap<String, XeroPath>() ;
        logger_id_ = logger.registerSubsystem(LoggerName) ;

        setExtensions("_left.csv", "_right.csv");
    }

    /// \brief return the base directory for the path manager
    /// \returns the base directory for the path manager
    public String getBaseDir() {
        return basedir_ ;
    }

    /// \brief load a path from the path data files
    /// The path manager will look for two files named BASEDIR/name.left_ext and BASEDIR/name.right_ext
    /// where BASEDIR is the base directory specified when the path manager was created, name is the
    /// name given in thie call, and left_ext and right_ext are the extensions set in the setExtensions()
    /// call.
    /// \param name the name of the path to load
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

    /// \brief returns a path given the path name
    /// \exception MissingPathException thrown when asking for a path that does not exist, see hasPath()
    /// \param name the name of the path to return
    /// \returns a path given its name
    public XeroPath getPath(String name) throws MissingPathException {
        XeroPath p = paths_.get(name) ;
        if (p == null)
            throw new MissingPathException(name) ;

        return p ;
    }

    /// \brief returns true if the path manager has loaded a path with the name given
    /// \returns true if the path manager has loaded a path with the name given
    public boolean hasPath(String name) {
        return paths_.containsKey(name) ;
    }

    /// \brief sets the extensions for loading paths
    /// The default extension are "_left.csv" and "_right.csv".  This only needs to be called if
    /// the extension for a given path are different than this.
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

}