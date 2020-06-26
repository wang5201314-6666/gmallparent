package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wang
 */
@RestController
@RequestMapping("admin/product")
public class SkuManageController {
    /**
     *  调用服务层
     */
    @Autowired
    private ManageService manageService;

    @GetMapping("spuImageList/{spuId}")
    public Result getSpuImageList(@PathVariable Long spuId){
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        //返回图片列表
        return Result.ok(spuImageList);
    }
    /**
     * 回显销售属性，属性值控制器
     */
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable Long spuId){
        //多个销售属性{销售属性有销售属性值集合}
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
       //返回数据
        return Result.ok(spuSaleAttrList);

    }

    /**
     * 大保存 skuInfo数据
     * 获取页面传递过来的数据 Json ----> JavaObject
     * 只要是Json转 JavaObject对象就使用@RequestBody
     */
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        //调用服务层
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    @GetMapping("list/{page}/{limit}")
    public Result getList(@PathVariable Long page,
                          @PathVariable Long limit){
        //需要将page，limit传给page对象
        Page<SkuInfo> skuInfoPage = new Page<>(page,limit);
        IPage<SkuInfo> skuInfoIPage = manageService.selectPage(skuInfoPage);
        //返回skuInfo 列表数据
        return Result.ok(skuInfoPage);

    }

    //商品上架
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        //调用服务层
        manageService.onSale(skuId);
        return Result.ok();
    }

    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){
        //调用服务层
        manageService.cancelSale(skuId);
        return Result.ok();
    }















}
