package org.xero1425.misc ;

public class MissingPathException extends Exception
{
    static final long serialVersionUID = 42 ;
    
    public MissingPathException(String name) {
        super("path '" + name + "' does not exist") ;
    }
}
