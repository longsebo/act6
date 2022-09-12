/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.activiti.converter;

import com.activiti.IAct6Constant;
import com.activiti.utils.RegExUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.editor.language.json.converter.ActivityProcessor;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.BpmnJsonConverterUtil;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author Tijs Rademakers
 */
public class CustomUserTaskJsonConverter extends BaseBpmnJsonConverter {
    
    private static final Logger LOG = LoggerFactory.getLogger(CustomUserTaskJsonConverter.class);
    
    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,

    Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }
    
    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_TASK_USER, CustomUserTaskJsonConverter.class);
    }
    
    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(UserTask.class, CustomUserTaskJsonConverter.class);
    }
    
    @Override
    public void convertToBpmnModel(JsonNode elementNode, JsonNode modelNode, ActivityProcessor processor, BaseElement parentElement,
                                   Map<String, JsonNode> shapeMap, BpmnModel bpmnModel) {
        
        this.processor = processor;
        this.model = bpmnModel;
        
        BaseElement baseElement = convertJsonToElement(elementNode, modelNode, shapeMap);
        baseElement.setId(BpmnJsonConverterUtil.getElementId(elementNode));
        
        if (baseElement instanceof FlowElement) {
            FlowElement flowElement = (FlowElement) baseElement;
            flowElement.setName(getPropertyValueAsString(PROPERTY_NAME, elementNode));
            flowElement.setDocumentation(getPropertyValueAsString(PROPERTY_DOCUMENTATION, elementNode));
            
            BpmnJsonConverterUtil.convertJsonToListeners(elementNode, flowElement);
            
            if (baseElement instanceof Activity) {
                Activity activity = (Activity) baseElement;
                activity.setAsynchronous(getPropertyValueAsBoolean(PROPERTY_ASYNCHRONOUS, elementNode));
                activity.setNotExclusive(!getPropertyValueAsBoolean(PROPERTY_EXCLUSIVE, elementNode));
                // 多实例类型 注意看
                String multiInstanceType = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_TYPE, elementNode);
                // 通过权重
                String multiInstanceCondition = getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_CONDITION, elementNode);
                if (StringUtils.isNotEmpty(multiInstanceType) && !IAct6Constant.PROPERTY_MULTIINSTANCE_NONE.equalsIgnoreCase(multiInstanceType)) {

                    
                    MultiInstanceLoopCharacteristics multiInstanceObject = new MultiInstanceLoopCharacteristics();
                    if (IAct6Constant.PROPERTY_MULTIINSTANCE_SEQUENTIAL.equalsIgnoreCase(multiInstanceType)) {
                        multiInstanceObject.setSequential(true);
                    } else {
                        multiInstanceObject.setSequential(false);
                    }
                    
                    if (StringUtils.isNotEmpty(multiInstanceCondition)) {
                        //转换为数字,转换失败时，默认为100%
                        double d=100;
                        try{
                            d = Double.valueOf(multiInstanceCondition);
                        }catch(Exception e){
                            d=100;
                        }
                        String exp= "${nrOfCompletedInstances/nrOfInstances >= "+d/100+"}";
                        multiInstanceObject.setCompletionCondition(exp);
                    } else {
                        String exp= "${nrOfCompletedInstances/nrOfInstances >= 1.0}";
                        multiInstanceObject.setCompletionCondition(exp);
                    }
                    //基数
                    if(StringUtils.isNotEmpty( getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_CARDINALITY, elementNode))){
                        String value = getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_CARDINALITY, elementNode);
                        multiInstanceObject.setLoopCardinality(value);
                    }
                    //多实例采集变量
                    if(StringUtils.isNotEmpty( getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION, elementNode))){
                        String value = getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION, elementNode);
                        multiInstanceObject.setInputDataItem(value);
                    }
                    //元素的变量(多实例)
                    if(StringUtils.isNotEmpty( getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_VARIABLE, elementNode))){
                        String value = getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_VARIABLE, elementNode);
                        multiInstanceObject.setElementVariable(value);
                    }
                        
                    activity.setLoopCharacteristics(multiInstanceObject);

                }
                
            } else if (baseElement instanceof Gateway) {
                // 网关流程顺序设置
                JsonNode flowOrderNode = getProperty(PROPERTY_SEQUENCEFLOW_ORDER, elementNode);
                if (flowOrderNode != null) {
                    flowOrderNode = BpmnJsonConverterUtil.validateIfNodeIsTextual(flowOrderNode);
                    JsonNode orderArray = flowOrderNode.get("sequenceFlowOrder");
                    if (orderArray != null && orderArray.size() > 0) {
                        for (JsonNode orderNode : orderArray) {
                            ExtensionElement orderElement = new ExtensionElement();
                            orderElement.setName("EDITOR_FLOW_ORDER");
                            orderElement.setElementText(orderNode.asText());
                            flowElement.addExtensionElement(orderElement);
                        }
                    }
                }
            }
        }
        
        if (baseElement instanceof FlowElement) {
            FlowElement flowElement = (FlowElement) baseElement;
            if (flowElement instanceof SequenceFlow) {
                ExtensionElement idExtensionElement = new ExtensionElement();
                idExtensionElement.setName("EDITOR_RESOURCEID");
                idExtensionElement.setElementText(elementNode.get(EDITOR_SHAPE_ID).asText());
                flowElement.addExtensionElement(idExtensionElement);
            }
            
            if (parentElement instanceof Process) {
                ((Process) parentElement).addFlowElement(flowElement);
                
            } else if (parentElement instanceof SubProcess) {
                ((SubProcess) parentElement).addFlowElement(flowElement);
                
            } else if (parentElement instanceof Lane) {
                Lane lane = (Lane) parentElement;
                lane.getFlowReferences().add(flowElement.getId());
                lane.getParentProcess().addFlowElement(flowElement);
            }
            
        } else if (baseElement instanceof Artifact) {
            Artifact artifact = (Artifact) baseElement;
            if (parentElement instanceof Process) {
                ((Process) parentElement).addArtifact(artifact);
                
            } else if (parentElement instanceof SubProcess) {
                ((SubProcess) parentElement).addArtifact(artifact);
                
            } else if (parentElement instanceof Lane) {
                Lane lane = (Lane) parentElement;
                lane.getFlowReferences().add(artifact.getId());
                lane.getParentProcess().addArtifact(artifact);
            }
        }
        
    }
    
    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        UserTask userTask = (UserTask) baseElement;
        String assignee = userTask.getAssignee();
        String owner = userTask.getOwner();

        //将扩展属性转换为json
        for(List<ExtensionAttribute>  attributeList:userTask.getAttributes().values()){
            for(ExtensionAttribute attribute:attributeList){
                if(!isDefaultAttribute(attribute.getName())){
                    propertiesNode.put(attribute.getName(),attribute.getValue());
                }
            }
        }
        if (StringUtils.isNotEmpty(assignee) || StringUtils.isNotEmpty(owner) || CollectionUtils.isNotEmpty(userTask.getCandidateUsers()) ||
                CollectionUtils.isNotEmpty(userTask.getCandidateGroups())) {
            
            ObjectNode assignmentNode = objectMapper.createObjectNode();
            ObjectNode assignmentValuesNode = objectMapper.createObjectNode();
            
            if (StringUtils.isNotEmpty(assignee)) {
                assignmentValuesNode.put(PROPERTY_USERTASK_ASSIGNEE, assignee);
            }
            
            if (StringUtils.isNotEmpty(owner)) {
                assignmentValuesNode.put(PROPERTY_USERTASK_OWNER, owner);
            }
            
            if (CollectionUtils.isNotEmpty(userTask.getCandidateUsers())) {
                ArrayNode candidateArrayNode = objectMapper.createArrayNode();
                for (String candidateUser : userTask.getCandidateUsers()) {
                    ObjectNode candidateNode = objectMapper.createObjectNode();
                    candidateNode.put("value", candidateUser);
                    candidateArrayNode.add(candidateNode);
                }
                assignmentValuesNode.put(PROPERTY_USERTASK_CANDIDATE_USERS, candidateArrayNode);
            }
            
            if (CollectionUtils.isNotEmpty(userTask.getCandidateGroups())) {
                ArrayNode candidateArrayNode = objectMapper.createArrayNode();
                for (String candidateGroup : userTask.getCandidateGroups()) {
                    ObjectNode candidateNode = objectMapper.createObjectNode();
                    candidateNode.put("value", candidateGroup);
                    candidateArrayNode.add(candidateNode);
                }
                assignmentValuesNode.put(PROPERTY_USERTASK_CANDIDATE_GROUPS, candidateArrayNode);
            }
            
            assignmentNode.put("assignment", assignmentValuesNode);
            propertiesNode.put(PROPERTY_USERTASK_ASSIGNMENT, assignmentNode);
        }
        
        if (userTask.getPriority() != null) { // getAttributes().get("weights").get(0).getValue()
            setPropertyValue(PROPERTY_USERTASK_PRIORITY, userTask.getPriority().toString(), propertiesNode);
        }
        

        
        setPropertyValue(PROPERTY_USERTASK_DUEDATE, userTask.getDueDate(), propertiesNode);
        setPropertyValue(PROPERTY_USERTASK_CATEGORY, userTask.getCategory(), propertiesNode);
        // 添加用户任务的自定义属性
        if (userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_TYPE) != null && !"".equals(userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_TYPE).get(0).getValue())) {
            setPropertyValue(PROPERTY_MULTIINSTANCE_TYPE, userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_TYPE).get(0).getValue(), propertiesNode);
        }
        
        if (userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_CARDINALITY) != null && !"".equals(userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_CARDINALITY).get(0).getValue())) {
            setPropertyValue(PROPERTY_MULTIINSTANCE_CARDINALITY, userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_CARDINALITY).get(0).getValue(), propertiesNode);
        }
        
        if (userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION) != null && !"".equals(userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION).get(0).getValue())) {
            setPropertyValue(PROPERTY_MULTIINSTANCE_COLLECTION, userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION).get(0).getValue(), propertiesNode);
        }
        if (userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_VARIABLE) != null && !"".equals(userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_VARIABLE).get(0).getValue())) {
            setPropertyValue(PROPERTY_MULTIINSTANCE_VARIABLE, userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_VARIABLE).get(0).getValue(), propertiesNode);
        }
        if (userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION) != null && !"".equals(userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION).get(0).getValue())) {
            setPropertyValue(PROPERTY_MULTIINSTANCE_CONDITION, userTask.getAttributes().get(IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION).get(0).getValue(), propertiesNode);
        }

        
        // 添加默认流程属性
        if (StringUtils.isNotEmpty(userTask.getDefaultFlow())) {
            setPropertyValue("default", userTask.getDefaultFlow(), propertiesNode);
        }
        // end
        addFormProperties(userTask.getFormProperties(), propertiesNode);
    }
    /**
     * 设置任务节点扩展属性
     * @param modelNode
     * @param userTask
     */
    private void setExtendProperties(JsonNode modelNode, UserTask userTask) {

        if (modelNode.get("properties") != null) {
            JsonNode propertiesNode = modelNode.get("properties");
            Iterator<String> it = propertiesNode.fieldNames();
            while(it.hasNext()){
                String propertyName =it.next();
                if(!isDefaultAttribute(propertyName)){
                    ExtensionAttribute extensionElement1 = new ExtensionAttribute();
                    extensionElement1.setName(propertyName);
                    JsonNode jsonNode = propertiesNode.get(propertyName);
                    if (jsonNode != null && !jsonNode.isNull()) {
                        extensionElement1.setValue( jsonNode.asText());
                    }

                    userTask.addAttribute(extensionElement1);
                }
            }
        }
    }
    /**
     * 判断是否为默认属性
     * @param attributeName
     * @return
     */
    private boolean isDefaultAttribute(String  attributeName) {
        for(ExtensionAttribute defaultAttribute: UserTaskXMLConverter.defaultUserTaskAttributes){
            if(defaultAttribute.getName().equalsIgnoreCase(attributeName)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        UserTask task = new UserTask();
        task.setPriority(getPropertyValueAsString(PROPERTY_USERTASK_PRIORITY, elementNode));
        String formKey = getPropertyValueAsString(PROPERTY_FORMKEY, elementNode);
        String name = getPropertyValueAsString(PROPERTY_NAME, elementNode);
        if (StringUtils.isNotEmpty(formKey)) {
            task.setFormKey(formKey);
        } else {
            LOG.error(name + "没有配置表单");
        }
        //设置扩展属性
        setExtendProperties(elementNode, task);
        task.setDueDate(getPropertyValueAsString(PROPERTY_USERTASK_DUEDATE, elementNode));
        task.setCategory(getPropertyValueAsString(PROPERTY_USERTASK_CATEGORY, elementNode));
        //设置代理人ID
        task.setAssignee(getPropertyValueAsString(IAct6Constant.EXP_NODE_PROP_ASSIGNEE_ID,elementNode));
        //task.setOwner(getPropertyValueAsString(ATTRIBUTE_TASK_USER_OWNER,elementNode));
        //task
            //    .setPriority(xtr.getAttributeValue(null, ATTRIBUTE_TASK_USER_PRIORITY));
        //候选用户id
        if (StringUtils.isNotEmpty(
                getPropertyValueAsString(IAct6Constant.EXP_NODE_PROP_CANDIDATE_USERS,elementNode))) {
            //提取id列表加入
            task.getCandidateUsers().addAll(RegExUtils.getValue(getPropertyValueAsString(IAct6Constant.EXP_NODE_PROP_CANDIDATE_USERS,elementNode),"\"id\":\"(.+?)\""));
        }
        //提取候选角色，候选机构
        if (StringUtils.isNotEmpty(
                getPropertyValueAsString(IAct6Constant.EXP_NODE_PROP_CANDIDATE_ROLES,elementNode))) {
            String expression = getPropertyValueAsString(IAct6Constant.EXP_NODE_PROP_CANDIDATE_ROLES,elementNode);
            task.getCandidateGroups().addAll(RegExUtils.getValue(expression,"\"id\":\"(.+?)\""));
        }
        if (StringUtils.isNotEmpty(
                getPropertyValueAsString(IAct6Constant.EXP_NODE_PROP_CANDIDATE_ORGANIZATIONS,elementNode))) {
            String expression = getPropertyValueAsString(IAct6Constant.EXP_NODE_PROP_CANDIDATE_ORGANIZATIONS,elementNode);
            task.getCandidateGroups().addAll(RegExUtils.getValue(expression,"\"id\":\"(.+?)\""));
        }
        // 多实例类型 注意看
        String multiInstanceType = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_TYPE, elementNode);
        // 通过权重
        String multiInstanceCondition = getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_CONDITION, elementNode);
        if (StringUtils.isNotEmpty(multiInstanceType) && !IAct6Constant.PROPERTY_MULTIINSTANCE_NONE.equalsIgnoreCase(multiInstanceType)) {
            MultiInstanceLoopCharacteristics multiInstanceObject = new MultiInstanceLoopCharacteristics();
            if (IAct6Constant.PROPERTY_MULTIINSTANCE_SEQUENTIAL.equalsIgnoreCase(multiInstanceType)) {
                multiInstanceObject.setSequential(true);
            } else {
                multiInstanceObject.setSequential(false);
            }

            if (StringUtils.isNotEmpty(multiInstanceCondition)) {
                //转换为数字,转换失败时，默认为100%
                double d = 100;
                try {
                    d = Double.valueOf(multiInstanceCondition);
                } catch (Exception e) {
                    d = 100;
                }
                String exp = "${nrOfCompletedInstances/nrOfInstances >= " + d / 100 + "}";
                multiInstanceObject.setCompletionCondition(exp);
            } else {
                String exp = "${nrOfCompletedInstances/nrOfInstances >= 1.0}";
                multiInstanceObject.setCompletionCondition(exp);
            }
            //基数
            if (StringUtils.isNotEmpty(getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_CARDINALITY, elementNode))) {
                String value = getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_CARDINALITY, elementNode);
                multiInstanceObject.setLoopCardinality(value);
            }
            //多实例采集变量
            if (StringUtils.isNotEmpty(getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION, elementNode))) {
                String value = getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_COLLECTION, elementNode);
                multiInstanceObject.setInputDataItem(value);
            }
            //元素的变量(多实例)
            if (StringUtils.isNotEmpty(getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_VARIABLE, elementNode))) {
                String value = getPropertyValueAsString(IAct6Constant.PROPERTY_MULTIINSTANCE_VARIABLE, elementNode);
                multiInstanceObject.setElementVariable(value);
            }

            task.setLoopCharacteristics(multiInstanceObject);


        }
        // 添加默认流程属性
        if (StringUtils.isNotEmpty(getPropertyValueAsString("default", elementNode))) {
            task.setDefaultFlow(getPropertyValueAsString("default", elementNode));
        }

        convertJsonToFormProperties(elementNode, task);
        return task;
    }
    @Override
    protected String getStencilId(BaseElement baseElement) {
        return STENCIL_TASK_USER;
    }
}
