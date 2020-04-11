package org.xero1425.misc ;



public class SettingsValue
{
    public enum SettingsType
    {
        Integer,
        Double,
        Boolean,
        String
    } ;

    public SettingsValue(final int v) {
        ivalue_ = v;
        type_ = SettingsType.Integer;
    }

    public SettingsValue(final long v) {
        ivalue_ = (int)v ;
        type_ = SettingsType.Integer ;
    }

    public SettingsValue(final double v) {
        dvalue_ = v;
        type_ = SettingsType.Double;
    }

    public SettingsValue(final boolean v) {
        bvalue_ = v;
        type_ = SettingsType.Boolean;
    }

    public SettingsValue(final String v) {
        svalue_ = v ;
        type_ = SettingsType.String ;
    }

    public SettingsType getType() {
        return type_ ;
    }

    public boolean isInteger() {
        return type_ == SettingsType.Integer ;
    }

    public boolean isDouble() {
        return type_ == SettingsType.Double ;
    }

    public boolean isBoolean() {
        return type_ == SettingsType.Boolean ;
    }

    public boolean isString() {
        return type_ == SettingsType.String ;
    }

    public int getInteger() throws BadParameterTypeException {
        if (type_ != SettingsType.Integer)
            throw new BadParameterTypeException(SettingsType.Integer, type_) ;

        return ivalue_ ;
    }

    public double getDouble() throws BadParameterTypeException {
        if (type_ != SettingsType.Double && type_ != SettingsType.Integer)
            throw new BadParameterTypeException(SettingsType.Double, type_) ;

        if (type_ == SettingsType.Integer)
            return (double)ivalue_ ;
            
        return dvalue_ ;
    }
    
    public boolean getBoolean() throws BadParameterTypeException {
        if (type_ != SettingsType.Boolean)
            throw new BadParameterTypeException(SettingsType.Boolean, type_) ;
        return bvalue_ ;
    }
    
    public String getString() throws BadParameterTypeException {
        if (type_ != SettingsType.String)
            throw new BadParameterTypeException(SettingsType.String, type_) ;
        return svalue_ ;
    }    

    public String toString() {
        String ret = "[" ;
        ret += type_.toString() ;
        ret += " " ;
        switch(type_) {
            case Integer:
                ret += Integer.toString(ivalue_) ;
                break; 

            case Double:
                ret += Double.toString(dvalue_) ;
                break ;

            case Boolean:
                ret += Boolean.toString(bvalue_) ;
                break ;

            case String:
                ret += "'" + svalue_ + "'" ;
                break ;
        }
        ret += "]" ;
        return ret ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false ;

        if (!(obj instanceof SettingsValue))
            return false ;

        SettingsValue other = (SettingsValue)obj ;
        if (other.type_ != type_)
            return false ;

        boolean ret = false ;

        switch(type_) {
            case Integer:
                ret = (ivalue_ == other.ivalue_) ;
                break ;
            case Double:
                ret = (Math.abs(dvalue_ - other.dvalue_) < 1e-6) ;
            case String:
                ret = svalue_.equals(other.svalue_) ;
                break ;
            case Boolean:
                ret = (bvalue_ == other.bvalue_) ;
                break ;
        }

        return ret ;
    }

    private SettingsType type_ ;
    private int ivalue_ ;
    private double dvalue_ ;
    private boolean bvalue_ ;
    private String svalue_ ;
} ;