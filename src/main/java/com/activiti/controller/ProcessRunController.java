package com.activiti.controller;

import com.activiti.IAct6Constant;
import com.activiti.converter.*;
import com.activiti.model.ActExtAPIVo;
import com.activiti.model.AuditInfoVo;
import com.activiti.model.ExtTask;
import com.activiti.model.ProcessDefinitionInfo;
import com.activiti.service.ActExtAPIService;
import com.activiti.service.ActivitiService;
import com.activiti.utils.BpmnConverterUtil;
import com.activiti.utils.DateUtil;
import com.activiti.utils.HttpClientUtil;
import com.activiti.utils.RegExUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.*;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.validator.internal.util.StringHelper;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 工作流运行包装控制器
 * 负责对外暴露服务，推动流程流转
 */
@Slf4j
@RestController
@RequestMapping("/processrun")
public class ProcessRunController {
    @Autowired
    ActivitiService activitiService;
    @Autowired
    IdentityService identityService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ActExtAPIService actExtAPIService;
    /**
     * 展示任务对应页面  普通提交，传任务id  启动时，应传流程定义id
     *
     * @throws Exception
     */
    @RequestMapping(value = "showTaskView/{processDefinitionId}/{taskId}")
    public void showTaskView(@PathVariable String processDefinitionId, @PathVariable String taskId, HttpServletRequest request, @RequestParam Map<String, String> values, HttpServletResponse response) throws Exception {
        log.info("showTaskView parameter processInstanceId:" + (processDefinitionId == null ? "" : processDefinitionId) + ",taskId:" + (taskId == null ? "" : taskId) + ",接收到动态参数:" + (values == null ? "" : values));
        String url;
        try {
            //如果任务id为空，且流程定义id为空，则抛异常
            if (StringUtils.isEmpty(processDefinitionId) && StringUtils.isEmpty(taskId)) {
                //log.info("任务id为空，且流程定义id为空");a
                throw new Exception("任务id为空，且流程定义id为空");
            }
            //如果任务id为空，则表示尚未启动流程，应展示开始节点界面
            if (StringUtils.isEmpty(taskId) || taskId.equals("*")) {
                //获取开始节点
                StartEvent startTask = getStartEvent(processDefinitionId);

                if (startTask == null) {
                    String msg = "流程定义id：" + processDefinitionId + "对应流程尚未定义开始节点!";
                    //log.info(msg);
                    throw new Exception(msg);
                }
                //获取展示表单url
                url = startTask.getAttributeValue(null, IAct6Constant.EXP_NODE_PROP_FORMKEYDEFINITION);
                if (StringUtils.isEmpty(url)) {
                    String msg = "流程定义id：" + processDefinitionId + "的开始节点尚未配置展示服务url!";
                    //log.info(msg);
                    throw new Exception(msg);
                }
                log.info("流程定义id：" + processDefinitionId + "的开始节点展示服务url:" + url);
            } else {
                //获取其他节点
                FlowElement task = getUserTask(processDefinitionId, taskId);
                if (task == null) {
                    throw new Exception("流程定义id：" + processDefinitionId + "的节点id:" + taskId + "不存在!");
                }

                //获取展示表单url
                url = task.getAttributeValue(null, IAct6Constant.EXP_NODE_PROP_FORMKEYDEFINITION);
                if (StringUtils.isEmpty(url)) {
                    String msg = "流程定义id：" + processDefinitionId + "的节点id:" + taskId + "尚未配置展示服务url!";
                    //log.info(msg);
                    throw new Exception(msg);
                }
                String busKey = getBusKey(taskId);
                //带回业务主键
                if (StringUtils.isNotEmpty(busKey)) {
                    url = formatUrl(busKey, url, IAct6Constant.PREDEFINED_BUSINESS_KEY);
                }
                log.info("流程定义id：" + processDefinitionId + "的节点id:" + taskId + "展示服务url:" + url);
            }
            //执行重定向
            //参数格式化${paramName}被替换为values对应值,url不存在的值，则拼接到url后面
            if (values != null) {
                Set<String> keys = values.keySet();
                for (String key : keys) {
                    url = formatUrl(values.get(key), url, key);
                }
            }
            //带回流程定义id和任务id
            if (StringUtils.isNotEmpty(processDefinitionId)) {
                url = formatUrl(processDefinitionId, url, IAct6Constant.PREDEFINED_PROCESS_DEFINITION_ID);
            }
            if (StringUtils.isNotEmpty(taskId)) {
                url = formatUrl(taskId, url, IAct6Constant.PREDEFINED_TASK_ID);
            }
            log.info("格式化后 url:" + url);
            response.sendRedirect(url);
        } catch (Exception e) {
            response.setCharacterEncoding(IAct6Constant.ENCODE_UTF_8);
            log.error("showTaskView", e);
            response.getOutputStream().print(e.getMessage());
        }
    }

