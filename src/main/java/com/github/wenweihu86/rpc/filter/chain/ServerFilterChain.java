package com.github.wenweihu86.rpc.filter.chain;

import com.github.wenweihu86.rpc.filter.Filter;
import com.github.wenweihu86.rpc.filter.ServerInvokeFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenweihu86 on 2017/4/30.
 */
public class ServerFilterChain extends DefaultFilterChain {

    public ServerFilterChain(List<Filter> filters) {
        this.filters = new ArrayList<>();
        if (filters != null && filters.size() > 0) {
            this.filters.addAll(filters);
        }
        this.filters.add(new ServerInvokeFilter());
    }

}
