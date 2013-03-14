package groovy.jsongrpc.handler;

import groovy.jsongrpc.constant.Constant;
import groovy.jsongrpc.engine.AbstractCaller;
import groovy.jsongrpc.engine.MethodDefefine;
import groovy.jsongrpc.engine.RpcResponse;
import groovy.jsongrpc.util.GroovyLoaders;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
		clazz = cl.parseClass(new File(ccl.url));
	    } catch (Exception e) {
		compilefail = e;
	    }
	    ccl.SetClassInfo(clazz, compilefail);
	    return compilefail == null;
	}

	private CompiledClass compile(final String url) {
	    Class<?> clazz = null;
	    Exception compilefail = null;
	    try {
		clazz = cl.parseClass(new File(url));
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
