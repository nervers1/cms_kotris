package kr.co.metabuild.kotris.file.cms;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;

public class CMSHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CMSHandler.class);
    BlockingDeque<ByteBuf> responseQueue;

    public CMSHandler(BlockingDeque<ByteBuf> responseQueue) {
        this.responseQueue = responseQueue;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("CMSchannelActive : {}", ctx.channel().toString());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("CMSchannelInactive : {}", ctx.channel().toString());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.debug("CMSchannelRegistered : {}", ctx.channel().toString());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        logger.debug("CMSchannelUnregistered : {}", ctx.channel().toString());
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        ByteBuf output = Unpooled.copiedBuffer(buf);
        responseQueue.offer(output);
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.debug(cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        logger.debug("CMSchannelWritabilityChanged : {}", ctx.channel().toString());
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("CMSchannelReadComplete : {}", ctx.channel().toString());
        super.channelReadComplete(ctx);
    }
}
