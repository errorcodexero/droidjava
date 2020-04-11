package org.xero1425.misc ;

import java.util.List ;
import java.util.ArrayList ;

public class XeroPath
{
    public XeroPath(String name) {
        name_ = name ;
        left_ = new ArrayList<XeroPathSegment>() ;
        right_ = new ArrayList<XeroPathSegment>() ;
    }

    public XeroPath(String name, List<XeroPathSegment> left, List<XeroPathSegment> right) throws Exception {
        if (left_.size() != right_.size())
            throw new Exception("path '" + name + "' has differing sizes left vs right") ;

        name_ = name ;
        left_ = left ;
        right_ = right ;
    }

    public String getName() {
        return name_ ;
    }

    public int getSize() {
        return left_.size() ;
    }

    public XeroPathSegment getLeftSegment(int index) {
        return left_.get(index) ;
    }

    public XeroPathSegment getRightSegment(int index) {
        return right_.get(index) ;
    }

    public void addPathSegment(XeroPathSegment left, XeroPathSegment right) {
        left_.add(left) ;
        right_.add(right) ;
    }

    private String name_ ;
    private List<XeroPathSegment> left_ ;
    private List<XeroPathSegment> right_ ;
} ;