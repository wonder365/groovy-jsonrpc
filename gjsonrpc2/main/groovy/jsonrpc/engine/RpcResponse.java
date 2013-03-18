package groovy.jsonrpc.engine;

import groovy.jsonrpc.constant.Constant;

import com.alibaba.fastjson.JSON;
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
    public final String jsonrpc = Constant.VERSION;
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
	RpcResponse.RpcError error = new RpcError();
	error.code = code;
	error.message = message;
	error.id = id;
	return error;
    }

    public static RpcResponse newError(Object id, int code, String message,
	    Object data) {
	RpcResponse.RpcErrorWithData error = new RpcErrorWithData();
	error.code = code;
	error.message = message;
	error.id = id;
	error.data = data;
	return error;
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

    @JSONType(orders = { "jsonrpc", "id", "code", "message" })
    public static class RpcError extends RpcResponse {
	public int code;
	public String message;

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

    @JSONType(orders = { "jsonrpc", "id", "code", "message", "data" })
    public static class RpcErrorWithData extends RpcResponse.RpcError {
	public Object data;

	public Object getData() {
	    return data;
	}

	public void setData(Object data) {
	    this.data = data;
	}
    }

    @JSONType(orders = { "jsonrpc", "id", "error" })
    public static class RpcRespError extends RpcResponse {
	public Object error;

	public Object getError() {
	    return error;
	}

	public void setError(Object error) {
	    this.error = error;
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
}