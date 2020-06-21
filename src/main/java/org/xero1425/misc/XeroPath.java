package org.xero1425.misc ;

import java.util.List ;
import java.util.ArrayList ;

/// \file

/// \brief This class represents a single path to be followed by the robot drive base
/// The XeroPath object has a name and a set of X and Y data points for both the left and right sides
/// of the drivebase
public class XeroPath
{
    //
    // The name of the path
    //
    private String name_ ;

    //
    // The set of segment for the left side of the robot
    //
    private List<XeroPathSegment> left_ ;

    //
    // The set of segment for the right side of the robot
    //
    private List<XeroPathSegment> right_ ;

    /// \brief create a new path with the name given
    /// \param name the name of the path
    public XeroPath(String name) {
        name_ = name ;
        left_ = new ArrayList<XeroPathSegment>() ;
        right_ = new ArrayList<XeroPathSegment>() ;
    }

    /// \brief create a new path with the name given and the left and right data
    /// \param name the name of the path
    /// \param left the data for the left side of the drive base
    /// \param right the data for the right side of the drive base
    public XeroPath(String name, List<XeroPathSegment> left, List<XeroPathSegment> right) throws Exception {
        if (left_.size() != right_.size())
            throw new Exception("path '" + name + "' has differing sizes left vs right") ;

        name_ = name ;
        left_ = left ;
        right_ = right ;
    }

    /// \brief return the name of the path
    /// \returns the name of the path
    public String getName() {
        return name_ ;
    }

    /// \brief returns the number of data points in the path
    /// \returns the numer of data points in the path
    public int getSize() {
        return left_.size() ;
    }

    /// \brief returns the duration of the path in seconds
    /// \returns the duration of the path in seconds
    public double getDuration() {
        return getLeftSegment(left_.size() - 1).getTime() ;
    }

    /// \brief returns a single segment of the path for the left side of the robot
    /// \param index the index of the segment to return
    /// \returns a single segment of the path for the left side of the robot
    public XeroPathSegment getLeftSegment(int index) {
        return left_.get(index) ;
    }

    /// \brief returns a single segment of the path for the right side of the robot
    /// \param index the index of the segment to return
    /// \returns a single segment of the path for the right side of the robot
    public XeroPathSegment getRightSegment(int index) {
        return right_.get(index) ;
    }

    /// \brief adds a new path segment to the left adn right sides of the robot
    /// \param left the segment for the left side of the robot
    /// \param right the segment for the right side of the robot
    public void addPathSegment(XeroPathSegment left, XeroPathSegment right) {
        left_.add(left) ;
        right_.add(right) ;
    }

} ;