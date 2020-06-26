package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wang
 */
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {

    /**
     * 细节：如果接口中传递多个参数，则需要指明参数与sql条件中的哪个参数
     * 编写xml文件。
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> selectBaseAttrInfoList(@Param("category1Id") Long category1Id,
                                              @Param("category2Id") Long category2Id,
                                              @Param("category3Id") Long category3Id);

    /**
     * 根据skuId获取到平台属性、平台属性值
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> selectAttrInfoList(Long skuId);


}
