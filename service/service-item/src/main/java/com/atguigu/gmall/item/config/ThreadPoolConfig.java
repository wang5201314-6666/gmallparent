package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author wang
 */
@Configuration
public class ThreadPoolConfig {

    /**
     *自定义一个线程池
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){

        return new ThreadPoolExecutor(
                // 核心线程池数
                50,
                // 最大线程池数据
                200,
                // 剩余空闲线程的存活时间
                30,
                // 时间单位
                TimeUnit.SECONDS,
                // 阻塞队列
                new ArrayBlockingQueue<>(50)
        );
    }
}
