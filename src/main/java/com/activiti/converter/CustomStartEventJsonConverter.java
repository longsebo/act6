package com.activiti.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.language.json.converter.ActivityProcessor;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.BpmnJsonConverterUtil;
import org.activiti.editor.language.json.model.ModelInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *  自定义开始节点json转换
 */
public class CustomStartEventJsonConverter extends BaseBpmnJsonConverter {
    protected Map<String, String> formMap;
    protected Map<String, ModelInfo> formKeyMap;
    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }
    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put("StartNoneEvent", CustomStartEventJsonConverter.class);
        convertersToBpmnMap.put("StartTimerEvent", CustomStartEventJsonConverter.class);
        convertersToBpmnMap.put("StartErrorEvent", CustomStartEventJsonConverter.class);
        convertersToBpmnMap.put("StartMessageEvent", CustomStartEventJsonConverter.class);
        convertersToBpmnMap.put("StartSignalEvent", CustomStartEventJsonConverter.class);
    }

    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(StartEvent.class, CustomStartEventJsonConverter.class);
    }
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        StartEvent startEvent = (StartEvent)baseElement;
        if (StringUtils.isNotEmpty(startEvent.getInitiator())) {
            propertiesNode.put("initiator", startEvent.getInitiator());
        }

        //将扩展属性转换为json
        for(List<ExtensionAttribute>  attributeList:startEvent.getAttributes().values()){
            for(ExtensionAttribute attribute:attributeList){
                if(!isDefaultAttribute(attribute.getName())){
                    propertiesNode.put(attribute.getName(),attribute.getValue());
                    //debug
                    ExtensionElement orderElement = new ExtensionElement();
                    orderElement.setName(attribute.getName());
                    orderElement.setElementText(attribute.getValue());
                    startEvent.addExtensionElement(orderElement);
                }
            }

        }
        ArrayNode extensionElArrayNode = objectMapper.createArrayNode();
        for(List<ExtensionAttribute>  attributeList:startEvent.getAttributes().values()){
            for(ExtensionAttribute attribute:attributeList) {
                if (!isDefaultAttribute(attribute.getName())) {
                    ObjectNode candidateNode = objectMapper.createObjectNode();
                    candidateNode.put(attribute.getName(), attribute.getValue());
                    extensionElArrayNode.add(candidateNode);

                }
            }
        }
        propertiesNode.put("extensionElements", extensionElArrayNode);

        if (StringUtils.isNotEmpty(startEvent.getFormKey())) {
            if (this.formKeyMap != null && this.formKeyMap.containsKey(startEvent.getFormKey())) {
                ObjectNode formRefNode = this.objectMapper.createObjectNode();
                ModelInfo modelInfo = (ModelInfo)this.formKeyMap.get(startEvent.getFormKey());
                formRefNode.put("id", modelInfo.getId());
                formRefNode.put("name", modelInfo.getName());
                formRefNode.put("key", modelInfo.getKey());
                propertiesNode.set("formreference", formRefNode);
            }
        }

        this.addFormProperties(startEvent.getFormProperties(), propertiesNode);
        this.addEventProperties(startEvent, propertiesNode);
    }

    /**
     * 判断是否为开始节点默认属性
     * @param attributeName
     * @return
     */
    private boolean isDefaultAttribute(String  attributeName) {
        for(ExtensionAttribute defaultAttribute: CustomStartEventXMLConverter.defaultStartEventAttributes){
            if(defaultAttribute.getName().equalsIgnoreCase(attributeName)){
                return true;
            }
        }
        return false;
    }

    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        StartEvent startEvent = new StartEvent();
        startEvent.setInitiator(this.getPropertyValueAsString("initiator", elementNode));
        String stencilId = BpmnJsonConverterUtil.getStencilId(elementNode);
        //设置扩展属性
        setExtendProperties(elementNode,startEvent);
        if ("StartNoneEvent".equals(stencilId)) {
            String formKey = this.getPropertyValueAsString("formkeydefinition", elementNode);
            if (StringUtils.isNotEmpty(formKey)) {
                startEvent.setFormKey(formKey);
            } else {
                JsonNode formReferenceNode = this.getProperty("formreference", elementNode);
                if (formReferenceNode != null && formReferenceNode.get("id") != null && this.formMap != null && this.formMap.containsKey(formReferenceNode.get("id").asText())) {
                    startEvent.setFormKey((String)this.formMap.get(formReferenceNode.get("id").asText()));
                }
            }

            this.convertJsonToFormProperties(elementNode, startEvent);
        } else if ("StartTimerEvent".equals(stencilId)) {
            this.convertJsonToTimerDefinition(elementNode, startEvent);
        } else if ("StartErrorEvent".equals(stencilId)) {
            this.convertJsonToErrorDefinition(elementNode, startEvent);
        } else if ("StartMessageEvent".equals(stencilId)) {
            this.convertJsonToMessageDefinition(elementNode, startEvent);
        } else if ("StartSignalEvent".equals(stencilId)) {
            this.convertJsonToSignalDefinition(elementNode, startEvent);
        }

        return startEvent;
    }

    protected String getStencilId(BaseElement baseElement) {
        Event event = (Event)baseElement;
        if (event.getEventDefinitions().size() > 0) {
            EventDefinition eventDefinition = (EventDefinition)event.getEventDefinitions().get(0);
            if (eventDefinition instanceof TimerEventDefinition) {
                return "StartTimerEvent";
            }

            if (eventDefinition instanceof ErrorEventDefinition) {
                return "StartErrorEvent";
            }

            if (eventDefinition instanceof MessageEventDefinition) {
                return "StartMessageEvent";
            }

            if (eventDefinition instanceof SignalEventDefinition) {
                return "StartSignalEvent";
            }
        }

        return "StartNoneEvent";
    }


    /**
     * 设置开始节点扩展属性
     * @param modelNode
     * @param startEvent
     */
    private void setExtendProperties(JsonNode modelNode, StartEvent startEvent) {

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

                    startEvent.addAttribute(extensionElement1);
                }
            }
        }
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
//            flowElement.setDocumentation(getPropertyValueAsString(PROPERTY_DOCUMENTATION, elementNode));

            BpmnJsonConverterUtil.convertJsonToListeners(elementNode, flowElement);
            if (baseElement instanceof FlowElement) {
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
    }
}
