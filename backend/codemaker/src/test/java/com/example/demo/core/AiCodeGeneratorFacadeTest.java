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
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个程序员ss的博客，不超过20行", CodeGenTypeEnum.HTML,1L);
        Assertions.assertNotNull(file);

    }

    @Test
    void generateStream(){

        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("创建一个简单的个人主页\n" +
                "\n" +
                "## 可用素材资源\n" +
                "请在生成网站使用以下图片资源，将这些图片合理地嵌入到网站的相应位置中。\n" +
                "```json\n" +
                "{\n" +
                "  \"status\": \"success\",\n" +
                "  \"message\": \"已成功为您的个人主页收集了全面的图片资源\",\n" +
                "  \"collected_images\": {\n" +
                "    \"logo\": \"已生成个人品牌Logo设计（简约现代风格，蓝色灰色调）\",\n" +
                "    \"background_images\": [\n" +
                "      {\n" +
                "        \"description\": \"香港建筑几何图案抽象视图\",\n" +
                "        \"url\": \"https://images.pexels.com/photos/32664236/pexels-photo-32664236.jpeg?auto=compress&cs=tinysrgb&h=350\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"description\": \"高雄城市天际线黑白照片\",\n" +
                "        \"url\": \"https://images.pexels.com/photos/31775378/pexels-photo-31775378.jpeg?auto=compress&cs=tinysrgb&h=350\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"description\": \"法国贡比涅现代建筑\",\n" +
                "        \"url\": \"https://images.pexels.com/photos/27153419/pexels-photo-27153419.jpeg?auto=compress&cs=tinysrgb&h=350\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"work_scenes\": [\n" +
                "      {\n" +
                "        \"description\": \"黑白剪影人物站在楼梯上\",\n" +
                "        \"url\": \"https://images.pexels.com/photos/9548519/pexels-photo-9548519.jpeg?auto=compress&cs=tinysrgb&h=350\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"description\": \"时尚年轻人在户外\",\n" +
                "        \"url\": \"https://images.pexels.com/photos/24032931/pexels-photo-24032931.jpeg?auto=compress&cs=tinysrgb&h=350\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"description\": \"咖啡馆场景\",\n" +
                "        \"url\": \"https://images.pexels.com/photos/30595028/pexels-photo-30595028.jpeg?auto=compress&cs=tinysrgb&h=350\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"architecture_diagram\": {\n" +
                "      \"description\": \"个人主页网站结构图\",\n" +
                "      \"url\": \"https://sss-1387458502.cos.ap-beijing.myqcloud.com/mermaid/iZK5v/mermaid_output_7806466603281027792.svg\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"recommendations\": [\n" +
                "    \"使用简约现代的背景图片作为网站主视觉\",\n" +
                "    \"Logo可用于网站头部和社交媒体头像\",\n" +
                "    \"工作场景图片可用于\"关于我\"和\"职业经历\"部分\",\n" +
                "    \"网站结构图有助于规划页面布局和导航\"\n" +
                "  ]\n" +
                "}\n" +
                "```", CodeGenTypeEnum.HTML,2L);

        //阻塞等待所有数据收集 完成
        List<String> block = codeStream.collectList().block();

        Assertions.assertNotNull(block);
        String join = String.join("", block);
        Assertions.assertNotNull(join);
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "简单的任务记录网站，总代码量不超过 200 行",
                CodeGenTypeEnum.VUE_PROJECT, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }


}