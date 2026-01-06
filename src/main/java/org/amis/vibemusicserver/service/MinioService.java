package org.amis.vibemusicserver.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    /**
     * 上传文件到 MinIo
     *
     * @param file   上传的文件对象
     * @param folder 上传的文件夹路径目录
     * @return 文件访问的URL地址
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问的URL地址
     */
    void deleteFile(String fileUrl);
}
