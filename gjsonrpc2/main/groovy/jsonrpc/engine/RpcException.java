package groovy.jsonrpc.engine;

import groovy.jsonrpc.constant.Constant;

public class RpcException extends Exception {

    private static final long serialVersionUID = -5445710915015343753L;
    Object id;
    int code;
    String message;
    Object data;

    public RpcException() {
	this.code = Constant.EC_INTERNAL_ERROR;
    }

    public RpcException(String msg) {
	super(msg);
	this.code = Constant.EC_INTERNAL_ERROR;
    }

    public RpcException(Throwable cause) {
	super(cause);
	this.code = Constant.EC_INTERNAL_ERROR;
    }

    public RpcException(String msg, Throwable cause) {
	super(msg, cause);
	this.code = Constant.EC_INTERNAL_ERROR;
    }

    @Override
    public String toString() {
	return "RpcException [id=" + id + ", code=" + code + ", message="
		+ message + ", data=" + data + "]";
    }

    @Override
    public String getMessage() {
	return super.getMessage();
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
