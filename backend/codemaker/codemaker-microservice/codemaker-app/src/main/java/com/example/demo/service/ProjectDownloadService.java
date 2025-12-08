package com.example.demo.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service

public interface ProjectDownloadService {

    /**
     * 下载项目压缩包
     * @param projectPath
     * @param downloadFileName
     * @param response
     * @return
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
