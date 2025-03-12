package kr.co.metabuild.kotris.file.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoClient {
    private static final Logger logger = LoggerFactory.getLogger(EchoClient.class);
    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        // 클라이언트용 스레드 그룹 생성
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            // 클라이언트 설정을 도와주는 헬퍼 클래스
            Bootstrap b = new Bootstrap();
            b.group(group)
                    // NIO 소켓 채널 사용
                    .channel(NioSocketChannel.class)
                    // 서버 주소와 포트로 연결
                    .remoteAddress(host, port)
                    // 채널 초기화 설정
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });

            // 서버에 연결하고 채널이 닫힐 때까지 대기
            ChannelFuture f = b.connect().sync();
            f.channel().closeFuture().sync();
        } finally {
            // 이벤트 루프 그룹 종료
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        logger.debug("Starting EchoClient...");
        new EchoClient("localhost", 8000).start();
    }
}

