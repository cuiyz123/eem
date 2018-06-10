package com.metarnet.core.common.adapter;

import com.metarnet.core.common.model.DownloadFileInfo;
import com.metarnet.core.common.model.UploadFileInfo;
import com.metarnet.core.common.service.FtpUpAndDownService;
import com.metarnet.core.common.utils.Constants;
import com.ucloud.paas.agent.PaasException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.DataInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: jtwu
 * Date: 12-11-28
 * Time: 下午4:47
 * 文件适配器，负责封装Paas平台提供的文件服务。
 */
@Service
public class FileAdapter {
    private static FileAdapter fileAdapter = new FileAdapter();

    @Autowired
    private FtpUpAndDownService ftpUpAndDownService;

    @PostConstruct
    public void init(){
        fileAdapter = this;
        fileAdapter.ftpUpAndDownService = this.ftpUpAndDownService;
    }
    private FileAdapter() {
    }

    public static FileAdapter getInstance() {
//        if (fileAdapter == null) {
//            fileAdapter = new FileAdapter();
//        }
        return fileAdapter;
    }

    /**
     * 上传文件
     *
     * @param info  文件信息
     * @param input 文件流
     * @return
     * @throws
     * @throws java.io.UnsupportedEncodingException
     */
    public String upload(String storageName ,UploadFileInfo info, DataInputStream input) throws PaasException{
        try {
            return fileAdapter.ftpUpAndDownService.upload(Constants.STORAGE_NAME, info, input);
        } catch (Exception e) {
           throw new PaasException(e);
        }
    }

    public DownloadFileInfo download(String fileUUID) throws PaasException{
        try {
            return fileAdapter.ftpUpAndDownService.download(fileUUID);
        } catch (Exception e) {
            throw new PaasException(e);
        }
    }

    public String delete(String fileUUID, String operator){

        return null;
    }
}
