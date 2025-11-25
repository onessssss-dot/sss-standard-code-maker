package com.example.demo.ai;

import com.example.demo.ai.model.HtmlCodeResult;
import com.example.demo.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode(){
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("创建一个简单的个人主页,20行代码左右");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiCode(){
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("创建一个程序员sss的博客，50行之内");
        Assertions.assertNotNull(result);
    }

}
