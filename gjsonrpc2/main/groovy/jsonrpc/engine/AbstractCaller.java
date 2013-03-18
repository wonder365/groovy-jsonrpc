package groovy.jsonrpc.engine;

import groovy.jsonrpc.constant.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * the main JSONRPC process flow:
 * <p>
 * http://www.jsonrpc.org/specification
 * </p>
 */
public abstract class AbstractCaller {
    static final Logger logger = LoggerFactory.getLogger(AbstractCaller.class);

    /**
     * the first step of call, maybe a initialze ( if needed )
     * 
     * @param jo
     */
    protected abstract void _callbefore(final Object jo);

    /**
     * just do this before invoke the real method, maybe a check ( if needed )
     * 
     * @param id
     * @return null (normally), an error when something fails
     */
    protected abstract RpcResponse _beforeInvoke(Object id);

    /**
     * get the method defines, just used by RPC.CALL
     * 
     * @return Map<methodname, MethodDefefine>
     */
    protected abstract Map<String, MethodDefefine> _getMethods();

    public Object call(final Object jo) {
	_callbefore(jo);
	if (jo instanceof JSONArray) {
	    return callArray((JSONArray) jo);
	} else if (jo instanceof JSONObject) {
	    return callone((JSONObject) jo);
	} else {
	    return RpcResponse.newError(null, Constant.EC_INVALID_REQUEST,
		    Constant.MSG_INVALID_REQUEST + "not a Request object");
	}
    }

    /**
     * process a JSONRPC batch
     * 
     * @param ja
     *            batch request objects
     * @return batch results (null when all request are notify)
     */
    private Object callArray(final JSONArray ja) {
	if (ja.size() == 0)
	    return RpcResponse.newError(null, Constant.EC_INVALID_REQUEST,
		    Constant.MSG_INVALID_REQUEST + "empty batch");
	// batch process
	List<RpcResponse> resp = new ArrayList<RpcResponse>();
	for (Object jo : ja) {
	    RpcResponse respt;
	    if (jo instanceof JSONObject) {
		respt = callone((JSONObject) jo);
	    } else {
		respt = RpcResponse.newError(null, Constant.EC_INVALID_REQUEST,
			Constant.MSG_INVALID_REQUEST + "not a Request object");
	    }
	    if (respt != null) {
		resp.add(respt);
	    }
	}
	return resp.size() > 0 ? resp : null;
    }

    /**
     * call one request (checked notify)
     * 
     * @param jo
     * @return response object ( null when notify )
     */
    private RpcResponse callone(JSONObject jo) {
	Object id = jo.get(Constant.FIELD_ID);
	RpcResponse respchk = checkone(jo, id);
	if (respchk != null) {
	    return respchk;
	}
	boolean isnotify = !jo.containsKey(Constant.FIELD_ID);
	RpcResponse resp = callone(jo, id);

	return isnotify ? null : resp;
    }

    private RpcResponse checkone(JSONObject jo, Object id) {
	// check 1: jsonrpc
	if (!jo.containsKey(Constant.FIELD_JSONRPC)) {
	    return RpcResponse.newError(id, Constant.EC_INVALID_REQUEST,
		    Constant.MSG_INVALID_REQUEST + "jsonrpc field not found");
	}
	String jsonrpc = jo.getString(Constant.FIELD_JSONRPC);
	if ((jsonrpc == null) || !jsonrpc.equals(Constant.VERSION)) {
	    return RpcResponse.newError(id, Constant.EC_INVALID_REQUEST,
		    Constant.MSG_INVALID_REQUEST + "jsonrpc must "
			    + Constant.VERSION);
	}
	// check 2: method
	if (!jo.containsKey(Constant.FIELD_METHOD)) {
	    return RpcResponse.newError(id, Constant.EC_INVALID_REQUEST,
		    Constant.MSG_INVALID_REQUEST + "method field not found");
	}

	String methodname = jo.getString(Constant.FIELD_METHOD);
	if ((methodname == null) || (methodname.equals(""))) {
	    return RpcResponse.newError(id, Constant.EC_INVALID_REQUEST,
		    Constant.MSG_INVALID_REQUEST + "method field empty");
	}
	return null;
    }

    /**
     * call one request object, but not check if it is a notify or not
     * 
     * @param jo
     * @param id
     * @return response object ( notify will return a response too )
     */
    private RpcResponse callone(JSONObject jo, Object id) {
	String methodname = jo.getString(Constant.FIELD_METHOD);

	if (methodname.startsWith("rpc.")) {
	    return _callrpc(methodname, id, jo);
	}

	RpcResponse resp1 = _beforeInvoke(id);
	if (resp1 != null)
	    return resp1;

	RpcResponse resp = MethodDefefine.callmethod(_getMethods(), methodname,
		id, jo);
	return resp;
    }

    /**
     * implement rpc.call, for some useful command
     * 
     * @param method
     * @param id
     * @param jo
     * @return rpc.call result
     */
    protected RpcResponse _callrpc(final String method, final Object id,
	    final JSONObject jo) {
	if (method.equals("rpc.ls")) {
	    Map<String, MethodDefefine> methods = _getMethods();
	    return RpcResponse.newRpcRespResult(id, methods.keySet());
	} else if (method.equals("rpc.ll")) {
	    Map<String, MethodDefefine> methods = _getMethods();
	    return RpcResponse.newRpcRespResult(id, methods);
	} else {
	    return RpcResponse.newError(id, Constant.EC_METHOD_NOT_FOUND,
		    "Method not found");
	}
    }
}
