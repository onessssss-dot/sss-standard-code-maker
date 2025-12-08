package com.example.demo;

import com.example.demouser.utils.SpringContextUtil;
import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(
        exclude = {RedisEmbeddingStoreAutoConfiguration.class},
        scanBasePackages = {"com.example.demo", "com.example.demouser"}
)
@MapperScan("com.example.demo.mapper")

@EnableCaching
public class CodeMakerAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeMakerAppApplication.class, args);

    }


}

