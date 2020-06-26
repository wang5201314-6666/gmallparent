package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wang
 */
@Service
public class ManageServiceImpl implements ManageService {
    //通常会调用mapper层

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;


    @Override
    public List<BaseCategory1> getCategory1() {
        //select * from base_category1; 表与实体类与mapper名称对应！
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        //select * from base_category2 where category1_id=category1Id;
        QueryWrapper<BaseCategory2> baseCategory2QueryWrapper = new QueryWrapper<>();
        baseCategory2QueryWrapper.eq("category1_id",category1Id);
        return baseCategory2Mapper.selectList(baseCategory2QueryWrapper);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        //select * from base_category2 where category2_id=category2Id;
        QueryWrapper<BaseCategory3> baseCategory3QueryWrapper = new QueryWrapper<>();
        baseCategory3QueryWrapper.eq("category2_id",category2Id);
        return baseCategory3Mapper.selectList(baseCategory3QueryWrapper);
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        /**
         * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓如果只根据分类Id 查询平台属性↓↓↓↓↓↓↓↓↓↓↓↓
         *
         * select * from base_attr_Info where category_id=category1Id and category_level = 1 or
         * select * from base_attr_Info where category_id=category2Id and category_level = 2 or
         * select * from base_attr_Info where category_id=category3Id and category_level = 3 or
         * category_level 表示层级关系
         * category1Id category_level = 1
         * category2Id category_level = 2
         * category3Id category_level = 3
         *
         *↑↑↑↑↑↑↑↑↑↑↑如果只根据分类Id 查询平台属性↑↑↑↑↑↑↑↑↑↑↑
         *
         * 扩展功能：需要根据分类Id，得到属性名，最好还可以得到属性值名称。
         *      通过分类Id 能够得到属性名，
         *      base_attr_value属性值数据在这张表中！
         */
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id,category2Id,category3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //在这个方法中，一个是插入数据功能，一个是修改数据功能
        //一个是平台属性表：baseAttrInfo
        if (baseAttrInfo.getId()!=null){
            //修改功能
            baseAttrInfoMapper.updateById(baseAttrInfo);
        }else {
            //插入数据
            baseAttrInfoMapper.insert(baseAttrInfo);
        }
        //一个是平台属性值表：baseAttrValue  插入的时候，直接insert
        //修改{一：update，二：【先将数据删除，然后新增】}

        //删除数据 有条件的删除
        QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
        baseAttrValueQueryWrapper.eq("attr_id",baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueQueryWrapper);
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            if(null != attrValueList && attrValueList.size()>0){
                for (BaseAttrValue baseAttrValue : attrValueList){
                    //页面在提交数据的时候，并没有给attrId赋值，所以在此处需要手动赋值
                    // attrId = baseAttrInfo.getId();
                    baseAttrValue.setAttrId(baseAttrInfo.getId());
                    //循环将数据添加到数据表中
                    baseAttrValueMapper.insert(baseAttrValue);
                }
            }

//        if(null != baseAttrInfo){
//            /**
//             * 一个是平台属性表：baseAttrInfo
//             * 一个是平台属性值表：baseAttrValue
//             */
//            baseAttrInfoMapper.insert(baseAttrInfo);
//            /**
//             *  平台属性值插入的时候，可能存在多个值的情况，具体是多少个值，需要看传递过来的数据。
//             *  页面在传递平台属性数据的时候，数据会自动封装到BaseAttrInfo中，这个属性中attrValueList
//             *  前台页面给封装好的
//             */
//            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
//            if(null != attrValueList && attrValueList.size()>0){
//                for (BaseAttrValue baseAttrValue : attrValueList){
//                    //页面在提交数据的时候，并没有给attrId赋值，所以在此处需要手动赋值
//                    // attrId = baseAttrInfo.getId();
//                    baseAttrValue.setAttrId(baseAttrInfo.getId());
//                    //循环将数据添加到数据表中
//                    baseAttrValueMapper.insert(baseAttrValue);
//                }
//            }
//        }
    }

    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        //attrId平台属性Id attrId=base_attr_info.id 此id是base_attr_info 主键
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        if (null!=baseAttrInfo){
        //不能直接返回baseAttrInfo,因为控制器需要的是baseAttrInfo下的平台属性值集合。
        //需要给平台属性值赋值。
        //select * from base_attr_value where attr_id = attrId;
        QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
        baseAttrValueQueryWrapper.eq("attr_id", attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(baseAttrValueQueryWrapper);
        //将平台属性值集合放入baseAttrInfo中，此时才能返回。
        baseAttrInfo.setAttrValueList(baseAttrValueList);
    }
        return baseAttrInfo;
    }

