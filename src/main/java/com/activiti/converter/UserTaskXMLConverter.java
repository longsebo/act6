package com.activiti.converter;

import com.activiti.IAct6Constant;
import com.activiti.utils.RegExUtils;
import org.activiti.bpmn.converter.BaseBpmnXMLConverter;
import org.activiti.bpmn.converter.XMLStreamReaderUtil;
import org.activiti.bpmn.converter.child.BaseChildElementParser;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.converter.util.CommaSplitter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.alfresco.AlfrescoUserTask;
import org.activiti.editor.constants.StencilConstants;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

public class UserTaskXMLConverter extends BaseBpmnXMLConverter {
    
    /**
     * default attributes taken from bpmn spec and from activiti extension
     */
    public static final List<ExtensionAttribute> defaultUserTaskAttributes = Arrays
            .asList(new ExtensionAttribute(null, ATTRIBUTE_FORM_FORMKEY),
                    new ExtensionAttribute(null, ATTRIBUTE_TASK_USER_DUEDATE),
                    new ExtensionAttribute(null, ATTRIBUTE_TASK_USER_ASSIGNEE),
                    new ExtensionAttribute(null, ATTRIBUTE_TASK_USER_PRIORITY),
                    new ExtensionAttribute(null, ATTRIBUTE_TASK_USER_CANDIDATEUSERS),
                    new ExtensionAttribute(null, ATTRIBUTE_TASK_USER_CANDIDATEGROUPS),
                    new ExtensionAttribute(null, ATTRIBUTE_TASK_USER_CATEGORY),
                    new ExtensionAttribute(null, ATTRIBUTE_TASK_SERVICE_EXTENSIONID),
                    new ExtensionAttribute(null, ATTRIBUTE_TASK_USER_SKIP_EXPRESSION));
    //所以缺省属性
    public   List<ExtensionAttribute> allDefaultAttributes;
    protected Map<String, BaseChildElementParser> childParserMap = new HashMap<String, BaseChildElementParser>();
    
    public UserTaskXMLConverter() {
        HumanPerformerParser humanPerformerParser = new HumanPerformerParser();
        childParserMap.put(humanPerformerParser.getElementName(),
                humanPerformerParser);
        PotentialOwnerParser potentialOwnerParser = new PotentialOwnerParser();
        childParserMap.put(potentialOwnerParser.getElementName(),
                potentialOwnerParser);
        CustomIdentityLinkParser customIdentityLinkParser = new CustomIdentityLinkParser();
        childParserMap.put(customIdentityLinkParser.getElementName(),
                customIdentityLinkParser);
        allDefaultAttributes= new ArrayList<>();
        allDefaultAttributes.addAll(defaultUserTaskAttributes);
        allDefaultAttributes.addAll(defaultElementAttributes);
        allDefaultAttributes.addAll(defaultActivityAttributes);
    }
    @Override
    public Class<? extends BaseElement> getBpmnElementType() {
        return UserTask.class;
    }
    
