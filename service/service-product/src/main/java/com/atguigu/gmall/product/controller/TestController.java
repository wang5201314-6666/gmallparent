package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wang
 */
@RestController
@RequestMapping("admin/product/test")
public class TestController {
    /**
     * 创建一个接口对象
     */
    @Autowired
    private TestService testService;

    @GetMapping("testLock")
    public Result testLock(){
        //调用锁的方法
        testService.testLock();

        return Result.ok();
    }
}
