package org.xero1425.misc;

import java.io.File;
import java.io.FileWriter;

public class MessageDestinationFile extends MessageDestination
{
    public MessageDestinationFile(final String filename) {
        final File f = new File(filename);
        try {
            file_ = new FileWriter(f) ;
            valid_ = true ;
        }
        catch(final Exception ex) {
            valid_ = false ;
            System.err.println("cannot open log file '" + filename_ + "' - " + ex.getMessage()) ;
        }
    }

    public void displayMessage(final MessageType type, final int subsystem, final String msg) {
        if (valid_) {
            try {
                file_.write(msg) ;
                file_.write("\n") ;
                file_.flush() ;
            }
            catch(final Exception ex) {
                System.err.println("cannot write to log file '" + filename_ + "' - " + ex.getMessage()) ;
            }
        }
    }

    private String filename_;
    private FileWriter file_ ;
    private boolean valid_ ;
}
