package com.github.wenweihu86.rpc.client.loadbalance;

import com.github.wenweihu86.rpc.client.RPCChannelGroup;
import io.netty.channel.Channel;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class RandomStrategy implements LoadBalanceStrategy {

    private Random random = new Random(System.currentTimeMillis());

    private static RandomStrategy instance = new RandomStrategy();

    public static RandomStrategy instance() {
        return instance;
    }

    @Override
    public RPCChannelGroup.ChannelInfo selectChannel(CopyOnWriteArrayList<RPCChannelGroup> allConnections) {
        long totalHostCount = allConnections.size();
        if (totalHostCount == 0) {
            return null;
        }

        int index = (int) (getRandomLong() % totalHostCount);
        RPCChannelGroup channelGroup = allConnections.get(index);
        int subIndex = (int) (getRandomLong() % channelGroup.getConnectionNum());
        Channel channel = channelGroup.getChannel(subIndex);
        return new RPCChannelGroup.ChannelInfo(channelGroup, channel);
    }

    private long getRandomLong() {
        long randomIndex = random.nextLong();
        if (randomIndex < 0) {
            randomIndex = 0 - randomIndex;
        }
        return randomIndex;
    }
}
