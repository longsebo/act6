package com.activiti.utils;

import com.activiti.IAct6Constant;
import com.activiti.converter.*;
import com.activiti.runconverter.ExtBpmnStartEventXMLConverter;
import com.activiti.runconverter.ExtBpmnUserTaskXMLConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.export.ProcessExport;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述: Bpmn模型转换类
 *
 * @author JingXu
 * @date 2019/10/24 17:19
 * @version: v1.0
 */
public class BpmnConverterUtil {
    /**
     * 描述 : 将JsonNode格式的流程图转为前端适应的xml
     *
     * @param jsonStr
     * @return java.lang.String
     * @author yiyoung
     * @date 2020/02/27
     */
    public static String converterJsonToWebXml(String jsonStr) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CustomBpmnJsonConverter customBpmnJsonConverter = new CustomBpmnJsonConverter();
        BpmnModel bpmnModel = customBpmnJsonConverter.convertToBpmnModel(jsonNode);
        // 如果没有Processes,认为是一个空流程
        if (bpmnModel.getProcesses().isEmpty()) {
            return "";
        }
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        BpmnXMLConverter.addConverter(new UserTaskXMLConverter());
        BpmnXMLConverter.addConverter(new CallActivityXMLConverter());
        BpmnXMLConverter.addConverter(new CustomStartEventXMLConverter());
        BpmnXMLConverter.addConverter(new CustomSequenceFlowXMLConverter());
        byte[] bytes = bpmnXMLConverter.convertToXML(bpmnModel);
        return new String(bytes);
    }
    
    /**
     * 描述 : 将JsonNode格式的流程图转为标准的xml
     *
     * @param jsonStr
     * @return java.lang.String
     * @author jx
     * @date 2019/10/24 17:23
     */
    public static String converterJsonToXml(String jsonStr) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(jsonNode);
        // 如果没有Processes,认为是一个空流程
        if (bpmnModel.getProcesses().isEmpty()) {
            return "";
        }
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        byte[] bytes = bpmnXMLConverter.convertToXML(bpmnModel);
        return new String(bytes);
    }
    /**
     * 描述 : 将JsonNode格式的流程图转为扩展的BPMN的xml
     *
     * @param jsonStr
     * @return java.lang.String
     * @author yiyoung
     * @date 2020/02/27
     */
    public static String converterJsonToBpmnXml(String jsonStr) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CustomBpmnJsonConverter customBpmnJsonConverter = new CustomBpmnJsonConverter();
        BpmnModel bpmnModel = customBpmnJsonConverter.convertToBpmnModel(jsonNode);
        // 如果没有Processes,认为是一个空流程
        if (bpmnModel.getProcesses().isEmpty()) {
            return "";
        }
        removeInvalidatePropForProcess(bpmnModel.getMainProcess());

        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        BpmnXMLConverter.addConverter(new ExtBpmnUserTaskXMLConverter());
        BpmnXMLConverter.addConverter(new CallActivityXMLConverter());
        BpmnXMLConverter.addConverter(new ExtBpmnStartEventXMLConverter());
        BpmnXMLConverter.addConverter(new CustomSequenceFlowXMLConverter());
        byte[] bytes = bpmnXMLConverter.convertToXML(bpmnModel);
        return new String(bytes);
    }

    /**
     * 移去流程中无效的属性
     * @param process
     */
    private static void removeInvalidatePropForProcess(Process process) {
        Map<String, List<ExtensionAttribute>> attributes = process.getAttributes();
        //仅仅保留id,name,isExecutable 属性
        List<String> removeKeys= new ArrayList<>();
        for(String key:attributes.keySet()){
            if(!IAct6Constant.STANTD_BPMN_XML_ID.equalsIgnoreCase(key) &&
                    !IAct6Constant.STANTD_BPMN_XML_ISEXECUTABLE.equalsIgnoreCase(key) &&
            !IAct6Constant.STANTD_BPMN_XML_NAME.equalsIgnoreCase(key)){
                removeKeys.add(key);
            }
        }
        for(String key:removeKeys){
            attributes.remove(key);
        }
        process.setAttributes(attributes);
    }

    /**
     * 描述 : 将xml转为jsonnode
     *
     * @param xml
     * @return com.fasterxml.jackson.databind.JsonNode
     * @author jx
     * @date 2019/10/24 17:31
     */
    public static JsonNode converterXmlToJson(String xml) {
        // 创建转换对象
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        // XMLStreamReader读取XML资源
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        StringReader stringReader = new StringReader(xml);
        XMLStreamReader xmlStreamReader = null;
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        BpmnXMLConverter.addConverter(new UserTaskXMLConverter());
        BpmnXMLConverter.addConverter(new CallActivityXMLConverter());
        BpmnXMLConverter.addConverter(new CustomStartEventXMLConverter());
        BpmnXMLConverter.addConverter(new CustomSequenceFlowXMLConverter());
        System.out.println("---------------------xml-----------------");
        System.out.println(xml);
        System.out.println("---------------------xml-----------------");
        // 把xml转换成BpmnModel对象
        BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xmlStreamReader);
        //填充流程扩展属性
        fillProcessProperties(bpmnModel,xml);
        // 创建转换对象
        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        // 把BpmnModel对象转换成json
        JsonNode jsonNodes = bpmnJsonConverter.convertToJson(bpmnModel);
        return jsonNodes;
    }

    /**
     * 描述 : 将bpmn xml转为jsonnode
     *
     * @param xml
     * @return com.fasterxml.jackson.databind.JsonNode
     * @author jx
     * @date 2019/10/24 17:31
     */
    public static JsonNode converterBpmnXmlToJson(String xml) {
        // 创建转换对象
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        // XMLStreamReader读取XML资源
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        StringReader stringReader = new StringReader(xml);
        XMLStreamReader xmlStreamReader = null;
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        BpmnXMLConverter.addConverter(new ExtBpmnUserTaskXMLConverter());
        BpmnXMLConverter.addConverter(new CallActivityXMLConverter());
        BpmnXMLConverter.addConverter(new ExtBpmnStartEventXMLConverter());
        BpmnXMLConverter.addConverter(new CustomSequenceFlowXMLConverter());
        System.out.println("---------------------xml-----------------");
        System.out.println(xml);
        System.out.println("---------------------xml-----------------");
        // 把xml转换成BpmnModel对象
        BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xmlStreamReader);
        //填充流程扩展属性
        fillProcessProperties(bpmnModel,xml);
        // 创建转换对象
        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        // 把BpmnModel对象转换成json
        JsonNode jsonNodes = bpmnJsonConverter.convertToJson(bpmnModel);
        return jsonNodes;
    }

    /**
     * 填充流程属性
     * @param bpmnModel
     * @param xml
     */
    private static void fillProcessProperties(BpmnModel bpmnModel, String xml) {
        //获取流程列表
        List<Process> processes = bpmnModel.getProcesses();
        for(Process process:processes){
            String regExp="<process id=\""+process.getId()+"\"(.+?)>";
            String strProcessProperties = RegExUtils.getValue(xml,regExp,1);
            if(StringUtils.isNotEmpty(strProcessProperties)){
                List<ExtensionAttribute> attributes = parseProperties(strProcessProperties);
                System.out.println("xml 转json 扩展属性");
                for(ExtensionAttribute attribute:attributes){

                    //判断是否为扩展属性
                    if(!isProcessDefaultAttribute(attribute)) {
                        System.out.println("加入扩展属性到json:"+attribute.getName());
                        process.addAttribute(attribute);
//                        //扩展了作者
//                        if ("activiti:process_author".equalsIgnoreCase(attribute.getName())) {
//                            process.addAttribute(attribute);
//                        }
//                        //扩展文档
//                        if ("documentation".equalsIgnoreCase(attribute.getName())) {
//                            attribute.setName("activiti:" + attribute.getName());
//                            process.addAttribute(attribute);
//                        }
                    }
                }
            }
        }
    }

    /**
     * 判断属性是否为流程的默认属性
     * @param attribute
     * @return
     */
    private static boolean isProcessDefaultAttribute(ExtensionAttribute attribute) {
        for(ExtensionAttribute defaultAttribute:ProcessExport.defaultProcessAttributes){
            if(defaultAttribute.getName().equalsIgnoreCase(attribute.getName())){
                return  true;
            }
        }
        return false;
    }

    /**
     *
     * @param strProcessProperties
     * @return
     */
    private static List<ExtensionAttribute> parseProperties(String strProcessProperties) {
        List<ExtensionAttribute> attributes = new ArrayList<>();
        //先按空格拆分，后拆分等号
        String[]  strPairs = strProcessProperties.split(" ");
        if(strPairs!=null && strPairs.length>0) {
            for (String temp : strPairs) {
                if (temp.contains("=")) {
                    String[] keyValue = temp.split("=");
                    if (keyValue != null && keyValue.length == 2 && StringUtils.isNotEmpty(keyValue[0]) && StringUtils.isNotEmpty(keyValue[1])) {
                        ExtensionAttribute attribute = new ExtensionAttribute();
                        attribute.setName(keyValue[0].trim());
                        attribute.setValue(keyValue[1].trim());
                        attributes.add(attribute);
                    }
                }
            }
        }
        return attributes;
    }
}