    @Override
    protected String getXMLElementName() {
        return ELEMENT_TASK_USER;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected BaseElement convertXMLToElement(XMLStreamReader xtr,
                                              BpmnModel model) throws Exception {
        String formKey = xtr.getAttributeValue(null, ATTRIBUTE_FORM_FORMKEY);
        UserTask userTask = null;
        if (StringUtils.isNotEmpty(formKey)) {
            if (model.getUserTaskFormTypes() != null
                    && model.getUserTaskFormTypes().contains(formKey)) {
                userTask = new AlfrescoUserTask();
            }
        }
        if (userTask == null) {
            userTask = new UserTask();
        }
        BpmnXMLUtil.addXMLLocation(userTask, xtr);
        userTask
                .setDueDate(xtr.getAttributeValue(null, ATTRIBUTE_TASK_USER_DUEDATE));
        userTask
                .setCategory(xtr.getAttributeValue(null, ATTRIBUTE_TASK_USER_CATEGORY));
        userTask.setFormKey(formKey);

        //设置代理人ID
        userTask
                .setAssignee(xtr.getAttributeValue(null, IAct6Constant.EXP_NODE_PROP_ASSIGNEE_ID));
        userTask.setOwner(xtr.getAttributeValue(null, ATTRIBUTE_TASK_USER_OWNER));
        userTask
                .setPriority(xtr.getAttributeValue(null, ATTRIBUTE_TASK_USER_PRIORITY));
        //候选用户id
        if (StringUtils.isNotEmpty(
                xtr.getAttributeValue(null, IAct6Constant.EXP_NODE_PROP_CANDIDATE_USERS))) {
            //提取id列表加入
            userTask.getCandidateUsers().addAll(RegExUtils.getValue(xtr.getAttributeValue(null, IAct6Constant.EXP_NODE_PROP_CANDIDATE_USERS),"\"id\":\"(.+?)\""));
        }
        //提取候选角色，候选机构
        if (StringUtils.isNotEmpty(
                xtr.getAttributeValue(null, IAct6Constant.EXP_NODE_PROP_CANDIDATE_ROLES))) {
            String expression = xtr.getAttributeValue(null,
                    IAct6Constant.EXP_NODE_PROP_CANDIDATE_ROLES);
            userTask.getCandidateGroups().addAll(RegExUtils.getValue(expression,"\"id\":\"(.+?)\""));
        }
        if (StringUtils.isNotEmpty(
                xtr.getAttributeValue(null, IAct6Constant.EXP_NODE_PROP_CANDIDATE_ORGANIZATIONS))) {
            String expression = xtr.getAttributeValue(null,
                    IAct6Constant.EXP_NODE_PROP_CANDIDATE_ORGANIZATIONS);
            userTask.getCandidateGroups().addAll(RegExUtils.getValue(expression,"\"id\":\"(.+?)\""));
        }
        userTask.setExtensionId(
                xtr.getAttributeValue(null, ATTRIBUTE_TASK_SERVICE_EXTENSIONID));
        
        if (StringUtils.isNotEmpty(
                xtr.getAttributeValue(null, ATTRIBUTE_TASK_USER_SKIP_EXPRESSION))) {
            String expression = xtr.getAttributeValue(null,
                    ATTRIBUTE_TASK_USER_SKIP_EXPRESSION);
            userTask.setSkipExpression(expression);
        }
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();


        //加入多实例的相关属性
        //多实例类型
        if(StringUtils.isNotEmpty(xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_TYPE)) &&
         !IAct6Constant.PROPERTY_MULTIINSTANCE_NONE.equalsIgnoreCase(xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_TYPE))){
            String multiinstanceType = xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_TYPE);
            if(IAct6Constant.PROPERTY_MULTIINSTANCE_SEQUENTIAL.equalsIgnoreCase(multiinstanceType)) {
                multiInstanceLoopCharacteristics.setSequential(true);
            }else {
                multiInstanceLoopCharacteristics.setSequential(false);
            }

            //基数
            if(StringUtils.isNotEmpty(xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_CARDINALITY))){
                String value = xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_CARDINALITY);
                multiInstanceLoopCharacteristics.setLoopCardinality(value);
            }
            //多实例采集变量
            if(StringUtils.isNotEmpty(xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION))){
                String value = xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION);
                multiInstanceLoopCharacteristics.setInputDataItem(value);
            }
            //元素的变量(多实例)
            if(StringUtils.isNotEmpty(xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_VARIABLE))){
                String value = xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_VARIABLE);
                multiInstanceLoopCharacteristics.setElementVariable(value);
            }
            // 通过条件---对应前端通过权重
            if(StringUtils.isNotEmpty(xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_CONDITION))){
                String value = xtr.getAttributeValue(null, IAct6Constant.PROPERTY_MULTIINSTANCE_CONDITION);
                //转换为数字,转换失败时，默认为100%
                double d=100;
                try{
                    d = Double.valueOf(value);
                }catch(Exception e){
                    d=100;
                }
                String exp= "${nrOfCompletedInstances/nrOfInstances >= "+d/100+"}";
                multiInstanceLoopCharacteristics.setCompletionCondition(exp);
            }
            //设置到用户
            userTask.setLoopCharacteristics(multiInstanceLoopCharacteristics);
        }
        // 全部的属性都在这里
        BpmnXMLUtil.addCustomAttributes(xtr, userTask, defaultElementAttributes,
                defaultActivityAttributes, defaultUserTaskAttributes);
        
