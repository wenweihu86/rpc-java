# 背景
rpc-java是一个基于netty4和protobuf3的java rpc框架，
主要使用场景是高并发server系统，如分布式消息队列、分布式存储系统等。

# 通信协议
客户端与服务端交互的协议格式为：<br>
4字节Header长度 + 4字节Body长度 + Header + Body。<br>
body为用户自定义的protobuf message结构。

Header格式如下：<br>
```protobuf
// 请求Header
message RequestHeader {
    string service_name = 1; // 服务名或接口名
    string method_name = 2; // 方法名
    string call_id = 3; // 请求id
    CompressType compress_type = 4; // body消息压缩类型，0：不压缩，1：SNAPPY压缩，2：GZIP压缩
    map<string, string> custom_param = 5; // 用户自定义参数
}

// 响应Header
message ResponseHeader {
    string call_id = 1; // 请求id
    ResCode res_code = 2; // 返回码，0：成功，1：失败
    string res_msg = 3; // 返回失败时的错误消息
}
```

# 使用方法
## 配置依赖
```
<dependency>
    <groupId>com.github.wenweihu86.rpc</groupId>
    <artifactId>rpc-java</artifactId>
    <version>1.7.0</version>
</dependency>
```

## 定义api接口
### 定义请求和响应的protobuf message结构
```protobuf
message SampleRequest {
    int32 a = 1;
    string b = 2;
}

message SampleResponse {
    string c = 1;
}
```

### 定义java接口类
```java
// 同步调用接口
public interface SampleService {
    Sample.SampleResponse sampleRPC(Sample.SampleRequest request);
}
// 异步调用接口
public interface SampleServiceAsync extends SampleService {
    Future<Sample.SampleResponse> sampleRPC(Sample.SampleRequest request,
                                       RPCCallback<Sample.SampleResponse> callback);
}
```

## 服务端开发
### 接口实现类
```java
public class SampleServiceImpl implements SampleService {

    @Override
    public Sample.SampleResponse sampleRPC(Sample.SampleRequest request) {
        String c = request.getB() + request.getA();
        Sample.SampleResponse response = Sample.SampleResponse.newBuilder()
                .setC(c).build();
        return response;
    }
}
```
### 服务端启动类
```java
public class RPCServerTest {
    public static void main(String[] args) {
        int port = 8766;
        if (args.length == 1) {
            port = Integer.valueOf(args[0]);
        }

        RPCServer rpcServer = new RPCServer(port);
        rpcServer.registerService(new SampleServiceImpl());
        rpcServer.start();

        // make server keep running
        synchronized (RPCServerTest.class) {
            try {
                RPCServerTest.class.wait();
            } catch (Throwable e) {
            }
        }
    }
}
```

## 客户端开发
```java
public class RPCClientTest {

    public static void main(String[] args) {
        RPCClientOptions clientOption = new RPCClientOptions();
        clientOption.setWriteTimeoutMillis(200);
        clientOption.setReadTimeoutMillis(500);

        String ipPorts = "127.0.0.1:8766";
        if (args.length == 1) {
            ipPorts = args[0];
        }

        RPCClient rpcClient = new RPCClient(ipPorts, clientOption);

        // build request
        Sample.SampleRequest request = Sample.SampleRequest.newBuilder()
                .setA(1)
                .setB("hello").build();

        final JsonFormat.Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
        // sync call
        SampleService sampleService = RPCProxy.getProxy(rpcClient, SampleService.class);
        Sample.SampleResponse response = sampleService.sampleRPC(request);
        if (response != null) {
            try {
                System.out.printf("sync call service=SampleService.sampleRPC success, " +
                                "request=%s response=%s\n",
                        printer.print(request), printer.print(response));
            } catch (InvalidProtocolBufferException ex) {
                System.out.println(ex.getMessage());
            }

        } else {
            System.out.println("server error, service=SampleService.sampleRPC");
        }

        // async call
        RPCCallback callback = new RPCCallback<Sample.SampleResponse>() {
            @Override
            public void success(Sample.SampleResponse response) {
                try {
                    System.out.printf("async call SampleService.sampleRPC success, response=%s\n",
                            printer.print(response));
                } catch (InvalidProtocolBufferException ex) {
                    System.out.println(ex.getMessage());
                }
            }

            @Override
            public void fail(Throwable e) {
                System.out.printf("async call SampleService.sampleRPC failed, %s\n", e.getMessage());
            }
        };
        SampleServiceAsync asyncSampleService = RPCProxy.getProxy(rpcClient, SampleServiceAsync.class);
        Future future = asyncSampleService.sampleRPC(request, callback);
        try {
            if (future != null) {
                future.get();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        rpcClient.stop();
    }

}
```
