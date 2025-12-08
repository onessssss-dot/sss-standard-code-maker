package com.example.demo.core.saver;

import cn.hutool.core.util.StrUtil;
import com.example.demo.ai.model.HtmlCodeResult;
import com.example.demouser.exception.BusinessException;
import com.example.demouser.exception.ErrorCode;
import com.example.demouser.model.enums.CodeGenTypeEnum;


public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    /**
     * 获取对应枚举信息
     * @return
     */
    @Override
    protected CodeGenTypeEnum getCodeType() {
       return CodeGenTypeEnum.HTML;
    }

    /**
     * 保存HTML文件
     * @param result       代码结果对象
     * @param baseDirPath  基础目标路径
     */
    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath,"index.html", result.getHtmlCode());
    }

    /**
     * 复写数据校验
     *
     * @param result       代码结果对象
     */
    @Override
    protected void validDataInput(HtmlCodeResult result) {
        super.validDataInput(result);

        //html代码不能为空
        if (StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"html内容不能为空");
        }
    }
}
