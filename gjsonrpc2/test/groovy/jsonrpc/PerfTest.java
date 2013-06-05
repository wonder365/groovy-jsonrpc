package groovy.jsonrpc;

import static groovy.jsonrpc.engine.RpcRequest.newNotify;
import static groovy.jsonrpc.engine.RpcRequest.newRequst;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import groovy.jsonrpc.constant.Constant;
import groovy.jsonrpc.engine.RpcResponse.RpcRespError;
import groovy.jsonrpc.engine.RpcResponse.RpcRespResult;
import groovy.jsonrpc.handler.GMapedHandler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class PerfTest {
    static final Logger logger = LoggerFactory.getLogger(PerfTest.class);
    static final GMapedHandler handler = new GMapedHandler();
    static final String JAVACLASS = "groovyclass.";
    static final String url = "test/test.groovy";
    static {
	handler.register(JAVACLASS, url, true);
    }

    static Object call(String url, Object reqobj, Class<?> clazz) {
	String req = JSON.toJSONString(reqobj);
	String rsp = call(url, req);
	return JSON.parseObject(rsp, clazz);
    }

    static Object call(String url, String req, Class<?> clazz) {
	String rsp = call(url, req);
	return JSON.parseObject(rsp, clazz);
    }

    static String call(String url, Object reqobj) {
	String req = JSON.toJSONString(reqobj);
	return call(url, req);
    }

    static List<RpcRespResult> callbatch(String url, Object[] reqobj) {
	String req = JSON.toJSONString(reqobj);
	String rsp = call(url, req);
	return JSON.parseArray(rsp, RpcRespResult.class);
    }

    static List<RpcRespError> callbatcherrors(String url, String req) {
	String rsp = call(url, req);
	return JSON.parseArray(rsp, RpcRespError.class);
    }

    static String call(String url, String req) {
	// logger.debug(">url[{}] req[{}]", req);
	String rsp = handler.call(req);
	// logger.debug("<{}", rsp);
	return rsp;
    }

    @Test
    public void testCallArgsCount() {
	RpcRespResult rsp = (RpcRespResult) call(url,
		newRequst(null, JAVACLASS + "add", new int[] { 1, 2 }),
		RpcRespResult.class);
	assertNull(rsp.id);
	assertEquals(3, rsp.result);
	long st = System.currentTimeMillis();
	for (int i = 0; i < 100000; i++) {
	    rsp = (RpcRespResult) call(url,
		    newRequst(null, JAVACLASS + "add", new int[] { 1, 2 }),
		    RpcRespResult.class);
	    assertNull(rsp.id);
	    assertEquals(3, rsp.result);
	}
	long ed = System.currentTimeMillis();
	logger.info("ut {}", ed - st);
    }

}
