package kr.co.metabuild.kotris.file.echo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EchoClientHandler.class);

    // 채널이 활성화되면 호출됨
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("channelActive");
        // 메시지 전송
        String message = "안녕하세요! Netty 에코 클라이언트입니다.";
        logger.debug("클라이언트에서 송신한 메시지: [{}]", message);
        byte[] bytes = message.getBytes();
        ByteBuf buffer = ctx.alloc().buffer(bytes.length);
        buffer.writeBytes(bytes);
        ctx.writeAndFlush(buffer);
    }

    // 메시지를 수신하면 호출됨
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("channelRead");
        ByteBuf in = (ByteBuf) msg;
        byte[] received = new byte[in.readableBytes()];
        in.readBytes(received);
        System.out.println("서버로부터 수신한 메시지: " + new String(received));
        logger.debug("서버로부터 수신한 메시지: [{}]", new String(received));
        ctx.close(); // 통신 완료 후 채널 닫기
    }

    // 예외 발생 시 호출됨
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug("exceptionCaught");
        cause.printStackTrace();
        ctx.close();
    }


}

