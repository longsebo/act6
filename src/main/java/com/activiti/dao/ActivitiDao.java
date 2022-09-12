package com.activiti.dao;

import com.activiti.model.ProcessInstance;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 作者：leimuzi
 * 日期：2020/07/31 09:38
 * todo:
 */
@Mapper
public interface ActivitiDao {


    public String getPidByMid(String modelId);

    public String getPidByDid(String deploymentId);

    public List<String> getTaskNameByDPid(String processDefinitionId);

    public String getTaskIdByPIid(String processDefinitionId);

    List<ProcessInstance> getTaskList();
}
