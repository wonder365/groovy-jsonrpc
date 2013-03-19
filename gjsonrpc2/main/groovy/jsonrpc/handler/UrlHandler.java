package groovy.jsonrpc.handler;

import groovy.jsonrpc.constant.Constant;
import groovy.jsonrpc.engine.AbstractCaller;
import groovy.jsonrpc.engine.MethodDefefine;
import groovy.jsonrpc.engine.RpcResponse;
import groovy.jsonrpc.util.GroovyLoaders;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;

/**
 * handler supports loading GROOVY through JSONRPC by file URL, when call a
 * method, must pass the real file path, the "method" field in JSONRPC would be
 * the method name exactly in the GROOVY class
 */
public class UrlHandler {
    static final Logger logger = LoggerFactory.getLogger(UrlHandler.class);
    private GroovyClassLoader cl;
    private ConcurrentHashMap<String, CompiledClass> classes = new ConcurrentHashMap<String, CompiledClass>();

    public UrlHandler() {
	super();
	this.cl = GroovyLoaders.createGroovyClassLoader("groovy.sql.Sql");
    }

    public UrlHandler(GroovyClassLoader cl) {
	super();
	this.cl = cl;
    }

    public byte[] call(final String url, final byte[] data) {
	Object jo;
	try {
	    jo = JSON.parse(data);
	} catch (Throwable t) {
	    return JSON.toJSONBytes(RpcResponse.newParseError(t));
	}
	Object ret = new UrlJsonRpcCaller(url, cl, classes).call(jo);
	return ret != null ? JSON.toJSONBytes(ret) : Constant.EMPTY_BYTES;
    }

    public String call(final String url, final String data) {
	Object jo;
	try {
	    jo = JSON.parse(data);
	} catch (Throwable t) {
	    return JSON.toJSONString(RpcResponse.newParseError(t));
	}
	Object ret = new UrlJsonRpcCaller(url, cl, classes).call(jo);
	return ret != null ? JSON.toJSONString(ret) : Constant.EMPTY_STRING;
    }

    /**
     * initialize some groovy scripts( example: utility class, constants,
     * database or other resource), if script can run, then run it
     * 
     * @param urls
     */
    public void initbase(final String... urls) {
	GroovyLoaders.initbase(this.cl, urls);
    }

    class UrlJsonRpcCaller extends AbstractCaller {

	@SuppressWarnings("unused")
	private UrlJsonRpcCaller() {

	}

	public UrlJsonRpcCaller(String url, GroovyClassLoader cl,
		ConcurrentHashMap<String, CompiledClass> classes) {
	    super();
	    this.url = url;
	    this.cl = cl;
	    this.classes = classes;
	}

	CompiledClass ccl;
	String url;
	private GroovyClassLoader cl;
	private ConcurrentHashMap<String, CompiledClass> classes;

	protected void _callbefore(final Object jo) {
	    ccl = getCompiledClass();
	}

	protected RpcResponse _beforeInvoke(Object id) {
	    if (!ccl.isok) {
		return RpcResponse.newError(id, Constant.EC_INTERNAL_ERROR,
			"rpc: compile fail " + ccl.failmsg);
	    } else {
		return null;
	    }
	}

	protected HashMap<String, MethodDefefine> _getMethods() {
	    return ccl.methods;
	}

	protected RpcResponse _callrpc(final String method, final Object id,
		final JSONObject jo) {
	    if (method.equals("rpc.recompile")) {
		return RpcResponse.newRpcRespResult(id, recompile(ccl));
	    } else if (method.equals("rpc.all")) {
		List<CompiledClass> all = new ArrayList<CompiledClass>(
			classes.values());
		return RpcResponse.newRpcRespResult(id, all);
	    }
	    return super._callrpc(method, id, jo);
	}

	private boolean recompile(CompiledClass ccl) {
	    Class<?> clazz = null;
	    Exception compilefail = null;
	    try {
		clazz = parseClass(url, false);
	    } catch (Exception e) {
		compilefail = e;
	    }
	    ccl.SetClassInfo(clazz, compilefail);
	    return compilefail == null;
	}

	private Class<?> parseClass(String url, boolean cacheable)
		throws CompilationFailedException, IOException {
	    return GroovyLoaders.parseClass(cl, new File(url), cacheable);
	}

	private CompiledClass compile(final String url) {
	    Class<?> clazz = null;
	    Exception compilefail = null;
	    try {
		clazz = parseClass(url, false);
	    } catch (Exception e) {
		compilefail = e;
	    }
	    CompiledClass ccl = new CompiledClass(url, clazz, compilefail);
	    return ccl;
	}

	private CompiledClass getCompiledClass() {
	    if (!classes.containsKey(url)) {
		CompiledClass ccl = compile(url);
		classes.put(url, ccl);
		return ccl;
	    }
	    return classes.get(url);
	}
    }

    @JSONType(orders = { "url", "classname", "isok", "compiletime", "failmsg",
	    "methods" })
    class CompiledClass {
	public String url;
	public String classname;
	// warn: transient methods for not ignore json.Serialize
	public transient HashMap<String, MethodDefefine> methods;
	public boolean isok = false;
	public String failmsg;
	public Date compiletime;

	public CompiledClass(String url, Class<?> clazz, Exception compilefail) {
	    this.url = url;
	    SetClassInfo(clazz, compilefail);
	}

	public void SetClassInfo(Class<?> clazz, Exception compilefail) {
	    if (compilefail != null) {
		this.failmsg = compilefail.getMessage();
		return;
	    }
	    this.classname = clazz.getName();
	    this.isok = true;
	    this.compiletime = new Date();
	    this.failmsg = "";
	    this.methods = new HashMap<String, MethodDefefine>();
	    MethodDefefine.collectMethods(clazz, null, this.methods);
	}
    }
}
