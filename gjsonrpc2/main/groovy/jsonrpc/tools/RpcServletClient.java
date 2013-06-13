package groovy.jsonrpc.tools;

import groovy.jsonrpc.constant.Constant;
import groovy.jsonrpc.engine.RpcException;
import groovy.jsonrpc.engine.RpcRequest;
import groovy.jsonrpc.engine.RpcResponse;
import groovy.jsonrpc.engine.RpcResponse.RpcRespError;
import groovy.jsonrpc.engine.RpcResponse.RpcRespResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wslite.http.HTTP;
import wslite.http.HTTPMethod;
import wslite.http.HTTPRequest;
import wslite.http.HTTPResponse;
import wslite.rest.RESTClient;

import com.alibaba.fastjson.JSON;

public class RpcServletClient extends RESTClient {
    static final Logger logger = LoggerFactory
	    .getLogger(RpcServletClient.class);

    URL _url;
    boolean gzip = true;
    AtomicInteger id = new AtomicInteger();

    // HTTP request headers, for performance
    private static final Map<String, String> REQHEADER;
    private static final Map<String, String> REQHEADERGZIP;
    private static final Map<String, String> REQHEADERGZIPED;
    static {
	REQHEADER = new HashMap<String, String>();
	REQHEADER.put(HTTP.getCONTENT_TYPE_HEADER(),
		Constant.DEFAULT_CONTENT_TYPE);
	REQHEADER.put(HTTP.getACCEPT_HEADER(), Constant.DEFAULT_CONTENT_TYPE);
	REQHEADERGZIP = new HashMap<String, String>();
	REQHEADERGZIP.putAll(REQHEADER);
	REQHEADERGZIP.put("accept-encoding", Constant.GZIP);
	REQHEADERGZIPED = new HashMap<String, String>();
	REQHEADERGZIPED.putAll(REQHEADERGZIP);
	REQHEADERGZIPED.put("Content-Encoding", Constant.GZIP);
    }

    public RpcServletClient(String url) throws MalformedURLException {
	super(url);
	this._url = new URL(url);
    }

    public RpcServletClient(String url, boolean isgzip)
	    throws MalformedURLException {
	super(url);
	this._url = new URL(url);
	this.gzip = isgzip;
    }

    public Object call(String method, Object... params) throws RpcException {
	int cid = id.incrementAndGet();
	RpcRequest req;
	if (params.length == 0) {
	    req = RpcRequest.newRequst(cid, method);
	} else if (params.length == 1) {
	    req = RpcRequest.newRequst(cid, method, params[0]);
	} else {
	    req = RpcRequest.newRequst(cid, method, params);
	}
	RpcResponse rpcret;
	rpcret = call(req);
	if (rpcret == null)
	    return null;
	if (rpcret instanceof RpcRespResult) {
	    RpcRespResult ret = (RpcRespResult) rpcret;
	    return ret.result;
	}
	if (rpcret instanceof RpcRespError) {
	    RpcRespError ret = (RpcRespError) rpcret;
	    throw ret.toException();
	}
	return null;
    }

    public Object methodMissing(String name, Object args) throws RpcException {
	Object[] argss = (Object[]) args;
	return (argss.length == 0) ? call(name) : call(name, args);
    }

    public void notify(String method, Object... params) throws RpcException {
	RpcRequest req;
	if (params.length == 0) {
	    req = RpcRequest.newNotify(method);
	} else if (params.length == 1) {
	    req = RpcRequest.newNotify(method, params[0]);
	} else {
	    req = RpcRequest.newNotify(method, params);
	}
	RpcResponse rpcret;
	rpcret = call(req);
	// normally, there is no result
	if (rpcret == null) {
	    return;
	}

	if (rpcret instanceof RpcRespError) {
	    RpcRespError ret = (RpcRespError) rpcret;
	    throw ret.toException();
	} else {
	    throw new RpcException("notify returns value:" + rpcret);
	}
    }

    public List<RpcResponse> batch(RpcRequest req1, RpcRequest req2,
	    RpcRequest... others) throws RpcException {
	List<RpcRequest> reqs = new ArrayList<RpcRequest>();
	reqs.add(req1);
	reqs.add(req2);
	for (RpcRequest req : others) {
	    reqs.add(req);
	}
	byte[] resdata = _call(reqs);
	return RpcResponse.builds(resdata);
    }

    private byte[] _call(Object req) throws RpcException {
	byte[] data = JSON.toJSONBytes(req);
	HTTPRequest request = new HTTPRequest();
	request.setMethod(HTTPMethod.POST);
	request.setSslTrustAllCerts(true);
	request.setUrl(_url);
	request.setHeaders(REQHEADER);
	if (gzip) {
	    request.setHeaders(REQHEADERGZIP);
	}

	try {
	    if (gzip && data.length > Constant.MIN_GZIP_LEN) {
		request.setHeaders(REQHEADERGZIPED);
		byte[] reqdata = gzip(data);
		if (logger.isDebugEnabled()) {
		    logger.debug("send gzip: {} to {}", data.length,
			    reqdata.length);
		}
		request.setData(reqdata);
	    } else {
		request.setData(data);
	    }
	    HTTPResponse resp = this.getHttpClient().execute(request);
	    byte[] resdata = resp.getData();
	    return resdata;
	} catch (Throwable t) {
	    throw new RpcException(t);
	}
    }

    public RpcResponse call(RpcRequest req) throws RpcException {
	byte[] resdata = _call(req);
	return RpcResponse.build(resdata);
    }

    public static byte[] gzip(byte[] data) throws IOException {
	byte[] b = null;
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	GZIPOutputStream gzip = new GZIPOutputStream(bos);
	gzip.write(data);
	gzip.finish();
	gzip.close();
	b = bos.toByteArray();
	bos.close();
	return b;
    }

    public boolean isGzip() {
	return gzip;
    }

    public void setGzip(boolean gzip) {
	this.gzip = gzip;
    }
}
