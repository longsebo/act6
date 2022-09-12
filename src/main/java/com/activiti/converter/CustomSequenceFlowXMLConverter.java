package com.activiti.converter;

import com.activiti.IAct6Constant;
import org.activiti.bpmn.converter.BaseBpmnXMLConverter;
import org.activiti.bpmn.converter.child.BaseChildElementParser;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.*;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

/**
 * 连线节点xml转换
 */
public class CustomSequenceFlowXMLConverter extends BaseBpmnXMLConverter {

    /**
     * default attributes taken from bpmn spec and from activiti extension
     */
    public static final List<ExtensionAttribute> defaultFlowAttributes = Arrays
            .asList(new ExtensionAttribute(null, ELEMENT_DOCUMENTATION),
                    new ExtensionAttribute(null, IAct6Constant.PROPERTY_CONDITIONSEQUENCEFLOW),
                    new ExtensionAttribute(null, "defaultflow"));
    protected Map<String, BaseChildElementParser> childParserMap = new HashMap<String, BaseChildElementParser>();
    public   List<ExtensionAttribute> allDefaultAttributes;
    public CustomSequenceFlowXMLConverter() {

        allDefaultAttributes= new ArrayList<>();
        allDefaultAttributes.addAll(defaultFlowAttributes);
        allDefaultAttributes.addAll(defaultElementAttributes);
        allDefaultAttributes.addAll(defaultActivityAttributes);
    }
    @Override
    public Class<? extends BaseElement> getBpmnElementType() {
        return SequenceFlow.class;
    }
    
