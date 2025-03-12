package kr.co.metabuild.kotris.file.echo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EchoServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("Echo Server Handler: channelRead");
        // 수신된 메시지를 그대로 송신자에게 돌려줌
        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Echo Server Handler: channelReadComplete");
        // 대기 중인 메시지를 모두 flush하여 원격 피어로 전송
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug("Echo Server Handler: exceptionCaught");
        // 예외 발생 시 스택 트레이스를 출력하고 채널을 닫음
        cause.printStackTrace();
        ctx.close();
    }
}
