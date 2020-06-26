package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author wang
 * 变成xml
 */
@Configuration
public class CorsConfig {
    /**
     * 创建一个Bean对象
     */
    @Bean
    public CorsWebFilter corsWebFilter(){
        //创建corsconfiguration();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //设置跨域属性   * ：设置允许访问的网络
        corsConfiguration.addAllowedOrigin("*");
        //表示是否从服务器中获取到cookie，如果允许true,否则false。
        corsConfiguration.setAllowCredentials(true);
        //* : 表示允许所有的请求方法进来
        corsConfiguration.addAllowedMethod("*");
        //表示设置请求头信息【设置任意参数】
        corsConfiguration.addAllowedHeader("*");
        //创建source
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
        //返回CorsWebFilter
        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }
}
