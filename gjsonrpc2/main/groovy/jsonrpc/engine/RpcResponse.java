package groovy.jsonrpc.engine;

import groovy.jsonrpc.constant.Constant;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * JSONRPC response object defines
 * <p>
 * <b>Warning:</b> if a field is set to SerializerFeature.WriteMapNullValue, it
 * must have a getter, otherwise fast-json will fail!!!
 * </p>
 */
@JSONType(orders = { "jsonrpc", "id" })
public class RpcResponse {
    public String jsonrpc = Constant.VERSION;
    @JSONField(serialzeFeatures = { SerializerFeature.WriteMapNullValue })
    public Object id;

    public static RpcResponse newRpcRespResult(Object id, Object result) {
	RpcResponse.RpcRespResult ret = new RpcRespResult();
	ret.id = id;
	ret.result = result;
	return ret;
    }

    public static RpcResponse newParseError(Throwable t) {
	return newError(null, Constant.EC_PARSE_ERROR, Constant.MSG_PARSE_ERROR
		+ t.getMessage());
    }

    public static RpcResponse newError(Object id, int code, String message) {
	RpcRespError resp = new RpcRespError();
	resp.id = id;
	RpcResponse.RpcError error = resp.error;
	error.code = code;
	error.message = message;
	return resp;
    }

    public static RpcResponse newError(Object id, int code, String message,
	    Object data) {
	RpcRespError resp = new RpcRespError();
	resp.id = id;
	RpcResponse.RpcError error = resp.error;
	error.code = code;
	error.message = message;
	error.data = data;
	return resp;
    }

    @JSONType(orders = { "jsonrpc", "id", "result" })
    public static class RpcRespResult extends RpcResponse {
	@JSONField(serialzeFeatures = { SerializerFeature.WriteMapNullValue })
	public Object result;

	public Object getResult() {
	    return result;
	}

	public void setResult(Object result) {
	    this.result = result;
	}
    }

    @JSONType(orders = { "code", "message", "data" })
    public static class RpcError {
	public int code;
	public String message;
	public Object data;

	public Object getData() {
	    return data;
	}

	public void setData(Object data) {
	    this.data = data;
	}

	public int getCode() {
	    return code;
	}

	public void setCode(int code) {
	    this.code = code;
	}

	public String getMessage() {
	    return message;
	}

	public void setMessage(String message) {
	    this.message = message;
	}
    }

    @JSONType(orders = { "jsonrpc", "id", "error" })
    public static class RpcRespError extends RpcResponse {
	public RpcError error = new RpcError();

	public RpcError getError() {
	    return error;
	}

	public void setError(RpcError error) {
	    this.error = error;
	}

	public RpcException toException() {
	    RpcException exception = new RpcException();
	    exception.id = id;
	    if (error != null) {
		exception.code = error.code;
		exception.data = error.data;
		exception.message = error.message;
	    }
	    return exception;
	}
    }

    public static RpcResponse genFail(Throwable t, Object id, int code,
	    String msg) {
	Throwable tmp = t.getCause();
	if (tmp == null)
	    tmp = t;
	return newError(id, code, msg, tmp.getMessage());
    }

    public Object getId() {
	return id;
    }

    public void setId(Object id) {
	this.id = id;
    }

    @Override
    public String toString() {
	return JSON.toJSONString(this);
    }

    public String getJsonrpc() {
	return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
	this.jsonrpc = jsonrpc;
    }

    public static List<RpcResponse> builds(byte[] resdata) {
	List<RpcResponse> resps = new ArrayList<RpcResponse>();
	if (resdata.length == 0) {
	    return resps;
	}
	Object json = JSON.parse(resdata);

	if (json instanceof JSONArray) {
	    JSONArray objs = (JSONArray) json;
	    for (Object obj : objs) {
		if (obj instanceof JSONObject) {
		    RpcResponse rpcret = genRpcResp((JSONObject) obj);
		    if (rpcret != null) {
			resps.add(rpcret);
		    }
		}
	    }
	}
	return resps;
    }

    public static RpcResponse build(byte[] resdata) {
	if (resdata.length == 0) {
	    return null;
	}
	Object json = JSON.parse(resdata);

	if (!(json instanceof JSONObject)) {
	    return null;
	}
	JSONObject obj = (JSONObject) json;
	return genRpcResp(obj);
    }

    private static RpcResponse genRpcResp(JSONObject obj) {
	if (obj.containsKey("result")) {
	    RpcRespResult rpcret = new RpcRespResult();
	    rpcret.id = obj.get("id");
	    rpcret.result = obj.get("result");
	    return rpcret;
	} else if (obj.containsKey("error")) {
	    RpcRespError rpcret = new RpcRespError();
	    rpcret.id = obj.get("id");
	    Object error = obj.get("error");
	    if (error instanceof JSONObject) {
		JSONObject err = (JSONObject) error;
		rpcret.error.code = err.getIntValue("code");
		rpcret.error.message = err.getString("message");
		rpcret.error.data = err.get("data");
	    }
	    return rpcret;
	} else {
	    throw new IllegalArgumentException("not valid RpcResponse");
	}
    }
}