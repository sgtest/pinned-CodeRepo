package com.crazymakercircle.zk.distributedLock;

import com.crazymakercircle.cocurrent.FutureTaskScheduler;
import com.crazymakercircle.zk.ZKclient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.junit.Test;

/**
 * create by 尼恩 @ 疯狂创客圈
 **/
@Slf4j
public class ZkLockTester {

    int count = 0;

    @Test
    public void testLock() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            FutureTaskScheduler.add(() -> {
                //创建锁
                ZkLock lock = new ZkLock();
                lock.lock();
//每条线程，执行10次累加
                for (int j = 0; j < 10; j++) {
//公共的资源变量累加
                    count++;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("count = " + count);
                //释放锁
                lock.unlock();

            });
        }

        Thread.sleep(Integer.MAX_VALUE);
    }


    @Test
    public void testzkMutex() throws InterruptedException {

        CuratorFramework client = ZKclient.instance.getClient();
        final InterProcessMutex zkMutex =
                new InterProcessMutex(client, "/mutex");
        ;
        for (int i = 0; i < 10; i++) {
            FutureTaskScheduler.add(() -> {

                try {
                    //获取互斥锁
                    zkMutex.acquire();

                    for (int j = 0; j < 10; j++) {
//公共的资源变量累加
                        count++;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.info("count = " + count);
                    //释放互斥锁
                    zkMutex.release();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }

        Thread.sleep(Integer.MAX_VALUE);
    }


}
