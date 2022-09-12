package com.activiti.controller;

import com.activiti.IAct6Constant;
import com.activiti.model.UrlProperties;
import com.activiti.service.ProcessDesignService;
import com.activiti.utils.HttpClientUtil;
import com.activiti.utils.RegExUtils;
import com.alibaba.fastjson.JSONObject;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.repository.ModelsPaginateList;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.activiti.model.ModelSaveRestResource;
import java.io.UnsupportedEncodingException;
import java.util.*;

import com.activiti.model.ModelEditorJsonRestResource;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author longsebo
 * @date 2020/4/21
 */
@RestController
@RequestMapping("/workflow")
public class ProcessDesignController {
    
    @Autowired
    private ProcessDesignService processDesignService;
    @Autowired
    private ModelSaveRestResource modelSaveRestResource;
    @Autowired
    private ModelEditorJsonRestResource modelEditorJsonRestResource;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private UrlProperties urlConfigInfo;
    @Autowired
    protected RestResponseFactory restResponseFactory;
    /**
     * 创建模型
     */
    @RequestMapping(value = "/model/insert", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void createModel(@RequestParam String key, @RequestParam String name, @RequestParam String category, @RequestParam String descp) throws UnsupportedEncodingException {

        processDesignService.createModel(key, name, category, descp);
    }
    
    @RequestMapping(value = "/model/list", method = RequestMethod.GET)
    public List<Model> listModel() {
        List<Model> listModel = processDesignService.listModel();
        return  listModel;
    }
    
    
    /**
     * 保存模型
     */
    @RequestMapping(value = "/model/{modelId}/xml/save", method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public void saveModelXml(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {
        modelSaveRestResource.saveModelXml(modelId, values);
    }
    
    @ResponseBody
    @GetMapping(value = "/deleteModel")
    public void flowDelete(@RequestParam(name = "modelId") String modelId){
        processDesignService.deleteModel(modelId);
    }
    
    /**
     * 根据生成的ID获取模型流程编辑器
     * @param modelId
     * @return
     */
    @RequestMapping(value = "/model/{modelId}/xml", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public JSONObject getEditorXml(@PathVariable String modelId) {
        return modelEditorJsonRestResource.getEditorXml(modelId);
    }
    
    @GetMapping(value = "/model/deploy")
    public String deploy(@RequestParam(name = "modelId") String modelId) throws Exception {
        return processDesignService.deployModel(modelId);
    }
    /**
     * 根据流程ID，任务名称获取所有扩展属性，适用于扩展属性
     * @return
     */
    @RequestMapping(value = "/getTaskCustomProperties/{processDefineId}/{taskName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public Map<String, List<ExtensionAttribute>> getTaskCustomProperties(@PathVariable("processDefineId") String processDefineId, @PathVariable("taskName") String taskName) {
        BpmnModel procModel = repositoryService.getBpmnModel(processDefineId);
        Collection<FlowElement> flowElements = procModel.getMainProcess().getFlowElements();

        for(FlowElement e : flowElements) {
            String classType = e.getClass().toString();

            if(!StringUtils.isEmpty(classType) && (classType.indexOf("Event")>-1 || classType.indexOf("Task")>-1)){
                if(e.getName().equalsIgnoreCase(taskName)){
                    return e.getAttributes();
                }
            }
        }
        return null;
    }
    /**
     * 获取用户列表
     *
     */
    @RequestMapping(value = "/listUsers/{pageSize}/{pageNo}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public Map<String,Object> listUsers(@PathVariable("pageSize") String  pageSize, @PathVariable("pageNo") String pageNo)  {
        Map<String,Object> returnMap = new HashMap<>();
        try {
            //调用外部url获取用户列表
            UrlProperties.UrlConfigInfo urlConfig = urlConfigInfo.getConfigByType(IAct6Constant.API_TYPE_USER);
            if (StringUtils.isEmpty(urlConfig.getUrl()))
                throw new Exception("url 尚未配置!");
            //判断http方法
            String result;
            if (IAct6Constant.HTTP_METHOD_POST.equalsIgnoreCase(urlConfig.getMethod())) {
                //附加翻页参数
                Map<String, String> parameter = new HashMap<>();
                parameter.put(urlConfig.getPageNoParameterName(), pageNo);
                parameter.put(urlConfig.getPageSizeParameterName(), pageSize);

                result = HttpClientUtil.post(urlConfig.getUrl(), parameter);
            } else {
                String url;
                url = urlConfig.getUrl();
                //附加翻页参数
                if (urlConfig.getUrl().contains("?")) {
                    url = url + "&" + urlConfig.getPageNoParameterName() + "=" + pageNo + "&" +
                            urlConfig.getPageSizeParameterName() + "=" + pageSize;
                } else {
                    url = url + "?" + urlConfig.getPageNoParameterName() + "=" + pageNo + "&" +
                            urlConfig.getPageSizeParameterName() + "=" + pageSize;
                }
                result = HttpClientUtil.get(url);
            }
            //判断结果是否成功
            if(!result.contains(urlConfig.getSuccessFlag())){
                returnMap.put(IAct6Constant.AJAX_STATUS,IAct6Constant.AJAX_RESULT_FAIL);
                returnMap.put(IAct6Constant.AJAX_MESSAGE,RegExUtils.getValue(result,urlConfig.getMessageRegExp(),1));
                return returnMap;
            }
            //从result提取id,name
            List<Map<String, Object>> returnList = new ArrayList<>();

            List<String> ids = RegExUtils.getValue(result,urlConfig.getIdRegExp());

            List<String> names = RegExUtils.getValue(result,urlConfig.getNameRegExp());
            for(int j=0;j<ids.size();j++) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", ids.get(j));
                if(j<names.size()) {
                    map.put("name", names.get(j));
                }else {
                    map.put("name","");
                }
                returnList.add(map);
            }

            returnMap.put(IAct6Constant.AJAX_STATUS,IAct6Constant.AJAX_RESULT_SUCCESS);
            returnMap.put(IAct6Constant.AJAX_LIST,returnList);
            //提取总记录数
            String totalRecordNum="0";
            if(!StringUtils.isEmpty(urlConfig.getTotalRecordNumRegExp())) {
                totalRecordNum = RegExUtils.getValue(result,urlConfig.getTotalRecordNumRegExp(),1);
            }
            returnMap.put(IAct6Constant.AJAX_TOTAL_RECORD,totalRecordNum);
        }catch (Exception e){
            returnMap.put(IAct6Constant.AJAX_STATUS,IAct6Constant.AJAX_RESULT_FAIL);
            returnMap.put(IAct6Constant.AJAX_MESSAGE,e.getMessage());
        }
        return returnMap;
    }
    /**
     * 获取角色列表
     *
     */
    @RequestMapping(value = "/listRoles/{pageSize}/{pageNo}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public Map<String,Object> listRoles(@PathVariable("pageSize") String  pageSize, @PathVariable("pageNo") String pageNo)  {
        Map<String,Object> returnMap = new HashMap<>();
        try {
            //调用外部url获取角色列表
            UrlProperties.UrlConfigInfo urlConfig = urlConfigInfo.getConfigByType(IAct6Constant.API_TYPE_ROLE);
            if (StringUtils.isEmpty(urlConfig.getUrl()))
                throw new Exception("url 尚未配置!");
            //判断http方法
            String result;
            if (IAct6Constant.HTTP_METHOD_POST.equalsIgnoreCase(urlConfig.getMethod())) {
                //附加翻页参数
                Map<String, String> parameter = new HashMap<>();
                parameter.put(urlConfig.getPageNoParameterName(), pageNo);
                parameter.put(urlConfig.getPageSizeParameterName(), pageSize);

                result = HttpClientUtil.post(urlConfig.getUrl(), parameter);
            } else {
                String url;
                url = urlConfig.getUrl();
                //附加翻页参数
                if (urlConfig.getUrl().contains("?")) {
                    url = url + "&" + urlConfig.getPageNoParameterName() + "=" + pageNo + "&" +
                            urlConfig.getPageSizeParameterName() + "=" + pageSize;
                } else {
                    url = url + "?" + urlConfig.getPageNoParameterName() + "=" + pageNo + "&" +
                            urlConfig.getPageSizeParameterName() + "=" + pageSize;
                }
                result = HttpClientUtil.get(url);
            }
            //判断结果是否成功
            if(!result.contains(urlConfig.getSuccessFlag())){
                returnMap.put(IAct6Constant.AJAX_STATUS,IAct6Constant.AJAX_RESULT_FAIL);
                returnMap.put(IAct6Constant.AJAX_MESSAGE,RegExUtils.getValue(result,urlConfig.getMessageRegExp(),1));
                return returnMap;
            }
            //从result提取id,name
            List<Map<String, Object>> returnList = new ArrayList<>();


            List<String> ids = RegExUtils.getValue(result,urlConfig.getIdRegExp());

            List<String> names = RegExUtils.getValue(result,urlConfig.getNameRegExp());
            for(int j=0;j<ids.size();j++) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", ids.get(j));
                if(j<names.size()) {
                    map.put("name", names.get(j));
                }else {
                    map.put("name","");
                }
                returnList.add(map);
            }

            returnMap.put(IAct6Constant.AJAX_STATUS,IAct6Constant.AJAX_RESULT_SUCCESS);
            returnMap.put(IAct6Constant.AJAX_LIST,returnList);
            //提取总记录数
            String totalRecordNum="0";
            if(!StringUtils.isEmpty(urlConfig.getTotalRecordNumRegExp())) {
                totalRecordNum = RegExUtils.getValue(result,urlConfig.getTotalRecordNumRegExp(),1);
            }
            returnMap.put(IAct6Constant.AJAX_TOTAL_RECORD,totalRecordNum);
        }catch (Exception e){
            returnMap.put(IAct6Constant.AJAX_STATUS,IAct6Constant.AJAX_RESULT_FAIL);
            returnMap.put(IAct6Constant.AJAX_MESSAGE,e.getMessage());
        }
        return returnMap;
    }
    /**
     * 获取机构列表
     *
     */
    @RequestMapping(value = "/listOrganizations/{pageSize}/{pageNo}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public Map<String,Object> listOrganizations(@PathVariable("pageSize") String  pageSize, @PathVariable("pageNo") String pageNo)  {
        Map<String,Object> returnMap = new HashMap<>();
        try {
            //调用外部url获取机构列表
            UrlProperties.UrlConfigInfo urlConfig = urlConfigInfo.getConfigByType(IAct6Constant.API_TYPE_ORGANIZATION);
            if (StringUtils.isEmpty(urlConfig.getUrl()))
                throw new Exception("url 尚未配置!");
            //判断http方法
            String result;
            if (IAct6Constant.HTTP_METHOD_POST.equalsIgnoreCase(urlConfig.getMethod())) {
                //附加翻页参数
                Map<String, String> parameter = new HashMap<>();
                parameter.put(urlConfig.getPageNoParameterName(), pageNo);
                parameter.put(urlConfig.getPageSizeParameterName(), pageSize);

                result = HttpClientUtil.post(urlConfig.getUrl(), parameter);
            } else {
                String url;
                url = urlConfig.getUrl();
                //附加翻页参数
                if (urlConfig.getUrl().contains("?")) {
                    url = url + "&" + urlConfig.getPageNoParameterName() + "=" + pageNo + "&" +
                            urlConfig.getPageSizeParameterName() + "=" + pageSize;
                } else {
                    url = url + "?" + urlConfig.getPageNoParameterName() + "=" + pageNo + "&" +
                            urlConfig.getPageSizeParameterName() + "=" + pageSize;
                }
                result = HttpClientUtil.get(url);
            }
            //判断结果是否成功
            if(!result.contains(urlConfig.getSuccessFlag())){
                returnMap.put(IAct6Constant.AJAX_STATUS,IAct6Constant.AJAX_RESULT_FAIL);
                returnMap.put(IAct6Constant.AJAX_MESSAGE,RegExUtils.getValue(result,urlConfig.getMessageRegExp(),1));
                return returnMap;
            }
            //从result提取id,name
            List<Map<String, Object>> returnList = new ArrayList<>();


            List<String> ids = RegExUtils.getValue(result,urlConfig.getIdRegExp());

            List<String> names = RegExUtils.getValue(result,urlConfig.getNameRegExp());
            for(int j=0;j<ids.size();j++) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", ids.get(j));
                if(j<names.size()) {
                    map.put("name", names.get(j));
                }else {
                    map.put("name","");
                }
                returnList.add(map);
            }

            returnMap.put(IAct6Constant.AJAX_STATUS,IAct6Constant.AJAX_RESULT_SUCCESS);
            returnMap.put(IAct6Constant.AJAX_LIST,returnList);
            //提取总记录数
            String totalRecordNum="0";
            if(!StringUtils.isEmpty(urlConfig.getTotalRecordNumRegExp())) {
                totalRecordNum = RegExUtils.getValue(result,urlConfig.getTotalRecordNumRegExp(),1);
            }
            returnMap.put(IAct6Constant.AJAX_TOTAL_RECORD,totalRecordNum);
        }catch (Exception e){
            returnMap.put(IAct6Constant.AJAX_STATUS,IAct6Constant.AJAX_RESULT_FAIL);
            returnMap.put(IAct6Constant.AJAX_MESSAGE,e.getMessage());
        }
        return returnMap;
    }
    @RequestMapping(value = "/models/{pageSize}/{pageNo}", method = RequestMethod.GET)
    public Map<String,Object> models(@PathVariable("pageSize") int  pageSize, @PathVariable("pageNo") int pageNo,@RequestParam Map<String, String> allRequestParams) {
        ModelQuery modelQuery = this.repositoryService.createModelQuery();
        Map<String,Object> returnMap = new HashMap<>();

        if (allRequestParams.containsKey("id")) {
            modelQuery.modelId((String)allRequestParams.get("id"));
        }

        if (allRequestParams.containsKey("category")) {
            modelQuery.modelCategory((String)allRequestParams.get("category"));
        }

        if (allRequestParams.containsKey("categoryLike")) {
            modelQuery.modelCategoryLike((String)allRequestParams.get("categoryLike"));
        }

        if (allRequestParams.containsKey("categoryNotEquals")) {
            modelQuery.modelCategoryNotEquals((String)allRequestParams.get("categoryNotEquals"));
        }

        if (allRequestParams.containsKey("name")) {
            modelQuery.modelName((String)allRequestParams.get("name"));
        }

        if (allRequestParams.containsKey("nameLike")) {
            modelQuery.modelNameLike((String)allRequestParams.get("nameLike"));
        }

        if (allRequestParams.containsKey("key")) {
            modelQuery.modelKey((String)allRequestParams.get("key"));
        }

        if (allRequestParams.containsKey("version")) {
            modelQuery.modelVersion(Integer.valueOf((String)allRequestParams.get("version")));
        }

        boolean withoutTenantId;
        if (allRequestParams.containsKey("latestVersion")) {
            withoutTenantId = Boolean.valueOf((String)allRequestParams.get("latestVersion"));
            if (withoutTenantId) {
                modelQuery.latestVersion();
            }
        }

        if (allRequestParams.containsKey("deploymentId")) {
            modelQuery.deploymentId((String)allRequestParams.get("deploymentId"));
        }

        if (allRequestParams.containsKey("deployed")) {
            withoutTenantId = Boolean.valueOf((String)allRequestParams.get("deployed"));
            if (withoutTenantId) {
                modelQuery.deployed();
            } else {
                modelQuery.notDeployed();
            }
        }

        if (allRequestParams.containsKey("tenantId")) {
            modelQuery.modelTenantId((String)allRequestParams.get("tenantId"));
        }

        if (allRequestParams.containsKey("tenantIdLike")) {
            modelQuery.modelTenantIdLike((String)allRequestParams.get("tenantIdLike"));
        }

        if (allRequestParams.containsKey("withoutTenantId")) {
            withoutTenantId = Boolean.valueOf((String)allRequestParams.get("withoutTenantId"));
            if (withoutTenantId) {
                modelQuery.modelWithoutTenantId();
            }
        }
        int startIndex;
        int endIndex;
        try {
            startIndex = (pageNo - 1) * pageSize;
            endIndex = startIndex + pageSize ;
            List<Model> modes=  modelQuery.listPage(startIndex,endIndex);
            returnMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_SUCCESS);
            returnMap.put(IAct6Constant.AJAX_LIST, modes);
            //提取总记录数
            long totalRecordNum = 0;
            totalRecordNum = modelQuery.count();
            returnMap.put(IAct6Constant.AJAX_TOTAL_RECORD, totalRecordNum);
        }catch(Exception e){
            returnMap.put(IAct6Constant.AJAX_STATUS, IAct6Constant.AJAX_RESULT_FAIL);
            returnMap.put(IAct6Constant.AJAX_MESSAGE,"查询模型报错");
        }
        return returnMap;
    }
}
