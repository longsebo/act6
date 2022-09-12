package com.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


//@SpringBootTest
class ActApplicationTests {

    @Test
    void contextLoads() {
        // 创建一个数据源
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql:///activiti-demo?useSSL=false&characterEncoding=utf8&serverTimezone=GMT%2B8");
        dataSource.setUsername("root");
        dataSource.setPassword("root");

        // 创建流程引擎配置
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();
        // 设置数据源
        //    configuration.setDataSource(dataSource);
        // 如果不使用数据源, 可以通过配置连接信息来连接数据库
        configuration.setJdbcDriver("com.mysql.jdbc.Driver");
        configuration.setJdbcUrl("jdbc:mysql://localhost:3306/activiti_demo?useUnicode=true&characterEncoding=utf-8&useSSL=false");
        configuration.setJdbcUsername("root");
        configuration.setJdbcPassword("root");

        // 设置创建表的一个规则,有三种
        // DB_SCHEMA_UPDATE_FALSE = "false" 如果数据库里没有acti相关的表, 也不会创建
        // DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop" 不管数据库里有没acti的相关表, 都会先删除旧表再创建新表, 不推荐在生产中使用
        // DB_SCHEMA_UPDATE_TRUE = "true" 如果数据库里没有acti相关的表, 会自动创建
        // 仔细看看, 是不是有些类似于hibernate里的ddl-auto :)
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        // 构建流程引擎, 这一步就会创建好表, 但基本上表内都是空的, 因为还没有部署, 再没有流程实例
        ProcessEngine processEngine = configuration.buildProcessEngine();
        // 可以获取流程引擎的一些信息, 不过这个东西没啥用..
        System.out.println(processEngine.getName());
    }

}
