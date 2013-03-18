groovy-jsonrpc
==============

jsonrpc2.0 implementation of java and groovy, simply expose the public static method of java class or groovy script
**note:** it use [FastJSON] (http://code.alibabatech.com/wiki/display/FastJSON/Home) as JSON-processor

# features
* easy: no annotation, interface ...
* light-weight: minimal depends on fastjson.jar and slf4j.jar 
* JSONRPC 2.0 
* dynamic load groovy scripts
* and has a "MapedHandler" dosn't depends on groovy.jar
* auto convert "params" of JSONRPC from list to array( "params":[1,2] fits int add(int a, int b) )
* support initialize some resource scripts( as utility, constants, acquire resource ...)

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
**Warning** the RpcServlet only supports UTF-8 encoding 

## groovy.jsonrpc.tools.RpcServerG

simple NIO JSONRPC server implement based on netty, use it you can communicate with javaclass or groovyclass

