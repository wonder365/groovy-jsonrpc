package groovy.jsongrpc.tools;

import groovy.jsongrpc.handler.UrlHandler;
import groovy.jsongrpc.util.GroovyLoaders;
import groovy.lang.GroovyClassLoader;
import groovy.servlet.GroovyServlet;
import groovy.util.GroovyScriptEngine;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the GroovyServlet supports JSONRPC, when requst.ContentType contains "json",
 * it will automatically call as a JSONRPC
 * <p>
 * <b>Warning:</b>the RpcServlet only supports UTF-8 encoding
 * </p>
 */
public class RpcServlet extends GroovyServlet {
    static final Logger logger = LoggerFactory.getLogger(RpcServlet.class);
    private static final long serialVersionUID = 5121241986847167685L;
    private GroovyClassLoader cl;
    private UrlHandler hl;

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
	    throws IOException {
	String ctxtype = request.getContentType();
	if (ctxtype.contains("json")) {
	    serviceRpc(request, response);
	} else {
	    super.service(request, response);
	}
    }

    private void serviceRpc(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
	String uri = getScriptUri(request);
	String url = servletContext.getRealPath(uri);
	byte[] reqdata = IOGroovyMethods.getBytes(request.getInputStream());
	byte[] resdata = hl.call(url, reqdata);
	if (resdata != null) {
	    response.setContentType("application/json; charset=utf8");
	    response.getOutputStream().write(resdata);
	}
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
    }

    @Override
    protected GroovyScriptEngine createGroovyScriptEngine() {
	this.cl = GroovyLoaders.createGroovyClassLoader();
	this.hl = new UrlHandler(this.cl);
	return new GroovyScriptEngine(this, this.cl);
    }
}
