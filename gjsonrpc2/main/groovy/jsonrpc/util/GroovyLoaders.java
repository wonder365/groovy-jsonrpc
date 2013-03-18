package groovy.jsonrpc.util;

import java.io.File;
import java.io.IOException;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

public class GroovyLoaders {
    private GroovyLoaders() {
    }

    public static GroovyClassLoader createGroovyClassLoader(ClassLoader cl,
	    String... imps) {
	ImportCustomizer importCustomizer = new ImportCustomizer();
	importCustomizer.addStaticStars(Preconditions.class.getName());
	for (String imp : imps) {
	    if (imp.endsWith(".*")) {
		importCustomizer.addStarImports(imp.substring(0,
			imp.length() - 3));
	    } else {
		importCustomizer.addImports(imp);
	    }
	}
	CompilerConfiguration conf = new CompilerConfiguration();
	conf.addCompilationCustomizers(importCustomizer);
	ClassLoader baseloader = (cl == null) ? GroovyLoaders.class
		.getClassLoader() : cl;
	return new GroovyClassLoader(baseloader, conf);
    }

    public static GroovyClassLoader createGroovyClassLoader(String... imps) {
	return createGroovyClassLoader(null, imps);
    }

    public static void _initbase(GroovyClassLoader cl, final String url) {
	try {
	    Class<?> clazz = cl.parseClass(new File(url));
	    if (Script.class.isAssignableFrom(clazz)) {
		// treat it just like a script if it is one
		Script script = null;
		try {
		    script = (Script) clazz.newInstance();
		} catch (InstantiationException e) {
		    // ignore instantiation errors,, try to do main
		} catch (IllegalAccessException e) {
		    // ignore instantiation errors, try to do main
		}
		if (script != null) {
		    script.run();
		}
	    }
	} catch (CompilationFailedException e) {
	    throw new IllegalStateException(e);
	} catch (IOException e) {
	    throw new IllegalStateException(e);
	}
    }

    public static void initbase(GroovyClassLoader cl, final String... urls) {
	for (String s : urls) {
	    _initbase(cl, s);
	}
    }
}
