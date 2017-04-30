package com.wenweihu86.rpc.filter.chain;

import com.wenweihu86.rpc.client.RPCClient;
import com.wenweihu86.rpc.filter.AbstractClientFilter;
import com.wenweihu86.rpc.filter.ClientInvokeFilter;
import com.wenweihu86.rpc.filter.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenweihu86 on 2017/4/30.
 */
public class ClientFilterChain extends DefaultFilterChain {

    public ClientFilterChain(List<Filter> filters, RPCClient rpcClient) {
        this.filters = new ArrayList<>();
        if (filters != null && filters.size() > 0) {
            this.filters.addAll(filters);
        }
        this.filters.add(new ClientInvokeFilter());
        for (Filter filter : this.filters) {
            if (AbstractClientFilter.class.isAssignableFrom(filter.getClass())) {
                ((AbstractClientFilter) filter).setRPCClient(rpcClient);
            }
        }
    }
}
