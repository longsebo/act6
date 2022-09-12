package com.activiti.runconverter;

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
 * 开始事件节点xml转换
 */
public class ExtBpmnStartEventXMLConverter extends BaseBpmnXMLConverter {

    /**
     * default attributes taken from bpmn spec and from activiti extension
     */
    public static final List<ExtensionAttribute> defaultStartEventAttributes = Arrays
            .asList(new ExtensionAttribute(null, ATTRIBUTE_FORM_FORMKEY),
                    new ExtensionAttribute(null, ATTRIBUTE_EVENT_START_INITIATOR),
                    new ExtensionAttribute(null, ATTRIBUTE_EVENT_START_INTERRUPTING),
                    new ExtensionAttribute(null, ELEMENT_FORMPROPERTY));
    protected Map<String, BaseChildElementParser> childParserMap = new HashMap<String, BaseChildElementParser>();
    public   List<ExtensionAttribute> allDefaultAttributes;
    public ExtBpmnStartEventXMLConverter() {
//        HumanPerformerParser humanPerformerParser = new HumanPerformerParser();
//        childParserMap.put(humanPerformerParser.getElementName(),
//                humanPerformerParser);
//        PotentialOwnerParser potentialOwnerParser = new PotentialOwnerParser();
//        childParserMap.put(potentialOwnerParser.getElementName(),
//                potentialOwnerParser);
//        CustomIdentityLinkParser customIdentityLinkParser = new CustomIdentityLinkParser();
//        childParserMap.put(customIdentityLinkParser.getElementName(),
//                customIdentityLinkParser);

        allDefaultAttributes= new ArrayList<>();
        allDefaultAttributes.addAll(defaultStartEventAttributes);
        allDefaultAttributes.addAll(defaultElementAttributes);
        allDefaultAttributes.addAll(defaultActivityAttributes);
    }
    @Override
    public Class<? extends BaseElement> getBpmnElementType() {
        return StartEvent.class;
    }
    