    /**
     * 根据任务id获取业务主键
     * @param taskId
     * @return
     */
    private String getBusKey(String taskId) throws Exception {
        Task task=taskService.createTaskQuery() // 创建任务查询
                .taskId(taskId) // 根据任务id查询
                .singleResult();
        if(task==null){
            String msg="任务:"+taskId+"对应任务不存在!";
            log.error(msg);
            throw new Exception(msg);
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        if(processInstance!=null){
            return processInstance.getBusinessKey();
        }else{
            String msg = "流程实例id："+task.getProcessInstanceId()+"对应流程实例不存在!";
            log.error(msg);
            throw new Exception(msg);
        }
    }

    /**
     * 获取开始节点
     *
     * @param processDefinitionId
     * @return
     * @throws Exception
     */
    private StartEvent getStartEvent(String processDefinitionId) throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        Deployment deploymentDefine = repositoryService.createDeploymentQuery().deploymentId(processDefinition.getDeploymentId()).singleResult();
        Model model = repositoryService.createModelQuery().deploymentId(deploymentDefine.getId()).singleResult();
        byte[] editorSourceVal = repositoryService.getModelEditorSource(model.getId());
        String tempEditorSourceVal = new String(editorSourceVal);
        log.info("editorSourceVal :" + tempEditorSourceVal);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(tempEditorSourceVal);
        } catch (IOException e) {
//                    e.printStackTrace();
            log.error("转换json串为json对象失败", e);
            throw e;
        }
        CustomBpmnJsonConverter customBpmnJsonConverter = new CustomBpmnJsonConverter();
        BpmnModel bpmnModel = customBpmnJsonConverter.convertToBpmnModel(jsonNode);
        Process mainProcess = bpmnModel.getMainProcess();
        StartEvent startTask = null;
        if (mainProcess != null) {
            for (FlowElement element : mainProcess.getFlowElements()) {
                if (element instanceof StartEvent) {
                    startTask = (StartEvent) element;
                    break;
                }
            }
        } else {
            String msg = "流程定义id：" + processDefinitionId + "没有主流程对象!";
            //log.info(msg);
            throw new Exception(msg);
        }
        return startTask;
    }

    /**
     * 根据流程id及节点id获取用户任务节点
     *
     * @param processDefinitionId
     * @param taskId
     * @return
     * @throws Exception
     */
    private FlowElement getUserTask(String processDefinitionId, String taskId) throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                processDefinition.getResourceName());
        byte[] editorSourceVal = null;
        editorSourceVal = new byte[resourceAsStream.available()];

        resourceAsStream.read(editorSourceVal);
        String tempEditorSourceVal = new String(editorSourceVal);
        log.info("editorSourceVal :"+ tempEditorSourceVal);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
//            BpmnConverterUtil.converterJsonToBpmnXml()
            jsonNode =  BpmnConverterUtil
                    .converterBpmnXmlToJson(tempEditorSourceVal);
        } catch (Exception e) {
//                    e.printStackTrace();
            log.error("xml转换为json对象失败", e);
            throw e;
        }
        CustomBpmnJsonConverter customBpmnJsonConverter = new CustomBpmnJsonConverter();
        BpmnModel bpmnModel = customBpmnJsonConverter.convertToBpmnModel(jsonNode);
        Process mainProcess = bpmnModel.getMainProcess();
//        UserTask userTask = null;
        Task task=taskService.createTaskQuery() // 创建任务查询
                .taskId(taskId) // 根据任务id查询
                .singleResult();
        if(task==null){
            String msg = "任务id：" + taskId + "没有找到!";
            log.error(msg);
            throw new Exception(msg);
        }

//        System.out.println("task taskDefinitionKey:"+task.getTaskDefinitionKey());
        if (mainProcess != null) {
            for (FlowElement element : mainProcess.getFlowElements()) {
                if (element instanceof UserTask && StringUtils.isNotEmpty(element.getId()) &&
                        element.getId().equals(task.getTaskDefinitionKey())) {
                    //userTask = (UserTask) element;
                    //break;
                    return element;
                }
            }
            String msg = "流程定义id：" + processDefinitionId + "没有找到任务id:"+taskId;
            //log.info(msg);
            throw new Exception(msg);
        } else {
            String msg = "流程定义id：" + processDefinitionId + "没有主流程对象!";
            //log.info(msg);
            throw new Exception(msg);
        }
