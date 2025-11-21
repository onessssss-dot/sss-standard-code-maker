package com.example.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.example.demo.ai.AiCodeGenTypeRoutingService;
import com.example.demo.ai.AiCodeGeneratorService;
import com.example.demo.constant.AppConstant;
import com.example.demo.core.AiCodeGeneratorFacade;
import com.example.demo.core.bulider.VueProjectBuilder;
import com.example.demo.core.handler.StreamHandlerExecutor;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.exception.ThrowUtils;
import com.example.demo.model.dto.app.AppAddRequest;
import com.example.demo.model.dto.app.AppQueryRequest;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.ChatHistoryMessageTypeEnum;
import com.example.demo.model.enums.CodeGenTypeEnum;
import com.example.demo.model.vo.AppVO;
import com.example.demo.model.vo.UserVO;
import com.example.demo.service.ChatHistoryService;
import com.example.demo.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.demo.model.entity.App;
import com.example.demo.mapper.AppMapper;
import com.example.demo.service.AppService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *  服务层实现。
 *
 * @author <a>SSS</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;
    @Resource
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public String deployApp(Long appId, User loginUser) {

        //参数校验
        ThrowUtils.throwIf(appId==null||appId<0,ErrorCode.PARAMS_ERROR,"");

        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR,"weidenglu");
        //查询应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"无应用信息");

        //权限校验
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()),ErrorCode.NO_AUTH_ERROR,"只能操作自己的");

        //检查是否已有deployKey
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            //如果没有,生成6位deploykey
            deployKey = RandomUtil.randomString(6);
        }
        //获取代码生成类型,获取原始代码生成路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName=codeGenType+"_"+ appId;
        String sourceDirPath= AppConstant.CODE_OUTPUT_ROOT_DIR+ File.separator+ sourceDirName;

        //检查路径是否存在
        File sourceDir=new File(sourceDirPath);
        if (!sourceDir.exists()||!sourceDir.isDirectory()){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"对应应用文件不存在");
        }
        //vue项目特殊处理，执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum==CodeGenTypeEnum.VUE_PROJECT){
            boolean buildSuccess=vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(buildSuccess,ErrorCode.SYSTEM_ERROR,"不饿构建失败，请重试");

            File distDir=new File(sourceDirPath,"dist");
            //构建完成后，需要将构建后的文件复制到部署目录
            sourceDir=distDir;
        }


        //复制文件到部署目录
        String deployDir=AppConstant.CODE_DEPLOY_ROOT_DIR+File.separator+deployKey;

        try {
            FileUtil.copyContent(sourceDir,new File(deployDir), true);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,e.getMessage());
        }

        //更新数据库

        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean b = this.updateById(updateApp);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR,"更新应用部署信息失败");

        //返回可访问的URL地址
        return StrUtil.format("{}/{}",AppConstant.CODE_DEPLOY_HOST,deployKey);

    }

    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }


    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(appId==null||appId<0, ErrorCode.PARAMS_ERROR,"应用ID错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR,"提示词不能为空");
        //查询应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在，请重新创建");
        //权限校验,仅本人可以和自己应用对话
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR,"仅能对自己的应用进行操作");
        //获取生成模式
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"类型错误");
        }
        chatHistoryService.addChatMessage(appId,message, ChatHistoryMessageTypeEnum.USER.getValue(),loginUser.getId());
        //调用AI生成代码
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        //收集AI响应内容，并在完成对话后保存到对话历史
        return streamHandlerExecutor.doExecute(codeStream,chatHistoryService,appId,loginUser,codeGenTypeEnum);

    }

    /**
     * 删除应用时关联删除对话历史
     * @param id
     * @return
     */
    @Override
    public boolean removeById(Serializable id){
        ThrowUtils.throwIf(id==null, ErrorCode.PARAMS_ERROR,"应用id不能为空");
        long appId=Long.valueOf(id.toString());
        if (appId<0){
            return false;
        }
        //先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("删除日志关联的对话历史失败"+e.getMessage());
        }
        //删除应用
        return super.removeById(id);
    }




    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用 AI 智能选择代码生成类型
        CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }


}
