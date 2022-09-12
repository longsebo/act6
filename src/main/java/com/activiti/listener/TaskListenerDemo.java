package com.activiti.listener;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;

/**
 * 作者：leimuzi
 * 日期：2020/07/31 17:58
 * todo:
 */
public class TaskListenerDemo implements Serializable, TaskListener {
    private Expression arg;

    public Expression getArg() {
        return arg;
    }

    public void setArg(Expression arg) {
        this.arg = arg;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("任务监听器:" + arg.getValue(delegateTask));
    }

}
