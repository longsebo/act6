package com.activiti.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * API URL 参数配置类
 */
@Component
@ConfigurationProperties(prefix ="url")
public class UrlProperties {
    private List<UrlConfigInfo> configs;

    public List<UrlConfigInfo> getConfigs() {
        return configs;
    }

    public void setConfigs(List<UrlConfigInfo> configs) {
        this.configs = configs;
    }

    /**
     * 根据类型找到配置
     * @param type
     * @return
     * @throws Exception
     */
    public UrlConfigInfo getConfigByType(String type) throws Exception {
        for(UrlConfigInfo config:configs){
            if(config.getType().equalsIgnoreCase(type)){
                return config;
            }
        }
        throw new Exception("没找到 类型为:"+type+"配置!");
    }
    @Component
    public  static class UrlConfigInfo{
        /**
         * 接口类型
         */
        private String type;
        /**
         * 接口url
         */
        private String url;
        /**
         * 接口提交方法
         */
        private String method;
        /**
         * 页号参数名称
         */
        private String pageNoParameterName;
        /**
         * 页面大小参数名称
         */
        private String pageSizeParameterName;
        /**
         * 提取id正则表达式
         */
        private String idRegExp;
        /**
         * 提取名称正则表达式
         */
        private String nameRegExp;
        /**
         * 接口成功标识,包含这个标识，则认为成功
         */
        private String successFlag;
        /**
         * 提取接口消息正则表达式，通常用于提取错误消息
         */
        private String messageRegExp;
        /**
         * 提取总记录数正则表达式
         */
        private String totalRecordNumRegExp;

        public String getTotalRecordNumRegExp() {
            return totalRecordNumRegExp;
        }

        public void setTotalRecordNumRegExp(String totalRecordNumRegExp) {
            this.totalRecordNumRegExp = totalRecordNumRegExp;
        }

        public String getSuccessFlag() {
            return successFlag;
        }

        public void setSuccessFlag(String successFlag) {
            this.successFlag = successFlag;
        }

        public String getMessageRegExp() {
            return messageRegExp;
        }

        public void setMessageRegExp(String messageRegExp) {
            this.messageRegExp = messageRegExp;
        }

        public String getPageNoParameterName() {
            return pageNoParameterName;
        }

        public void setPageNoParameterName(String pageNoParameterName) {
            this.pageNoParameterName = pageNoParameterName;
        }

        public String getPageSizeParameterName() {
            return pageSizeParameterName;
        }

        public void setPageSizeParameterName(String pageSizeParameterName) {
            this.pageSizeParameterName = pageSizeParameterName;
        }

        public String getIdRegExp() {
            return idRegExp;
        }

        public void setIdRegExp(String idRegExp) {
            this.idRegExp = idRegExp;
        }

        public String getNameRegExp() {
            return nameRegExp;
        }

        public void setNameRegExp(String nameRegExp) {
            this.nameRegExp = nameRegExp;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

}
