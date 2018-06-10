package com.metarnet.core.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Created by IntelliJ IDEA.
 * User: jtwu
 * Date: 2009-12-7
 * Time: 21:32:57
 * spring工具类
 */
public class SpringContextUtils implements ApplicationContextAware {
    public static final String DATA_SOURCE = "dataSource";
    private static ApplicationContext applicationContext;
    public SpringContextUtils(){}

    public void setApplicationContext(ApplicationContext _applicationContext) throws BeansException {
        applicationContext = _applicationContext;
    }

    public static void refresh(){
        ((AbstractRefreshableWebApplicationContext)applicationContext).refresh();
    }
    /**
     * 便捷方法, 可以得到spring管理的bean *
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 得到链接的方法, 如果log处于debug模式, 则得到Log连接, 否则得到的仍然是普通连接.
     */
    public static Connection getConnection() {
        try {
            return DataSourceUtils.getConnection((DataSource) getBean(DATA_SOURCE));
        } catch (CannotGetJdbcConnectionException e) {
            throw e;
        }
    }
    /**
     * 得到链接的方法, 如果log处于debug模式, 则得到Log连接, 否则得到的仍然是普通连接.
     */
    public static Connection getConnection(String dataSource) {
        try {
            return DataSourceUtils.getConnection((DataSource) getBean(dataSource));
        } catch (CannotGetJdbcConnectionException e) {
            throw e;
        }
    }



    public static void closeConnection(Connection conn) {
        DataSourceUtils.releaseConnection(conn, (DataSource) getBean(DATA_SOURCE));            //spring 2.0
    }
    /**
     *  addby pengdw
     * 判定BeanFactory 是否包含 该对象(beanName)
     * */
    public static Boolean containsBean(String beanName){
        return applicationContext.containsBean(beanName);
    }
}
