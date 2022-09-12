package com.activiti.listener;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;

/**
 * 作者：leimuzi
 * 日期：2020/07/31 17:57
 * todo:
 *  * 可以使用 CLASS ,EXPRESSION,DELEGATE EXPRESSSION三种方式来创建监听器，这里使用第三种方式，其他两种方式和
 *  * 在servicetask中的使用方式相同
 */
public class ExectuionListenerDemo implements Serializable, ExecutionListener {

    /**
     * @Fields serialVersionUID : TODO(用一句话描述这个变量表示什么)
     */
    private static final long serialVersionUID = 8513750196548027535L;
    private Expression message;

    public Expression getMessage() {
        return message;
    }

    public void setMessage(Expression message) {
        this.message = message;
    }

    @Override
    public void notify(DelegateExecution execution) {
        System.out.println("流程监听器: id" + execution.getId() + " 开始了！");
    }

}
