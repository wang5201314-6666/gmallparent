package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author wang
 */
@RestController
@RequestMapping("admin/product")
public class FileUploadController {
    /**
     * 引入图片服务器地址
     */
    @Value("${fileServer.url}")
    private String fileUrl;
    /**
     * 获取上传文件！使用springMVC
     * 当图片上传完成后，回显图片：回显图片本质是：图片路径字符串
     * @return
     */
    @RequestMapping("fileUpload")
    public Result<String> fileUpload(MultipartFile file) throws Exception {
        //读取到racker.conf
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        //返回的路径
        String path = null;
        if(null != configFile){
            //初始化数据
            ClientGlobal.init(configFile);
            //创建trackerClient,trackerService
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            //创建storageClient1
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,null);
            //准备上传文件
            //获取文件后缀名
            String extName = FilenameUtils.getExtension(file.getOriginalFilename());
            path = storageClient1.upload_appender_file1(file.getBytes(), extName, null);
            //打印文件路径
            System.out.println("上传后的文件路径：\t" + fileUrl+path);

        }
        //拼接上传之后的文件路径
        //返回图片路径
        return Result.ok(fileUrl+path);

    }
}
