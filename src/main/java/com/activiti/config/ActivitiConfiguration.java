package com.activiti.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 作者：leimuzi
 * 日期：2020/07/31 15:31
 * todo:
 */
@Configuration
public class ActivitiConfiguration  {


    @Bean
    public ProcessEngineConfiguration processEngineConfiguration(DataSource dataSource, PlatformTransactionManager transactionManager){
        SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(dataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate("true");
        processEngineConfiguration.setDatabaseType("mysql");

        processEngineConfiguration.setTransactionManager(transactionManager);

        //流程图字体
        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");

        return processEngineConfiguration;
    }
    /**
     * 流程引擎，与spring整合使用factoryBean
     * @param processEngineConfiguration
     * @return
     */
//    @Bean
//    public ProcessEngineFactoryBean processEngine(ProcessEngineConfiguration processEngineConfiguration){
//        ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
//        processEngineFactoryBean.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
//        return processEngineFactoryBean;
//    }

    /**
     * 八大接口
     */
    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine){
        return processEngine.getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine){
        return processEngine.getRuntimeService();
    }

    @Bean
    public TaskService taskService(ProcessEngine processEngine){
        return processEngine.getTaskService();
    }

    @Bean
    public HistoryService historyService(ProcessEngine processEngine){
        return processEngine.getHistoryService();
    }

    @Bean
    public FormService formService(ProcessEngine processEngine){
        return processEngine.getFormService();
    }

    @Bean
    public IdentityService identityService(ProcessEngine processEngine){
        return processEngine.getIdentityService();
    }

    @Bean
    public ManagementService managementService(ProcessEngine processEngine){
        return processEngine.getManagementService();
    }

    @Bean
    public DynamicBpmnService dynamicBpmnService(ProcessEngine processEngine){
        return processEngine.getDynamicBpmnService();
    }

    //八大接口 end

    /**
     * 一些工具bean
     */

    /**
     * jackson xml util
     * @return
     */
    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}