//        return userTask;
    }

    /**
     * 替换url中变量名
     *
     * @param value
     * @param url
     * @param key
     * @return
     */
    private String replaceUrlVarName(String value, String url, String key) {
        if (url.contains("${" + key + "}")) {
            url = url.replaceAll("\\$\\{" + key + "\\}", value);
        }
        return url;

    }

    /**
     * 格式url
     *
     * @param value
     * @param url
     * @param key
     * @return
     */
    private String formatUrl(String value, String url, String key) {
        if (url.contains("${" + key + "}")) {
            url = url.replaceAll("\\$\\{" + key + "\\}", value);
        } else {
            if (!url.contains(key + "=")) {
                if (url.contains("?")) {
                    if (url.endsWith("&")) {
                        url = url + key + "=" + value;
                    } else {
                        url = url + "&" + key + "=" + value;
                    }
                } else {
                    url = url + "?" + key + "=" + value;
                }
            }
        }
        return url;
    }

    /**
     * 提交任务 如果是启动流程，只传流程定义id；提交任务，只传任务id
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "commitTask/{processDefinitionId}/{taskId}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> commitTask(@PathVariable String processDefinitionId,@PathVariable String taskId,  @RequestBody Map<String, Object> values) throws Exception {
        log.info("completeTask parameter processInstanceId:"+(processDefinitionId==null?"":processDefinitionId)+",taskId:"+(taskId==null?"":taskId)+",接收到动态参数:"+(values==null?"":values));
        String url;
        Map<String, Object> retMap = new HashMap<>();
        String processInstanceId = null;
        try {
            //如果任务id为空，且流程定义id为空，则抛异常
            if (StringUtils.isEmpty(processDefinitionId) && StringUtils.isEmpty(taskId)) {
                //log.info("任务id为空，且流程定义id为空");
                throw new Exception("任务id为空，且流程定义id为空");
            }

            //带回流程定义id和任务id,流程实例id
            if (StringUtils.isNotEmpty(processDefinitionId)) {
                values.put(IAct6Constant.PREDEFINED_PROCESS_DEFINITION_ID, processDefinitionId);
            }
            if (StringUtils.isNotEmpty(taskId)) {
                values.put(IAct6Constant.PREDEFINED_TASK_ID, taskId);
            }
            if (StringUtils.isNotEmpty(processInstanceId)) {
                values.put(IAct6Constant.PREDEFINED_PROCESS_INSTANCE_ID, processInstanceId);
            }
            //如果任务id为空或*，则表示尚未启动流程，应启动流程
            if (StringUtils.isEmpty(taskId) || "*".equals(taskId)) {
                //获取开始节点
                StartEvent startTask = getStartEvent(processDefinitionId);
                if (startTask == null) {
                    String msg = "流程定义id：" + processDefinitionId + "对应流程尚未定义开始节点!";
                    //log.info(msg);
                    throw new Exception(msg);
                }
                //获取主键服务名称
                String commitServiceName = startTask.getAttributeValue(null, IAct6Constant.EXP_NODE_PROP_COMMITSERVICE);
                if (StringUtils.isEmpty(commitServiceName)) {
                    String msg = "流程定义id：" + processDefinitionId + "的开始节点尚未配置提交服务!";
                    //log.info(msg);
                    throw new Exception(msg);
                }
                log.info("流程定义id：" + processDefinitionId + "的开始节点提交服务:" + commitServiceName);
                ActExtAPIVo apiVo = actExtAPIService.get(commitServiceName);
                if(apiVo==null)
                    throw new Exception("提交服务名称："+commitServiceName+"不存在!");
                if(apiVo.getIsUse().equalsIgnoreCase(IAct6Constant.DICTIONARY_RM_YES_NOT_0))
                    throw new Exception("服务接口:"+commitServiceName+"尚未启用!");
                url = apiVo.getCommitService();
                if(StringUtils.isEmpty(url))
                    throw new Exception("服务接口:"+commitServiceName+"尚未配置提交url");
                //参数格式化${paramName}被替换为values对应值
                if (values != null) {
                    Set<String> keys = values.keySet();
                    for (String key : keys) {
                        url = replaceUrlVarName((String) values.get(key), url, key);
                    }
                }
                //如果传了发起人，则指定发起人
                if (StringUtils.isNotEmpty((String) values.get(IAct6Constant.INITIATOR))) {
                    identityService.setAuthenticatedUserId((String) values.get(IAct6Constant.INITIATOR));
                    startTask.setInitiator((String) values.get(IAct6Constant.INITIATOR));
                }
                log.info("启动流程:" + processDefinitionId + "，参数为:" + ((values == null) ? "" : values));
                log.info("格式化后 url:" + url);
                log.info("提交到url:" + url + "，参数值 :" + ((values == null) ? "" : values));
                //执行提交服务,获取业务主键
                String busKeyReg=apiVo.getBusinesskeyRegExp();
                if(StringUtils.isEmpty(busKeyReg))
                    throw new Exception("服务接口:"+commitServiceName+"尚未配置提取业务主键正则表达式!");
                apiVo.setCommitService(url);
                String busKey = executeApi(apiVo, values);
                //以流程定义id，业务主键，参数方式启动流程实例
                ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId,busKey, values);
                processInstanceId = processInstance.getId();

                log.info("启动流程:" + processDefinitionId + "成功，参数为:" + ((values == null) ? "" : values) + ",流程实例id:" + processInstanceId);
            } else {
                //获取其他节点
                FlowElement task = getUserTask(processDefinitionId, taskId);
                if (task == null) {
                    throw new Exception("流程定义id：" + processDefinitionId + "的节点id:" + taskId + "不存在!");
                }

                //获取提交服务
                String commitServiceName = task.getAttributeValue(null, IAct6Constant.EXP_NODE_PROP_COMMITSERVICE);
                if (StringUtils.isEmpty(commitServiceName)) {
                    String msg = "流程定义id：" + processDefinitionId + "的节点id:" + taskId + "尚未配置提交服务!";
                    //log.info(msg);
                    throw new Exception(msg);
                }
                log.info("流程定义id：" + processDefinitionId + "的节点id:" + taskId + "提交服务:" + commitServiceName);
                ActExtAPIVo apiVo = actExtAPIService.get(commitServiceName);
                if(apiVo==null)
                    throw new Exception("提交服务名称："+commitServiceName+"不存在!");
                if(apiVo.getIsUse().equalsIgnoreCase(IAct6Constant.DICTIONARY_RM_YES_NOT_0))
                    throw new Exception("服务接口:"+commitServiceName+"尚未启用!");
                url = apiVo.getCommitService();
                if(StringUtils.isEmpty(url))
                    throw new Exception("服务接口:"+commitServiceName+"尚未配置提交url");
                //参数格式化${paramName}被替换为values对应值
                if (values != null) {
                    Set<String> keys = values.keySet();
                    for (String key : keys) {
                        url = replaceUrlVarName((String) values.get(key), url, key);
                    }
                }
                log.info("格式化后 url:" + url);
                log.info("提交到url:" + url + "，参数值 :" + ((values == null) ? "" : values));
                //执行提交服务
                apiVo.setCommitService(url);
                executeApi(apiVo, values);
                log.info("流程定义id：" + processDefinitionId + "的节点id:" + taskId + "提交服务url:" + url);
                completeTask(taskId, values);
                log.info("流程定义id：" + processDefinitionId + "的节点id:" + taskId + "提交任务成功!");

            }



            retMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
            retMap.put(IAct6Constant.AJAX_MESSAGE, "提交任务成功!");
            log.info("提交到url:" + url + "，参数值 :" + ((values == null) ? "" : values) + ",提交任务成功!");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("commitTask", e);
            retMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
            retMap.put(IAct6Constant.AJAX_MESSAGE, e.getMessage());
        }
        return retMap;
    }

    /**
     * 执行服务API接口
     * @param apiVo api定义
     * @param values 业务参数
     * @return
     */
    private String executeApi(ActExtAPIVo apiVo, Map<String, Object> values) throws Exception {
        //检查请求头，请求参数，请求body是否有依赖其他接口，如果有先调用
        if(StringUtils.isNotEmpty(apiVo.getRequestHeadSet())){
            //====请求头
            apiVo.setRequestHeadSet(checkAndCallOtherAPI(apiVo.getRequestHeadSet(), values));
        }
        if(StringUtils.isNotEmpty(apiVo.getRequestParameterSet())){
            //请求参数
            apiVo.setRequestParameterSet(checkAndCallOtherAPI(apiVo.getRequestParameterSet(),values));
        }
        if(StringUtils.isNotEmpty(apiVo.getBodyData())){
            //Body 暂时考虑JSON格式
            //替换body中${参数名}为真实值
            if (values != null) {
                Set<String> keys = values.keySet();
                for (String key : keys) {
                    apiVo.setBodyData(replaceUrlVarName((String) values.get(key), apiVo.getBodyData(), key));
                }
            }
            apiVo.setBodyData(checkAndCallOtherAPI(apiVo.getBodyData(),values));
        }
        //处理本接口调用
        return HttpClientUtil.executeApi(apiVo,values);
    }

    /**
     * 检查并调用其他API接口
     * @param checkValue
     * @param values
     * @throws Exception
     */
    private String checkAndCallOtherAPI(String checkValue, Map<String, Object> values) throws Exception {
        //转换为JSON对象
        //空串直接返回原值
        if(IAct6Constant.EMPTY_JSON_ARRAY.equals(checkValue)||StringUtils.isEmpty(checkValue)){
            return checkValue;
        }
//
        //如果以{开头，则尝试转成JSONOjbect,否则尝试转成JSONArray
        if(checkValue.startsWith("{")) {
            JSONObject jsonObject = JSON.parseObject(checkValue);
            recursiveCallByParameter(values, jsonObject);
            return jsonObject.toJSONString();
        }else if(checkValue.startsWith("[")){
            JSONArray jsonArray = JSON.parseArray(checkValue);
            for(int i=0;i<jsonArray.size();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recursiveCallByParameter(values, jsonObject);
            }
            return jsonArray.toJSONString();
        }else {
            //throw new Exception("无效的JSON 串:"+checkValue);
            return recursiveCallByParameterForNonJson(values,checkValue);
        }
    }

    /**
     * 递归调用，并修改参数（非Json格式)
     * @param values
     * @param checkValue
     * @return
     */
    private String recursiveCallByParameterForNonJson(Map<String, Object> values, String checkValue) throws Exception {
            //尝试提取API参数:名称，返回值提取正则表达式
            String operatorStr=checkValue;
            Pattern p = Pattern.compile(IAct6Constant.REF_API_REG_EXP,Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(operatorStr);
            while(m.find()) {
                String apiParameters =operatorStr.substring(m.start(1),m.end(1));
                if(StringUtils.isEmpty(apiParameters)){
                    m = p.matcher(operatorStr);
                    continue;
                }
                //如果包括逗号，则认为是API格式
                if (apiParameters.contains(",")) {
                    String apiParamerterArray[] = apiParameters.split(",");
                    if (apiParamerterArray.length == 2) {
                        //查找对应api
                        ActExtAPIVo vo1 = actExtAPIService.get(apiParamerterArray[0]);
                        if (vo1 == null)
                            throw new Exception("接口名称:" + apiParamerterArray[0] + "不存在!");
                        if (IAct6Constant.DICTIONARY_RM_YES_NOT_0.equals(vo1.getIsUse()))
                            throw new Exception("接口:" + apiParamerterArray[0] + "已停用!");
                        //递归调用接口
                        String retVal = executeApi(vo1, values);
                        //从返回值提取想要的值
                        String pickVal = RegExUtils.getValue(retVal, apiParamerterArray[1], 1);
                        //替换值
                        operatorStr = operatorStr.replace(IAct6Constant.REF_API_REG_EXP,pickVal);
                    }
                }

                m = p.matcher(operatorStr);
            }
            return operatorStr;

    }

    /**
     * 递归调用，并修改参数
     * @param values
     * @param jsonObject
     * @throws Exception
     */
    private void recursiveCallByParameter(Map<String, Object> values, JSONObject jsonObject) throws Exception {
        Set<String> keySet = jsonObject.keySet();
        for (String key : keySet) {
            String value;
            value = jsonObject.getString(key);
            //尝试提取API参数:名称，返回值提取正则表达式
            String apiParameters = RegExUtils.getValue(value, IAct6Constant.REF_API_REG_EXP, 1);
            //如果包括逗号，则认为是API格式
            if (apiParameters.contains(",")) {
                String apiParamerterArray[] = apiParameters.split(",");
                if (apiParamerterArray.length == 2) {
                    //查找对应api
                    ActExtAPIVo vo1 = actExtAPIService.get(apiParamerterArray[0]);
                    if (vo1 == null)
                        throw new Exception("接口名称:" + apiParamerterArray[0] + "不存在!");
                    if (IAct6Constant.DICTIONARY_RM_YES_NOT_0.equals(vo1.getIsUse()))
                        throw new Exception("接口:" + apiParamerterArray[0] + "已停用!");
                    //递归调用接口
                    String retVal = executeApi(vo1, values);
                    //从返回值提取想要的值
                    String pickVal = RegExUtils.getValue(retVal, apiParamerterArray[1], 1);
                    //替换值
                    jsonObject.put(key, pickVal);
                }
            }
//            }
    }
    }

    /**
     * 提取业务主键
     * @param busResult  业务处理结果
     * @param busKeyReg 提取业务主键正则表达式
     * @return
     */
    private String parseBusKey(String busResult, String busKeyReg) {
        return RegExUtils.getValue(busResult,busKeyReg,1);
    }

    /**
     * 提交任务，推动任务往下走
     *
     * @param taskId
     * @param values
     */
    private void completeTask(String taskId, Map<String, Object> values) {
        //判断是否是带有审核意见的
        //审核意见
        String auditOpinion = (String) values.get(IAct6Constant.PREDEFINED_AUDIT_OPINION);
        Task task = null;
        if (!StringUtils.isEmpty(auditOpinion)) {
            TaskQuery taskQuery = taskService.createTaskQuery();

            task = (Task) taskQuery.taskId(taskId).singleResult();
            if (task != null) {
                taskService.addComment(taskId, task.getProcessInstanceId(), auditOpinion);
            }
        }
        String isPass = (String) values.get(IAct6Constant.PREDEFINED_IS_PASS);
        Map<String, Object> temp = new HashMap<String, Object>();
        if (!StringUtils.isEmpty(isPass)) {
            String auditState = "";
            if (IAct6Constant.DICTIONARY_RM_YES_NOT_1.equals(isPass)) {
                auditState = IAct6Constant.PREDEFINED_IS_PASS_YES_CHINESE;
            } else {
                auditState = IAct6Constant.PREDEFINED_IS_PASS_NOT_CHINESE;
            }
            temp.put(IAct6Constant.PREDEFINED_TASK_ID, taskId);
            temp.put(IAct6Constant.PREDEFINED_COMMIT_USER_ID, values.get(IAct6Constant.PREDEFINED_COMMIT_USER_ID));
            temp.put(IAct6Constant.PREDEFINED_COMMIT_USER_NAME, values.get(IAct6Constant.PREDEFINED_COMMIT_USER_NAME));
            temp.put(IAct6Constant.PREDEFINED_AUDIT_STATE, auditState);
            temp.put(IAct6Constant.PREDEFINED_AUDIT_TIME, DateUtil.getNowDate("yyyy-MM-dd HH:mm:ss"));
            if (!StringUtils.isEmpty(isPass) && IAct6Constant.DICTIONARY_RM_YES_NOT_1.equals(isPass)) {
                temp.put(IAct6Constant.PREDEFINED_IS_PASS, IAct6Constant.DICTIONARY_RM_YES_NOT_1);
            } else {
                temp.put(IAct6Constant.PREDEFINED_IS_PASS, IAct6Constant.DICTIONARY_RM_YES_NOT_0);
            }
        }else{
            temp.put(IAct6Constant.PREDEFINED_COMMIT_USER_ID, values.get(IAct6Constant.PREDEFINED_COMMIT_USER_ID));
            temp.put(IAct6Constant.PREDEFINED_COMMIT_USER_NAME, values.get(IAct6Constant.PREDEFINED_COMMIT_USER_NAME));
        }

        //去掉审核标志
        values.remove(IAct6Constant.PREDEFINED_IS_PASS);
        temp.putAll(values);
        taskService.setVariablesLocal(taskId, temp);
        taskService.complete(taskId, temp);
    }

    /**
     * 根据流程实例id获取流程实例的审核意见列表
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "auditOpinions/{taskId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> auditOpinions(@PathVariable String taskId) throws Exception {
        log.info("auditOpinions parameter taskId:" + (taskId == null ? "" : taskId));
        Map<String, Object> retMap = new HashMap<>();
        try {
            //根据任务id查询实例id
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if(task==null){
                String msg="任务id:"+taskId+"对应任务找不到!";
                log.error(msg);
                throw  new Exception(msg);
            }
            List<Comment> comments1 = activitiService.getTaskComments(task.getProcessInstanceId());

            List<HistoricActivityInstance> historicTaskInstanceList = historyService.createHistoricActivityInstanceQuery().processInstanceId(task.getProcessInstanceId())
                    .orderByHistoricActivityInstanceStartTime().desc().orderByHistoricActivityInstanceEndTime().desc().list();
            List<AuditInfoVo> listAuditInfoVos = new ArrayList<>();
            for (HistoricActivityInstance historicActivityInstance : historicTaskInstanceList) {
                if(StringUtils.isNotEmpty(historicActivityInstance.getTaskId())) {
                    List<Comment> comments = taskService.getTaskComments(historicActivityInstance.getTaskId());
                    if (comments != null && comments.size() > 0) {
                        AuditInfoVo auditInfoVo = new AuditInfoVo();
                        //审核意见
                        auditInfoVo.setAuditOpinion(comments.get(0).getFullMessage());
                        List<HistoricVariableInstance> taskVarlist=historyService.createHistoricVariableInstanceQuery()
                                .taskId(historicActivityInstance.getTaskId()).list();
                        for(HistoricVariableInstance historicTaskInstance:taskVarlist) {
                            if(IAct6Constant.PREDEFINED_COMMIT_USER_NAME.equalsIgnoreCase(historicTaskInstance.getVariableName())) {
                                auditInfoVo.setAuditName((String)historicTaskInstance.getValue());
                            } else if(IAct6Constant.PREDEFINED_AUDIT_TIME.equalsIgnoreCase(historicTaskInstance.getVariableName())) {
                                auditInfoVo.setAuditTime((String)historicTaskInstance.getValue());
                            } else if(IAct6Constant.PREDEFINED_AUDIT_STATE.equalsIgnoreCase(historicTaskInstance.getVariableName())) {
                                //翻译状态

                                auditInfoVo.setAuditState((String)historicTaskInstance.getValue());
                                if(IAct6Constant.DICTIONARY_RM_YES_NOT_1.equalsIgnoreCase(auditInfoVo.getAuditState())){
                                    auditInfoVo.setAuditState(IAct6Constant.PREDEFINED_IS_PASS_YES_CHINESE);
                                }else{
                                    auditInfoVo.setAuditState(IAct6Constant.PREDEFINED_IS_PASS_NOT_CHINESE);
                                }
                            }
                        }
                        listAuditInfoVos.add(auditInfoVo);
                    }
                }
            }

            //返回的审核意见
            log.info("返回审核意见:" + JSON.toJSONString(listAuditInfoVos));
            retMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
            retMap.put(IAct6Constant.AJAX_MESSAGE, "查询成功");
            retMap.put(IAct6Constant.AJAX_LIST, listAuditInfoVos);
        } catch (Exception e) {
            log.error("auditOpinions error", e);
            retMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
            retMap.put(IAct6Constant.AJAX_MESSAGE, "查询审核意见失败!");
        }
        return retMap;
    }

    /**
     * 根据用户id的翻页查询待办任务列表
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "todoTasks/{userId}/{pageSize}/{pageNo}",
             produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> todoTasks(@PathVariable String userId, @PathVariable("pageSize") int pageSize, @PathVariable("pageNo") int pageNo) throws Exception {
        log.info("todoTasks parameter userId:" + (userId == null ? "" : userId));
        Map<String, Object> retMap = new HashMap<>();
        int startIndex;
        int endIndex;
        try {
            List<ExtTask> retTasks = new ArrayList<>();
            if (StringUtils.isEmpty(userId))
                throw new Exception("用户id为空!");
            startIndex = (pageNo - 1) * pageSize;
            endIndex = startIndex + pageSize;

            // 根据当前人的ID查询已签收，未签收用户
            long totalRecordNum = taskService.createTaskQuery().taskCandidateOrAssigned(userId).count();
            List<Task> todoList = taskService.createTaskQuery().taskCandidateOrAssigned(userId).orderByTaskCreateTime().desc().listPage(startIndex, endIndex);

            for (Task task : todoList) {
                String processInstanceId = task.getProcessInstanceId();
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                String businessKey = processInstance.getBusinessKey();
                ProcessDefinition processDefinition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());

                ExtTask extTask = makeExtTask(task, businessKey, processDefinition.getName());
                retTasks.add(extTask);
            }
            retMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
            retMap.put(IAct6Constant.AJAX_MESSAGE, "查询代办任务成功");
            retMap.put(IAct6Constant.AJAX_LIST, retTasks);
            retMap.put(IAct6Constant.AJAX_TOTAL_RECORD, totalRecordNum);
        } catch (Exception e) {
            log.error("查询代办任务失败！", e);
            retMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
            retMap.put(IAct6Constant.AJAX_MESSAGE, "查询失败!");
        }
        return retMap;
    }

    /**
     * 构造扩展的任务
     *
     * @param task
     * @param processDefineName
     * @param businessKey
     * @return
     */
    private ExtTask makeExtTask(Task task, String businessKey, String processDefineName) {
        ExtTask extTask = new ExtTask();
        extTask.setId(task.getId());
        extTask.setName(task.getName());
        extTask.setProcessInstanceId(task.getProcessInstanceId());
        extTask.setProcessDefinitionId(task.getProcessDefinitionId());
        extTask.setCreateTime(task.getCreateTime());
        extTask.setClaimTime(task.getClaimTime());
        extTask.setAssignee(task.getAssignee());
        if (StringUtils.isNotEmpty(businessKey)) {
            extTask.setBusinessKey(businessKey);
        }
        extTask.setProcessDefineName(processDefineName);
        return extTask;
    }

    @RequestMapping(value = "/process-definitions/{pageSize}/{pageNo}", method = RequestMethod.GET)
    public Map<String, Object> processdefinitions(@PathVariable("pageSize") int pageSize, @PathVariable("pageNo") int pageNo, @RequestParam Map<String, String> allRequestParams) {
        ProcessDefinitionQuery processDefinitionQuery = this.repositoryService.createProcessDefinitionQuery();
        Map<String, Object> returnMap = new HashMap<>();
        if (allRequestParams.containsKey("category")) {
            processDefinitionQuery.processDefinitionCategory((String) allRequestParams.get("category"));
        }

        if (allRequestParams.containsKey("categoryLike")) {
            processDefinitionQuery.processDefinitionCategoryLike((String) allRequestParams.get("categoryLike"));
        }

        if (allRequestParams.containsKey("categoryNotEquals")) {
            processDefinitionQuery.processDefinitionCategoryNotEquals((String) allRequestParams.get("categoryNotEquals"));
        }

        if (allRequestParams.containsKey("key")) {
            processDefinitionQuery.processDefinitionKey((String) allRequestParams.get("key"));
        }

        if (allRequestParams.containsKey("keyLike")) {
            processDefinitionQuery.processDefinitionKeyLike((String) allRequestParams.get("keyLike"));
        }

        if (allRequestParams.containsKey("name")) {
            processDefinitionQuery.processDefinitionName((String) allRequestParams.get("name"));
        }

        if (allRequestParams.containsKey("nameLike")) {
            processDefinitionQuery.processDefinitionNameLike((String) allRequestParams.get("nameLike"));
        }

        if (allRequestParams.containsKey("resourceName")) {
            processDefinitionQuery.processDefinitionResourceName((String) allRequestParams.get("resourceName"));
        }

        if (allRequestParams.containsKey("resourceNameLike")) {
            processDefinitionQuery.processDefinitionResourceNameLike((String) allRequestParams.get("resourceNameLike"));
        }

        if (allRequestParams.containsKey("version")) {
            processDefinitionQuery.processDefinitionVersion(Integer.valueOf((String) allRequestParams.get("version")));
        }

        Boolean latest;
        if (allRequestParams.containsKey("suspended")) {
            latest = Boolean.valueOf((String) allRequestParams.get("suspended"));
            if (latest != null) {
                if (latest) {
                    processDefinitionQuery.suspended();
                } else {
                    processDefinitionQuery.active();
                }
            }
        }

        if (allRequestParams.containsKey("latest")) {
            latest = Boolean.valueOf((String) allRequestParams.get("latest"));
            if (latest != null && latest) {
                processDefinitionQuery.latestVersion();
            }
        }

        if (allRequestParams.containsKey("deploymentId")) {
            processDefinitionQuery.deploymentId((String) allRequestParams.get("deploymentId"));
        }

        if (allRequestParams.containsKey("startableByUser")) {
            processDefinitionQuery.startableByUser((String) allRequestParams.get("startableByUser"));
        }

        if (allRequestParams.containsKey("tenantId")) {
            processDefinitionQuery.processDefinitionTenantId((String) allRequestParams.get("tenantId"));
        }

        if (allRequestParams.containsKey("tenantIdLike")) {
            processDefinitionQuery.processDefinitionTenantIdLike((String) allRequestParams.get("tenantIdLike"));
        }

        int startIndex;
        int endIndex;
        try {
            startIndex = (pageNo - 1) * pageSize;
            endIndex = startIndex + pageSize;
            List<ProcessDefinition> processDefinitions = processDefinitionQuery.listPage(startIndex, endIndex);
            returnMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
            List<ProcessDefinitionInfo> wrapperProcessDefinetions = convertOrgProcessDefinitions(processDefinitions);
            returnMap.put(IAct6Constant.AJAX_LIST, wrapperProcessDefinetions);
            //提取总记录数
            long totalRecordNum = 0;
            totalRecordNum = processDefinitionQuery.count();
            returnMap.put(IAct6Constant.AJAX_TOTAL_RECORD, totalRecordNum);
        } catch (Exception e) {
            returnMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
            returnMap.put(IAct6Constant.AJAX_MESSAGE, "查询流程报错");
        }
        return returnMap;
    }

    /**
     * 转换原生的流程定义信息未包装后流程信息
     *
     * @param processDefinitions
     * @return
     */
    private List<ProcessDefinitionInfo> convertOrgProcessDefinitions(List<ProcessDefinition> processDefinitions) {
        List<ProcessDefinitionInfo> retList = new ArrayList<>();
        for (ProcessDefinition processDefinition : processDefinitions) {
            ProcessDefinitionInfo processDefinitionInfo = new ProcessDefinitionInfo();

            processDefinitionInfo.setCategory(processDefinition.getCategory());
            processDefinitionInfo.setDeploymentId(processDefinition.getDeploymentId());
            processDefinitionInfo.setDescription(processDefinition.getDescription());

            processDefinitionInfo.setCategory(processDefinition.getCategory());
            processDefinitionInfo.setHasStartFormKey(processDefinition.hasStartFormKey());
            processDefinitionInfo.setKey(processDefinition.getKey());
            processDefinitionInfo.setName(processDefinition.getName());
            processDefinitionInfo.setVersion(processDefinition.getVersion());
            processDefinitionInfo.setId(processDefinition.getId());
//            processDefinitionInfo.setSuspensionState(processDefinition.);

            retList.add(processDefinitionInfo);
        }

        return retList;
    }
}
