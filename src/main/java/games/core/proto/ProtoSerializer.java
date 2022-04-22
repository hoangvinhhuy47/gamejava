package games.core.proto;

import com.google.protobuf.Message;

@FunctionalInterface
public interface ProtoSerializer<B extends Message> {

    B.Builder parseProtoBuilder();
    @SuppressWarnings("unchecked")
    default B getProtoMessage(){
        return (B)parseProtoBuilder().build();
    }
}