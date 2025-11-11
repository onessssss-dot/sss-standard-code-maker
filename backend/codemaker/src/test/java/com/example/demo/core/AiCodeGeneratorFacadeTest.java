package com.example.demo.core;

import com.example.demo.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generate(){
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个程序员ss的博客，不超过20行", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);

    }

    @Test
    void generateStream(){

        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个程序员ss的博客，不超过20行", CodeGenTypeEnum.MULTI_FILE);

        //阻塞等待所有数据收集 完成
        List<String> block = codeStream.collectList().block();

        Assertions.assertNotNull(block);
        String join = String.join("", block);
        Assertions.assertNotNull(join);
    }


}