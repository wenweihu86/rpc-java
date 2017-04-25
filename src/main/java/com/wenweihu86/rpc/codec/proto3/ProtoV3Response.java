package com.wenweihu86.rpc.codec.proto3;

/**
 * Created by baidu on 2017/4/25.
 */
public class ProtoV3Response {

    private ProtoV3Header.ResponseHeader header;

    private byte[] body;

    public ProtoV3Header.ResponseHeader getHeader() {
        return header;
    }

    public void setHeader(ProtoV3Header.ResponseHeader header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
