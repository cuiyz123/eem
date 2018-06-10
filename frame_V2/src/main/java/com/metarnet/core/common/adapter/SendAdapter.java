package com.metarnet.core.common.adapter;

import com.alibaba.fastjson.JSONArray;
import com.metarnet.core.common.utils.Constants;
import com.unicom.ucloud.workflow.objects.Participant;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: yyou
 * Date: 16-1-4
 * Time: 下午2:39
 * To change this template use File | Settings | File Templates.
 */

//@Service
public class SendAdapter {

    private static org.apache.logging.log4j.Logger logger =  LogManager.getLogger(SendAdapter.class);

    // ConnectionFactory ：连接工厂，JMS 用它创建连接
    static ConnectionFactory connectionFactory;
    // Connection ：JMS 客户端到JMS Provider 的连接
    static Connection connection;

    // Destination ：消息的目的地;消息发送给谁.
    private static  Destination destination;


    // Session： 一个发送或接收消息的线程
    static Session session;

    // MessageProducer：消息发送者
    private static MessageProducer producer;

    static {

        // TextMessage message;
        // 构造ConnectionFactory实例对象，此处采用ActiveMq的实现jar
        connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                Constants.MQ_CONN_URL);
        try {
            // 构造从工厂得到连接对象
            connection = connectionFactory.createConnection();
            // 启动
            connection.start();
            // 获取操作连接
            initSession();
            initProducer();
            //session = connection.createSession(Boolean.TRUE ,Session.AUTO_ACKNOWLEDGE);
            // 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public SendAdapter(){
//        try {
//            String queueName = Constants.QUEUE_NAME;
//            destination = session.createQueue(queueName);
//            producer = session.createProducer(destination);
//            // 设置不持久化，此处学习，实际根据项目决定
//            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }
    }

    public void closeSession(){
        try {
            if (null != connection)
                connection.close();
        } catch (Throwable ignore) {
        }
    }

    /**
     * 发送MQ消息方法
     *
     * 注：要将消息体写成JSON
     */
    public void sendMessage(String[] address,String msg){
        TextMessage message = null;
        //拼接json串
        Map map = new HashMap<String, Object>();
        map.put("orgSystem",Constants.MODEL_NAME); //系统标识
        map.put("msgKey",new Date().getTime());
        map.put("msgType","SMS");
        map.put("msgText",msg);
        map.put("msgAddress",address);
        map.put("msgLevel","MINOR");
        String messText = JSONArray.toJSONString(map);
        try {
            message = session.createTextMessage(messText);
            // 发送消息到目的地方
            producer.send(message);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public static void sentMessageToDo(String appId, String jobCode, String jobTitle, List<Participant> participants, long processInstID, long workItemID)
    {
        TextMessage message = null;
        String firstStepUser="";
        String participant="{'areacode':[],'majorcode':[],'orgcode':[],'productcode':[],'participant':[" ;
        if(participants!=null&&participants.size()>0){
            for(Participant ps:participants){
                participant+="{\"participantID\":\""+ps.getParticipantID()+"\",\"participantName\":\"\",\"participantType\":\"1\"},";
            }
        }
        participant=participant.substring(0,participant.length()-1);
        participant+="]}";
        firstStepUser=  participant;
        logger.info("sendMessage===========" + appId + "/" + jobCode + "/" + jobTitle + "/" + firstStepUser + "/" + processInstID + "/" + workItemID);
        Map map = new HashMap();
        map.put("modelName", appId);
        map.put("participant", firstStepUser);
        map.put("workOrderNumber", jobCode);
        map.put("jobTitle", jobTitle);
        map.put("processInstID", Long.valueOf(processInstID));
        map.put("taskInstID", Long.valueOf(workItemID));
        String messText = JSONArray.toJSONString(map);
        try {
            if(session == null){
                initSession();
            }
            message = session.createTextMessage(messText);
            if (producer == null) {
                initProducer();
            }

            producer.send(message);
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("...........................mq  restart....");
            initSession();
            initProducer();
        }
    }
    public static void initSession(){
        try {
            if(connection==null){
                logger.info("______________________________>connection in null ------url:" + Constants.MQ_CONN_URL + "queueName:" + Constants.QUEUE_NAME);
                connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, Constants.MQ_CONN_URL);
            }
            connection = connectionFactory.createConnection();

            connection.start();

            session = connection.createSession(Boolean.TRUE.booleanValue(), 1);

            destination = session.createQueue(Constants.QUEUE_NAME);
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
    public static void initProducer()
    {
        try
        {
            producer = session.createProducer(destination);
            producer.setDeliveryMode(1);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送短消息入口
     * @param address
     * @param msg
     */
//    public static void sendMsg(String[] address,String msg){
//        sendMessage(address,msg);
//    }
}
