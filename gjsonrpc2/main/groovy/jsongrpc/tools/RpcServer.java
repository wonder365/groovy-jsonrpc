package groovy.jsongrpc.tools;

import groovy.jsongrpc.handler.MapedHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;


public class RpcServer {
    private final int port;
    public MapedHandler handler;

    public RpcServer(int port) {
	this.port = port;
	this.handler = new MapedHandler();
    }

    public void run() {
	// Configure the server.
	ServerBootstrap bootstrap = new ServerBootstrap(
		new NioServerSocketChannelFactory(
			Executors.newCachedThreadPool(),
			Executors.newCachedThreadPool()));

	// Configure the pipeline factory.
	bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
	    public ChannelPipeline getPipeline() throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = Channels.pipeline();

		// Add the text line codec combination first,
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192,
			Delimiters.lineDelimiter()));
		// and then business logic.
		pipeline.addLast("handler", new RpcServerHandler(handler));

		return pipeline;
	    }
	});

	// Bind and start to accept incoming connections.
	bootstrap.bind(new InetSocketAddress(port));
    }

    public static String test() {
	return "return from server";
    }

    public static void main(String[] args) throws Exception {
	int port;
	if (args.length > 0) {
	    port = Integer.parseInt(args[0]);
	} else {
	    port = 8080;
	}
	RpcServer server = new RpcServer(port);
	server.handler.register("test.", RpcServer.class.getName(), false);
	server.run();
    }

}
