package com.metarnet.eomeem.utils;

import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class Configuration extends WebApplicationObjectSupport {

    protected Log log;
    protected String resource;
    protected String fileName;
    protected long resourceLastModified;

    public Configuration() {
        log = logger;
        resource = null;
        fileName = null;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    protected InputStream getConfigurationInputStream(String resource)
            throws Exception {
        log.info((new StringBuilder("Begin to configure resource: ")).append(resource).toString());
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = null;
        if (is == null)
            try {
                log.info((new StringBuilder("try resource: /WEB-INF/classes/")).append(resource).toString());
                is = classLoader.getResourceAsStream(resource);
                fileName = (new StringBuilder("/WEB-INF/classes/")).append(resource).toString();
            } catch (Exception e) {
                log.debug((new StringBuilder("getConfigurationInputStream error: ")).append(e.getMessage()).toString());
            }
        if (is == null)
            try {
                log.info((new StringBuilder("try resource: /WEB-INF/")).append(resource).toString());
                is = classLoader.getResourceAsStream((new StringBuilder("../")).append(resource).toString());
                fileName = (new StringBuilder("/WEB-INF/")).append(resource).toString();
            } catch (Exception e) {
                log.debug((new StringBuilder("getConfigurationInputStream error: ")).append(e.getMessage()).toString());
            }
        if (is == null)
            try {
                log.info((new StringBuilder("try resource: /WEB-INF/lib/")).append(resource).toString());
                is = classLoader.getResourceAsStream((new StringBuilder("../lib/")).append(resource).toString());
                fileName = (new StringBuilder("/WEB-INF/lib/")).append(resource).toString();
            } catch (Exception e) {
                log.debug((new StringBuilder("getConfigurationInputStream error: ")).append(e.getMessage()).toString());
            }
        if (is == null)
            try {
                log.info((new StringBuilder("try resource: /WEB-INF/config/")).append(resource).toString());
                is = classLoader.getResourceAsStream((new StringBuilder("../config/")).append(resource).toString());
                fileName = (new StringBuilder("/WEB-INF/config/")).append(resource).toString();
            } catch (Exception e) {
                log.debug((new StringBuilder("getConfigurationInputStream error: ")).append(e.getMessage()).toString());
            }
        if (is == null) {
            log.warn((new StringBuilder("resource '")).append(resource).append("' not found").toString());
            throw new Exception();
        }
        try {
            log.debug((new StringBuilder("׼����ȡ�ļ�")).append(fileName).append("�ڷ���ʱ����޸�ʱ�� ...").toString());
            String realPath = getServletContext().getRealPath(fileName);
            log.debug((new StringBuilder("�����ļ��ľ��·��Ϊ��")).append(realPath).toString());
            File file = new File(realPath);
            resourceLastModified = file.lastModified();
            log.info((new StringBuilder("�����ļ��ڷ���ʱ����޸�ʱ��Ϊ��")).append(resourceLastModified).toString());
            fileName = realPath;
        } catch (RuntimeException e) {
            e.printStackTrace();
            fileName = null;
            log.error((new StringBuilder("��ȡ�����ļ��ڷ���ʱ����޸�ʱ�䷢�����")).append(e.getMessage()).toString());
        }
        return is;
    }

    public Configuration configure()
            throws Exception {
        if (resource == null) {
            log.info("cann't configure from resource null, please set the resource first. ");
            return this;
        } else {
            log.info((new StringBuilder("configuring from resource: ")).append(resource).toString());
            InputStream stream = getConfigurationInputStream(resource);
            return doConfigure(stream, resource);
        }
    }

    public Configuration configure(String res)
            throws Exception {
        log.info((new StringBuilder("configuring from resource: ")).append(res).toString());
        resource = res;
        InputStream stream = getConfigurationInputStream(res);
        return doConfigure(stream, res);
    }

    protected Configuration doConfigure(InputStream stream, String resourceName)
            throws Exception {
        Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader(resourceName, errors).read(new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception();
        } catch (Exception e) {
            log.error((new StringBuilder("problem parsing configuration")).append(resourceName).toString(), e);
            throw new Exception();
        }


        try {
            stream.close();
        } catch (IOException ioe) {
            log.error((new StringBuilder("could not close stream on: ")).append(resourceName).toString(), ioe);
        }

        try {
            stream.close();
        } catch (IOException ioe) {
            log.error((new StringBuilder("could not close stream on: ")).append(resourceName).toString(), ioe);
        }
        return doConfigure(doc);
    }

    public abstract Configuration doConfigure(Document document)
            throws Exception;

    public abstract boolean supports();

    protected void reset() {
        log.info((new StringBuilder("����������: ")).append(getClass().getName()).toString());
    }

    public Configuration reConfigure()
            throws Exception {
        reset();
        log.info((new StringBuilder("׼������������: ")).append(getClass().getName()).toString());
        return configure();
    }

    public boolean isLatest() {
        if (fileName == null)
            return true;
        long latest;
        File file = new File(fileName);
        latest = file.lastModified();
        log.info((new StringBuilder("�����ļ�������޸�ʱ��Ϊ��")).append(latest).toString());
        return resourceLastModified == latest;

    }

    public String getRealPath(String fileName) {
        return getServletContext().getRealPath(fileName);
    }
}
