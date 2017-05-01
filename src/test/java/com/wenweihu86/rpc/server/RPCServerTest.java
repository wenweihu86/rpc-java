package com.wenweihu86.rpc.server;

import com.wenweihu86.rpc.filter.Filter;
import com.wenweihu86.rpc.filter.ServerCustomParamFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCServerTest {
    public static void main(String[] args) {
        int port = 8766;
        if (args.length == 1) {
            port = Integer.valueOf(args[0]);
        }

        List<Filter> filters = new ArrayList<>();
//        ServerCustomParamFilter filter = new ServerCustomParamFilter();
//        filters.add(filter);
        RPCServer rpcServer = new RPCServer(port, filters);
        rpcServer.registerService(new SampleServiceImpl());
        rpcServer.start();
    }
}
