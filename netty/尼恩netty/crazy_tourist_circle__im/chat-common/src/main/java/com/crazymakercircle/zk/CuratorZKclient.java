package com.crazymakercircle.zk;

import com.crazymakercircle.util.SpringContextUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * create by 尼恩 @ 疯狂创客圈
 **/
@Slf4j
@Data
public class CuratorZKclient
{


    private final String zkSessionTimeout;
    private CuratorFramework client;


    //Zk集群地址
    private String zkAddress = "127.0.0.1:2181";
    public static CuratorZKclient instance = null;



    private static CuratorZKclient singleton = null;

    public static CuratorZKclient getSingleton()
    {
        if (null == singleton)
        {
            singleton = SpringContextUtil.getBean("curatorZKClient");

        }
        return singleton;
    }

    public CuratorZKclient(String zkConnect, String zkSessionTimeout)
    {
        this.zkAddress = zkConnect;
        this.zkSessionTimeout = zkSessionTimeout;
        init();
    }

    public void init()
    {

        if (null != client)
        {
            return;
        }
        //创建客户端
        client = ClientFactory.createSimple(zkAddress,zkSessionTimeout);

        //启动客户端实例,连接服务器
        client.start();

        instance = this;
    }

    public void destroy()
    {
        CloseableUtils.closeQuietly(client);
    }


    /**
     * 创建节点
     */
    public void createNode(String zkPath, String data)
    {
        try
        {
            // 创建一个 ZNode 节点
            // 节点的数据为 payload
            byte[] payload = "to set content".getBytes("UTF-8");
            if (data != null)
            {
                payload = data.getBytes("UTF-8");
            }
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(zkPath, payload);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 删除节点
     */
    public void deleteNode(String zkPath)
    {
        try
        {
            if (!isNodeExist(zkPath))
            {
                return;
            }
            client.delete()
                    .forPath(zkPath);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * 检查节点
     */
    public boolean isNodeExist(String zkPath)
    {
        try
        {

            Stat stat = client.checkExists().forPath(zkPath);
            if (null == stat)
            {
                log.info("节点不存在:", zkPath);
                return false;
            } else
            {

                log.info("节点存在 stat is:", stat.toString());
                return true;

            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建 临时 顺序 节点
     */
    public String createEphemeralSeqNode(String srcpath)
    {
        try
        {

            // 创建一个 ZNode 节点
            String path = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(srcpath);

            return path;

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
