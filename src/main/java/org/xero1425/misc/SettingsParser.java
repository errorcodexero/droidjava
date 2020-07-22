
package org.xero1425.misc;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.HashMap ;

/// \file

/// \brief This class reads setting from a settings file.
/// The application can then query the settings via their names.
public final class SettingsParser
{
    //
    // The message logger for logging messages
    //   
    private final MessageLogger logger_;

    //
    // The ID to use for SettingsParser meessages
    //
    private final int logger_id_;

    //
    // The list of defines set that should be honored while reading the
    // settings file.
    //
    private final List<String> defines_;

    //
    // The settings read from the settings file
    //
    private final Map<String, SettingsValue> values_;

    //
    // If true, we are skipping settings from the settings file due to a defined
    //
    private boolean skipping_ ;

    //
    // If skipping the level of if statements we have seen.  We must see a matching
    // number of endif statements to be through skipping.
    //
    private int skipping_level_ ;

    //
    // The name of the message for the log file
    //
    static private final String LoggerName = "settings" ;

    /// \brief create a new settings parser
    /// \param logger the message logger
    public SettingsParser(final MessageLogger logger) {
        logger_ = logger;
        logger_id_ = logger_.registerSubsystem(LoggerName);
        defines_ = new ArrayList<String>();
        values_ = new HashMap<String, SettingsValue>();
        skipping_ = false ;
    }

    /// \brief print all of the keys read and their values to the message logger
    public void dumpKeys() {
        logger_.startMessage(MessageType.Info) ;
        logger_.add("Settings Parser Keys\n") ;
        logger_.add("-----------------------------------------------------------------\n") ;
        for(String key : values_.keySet()) {
            logger_.add("    ").add(key).add("=").add(values_.get(key).toString()).add("\n") ;
        }
        logger_.add("-----------------------------------------------------------------") ;
        logger_.endMessage();
    }

    /// \brief return the message logger ID to use for SettingsParser messages
    /// \returns the message logger ID to use for SettingsParser messages    
    public int getLoggerID() {
        return logger_id_ ;
    }

    /// \brief add a define to be honored while reading the settings file
    /// \param def the define to be added to the define list
    public void addDefine(final String def) {
        if (!defines_.contains(def))
            defines_.add(def);
    }

    /// \brief return the list of defines in the defines list
    /// \returns the list of defines in the defines list
    public List<String> getDefines() {
        return defines_;
    }

    /// \brief returns true if a given define is in the defines list
    /// \param def the define to check for
    /// \returns true if a given define is in the defines list    
    public boolean isDefined(final String def) {
        return values_.containsKey(def);
    }

    /// \brief read a settings file
    /// If there is a failure while reading the settings file, there will be no settings stored in the file.
    /// \param filename the name of the file to read
    /// \returns true if the file was read sucessfully, otherwise false
    public boolean readFile(final String filename) {
        FileReader rdr ;
        boolean ret = true ;

        logger_.startMessage(MessageType.Debug, logger_id_).add("reading file '")
                .add(filename).add("'").endMessage();

        try {
            rdr = new FileReader(filename);
        }
        catch(final Exception ex)
        {
            logger_.startMessage(MessageType.Error).add("cannot open file '").add(filename) ;
            logger_.add("' for reading - ").add(ex.getMessage()).endMessage();
            return false ;
        }

        final BufferedReader br = new BufferedReader(rdr) ;

        String line ;
        int lineno = 1 ;
        while (true)
        {
            try {
                line = br.readLine() ;
                if (line == null)
                    break ;
                if (!processLine(filename, lineno, line))
                {
                    ret = false ;
                    break ;
                }
                lineno++ ;
            }
            catch(final Exception ex)
            {
                logger_.startMessage(MessageType.Error).add("cannot read line from file '").add(filename) ;
                logger_.add("' - ").add(ex.getMessage()).endMessage();
                ret = false ;
                break ;
            }
        }

        try {
            br.close() ;
            rdr.close() ;
        }
        catch(Exception ex)
        {
            logger_.startMessage(MessageType.Error).add("error closing file'").add(filename) ;
            logger_.add("' - ").add(ex.getMessage()).endMessage();
            ret = false ;            
        }

        if (ret == false)
            values_.clear() ;
        return ret ;
    }

    /// \brief return a settings value given its name.
    /// \exception throws a MissingParameterException if a settings with the given name is not found
    /// \returns the SettingsValue for the name given
    public SettingsValue get(final String name) throws MissingParameterException {
        SettingsValue v = values_.get(name) ;
        if (v == null)
            throw new MissingParameterException(name) ;

        return v ;
    }

    /// \brief returns a settings value given its name, or returns null if it does not exist
    /// \returns a settings value given its name
    public SettingsValue getOrNull(final String name) {
        return values_.get(name) ;
        // this one isn't returning null even though it's described in brief ?? //
    }    

