package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wang
 * 电商后台管理控制器
 */
@Api("后台接口测试")
@RestController
@RequestMapping("admin/product")
public class BaseManageController {
    @Autowired
    private ManageService manageService;
    /**
     * 写: getCategory1  vue项目页面需要获取的数据是Json数据
     *  封装过一个结果集的类 Result
     */
    @GetMapping("getCategory1")
    public Result<List<BaseCategory1>> getCategory1(){
        //最基本的返回方式
        List<BaseCategory1> category1List = manageService.getCategory1();
        return Result.ok(category1List);
    }

    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){
        //根据一级分类Id 查询二级分类数据
        List<BaseCategory2> category2List = manageService.getCategory2(category1Id);
        return Result.ok(category2List);
    }

    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){
        //根据二级分类Id 查询三级分类数据
        List<BaseCategory3> category3List = manageService.getCategory3(category2Id);
        return Result.ok(category3List);
    }

    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getCategory3(@PathVariable Long category1Id,
                               @PathVariable Long category2Id,
                               @PathVariable Long category3Id){
        //根据分类Id 查询平台属性数据
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(attrInfoList);
    }

    /**
     * 因为这个实体类中既有平台属性的数据，也有平台属性值的数据
     * vue 项目在页面传递过来的是json字符串，能否直接映射成Java对象？
     * @RequestBody : 将json数据转化为Java对象
     * @return
     */
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        //调用保存方法
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 修改平台属性，根据平台属性Id获取平台属性数据
     * 根据文档接口：http://api.gmall.com/admin/product/getAttrValueList/{attrId}
     * 由于页面返回值需要{Result结构的数据}
     */
    @GetMapping("getAttrValueList/{attrId}")
    public Result<List<BaseAttrValue>> getAttrValueList(@PathVariable Long attrId){
        //方法一：只是从功能上而言完成的。
        //select * from base_attr_value where attr_id = attrId;
        //控制层调用服务层
        //List<BaseAttrValue> baseAttrValueList = manageService.getAttrValueList(attrId);

        //方法二：是从业务逻辑上完成的。
        //根据业务进行分析，这样直接查询是否可以？
        //attrId平台属性Id attrId=base_attr_info.id
        //平台属性 平台属性值  关系 1：n
        //要想查询平台属性值，应该从平台属性入手！如果有属性的话，才会去查询属性值。
        //根据上述分析，应该先查询平台属性，从平台属性中获取平台属性值集合。

        BaseAttrInfo baseAttrInfo = manageService.getAttrInfo(attrId);
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        return Result.ok(attrValueList);
    }

    /**
     *  根据条件查询spuInfo 数据列表  / 传递过来的数据，如果正好是实体类的属性，那么就可以给实体类对象传值。
     */
    @GetMapping("{page}/{limit}")
    public Result getPageList(@PathVariable Long page,
                              @PathVariable Long limit,
                              SpuInfo spuInfo){
        //创建一个Page对象
        Page<SpuInfo> spuInfoPage = new Page<>(page,limit);
        //调用服务层
        IPage<SpuInfo> spuInfoIPageList = manageService.selectPage(spuInfoPage, spuInfo);
        //返回给Result
        return Result.ok(spuInfoIPageList);

    }


}
