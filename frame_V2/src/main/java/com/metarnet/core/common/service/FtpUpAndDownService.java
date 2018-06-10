package com.metarnet.core.common.service;

import com.metarnet.core.common.model.DownloadFileInfo;
import com.metarnet.core.common.model.UploadFileInfo;
import com.ucloud.paas.agent.PaasException;

import java.io.DataInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: yyou
 * Date: 15-7-16
 * Time: 下午4:37
 * To change this template use File | Settings | File Templates.
 */
public interface FtpUpAndDownService {

    public String upload(String storageName, UploadFileInfo info, DataInputStream input) throws PaasException;
    public DownloadFileInfo download(String fileUUID) throws PaasException;
    public String delete(String fileUUID, String operator) throws PaasException;
}
