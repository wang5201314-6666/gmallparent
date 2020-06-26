package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * @author wang
 */
public interface ItemService {
    /**
     * 通过skuId 获取数据
     */
    Map<String,Object> getBySkuId(Long skuId);

}
