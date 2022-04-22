package games.core.application;

import games.core.application.handler.HandlerChannelContext;
import games.core.application.handler.UdpServerInitializer;
import games.core.application.handler.websocket.WebSocketServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import javax.mail.MessagingException;
import java.net.InetAddress;

/**
 * Created by tuanhoang on 10/14/17.
 */
public class   UdpServer {
    private int port;

    public UdpServer(int port) {
        this.port = port;
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup	workerGroup;
    HandlerChannelContext handler;
    private Thread gameThread;
    public UdpServer() throws MessagingException {
        handler = HandlerChannelContext.getInstance();
        gameThread = new Thread(() -> {
            try{
                WebSocketServer.start(handler);

                bossGroup = new NioEventLoopGroup();
                workerGroup = new NioEventLoopGroup();
                //handle game
                Bootstrap xserverBootstrap = new Bootstrap();
                xserverBootstrap.group(workerGroup).channel(NioDatagramChannel.class)
                        .option(ChannelOption.SO_BROADCAST, true)
                        .handler(new UdpServerInitializer(handler));
                String host = "127.0.0.1";
                InetAddress address  = InetAddress.getLocalHost();
                int port = 25249;
                ChannelFuture f = xserverBootstrap.bind(address, port).sync() ;
                int cores = Runtime.getRuntime().availableProcessors();
                System.out.printf("Start upd server with - " + cores);
                f.channel().closeFuture().await();
//                f.await();
                System.out.printf("Started upd server");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.printf("Shutdown upd server");
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });
        gameThread.start();

    }

    public static void main(String[] args) throws Exception {

        new UdpServer();
    }
}
