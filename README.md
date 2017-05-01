# 背景
rpc-java是一个基于netty4和protobuf3的java rpc框架。
rpc-java并没有引入与spring集成相关功能，也没有引入注册中心，
所以不适合于SOA场景（当然如果有感兴趣的话，也可以加上对SOA的相关支持）。
rpc-java的主要使用场景是高并发server系统，如分布式消息队列、分布式存储系统等。

# 通信协议
客户端与服务端交互的协议格式为：<br>
4字节Header长度 + 4字节Body长度 + Header + Body。<br>
body为用户自定义的protobuf message结构。

Header格式如下：<br>
```protobuf
// 请求Header
message RequestHeader {
    string serviceName = 1; // 服务名或接口名
    string methodName = 2; // 方法名
    string logId = 3; // 日志id
    CompressType compressType = 4; // body消息压缩类型，0：不压缩，1：SNAPPY压缩，2：GZIP压缩
    map<string, string> customParam = 5; // 用户自定义参数
}

// 响应Header
message ResponseHeader {
    string logId = 1; // 请求的logId
    ResCode resCode = 2; // 返回码，0：成功，1：失败
    string resMsg = 3; // 返回失败时的错误消息
}
```

# 使用方法

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
public interface SampleService {
    Sample.SampleResponse sampleRPC(Sample.SampleRequest request);
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

        List<Filter> filters = new ArrayList<>();
        ServerCustomParamFilter filter = new ServerCustomParamFilter();
        filters.add(filter);
        RPCServer rpcServer = new RPCServer(port, filters);
        rpcServer.registerService(new SampleServiceImpl());
        rpcServer.start();
    }
}
```

## 客户端开发
```java
public class RPCClientTest {

    public static void main(String[] args) {
        RPCClientOption clientOption = new RPCClientOption();
        clientOption.setWriteTimeoutMillis(200);
        clientOption.setReadTimeoutMillis(500);

        String ipPorts = "127.0.0.1:8766";
        if (args.length == 1) {
            ipPorts = args[0];
        }

        List<Filter> filters = new ArrayList<>();
        ClientCustomParamFilter customParamFilter = new ClientCustomParamFilter();
        filters.add(customParamFilter);
        RPCClient rpcClient = new RPCClient(ipPorts, clientOption, filters);

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
        rpcClient.asyncCall("SampleService.sampleRPC", request, callback);
    }

}
```

# 性能
QPS可达50000+
