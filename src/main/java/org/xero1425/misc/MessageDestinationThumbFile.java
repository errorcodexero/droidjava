package org.xero1425.misc;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

public class MessageDestinationThumbFile extends MessageDestination
{
    public MessageDestinationThumbFile(final String bdir, final long timeout) {
        int index = 1;
        valid_ = false;
        final long startms = new Date().getTime();

        while (true) {
            final long now = new Date().getTime();
            if (now - startms > timeout) {
                System.err.println("timeout while opening robot log file");
                valid_ = false;
                break;
            }

            final String filename = bdir + "/logfile_" + Integer.toString(index++);
            final File f = new File(filename);
            if (!f.exists())
            {
                valid_ = true ;
                filename_ = filename ;
                try
                {
                    file_ = new FileWriter(f) ;
                }
                catch(final Exception ex)
                {
                    valid_ = false ;
                    System.err.println("cannot open log file '" + filename_ + "' - " + ex.getMessage()) ;
                }
                break ;
            }
        }
    }

    public void displayMessage(final MessageType type, final int subsystem, final String msg) {
        if (valid_) {
            try
            {
                file_.write(msg) ;
                file_.write("\n") ;
                file_.flush() ;
            }
            catch(final Exception ex)
            {
                System.err.println("cannot write to log file '" + filename_ + "' - " + ex.getMessage()) ;
            }
        }
    }

    private String filename_;
    private FileWriter file_ ;
    private boolean valid_;
}
