package groovy.jsongrpc.tools;

import groovy.jsongrpc.handler.GMapedHandler;

/**
 * simple NIO JSONRPC server implement based on netty, use it you can
 * communicate with javaclass or groovyclass
 */
public class RpcServerG extends RpcServer {
    public RpcServerG(int port) {
	super(port);
	this.handler = new GMapedHandler();
    }

    public static void main(String[] args) throws Exception {
	int port;
	if (args.length > 0) {
	    port = Integer.parseInt(args[0]);
	} else {
	    port = 8080;
	}
	RpcServerG server = new RpcServerG(port);
	server.handler.register("test.", RpcServer.class.getName(), false);
	server.handler.register("testg.", "test/test.groovy", true);
	server.run();
    }

}
