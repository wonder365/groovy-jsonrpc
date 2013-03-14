package groovy.jsongrpc.handler;

import groovy.jsongrpc.constant.Constant;
import groovy.jsongrpc.engine.AbstractCaller;
import groovy.jsongrpc.engine.MethodDefefine;
import groovy.jsongrpc.engine.RpcResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * handler supports Java class, these class will mapped to a "virtual class", so
 * when call from JSONRPC, the method field will be "class.method", for example:
 * register "java.lang.Math" to "my.math.", then you can call Math.abs by method
 * "my.math.bas"
 */
public class MapedHandler {
    static final Logger logger = LoggerFactory.getLogger(MapedHandler.class);

    protected ClassLoader cl;

    ConcurrentHashMap<String, ClassDef> defmap = new ConcurrentHashMap<String, ClassDef>();
    Map<String, MethodDefefine> methods = new ConcurrentHashMap<String, MethodDefefine>();

    public MapedHandler() {
	super();
	this.cl = MapedHandler.class.getClassLoader();
    }

    public MapedHandler(ClassLoader cl) {
	super();
	this.cl = cl;
    }

    public byte[] call(final byte[] data) {
	Object jo;
	try {
	    jo = JSON.parse(data);
	} catch (Throwable t) {
	    return JSON.toJSONBytes(RpcResponse.newParseError(t));
	}
	Object ret = new MapJsonRpcCaller(this).call(jo);
	return ret != null ? JSON.toJSONBytes(ret) : Constant.EMPTY_BYTES;
    }

    public String call(final String data) {
	Object jo;
	try {
	    jo = JSON.parse(data);
	} catch (Throwable t) {
	    return JSON.toJSONString(RpcResponse.newParseError(t));
	}
	Object ret = new MapJsonRpcCaller(this).call(jo);
	return ret != null ? JSON.toJSONString(ret) : Constant.EMPTY_STRING;
    }

    public boolean register(String profix, String clazzname, boolean isgroovy) {
	ClassDef classdef = defmap.get(profix);
	if (classdef == null) {
	    classdef = new ClassDef(profix, clazzname, isgroovy);
	    defmap.put(profix, classdef);
	} else {
	    classdef.isgroovy = isgroovy;
	    classdef.isok = false;
	    for (String name : classdef.methods) {
		methods.remove(name);
	    }
	}
	classdef.loadtime = new Date();
	if (isgroovy) {
	    return registerGroovy(profix, classdef, clazzname);
	}
	return registerClass(profix, classdef, clazzname);
    }

    protected boolean registerGroovy(String profix, ClassDef classdef,
	    String clazzname) {
	return false;
    }

    private boolean registerClass(String profix, ClassDef classdef,
	    String clazzname) {
	try {
	    Class<?> clazz = Class.forName(clazzname, true, cl);
	    return registerClass(profix, classdef, clazz);
	} catch (ClassNotFoundException e) {
	    return false;
	}
    }

    protected boolean registerClass(String profix, ClassDef classdef,
	    Class<?> clazz) {
	List<String> methodnames = MethodDefefine.collectMethods(clazz, profix,
		methods);
	classdef.isok = true;
	classdef.methods = new ArrayList<String>(methodnames);
	return classdef.isok;
    }

    class ClassDef {
	public ClassDef(String profix, String nameorurl, boolean isgroovy) {
	    super();
	    this.profix = profix;
	    this.nameorurl = nameorurl;
	    this.isgroovy = isgroovy;
	}

	public String profix;
	public String nameorurl;
	public Class<?> clazz;
	public boolean isgroovy;
	public boolean isok;
	public Date loadtime;
	public List<String> methods;
    }

    class MapJsonRpcCaller extends AbstractCaller {
	Map<String, ClassDef> defmap;
	Map<String, MethodDefefine> methods;
	MapedHandler handler;

	@SuppressWarnings("unused")
	private MapJsonRpcCaller() {
	}

	protected void _callbefore(final Object jo) {
	}

	protected RpcResponse _beforeInvoke(Object id) {
	    return null;
	}

	public MapJsonRpcCaller(MapedHandler handler) {
	    super();
	    this.defmap = handler.defmap;
	    this.methods = handler.methods;
	    this.handler = handler;
	}

	protected Map<String, MethodDefefine> _getMethods() {
	    return methods;
	}

	private boolean register(JSONObject jo) {
	    try {
		JSONArray ja = jo.getJSONArray(Constant.FIELD_PARAMS);
		return this.handler.register(ja.getString(0), ja.getString(1),
			ja.getBooleanValue(2));
	    } catch (Throwable t) {
		return false;
	    }
	}

	protected RpcResponse _callrpc(final String method, final Object id,
		final JSONObject jo) {
	    if (method.equals("rpc.register")) {
		return RpcResponse.newRpcRespResult(id, register(jo));
	    } else if (method.equals("rpc.all")) {
		return RpcResponse.newRpcRespResult(id, defmap.values());
	    }
	    return super._callrpc(method, id, jo);
	}
    }

}
