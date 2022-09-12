package com.activiti.controller;

/**
 * 作者：leimuzi
 * 日期：2020/07/28 15:54
 * todo:
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.activiti.config.CustomProcessDiagramGenerator;
import com.activiti.config.WorkflowConstants;
import com.activiti.service.ActivitiService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作流操作类
 * @author yangyicai
 */
@Controller
public class ActititiCtrl {
    //日志
    private static final Logger logger = LoggerFactory.getLogger(ActititiCtrl.class);
    @Autowired
    //流程服务组件：用于流程定义和存取
    private RepositoryService repositoryService;
    @Autowired
    //历史服务组件：用于获取正在运行或已经完成的流程实例的信息
    private HistoryService historyService;
    @Autowired
    //运行时服务组件：提供了启动流程、查询流程实例、设置获取流程实例变量等功能。
    private RuntimeService runtimeService;
    @Autowired
    //数据模型转换
    private ObjectMapper objectMapper;

    //流程服务 自建
    @Autowired
    private ActivitiService activitiService;

    /**
     * 首页控制器：获取工作流模型列表控制器
     *
     * @param modelAndView 页面对象
     * @return 返回页面对象
     */
    @RequestMapping("/")
    public ModelAndView index(ModelAndView modelAndView) {
        modelAndView.setViewName("index");
        //通过流程服务组件获取当前的工作流模型列表
        List<Model> actList = repositoryService.createModelQuery().list();
        modelAndView.addObject("actList", actList);
        //获取当前以部署的流程
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
        modelAndView.addObject("pdList", processDefinitions);
        //回去当前已经开始执行的流程
        List<com.activiti.model.ProcessInstance> processInstances = activitiService.getTaskList();
        modelAndView.addObject("piList", processInstances);
        return modelAndView;
    }

    /**
     * 跳转编辑器/编辑工作流页面
     *
     * @return
     */
    @GetMapping("/editor")
    public String editor() {
        return "modeler";
    }

