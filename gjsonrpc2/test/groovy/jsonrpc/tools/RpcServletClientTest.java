package groovy.jsonrpc.tools;

import static groovy.jsonrpc.engine.RpcRequest.newRequst;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import groovy.jsonrpc.constant.Constant;
import groovy.jsonrpc.engine.RpcException;
import groovy.jsonrpc.engine.RpcRequest;
import groovy.jsonrpc.engine.RpcResponse;
import groovy.jsonrpc.engine.RpcResponse.RpcRespError;
import groovy.jsonrpc.engine.RpcResponse.RpcRespResult;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class RpcServletClientTest {
    static final Logger logger = LoggerFactory
	    .getLogger(RpcServletClientTest.class);
    static {
	try {
	    initserver();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	try {
	    client = new RpcServletClient("http://127.0.0.1:8080/test.groovy");
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	}
    }

    static void initserver() throws Exception {
	Server server = new Server(8080);
	WebAppContext context = new WebAppContext("web", "/");
	ServletHolder holder = new ServletHolder(new RpcServlet());
	context.addServlet(holder, "*.groovy");
	server.setHandler(context);
	server.start();
	// server.join();
    }

    static RpcServletClient client;

    static Object call(RpcRequest reqobj, Class<?> clazz) {
	logger.info("> {}", JSON.toJSONString(reqobj, true));
	RpcResponse rsp;
	try {
	    rsp = client.call(reqobj);
	    logger.info("< {}", JSON.toJSONString(rsp, true));
	    return rsp;
	} catch (Throwable e) {
	    e.getCause().printStackTrace();
	}
	return null;
    }

    static final String url = "";

    @Test
    public void testCallGroovy() {
	RpcRespResult rsp = (RpcRespResult) call(
		newRequst(null, "add", new int[] { 1, 2 }), RpcRespResult.class);
	assertNull(rsp.id);
	assertEquals(3, rsp.result);
	// 3 parameters
	rsp = (RpcRespResult) call(newRequst(1, "adds", new int[] { 1, 2, 3 }),
		RpcRespResult.class);
	assertEquals(1, rsp.id);
	assertEquals(6, rsp.result);
    }

    @Test
    public void testCallArgsCount() {
	// no parameter
	RpcRespResult rsp = (RpcRespResult) call(newRequst(null, "getDate"),
		RpcRespResult.class);
	assertNull(rsp.id);
	assertNotNull(rsp.result);
	// 1 parameter
	rsp = (RpcRespResult) call(newRequst(null, "fun1arg", 1),
		RpcRespResult.class);
	assertNull(rsp.id);
	assertEquals(2, rsp.result);
	// 2 parameters
	rsp = (RpcRespResult) call(newRequst(null, "add", new int[] { 1, 2 }),
		RpcRespResult.class);
	assertNull(rsp.id);
	assertEquals(3, rsp.result);
	// 3 parameters
	rsp = (RpcRespResult) call(newRequst(1, "adds", new int[] { 1, 2, 3 }),
		RpcRespResult.class);
	assertEquals(1, rsp.id);
	assertEquals(6, rsp.result);
    }

    @Test
    public void testCallAppException() {
	RpcRespError rsp = (RpcRespError) call(
		newRequst(null, "adds", new int[] { 1, 2 }), RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_DEFAULT_APP_ERROR, rsp.error.code);
	assertNotNull(rsp.error.message);
	assertNotNull(rsp.error.data);
    }

    @Test
    public void testCallMethodNotFound() {
	RpcRespError rsp = (RpcRespError) call(
		newRequst(null, "notfound", new int[] { 1, 2 }),
		RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_METHOD_NOT_FOUND, rsp.error.code);
	assertNotNull(rsp.error.message);
	assertNull(rsp.error.data);
    }

    @Test
    public void testCallMethodParamsError() {
	RpcRespError rsp = (RpcRespError) call(newRequst(null, "add", 1),
		RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_PARAMS, rsp.error.code);
	assertNotNull(rsp.error.message);
	assertNotNull(rsp.error.data);
    }

    @Test
    public void testCallBase() {
	RpcRespResult rsp = (RpcRespResult) call(newRequst(null, "rpc.ls"),
		RpcRespResult.class);
	assertEquals(Constant.VERSION, rsp.jsonrpc);
	assertNull(rsp.id);
	assertNotNull(rsp.result);
    }

    @Test
    public void testCallRpccmd() {
	String[] cmds = new String[] { "rpc.ls", "rpc.ll", "rpc.all",
		"rpc.recompile" };
	for (String cmd : cmds) {
	    RpcRespResult rsp = (RpcRespResult) call(newRequst(null, cmd),
		    RpcRespResult.class);
	    assertEquals(Constant.VERSION, rsp.jsonrpc);
	    assertNull(rsp.id);
	    assertNotNull(rsp.result);
	}
    }

    @Test
    public void testCallAutoConvertParameter() {
	// 1 arg
	RpcRespResult rsp = (RpcRespResult) call(
		newRequst(null, "fun1arg", new int[] { 1 }),
		RpcRespResult.class);
	assertEquals(2, rsp.result);
	rsp = (RpcRespResult) call(newRequst(null, "fun1arg", 1),
		RpcRespResult.class);
	assertEquals(2, rsp.result);
	// 1 arg String
	rsp = (RpcRespResult) call(
		newRequst(null, "fun1argstr", new String[] { "abc" }),
		RpcRespResult.class);
	assertEquals("cba", rsp.result);
	// 2 args
	rsp = (RpcRespResult) call(newRequst(null, "add", new int[] { 1, 2 }),
		RpcRespResult.class);
	assertEquals(3, rsp.result);
    }

    @Test
    public void testCallCheckId() {
	// null
	RpcRespResult rsp = (RpcRespResult) call(newRequst(null, "rpc.ls"),
		RpcRespResult.class);
	assertNull(rsp.id);
	// int
	rsp = (RpcRespResult) call(newRequst(1, "rpc.ls"), RpcRespResult.class);
	assertEquals(1, rsp.id);
	rsp = (RpcRespResult) call(newRequst("strid", "rpc.ls"),
		RpcRespResult.class);
	// long
	assertEquals(rsp.id, "strid");
	long id = Long.MAX_VALUE;
	rsp = (RpcRespResult) call(newRequst(id, "rpc.ls"), RpcRespResult.class);
	assertEquals(id, rsp.id);
	id = Long.MIN_VALUE;
	rsp = (RpcRespResult) call(newRequst(id, "rpc.ls"), RpcRespResult.class);
	assertEquals(id, rsp.id);
	// complicate object
	Map<String, String> mid = new HashMap<String, String>();
	mid.put("aa", "bb");
	rsp = (RpcRespResult) call(newRequst(mid, "rpc.ls"),
		RpcRespResult.class);
	assertTrue(rsp.id instanceof Map);
	@SuppressWarnings("unchecked")
	Map<String, String> rspmid = (Map<String, String>) (rsp.id);
	assertEquals("bb", rspmid.get("aa"));
    }

    @Test
    public void testCallCheckCompId() {
	// complicate object
	Map<String, String> mid = new HashMap<String, String>();
	mid.put("aa", "bb");
	RpcRespResult rsp = (RpcRespResult) call(newRequst(mid, "rpc.ls"),
		RpcRespResult.class);
	assertTrue(rsp.id instanceof Map);
	@SuppressWarnings("unchecked")
	Map<String, String> rspmid = (Map<String, String>) (rsp.id);
	assertEquals("bb", rspmid.get("aa"));
    }

    @Test
    public void testCall() {
	try {
	    Object ret = client.call("rpc.ls");
	    assertTrue(ret instanceof List);
	    ret = client.call("adds", 1, 2, 3);
	    assertEquals(6, ret);
	    ret = client.call("echo", 1, 2, 3);
	    assertTrue(ret instanceof List);
	    List<Integer> aret = (List<Integer>) ret;
	    assertTrue(ret instanceof List);
	    assertEquals(3, aret.size());
	    assertEquals(3, (int)aret.get(2));
	    ret = client.call("rpc.lsx");
	} catch (RpcException e) {
	    logger.error("fail: ", e);
	    assertEquals(Constant.EC_METHOD_NOT_FOUND, e.getCode());
	}
    }

    @Test
    public void testNotify() throws RpcException {
	client.notify("rpc.ls");
    }

    @Test
    public void testBatch() throws RpcException {
	RpcRequest[] fails = new RpcRequest[10];
	for (int i = 0; i < 10; i++) {
	    fails[i] = newRequst("fail" + i, "失败的方法" + i);
	}

	List<RpcResponse> rets = client.batch(newRequst("1", "rpc.ls"),
		newRequst("2", "rpc.ls"), fails);
	logger.debug("rets: {}", rets);
    }

    @Test
    public void testZ() throws MalformedURLException {
	try {
	    RpcServletClient client1 = new RpcServletClient(
		    "http://172.16.36.70/multilevel/static/level/level.groovy");
	    Object ret = client1.call("getResHis",
		    "0d886f10-b659-3071-9639-0e400e44885b", "CPURate", "1d");
	    logger.debug("ret {}", ret);
	} catch (RpcException e) {
	    logger.error("fail: ", e);
	    assertEquals(Constant.EC_METHOD_NOT_FOUND, e.getCode());
	}
    }

}
