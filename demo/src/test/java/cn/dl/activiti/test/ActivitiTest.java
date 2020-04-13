package cn.dl.activiti.test;


import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: dl
 * @Date: 2020/4/13 12:45
 **/
public class ActivitiTest {
    @Test
    public void createTable() {
        //创建ProcessEngineConfiguration
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        //设置引擎配置信息
        processEngineConfiguration = loadProcessEngineConfiguration(processEngineConfiguration);
        // 获取流程引擎对象
        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
        System.out.println("processEngine:" + processEngine);
    }

    @Test
    public void deploy(){
        //创建ProcessEngineConfiguration
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        //设置引擎配置信息
        processEngineConfiguration = loadProcessEngineConfiguration(processEngineConfiguration);
        // 获取流程引擎对象
        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
        System.out.println("processEngine:" + processEngine);

        String processId = "mail_demo";
        String processName = "activiti邮件发送";
        BpmnModel bpmnModel = createBpmnModel(processId, processName);
        //输出控制台
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        System.out.println(new String(bpmnBytes));
        // 它提供了管理和控制发布包和流程定义的操作
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //repositoryService.createDeployment().addString(process.getId()+".bpmn20.xml", xml).name(process.getName()).deploy();//用xml
        repositoryService.createDeployment().addBpmnModel(processId+".bpmn20.xml", bpmnModel).name(processName).deploy();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.startProcessInstanceByKey("mail_demo");
    }



    /**
     * 代码构造邮件服务流程图 eg：开始->serviceTask->结束
     * @return
     */
    public BpmnModel createBpmnModel(String processId, String processName){
        BpmnModel bpmnModel = new BpmnModel();
        org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
        process.setId(processId);
        process.setName(processName);
        //开始节点
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        startEvent.setName("开始");
        process.addFlowElement(startEvent);
        //开始节点到邮件服务的连线
        SequenceFlow startToServiceEvent = new SequenceFlow();
        startToServiceEvent.setSourceRef("start");
        startToServiceEvent.setTargetRef("mailTask");
        process.addFlowElement(startToServiceEvent);
        //邮件服务
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setType("mail");
        serviceTask.setId("mailTask");
        serviceTask.setName("邮件服务");

        Map<String, List<ExtensionElement>> extensionElements = serviceTask.getExtensionElements();
        extensionElements = setMailConfig(extensionElements, "abc@163.com", "asd@163.com", null , null,
                "activiti邮件主题", "邮件内容", "utf-8");
        serviceTask.setExtensionElements(extensionElements);
        process.addFlowElement(serviceTask);

        //邮件服务到结束节点
        SequenceFlow serviceEventToEnd = new SequenceFlow();
        serviceEventToEnd.setSourceRef("mailTask");
        serviceEventToEnd.setTargetRef("end");
        process.addFlowElement(serviceEventToEnd);
        //结束节点
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        endEvent.setName("结束");
        process.addFlowElement(endEvent);
        //添加流程
        bpmnModel.addProcess(process);
        return bpmnModel;
    }




    public ProcessEngineConfiguration loadProcessEngineConfiguration(ProcessEngineConfiguration processEngineConfiguration) {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf8";
        String name = "root";
        String password = "root";
        processEngineConfiguration.setJdbcDriver(driver);
        processEngineConfiguration.setJdbcUrl(url);
        processEngineConfiguration.setJdbcUsername(name);
        processEngineConfiguration.setJdbcPassword(password);
        /**
         * public static final String DB_SCHEMA_UPDATE_FALSE = "false";//不能自动创建表，需要表存在
         * public static final String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";//先删除表再创建表
         * public static final String DB_SCHEMA_UPDATE_TRUE = "true";//如果表不存在，自动创建表
         **/
        processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        //设置邮件服务
        processEngineConfiguration.setMailServerHost("smtp.163.com");
        processEngineConfiguration.setMailServerDefaultFrom("abc");
        processEngineConfiguration.setMailServerUsername("abc");
        processEngineConfiguration.setMailServerPassword("***");
        processEngineConfiguration.setMailServerPort(465);
        processEngineConfiguration.setMailServerUseSSL(true);
        return processEngineConfiguration;
    }


    public Map<String, List<ExtensionElement>> addExtensionElement(Map<String, List<ExtensionElement>> extensionElements, ExtensionElement extensionElement) {
        List<ExtensionElement> elementList = null;
        if (extensionElements.containsKey(extensionElement.getName()) == false) {
            elementList = new ArrayList<ExtensionElement>();
            extensionElements.put(extensionElement.getName(), elementList);
        }
        extensionElements.get(extensionElement.getName()).add(extensionElement);
        return extensionElements;
    }

