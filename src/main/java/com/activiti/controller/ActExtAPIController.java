package com.activiti.controller;

import com.activiti.IAct6Constant;
import com.activiti.model.ActExtAPIVo;
import com.activiti.service.ActExtAPIService;
import com.activiti.utils.RegExUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/actextapi")
@Slf4j
public class ActExtAPIController {
    @Autowired
    ActExtAPIService actExtAPIService;
    /**
     * 从页面表单获取信息注入vo，并插入单条记录
     * @param request http请求对象
     * @param vo 值对象
     * @return 返回Ajax应答实体对象
     */
    @RequestMapping(value = "insert", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> insert(HttpServletRequest request, @RequestBody ActExtAPIVo vo) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            verifyInsertVo(vo);  //打创建时间,IP戳
            //设置当前时间
            vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
            actExtAPIService.insert(vo);  //插入单条记录
            result.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
            result.put(IAct6Constant.AJAX_MESSAGE, "新增成功!");
        }catch (Exception e){
            log.error("新增失败",e);
            result.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
            result.put(IAct6Constant.AJAX_MESSAGE, e.getMessage());
        }
        return result;
    }

    /**
     * 校验插入数据
     * @param vo
     */
    private void verifyInsertVo(ActExtAPIVo vo) throws Exception {
        if(vo==null)
            throw new Exception("值对象为空!");
        //服务名称不能为空
        if(StringUtils.isEmpty(vo.getServiceName()))
            throw new Exception("服务名称为空!");
        //服务名称不能重复
        Map<String,Object> searchMap = new HashMap<>();
        searchMap.put("serviceName",vo.getServiceName());
         if(actExtAPIService.getCount(searchMap)>0)
             throw new Exception("服务名称:"+vo.getServiceName()+"已存在!");
        //下列属性不能为空
        //提交方法
        if(StringUtils.isEmpty(vo.getMethod()))
            throw new Exception("提交方法为空!");
        //提交服务url
        if(StringUtils.isEmpty(vo.getCommitService()))
            throw new Exception("提交服务url为空!");
        //内容编码
        if(StringUtils.isEmpty(vo.getContentEncode()))
            throw new Exception("内容编码为空!");
        //请求参数格式
        if(StringUtils.isEmpty(vo.getRequestParameterFormat()))
            throw new Exception("请求参数格式为空!");
        if(IAct6Constant.REQUEST_PARAMETER_FORMAT_PARAMETER.equalsIgnoreCase(vo.getRequestParameterFormat())) {
            //请求参数集合
            if (StringUtils.isEmpty(vo.getRequestParameterSet()))
                throw new Exception("请求参数集合为空!");
        }else {
            //BodyData
            if (StringUtils.isEmpty(vo.getBodyData()))
                throw new Exception("BodyData为空!");
            //ContentType
            if (StringUtils.isEmpty(vo.getContentType()))
                throw new Exception("ContentType为空!");
        }
        //应答测试字段
        if(StringUtils.isEmpty(vo.getRespondTestField()))
            throw new Exception("应答测试字段为空!");
        //正确应答匹配字符串
        if(StringUtils.isEmpty(vo.getCorrectAnswerMatches()))
            throw new Exception("正确应答匹配字符串为空!");
        //提取响应错误消息正则表达式
        if(StringUtils.isEmpty(vo.getRespondMessageRegExp()))
            throw new Exception("提取响应错误消息正则表达式为空!");
        //是否启用
        if(StringUtils.isEmpty(vo.getIsUse()))
            throw new Exception("是否启用为空!");

    }
    /**
     * 校验更新数据
     * @param vo
     */
    private void verifyUpdateVo(ActExtAPIVo vo) throws Exception {
        if(vo==null)
            throw new Exception("值对象为空!");
        //服务名称不能为空
        if(StringUtils.isEmpty(vo.getServiceName()))
            throw new Exception("服务名称为空!");
        //下列属性不能为空
        //提交方法
        if(StringUtils.isEmpty(vo.getMethod()))
            throw new Exception("提交方法为空!");
        //提交服务url
        if(StringUtils.isEmpty(vo.getCommitService()))
            throw new Exception("提交服务url为空!");
        //内容编码
        if(StringUtils.isEmpty(vo.getContentEncode()))
            throw new Exception("内容编码为空!");
        //请求参数格式
        if(StringUtils.isEmpty(vo.getRequestParameterFormat()))
            throw new Exception("请求参数格式为空!");
        if(IAct6Constant.REQUEST_PARAMETER_FORMAT_PARAMETER.equalsIgnoreCase(vo.getRequestParameterFormat())) {
            //请求参数集合
            if (StringUtils.isEmpty(vo.getRequestParameterSet()))
                throw new Exception("请求参数集合为空!");
        }else {
            //BodyData
            if (StringUtils.isEmpty(vo.getBodyData()))
                throw new Exception("BodyData为空!");
            //ContentType
            if (StringUtils.isEmpty(vo.getContentType()))
                throw new Exception("ContentType为空!");
        }
        //应答测试字段
        if(StringUtils.isEmpty(vo.getRespondTestField()))
            throw new Exception("应答测试字段为空!");
        //正确应答匹配字符串
        if(StringUtils.isEmpty(vo.getCorrectAnswerMatches()))
            throw new Exception("正确应答匹配字符串为空!");
        //提取响应错误消息正则表达式
        if(StringUtils.isEmpty(vo.getRespondMessageRegExp()))
            throw new Exception("提取响应错误消息正则表达式为空!");
        //是否启用
        if(StringUtils.isEmpty(vo.getIsUse()))
            throw new Exception("是否启用为空!");

    }

    /**
     * 从页面表单获取信息注入vo，并修改单条记录
     * @param request 请求对象
     * @param vo 值对象
     * @return 返回Ajax应答实体对象
     */
    @RequestMapping(value = "update", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> update(HttpServletRequest request, @RequestBody ActExtAPIVo vo) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            verifyUpdateVo(vo);
            actExtAPIService.update(vo);  //更新单条记录

            result.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
            result.put(IAct6Constant.AJAX_MESSAGE, "修改成功!");
        }catch (Exception e){
            log.error("更新接口服务定义失败",e);
            result.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
            result.put(IAct6Constant.AJAX_MESSAGE, e.getMessage());
        }
        return result;
    }
    /**
     * 根据id获取单条记录
     * @param request 请求对象
     * @param
     * @return 返回Ajax应答实体对象
     */
    @RequestMapping(value = "get/{serviceName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> get(@PathVariable("serviceName") String serviceName, HttpServletRequest request) {
        ActExtAPIVo bean = actExtAPIService.get(serviceName);
        Map<String, Object> result = new HashMap<String, Object>();
        if(bean==null){
            result.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
            result.put(IAct6Constant.AJAX_MESSAGE, "记录没找到(serviceName:"+serviceName+")!");
            return result;
        }

        result.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
        result.put(IAct6Constant.AJAX_BEAN, bean);
        return result;
    }
    /**
     * 从页面的表单获取单条记录serviceName并删除
     * @param request http请求对象
     * @return 返回列表页面
     */
    @RequestMapping(value = "delete/{serviceName}", method = RequestMethod.POST)
    public Map<String,Object> delete(@PathVariable("serviceName") String serviceName,HttpServletRequest request) {
        Map<String, Object> result = new HashMap<String, Object>();
        int deleteCount = 0;  //定义成功删除的记录数
        try {
            if (StringUtils.isNotEmpty(serviceName)) {
                deleteCount = actExtAPIService.delete(serviceName);
                if(deleteCount>0) {
                    result.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
                    result.put(IAct6Constant.AJAX_MESSAGE, "删除成功!");
                }else{
                    result.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
                    result.put(IAct6Constant.AJAX_MESSAGE, "删除失败!");
                }
            } else{
                result.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
                result.put(IAct6Constant.AJAX_MESSAGE, "删除失败,服务名称为空!");
            }


        }catch (Exception e){
            log.error("删除失败",e);
            result.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
            result.put(IAct6Constant.AJAX_MESSAGE, e.getMessage());
        }
        return result;
    }
    /**
     *  请求列表数据
     * @return
     */
    @RequestMapping(value = "/list/{pageSize}/{pageNo}/{orderBy}", produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public Map<String,Object> list(@PathVariable("pageNo") int pageNo,@PathVariable("pageSize") int pageSize,@PathVariable("orderBy") String orderBy, @RequestParam Map<String, Object> values){
        Map<String,Object> returnMap=new HashMap<String,Object>();
        int startIndex;
        try {
            startIndex = (pageNo - 1) * pageSize;
            List<ActExtAPIVo> returnList = actExtAPIService.search(values, orderBy, startIndex, pageSize);
            returnMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
            returnMap.put(IAct6Constant.AJAX_LIST, returnList);
            //提取总记录数
            int recordNum = actExtAPIService.getCount(values);
            returnMap.put(IAct6Constant.AJAX_TOTAL_RECORD, recordNum);
        }catch (Exception e){
            log.error("查询接口定义列表失败！",e);
            returnMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
            returnMap.put(IAct6Constant.AJAX_MESSAGE, "查询列表失败!");
        }
        return returnMap;
    }
    /**
     *  请求列表数据(只查名称)
     * @return
     */
    @RequestMapping(value = "/listName/{orderBy}",method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Map<String,Object> listName(@PathVariable("orderBy") String orderBy, @RequestBody Map<String, Object> values){
        Map<String,Object> returnMap=new HashMap<String,Object>();
        int startIndex;
        try {
            startIndex = 0;
            List<ActExtAPIVo> returnList = actExtAPIService.search(values, orderBy, startIndex, Integer.MAX_VALUE);
            List<String> returnNames = new ArrayList<>();
            for(ActExtAPIVo vo:returnList){
                returnNames.add(vo.getServiceName());
            }
            returnMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
            returnMap.put(IAct6Constant.AJAX_LIST, returnNames);
        }catch (Exception e){
            log.error("查询接口定义列表失败！",e);
            returnMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
            returnMap.put(IAct6Constant.AJAX_MESSAGE, "查询名称列表失败!");
        }
        return returnMap;
    }
}
