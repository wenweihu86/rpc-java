package com.wenweihu86.rpc.codec;

import com.google.protobuf.GeneratedMessageV3;

/**
 * Created by wenweihu86 on 2017/4/26.
 */
public class RPCMessage<T extends GeneratedMessageV3> {

    private T header;
    private byte[] body;

    public T getHeader() {
        return header;
    }

    public void setHeader(T header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
