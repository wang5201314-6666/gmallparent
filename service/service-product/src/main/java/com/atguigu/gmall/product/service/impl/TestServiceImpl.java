package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author wang
 */
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;
    @Override
    public void testLock() {
//        Config config = new Config();
//        config.useSingleServer().setAddress("redis://127.0.0.1:7181");
//        RedissonClient redisson = Redisson.create(config);
//
//        RLock lock = redisson.getLock("lock");
//        lock.lock();
//        //执行的业务逻辑代码
//        lock.unlock();
        String skuId = "18";
        //可以根据自己的规则定义锁的key
        String lockKey = "sku:" + skuId + "lock";
        RLock lock = redissonClient.getLock(lockKey);
        //加锁
        lock.lock(10,TimeUnit.SECONDS);
        //业务逻辑代码
        //获取缓存中的key=num
            String num = redisTemplate.opsForValue().get("num");
            if (StringUtils.isEmpty(num)){
                //为空就返回
                return;
            }
            //将num转换为int数据类型
            int number = Integer.parseInt(num);
            //那么则将num进行加1操作，放入缓存。
            redisTemplate.opsForValue().set("num",String.valueOf(++number));
        //lock.unlock();


    }

//    @Override
//    public void testLock() {
//        //set k1 v1 px 10000 nx  --- 原生命令是Jedis可以操作的命令。现在使用的是redisTemplate，没有直接使用的set命令。
//        //相当于nx=setnx("lock","atguigu")当key不存在的时候才会生效。
//        //Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "atguigu",);
//        //相当于set set lock atguigu px 30000 nx 具有了原子性，如果中间出错会自动释放锁资源。
//        //Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "atguigu",3, TimeUnit.SECONDS);
//        //防止误删锁，给value设置一个UUID值
//        String uuid = UUID.randomUUID().toString();
//        //Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,3, TimeUnit.SECONDS);
//        //模拟用户访问商品详情，通过skuId访问item.gmall.com/18.html
//        String skuId = "18";
//        //可以根据自己的规则定义锁的key
//        String lockKey = "sku:" + skuId + "lock";
//
//        Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, 3, TimeUnit.SECONDS);
//
//        //如果返回的true，说明上述命令执行成功，加锁成功！
//        if (lock){
//            //获取缓存中的key=num
//            String num = redisTemplate.opsForValue().get("num");
//            if (StringUtils.isEmpty(num)){
//                //为空就返回
//                return;
//            }
//            //将num转换为int数据类型
//            int number = Integer.parseInt(num);
//            //那么则将num进行加1操作，放入缓存。
//            redisTemplate.opsForValue().set("num",String.valueOf(++number));
//            //初始化num为0||set num 0
//            //操作完成资源之后，将锁删除。
//            //线程进来的时候会生成一个uuid，上锁的时候会将uuid放入缓存。
////            if (uuid.equals(redisTemplate.opsForValue().get("lock"))){
////                redisTemplate.delete("lock");
////            }
//            //推荐使用lua脚本
//            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//            //如何操作
//            //构建RedisScript 数据类型需要确定一下，默认情况下返回的Object
//            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//            //指定好返回的数据类型
//            redisScript.setResultType(Long.class);
//            //指定好lua脚本
//            redisScript.setScriptText(script);
//            redisTemplate.execute(redisScript, Arrays.asList(lockKey),uuid);
//        }else {
//            //说明上锁没有成功，有人在操作资源 外面人只能等待
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            testLock();
//        }
//    }
}
