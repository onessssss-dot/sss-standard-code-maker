package com.example.demo.core;


import com.example.demo.ai.AiCodeGeneratorService;
import com.example.demo.ai.model.HtmlCodeResult;
import com.example.demo.ai.model.MultiFileCodeResult;
import com.example.demo.common.ResultUtils;
import com.example.demo.core.parser.CodeParserExecutor;
import com.example.demo.core.saver.CodeFileSaverExecutor;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/*
* AI代码生成门面类
* */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;




    /**
     * 统一入口
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId 保存的目录
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId){
        if (codeGenTypeEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        }

       return switch (codeGenTypeEnum){
            case HTML -> {
                //向模型发送请求并接受对象
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);

                //返回文件对象
                yield  CodeFileSaverExecutor.executeSaver(htmlCodeResult,CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(multiFileCodeResult,CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default ->{
                String errorMessage="不支持的生成类型："+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
            }
        };
    }

    /**
     * 统一入口(流式)
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId 应用ID
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId){
        if (codeGenTypeEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        }

        return switch (codeGenTypeEnum){
            case HTML -> {
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield  processCodeStream(result,CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result,CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default ->{
                String errorMessage="不支持的生成类型："+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
            }
        };
    }

    /**
     * 通用的流式代码处理方法
     * @param codeStream         代码留
     * @param codeGenTypeEnum    代码生成类型
     * @param appId 应用ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream,CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        //定义一个字符串拼接器用于当流式返回所有代码之后

        StringBuilder codeBuilder = new StringBuilder();

        return codeStream.doOnNext(chunk->{
            //实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(()->{
            try {
                //流式返回完成后，保存代码
                String completeCode = codeBuilder.toString();
                //使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenTypeEnum);
                //使用执行器保存文件
                File file = CodeFileSaverExecutor.executeSaver(parsedResult,codeGenTypeEnum,appId);
                log.info("文件创建完成，目录为:{}",file.getAbsolutePath());
            }catch (Exception e){
                log.info("保存失败：{}",e.getMessage());
            }

        });
    }



}
