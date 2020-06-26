package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wang
 */
@FeignClient(name = "service-item",fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {
    /**
     * 将ItemApiController中的控制器注入到当前接口api/item
     */
    /**
     * 根据skuId获取商品详情的数据Result
     * @param skuId
     * @return
     */
    @GetMapping("api/item/{skuId}")
    Result getItem(@PathVariable Long skuId);



}