    /**
     * 创建模型
     *
     * @param response
     */
    @RequestMapping("/create")
    public void create(HttpServletResponse response) throws IOException {
        //创建一个空模型
        Model model = repositoryService.newModel();

        //设置一下默认信息
        String modelName = "new-model";//模型名称
        String modelKey = "new-key";// 模型key
        String modelDescription = ""; //模型描述
        int modelVersion = 1; //默认版本号


        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, modelName);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, modelDescription);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, modelVersion);
        model.setName(modelName);
        model.setKey(modelKey);
        model.setMetaInfo(modelNode.toString());
        repositoryService.saveModel(model);
        createObjectNode(model.getId());
        response.sendRedirect("/editor?modelId=" + model.getId());
        logger.info("创建模型结束，返回模型ID：{}", model.getId());
    }

    /**
     * 创建模型时完善ModelEditorSource
     *
     * @param modelId
     */
    @SuppressWarnings("/deprecation")
    private void createObjectNode(String modelId) {
        logger.info("创建模型完善ModelEditorSource入参模型ID：{}", modelId);
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        try {
            repositoryService.addModelEditorSource(modelId, editorNode.toString().getBytes("utf-8"));
        } catch (Exception e) {
            logger.info("创建模型时完善ModelEditorSource服务异常：{}", e);
        }
        logger.info("创建模型完善ModelEditorSource结束");
    }

    /**
     * 发布流程
     *
     * @param modelId 模型ID
     * @return
     */
    @RequestMapping("/publish")
    public String publish(String modelId) {
        logger.info("流程部署入参modelId：{}", modelId);
        Map<String, String> map = new HashMap<String, String>();
        try {
            Model modelData = repositoryService.getModel(modelId);
            byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
            if (bytes == null) {
                logger.info("部署ID:{}的模型数据为空，请先设计流程并成功保存，再进行发布", modelId);
                map.put("code", "FAILURE");
            }
            JsonNode modelNode = new ObjectMapper().readTree(bytes);
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addBpmnModel(modelData.getKey() + ".bpmn20.xml", model)
                    .deploy();
            modelData.setDeploymentId(deployment.getId());
            repositoryService.saveModel(modelData);
            map.put("code", "SUCCESS");
        } catch (Exception e) {
            logger.info("部署modelId:{}模型服务异常：{}", modelId, e);
            map.put("code", "FAILURE");
        }
        logger.info("流程部署出参map：{}", map);

        return "redirect:/";
    }

    /**
     * 撤销流程定义
     *
     * @param modelId 模型ID
     * @return
     */
    @RequestMapping("/revokePublish")
    public String revokePublish(String modelId) {
        logger.info("撤销发布流程入参modelId：{}", modelId);
        Map<String, String> map = new HashMap<String, String>();
        Model modelData = repositoryService.getModel(modelId);
        if (null != modelData) {
            try {
                /**
                 * 参数不加true:为普通删除，如果当前规则下有正在执行的流程，则抛异常
                 * 参数加true:为级联删除,会删除和当前规则相关的所有信息，包括历史
                 */
                repositoryService.deleteDeployment(modelData.getDeploymentId(), true);
                map.put("code", "SUCCESS");
            } catch (Exception e) {
                logger.error("撤销已部署流程服务异常：{}", e);
                map.put("code", "FAILURE");
            }
        }

        return "redirect:/";
    }

    /**
     * 删除流程实例
     *
     * @param modelId 模型ID
     * @return
     */
    @RequestMapping("/delete")
    public String deleteProcessInstance(String modelId) {
        logger.info("删除流程实例入参modelId：{}", modelId);
        Map<String, String> map = new HashMap<String, String>();
        Model modelData = repositoryService.getModel(modelId);
        if (null != modelData) {
            try {
                //先删除流程实例，再删除工作流模型
                ProcessInstance pi = runtimeService.createProcessInstanceQuery().
                        processDefinitionKey(modelData.getKey()).singleResult();
                if (null != pi) {
                    runtimeService.deleteProcessInstance(pi.getId(), "");
                    historyService.deleteHistoricProcessInstance(pi.getId());
                }
                //删除流程模型
                repositoryService.deleteModel(modelId);
                map.put("code", "SUCCESS");
            } catch (Exception e) {
                logger.error("删除流程实例服务异常：{}", e);
                map.put("code", "FAILURE");
            }
        }
        logger.info("删除流程实例出参map：{}", map);

        return "redirect:/";
    }

    /**
     * @读取动态流程图 带绿色的
     */
    public void readProcessImg(String processInstanceId, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(processInstanceId)) {
            logger.error("参数为空");
        }
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) repositoryService
                .getProcessDefinition(processInstance.getProcessDefinitionId());
        List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
        // 高亮环节id集合
        List<String> highLightedActivitis = new ArrayList<String>();
        // 高亮线路id集合
        List<String> highLightedFlows = getHighLightedFlows(definitionEntity, highLightedActivitList);
        for (HistoricActivityInstance tempActivity : highLightedActivitList) {
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }

        Set<String> currIds = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list()
                .stream().map(e->e.getActivityId()).collect(Collectors.toSet());

        CustomProcessDiagramGenerator diagramGenerator = new CustomProcessDiagramGenerator();
        InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis,
                highLightedFlows, "宋体", "宋体", "宋体", null, 1.0, new Color[] { WorkflowConstants.COLOR_NORMAL, WorkflowConstants.COLOR_CURRENT }, currIds);
        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    /**
     * 获取需要高亮的线
     *
     * @param processDefinitionEntity
     * @param historicActivityInstances
     * @return
     */
    private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinitionEntity,
                                             List<HistoricActivityInstance> historicActivityInstances) {
        List<String> highFlows = new ArrayList<>();// 用以保存高亮的线flowId
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
//            ActivityImpl activityImpl = processDefinitionEntity.findActivity(historicActivityInstances.get(i).getActivityId());// 得到节点定义的详细信息
//            List sameStartTimeNodes = new ArrayList<>();// 用以保存后需开始时间相同的节点
//            ActivityImpl sameActivityImpl1 = processDefinitionEntity.findActivity(historicActivityInstances.get(i + 1).getActivityId());
//            // 将后面第一个节点放在时间相同节点的集合里
//            sameStartTimeNodes.add(sameActivityImpl1);
//            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
//                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
//                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点
//                if (Math.abs(activityImpl1.getStartTime().getTime()-activityImpl2.getStartTime().getTime()) < 200) {
////                    if (activityImpl1.getStartTime().equals(activityImpl2.getStartTime())) {
//                    // 如果第一个节点和第二个节点开始时间相同保存
//                    ActivityImpl sameActivityImpl2 = processDefinitionEntity
//                            .findActivity(activityImpl2.getActivityId());
//                    sameStartTimeNodes.add(sameActivityImpl2);
//                } else {
//                    // 有不相同跳出循环
//                    break;
//                }
//            }
//            List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();// 取出节点的所有出去的线
//            for (PvmTransition pvmTransition : pvmTransitions) {
//                // 对所有的线进行遍历
//                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition.getDestination();
//                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
//                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
//                    highFlows.add(pvmTransition.getId());
//                }
//            }
            highFlows.add(historicActivityInstances.get(i).getActivityId());
        }
        return highFlows;
    }




    /**
     * 已部署流程
     */

    /**
     * 开始流程实例
     *
     * @return
     */
    @RequestMapping("/startByPDKey")
    public String startByPDKey(String processDefinitionKey) {
        logger.info("开始流程实例入参piId：{}", processDefinitionKey);
        Map<String, String> map = new HashMap<String, String>();
        try {
            //开始流程实例
            ProcessInstance myProcess1 = runtimeService.startProcessInstanceById(processDefinitionKey);
            //输出运行时流程实例id以及我们启动的流程它的一个定义id
            System.out.println("流程实例id：" + myProcess1.getId());
            System.out.println("流程定义ID:" + myProcess1.getProcessDefinitionId());
            map.put("code", "SUCCESS");
        } catch (Exception e) {
            logger.error("开始流程实例服务异常：{}", e);
            map.put("code", "FAILURE");
        }
        logger.info("开始流程实例出参map：{}", map);

        return "redirect:/";
    }

    /**
     * 删除流程实例
     *
     * @return
     */
    @RequestMapping("/deleteByDid")
    public String deleteProcessInstanceByDid(String deploymentId) {
        logger.info("删除流程实例入参piId：{}", deploymentId);
        Map<String, String> map = new HashMap<String, String>();
        try {
            //删除流程模型
            repositoryService.deleteDeployment(deploymentId, true);
            map.put("code", "SUCCESS");
        } catch (Exception e) {
            logger.error("删除流程实例服务异常：{}", e);
            map.put("code", "FAILURE");
        }
        logger.info("删除流程实例出参map：{}", map);

        return "redirect:/";
    }







    /**
     * 已开始流程
     */

    /**
     * 完成任务
     */
    @RequestMapping("/complete")
    public String complete(String taskId) {
        //获取task、id
        ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = defaultProcessEngine.getTaskService();
        //根据ID完成任务
        taskService.complete(taskId);
        return "redirect:/";
    }

    /**
     * 取消任务
     */
    @RequestMapping("/unclaim")
    public String unclaim(String taskId) {
        //获取task、id
        ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = defaultProcessEngine.getTaskService();
        //根据ID取消任务
        taskService.unclaim(taskId);
        return "redirect:/";
    }

    /**
     * 图片展示 页面
     */
    @RequestMapping("/img")
    public String img(org.springframework.ui.Model model, String imgUrl) {
        model.addAttribute("imgUrl","/getFlowImgByDid2?deploymentId="+imgUrl);
        return "img";
    }

    /**
     * 获取流程图
     */
    @RequestMapping("/getFlowImgByDid")
    public void getFlowImgByDid(HttpServletRequest request,
                           HttpServletResponse response,
                           String deploymentId, String modelId) {
        try {
            // 写流文件到前端浏览器
            ServletOutputStream out = response.getOutputStream();
            response.addHeader("Content-Disposition", "inline;filename="+ new String(("img.png").getBytes("UTF-8"), "UTF-8"));
            response.setContentType("image/jpeg;charset=UTF-8");
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            //获取act 图片
            InputStream actImg = null;
            if(deploymentId != null && !"".equals(deploymentId)){
                String pidByMid = activitiService.getPidByDid(deploymentId);
                actImg = getActImg(pidByMid);
            }
            if(modelId != null && !"".equals(modelId)){
                String pidByMid = activitiService.getPidByMid(modelId);
                actImg = getActImg(pidByMid);
            }
            try {
                bis = new BufferedInputStream(actImg);
                bos = new BufferedOutputStream(out);
                byte[] buff = new byte[1024];
                int bytesRead;
                while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                    bos.write(buff, 0, bytesRead);
                }
                out.flush();
            } catch (IOException e) {
                throw e;
            } finally {
                if (bis != null){bis.close();}
                if (bos != null){bos.close();}
                if(out!=null){out.close();}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InputStream getActImg(String processDefinitionId){
        //得到流程引擎的方式三，利用底层封装，来加载配置文件，只需要调用方法即可
        ProcessEngine pec = ProcessEngines.getDefaultProcessEngine();
        //部署的服务对象`
        RepositoryService repositoryService = pec.getRepositoryService();
        //这个类在5.22.0往上的版本中才有
        DefaultProcessDiagramGenerator diagramGenerator=new DefaultProcessDiagramGenerator();
        //根据id获取
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<String> stirngs = new ArrayList<String>();
        List<String> taskNameByDPid = activitiService.getTaskNameByDPid(processDefinitionId);
        if(taskNameByDPid != null && taskNameByDPid.size() > 0){
            for (String taskName : taskNameByDPid) {
                stirngs.add(taskName);
            }
        }
        //bpmnModel:当前流程对应的流程模型，可以通过repositoryService.getBpmnModel(processDefinitionId)获取
        //imageType:图片类型,jpg,png等
        //highLightedActivities:需要高亮显示的节点的id
        InputStream png = diagramGenerator.generateDiagram(bpmnModel, "png", stirngs,
                new ArrayList<String>(),
                "宋体",
                "宋体",
                "宋体",
                null,
                1.0);
        return png;
    }

    /**
     * 获取流程图2
     */
    @RequestMapping("/getFlowImgByDid2")
    public void getFlowImgByDid2(HttpServletRequest request,
                                HttpServletResponse response,
                                String deploymentId) {
        try {
            readProcessImg(deploymentId,response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
