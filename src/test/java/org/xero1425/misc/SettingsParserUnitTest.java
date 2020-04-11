package org.xero1425.misc ;

import org.junit.*;

import org.xero1425.misc.SettingsValue.SettingsType;

public class SettingsParserUnitTest
{
    @Before
    public void init() {
        if (logger_ == null) {
            logger_ = new MessageLogger() ;

            //
            // Uncomment the lines below to write log files
            //

            //MessageDestinationThumbFile dest = new MessageDestinationThumbFile("logs", 10000) ;
            //logger_.addDestination(dest);
        }
    }

    private SettingsParser createParser() {
        SettingsParser p = new SettingsParser(logger_) ;
        return p ;
    }

    @Test
    public void testSimpleFile() {
        SettingsValue v ;

        logger_.startMessage(MessageType.Info).add("Starting 'testSimpleFile'").endMessage(); ;

        SettingsParser parser = createParser() ;
        boolean b = parser.readFile("src/test/java/org/xero1425/misc/param1") ;

        Assert.assertEquals(b, true);

        Assert.assertEquals(parser.isDefined("a:bool"), true) ;
        v = parser.getOrNull("a:bool") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.Boolean) ;
        try {
            Assert.assertEquals(v.getBoolean(), true) ;
        }
        catch(Exception ex) {
        }

        Assert.assertEquals(parser.isDefined("b:integer"), true) ;
        v = parser.getOrNull("b:integer") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.Integer) ;
        try {
            Assert.assertEquals(v.getInteger(), 1) ;
        }
        catch(Exception ex) {
        }
        
        Assert.assertEquals(parser.isDefined("c:double"), true) ;
        v = parser.getOrNull("c:double") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.Double) ;
        try {
            Assert.assertEquals(v.getDouble(), 3.14, 0.00001) ;
        }
        catch(Exception ex) {
        }
        
        Assert.assertEquals(parser.isDefined("d:string"), true) ;
        v = parser.getOrNull("d:string") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.String) ;
        try {
            Assert.assertEquals(v.getBoolean(), "string") ;
        }
        catch(Exception ex) {
        }        
    }

    @Test
    public void testIfDefFileFirst() {
        SettingsValue v ;

        logger_.startMessage(MessageType.Info).add("Starting 'testIfDefFile - FIRST'").endMessage(); ;

        SettingsParser parser = createParser() ;
        parser.addDefine("FIRST");
        boolean b = parser.readFile("src/test/java/org/xero1425/misc/param2") ;

        Assert.assertEquals(b, true);

        Assert.assertEquals(parser.isDefined("a:bool"), true) ;
        v = parser.getOrNull("a:bool") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.Boolean) ;
        try {
            Assert.assertEquals(v.getBoolean(), true) ;
        }
        catch(Exception ex) {
        }

        Assert.assertEquals(parser.isDefined("b:integer"), true) ;
        v = parser.getOrNull("b:integer") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.Integer) ;
        try {
            Assert.assertEquals(v.getInteger(), 1) ;
        }
        catch(Exception ex) {
        }
        
        Assert.assertEquals(parser.isDefined("c:double"), true) ;
        v = parser.getOrNull("c:double") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.Double) ;
        try {
            Assert.assertEquals(v.getDouble(), 3.14, 0.00001) ;
        }
        catch(Exception ex) {
        }
        
        Assert.assertEquals(parser.isDefined("d:string"), true) ;
        v = parser.getOrNull("d:string") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.String) ;
        try {
            Assert.assertEquals(v.getBoolean(), "string") ;
        }
        catch(Exception ex) {
        }        
    }

    @Test
    public void testIfDefFileSecond() {
        SettingsValue v ;

        logger_.startMessage(MessageType.Info).add("Starting 'testIfDefFile - SECOND'").endMessage(); ;

        SettingsParser parser = createParser() ;
        parser.addDefine("SECOND");
        boolean b = parser.readFile("src/test/java/org/xero1425/misc/param2") ;

        Assert.assertEquals(b, true);

        Assert.assertEquals(parser.isDefined("a:bool"), true) ;
        v = parser.getOrNull("a:bool") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.Boolean) ;
        try {
            Assert.assertEquals(v.getBoolean(), false) ;
        }
        catch(Exception ex) {
        }

        Assert.assertEquals(parser.isDefined("b:integer"), true) ;
        v = parser.getOrNull("b:integer") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.Integer) ;
        try {
            Assert.assertEquals(v.getInteger(), 13) ;
        }
        catch(Exception ex) {
        }
        
        Assert.assertEquals(parser.isDefined("c:double"), true) ;
        v = parser.getOrNull("c:double") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.Double) ;
        try {
            Assert.assertEquals(v.getDouble(), 42.42, 0.00001) ;
        }
        catch(Exception ex) {
        }
        
        Assert.assertEquals(parser.isDefined("d:string"), true) ;
        v = parser.getOrNull("d:string") ;
        Assert.assertNotNull(v);
        Assert.assertEquals(v.getType(), SettingsType.String) ;
        try {
            Assert.assertEquals(v.getBoolean(), "ball-of-twine") ;
        }
        catch(Exception ex) {
        }        
    }    

    private MessageLogger logger_ = null ;
}