package com.metarnet.core.common.service.impl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.DownloadFileInfo;
import com.metarnet.core.common.model.TEomAttachmentRelProc;
import com.metarnet.core.common.model.UploadFileInfo;
import com.metarnet.core.common.service.FtpUpAndDownService;
import com.metarnet.core.common.service.IAttachmentRelProcService;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.SFTPChannel;
import com.ucloud.paas.agent.PaasException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: yyou
 * Date: 15-7-16
 * Time: 下午4:43
 * To change this template use File | Settings | File Templates.
 */
@Service
public class FtpUpAndDownServiceImpl implements FtpUpAndDownService {

    protected static final Log log = LogFactory.getLog(FtpUpAndDownServiceImpl.class);
    private static final int OUTPUT_SIZE = 4096;
    @Resource
    private IBaseDAO baseDAO;
    @Resource
    private IAttachmentRelProcService relProcService;

    private static ChannelSftp connChannelSftp;

    @PostConstruct
    public void init() {
        /*new Timer().schedule(new TimerTask(){
            public void run() {
                connChannelSftp = initializeChannelSFTP();
                this.cancel(); //延迟一次，用cancel方法取消掉．
            }}, 120000);*/
    }

    //初始化sftp连接
    private static ChannelSftp initializeChannelSFTP() {
        ChannelSftp chSftp = null;
        Map<String, String> sftpDetails = new HashMap<String, String>();
        sftpDetails.put("host", Constants.ftpServer);
        sftpDetails.put("port", Constants.ftpPort + "");
        sftpDetails.put("username", Constants.ftpUsername);
        sftpDetails.put("password", Constants.ftpPassword);
        try {
            SFTPChannel channel = SFTPChannel.getInstance();
            chSftp = channel.getChannel(sftpDetails, 60000);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("连接SFTP服务失败！", e);
        }
        return chSftp;
    }

    private static FTPClient loginFTP() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(Constants.ftpServer, Constants.ftpPort);
            ftpClient.login(Constants.ftpUsername, Constants.ftpPassword);
            ftpClient.setDataTimeout(60000);       //设置传输超时时间为60秒
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置文件传输类型为二进制
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("FTP客户端连接失败！", e);
        }
        return ftpClient;
    }

    @Override
    public String delete(String fileUUID, String operator) {

        return null;
    }


    /**
     * 上传到FTP服务器
     *
     * @param info  上传文件信息
     * @param input 上传文件流
     * @return
     * @throws Exception
     */
    @Override
    public String upload(String storageName, UploadFileInfo info, DataInputStream input) throws PaasException {

        //生成UUID与文件做关联
//        Date dt = new Date(System.currentTimeMillis());
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String uuID = new Date().getTime() + "";
        String fileName = uuID;
        ChannelSftp chSftp = null;
        try {
//            chSftp = connChannelSftp;
            chSftp = initializeChannelSFTP();
            chSftp.cd(Constants.ftpUpDirectory);
            chSftp.put(input, fileName);
        } catch (SftpException e) {
            try {
                chSftp.cd(Constants.ftpUpDirectory);
                chSftp.put(input, fileName);
            } catch (SftpException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return null;
            }
        } finally {
            try {
                if (chSftp.getSession() != null) {
                    if (chSftp != null) {
                        chSftp.disconnect();
                    }
                    chSftp.getSession().disconnect();
                }
            } catch (JSchException e) {
                log.info("关闭连接出错");
            }
        }

       /* FTPClient ftpClient = loginFTP();
        try {
            ftpClient.changeWorkingDirectory(Constants.ftpUpDirectory);
            ftpClient.storeFile(fileName, input);  // 将上传文件存储到指定目录
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        return uuID;

    }


    /**
     * FTP下载
     *
     * @param fileUUID
     * @return
     * @throws Exception
     */

    @Override
    public DownloadFileInfo download(String fileUUID) throws PaasException {
        DownloadFileInfo fileInfo = new DownloadFileInfo();
        String filePath = Constants.ftpDownDirectory;
        String fileName = "";
        //通过fileUUID查找对应的文件
        TEomAttachmentRelProc attachmentRelProc = new TEomAttachmentRelProc();
        attachmentRelProc.setAttachmentId(fileUUID);
        List<TEomAttachmentRelProc> relaProc = null;
        try {
            relaProc = relProcService.findByExample(attachmentRelProc);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        String oldName = "";
        if (relaProc != null && relaProc.size() > 0) {
            for (TEomAttachmentRelProc tEomAttachmentRelProc : relaProc) {
                oldName = tEomAttachmentRelProc.getAttachmentName();
                fileName = tEomAttachmentRelProc.getAttachmentId();
                fileInfo.setFileName(oldName);
                fileInfo.setKeywords(fileName);
                fileInfo.setDesc(oldName);
                fileInfo.setFileSize(0L);//没有存值，设为默认值
                fileInfo.setCreateTime(tEomAttachmentRelProc.getCreationTime());
            }
        }
        /*try {
            ftpClient = loginFTP();
            ftpClient.changeWorkingDirectory(filePath); //  转到指定下载目录
            InputStream fileStream = ftpClient.retrieveFileStream(fileUUID);
            fileInfo.setInput(fileStream);
            // 关闭ftp连接
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        // 从ftp服务器上下载文件
        ChannelSftp chSftp = null;
        try {
//            chSftp = connChannelSftp;
            chSftp = initializeChannelSFTP();
            chSftp.cd(filePath);
            InputStream fileStream = chSftp.get(fileUUID);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] b = new byte[OUTPUT_SIZE];
            int len = 0;
            while ((len = fileStream.read(b)) != -1) {
                byteArrayOutputStream.write(b, 0, len);
            }
            fileInfo.setByteArrayOutputStream(byteArrayOutputStream);
        } catch (Exception e) {
            try {
                chSftp = initializeChannelSFTP();
                chSftp.cd(filePath);
                InputStream fileStream = chSftp.get(fileUUID);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] b = new byte[OUTPUT_SIZE];
                int len = 0;
                while ((len = fileStream.read(b)) != -1) {
                    byteArrayOutputStream.write(b, 0, len);
                }
                fileInfo.setByteArrayOutputStream(byteArrayOutputStream);
            } catch (SftpException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (chSftp.getSession() != null) {
                    if (chSftp != null) {
                        chSftp.disconnect();
                    }
                    chSftp.getSession().disconnect();
                }
            } catch (JSchException e) {
                log.info("关闭连接出错");
            }
        }
        return fileInfo;
    }
}
