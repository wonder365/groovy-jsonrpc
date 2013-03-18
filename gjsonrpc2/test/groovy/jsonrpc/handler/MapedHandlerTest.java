package groovy.jsonrpc.handler;

import static groovy.jsonrpc.engine.RpcRequest.newNotify;
import static groovy.jsonrpc.engine.RpcRequest.newRequst;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import groovy.jsonrpc.constant.Constant;
import groovy.jsonrpc.engine.RpcResponse.RpcErrorWithData;
import groovy.jsonrpc.engine.RpcResponse.RpcRespResult;
import groovy.jsonrpc.handler.MapedHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class MapedHandlerTest {
    static final MapedHandler handler = new MapedHandler();
    static final String JAVACLASS = "javaclass.";
    static {
	handler.register(JAVACLASS, TestJava.class.getName(), false);
    }
    static final String url = "test/test.groovy";

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

    static List<RpcErrorWithData> callbatcherrors(String url, String req) {
	String rsp = call(url, req);
	return JSON.parseArray(rsp, RpcErrorWithData.class);
    }

    static String call(String url, String req) {
	System.out.println(">" + req);
	String rsp = handler.call(req);
	System.out.println("<" + rsp);
	return rsp;
    }

    @Test
    public void testCallArgsCount() {
	// no parameter
	RpcRespResult rsp = (RpcRespResult) call(url,
		newRequst(null, JAVACLASS + "getDate"), RpcRespResult.class);
	assertNull(rsp.id);
	assertNotNull(rsp.result);
	// 1 parameter
	rsp = (RpcRespResult) call(url,
		newRequst(null, JAVACLASS + "fun1arg", 1), RpcRespResult.class);
	assertNull(rsp.id);
	assertEquals(2, rsp.result);
	// 2 parameters
	rsp = (RpcRespResult) call(url,
		newRequst(null, JAVACLASS + "add", new int[] { 1, 2 }),
		RpcRespResult.class);
	assertNull(rsp.id);
	assertEquals(3, rsp.result);
	// 3 parameters
	rsp = (RpcRespResult) call(url,
		newRequst(1, JAVACLASS + "adds", new int[] { 1, 2, 3 }),
		RpcRespResult.class);
	assertEquals(1, rsp.id);
	assertEquals(6, rsp.result);
    }

    @Test
    public void testCallAppException() {
	RpcErrorWithData rsp = (RpcErrorWithData) call(url,
		newRequst(null, JAVACLASS + "adds", new int[] { 1, 2 }),
		RpcErrorWithData.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_DEFAULT_APP_ERROR, rsp.code);
	assertNotNull(rsp.message);
	assertNotNull(rsp.data);
    }

    @Test
    public void testCallMethodNotFound() {
	RpcErrorWithData rsp = (RpcErrorWithData) call(url,
		newRequst(null, JAVACLASS + "notfound", new int[] { 1, 2 }),
		RpcErrorWithData.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_METHOD_NOT_FOUND, rsp.code);
	assertNotNull(rsp.message);
	assertNull(rsp.data);
    }

    @Test
    public void testCallMethodParamsError() {
	RpcErrorWithData rsp = (RpcErrorWithData) call(url,
		newRequst(null, JAVACLASS + "add", 1), RpcErrorWithData.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_PARAMS, rsp.code);
	assertNotNull(rsp.message);
	assertNotNull(rsp.data);
    }

    @Test
    public void testCallBatchBase() {
	Object[] batch = new Object[] {
		newRequst(null, JAVACLASS + "add", new int[] { 1, 2 }),
		newRequst(2, JAVACLASS + "adds", new int[] { 1, 2, 3 }) };
	List<RpcRespResult> rsp = callbatch(url, batch);
	assertEquals(2, rsp.size());
	RpcRespResult rsp1 = rsp.get(0);
	assertNull(rsp1.id);
	assertEquals(3, rsp1.result);
	RpcRespResult rsp2 = rsp.get(1);
	assertEquals(2, rsp2.id);
	assertEquals(6, rsp2.result);
    }

    @Test
    public void testCallBatchContainsNotify() {
	Object[] batch = new Object[] {
		newRequst(1, JAVACLASS + "add", new int[] { 1, 2 }),
		newNotify(JAVACLASS + "donotify") };
	List<RpcRespResult> rsp = callbatch(url, batch);
	assertEquals(1, rsp.size());
	RpcRespResult rsp1 = rsp.get(0);
	assertEquals(1, rsp1.id);
	assertEquals(3, rsp1.result);
    }

    @Test
    public void testCallBatchAllNotify() {
	Object[] batch = new Object[] { newNotify(JAVACLASS + "donotify"),
		newNotify(JAVACLASS + "notfoundnotify") };
	String rsp = call(url, batch);
	assertEquals("", rsp);
    }

    @Test
    public void testCallBase() {
	RpcRespResult rsp = (RpcRespResult) call(url,
		newRequst(null, "rpc.ls"), RpcRespResult.class);
	assertEquals(Constant.VERSION, rsp.jsonrpc);
	assertNull(rsp.id);
	assertNotNull(rsp.result);
    }

    @Test
    public void testCallAutoConvert() {
	String rsp = call(url, newNotify(JAVACLASS + "donotify"));
	assertEquals("", rsp);
	rsp = call(url, newNotify(JAVACLASS + "echo", new int[] { 1, 2 }));
	assertEquals("", rsp);
    }

    @Test
    public void testCallNotify() {
	String rsp = call(url, newNotify(JAVACLASS + "donotify"));
	assertEquals("", rsp);
	rsp = call(url, newNotify(JAVACLASS + "echo", new int[] { 1, 2 }));
	assertEquals("", rsp);
    }

    @Test
    public void testCallCheckId() {
	// null
	RpcRespResult rsp = (RpcRespResult) call(url,
		newRequst(null, "rpc.ls"), RpcRespResult.class);
	assertNull(rsp.id);
	// int
	rsp = (RpcRespResult) call(url, newRequst(1, "rpc.ls"),
		RpcRespResult.class);
	assertEquals(1, rsp.id);
	rsp = (RpcRespResult) call(url, newRequst("strid", "rpc.ls"),
		RpcRespResult.class);
	// long
	assertEquals(rsp.id, "strid");
	long id = Long.MAX_VALUE;
	rsp = (RpcRespResult) call(url, newRequst(id, "rpc.ls"),
		RpcRespResult.class);
	assertEquals(id, rsp.id);
	id = Long.MIN_VALUE;
	rsp = (RpcRespResult) call(url, newRequst(id, "rpc.ls"),
		RpcRespResult.class);
	assertEquals(id, rsp.id);
	// complicate object
	Map<String, String> mid = new HashMap<String, String>();
	mid.put("aa", "bb");
	rsp = (RpcRespResult) call(url, newRequst(mid, "rpc.ls"),
		RpcRespResult.class);
	assertTrue(rsp.id instanceof Map);
	@SuppressWarnings("unchecked")
	Map<String, String> rspmid = (Map<String, String>) (rsp.id);
	assertEquals("bb", rspmid.get("aa"));
    }

    @Test
    public void testCallInValid() {
	// parse error
	RpcErrorWithData rsp = (RpcErrorWithData) call(url, "[",
		RpcErrorWithData.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_PARSE_ERROR, rsp.code);
	// parse error, not terminated batch
	rsp = (RpcErrorWithData) call(
		url,
		"[ {'jsonrpc': '2.0', 'method': 'sum'},  {'jsonrpc': '2.0', 'method']",
		RpcErrorWithData.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_PARSE_ERROR, rsp.code);
	// empty batch
	rsp = (RpcErrorWithData) call(url, "[]", RpcErrorWithData.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_REQUEST, rsp.code);
	// rpc call with an invalid Batch (but not empty)
	List<RpcErrorWithData> rspes = callbatcherrors(url, "[1]");
	assertEquals(1, rspes.size());
	rsp = rspes.get(0);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_REQUEST, rsp.code);
	// rpc call with invalid Batchs
	rspes = callbatcherrors(url, "[1, 2, 3]");
	assertEquals(3, rspes.size());
	rsp = rspes.get(0);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_REQUEST, rsp.code);
    }
}
