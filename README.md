# Arthas和jvm-sandbox对比简单使用
https://alibaba.github.io/arthas/
# 1 阿里在线分析诊断工具Arthas(阿尔萨斯)

> 参考: https://alibaba.github.io/arthas/
>
> 参考: https://github.com/alibaba/arthas/blob/master/README_CN.md

![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827143158532-445480955.png)

# 2 阿里在线分析诊断工具Jvm-Sandbox(JVM沙盒)

> 参考： https://github.com/alibaba/jvm-sandbox

![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827151402817-1973943338.png)

![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827143328058-1989224629.png)

# 3 Arthas与Jvm-Sandbox比较

> Arthas就像是一个剑冢，如果你希望得到一把宝剑就进去拿就好了! 它提供了各式各样的命令可以满足你的各项业务需求，上手成本较高。

> Jvm-Sandbox就像是一个剑炉，其提供了Module的概念，每个Module都是一个AOP的实例，也就是一把剑，怎么样锻造取决于工匠，也就是你啦！它提供了模型，具体实现需要你自己编写，灵活性更高。上手成本相对较低

- 下面将通过日常碰到的需要添加日志的需求，对两个开源项目的进行体验。

# 4 添加日志案例

> 项目地址: https://github.com/70416450/Online-debugging-demo

- 针对springboot-demo项目

1. 使用 mvn clean install命令编译
2. 上传springboot-demo-1.0-SNAPSHOT.jar包到服务器上
3. 使用java -jar springboot-demo-1.0-SNAPSHOT.jar启动项目，看见如下效果
4. ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827164929719-2110338523.png)

## 4.1 Jvm-Sandbox体验

### 4.1.1 下载解压

```
# 下载最新版本的JVM-SANDBOX
wget http://ompc.oss-cn-hangzhou.aliyuncs.com/jvm-sandbox/release/sandbox-stable-bin.zip

# 解压
unzip sandbox-stable-bin.zip
```

### 4.1.2 挂载目标应用

```
# 进入沙箱执行脚本
cd sandbox/bin

# 常用命令!!!
# 目标JVM进程93726(使用jps命令查看)
./sandbox.sh -p 93726
#卸载沙箱
./sandbox.sh -p 93726 -S
#查询沙箱
./sandbox.sh -p 93726 -l
#刷新沙箱
./sandbox.sh -p 93726 -F
#使用自定义module执行(my-sandbox-module:自定义的module名字，addLog自定义切入方法名字)
./sandbox.sh -p 93726 -d 'my-sandbox-module/addLog'

#日志配置及查看
#配置文件在    /sandbox/cfg/sandbox-logback.xml
#默认日志路径    ~/logs/sandbox/sandbox.log
```

![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827165032663-117868859.png)

- 针对springboot-demo项目

1. 使用 mvn clean compile assembly:single 打包，上传至sandbox/sandbox-module目录下
2. ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827160000561-1037081875.png)
3. 回到bin目录，
   - ./sandbox.sh -p 93726 -S  停止沙箱 
   - ./sandbox.sh -p 93726 -F  刷新沙箱 
   - ./sandbox.sh -p 93726 -l  查看沙箱 
   - ./sandbox.sh -p 93726 -d 'my-sandbox-module/addLog'  使用自定义module执行(my-sandbox-module:自定义的module名字，addLog自定义切入方法名字)
4. ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827165434936-504091008.png)
5. 切换到springboot-demo项目日志查看
6. ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827165401592-260487226.png)



## 4.2 Arthas体验

- 下载idea插件 Alibaba Cloud Toolkit   https://plugins.jetbrains.com/plugin/11386-alibaba-cloud-toolkit/

- 添加服务器地址并打开Arthas监控

- ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827171248333-355201520.png)

- 输入数字选择对应的进程

- ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827171600796-683294593.png)

- 启动成功

- ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827171643318-1624308648.png)

- **通过sc命令查找需要修改的class的ClassLoader** 

- ```
  sc -d *TestAdd | grep classLoaderHash
  ```

- ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827172322840-551859692.png)

- 修改TestAdd

- ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827172620239-1121664723.png)

- ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827172745575-1609500150.png)

- 将重新编译的class文件上传至服务器指定目录

- ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827172925575-1854637570.png)

- ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827173057958-723692068.png)

- **再使用redefine命令重新加载新编译好的TestAdd.class**（注意hash码和需要替换的类路径）

- ```
  redefine -c 439f5b3d /usr/local/src/jvm-sandbox/test/TestAdd.class
  ```

- ![](https://img2018.cnblogs.com/blog/1235870/201908/1235870-20190827173328274-1772966599.png)

- **注意点：**

  - **重启项目可以恢复**
  - **不允许新增加field/method**
  - **正在跑的函数，没有退出不能生效**





