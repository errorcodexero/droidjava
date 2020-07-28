package org.xero1425.base.actions ;

import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType; 

public abstract class Action
{
    public static final String LoggerName = "action" ;
    
    private boolean done_ ;
    private MessageLogger logger_ ;
    private int id_ ;
    private static int current_id_ = 0 ;
    private static int logger_id_ = -1 ;

    public Action(MessageLogger logger) {
        logger_ = logger ;
        id_ = current_id_++ ;
        logger_id_ = getLoggerID(logger) ;
    }

    public void start() throws Exception {
        logger_.startMessage(MessageType.Debug, logger_id_) ;
        logger_.add("starting action: ").add(id_) ;
        logger_.add(", action ").add(toString(0)).endMessage();
        done_ = false ;
    }

    public void run() throws Exception {
    }

    public boolean isDone() {
        return done_ ;
    }

    public int getID() {
        return id_ ;
    }

    public abstract String toString(int indent) ;

    public MessageLogger getMessageLogger() {
        return logger_ ;
    }

    public void cancel() {
        if (!isDone()) {
            logger_.startMessage(MessageType.Debug, logger_id_) ;
            logger_.add("canceling action: ").add(id_) ;
            logger_.add(", action ").add(toString(0)).endMessage();
            done_ = true ;
        }
    }

    public static int getLoggerID(MessageLogger logger) {
        if (logger_id_ == -1)
            logger_id_ = logger.registerSubsystem(LoggerName) ;

        return logger_id_ ;
    }

    protected void setDone() {
        logger_.startMessage(MessageType.Debug, logger_id_) ;
        logger_.add("completing action: ").add(id_) ;
        logger_.add(", action ").add(toString(0)).endMessage();        
        done_ = true ;
    }

    protected String spaces(int n) {
        StringBuilder str = new StringBuilder() ;

        for(int i = 0 ; i < n ; i++)
            str.append(' ') ;

        return str.toString() ;
    }
} ;
