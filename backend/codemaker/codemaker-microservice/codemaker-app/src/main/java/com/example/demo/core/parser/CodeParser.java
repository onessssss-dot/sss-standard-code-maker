package com.example.demo.core.parser;

/*
* 代码解析器策略接口
* */
public interface CodeParser<T> {

    /*
    * 解析代码内容
    *
    * @param 原始代码内容
    *  @return 解析后的对象
    * */
    T codeParser(String codeContent);
}
