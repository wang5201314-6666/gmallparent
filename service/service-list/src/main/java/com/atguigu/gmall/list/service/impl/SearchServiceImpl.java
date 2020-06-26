package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wang
 */
@Service
public class SearchServiceImpl implements SearchService {

    /**
     * 注入service-product-client对象
     */
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void upperGoods(Long skuId) {
        //声明一个实体类Goods
        Goods goods = new Goods();
        //商品的基本信息
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
        if (null!=skuInfo) {
            goods.setId(skuId);
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setTitle(skuInfo.getSkuName());
            goods.setCreateTime(new Date());
            //商品的分类信息 传入三级分类Id
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            if (null != categoryView) {
                goods.setCategory1Id(categoryView.getCategory1Id());
                goods.setCategory1Name(categoryView.getCategory1Name());
                goods.setCategory2Id(categoryView.getCategory2Id());
                goods.setCategory3Id(categoryView.getCategory3Id());
                goods.setCategory3Name(categoryView.getCategory3Name());
            }
            //平台属性信息
            /*  productFeignClient.getAttrList(skuId);
                skuId=skuInfo.getId();
            */
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuInfo.getId());
            List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
                //通过baseAttrInfo获取平台属性Id
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                //赋值平台属性值名称
                //获取平台属性值的集合
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                searchAttr.setAttrValue(attrValueList.get(0).getValueName());
                //将每个平台属性对象searchAttr返回去
                return searchAttr;
            }).collect(Collectors.toList());
            //储存平台属性
            if (null!=searchAttrList){
                goods.setAttrs(searchAttrList);
            }
            //品牌信息
            BaseTrademark trademark = productFeignClient.getTrademarkByTmId(skuInfo.getTmId());
            if (null!=trademark){
                goods.setTmId(trademark.getId());
                goods.setTmName(trademark.getTmName());
                goods.setTmLogoUrl(trademark.getLogoUrl());
            }
        }
        //将数据保存到es中
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        //商品的下架
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        //需要借助redis
        String key = "hotScore";
        //用户每访问一次，那么这个数据应该+1
        Double hotScore = redisTemplate.opsForZSet().incrementScore(key, "skuId:" + skuId, 1);
        //按照规定来更新es中的数据
        if (hotScore%10==0){
            //更新一次es中hotScore
            //获取到es中的对象
            Optional<Goods> optional = goodsRepository.findById(skuId);
            //获取到了当前对象
            Goods goods = optional.get();
            goods.setHotScore(Math.round(hotScore));
            goodsRepository.save(goods);

        }
    }
}
