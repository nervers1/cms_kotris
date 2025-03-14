package kr.co.metabuild.kotris.file.cms;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.concurrent.BlockingQueue;

public class CMSInitializer extends ChannelInitializer<SocketChannel> {
    BlockingQueue<ByteBuf> responseQueue;

    public CMSInitializer(BlockingQueue<ByteBuf> responseQueue) {
        this.responseQueue = responseQueue;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(4100, 0, 4, 0, 0, true), new CMSHandler(responseQueue));
    }
}
