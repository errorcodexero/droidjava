
package org.xero1425.misc;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.HashMap ;

public final class SettingsParser
{
    static public final String LoggerName = "settings" ;

    public SettingsParser(final MessageLogger logger) {
        logger_ = logger;
        logger_id_ = logger_.registerSubsystem(LoggerName);
        defines_ = new ArrayList<String>();
        values_ = new HashMap<String, SettingsValue>();
        skipping_ = false ;
    }

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

    public int getLoggerID() {
        return logger_id_ ;
    }

    public void addDefine(final String def) {
        if (!defines_.contains(def))
            defines_.add(def);
    }

    public List<String> getDefines() {
        return defines_;
    }

    public boolean isDefined(final String def) {
        return values_.containsKey(def);
    }

    public boolean readFile(final String filename) {
        FileReader rdr ;
        boolean ret = true ;

        logger_.startMessage(MessageType.Debug, logger_id_).add("reading file '").add(filename).add("'")
                .endMessage();

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
        return ret ;
    }

    public SettingsValue get(final String name) throws MissingParameterException {
        SettingsValue v = values_.get(name) ;
        if (v == null)
            throw new MissingParameterException(name) ;

        return v ;
    }

    public SettingsValue getOrNull(final String name) {
        return values_.get(name) ;
    }    

    private SettingsValue parseValue(String name, String s) {
        SettingsValue v = null ;

        if (s.charAt(0) == 8237) {
            //
            // Copied in a value from the microsoft windows calculator.  This prepends
            // the text with a hiddle right to left indicator (to indicate left to right display)
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
            boolean isInteger = Pattern.matches("^\\d*$", s) ;

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

    public Set<String> getKeys() {
        return values_.keySet() ;
    }

    private final MessageLogger logger_;
    private final int logger_id_;
    private final List<String> defines_;
    private final Map<String, SettingsValue> values_;
    private boolean skipping_ ;
    private int skipping_level_ ;
}
