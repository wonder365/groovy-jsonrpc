groovy-jsonrpc
==============

jsonrpc2.0 implementation of java and groovy, simply expose the public static method of java class or groovy script
**note:** it use [FastJSON] (http://code.alibabatech.com/wiki/display/FastJSON/Home) as JSON-processor

# download
 [groovy-jsonrpc.jar] (../../raw/master/gjsonrpc2/files/groovy-jsonrpc.jar)

# features
* easy: no annotation, interface ...
* light-weight: minimal depends on fastjson.jar and slf4j.jar 
* JSONRPC 2.0 
* dynamic load groovy scripts
* and has a "MapedHandler" dosn't depends on groovy.jar
* auto convert "params" of JSONRPC from list to array( "params":[1,2] fits int add(int a, int b) )
* support initialize some resource scripts( as utility, constants, acquire resource ...)
* HTTP RpcServlet and RpcServletClient (supports gzip)

## groovy.jsonrpc.handler.UrlHandler

handler supports loading GROOVY through JSONRPC by file URL, when call a method, must pass the real file path, the "method" field in JSONRPC would be the method name exactly in the GROOVY class

### Example

``` java
UrlHandler handler = new UrlHandler();
handler.initbase("test/startdb.groovy", "test/contants.groovy");
String url = "test/test.groovy";
String req = "{"jsonrpc":"2.0","id":null,"method":"add","params":[1,2]}";
String rsp = handler.call(url, req);
// rsp : {"jsonrpc":"2.0","id":null,"result":3}
// or
byte[] rsp = handler.call(url, byte[]data)
```

### RPC commands
```
>{"jsonrpc":"2.0","id":null,"method":"rpc.ls"}
<{"jsonrpc":"2.0","id":null,"result":["subtract","getDate","nullf","donotify","notify_sum","sum","adds","fun1arg","add","echo"]}

>{"jsonrpc":"2.0","id":null,"method":"rpc.ll"}
<{"jsonrpc":"2.0","id":null,"result":{"add":'public static int jsonrpc.dynamic.test.add(int,int)',"adds":'public static int jsonrpc.dynamic.test.adds(java.util.List)',"donotify":'public static java.lang.Object jsonrpc.dynamic.test.donotify()',"echo":'public static java.lang.Object jsonrpc.dynamic.test.echo(java.lang.Object)',"fun1arg":'public static java.lang.Object jsonrpc.dynamic.test.fun1arg(int)',"getDate":'public static java.lang.Object jsonrpc.dynamic.test.getDate()',"notify_sum":'public static java.lang.Object jsonrpc.dynamic.test.notify_sum(java.util.List)',"nullf":'public static java.lang.Object jsonrpc.dynamic.test.nullf()',"subtract":'public static int jsonrpc.dynamic.test.subtract(int,int)',"sum":'public static java.lang.Object jsonrpc.dynamic.test.sum(java.util.List)'}}

>{"jsonrpc":"2.0","id":null,"method":"rpc.all"}
<{"jsonrpc":"2.0","id":null,"result":[{"url":"test/test.groovy","classname":"jsonrpc.dynamic.test","isok":true,"compiletime":1363253990140,"failmsg":""}]}

>{"jsonrpc":"2.0","id":null,"method":"rpc.recompile"}
<{"jsonrpc":"2.0","id":null,"result":true}
```

## groovy.jsonrpc.handler.GMapedHandler

handler supports GROOVY class and Java class, these class will mapped to a "virtual class", so when call from JSONRPC, the method field will be "class.method", for example: register "java.lang.Math" to "my.math.", then you can call Math.abs by method "my.math.bas"; register "test/test.groovy" to "my.testg.", then call "my.testg.methodname" to invoke methods in test.groovy
### Example

``` java
GMapedHandler handler = new GMapedHandler();
// register a groovy script
handler.register("groovyclass.", "test/test.groovy", true);
// register a java class
handler.register("javaclass.", "test.class", false);
// call groovy
String req = "{"jsonrpc":"2.0","id":null,"method":"groovyclass.add","params":[1,2]}";
String rsp = handler.call(req);
// call java
String req = "{"jsonrpc":"2.0","id":null,"method":"javaclass.add","params":[1,2]}";
String rsp = handler.call(req);
```
### RPC commands
```
>{"jsonrpc":"2.0","id":null,"method":"rpc.register","params":["groovyclass.2","test/test.groovy",true]}
<{"jsonrpc":"2.0","id":null,"result":true}
```
## groovy.jsonrpc.tools.RpcServlet

the GroovyServlet supports JSONRPC, when requst.ContentType contains "json", it will automatically call as a JSONRPC 

* GZIP supported: when a response is large than 256 byte( and client supports gzip ), RpcServletwiil gzip the data
* Warning: the RpcServlet only supports UTF-8 encoding 

### config WEB-INF/web.xml
```
        <servlet>
                <servlet-name>grpc</servlet-name>
                <servlet-class>groovy.jsonrpc.tools.RpcServlet</servlet-class>
                <init-param>
                        <param-name>initbase</param-name>
                        <param-value>Util.groovy;JobScheduler.groovy</param-value>
                </init-param>
                <load-on-startup>1</load-on-startup>
        </servlet>

        <servlet-mapping>
                <servlet-name>grpc</servlet-name>
                <url-pattern>*.grpc</url-pattern>
        </servlet-mapping>

        <servlet-mapping>
                <servlet-name>grpc</servlet-name>
                <url-pattern>*.gsp</url-pattern>
        </servlet-mapping>
```
#### Parameter: initbase
You may load common codes here, If the file extends groovy.lang.Script, then the RpcServlet will invoke the "run" method ( for initialize environments, load a database, jobrunner ...), now you can write a webservice in Groovy totally, and this webservice may invoke from web or clients. 

### CALL 
#### AJAX
You can call RpcServlet from web by [jquery-jsonrpc] (https://github.com/datagraph/jquery-jsonrpc)
```
#coffee
rpc = $.jsonRPC.setup { endPoint:'level.grpc', namespace:'' }
suc_call = (result) ->
        console?.debug 'ret: %s', JSON.stringify(result)
        ret = result.result
	dosuc(ret)

err_call = (result) ->
	console?.warn 'rpcfail: %s', JSON.stringify(result)
	dofail(result.message)

btnclick = ->
	rpc.request 'methodname', { params:[arg1, arg2], success:suc_call, error:err_call}
...
```
#### OR: other JSONRPC client supports HTTP

## groovy.jsonrpc.tools.RpcServletClient

A HTTP client for JSONRPC, based on [groovy-wslite] (https://github.com/jwagenleitner/groovy-wslite).
```
def client = new groovy.jsonrpc.tools.RpcServletClient('http://ip:port/sth.grpc')
try {
	def result = rpc.call(methodname, arg1, arg2)
	//or more groovy way ( based on groovy.methodMissing )
	def retsult = rpc.methodname(arg1, arg2)
} catch (groovy.jsonrpc.engine.RpcException e){
	pritnln e.message
}
```

## groovy.jsonrpc.tools.RpcServerG

simple NIO JSONRPC server implement based on netty, use it you can communicate with javaclass or groovyclass