    @Override
    protected String getXMLElementName() {
        return ELEMENT_SEQUENCE_FLOW;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected BaseElement convertXMLToElement(XMLStreamReader xtr,
                                              BpmnModel model) throws Exception {

        SequenceFlow sequenceFlow = new SequenceFlow();

        BpmnXMLUtil.addXMLLocation(sequenceFlow, xtr);
        //加入默认属性
        //名称
        String name = xtr.getAttributeValue(null, ATTRIBUTE_NAME);
        sequenceFlow.setName(name);
        //备注
        String documentation = xtr.getAttributeValue(null,ELEMENT_DOCUMENTATION);
        sequenceFlow.setDocumentation(documentation);

        //条件
        for(int i = 0; i < xtr.getAttributeCount(); ++i) {
            if(xtr.getAttributeLocalName(i).contains(IAct6Constant.PROPERTY_CONDITIONSEQUENCEFLOW)){
                String conditionExp = xtr.getAttributeValue(i);
                if(!IAct6Constant.NULL_STR.equalsIgnoreCase(conditionExp)) {
                    sequenceFlow.setConditionExpression(conditionExp);
                }
                break;
            }
        }

        //源点
        String sourceRef = xtr.getAttributeValue(null,ELEMENT_SOURCE_REF);
        sequenceFlow.setSourceRef(sourceRef);
        //目标点
        String taregtRef = xtr.getAttributeValue(null,ELEMENT_TARGET_REF);
        sequenceFlow.setTargetRef(taregtRef);
        // 全部的属性都在这里
        BpmnXMLUtil.addCustomAttributes(xtr, sequenceFlow, new List[]{defaultElementAttributes, defaultActivityAttributes, defaultFlowAttributes});

        parseChildElements(getXMLElementName(), sequenceFlow, childParserMap, model,
                xtr);
        
        return sequenceFlow;
    }

    @Override
    protected void writeQualifiedAttribute(String attributeName, String value, XMLStreamWriter xtw) throws Exception {
        super.writeQualifiedAttribute(attributeName, value, xtw);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void writeAdditionalAttributes(BaseElement element, BpmnModel model,
                                             XMLStreamWriter xtw) throws Exception {
        SequenceFlow sequenceFlow = (SequenceFlow) element;
        writeQualifiedAttribute(ATTRIBUTE_NAME,
                sequenceFlow.getName(), xtw);
        writeQualifiedAttribute(ELEMENT_DOCUMENTATION, sequenceFlow.getDocumentation(),
                xtw);
        writeQualifiedAttribute(IAct6Constant.PROPERTY_CONDITIONSEQUENCEFLOW,
                String.valueOf(sequenceFlow.getConditionExpression()), xtw);
        //写入来源节点及目标节点
        writeDefaultAttribute(ELEMENT_SOURCE_REF, sequenceFlow.getSourceRef(),
                xtw);
        writeDefaultAttribute(ELEMENT_TARGET_REF, sequenceFlow.getTargetRef(),
                xtw);
        //扩展属性添加activiti前缀

        List<ExtensionAttribute> prefixActivitiList =  addActivitiPrefixForCustomAttributes(sequenceFlow.getAttributes().values(),allDefaultAttributes);
        List<List<ExtensionAttribute>> wrapAttributeCollection = new ArrayList<>();
        wrapAttributeCollection.add(prefixActivitiList);
        // write custom attributes
//        BpmnXMLUtil.writeCustomAttributes(wrapAttributeCollection, xtw,
//                defaultElementAttributes, defaultActivityAttributes,
//                defaultFlowAttributes);
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
        SequenceFlow sequenceFlow = (SequenceFlow) element;
        didWriteExtensionStartElement = writeFormProperties(sequenceFlow,
                didWriteExtensionStartElement, xtw);
       // didWriteExtensionStartElement = writeCustomIdentities(element,
         //       didWriteExtensionStartElement, xtw);
//        if (!sequenceFlow.getCustomProperties().isEmpty()) {
//            for (CustomProperty customProperty : sequenceFlow.getCustomProperties()) {
//
//                if (StringUtils.isEmpty(customProperty.getSimpleValue())) {
//                    continue;
//                }
//
//                if (!didWriteExtensionStartElement) {
//                    xtw.writeStartElement(ELEMENT_EXTENSIONS);
//                    didWriteExtensionStartElement = true;
//                }
//                xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX,
//                        customProperty.getName(), ACTIVITI_EXTENSIONS_NAMESPACE);
//                xtw.writeCharacters(customProperty.getSimpleValue());
//                xtw.writeEndElement();
//            }
//        }conditionExpression
        //写入条件节点
//        xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX,
//                        customProperty.getName(), ACTIVITI_EXTENSIONS_NAMESPACE);
        if(StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
            xtw.writeStartElement(IAct6Constant.CONDITION_EXPRESSION);
            this.writeDefaultAttribute("xsi:type", IAct6Constant.TFORMAL_EXPRESSION, xtw);
            //String exp = "<![CDATA[%s]]>";

            System.out.println(sequenceFlow.getConditionExpression());
            xtw.writeCharacters(sequenceFlow.getConditionExpression());
            xtw.writeEndElement();
        }
        return didWriteExtensionStartElement;
    }
    
    protected boolean writeCustomIdentities(BaseElement element,
                                            boolean didWriteExtensionStartElement, XMLStreamWriter xtw)
            throws Exception {
        SequenceFlow sequenceFlow = (SequenceFlow) element;
//        if (sequenceFlow.getCustomUserIdentityLinks().isEmpty()
//                && sequenceFlow.getCustomGroupIdentityLinks().isEmpty()) {
//            return didWriteExtensionStartElement;
//        }
        
        if (!didWriteExtensionStartElement) {
            xtw.writeStartElement(ELEMENT_EXTENSIONS);
            didWriteExtensionStartElement = true;
        }
        Set<String> identityLinkTypes = new HashSet<String>();
//        identityLinkTypes.addAll(sequenceFlow.getCustomUserIdentityLinks().keySet());
//        identityLinkTypes.addAll(sequenceFlow.getCustomGroupIdentityLinks().keySet());
//        for (String identityType : identityLinkTypes) {
//            writeCustomIdentities(sequenceFlow, identityType,
//                    sequenceFlow.getCustomUserIdentityLinks().get(identityType),
//                    sequenceFlow.getCustomGroupIdentityLinks().get(identityType), xtw);
//        }
        
        return didWriteExtensionStartElement;
    }
    
    protected void writeCustomIdentities(SequenceFlow sequenceFlow, String identityType,
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
    

    

    

}
