
package org.xero1425.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.text.DecimalFormat;
import java.util.ArrayList ;

public final class MessageLogger
{
    public static final int NOSUBSYSTEM = 0 ;

    /**
     * Create a new message logger object
     */
    public MessageLogger()
    {
        subsystems_ = new HashMap<Integer, String>() ;
        subsystem_index_ = 1 ;
        time_src_ = null ;
        destinations_ = new ArrayList<MessageDestination>() ;
        per_thread_data_ = new HashMap<Long, ThreadData>() ;

        enabled_types_ = new ArrayList<MessageType>() ;
        enabled_types_.add(MessageType.Debug) ;
        enabled_types_.add(MessageType.Info) ;
        enabled_types_.add(MessageType.Warning) ;
        enabled_types_.add(MessageType.Error) ;
        enabled_types_.add(MessageType.Fatal) ;

        enabled_subsystems_ = new ArrayList<Integer>() ;
        to_be_enabled_ = new ArrayList<String>() ;

        lock_ = new Object() ;

        format_ = new DecimalFormat("000.0000") ;
    }

    /**
     * Register a new subsystem with the message logger
     * @param name the name of the subsystem
     * @return the integer handle for message assocaited with the subsystem
     */
    public int registerSubsystem(final String name) {
        final int index = subsystem_index_++;
        subsystems_.put(index, name);

        if (to_be_enabled_.contains(name)) {
            enableLoggerID(index) ;
            to_be_enabled_.remove(name) ;
        }
        return index;
    }

    /**
     * Register a time source for the message logger
     * 
     * @param src the time source for the message logger
     */
    public void setTimeSource(final MessageTimeSource src) {
        time_src_ = src;
    }

    /**
     * Clear all message destinations for messages
     */
    public void clear() {
        destinations_.clear();
    }

    /**
     * Adds a new message destination to the message logger
     */
    public void addDestination(final MessageDestination d) {
        destinations_.add(d);
    }

    /**
     * Enable the message type given in the message logger
     * 
     * @param mt the message type to enable
     */
    public void enableMessageType(final MessageType mt) {
        if (!enabled_types_.contains(mt))
            enabled_types_.add(mt);
    }

    /**
     * Disable the message type given in the message logger
     * 
     * @param mt the message type to disable
     */
    public void disableMessageType(final MessageType mt) {
        if (enabled_types_.contains(mt))
            enabled_types_.remove(mt);
    }

    /**
     * Returns true if the message type given is enabled
     * 
     * @param mt the message type to check
     * @return true if the message type is enabled
     */
    public boolean isTypeEnabled(final MessageType mt) {
        return enabled_types_.contains(mt);
    }

    /**
     * Enable a given subsystem
     * 
     * @param handle the handle to the subsystem to disable
     */
    private void enableLoggerID(final int handle) {
        if (!enabled_subsystems_.contains(handle))
            enabled_subsystems_.add(handle);
    }

    /**
     * Enable a given subsystem
     * 
     * @param name the name of the subsystem to enable
     * @return true if the subsystem was valid, otherwise false
     */
    public boolean enableLogging(final String name) {
        boolean ret = true;

        for (final Integer key : subsystems_.keySet()) {
            final String subname = subsystems_.get(key);
            if (subname == name) {
                enableLoggerID(key);
                ret = true;
                break;
            }
        }

        if (!to_be_enabled_.contains(name))
            to_be_enabled_.add(name) ;

        return ret;
    }

    /**
     * Disable a given subsystem
     * 
     * @param handle the handle to the subsystem to disable
     */
    public void disableSubsystem(final int handle) {
        if (enabled_subsystems_.contains(handle))
            enabled_subsystems_.remove(handle);
    }

    /**
     * Disable a given subsystem
     * 
     * @param name the name of the subsystem to enable
     * @return true if the subsystem was valid, otherwise false
     */
    public boolean disableSubsystem(final String name) {
        boolean ret = false;

        for (final Integer key : subsystems_.keySet()) {
            final String subname = subsystems_.get(key);
            if (subname == name) {
                disableSubsystem(key);
                ret = true;
                break;
            }
        }

        return ret;
    }

    /**
     * Returns true if the subsystem with the given handle is enabled in the mssage
     * logger
     * 
     * @param handle the handle to the subsystem in question
     * @return true if the subsystem is enabled
     */
    public boolean isSubsystemEnabled(final int handle) {
        return enabled_subsystems_.contains(handle);
    }

    /**
     * Returns true if the subsystem with the given name is enabled
     * 
     * @param name the name of the subsystem to check
     * @return true if message for the subsystem are enabled
     */
    public boolean isSubsystemEnabled(final String name) {
        boolean ret = false;

        for (final Integer key : subsystems_.keySet()) {
            final String subname = subsystems_.get(key);
            if (subname == name) {
                ret = isSubsystemEnabled(key);
                break;
            }
        }

        return ret;
    }

    /**
     * start a new message.
     * 
     * @param mtype     the type of message
     * @param subsystem the subsystem for the message
     * @return the message logger object
     */
    public MessageLogger startMessage(final MessageType mtype, final int subsystem) {
        final ThreadData per = getPerThreadData();

        if (per.in_message_) {
            //
            // We have a nested message, someone forgot to close off the current
            // message.
            //
            per.message_.append(" DID NOT CALL ENDMESSAGE, serial = ") ;
            per.message_.append(per.serial_) ;
            endMessage();
        }

        per.serial_ = getSerial() ;
        per.in_message_ = true;
        per.message_ = new StringBuilder(80) ;
        per.type_ = mtype;
        per.subsystem_ = subsystem;
        per.enabled_ = enabled_types_.contains(per.type_) && subsystemEnabled(per.subsystem_) ;

        return this;
    }

