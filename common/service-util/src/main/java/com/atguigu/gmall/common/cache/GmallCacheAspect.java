package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author wang
 */
@Component
@Aspect
public class GmallCacheAspect {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 编写环绕通知
     */
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) throws Throwable {
        Object result = null;
        //获取到传递的参数 方法上的参数
        Object[] args = point.getArgs();

        //获取方法上的注解
        MethodSignature signature = (MethodSignature) point.getSignature();
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);

        //获取注释上的prifex
        String prefix = gmallCache.prefix();
        //定义一个key  key-sku[18]  SkuInfo getSkuInfo(Long skuId) skuId = 18
        String key = prefix + Arrays.asList(args).toString();
        //需要将数据存储到缓存中，key=prefix+ Arrays.asList(args).toString();  值getSkuInfo()方法中执行的返回值数据
        /*
            1.先判断缓存中是否有数据；
            2.缓存中有数据，从缓存中获取；
            3.缓存中没有数据，从数据库中获取并放入缓存。
         */
        //表示根据key获取缓存中返回的数据
        result = cacheHit(signature, key);
        //判断缓存中是否获取到了数据
        if (result != null) {
            return result;
        }
        //如果获取的数据是空，那么应该走数据库，并放入缓存.  自定义一个锁
        RLock lock = redissonClient.getLock(key + ":lock");
        try {
            boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX2, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
            //返回true 说明上锁成功
            try {
                if (res) {
                    //查询数据库中数据 相当于执行getSkuInfo方法并能够得到返回值.
                    //根据注解在哪，如果注解在getSkuInfo(Long skuId)
                    //point.getArgs()相当于获取方法体上的参数
                    //point.proceed(point.getArgs()); 表执行带有@GmallCache的方法体
                    result = point.proceed(point.getArgs());
                    //判断result 返回的数据是否为空
                    if (result == null) {
                        //说明数据库中没有这个数据  防止缓存穿透
                        Object o = new Object();
                        //为什么这里需要转换成字符串  o表示祖先
                        redisTemplate.opsForValue().set(key, JSON.toJSONString(o), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                        //返回数据
                        return o;
                    }
                    //查询的数据不是空
                    redisTemplate.opsForValue().set(key, JSON.toJSONString(result), RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    //返回数据
                    return result;
                } else {
                    //其他线程睡眠
                    Thread.sleep(1000);
                    //继续获取数据
                    return cacheHit(signature, key);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Object cacheHit(MethodSignature signature, String key) {
        /**
         * 表示获取缓存中的数据
         */
        //根据key 获取缓存数据
        //BigDecimal getSkuPriceBySkuId(Long skuId); 返回BigDecimal
        //List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId,Long spuId) 返回的是集合
        //放入缓存中时是字符串，所以获取出来也需要是字符串
        String object = (String) redisTemplate.opsForValue().get(key);
        //此时获取返回值必须明确
        if (!StringUtils.isEmpty(object)) {
            //表示缓存中有数据，并获取返回值数据类型
            Class returnType = signature.getReturnType();
            //返回数据

            return JSON.parseObject(object, returnType);
        }
        return null;
    }
}
