package games.core.application;

import games.core.application.handler.ApiServerInitializer;
import games.core.application.handler.HandlerChannelApi;
import games.core.application.handler.HandlerChannelContext;
import games.core.application.handler.GameServerInitializer;
import games.core.application.channel.ChannelService;
import games.core.application.handler.websocket.WebSocketServer;
import games.core.database.InitDatabase;
import games.core.database.MongoManager;
import games.core.database.RedisManager;
import games.shootingfish.GameLobby;
import libs.util.LogFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

import javax.mail.MessagingException;

/**
 * Created by Cuong Nguyen Cao on 9/8/17.
 */
public class GameServer {

    private Logger log;
    private HandlerChannelContext handler;
    private HandlerChannelApi handlerChannelApi;
    private ServerConfig serverConfig;
    private EventLoopGroup bossGroup;
    private EventLoopGroup	workerGroup;
    private EventLoopGroup bossGroupApi;
    private EventLoopGroup workerGroupApi;
    private Thread gameThread;
    private Thread apiThread;

    private static GameServer instance;

    public static void main(String[] args){
        getInstance();
    }

    public synchronized static GameServer getInstance(){
        if (instance == null){
            instance = new GameServer();
        }
        return instance;
    }

    private GameServer(){
        log = LogFactory.getLogger(this.getClass().getSimpleName());
        log.info("Start init server...");

        RedisManager.getInstance();
        log.info("Init Redis done!");

        MongoManager.getInstance();
        log.info("Init Mongo done!");

        serverConfig = ServerConfig.getInstance();
        log.info("Init ServerConfig(load from server_config.json) done!");

        //Khởi tạo 1 số database cần để test
        //DefaultDatabase.getInstance().init();

        //Migrate database
        InitDatabase.migrate();
        log.info("Migrate database done!");

        GameLobby.getInstance();
        log.info("Init Fishing gameLobby done!");

        ChannelService.getInstance();
        log.info("Init ChannelService done!");

        handler = HandlerChannelContext.getInstance();
        log.info("Init HandlerChannelContext done (Also init all game instance)!");

        handlerChannelApi = HandlerChannelApi.getInstance();
        log.info("Init HandlerChannelApi done!");


        gameThread = new Thread(() -> {
            try{
                WebSocketServer.start(handler);

                bossGroup = new NioEventLoopGroup();
                workerGroup = new NioEventLoopGroup();
                //handle game
                ServerBootstrap xserverBootstrap = new ServerBootstrap();
                xserverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).
                        childHandler(new GameServerInitializer(handler)).option(ChannelOption.SO_BACKLOG, 128).
                        childOption(ChannelOption.SO_KEEPALIVE, true);
                String host = serverConfig.getGameServerHost();
                int port = serverConfig.getGameServerPort();
                ChannelFuture f = xserverBootstrap.bind(host, port).sync();
                int cores = Runtime.getRuntime().availableProcessors();
                log.info("GAME SERVER STARTED AT HOST[" + host + "] PORT [" + port + "] HAS [" + cores + "] CORES");
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error(e);
            } finally
            {
                log.info("CLOSE SERVER GAME");
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });
        gameThread.start();

        apiThread = new Thread(() -> {
            try{
                bossGroupApi = new NioEventLoopGroup();
                workerGroupApi = new NioEventLoopGroup();
                //Handle http request
                ServerBootstrap httpReqApiBootstrap = new ServerBootstrap();
                httpReqApiBootstrap.group(bossGroupApi, workerGroupApi)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ApiServerInitializer(handlerChannelApi))
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, false);
                // TODO: move to config
                String host = ServerConfig.getInstance().getApiHost();
                int port = ServerConfig.getInstance().getApiPort();
                ChannelFuture channelFuture = httpReqApiBootstrap.bind(host, port).sync();
                log.info("API SERVER STARTED AT HOST[" + host + "] PORT [" + port + "]");
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e){
                log.error(e);
            } finally {
                log.info("CLOSE SERVER GAME API");
            }

        });
        apiThread.start();

    }

    public void stopGameServer(){
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        gameThread = null;
        WebSocketServer.stop();

    }

    public void stopAPIServer(){
        workerGroupApi.shutdownGracefully();
        bossGroupApi.shutdownGracefully();
        apiThread = null;
    }
}