//        parseChildElements(getXMLElementName(), userTask, childParserMap, model,
//                xtr);
        
        return userTask;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void writeAdditionalAttributes(BaseElement element, BpmnModel model,
                                             XMLStreamWriter xtw) throws Exception {
        UserTask userTask = (UserTask) element;
        writeQualifiedAttribute(ATTRIBUTE_TASK_USER_ASSIGNEE,
                userTask.getAssignee(), xtw);
        writeQualifiedAttribute(ATTRIBUTE_TASK_USER_OWNER, userTask.getOwner(),
                xtw);
        writeQualifiedAttribute(ATTRIBUTE_TASK_USER_CANDIDATEUSERS,
                convertToDelimitedString(userTask.getCandidateUsers()), xtw);
        writeQualifiedAttribute(ATTRIBUTE_TASK_USER_CANDIDATEGROUPS,
                convertToDelimitedString(userTask.getCandidateGroups()), xtw);
        writeQualifiedAttribute(ATTRIBUTE_TASK_USER_DUEDATE, userTask.getDueDate(),
                xtw);
        writeQualifiedAttribute(ATTRIBUTE_TASK_USER_CATEGORY,
                userTask.getCategory(), xtw);
        writeQualifiedAttribute(ATTRIBUTE_FORM_FORMKEY, userTask.getFormKey(), xtw);
        if (userTask.getPriority() != null) {
            writeQualifiedAttribute(ATTRIBUTE_TASK_USER_PRIORITY,
                    userTask.getPriority().toString(), xtw);
        }
        if (StringUtils.isNotEmpty(userTask.getExtensionId())) {
            writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_EXTENSIONID,
                    userTask.getExtensionId(), xtw);
        }
        if (userTask.getSkipExpression() != null) {
            writeQualifiedAttribute(ATTRIBUTE_TASK_USER_SKIP_EXPRESSION,
                    userTask.getSkipExpression(), xtw);
        }
        //扩展属性添加activiti前缀

        List<ExtensionAttribute> prefixActivitiList =  addActivitiPrefixForCustomAttributes(userTask.getAttributes().values(),allDefaultAttributes);
        List<List<ExtensionAttribute>> wrapAttributeCollection = new ArrayList<>();
        wrapAttributeCollection.add(prefixActivitiList);

        // write custom attributes
        BpmnXMLUtil.writeCustomAttributes(wrapAttributeCollection, xtw,
                defaultElementAttributes, defaultActivityAttributes,
                defaultUserTaskAttributes);
    }
    /**
     * 扩展属性添加activiti前缀
     * @param attributes
     * @param allDefaultAttributes
     * @return
     */
    private List<ExtensionAttribute> addActivitiPrefixForCustomAttributes(Collection<List<ExtensionAttribute>> attributes, List<ExtensionAttribute> allDefaultAttributes) {
        List<ExtensionAttribute> returnAttributes = new ArrayList<>();
        for(List<ExtensionAttribute> attributeList:attributes){
            for(ExtensionAttribute attribute:attributeList){
                boolean isDefault = false;
                //判断是否为缺省的
                for(ExtensionAttribute defaultAttribute:allDefaultAttributes){
                    if(attribute.getName().equalsIgnoreCase(defaultAttribute.getName())){
                        isDefault = true;
                        break;
                    }
                }
                if(!isDefault){
                    ExtensionAttribute cloneAttribute = cloneAttribute(attribute);
                    //增加前缀
                    if(!cloneAttribute.getName().startsWith(IAct6Constant.VUE_ACTIVITI_PREFIX)){
                        cloneAttribute.setName(IAct6Constant.VUE_ACTIVITI_PREFIX+cloneAttribute.getName());
                    }
                    returnAttributes.add(cloneAttribute);
                }
            }
        }
        return returnAttributes;
    }

    /**
     * 克隆属性对象
     * @param attribute
     * @return
     */
    private ExtensionAttribute cloneAttribute(ExtensionAttribute attribute) {
        ExtensionAttribute returnAttribute = new ExtensionAttribute();
        returnAttribute.setName(attribute.getName());
        returnAttribute.setValue(attribute.getValue());
        returnAttribute.setNamespace(attribute.getNamespace());
        returnAttribute.setNamespacePrefix(attribute.getNamespacePrefix());
        return returnAttribute;
    }

    @Override
    protected boolean writeExtensionChildElements(BaseElement element,
                                                  boolean didWriteExtensionStartElement, XMLStreamWriter xtw)
            throws Exception {
        UserTask userTask = (UserTask) element;
        didWriteExtensionStartElement = writeFormProperties(userTask,
                didWriteExtensionStartElement, xtw);
        didWriteExtensionStartElement = writeCustomIdentities(element,
                didWriteExtensionStartElement, xtw);
        if (!userTask.getCustomProperties().isEmpty()) {
            for (CustomProperty customProperty : userTask.getCustomProperties()) {
                
                if (StringUtils.isEmpty(customProperty.getSimpleValue())) {
                    continue;
                }
                
                if (!didWriteExtensionStartElement) {
                    xtw.writeStartElement(ELEMENT_EXTENSIONS);
                    didWriteExtensionStartElement = true;
                }
                xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX,
                        customProperty.getName(), ACTIVITI_EXTENSIONS_NAMESPACE);
                xtw.writeCharacters(customProperty.getSimpleValue());
                xtw.writeEndElement();
            }
        }
        return didWriteExtensionStartElement;
    }
    
    protected boolean writeCustomIdentities(BaseElement element,
                                            boolean didWriteExtensionStartElement, XMLStreamWriter xtw)
            throws Exception {
        UserTask userTask = (UserTask) element;
        if (userTask.getCustomUserIdentityLinks().isEmpty()
                && userTask.getCustomGroupIdentityLinks().isEmpty()) {
            return didWriteExtensionStartElement;
        }
        
        if (!didWriteExtensionStartElement) {
            xtw.writeStartElement(ELEMENT_EXTENSIONS);
            didWriteExtensionStartElement = true;
        }
        Set<String> identityLinkTypes = new HashSet<String>();
        identityLinkTypes.addAll(userTask.getCustomUserIdentityLinks().keySet());
        identityLinkTypes.addAll(userTask.getCustomGroupIdentityLinks().keySet());
        for (String identityType : identityLinkTypes) {
            writeCustomIdentities(userTask, identityType,
                    userTask.getCustomUserIdentityLinks().get(identityType),
                    userTask.getCustomGroupIdentityLinks().get(identityType), xtw);
        }
        
        return didWriteExtensionStartElement;
    }
    
    protected void writeCustomIdentities(UserTask userTask, String identityType,
                                         Set<String> users, Set<String> groups, XMLStreamWriter xtw)
            throws Exception {
        xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_CUSTOM_RESOURCE,
                ACTIVITI_EXTENSIONS_NAMESPACE);
        writeDefaultAttribute(ATTRIBUTE_NAME, identityType, xtw);
        
        List<String> identityList = new ArrayList<String>();
        
        if (users != null) {
            for (String userId : users) {
                identityList.add("user(" + userId + ")");
            }
        }
        
        if (groups != null) {
            for (String groupId : groups) {
                identityList.add("group(" + groupId + ")");
            }
        }
        
        String delimitedString = convertToDelimitedString(identityList);
        
        xtw.writeStartElement(ELEMENT_RESOURCE_ASSIGNMENT);
        xtw.writeStartElement(ELEMENT_FORMAL_EXPRESSION);
        xtw.writeCharacters(delimitedString);
        xtw.writeEndElement(); // End ELEMENT_FORMAL_EXPRESSION
        xtw.writeEndElement(); // End ELEMENT_RESOURCE_ASSIGNMENT
        
        xtw.writeEndElement(); // End ELEMENT_CUSTOM_RESOURCE
    }
    
    @Override
    protected void writeAdditionalChildElements(BaseElement element,
                                                BpmnModel model, XMLStreamWriter xtw) throws Exception {
    }
    
    public class HumanPerformerParser extends BaseChildElementParser {
        @Override
        public String getElementName() {
            return "humanPerformer";
        }
        @Override
        public void parseChildElement(XMLStreamReader xtr,
                                      BaseElement parentElement, BpmnModel model) throws Exception {
            String resourceElement = XMLStreamReaderUtil.moveDown(xtr);
            if (StringUtils.isNotEmpty(resourceElement)
                    && ELEMENT_RESOURCE_ASSIGNMENT.equals(resourceElement)) {
                String expression = XMLStreamReaderUtil.moveDown(xtr);
                if (StringUtils.isNotEmpty(expression)
                        && ELEMENT_FORMAL_EXPRESSION.equals(expression)) {
                    ((UserTask) parentElement).setAssignee(xtr.getElementText());
                }
            }
        }
    }
    
    public class PotentialOwnerParser extends BaseChildElementParser {
        @Override
        public String getElementName() {
            return "potentialOwner";
        }
        @Override
        public void parseChildElement(XMLStreamReader xtr,
                                      BaseElement parentElement, BpmnModel model) throws Exception {
            String resourceElement = XMLStreamReaderUtil.moveDown(xtr);
            if (StringUtils.isNotEmpty(resourceElement)
                    && ELEMENT_RESOURCE_ASSIGNMENT.equals(resourceElement)) {
                String expression = XMLStreamReaderUtil.moveDown(xtr);
                if (StringUtils.isNotEmpty(expression)
                        && ELEMENT_FORMAL_EXPRESSION.equals(expression)) {
                    
                    List<String> assignmentList = CommaSplitter
                            .splitCommas(xtr.getElementText());
                    
                    for (String assignmentValue : assignmentList) {
                        if (assignmentValue == null) {
                            continue;
                        }
                        
                        assignmentValue = assignmentValue.trim();
                        
                        if (assignmentValue.length() == 0) {
                            continue;
                        }
                        
                        String userPrefix = "user(";
                        String groupPrefix = "group(";
                        if (assignmentValue.startsWith(userPrefix)) {
                            assignmentValue = assignmentValue
                                    .substring(userPrefix.length(), assignmentValue.length() - 1)
                                    .trim();
                            ((UserTask) parentElement).getCandidateUsers()
                                    .add(assignmentValue);
                        } else if (assignmentValue.startsWith(groupPrefix)) {
                            assignmentValue = assignmentValue
                                    .substring(groupPrefix.length(), assignmentValue.length() - 1)
                                    .trim();
                            ((UserTask) parentElement).getCandidateGroups()
                                    .add(assignmentValue);
                        } else {
                            ((UserTask) parentElement).getCandidateGroups()
                                    .add(assignmentValue);
                        }
                    }
                }
            } else if (StringUtils.isNotEmpty(resourceElement)
                    && ELEMENT_RESOURCE_REF.equals(resourceElement)) {
                String resourceId = xtr.getElementText();
                if (model.containsResourceId(resourceId)) {
                    Resource resource = model.getResource(resourceId);
                    ((UserTask) parentElement).getCandidateGroups()
                            .add(resource.getName());
                } else {
                    Resource resource = new Resource(resourceId, resourceId);
                    model.addResource(resource);
                    ((UserTask) parentElement).getCandidateGroups()
                            .add(resource.getName());
                }
            }
        }
    }
    
    public class CustomIdentityLinkParser extends BaseChildElementParser {
        @Override
        public String getElementName() {
            return ELEMENT_CUSTOM_RESOURCE;
        }
        @Override
        public void parseChildElement(XMLStreamReader xtr,
                                      BaseElement parentElement, BpmnModel model) throws Exception {
            String identityLinkType = xtr
                    .getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_NAME);
            
            // the attribute value may be unqualified
            if (identityLinkType == null) {
                identityLinkType = xtr.getAttributeValue(null, ATTRIBUTE_NAME);
            }
            
            if (identityLinkType == null) {
                return;
            }
            
            String resourceElement = XMLStreamReaderUtil.moveDown(xtr);
            if (StringUtils.isNotEmpty(resourceElement)
                    && ELEMENT_RESOURCE_ASSIGNMENT.equals(resourceElement)) {
                String expression = XMLStreamReaderUtil.moveDown(xtr);
                if (StringUtils.isNotEmpty(expression)
                        && ELEMENT_FORMAL_EXPRESSION.equals(expression)) {
                    
                    List<String> assignmentList = CommaSplitter
                            .splitCommas(xtr.getElementText());
                    
                    for (String assignmentValue : assignmentList) {
                        if (assignmentValue == null) {
                            continue;
                        }
                        
                        assignmentValue = assignmentValue.trim();
                        
                        if (assignmentValue.length() == 0) {
                            continue;
                        }
                        
                        String userPrefix = "user(";
                        String groupPrefix = "group(";
                        if (assignmentValue.startsWith(userPrefix)) {
                            assignmentValue = assignmentValue
                                    .substring(userPrefix.length(), assignmentValue.length() - 1)
                                    .trim();
                            ((UserTask) parentElement)
                                    .addCustomUserIdentityLink(assignmentValue, identityLinkType);
                        } else if (assignmentValue.startsWith(groupPrefix)) {
                            assignmentValue = assignmentValue
                                    .substring(groupPrefix.length(), assignmentValue.length() - 1)
                                    .trim();
                            ((UserTask) parentElement).addCustomGroupIdentityLink(
                                    assignmentValue, identityLinkType);
                        } else {
                            ((UserTask) parentElement).addCustomGroupIdentityLink(
                                    assignmentValue, identityLinkType);
                        }
                    }
                }
            }
        }
    }
}
