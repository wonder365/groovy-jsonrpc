package groovy.jsonrpc.tools;

import groovy.jsonrpc.constant.Constant;
import groovy.jsonrpc.handler.UrlHandler;
import groovy.jsonrpc.util.GroovyLoaders;
import groovy.lang.GroovyClassLoader;
import groovy.servlet.GroovyServlet;
import groovy.util.GroovyScriptEngine;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.ThreadLocalCache;

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
    String basepath = "";

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
	    throws IOException {
	String ctxtype = request.getContentType();
	if (ctxtype != null && ctxtype.contains(Constant.JSON)) {
	    serviceRpc(request, response);
	} else {
	    super.service(request, response);
	}
    }

    private void serviceRpc(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
	String uri = getScriptUri(request);
	String url = servletContext.getRealPath(uri);

	String cen = request.getHeader(Constant.CONTENT_ENCODING);
	boolean isgzip = (cen != null) && (cen.indexOf(Constant.GZIP) != -1);
	String ae = request.getHeader(Constant.ACCEPT_ENCODING);
	boolean isae = (ae != null && ae.indexOf(Constant.GZIP) != -1);

	if (logger.isDebugEnabled()) {
	    String ip = getIpAddr(request);
	    int contentlen = request.getContentLength();
	    logger.debug("rpc from ip:{}, contentlen:{}", ip, contentlen);
	}

	ServletInputStream ism = request.getInputStream();
	byte[] reqdata = getBytes(isgzip ? new GZIPInputStream(ism) : ism);
	if (logger.isTraceEnabled()) {
	    logger.trace("reqdata: {}", convertBytesToBuf(reqdata));
	}
	byte[] resdata = hl.call(url, reqdata);
	if (logger.isDebugEnabled()) {
	    int resplen = resdata == null ? 0 : resdata.length;
	    logger.debug("response datalen(before gzip): {}", resplen);
	}
	if (resdata != null) {
	    if (logger.isTraceEnabled()) {
		logger.trace("resdata: {}", convertBytesToBuf(resdata));
	    }
	    OutputStream stream = response.getOutputStream();
	    response.setContentType(Constant.DEFAULT_CONTENT_TYPE);
	    if (isae && (resdata.length > Constant.MIN_GZIP_LEN)) {
		response.addHeader(Constant.CONTENT_ENCODING, Constant.GZIP);
		GZIPOutputStream st = new GZIPOutputStream(stream);
		st.write(resdata);
		st.finish();
	    } else {
		stream.write(resdata);
	    }
	}
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	String value = config.getInitParameter("initbase");
	initbase(value);
	this.basepath = servletContext.getRealPath("/");
    }

    public static String getIpAddr(HttpServletRequest request) {
	String ip = request.getHeader("x-forwarded-for");
	if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
	    ip = request.getHeader("Proxy-Client-IP");
	}
	if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
	    ip = request.getHeader("WL-Proxy-Client-IP");
	}
	if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
	    ip = request.getRemoteAddr();
	}
	return ip;
    }

    public static CharBuffer convertBytesToBuf(byte[] input) {
	CharsetDecoder charsetDecoder = ThreadLocalCache.getUTF8Decoder();
	charsetDecoder.reset();
	int len = input.length;
	int off = 0;
	int scaleLength = (int) (len * (double) charsetDecoder
		.maxCharsPerByte());
	char[] chars = ThreadLocalCache.getChars(scaleLength);

	ByteBuffer byteBuf = ByteBuffer.wrap(input, off, len);
	CharBuffer charBuf = CharBuffer.wrap(chars);
	IOUtils.decode(charsetDecoder, byteBuf, charBuf);
	charBuf.flip();
	return charBuf;
    }

    /**
     * initialize some groovy scripts( example: utility class, constants,
     * database or other resource), if script can run, then run it
     * 
     * @param val
     *            paths split by ';', these path will be converted by
     *            servletContext.getRealPath
     */
    public void initbase(String val) {
	if (val != null) {
	    String[] pats = val.split(";");
	    String[] realpats = new String[pats.length];
	    for (int i = 0, sz = pats.length; i < sz; i++) {
		realpats[i] = servletContext.getRealPath(pats[i]);
	    }
	    this.hl.initbase(realpats);
	}
    }

    @Override
    protected GroovyScriptEngine createGroovyScriptEngine() {
	this.cl = GroovyLoaders.createGroovyClassLoader();
	this.hl = new UrlHandler(this.cl);
	return new GroovyScriptEngine(this, this.cl);
    }

    public static byte[] getBytes(InputStream is) throws IOException {
	ByteArrayOutputStream answer = new ByteArrayOutputStream();
	// reading the content of the file within a byte buffer
	byte[] byteBuffer = new byte[8192];
	int nbByteRead /* = 0 */;
	try {
	    while ((nbByteRead = is.read(byteBuffer)) != -1) {
		// appends buffer
		answer.write(byteBuffer, 0, nbByteRead);
	    }
	} finally {
	    closeWithWarning(is);
	}
	return answer.toByteArray();
    }

    public static void closeWithWarning(Closeable c) {
	if (c != null) {
	    try {
		c.close();
	    } catch (IOException e) {
		logger.warn("Caught exception during close(): ", e);
	    }
	}
    }
}
