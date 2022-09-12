package com.activiti.service;

import com.activiti.dao.ActivitiDao;
import com.activiti.model.ProcessInstance;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.task.Comment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 作者：leimuzi
 * 日期：2020/07/31 09:36
 * todo:
 */
@Service
public class ActivitiService {

    @Autowired
    private ActivitiDao activitiDao;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    /**
     * 根据modelid 获取 processDefinitionId
     */
    public String getPidByMid(String modelId){
        return activitiDao.getPidByMid(modelId);
    }

    /**
     * 根据deploymentId 获取 processDefinitionId
     */
    public String getPidByDid(String deploymentId){
        return activitiDao.getPidByDid(deploymentId);
    }

    /**
     * 根据 processDefinitionId 获取 当前任务节点
     */
    public List<String> getTaskNameByDPid(String processDefinitionId){
        return activitiDao.getTaskNameByDPid(processDefinitionId);
    }

    /**
     * 根据 processDefinitionId 获取 当前任务节点
     */
    public String getTaskIdByPIid(String processDefinitionId){
        return activitiDao.getTaskIdByPIid(processDefinitionId);
    }

    /**
     * 获取当前执行任务列表
     * @return
     */
    public List<ProcessInstance> getTaskList(){
        return activitiDao.getTaskList();
    }
    /**
     * 根据流程定义id和任务定义key获取任务表单数据key
     * @param processDefinitionId 流程定义id
     * @param taskId 任务定义id
     * @return 任务表单数据key
     */
    public FlowElement getTask(String processDefinitionId, String taskId){
        BpmnModel procModel = repositoryService.getBpmnModel(processDefinitionId);
        Collection<FlowElement> flowElements = procModel.getMainProcess().getFlowElements();
        for(FlowElement e : flowElements) {
            if(e instanceof UserTask){
                UserTask task =(UserTask)e;
                if(task.getId().equals(taskId)){
                    return e;
                }

            }else if(e instanceof StartEvent){
                StartEvent task =(StartEvent)e;
                if(task.getId().equals(taskId)){
                    return e;
                }

            }
        }
        return null;
    }
    /**
     * 根据流程定义id获取开始节点
     * @param processDefinitionId 流程定义id
     * @return 任务表单数据key
     */
    public StartEvent getStartTaskByProcessDefineId(String processDefinitionId){
//		 return formService.getTaskFormKey(processDefinitionId, taskDefinitionKey);
        BpmnModel procModel = repositoryService.getBpmnModel(processDefinitionId);

        Collection<FlowElement> flowElements = procModel.getMainProcess().getFlowElements();
        for(FlowElement e : flowElements) {
          if(e instanceof StartEvent){
                StartEvent task =(StartEvent)e;
                return task;

            }
        }
        return null;
    }
    /**
     * 获取某节点审核意见
     * @param processInstanceId 流程实例id
     * @return
     */
    public List<Comment> getTaskComments(String processInstanceId){

        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().desc().orderByHistoricActivityInstanceEndTime().desc().list();
        List<Comment> comments = new ArrayList<Comment>();

        for(HistoricActivityInstance instance:list){
            if(!StringUtils.isEmpty(instance.getTaskId())){
                List<Comment> taskComments = taskService.getTaskComments(instance.getTaskId());
                if(!CollectionUtils.isEmpty(taskComments)){
                    comments.addAll(taskComments);
                }
            }
        }
        return comments;
    }
}
