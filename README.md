basic user doc
===============

retry
-----------------

1. Coordinator在调用各个服务的confirm或cancel时，会根据各个服务设置的retry次数来retry。

2. 当Coordinator主动抛出异常时，如dubbo超时异常，LogException和IllegalActionException，会根据客户端和coordinator的设置进行retry。

3. 客户达可以自行设置Coordinator的retry次数，Coordinator设置retry次数为2，客户端的设置会覆盖掉服务端的设置。

4. Heuristics异常在与Coordinator交互过程中直接返回code，不抛异常（客户端会根据code生成异常），因此不会retry。
	这样做是因为coordinator内已经retry过了，再换个Coordinator retry意义不大。

EXCEPTION:
-----------------

客户端可能抛出以下几个异常：

1. LogException: 
	若Coordinator的日志系统不可用，或发生瞬间的错误，会抛出这个异常，当客户端设置了重试，基本可以杜绝这种异常，除非RDS网络发生分区，导致多个Coordinator的日志系统同时发生不可用的情况。

2. IllegalActionException: 
	当一个Coordinator在一个事务的生命周期内收到重复的confirm或cancel请求时，会抛这种异常，对这个异常要注意以下几点：
	a) 当一个Coordinator与客户端发生瞬间的网络分区，在重试机制下，可能造成两个Coordinator收到重复的confirm或cancel请求，这种情况下不会抛异常，并且两个coordinator都会去confirm，需要Participant保证幂等性。
	b) 一个事务confirm，cancel或expire成功后，会从Coordinator内的事务表删除，这时认为事务生命周期结束，如果再次提交这个事务的confirm，会被当做新事务再执行一遍，不会抛异常。
	c) 若一个事务注册成功后，经历了一个expire时间（很长，可以设置）才去confirm或cancel，可能因为事务正在expire而抛出这个异常。
	d) 只要客户端没有写错误的代码，基本上不会遇到这种异常。

3. CoordinatorException: 当某个Participant的confirm或cancel抛出异常后，coordinator会返回客户端这个异常，并且把相关的ErrorCode返回，通过
	short getErrorCode()
	获取异常码

4. TimeoutException:
	当某个Participant超时时会抛出这种异常，如果Coordinator超时则会在客户端抛出RpcException(Runtime)

5. ServiceUnavailableException:
	当某个Participant初始化失败，或无法访问时抛出这个异常


ERROR CODE:
-----------------

建议应用Participant在每个自主抛出的PartipantException中都设置ErrorCode，并且客户端可以对ErrorCode自行区分，建议使用构造函数:

public ParticipantException(String message, short code)

若使用带有cause的构造，可能造成客户端无法找到cause对应的类而抛错的情况

code的类型为short, 设置范围为：0-0x3FFF



TCC V1.0部署文档
===============
http://doc.hz.netease.com/pages/viewpage.action?pageId=41845962

打包与部署：
1. 首先从git上拉下代码：https://git.hz.netease.com/tcc/tcc，也可以通过其他渠道获取打包后的tar包，已经获取tar包的话直接从第4步开始。

git clone ssh://XXXX@git.hz.netease.com:22222/tcc/tcc.git

2. 对代码进行打包(确保装有maven，并设置了相应settings.xml)：

cd tcc
mvn clean
mvn compile
mvn package
依次执行。

3. 在tcc/tcc_coordinator/target目录下找到打包完成的coordinator tar包：

tcc_coordinator-X-assembly.tar.gz
除此之外，还有DEMO程序打包位置在tcc_demo_consumer/target/和cc_demo_server/target/下，分别找到他们：

tcc_demo_consumer-X-assembly.tar.gz
tcc_demo_server-X-assembly.tar.gz
4. 将tar包放置到要部署的位置，对coordinator tar包进行解压

tar xzfv tcc_coordinator-X-assembly.tar.gz
tar xzfv tcc_demo_consumer-X-assembly.tar.gz
tar xzfv tcc_demo_server-X-assembly.tar.gz
5. cd tcc_coordinator-X-assembly/conf，进入配置目录，修改config.xml中的系统库，本地库url，根据需要修改各项配置，另外也要注意在dubbo.properties中修改zookeeper注册中心地址。

6. 依次启动tcc_demo_server, tcc_coordinator, tcc_demo_consumer:

cd tcc_demo_server-X-assembly/ 
./start.sh &
cd tcc_coordinator-X-assembly/scripts/
./start.sh &
cd tcc_demo_consumer-X-assembly
./start.sh &
在启动coordinator之前， 确保coordinator系统库已经经过初始化，初始化的建表语句位于conf目录下。

本地库无需手动初始化，coordinator第一次启动时会完成所有本地表的初始化。

可以查看各个程序的log，观察demo程序是否正常运行，正常情况下，demo_consumer会打印log在consumer.log中，内容如下：

......
canceling 131073
confirming 131074
waiting expiring 131075
confirming 131076
confirming 131077
confirming 131078
confirming 131079
......
