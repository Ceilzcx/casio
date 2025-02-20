## Casio——RPC远程调用框架

### 1、项目结构

+ casio-core【核心代码模块】
  + boostrap：启动辅助类（启动框架时，代码整洁）
  + common：公用包
    + annotation：注解（服务注册、消费、SPI）
    + exception：自定义异常
      + `RemotingException.class`：远程连接异常
      + `RpcException.class` ：RPC调用异常
    + extension：SPI，参照Dubbo实现，没有dubbo那么复杂
    + utils：通用工具类
    + cache：缓存实现（LRU和LFU）
  + config：
    + context：启动上下文相关（注册中心、服务提供者、服务消费者等等）
    + parser：xml和yaml的解析
  + domain
    + `RpcMessage.class`：消息传递的数据通用类型，数据通过protocol包装后的类
    + `RpcRequest.class`：服务消费者请求
    + `RpcResponse.class`：服务提供者调用服务后返回结果
    + `RpcStatus`：状态和异常
  + monitor：监控
  + registry：注册中心相关
    + cache：注册中心相关信息缓存，不需要重复调用注册中心
    + nacos（未实现）
    + zookeeper
    + `ServiceDiscovery.class`：服务发现接口
    + `ServiceRegistry.class` ： 服务注册接口
  + remoting：远程连接
    + http：http形式调用，类似Spring cloud
    + netty：netty连接
  + rpc：与远程连接模块区分，是远程连接前置和后置工作
    + cluster：集群相关，负载均衡等
    + filter：前置过滤
    + protocol：协议，`com.report.casio.remoting.transport.netty.codec`调用
    + proxy：代理，客户端和服务端
+ casio-embed：内嵌其他应用（例：内嵌Zookeeper，不需要额外安装启动zk）
+ casio-spring：结合Spring模块
+ casio-spring-test：casio-spring模块的测试类
+ casio-test：casio-core模块的测试类



### 二、实现原理

RPC分为服务提供者和消费者两部分。

RPC调用流程：消费者通过类生成对应的URL，访问注册中心，拿到提供者所在的IP地址，通过负载均衡选择其中一个IP地址，进行连接（Netty），将服务名、方法名、参数等信息传递给提供者，提供者调用实际的实现类，将结果包装后返回给消费者，消费者将结果输出。

#### 提供者

启动时扫描项目中 `@Register` 的类，将类注册进注册中心，这里就需要几个部分：注册中心配置类、注册中心连接工具类，扫描类、已经服务注册类。

目前只实现zookeeper注册中心，工具类采用 `org.apache.curator ` 。

注册仿照dubbo的方式，但功能相对少了很多，因此路径也优化了很多。路径格式 `casio/serviceName/provider` 。

扫描类 `AnnScanUtils` ，没有使用 Spring 结合实现，这个扫描类通过路径递归查找判断扫描，可能存在问题。

NettyServer：等待消费者发送消息，调用后返回结果

#### 消费者

服务发现类 `ServiceDiscovery`：通过方法名生成对应路径，去注册中心获取ip，每次调用都需要访问注册中心，会对注册中心造成比较大的压力，因此第一次调用获取后将结果缓存起来，通过添加watcher，如果该节点发生变化，删除对应的缓存。缓存需要考虑超过cacheSize后的删除机制，这里参考dubbo

NettyClient：消费者比较复杂，首先你需要拿到对应IP的 Channel，进行消息发送，其次需要等待提供者将消息返回，这里的实现采用 Future 模式实现（不用通过阻塞的方式实现 wait、notify，多线程会出现问题）。



#### SPI机制

仿照 dubbo 实现，只实现了扫描文件获取对应的类，源码还有类属性的注入等实现。



#### 粘包和拆包——采用协议
```zsh
+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+ 
|  BYTE  |   BYTE |  BYTE  |           int            |         ........         |        | 
+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+ 
|  magic | version|  type  |       content length     |            content byte[]                  | 
+--------+--------------------------------------------+--------+--------+--------+--------+--------+
```
> `magic`:固定为 0x27(00011011)   
> `version`:区分协议解析器版本  
> `type`:请求类型  
> `content length`:消息长度  
> `content byte[]`:消息内容



#### 超时和重试机制

重试的设置最好是在提供者，因为提供者对业务熟悉，能够合理的设置重试次数。

dubbo的超时针对消费者，消费者轮询Future发现超时时，不再等待该返回结果，并不意味着该线程结束执行。



### 三、其他

+ Rpc涉及到类在网络上的传输，因此需要注意几点：序列化；不需要传输，但是本地需要用到的属性添加 `transient` 修饰符；类的长度丢失问题，涉及协议
+ 在Rpc中会涉及到需要多线程的问题，因此 `ConcurrentMap` 的使用非常必要，在累加的时候需要使用 `AtomicInteger`。
+ `CompletableFuture` 类的使用，这个类在异步中使用非常强大。
+ LRU和LFU算法：一定程度上参考了dubbo。我的LRU是将所有被调用的service全部添加，这样会有频繁切换的缓存的问题，dubbo有一个Map记录service访问次数，达到某个阈值才会被添加到缓存。LFU算法通过Frequency table + 链表实现，具体参考 `com.report.casio.common.cache.LFUCache`




#### 存在问题或改进

+ 服务发现的缓存存在问题，测试时发现服务提供者断线之后，注册中心的节点存在延迟，这个时候调用会发现服务提供者并不存在，需要添加重试机制。
+ 目前消息传递使用序列化和对象转byte数组实现，可以考虑其他，例：kyro、protobuff（GRPC）
+ 启动的信息不知道该如何加载和放置，目前全部堆积在 config 下，存在一定问题。
+ 配置文件的读取也没有实现，目前zookeeper等配置信息都是写死的



#### 参考

参考阿里文章：https://zhuanlan.zhihu.com/p/388848964

dubbo项目：https://github.com/apache/dubbo

几位大佬自己实现的RPC框架（相比我的完善许多，给我提供了许多思路）：https://github.com/Snailclimb/guide-rpc-framework



联系方式：QQ1758619238