    @Override
    protected String getXMLElementName() {
        return ELEMENT_EVENT_START;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected BaseElement convertXMLToElement(XMLStreamReader xtr,
                                              BpmnModel model) throws Exception {

        StartEvent startEvent = new StartEvent();

        BpmnXMLUtil.addXMLLocation(startEvent, xtr);
        //加入默认属性
        String formKey = xtr.getAttributeValue(null, ATTRIBUTE_FORM_FORMKEY);
        startEvent.setFormKey(formKey);

        String startInitor = xtr.getAttributeValue(null, IAct6Constant.DESIGN_INITIATOR);
        if(StringUtils.isEmpty(startInitor)){
            startInitor = xtr.getAttributeValue(null,ATTRIBUTE_EVENT_START_INITIATOR);
        }
        startEvent.setInitiator(startInitor);

        String interrupting = xtr.getAttributeValue(null, ATTRIBUTE_EVENT_START_INTERRUPTING);
        startEvent.setInterrupting(Boolean.valueOf(interrupting));
        //TODO:formProperty
//        String formProperty = xtr.getAttributeValue(null, ELEMENT_FORMPROPERTY);
//        startEvent.setFormProperties();
        // 全部的属性都在这里
        BpmnXMLUtil.addCustomAttributes(xtr, startEvent, new List[]{defaultElementAttributes, defaultActivityAttributes, defaultStartEventAttributes});
//
//        parseChildElements(getXMLElementName(), startEvent, childParserMap, model,
//                xtr);
//        this.parseChildElements();
        //扩展属性添加activiti前缀,写入扩展节点
       parseChildElements(getXMLElementName(),startEvent,model,xtr);
       //循环扩展节点,放到属性里头
        Map<String, List<ExtensionElement>> extensionElements = startEvent.getExtensionElements();
        for(String key:extensionElements.keySet()){
            List<ExtensionElement> extensionElementList = extensionElements.get(key);
            if(!extensionElementList.isEmpty()){
                ExtensionAttribute extendAttribute = new ExtensionAttribute();
                extendAttribute.setName(key);
                extendAttribute.setValue(extensionElementList.get(0).getElementText());
                startEvent.addAttribute(extendAttribute);
            }
        }
        return startEvent;
    }

    @Override
    protected void writeQualifiedAttribute(String attributeName, String value, XMLStreamWriter xtw) throws Exception {
        super.writeQualifiedAttribute(attributeName, value, xtw);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void writeAdditionalAttributes(BaseElement element, BpmnModel model,
                                             XMLStreamWriter xtw) throws Exception {
        StartEvent startEvent = (StartEvent) element;
        writeQualifiedAttribute(ATTRIBUTE_FORM_FORMKEY,
                startEvent.getFormKey(), xtw);
        if(StringUtils.isNotEmpty(startEvent.getInitiator())) {
            writeQualifiedAttribute(ATTRIBUTE_EVENT_START_INITIATOR, startEvent.getInitiator(),
                    xtw);
        }else{
            String initator ;
            initator = startEvent.getAttributeValue(null,IAct6Constant.DESIGN_INITIATOR);
            writeQualifiedAttribute(ATTRIBUTE_EVENT_START_INITIATOR, initator,
                    xtw);
        }
        writeQualifiedAttribute(ATTRIBUTE_EVENT_START_INTERRUPTING,
                String.valueOf(startEvent.isInterrupting()), xtw);
        //扩展属性添加activiti前缀

        //List<ExtensionAttribute> prefixActivitiList =  addActivitiPrefixForCustomAttributes(startEvent.getAttributes().values(),allDefaultAttributes);
//        List<List<ExtensionAttribute>> wrapAttributeCollection = new ArrayList<>();
//        wrapAttributeCollection.add(prefixActivitiList);
        
        // write custom attributes
//        BpmnXMLUtil.writeCustomAttributes(wrapAttributeCollection, xtw,
//                defaultElementAttributes, defaultActivityAttributes,
//                defaultStartEventAttributes);

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
        StartEvent startEvent = (StartEvent) element;
        didWriteExtensionStartElement = writeFormProperties(startEvent,
                didWriteExtensionStartElement, xtw);
        didWriteExtensionStartElement = writeCustomIdentities(element,
                didWriteExtensionStartElement, xtw);

        //扩展属性添加activiti前缀,写入扩展节点
        List<ExtensionAttribute> prefixActivitiList =  addActivitiPrefixForCustomAttributes(startEvent.getAttributes().values(),allDefaultAttributes);
        if (!prefixActivitiList.isEmpty()) {
            for (ExtensionAttribute extensionAttribute : prefixActivitiList) {

                if (StringUtils.isEmpty(extensionAttribute.getName())) {
                    continue;
                }

                if (!didWriteExtensionStartElement) {
                    xtw.writeStartElement(ELEMENT_EXTENSIONS);
                    didWriteExtensionStartElement = true;
                }
                //去掉VUE_ACTIVITI_PREFIX,后写入。以防出现多层
                xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX,
                        extensionAttribute.getName().replaceAll(IAct6Constant.VUE_ACTIVITI_PREFIX,""), ACTIVITI_EXTENSIONS_NAMESPACE);

                xtw.writeCharacters(extensionAttribute.getValue());
                xtw.writeEndElement();
            }
        }
        return didWriteExtensionStartElement;
    }
    
    protected boolean writeCustomIdentities(BaseElement element,
                                            boolean didWriteExtensionStartElement, XMLStreamWriter xtw)
            throws Exception {
        StartEvent userTask = (StartEvent) element;
//        if (userTask.getCustomUserIdentityLinks().isEmpty()
//                && userTask.getCustomGroupIdentityLinks().isEmpty()) {
//            return didWriteExtensionStartElement;
//        }
        
        if (!didWriteExtensionStartElement) {
            xtw.writeStartElement(ELEMENT_EXTENSIONS);
            didWriteExtensionStartElement = true;
        }
        Set<String> identityLinkTypes = new HashSet<String>();
//        identityLinkTypes.addAll(userTask.getCustomUserIdentityLinks().keySet());
//        identityLinkTypes.addAll(userTask.getCustomGroupIdentityLinks().keySet());
//        for (String identityType : identityLinkTypes) {
//            writeCustomIdentities(userTask, identityType,
//                    userTask.getCustomUserIdentityLinks().get(identityType),
//                    userTask.getCustomGroupIdentityLinks().get(identityType), xtw);
//        }
        
        return didWriteExtensionStartElement;
    }
    
    protected void writeCustomIdentities(StartEvent userTask, String identityType,
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
