package org.amis.vibemusicserver.service.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.service.MinioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author : KwokChichung
 * @description : MinioService的实现类
 * @createDate : 2026/1/7 3:53
 */
@Slf4j
@Service
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;


    public MinioServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 上传文件到 MinIo
     *
     * @param file   上传的文件对象
     * @param folder 上传的文件夹路径目录
     * @return
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 生成唯一文件名：文件夹/UUID-原始文件名
            String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            // 获取文件输入流
            InputStream inputStream = file.getInputStream();
            // 上传文件到Minio存储桶
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            // 返回文件的完整访问URL
            return endpoint + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            // 记录上传失败日志并抛出运行时异常
            log.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED + ":{}", e.getMessage());
            throw new RuntimeException(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED + ":" + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问的URL地址
     */
    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 从文件URL中提取Minio中的相对文件路径
            String filePath = fileUrl.replace(endpoint + "/" + bucketName + "", "");

            // 调用Minio API删除指定文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
        } catch (Exception e) {
            // 删除失败时抛出运行时异常，包含错误信息
            throw new RuntimeException(MessageConstant.FILE_DELETE + MessageConstant.FAILED + ":" + e.getMessage());
        }
    }
}

