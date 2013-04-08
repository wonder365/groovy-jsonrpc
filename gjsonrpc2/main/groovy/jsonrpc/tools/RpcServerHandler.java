package groovy.jsonrpc.tools;

import groovy.jsonrpc.handler.MapedHandler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcServerHandler extends SimpleChannelUpstreamHandler {
    static final Logger logger = LoggerFactory
	    .getLogger(RpcServerHandler.class);
    private MapedHandler mhandler;

    public RpcServerHandler(MapedHandler mhandler) {
	this.mhandler = mhandler;
    }

    static byte[] dlm = new byte[] { '\r', '\n' };

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
	    throws Exception {
	byte[] data = ((ChannelBuffer) e.getMessage()).array();
	byte[] rsp = mhandler.call(data);
	if (rsp.length > 0) {
	    ChannelBuffer buf = ChannelBuffers.copiedBuffer(rsp, dlm);
	    e.getChannel().write(buf);
	}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext arg0, ExceptionEvent arg1)
	    throws Exception {
	logger.warn("exceptionCaught {} : {}", arg1.getChannel()
		.getRemoteAddress(), arg1.getCause().getMessage());
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
	    throws Exception {
	logger.warn("channelConnected {}", e.getChannel().getRemoteAddress());
    }
}
