package com.example.demo.core.parser;

import com.example.demouser.exception.BusinessException;
import com.example.demouser.exception.ErrorCode;
import com.example.demouser.model.enums.CodeGenTypeEnum;

/**
 * 代码解析生成器
 * 根据代码生成相应的解析逻辑
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser=new HtmlCodeParser();
    private static final MultiFileCodeParser multiFileCodeParser=new MultiFileCodeParser();

    /**
     *
     * @param codeContent 用户输入信息
     * @param codeGenTypeEnum 生成类型
     * @return HtmlCodeResult或MultiFileCodeResult
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum){
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.codeParser(codeContent);
            case MULTI_FILE -> multiFileCodeParser.codeParser(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型");
        };
    }
}
