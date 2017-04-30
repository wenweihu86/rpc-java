package com.wenweihu86.rpc.filter.chain;

import com.wenweihu86.rpc.codec.RPCHeader;
import com.wenweihu86.rpc.codec.RPCMessage;
import com.wenweihu86.rpc.filter.Filter;

import java.util.Iterator;
import java.util.List;

/**
 * Created by wenweihu86 on 2017/4/30.
 */
public abstract class DefaultFilterChain implements FilterChain {

    protected List<Filter> filters;
    protected Iterator<Filter> iterator;
    protected RPCMessage<RPCHeader.RequestHeader> fullRequest;
    protected RPCMessage<RPCHeader.ResponseHeader> fullResponse;

    public void doFilter(RPCMessage<RPCHeader.RequestHeader> fullRequest,
                  RPCMessage<RPCHeader.ResponseHeader> fullResponse) {
        if (fullRequest == null) {
            throw new IllegalArgumentException("fullRequest == null");
        }
        if (this.iterator == null) {
            this.iterator = this.filters.iterator();
        }
        if (this.iterator.hasNext()) {
            Filter nextFilter = this.iterator.next();
            nextFilter.doFilter(fullRequest, fullResponse, this);
        }
        this.fullRequest = fullRequest;
        this.fullResponse = fullResponse;
    }

    public RPCMessage<RPCHeader.RequestHeader> getFullRequest() {
        return fullRequest;
    }

    public RPCMessage<RPCHeader.ResponseHeader> getFullResponse() {
        return fullResponse;
    }

}
