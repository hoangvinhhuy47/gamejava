package games.core.application.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import message.XProtos;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class GameServerInitializer extends ChannelInitializer<SocketChannel> {

    private final HandlerChannelContext handlerChannelContext;
    private final ProtobufDecoder protobufDecoder;
    private final ProtobufVarint32LengthFieldPrepender protobufVarint32LengthFieldPrepender;
    private final ProtobufEncoder protobufEncoder;

    public GameServerInitializer(HandlerChannelContext handlerChannelContext) {
        protobufDecoder = new ProtobufDecoder(XProtos.XMessage.getDefaultInstance());
        protobufVarint32LengthFieldPrepender = new ProtobufVarint32LengthFieldPrepender();
        protobufEncoder = new ProtobufEncoder();
        this.handlerChannelContext = handlerChannelContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        System.out.printf("Init channel");
        ChannelPipeline p = ch.pipeline();
        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        p.addLast("protobufDecoder", protobufDecoder);



        p.addLast("frameEncoder", protobufVarint32LengthFieldPrepender);
        p.addLast("protobufEncoder", protobufEncoder);

        p.addLast("handler", handlerChannelContext);

    }
}