    static private /* synchronized */ int getSerial() {
        return global_serial_++ ;
    }

    /**
     * start a message with no subsystem
     * 
     * @param mtype the message type for the message
     * @return the message logger
     */
    public MessageLogger startMessage(final MessageType mtype) {
        return startMessage(mtype, NOSUBSYSTEM);
    }

    /**
     * end a message and print if enabled
     */
    public void endMessage() {
        final ThreadData per = getPerThreadData();

        if (!per.in_message_)
            return;

        // synchronized(lock_) {
            if (per.message_.length() > 0) {
                if (enabled_types_.contains(per.type_) && subsystemEnabled(per.subsystem_)) {
                    String msgstr;
                    if (time_src_ == null) {
                        msgstr = "???.????";
                    } else {
                        msgstr = format_.format(time_src_.getTime()) ;
                    }

                    msgstr += ": " + per.type_.toString() + ": " + per.message_;
                    for (final MessageDestination dest : destinations_) {
                        dest.displayMessage(per.type_, per.subsystem_, msgstr);
                    }
                }
            }

            if (per.type_ == MessageType.Fatal) {
                for (final MessageDestination dest : destinations_) {                
                    dest.displayMessage(per.type_, per.subsystem_, "fatal error occurred - code aborting") ;
                }
                System.exit(-2);
            }
        //}

        per.message_ = null ;
        per.subsystem_ = 0;
        per.in_message_ = false;
    }

    public MessageLogger add(final String str) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_ && per.in_message_)
            per.message_.append(str) ;
        return this;
    }

    public MessageLogger add(final String name, final double value) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_) {
            per.message_.append(" ") ;
            per.message_.append(name) ;
            per.message_.append(" = ") ;
            per.message_.append(value) ;
        }

        return this;        
    }

    public MessageLogger add(final String name, final int value) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_) {
            per.message_.append(" ") ;
            per.message_.append(name) ;
            per.message_.append(" = ") ;
            per.message_.append(value) ;
        }

        return this;        
    }  
    
    public MessageLogger add(final String name, final boolean value) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_) {
            per.message_.append(" ") ;
            per.message_.append(name) ;
            per.message_.append(" = ") ;
            per.message_.append(value) ;
        }

        return this;        
    }  
    
    public MessageLogger add(final String name, final String value) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_) {
            per.message_.append(" ") ;
            per.message_.append(name) ;
            per.message_.append(" = ") ;
            per.message_.append(value) ;
        }

        return this;        
    }       

    public MessageLogger addQuoted(final String str) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_)
        {
            per.message_.append("'") ;
            per.message_.append(str) ;
            per.message_.append("'") ;
        }

        return this;
    }    

    public MessageLogger add(final char ch) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_)
            per.message_.append(ch) ;

        return this;        
    }

    public MessageLogger add(final int value) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_)
            per.message_.append(value) ;

        return this;
    }

    public MessageLogger add(final long value) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_)
            per.message_.append(value) ;

        return this;
    }

    public MessageLogger add(final boolean value) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_)
            per.message_.append(value) ;

        return this;
    }

    public MessageLogger add(final double value) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_)
            per.message_.append(value) ;

        return this;
    }

    public MessageLogger add(final float value) {
        final ThreadData per = getPerThreadData();
        if (per.enabled_&& per.in_message_)
            per.message_.append(value) ;

        return this;
    }

    private boolean subsystemEnabled(final int sub) {
        return sub == NOSUBSYSTEM || enabled_subsystems_.contains(sub);
    }

    private ThreadData getPerThreadData() {
        ThreadData per = null;

        synchronized(lock_) {
            final long id = Thread.currentThread().getId();
            if (per_thread_data_.containsKey(id))
            {
                per = per_thread_data_.get(id) ;
            }
            else
            {
                per = new ThreadData() ;
                per.in_message_ = false ;
                per_thread_data_.put(id, per) ;
            }
        }

        return per ;
    }

    private class ThreadData
    {
        public boolean in_message_ ;
        public boolean enabled_ ;
        public MessageType type_ ;
        public int subsystem_ ;
        public StringBuilder message_ ;
        public int serial_ ;
    } ;

    // The per thread data for 
    private Map<Long, ThreadData> per_thread_data_ ;

    // The set of destinations for messages
    private List<MessageDestination> destinations_ ;

    // The set of message types enabled
    private List<MessageType> enabled_types_ ;

    // The time source for messages
    private MessageTimeSource time_src_ ;

    // This is a mapping from subsystem number to subsystem name
    private Map<Integer, String> subsystems_ ;

    // The list of enabled subsytems
    private List<Integer> enabled_subsystems_ ;

    // This is the number for the next subsystem registered
    private int subsystem_index_ ;

    // The lock for the serial number
    private Object lock_ ;

    // the list of subsystem to be enabled if ethey are created
    private List<String> to_be_enabled_ ;

    // the format for the time value
    private DecimalFormat format_ ;

    // Serial number for each message
    static int global_serial_ = 1 ;
}