    @Override
    public IPage<SpuInfo> selectPage(Page<SpuInfo> spuInfoPageParam, SpuInfo spuInfo) {
        //封装查询条件 where category3_id = ? order by id.
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id",spuInfo.getCategory3Id());
        //查询完成之后，可以按照某一种规则进行排序。
        spuInfoQueryWrapper.orderByDesc("id");
        return spuInfoMapper.selectPage(spuInfoPageParam,spuInfoQueryWrapper);
    }

    @Override
    public IPage<SkuInfo> selectPage(Page<SkuInfo> skuInfoPage) {
        //需要使用mapper
        return skuInfoMapper.selectPage(skuInfoPage,new QueryWrapper<SkuInfo>().orderByDesc("id"));
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        //调用mapper层
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        /*
            需要对应的mapper
            spuInfo  表中的数据
            spuImage  图片列表
            spuSaleAttr  销售属性
            spuSaleAttrValue  销售属性值
         */
        spuInfoMapper.insert(spuInfo);
        //获取到数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (null!=spuImageList && spuImageList.size()>0){
            //循环遍历添加
            for (SpuImage spuImage : spuImageList){
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }
        }
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(null!=spuSaleAttrList && spuSaleAttrList.size()>0){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                //在销售属性中获取销售属性值集合
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (null != spuSaleAttrValueList && spuSaleAttrValueList.size()> 0){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        //select * from spu_image where spu_id = spuId
        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id",spuId));
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        //由于数据存在多张表中，所以需要自定义xml文件来实现。
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
//            skuInfo  库存单元表
//            skuSaleAttrValue  sku与销售属性值的中间表
//            skuAttrValue  sku与平台属性中间表
//            skuImage  库存单元图片表
        skuInfoMapper.insert(skuInfo);
        //获取销售属性的数据
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(null!=skuSaleAttrValueList && skuSaleAttrValueList.size()>0){
            //循环遍历
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                //在已知的条件中获取spuId，skuId
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                //从哪里获取当数据从页面提交过来的时候，spuId在skuInfo中已经赋值了
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                //插入数据
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }
        //skuAttrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (null != skuAttrValueList && skuAttrValueList.size()>0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }
        //skuImage 图片列表
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (null != skuImageList && skuImageList.size()>0){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }
    }

    @Override
    public void onSale(Long skuId) {
       //is_sale = 1表示可以上架
       //update sku_info set is_sale = 1 where id = skuId
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(1);
        skuInfo.setId(skuId);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public void cancelSale(Long skuId) {
         // 0 表示商品不可以买 update sku_info set is_sale = 0 where id = skuId
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(0);
        skuInfo.setId(skuId);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    @GmallCache(prefix = "sku")  //prefix 自定义变量
    public SkuInfo getSkuInfo(Long skuId) {
        //ctrl+alt+m
        //return getSkuInfoRedisson(skuId);
        return getSkuInfoDB(skuId);


    }

    private SkuInfo getSkuInfoRedisson(Long skuId) {
        //在此获取skuInfo的时候，先查询缓存，如果缓存中有数据，则查询，没有则不查询数据库并放入缓存。
        SkuInfo skuInfo = null;
        try {
            //先判断缓存中是否有数据，查询缓存必须知道缓存的key是什么
            //定义缓存的key 商品详情的缓存key=sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            //根据key获取缓存中的数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            if (skuInfo==null){
               //从数据库中获取数据，防止缓存击穿做分布式锁
                // 定义分布式锁的key lockKey=sku:skuId:lock
                String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                //使用redisson
                RLock lock = redissonClient.getLock(lockKey);
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX2, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                if (res){
                    try {
                        //从数据库中获取数据
                        skuInfo = getSkuInfoDB(skuId);
                        if (skuInfo==null){
                            //为了防止缓存穿透，设置一个空对象放入缓存,这个时间不要太长
                            SkuInfo skuInfo1 = new SkuInfo();
                            //放入缓存
                            redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            //返回数据
                            return skuInfo1;
                        }
                        //从数据库中获取到数据，放入缓存
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        return skuInfo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        //解锁
                        lock.unlock();
                    }
                }else {
                    //此时的线程并没有获取到分布式锁，应该等待
                    Thread.sleep(1000);
                    //等待完成继续查询
                    return getSkuInfo(skuId);
                }
            }else {
                //表示缓存中有数据
                //            if (skuInfo.getId()==null){
                //                return null;
                //            }
                //缓存中有数据直接返回即可
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //如果中途发送了异常，数据源停一下。
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoRedis(Long skuId) {
        //在此获取skuInfo的时候，先查询缓存，如果缓存中有数据，则查询，没有则不查询数据库并放入缓存。
        SkuInfo skuInfo = null;
        try {
            //先判断缓存中是否有数据，查询缓存必须知道缓存的key是什么
            //定义缓存的key 商品详情的缓存key=sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            //根据key获取缓存中的数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            if (skuInfo==null){
                //应该获取数据库中的数据，放入缓存，分布式锁，为了防止缓存击穿
                //定义分布式锁的key lockKey=sku:skuId:lock
                String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                //还需要一个uuid，作为锁的值value
                String uuid = UUID.randomUUID().toString();
                //开始上锁
                Boolean isExist = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                //如果返回true 获取到分布式锁
                if (isExist){
                    System.out.println("获取到锁");
                    //去数据库获取数据，并放入缓存
                    skuInfo = getSkuInfoDB(skuId);
                    if (skuInfo==null){
                        //为了防止缓存穿透，设置一个空对象放入缓存,这个时间不要太长
                        SkuInfo skuInfo1 = new SkuInfo();
                        //放入缓存
                        redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                        //返回数据
                        return skuInfo1;
                    }
                    //从数据库中查询不是空
                    redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    //删除锁
                    //推荐使用lua脚本
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    //如何操作
                    //构建RedisScript 数据类型需要确定一下，默认情况下返回的Object
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    //指定好返回的数据类型
                    redisScript.setResultType(Long.class);
                    //指定好lua脚本
                    redisScript.setScriptText(script);
                    redisTemplate.execute(redisScript, Arrays.asList(lockKey),uuid);

                    //返回正常数据
                    return skuInfo;
                }else {
                    //此时的线程并没有获取到分布式锁，应该等待
                    Thread.sleep(1000);
                    //等待完成继续查询
                    return getSkuInfo(skuId);
                }
            }else {
    //            if (skuInfo.getId()==null){
    //                return null;
    //            }
                //缓存中有数据直接返回即可
                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //如果中途发送了异常，数据库挺一下。
        return getSkuInfoDB(skuId);
    }

    //提取方法
    private SkuInfo getSkuInfoDB(Long skuId) {
        //select * from sku_info where id = skuId
        //skuId=1000 在数据库中根本不存在，skuInfo应该是空对象。
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (null!=skuInfo){
            //查询Sku图片赋值给skuInfo对象，那么此时skuInfo对象中有：sku基本数据，sku图片数据
            //select * from sku_image where sku_id = skuId
            List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

    @Override
    @GmallCache(prefix = "baseCategoryView")
    public BaseCategoryView getBaseCategoryViewBycategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    @GmallCache(prefix = "price")
    public BigDecimal getSkuPriceBySkuId(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(null!=skuInfo){
            return skuInfo.getPrice();
        }
        return new BigDecimal(0);
    }



    @Override
    @GmallCache(prefix = "spuSaleAttr")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    @Override
    @GmallCache(prefix = "skuValueIdsMap")
    public Map getSkuValueIdsMap(Long spuId) {
        //调用mapper自定义方法获取数据，将数据查询后直接放入List
        HashMap<Object, Object> map = new HashMap<>();
        List<Map> mapList = skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);
        //获取到数据后，开始循环遍历集合中的每一条数据
        if (null!=mapList && mapList.size()>0){
            for (Map skuMaps : mapList) {
                map.put(skuMaps.get("value_ids"),skuMaps.get("sku_id"));
            }
        }
        return map;
    }

    @Override
    @GmallCache(prefix = "index")
    public List<JSONObject> getBaseCategoryList() {
        List<JSONObject> list = new ArrayList<>();
        //先获取所有的分类数据
        //select * from base_category_view
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //按照一级分类Id分组
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        //初始化一个index 构建json字符串 "index" : 1
        int index = 1;
        //获取一级分类的数据
        for (Map.Entry<Long, List<BaseCategoryView>> entry : category1Map.entrySet()) {
            //获取一级分类Id
            Long categor1Id = entry.getKey();
            //一级分类Id 下面所有对应的集合数据
            List<BaseCategoryView> category2List = entry.getValue();
            //声明一个对象保存一级分类数据 一级分类的Json字符串
            JSONObject category1 = new JSONObject();
            category1.put("index",index);
            category1.put("categoryId",categor1Id);
            //由于按照了一级分类Id进行分组
            String categoryName = category2List.get(0).getCategory1Name();
            category1.put("categoryName",categoryName);
            //变量迭代
            index++;
            //获取二级分类的数据
            Map<Long, List<BaseCategoryView>> category2Map = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            //创建一个二级分类对象的集合
            List<JSONObject> category2Child = new ArrayList<>();
            //获取二级分类的数据
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : category2Map.entrySet()) {
                //获取的二级分类Id
                Long category2Id = entry2.getKey();
                //获取二级分类下的所有数据
                List<BaseCategoryView> category3List = entry2.getValue();
                //声明一个二级分类Json对象
                JSONObject category2 = new JSONObject();
                category2.put("categoryId",category2Id);
                category2.put("categoryName",category3List.get(0).getCategory2Name());

                //二级分类名是多个，将所有二级分类添加到集合中
                category2Child.add(category2);

                //处理三级分类数据
                //声明一个三级分类数据的集合
                List<JSONObject> category3Child = new ArrayList<>();
                category3List.stream().forEach(category3View ->{
                    //创建一个三级分类对象
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId",category3View.getCategory3Id());
                    category3.put("categoryName",category3View.getCategory3Name());

                    //将三级分类数据添加到集合
                    category3Child.add(category3);
                });
                //将三级分类数据放入到二级分类的categoryChild
                category2.put("categoryChild",category3Child);
            }
            //将二级分类数据放入一级分类的categoryChild
            category1.put("categoryChild",category2Child);
            //按照json数据接口方式，分别去封装 一级分类，二级分类，三级分类数据。
            list.add(category1);
        }
        //封装完成之后，将数据返回
        return list;
    }

    @Override
    public BaseTrademark getBaseTrademarkByTmId(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long skuId) {
        //sku_attr_value这个中间表没有属性名称、属性值名称等，所以要进行多表关联查询。
        return baseAttrInfoMapper.selectAttrInfoList(skuId);
    }

}
