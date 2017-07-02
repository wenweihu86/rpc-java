package com.github.wenweihu86.rpc.client;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by wenweihu86 on 2017/5/17.
 */
public class EndPoint {
    String ip;
    int port;

    public EndPoint(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(ip)
                .append(port)
                .toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        boolean flag = false;
        if (object != null && EndPoint.class.isAssignableFrom(object.getClass())) {
            EndPoint rhs = (EndPoint) object;
            flag = new EqualsBuilder()
                    .append(ip, rhs.ip)
                    .append(port, rhs.port)
                    .isEquals();
        }
        return flag;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
