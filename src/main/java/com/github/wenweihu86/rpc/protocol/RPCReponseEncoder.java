package com.github.wenweihu86.rpc.protocol;

import com.github.wenweihu86.rpc.protocol.standard.StandardProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCReponseEncoder extends MessageToMessageEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object object, List<Object> out)
            throws Exception {
        ProtocolProcessor protocol = StandardProtocol.instance();
        protocol.encodeResponse(ctx, object, out);
    }
}
