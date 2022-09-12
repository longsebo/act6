package com.activiti.model;

import java.sql.Timestamp;

/**
 * 扩展接口定义VO
 */
public class ActExtAPIVo {
    private String serviceName;
    private String method;
    private String commitService;
    private String contentEncode;
    private String requestParameterFormat;
    private String requestParameterSet;
    private String bodyData;
    private String contentType;
    private String respondTestField;
    private String correctAnswerMatches;
    private String businesskeyRegExp;
    private String respondMessageRegExp;
    private String requestHeadSet;
    private String isUse;
    private String remarker;
    private Timestamp createTime;
    private String oldServiceName;

    public String getOldServiceName() {
        return oldServiceName;
    }

    public void setOldServiceName(String oldServiceName) {
        this.oldServiceName = oldServiceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCommitService() {
        return commitService;
    }

    public void setCommitService(String commitService) {
        this.commitService = commitService;
    }

    public String getContentEncode() {
        return contentEncode;
    }

    public void setContentEncode(String contentEncode) {
        this.contentEncode = contentEncode;
    }

    public String getRequestParameterFormat() {
        return requestParameterFormat;
    }

    public void setRequestParameterFormat(String requestParameterFormat) {
        this.requestParameterFormat = requestParameterFormat;
    }

    public String getRequestParameterSet() {
        return requestParameterSet;
    }

    public void setRequestParameterSet(String requestParameterSet) {
        this.requestParameterSet = requestParameterSet;
    }

    public String getBodyData() {
        return bodyData;
    }

    public void setBodyData(String bodyData) {
        this.bodyData = bodyData;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getRespondTestField() {
        return respondTestField;
    }

    public void setRespondTestField(String respondTestField) {
        this.respondTestField = respondTestField;
    }

    public String getCorrectAnswerMatches() {
        return correctAnswerMatches;
    }

    public void setCorrectAnswerMatches(String correctAnswerMatches) {
        this.correctAnswerMatches = correctAnswerMatches;
    }

    public String getBusinesskeyRegExp() {
        return businesskeyRegExp;
    }

    public void setBusinesskeyRegExp(String businesskeyRegExp) {
        this.businesskeyRegExp = businesskeyRegExp;
    }

    public String getRespondMessageRegExp() {
        return respondMessageRegExp;
    }

    public void setRespondMessageRegExp(String respondMessageRegExp) {
        this.respondMessageRegExp = respondMessageRegExp;
    }

    public String getRequestHeadSet() {
        return requestHeadSet;
    }

    public void setRequestHeadSet(String requestHeadSet) {
        this.requestHeadSet = requestHeadSet;
    }

    public String getIsUse() {
        return isUse;
    }

    public void setIsUse(String isUse) {
        this.isUse = isUse;
    }

    public String getRemarker() {
        return remarker;
    }

    public void setRemarker(String remarker) {
        this.remarker = remarker;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