    private SettingsValue parseValue(String name, String s) {
        SettingsValue v = null ;

        if (s.charAt(0) == 8237) {
            //
            // Copied in a value from the microsoft windows calculator.  This prepends
            // the text with a middle right to left indicator (to indicate left to right display)
            // even when not needed.  Skip this character for now.
            //
            s = s.substring(1).trim() ;
        }

        if (s.toLowerCase().equals("true"))
        {
            v = new SettingsValue(true) ;
        }
        else if (s.toLowerCase().equals("false"))
        {
            v = new SettingsValue(false) ;
        }
        else if (s.startsWith("\"") && s.endsWith("\"") && s.length() > 1)
        {
            v = new SettingsValue(s.substring(1, s.length() - 1));
        }
        else
        {
            boolean isDouble = Pattern.matches("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$", s);
            boolean isInteger = Pattern.matches("^[-+]?\\d*$", s) ;

            if (isInteger) {
                try 
                {
                    int num = Integer.parseInt(s) ;
                    v = new SettingsValue(num) ;
                }                
                catch(Exception ex)
                {
                }
            }
            else if (isDouble) {
                try {
                    double num = Double.parseDouble(s) ;
                    v = new SettingsValue(num) ;
                }
                catch(Exception ex2) {
                    logger_.startMessage(MessageType.Error) ;
                    logger_.add("cannot parse value for parameter ").addQuoted(name) ;
                    logger_.add(" the value ").add(s).add(" is not a legal value") ;
                    logger_.endMessage();
                }
            }
        }
        return v ;
    }

    private boolean processLine(String filename, int lineno, String line) {
        boolean ret = false ;

        line = line.strip() ;
        if (line.length() == 0 || line.charAt(0) == '#')
            return true ;

        if (line.startsWith("if")) {
            ret = processIf(filename, lineno, line) ;
        }
        else if (line.startsWith("endif")) {
            ret = processEndIf(filename, lineno, line) ;
        }
        else {
            if (!skipping_)
                ret = processDefine(filename, lineno, line) ;
            else
                ret = true ;
        }

        return ret ;
    }

    private boolean processIf(String filename, int lineno, String line) {
        int index = 0 ;
        String name ;

        if (skipping_) 
        {
            skipping_level_++ ;
        }
        else 
        {
            line = line.substring(2).trim() ;
            while (index < line.length())
            {
                if (Character.isWhitespace(line.charAt(index)) || line.charAt(index) == '#')
                    break ;

                index++ ;
            }
            name = line.substring(0, index) ;

            if (!defines_.contains(name)) {
                skipping_ = true ;
                skipping_level_ = 0 ;
            }
        }

        return true ;
    }

    private boolean processEndIf(String filename, int lineno, String line) {
        if (skipping_) {
            if (skipping_level_ == 0)
                skipping_ = false ;
            else
                skipping_level_-- ;
        }
        return true ;
    }

    private boolean processDefine(String filename, int lineno, String line) {
        String name, value ;
        int index = 0 ;

        //
        // Parse the name of the value from the params file
        //
        while (index < line.length())
        {
            if (Character.isWhitespace(line.charAt(index)) || line.charAt(index) == '#')
                break ;

            index++ ;
        }

        if (index == line.length() || line.charAt(index) == '#')
        {
            //
            // Either we parsed to the end of the line, or we parsed to
            // a comment.  In either case, we are missing the value
            //
            logger_.startMessage(MessageType.Error).add(filename).add(lineno).add(":") ;
            logger_.add("missing value in line").endMessage();
            return false ;
        }
        name = line.substring(0, index) ;

        // Skip past name and any white space
        line = line.substring(index).strip() ;
        if (line.length() == 0)
        {
            logger_.startMessage(MessageType.Error).add(filename).add(lineno).add(":") ;
            logger_.add("missing value in line").endMessage();
            return false ;            
        }
        index = 0 ;

        if (line.charAt(0) == '"')
        {
            index++ ;
            while (index < line.length() && line.charAt(index) != '"')
                index++ ;

            if (index == line.length())
            {
                logger_.startMessage(MessageType.Error).add(filename).add(lineno).add(":") ;
                logger_.add("missing trailing quote (\") in value").endMessage();
                return false ;                   
            }

            value = line.substring(0, index + 1) ;
        }
        else
        {
            while (index < line.length())
            {
                char ch = line.charAt(index) ;
                if (Character.isWhitespace(ch) || ch == '#')
                    break ;

                index++ ;
            }

            value = line.substring(0, index) ;
        }

        SettingsValue v = parseValue(name, value) ;
        if (v == null)
        {
            logger_.startMessage(MessageType.Error).add(filename).add(":").add(lineno).add(":") ;
            logger_.add("invalid value in line").endMessage();
            return false ;            
        }
        values_.put(name, v) ;
        return true ;
    }


}
