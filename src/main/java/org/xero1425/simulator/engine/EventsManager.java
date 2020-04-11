package org.xero1425.simulator.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;

public class EventsManager {
    public EventsManager(SimulationEngine engine) {
        engine_ = engine ;
        events_ = new ArrayList<SimulationEvent>() ;
    }

    public boolean readEventsFile(String file) {
        MessageLogger logger = engine_.getMessageLogger();

        logger.startMessage(MessageType.Info);
        logger.add("reading simulator events file ").addQuoted(file) ;
        logger.endMessage();        

        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(file));
        } catch (IOException e) {
            logger.startMessage(MessageType.Error);
            logger.add("cannot read events file ").addQuoted(file).add(" - ");
            logger.add(e.getMessage()).endMessage();
            return false;
        }

        String fulltext = new String(encoded);

        Object obj = JSONValue.parse(fulltext);
        if (!(obj instanceof JSONObject)) {
            logger.startMessage(MessageType.Error);
            logger.add("cannot read events file ").addQuoted(file).add(" - ");
            logger.add("file does not contain a JSON object").endMessage();
            return false;
        }

        JSONObject jobj = (JSONObject) obj;
        obj = jobj.get("stimulus");

        if (!(obj instanceof JSONArray)) {
            logger.startMessage(MessageType.Error);
            logger.add("cannot read events file ").addQuoted(file).add(" - ");
            logger.add("top level json object does not contains a models entry");
            return false;
        }

        JSONArray stimarray = (JSONArray) obj;

        for (int i = 0; i < stimarray.size(); i++) {
            obj = stimarray.get(i);
            if (obj instanceof JSONObject) {
                parseTimePoint((JSONObject) obj);
            }
        }

        return true;        
    }

    public int size() {
        return events_.size() ;
    }

    public SimulationEvent getFirstEvent() {
        return events_.get(0) ;
    }

    public void removeFirstEvent() {
        events_.remove(0) ;
    }

    private void parseTimePoint(JSONObject tpt) {
        Object obj ;

        if (!tpt.containsKey("time"))
            return ;

        obj = tpt.get("time") ;
        if (!(obj instanceof Double))
            return ;

        double t = (Double)obj ;

        if (tpt.containsKey("events")) {
            obj = tpt.get("events") ;
            if (obj instanceof JSONArray)
                parseSimEvents(t, (JSONArray)obj) ;
        }

        if (tpt.containsKey("asserts")) {
            obj = tpt.get("asserts") ;
            if (obj instanceof JSONArray)
                parseSimAsserts(t, (JSONArray)obj);
        }
    }

    private void parseSimEvents(double t, JSONArray evs) {
        MessageLogger logger = engine_.getMessageLogger() ;

        for(int i = 0 ; i < evs.size() ; i++) {
            Object obj = evs.get(i) ;
            if (!(obj instanceof JSONObject))
                continue ;

            JSONObject jobj = (JSONObject)obj ;

            if (!jobj.containsKey("model")) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" is missing the 'model' property") ;
                logger.endMessage();
                continue ;
            }

            if (!jobj.containsKey("instance")) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" is missing the 'instance' property") ;
                logger.endMessage();                
                continue ;
            }

            if (!jobj.containsKey("values")) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" is missing the 'values' property") ;
                logger.endMessage();                
                continue ;
            }            

            Object mobj = jobj.get("model") ;
            Object iobj = jobj.get("instance") ;
            Object vobj = jobj.get("values") ;

            if (!(mobj instanceof String)) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" has 'model' property but it is not a string") ;
                logger.endMessage();
                continue ;
            }
            
            if (!(iobj instanceof String)) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" has 'instance' property but it is not a string") ;
                logger.endMessage();
                continue ;                
            }

            if (!(vobj instanceof JSONObject)) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" has 'values' property but it is not a JSON object") ;
                logger.endMessage();
                continue ;                  
            }

            SimulationModel model = engine_.findModel((String)mobj, (String)iobj) ;
            if (model == null) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" does not have simulation model with model ") ;
                logger.addQuoted((String)mobj).add(" and instance ").addQuoted((String)iobj) ;
                logger.endMessage();

                return ;
            }

            JSONObject vjobj = (JSONObject)vobj ;
            for(Object key : vjobj.keySet()) {
                String keystr = (String)key ;
                Object evval = vjobj.get(key) ;
                SettingsValue v = null ;

                if (evval instanceof String) {
                    v = new SettingsValue((String)evval) ;
                }
                else if (evval instanceof Integer) {
                    v = new SettingsValue((Integer)evval) ;
                }
                else if (evval instanceof Long) {
                    v = new SettingsValue((Long)evval) ;
                }
                else if (evval instanceof Boolean) {
                    v = new SettingsValue((Boolean)evval) ;                        
                }
                else if (evval instanceof Double) {
                    v = new SettingsValue((Double)evval) ;  
                }
                else {
                    v = null ;
                }

                if (v != null) {
                    SimulationModelEvent ev = new SimulationModelEvent(t, model.getModelName(), model.getInstanceName(), keystr, v) ;
                    insertEvent(ev) ;
                }
            }
        }
    }

    private void parseSimAsserts(double t, JSONArray evs) {
        MessageLogger logger = engine_.getMessageLogger() ;

        for(int i = 0 ; i < evs.size() ; i++) {
            Object obj = evs.get(i) ;
            if (!(obj instanceof JSONObject))
                continue ;

            JSONObject jobj = (JSONObject)obj ;

            if (!jobj.containsKey("subsystem")) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" is missing the 'model' property") ;
                logger.endMessage();
                continue ;
            }

            if (!jobj.containsKey("property")) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" is missing the 'instance' property") ;
                logger.endMessage();                
                continue ;
            }

            if (!jobj.containsKey("value")) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" is missing the 'value' property") ;
                logger.endMessage();                
                continue ;
            }            

            Object mobj = jobj.get("subsystem") ;
            Object iobj = jobj.get("property") ;
            Object vobj = jobj.get("value") ;


            if (!(mobj instanceof String)) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" has 'model' property but it is not a string") ;
                logger.endMessage();
                continue ;
            }
            
            if (!(iobj instanceof String)) {
                logger.startMessage(MessageType.Warning) ;
                logger.add("events at index ").add(i).add(" has 'instance' property but it is not a string") ;
                logger.endMessage();
                continue ;                
            }

            SettingsValue v = null ;
            double tolerance = 1e-9 ;

            if (vobj instanceof String) {
                v = new SettingsValue((String)vobj) ;
            }
            else if (vobj instanceof Integer) {
                v = new SettingsValue((Integer)vobj) ;
            }
            else if (vobj instanceof Long) {
                v = new SettingsValue((Long)vobj) ;
            }            
            else if (vobj instanceof Boolean) {
                v = new SettingsValue((Boolean)vobj) ;                        
            }
            else if (vobj instanceof Double) {
                v = new SettingsValue((Double)vobj) ;
                if (jobj.containsKey("tolerance")) {
                    Object dobj = jobj.get("tolerance") ;
                    if (!(dobj instanceof Double)) {
                        logger.startMessage(MessageType.Warning) ;
                        logger.add("events at index ").add(i).add(" has 'tolerance' property but it is not a double - tolerance defaults to 1e-9") ;
                        logger.endMessage();                        
                    }
                    else {
                        tolerance = (Double)dobj ;
                    }
                }
            }
            else {
                v = null ;
            }

            if (v != null) {
                SimulationAssertEvent ev = new SimulationAssertEvent(t, (String)mobj, (String)iobj, v) ;
                if (v.isDouble())
                    ev.setTolerance(tolerance) ;
                insertEvent(ev) ;
            }
        }
    }

    private void insertEvent(SimulationEvent ev) {
        if (events_.size() == 0) {
            events_.add(ev) ;
        } else {
            boolean ins = false ;
            int i = 0 ;
            while (i < events_.size()) {
                if (ev.getTime() < events_.get(i).getTime()) {
                    events_.add(i, ev) ;
                    ins = true ;
                    break ;
                }

                i++ ;
            }

            if (!ins)
                events_.add(ev) ;
        }
    }

    private SimulationEngine engine_ ;
    private List<SimulationEvent> events_ ;
} ;
