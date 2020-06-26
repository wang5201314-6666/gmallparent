package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author wang
 */
public interface BaseTrademarkService extends IService<BaseTrademark> {
    /**
     * 分页查询品牌数据
     */
    IPage<BaseTrademark> selecrPage(Page<BaseTrademark> baseTrademarkPage);
}
