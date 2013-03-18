package groovy.jsonrpc.engine;

import groovy.jsonrpc.constant.Constant;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;

@JSONType(orders = { "jsonrpc", "method" })
public class RpcRequest {
    public final String jsonrpc = Constant.VERSION;
    public String method;

    @JSONType(orders = { "jsonrpc", "id", "method" })
    static class RpcReqObjectNoParams extends RpcRequest {
	@JSONField(serialzeFeatures = { SerializerFeature.WriteMapNullValue })
	public Object id;

	public Object getId() {
	    return id;
	}

	public void setId(Object id) {
	    this.id = id;
	}
    }

    @JSONType(orders = { "jsonrpc", "id", "method", "params" })
    static class RpcReqObject extends RpcReqObjectNoParams {
	public Object params;

	public Object getParams() {
	    return params;
	}

	public void setParams(Object params) {
	    this.params = params;
	}
    }

    @JSONType(orders = { "jsonrpc", "method", "params" })
    static class RpcNotifyWithParams extends RpcRequest {
	public Object params;

	public Object getParams() {
	    return params;
	}

	public void setParams(Object params) {
	    this.params = params;
	}
    }

    public static RpcRequest newNotify(String method) {
	RpcRequest req = new RpcRequest();
	req.method = method;
	return req;
    }

    public static RpcRequest newNotify(String method, Object params) {
	RpcNotifyWithParams req = new RpcNotifyWithParams();
	req.method = method;
	req.params = params;
	return req;
    }

    public static RpcRequest newRequst(Object id, String method) {
	RpcReqObjectNoParams req = new RpcReqObjectNoParams();
	req.id = id;
	req.method = method;
	return req;
    }

    public static RpcRequest newRequst(Object id, String method, Object params) {
	RpcReqObject req = new RpcReqObject();
	req.id = id;
	req.method = method;
	req.params = params;
	return req;
    }
}
