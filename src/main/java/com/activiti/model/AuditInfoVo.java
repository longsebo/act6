/**
 * 软件著作权：东方汇创
 *
 * 系统名称：  qb5&activiti
 *
 * 文件名称：  ProcessDataItemsVo.java
 *
 * 功能描述：  流程数据项值对象
 * 
 * 版本历史：
 * 
 * 2016-12-26   1.0.0版 （龙色波）（创建文件）
 */
package com.activiti.model;



/**
 *  审核信息vo
 * 
 * @author 龙色波
 */
public class AuditInfoVo {


    //开始vo的属性
    /**
     * auditName 表示：审核人
     */
    private String auditName;
    /**
     * auditTime 表示：审核时间
     */
    private String auditTime;
    /**
     * auditState 表示：审核状态
     */
    private String auditState;
    /**
     * auditOpinion 表示：审核意见
     */
    private String auditOpinion;
	public String getAuditName() {
		return auditName;
	}
	public void setAuditName(String auditName) {
		this.auditName = auditName;
	}
	public String getAuditTime() {
		return auditTime;
	}
	public void setAuditTime(String auditTime) {
		this.auditTime = auditTime;
	}
	public String getAuditState() {
		return auditState;
	}
	public void setAuditState(String auditState) {
		this.auditState = auditState;
	}
	public String getAuditOpinion() {
		return auditOpinion;
	}
	public void setAuditOpinion(String auditOpinion) {
		this.auditOpinion = auditOpinion;
	}
    

}
