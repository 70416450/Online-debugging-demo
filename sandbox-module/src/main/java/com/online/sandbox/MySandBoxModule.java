package com.online.sandbox;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.annotation.Command;
import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;

@MetaInfServices(Module.class)
@Information(id = "my-sandbox-module")// 模块名,在指定挂载进程后通过-d指定模块,配合@Command注解来唯一确定方法
@Slf4j
public class MySandBoxModule implements Module {

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    @Command("addLog")// 模块命令名
    public void addLog() {
        new EventWatchBuilder(moduleEventWatcher)
                .onClass("com.sandbox.test.TestSandbox")// 想要对 TestSandbox 这个类进行切面
                .onBehavior("test")// 想要对上面类的 bathSave 方法进行切面
                .onWatch(new AdviceListener() {
                    //对方法执行之前执行
                    @Override
                    protected void before(Advice advice) throws Throwable {
                        log.info("sandbox切入成功!!!!!!");
                        //获取方法的所有参数
                        Object[] parameterArray = advice.getParameterArray();
                        if (parameterArray != null) {
                            for (Object po : parameterArray) {
                                log.info("形参类型为:" + po.getClass().getName() + "!!!!!!!");
                                log.info("形参值为:" + po + "!!!!!!!");
                            }
                        }
                    }
                });
    }
}