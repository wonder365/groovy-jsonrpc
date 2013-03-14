package groovy.jsongrpc.util;

import groovy.lang.GroovyClassLoader;

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

}
