package groovy.jsonrpc.handler;

import groovy.jsonrpc.util.GroovyLoaders;
import groovy.lang.GroovyClassLoader;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handler supports GROOVY class and Java class, these class will mapped to a
 * "virtual class", so when call from JSONRPC, the method field will be
 * "class.method", for example: register "java.lang.Math" to "my.math.", then
 * you can call Math.abs by method "my.math.bas"; register "test/test.groovy" to
 * "my.testg.", then call "my.testg.methodname" to invoke methods in test.groovy
 */
public class GMapedHandler extends MapedHandler {
    static final Logger logger = LoggerFactory.getLogger(GMapedHandler.class);

    private GroovyClassLoader gcl;

    public GMapedHandler() {
	super();
	this.gcl = GroovyLoaders.createGroovyClassLoader("groovy.sql.Sql");
    }

    public GMapedHandler(ClassLoader cl) {
	super(cl);
	this.gcl = GroovyLoaders.createGroovyClassLoader(cl, "groovy.sql.Sql");
    }

    public GMapedHandler(ClassLoader cl, GroovyClassLoader gcl) {
	super(cl);
	this.gcl = gcl;
    }

    protected boolean registerGroovy(String profix, ClassDef classdef,
	    String clazzname) {
	try {
	    Class<?> clazz = gcl.parseClass(new File(clazzname));
	    return registerClass(profix, classdef, clazz);
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * initialize some groovy scripts( example: utility class, constants,
     * database or other resource), if script can run, then run it
     * 
     * @param urls
     */
    public void initbase(final String... urls) {
	GroovyLoaders.initbase(this.gcl, urls);
    }
}
