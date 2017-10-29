package com.github.wenweihu86.rpc.client.loadbalance;

import com.github.wenweihu86.rpc.client.RPCChannelGroup;

import java.util.concurrent.CopyOnWriteArrayList;

public interface LoadBalanceStrategy {
    RPCChannelGroup.ChannelInfo selectChannel(CopyOnWriteArrayList<RPCChannelGroup> allConnections);
}
