package com.activiti;

/**
 * 常用系统常量
 */
public interface IAct6Constant {
    /**
     * vue 工作流界面设计器前缀
     */
    public static  final String VUE_ACTIVITI_PREFIX="activiti:";
    /**
     * UTF-8字符集
     */
    public static  final String ENCODE_UTF_8="UTF-8";
    //api url接口类型
    /**
     * 用户接口
     */
    public static final String API_TYPE_USER="user";
    /**
     * 角色接口
     */
    public static final String API_TYPE_ROLE="role";
    /**
     * 机构接口
     */
    public static final String API_TYPE_ORGANIZATION="organization";
    //HTTP 方法
    /**
     * POST
     */
    public static final String HTTP_METHOD_POST="post";
    /**
     * GET
     */
    public static final String HTTP_METHOD_GET="get";
// AJAX处理结果
    // 处理结果标志
    /**
     * 成功
     */
    public static final String AJAX_RESULT_SUCCESS = "1";
    /**
     * 失败
     */
    public static final String AJAX_RESULT_FAIL = "0";
    /**
     * 处理消息
     */
    public static final String AJAX_MESSAGE = "message";
    /**
     * 结果状态key
     */
    public static final String AJAX_STATUS = "status";
    /**
     * 结果列表key
     */
    public static final String AJAX_LIST = "list";
    /**
     * 结果bean key
     */
    public static final String AJAX_BEAN = "bean";
    /**
     * 总记录数
     */
    public static final String AJAX_TOTAL_RECORD="totalResult";
    //节点扩展属性
    /**
     * 展示表单
     */
    public static final String EXP_NODE_PROP_FORMKEYDEFINITION="formkeydefinition";
    /**
     * 提交服务
     */
    public static final String EXP_NODE_PROP_COMMITSERVICE="commitService";
    /**
     * 主办用户（代理人）id
     */
    public static final String EXP_NODE_PROP_ASSIGNEE_ID="assigneeId";
    /**
     * 候选用户
     */
    public static final String EXP_NODE_PROP_CANDIDATE_USERS="candidateUsers1";
    /**
     * 候选角色
     */
    public static final String EXP_NODE_PROP_CANDIDATE_ROLES="candidateRoles";
    /**
     * 候选机构
     */
    public static final String EXP_NODE_PROP_CANDIDATE_ORGANIZATIONS="candidateOrganizations";
    //多实例相关参数名
    /**
     * 多实例类型
     */
    public static final String PROPERTY_MULTIINSTANCE_TYPE = "multiinstance_type";
    /**
     * 非多实例
     */
    public static final String PROPERTY_MULTIINSTANCE_NONE="None";
    /**
     * 同时进行
     */
    public static final String PROPERTY_MULTIINSTANCE_PARALLEL="Parallel";
    /**
     * 顺序执行
     */
    public static final String PROPERTY_MULTIINSTANCE_SEQUENTIAL="Sequential";
    /**
     * 基数(多实例)
     */
    public static final String PROPERTY_MULTIINSTANCE_CARDINALITY = "multiinstance_cardinality";
    /**
     * 多实例采集变量
     */
    public static final String PROPERTY_MULTIINSTANCE_COLLECTION = "multiinstance_collection";
    /**
     * 元素的变量(多实例)
     */
    public static final String PROPERTY_MULTIINSTANCE_VARIABLE = "multiinstance_variable";
    /**
     * 多实例通过条件,对应前端通过权重
     */
    public static final  String PROPERTY_MULTIINSTANCE_CONDITION = "multiinstance_condition1";
    /**
     * 连接线条件
     */
    public static final String PROPERTY_CONDITIONSEQUENCEFLOW="conditionsequenceflow";
    /**
     * 缺省的bpmnXml
     */
    //debug 2020-10-6
    public static  final String DEFAULT_BPMNXML="<?xml version=\"1.0\" encoding=\"UTF-8\"?>+\n" +
            "<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:activiti=\"http://activiti.org/bpmn\" id=\"sample-diagram\" targetNamespace=\"http://activiti.org/bpmn\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">+\n" +
            "  <bpmn2:process id=\"Process_1\" isExecutable=\"true\"  process_id=\"Process_1\">+\n" +
            "    <bpmn2:startEvent id=\"StartEvent_0w9qcue\" name=\"交易开始\">+\n" +
            "      <bpmn2:outgoing>SequenceFlow_0zyxqaf</bpmn2:outgoing>+\n" +
            "    </bpmn2:startEvent>+\n" +
            "  </bpmn2:process>+\n" +
            "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">+\n" +
            "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_1\">+\n" +
            "      <bpmndi:BPMNShape id=\"StartEvent_0w9qcue_di\" bpmnElement=\"StartEvent_0w9qcue\">+\n" +
            "        <dc:Bounds x=\"292\" y=\"62\" width=\"36\" height=\"36\" />+\n" +
            "        <bpmndi:BPMNLabel>+\n" +
            "          <dc:Bounds x=\"288\" y=\"105\" width=\"45\" height=\"14\" />+\n" +
            "        </bpmndi:BPMNLabel>+\n" +
            "      </bpmndi:BPMNShape>+\n" +
            "    </bpmndi:BPMNPlane>+\n" +
            "  </bpmndi:BPMNDiagram>+\n" +
            "</bpmn2:definitions>";
    //工作流相关参数
    /**
     * processDefinitionId 表示：预定义流程定义id
     */
    public static final String PREDEFINED_PROCESS_DEFINITION_ID = "processDefinitionId";
    /**
     * processInstanceId 表示：预定义流程实例id
     */
    public static final String PREDEFINED_PROCESS_INSTANCE_ID = "processInstanceId";
    /**
     * businessKey  表示：业务主键
     */
    public static final String PREDEFINED_BUSINESS_KEY="businessKey";
    /**
     * 预定义任务id
     */
    public static final String PREDEFINED_TASK_ID = "taskId";
    /**
     * 应用发起人id
     */
    public static final String INITIATOR="_initiator_";
    /**
     * Vue 设计发起人id
     */
    public static final String DESIGN_INITIATOR="initator";
    /**
     * isPass 表示：审核状态标识
     */
    public static final String PREDEFINED_IS_PASS = "isPass";
    /**
     * 审核通过的中文显示:审核通过
     */
    public static final String PREDEFINED_IS_PASS_YES_CHINESE = "审核通过";
    /**
     * 审核不通过的中文显示：审核不通过
     */
    public static final String PREDEFINED_IS_PASS_NOT_CHINESE = "审核不通过";
    /**
     * 审核意见标识
     */
    public static final String PREDEFINED_AUDIT_OPINION = "auditOpinion";
    /**
     * 否
     */
    public static final String DICTIONARY_RM_YES_NOT_0 = "0";
    /**
     * 是
     */
    public static final String DICTIONARY_RM_YES_NOT_1 = "1";
    //审核相关常量
    /**
     * 提交任务用户id
     */
    public static final String
            PREDEFINED_COMMIT_USER_ID="userId";
    /**
     * 提交任务用户名
     */
    public static final String PREDEFINED_COMMIT_USER_NAME="userName";
    /**
     * 审核状态
     */
    public static final String PREDEFINED_AUDIT_STATE="isPass";
    /**
     * 审核时间
     */
    public static final String PREDEFINED_AUDIT_TIME="auditTime";
    /**
     * 空串标志
     */
    public static  final String NULL_STR="null";
    //标准bpmn xml 流程固有属性
    /**
     * id
     */
    public static final String STANTD_BPMN_XML_ID="id";
    /**
     * name
     */
    public static final String STANTD_BPMN_XML_NAME="name";
    /**
     * isExecutable
     */
    public static final String STANTD_BPMN_XML_ISEXECUTABLE="isExecutable";
    //请求参数格式
    /**
     * 请求参数集合
     */
    public static final String REQUEST_PARAMETER_FORMAT_PARAMETER="parameter";
    /**
     * Body Data
     */
    public static final String REQUEST_PARAMETER_FORMAT_BODY_DATA="bodyData";
    //API相关常量
    /**
     * 提交调用API相关参数
     */
    public static  final String  REF_API_REG_EXP="\\$refApi\\{(.+?)\\}";
    /**
     * Content Type
     */
    public static final String CONTENT_TYPE="Content-Type";
    //应答测试字段
    /**
     * 响应文本
     */
    public static final  String RESPOND_TEXT="respondText";
    /**
     * 响应头
     */
    public static final String RESPOND_HEADER="respondHeader";
    /**
     * 空的JSON 数组集合
     */
    public static final String EMPTY_JSON_ARRAY = "[]";
    /**
     * 接口中属性表格中的name
     */
    public static final String API_TABLE_PROPERTY_NAME="name";
    /**
     * 接口中属性表格中的value
     */
    public static final String API_TABLE_PROPERTY_VALUE="value";
    /**
     * 分支条件表达式名称
     */
    public static final String CONDITION_EXPRESSION="conditionExpression";
    public static final String TFORMAL_EXPRESSION="tFormalExpression";
}
