package kr.co.metabuild.kotris.file.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServer {
    private static final Logger logger = LoggerFactory.getLogger(EchoServer.class);
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        logger.debug("Echo Server: start!");
        // 연결을 수락하는 스레드 그룹 (Boss Group)
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);

        // 실제 데이터 처리를 담당하는 스레드 그룹 (Worker Group)
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 서버 설정을 도와주는 헬퍼 클래스
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    // NIO 전송 채널을 사용하도록 지정
                    .channel(NioServerSocketChannel.class)
                    // 새로운 채널이 생성될 때 호출될 초기화 클래스 지정
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            // 채널 파이프라인에 사용자 지정 핸들러 추가
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            // 서버를 지정된 포트로 바인딩하고 시작
            ChannelFuture f = b.bind(port).sync();
            System.out.println("에코 서버가 시작되었습니다. 포트: " + port);
            logger.debug("에코 서버가 시작되었습니다. 포트: {}", port);

            // 서버 소켓이 닫힐 때까지 대기
            f.channel().closeFuture().sync();
        } finally {
            // 모든 이벤트 루프를 종료하여 스레드 종료
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        logger.debug("Echo Server: main executed ...");
        new EchoServer(8000).start();
    }
}
