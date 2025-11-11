package com.example.demo.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.prefs.BackingStoreException;

public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";


    /**
     * 模板方法，保存代码的标准流程
     *
     * @param result
     * @return
     */
    public final File saveCode(T result){
        //数据校验
        validDataInput(result);
        //构建唯一目录
        String baseDirPath = buildUniqueDir();
        //保存文件(具体 实现交给子类)
        saveFiles(result,baseDirPath);
        //返回文件目录对象
        return new File(baseDirPath);
    }

    /**
     * 检验输入参数，可由子类复写
     * @param result
     */
    protected void validDataInput(T result){
        if (result==null) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"代码对象不能为空");
    }

    /**
     *
     * @param dirPath 路径
     * @param filename 文件名
     * @param content 内容
     */
    public final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)){
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 构建唯一目录路径：tmp/code_output/Type_雪花ID
     */
    private String buildUniqueDir() {

        String bizType=getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 获取代码类型（子类实现）
     * @return
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件的具体实现（子类实现）
     *
     * @param result       代码结果对象
     * @param baseDirPath  基础目标路径
     */
    protected abstract void saveFiles(T result,String baseDirPath);

}
