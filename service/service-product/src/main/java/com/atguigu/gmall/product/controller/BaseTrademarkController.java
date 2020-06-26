package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wang
 */
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {
    /**
     * BaseTrademarkService 接口中就有了特殊的方法
     */
    @Autowired
    private BaseTrademarkService baseTrademarkService;

    @GetMapping("getTrademarkList")
    public Result getTrademarkList(){
        //List<BaseTrademark> baseTrademarks = baseTrademarkService.getTrademarkList();
        List<BaseTrademark> baseTrademarks = baseTrademarkService.list(null);
        return Result.ok(baseTrademarks);
    }

    @GetMapping("{page}/{limit}")
    public Result getPageList(@PathVariable Long page,
                              @PathVariable Long limit){
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page,limit);
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.selecrPage(baseTrademarkPage);
        return Result.ok(baseTrademarkIPage);
    }

    /**
     * 传递数据：id，imageUrl，tmName
     * vue项目做保存的时候，传递的是json数据，后台需要使用Java对象接受
     * @return
     */
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        //调用服务层
        baseTrademarkService.save(baseTrademark);
        return Result.ok();

    }

    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    @GetMapping("get/{id}")
    public Result getInfo(@PathVariable Long id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }
}