    /**
     * 代码生成activiti邮件服务的extensionElements
     * @param extensionElements
     * @param from              发送人,必填
     * @param to                主送,必填
     * @param cc                抄送
     * @param bcc               密送
     * @param subject           主题
     * @param html              内容,必填
     * @param charset           编码,必填
     * @return
     */
    public Map<String, List<ExtensionElement>> setMailConfig(Map<String, List<ExtensionElement>> extensionElements,
                                                             String from, String to, String cc, String bcc, String subject, String html, String charset) {
        //发送人
        ExtensionElement fromExtensionElement = new ExtensionElement();
        fromExtensionElement.setName("activiti:field");
        //发送人属性name="from"
        ExtensionAttribute fromAttribute = new ExtensionAttribute();
        fromAttribute.setName("name");
        fromAttribute.setValue("from");
        //发送人属性值stringValue="abc@..."
        ExtensionAttribute fromValueAttribute = new ExtensionAttribute();
        fromValueAttribute.setName("stringValue");
        fromValueAttribute.setValue(from);
        fromExtensionElement.addAttribute(fromAttribute);
        fromExtensionElement.addAttribute(fromValueAttribute);
        extensionElements = addExtensionElement(extensionElements, fromExtensionElement);
        //主送
        ExtensionElement toExtensionElement = new ExtensionElement();
        toExtensionElement.setName("activiti:field");
        ExtensionAttribute toAttribute = new ExtensionAttribute();
        toAttribute.setName("name");
        toAttribute.setValue("to");
        ExtensionAttribute toValueAttribute = new ExtensionAttribute();
        toValueAttribute.setName("stringValue");
        toValueAttribute.setValue(to);
        toExtensionElement.addAttribute(toAttribute);
        toExtensionElement.addAttribute(toValueAttribute);
        extensionElements = addExtensionElement(extensionElements, toExtensionElement);
        //抄送
        if (cc != null) {
            ExtensionElement ccExtensionElement = new ExtensionElement();
            ccExtensionElement.setName("activiti:field");
            ExtensionAttribute ccAttribute = new ExtensionAttribute();
            ccAttribute.setName("name");
            ccAttribute.setValue("cc");
            ExtensionAttribute ccValueAttribute = new ExtensionAttribute();
            ccValueAttribute.setName("stringValue");
            ccValueAttribute.setValue(cc);
            ccExtensionElement.addAttribute(ccAttribute);
            ccExtensionElement.addAttribute(ccValueAttribute);
            extensionElements = addExtensionElement(extensionElements, ccExtensionElement);
        }
        //密送
        if (bcc != null) {
            ExtensionElement bccExtensionElement = new ExtensionElement();
            bccExtensionElement.setName("activiti:field");
            ExtensionAttribute bccAttribute = new ExtensionAttribute();
            bccAttribute.setName("name");
            bccAttribute.setValue("bcc");
            ExtensionAttribute bccValueAttribute = new ExtensionAttribute();
            bccValueAttribute.setName("stringValue");
            bccValueAttribute.setValue(bcc);
            bccExtensionElement.addAttribute(bccAttribute);
            bccExtensionElement.addAttribute(bccValueAttribute);
            extensionElements = addExtensionElement(extensionElements, bccExtensionElement);
        }
        //主题
        ExtensionElement subjectExtensionElement = new ExtensionElement();
        subjectExtensionElement.setName("activiti:field");
        ExtensionAttribute subjectAttribute = new ExtensionAttribute();
        subjectAttribute.setName("name");
        subjectAttribute.setValue("subject");
        ExtensionAttribute subjectValueAttribute = new ExtensionAttribute();
        subjectValueAttribute.setName("expression");
        subjectValueAttribute.setValue(subject);
        subjectExtensionElement.addAttribute(subjectAttribute);
        subjectExtensionElement.addAttribute(subjectValueAttribute);
        extensionElements = addExtensionElement(extensionElements, subjectExtensionElement);
        //内容
        ExtensionElement htmlExtensionElement = new ExtensionElement();
        htmlExtensionElement.setName("activiti:field");
        ExtensionAttribute htmlAttribute = new ExtensionAttribute();
        htmlAttribute.setName("name");
        htmlAttribute.setValue("html");//html和text选择其一
        ExtensionAttribute htmlValueAttribute = new ExtensionAttribute();
        htmlValueAttribute.setName("stringValue");
        htmlValueAttribute.setValue(html);
        htmlExtensionElement.addAttribute(htmlAttribute);
        htmlExtensionElement.addAttribute(htmlValueAttribute);
        extensionElements = addExtensionElement(extensionElements, htmlExtensionElement);
        //编码
        ExtensionElement charsetExtensionElement = new ExtensionElement();
        charsetExtensionElement.setName("activiti:field");
        ExtensionAttribute charsetAttribute = new ExtensionAttribute();
        charsetAttribute.setName("name");
        charsetAttribute.setValue("charset");
        ExtensionAttribute charsetValueAttribute = new ExtensionAttribute();
        charsetValueAttribute.setName("stringValue");
        charsetValueAttribute.setValue(charset);
        charsetExtensionElement.addAttribute(charsetAttribute);
        charsetExtensionElement.addAttribute(charsetValueAttribute);
        extensionElements = addExtensionElement(extensionElements, charsetExtensionElement);

        return extensionElements;
    }
}
