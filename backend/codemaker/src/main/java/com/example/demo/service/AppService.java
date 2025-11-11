package com.example.demo.service;

import cn.hutool.core.bean.BeanUtil;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.model.dto.app.AppQueryRequest;
import com.example.demo.model.entity.User;
import com.example.demo.model.vo.AppVO;
import com.example.demo.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.example.demo.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a>SSS</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用封装类
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    String deployApp(Long appId,User loginUser);


    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构造应用 查询条件
     * @param appQueryRequest
     * @return
     */
     QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


    /**
     * 生成应用代码
     * @param appId 应用ID
     * @param message 生成提示词
     * @param loginUser 登录用户
     * @return
     */
     Flux<String> chatToGenCode(Long appId, String message, User loginUser);

}
