package org.xero1425.base.controllers;

import org.xero1425.base.XeroRobot;

public abstract class BaseController
{
    public BaseController(XeroRobot robot, String name) {
        robot_ = robot ;
        name_ = name ;
    }

    public String getName() {
        return name_ ;
    }

    public abstract void init() ;

    public abstract void run() ;

    public XeroRobot getRobot() {
        return robot_ ;
    }

    private XeroRobot robot_ ;
    private String name_ ;
} ;
