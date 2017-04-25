package com.wenweihu86.rpc.codec.proto3;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class ProtoV3Request {
    private ProtoV3Header.RequestHeader header;
    private byte[] body;

    public ProtoV3Header.RequestHeader getHeader() {
        return header;
    }

    public void setHeader(ProtoV3Header.RequestHeader header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
