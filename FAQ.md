1. What's groovy-jsonrpc
	> A Java implementation of JSON-RPC(2.0), very simple (like JSON-RPC itself).
1. When to use it
	> Want exposing a java program to other applications over net
1. How it works
	> This RPC finds all public static method to a hashmap, when a request comes, use the "params" invoke the method by the "method". It use [fast-json][fast-json] to process json (It's very fast^_^). 
1. How to use it
	* Create a RpcServer, register the classes to expose, and run
	* Or: implement a RpcServer youself, it's a demo tool, only 57 line codes.
1. Why groovy
	> There two kind of class, one is pure java, the one supports invoke groovy script from file. When the method has complicated parameter that JSON does not support, implement a groovy wrapper script is a easy way. Create a RpcServerG, register the class or groovy file to the server, and run.
1. What about Web
	> We have a RpcServlet (based on [GroovyServlet][gs]). When call the urlwith JSON data through web, RpcServlet compile the script and invoke the public static method(do some DB work use groovy.sql.Sql, etc). When the data is not a JSON data, RpcServlet works the same as GroovyServlet.
1. Groovy seems good, BUT where can I put the common classes(common utils, etc) 
	* Write and compile a java class straight, OR
	* Pass "initbase" parameter to RpcServlet, "initbase" is the script urls to be loaded (seperated by ";"). the RpcServelt will load and run them one by one. You can start databse, initialize DB pool, or anything you want. Now use RpcServlet you can write a web application purely in groovy.
	* "initbase" is avaliable to GMapedHandler, so You may init a RpcServerG too.
1. I want ...
	> RpcServlet/RpcServer are demo tools, you may create you own use groovy.jsonrpc.handler.

[fast-json]: http://code.alibabatech.com/wiki/display/FastJSON/Documentation "Fast JSON Processor"
[gs]: http://groovy.codehaus.org/Groovlets "Groovlets"
