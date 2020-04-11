package org.xero1425.base.actions;

import java.util.List;

import org.xero1425.misc.MessageLogger;

public abstract class ActionGroup extends Action {
    public ActionGroup(MessageLogger logger) {
        super(logger) ;
    }

    public abstract void getAllChildren(List<Action> output) ;
}