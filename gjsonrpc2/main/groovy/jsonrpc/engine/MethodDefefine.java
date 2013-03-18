package groovy.jsonrpc.engine;

import groovy.jsonrpc.constant.Constant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

public class MethodDefefine implements JSONAware {
    static final Logger logger = LoggerFactory.getLogger(MethodDefefine.class);
    public Method method;
    /**
     * parameter count of the method
     */
    public int parcount;

    public boolean isSingleParConvertable;

    public MethodDefefine(Method method) {
	super();
	this.method = method;
	Class<?>[] pts = method.getParameterTypes();
	this.parcount = pts.length;
	this.isSingleParConvertable = (pts.length == 1)
		&& (pts[0].isPrimitive() || pts[0].equals(String.class));
    }

    /**
     * collect all method (public static) to method map, the method name will
     * add the profix
     * 
     * @param clazz
     * @param profix
     * @param methodmap
     * @return the method names that collected to method map
     */
    public static List<String> collectMethods(Class<?> clazz, String profix,
	    Map<String, MethodDefefine> methodmap) {
	Method[] methods = clazz.getDeclaredMethods();
	List<String> names = new ArrayList<String>();
	// get methods, static and not start with'-' and not 'main'
	for (Method m : methods) {
	    int modifiers = m.getModifiers();
	    if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
		String mname = m.getName();
		// warning: the _function and main will ignored
		if ((!mname.startsWith("_") && !mname.equals("main"))) {
		    if (profix != null)
			mname = profix + mname;
		    names.add(mname);
		    methodmap.put(mname, new MethodDefefine(m));
		}
	    }
	}
	return names;
    }

    /**
     * invoke the method, when needed the parameters will turn to array
     * 
     * @param params
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Object invoke(Object params) throws IllegalAccessException,
	    InvocationTargetException {
	Object ret;
	if (params != null) {
	    // info: simply cast list to Array, then invoked by function
	    if ((params instanceof List)
		    && (this.parcount == ((List<?>) params).size())
		    && ((this.isSingleParConvertable) || (this.parcount > 1))) {
		ret = this.method.invoke(null, ((List<?>) params).toArray());
	    } else {
		ret = this.method.invoke(null, params);
	    }
	} else {
	    ret = this.method.invoke(null);
	}
	return ret;
    }

    /**
     * invoke the method from methods
     * 
     * @param methods
     * @param methodname
     * @param id
     * @param jo
     *            JSONRPC request object
     * @return JSONRPC response object
     */
    public static RpcResponse callmethod(
	    final Map<String, MethodDefefine> methods, final String methodname,
	    final Object id, final JSONObject jo) {
	MethodDefefine mth = methods.get(methodname);
	if (mth == null) {
	    return RpcResponse.newError(id, Constant.EC_METHOD_NOT_FOUND,
		    "Method not found");
	}
	return mth.call(id, jo);
    }

    /**
     * invoke the method
     * 
     * @param id
     * @param jo
     * @return JSONRPC response object
     */
    public RpcResponse call(final Object id, final JSONObject jo) {
	Object params = jo.get(Constant.FIELD_PARAMS);
	Object ret;
	try {
	    ret = this.invoke(params);
	    return RpcResponse.newRpcRespResult(id, ret);
	} catch (IllegalArgumentException t) {
	    return RpcResponse.genFail(t, id, Constant.EC_INVALID_PARAMS,
		    "Invalid params");
	} catch (Throwable t) {
	    logger.warn("", t);
	    return RpcResponse.genFail(t, id, Constant.EC_DEFAULT_APP_ERROR,
		    "Uncatched application exception");
	}
    }

    @Override
    public String toJSONString() {
	return "'" + method + "'";
    }
}