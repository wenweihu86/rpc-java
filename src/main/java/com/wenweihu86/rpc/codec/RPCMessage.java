package com.wenweihu86.rpc.codec;

import com.google.protobuf.GeneratedMessageV3;

/**
 * Created by wenweihu86 on 2017/4/26.
 */
public class RPCMessage<T extends GeneratedMessageV3> {

    private T header;
    private byte[] body;
    private GeneratedMessageV3 bodyMessage;
    private Class responseBodyClass;

    public RPCMessage<T> copyFrom(RPCMessage<T> rhs) {
        this.header = rhs.getHeader();
        this.body = rhs.getBody();
        this.bodyMessage = rhs.getBodyMessage();
        this.responseBodyClass = rhs.getResponseBodyClass();
        return this;
    }

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

    public GeneratedMessageV3 getBodyMessage() {
        return bodyMessage;
    }

    public void setBodyMessage(GeneratedMessageV3 bodyMessage) {
        this.bodyMessage = bodyMessage;
    }

    public Class getResponseBodyClass() {
        return responseBodyClass;
    }

    public void setResponseBodyClass(Class responseBodyClass) {
        this.responseBodyClass = responseBodyClass;
    }
}
