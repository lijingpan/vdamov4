package com.vdamo.ordering.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.vdamo.ordering.mapper")
public class MybatisPlusConfig {
}
