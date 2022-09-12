package com.activiti.utils;

import com.activiti.IAct6Constant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class RegExUtilsTest {

    @Test
    void getValue() {
        String orgStr;
        orgStr="$refApi{登录,\"token\":\"(.+?)\"}";
        System.out.println(IAct6Constant.REF_API_REG_EXP);
        String retVal = RegExUtils.getValue(orgStr, IAct6Constant.REF_API_REG_EXP,1);
        System.out.println(retVal);
    }
    @Test
    void parseJsonArray(){
        JSONArray jsonArray = JSON.parseArray("[{\"name\":\"userName\",\"value\":\"longsebo\",\"_XID\":\"row_36\"},{\"name\":\"passWord\",\"value\":\"111111\",\"_XID\":\"row_37\"}]");
        System.out.println(jsonArray.size());
    }
    @Test
    void testReplaceUrlVarName(){
        String value="111";
        String url="leaveType=${leaveType}";
        String key="leaveType";
        replaceUrlVarName(value,url,key);

    }
    @Test
    void testRelaceyReg(){
        String test="<img src=\"http://localhost:8080/static/img/1.png\">";
        String pattern="<img\\s*([^>]*)\\s*src=\\\"(http://.*?/)(.*?)\\\"\\s*([^>]*)>";
        //System.out.println(regReplaceImage(test,pattern));
    }
    @Test
    void testReplaceFun(){
        String fun=IAct6Constant.REF_API_REG_EXP;
        String test="name=$refApi{登录,2343}";
//        System.out.println(regReplaceImage(test,fun));
    }
    /**
     * 替换url中变量名
     *
     * @param value
     * @param url
     * @param key
     * @return
     */
    private String replaceUrlVarName(String value, String url, String key) {
        if (url.contains("${" + key + "}")) {
            url = url.replaceAll("\\$\\{" + key + "\\}", value);
        }
        return url;

    }
    public String regReplaceImage(String content,String pattern){

        String operatorStr=content;
        Pattern p = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        while(m.find()) {
            //使用分组进行替换
//            for(int i=0;i<m.groupCount();i++){
//                System.out.print(m.group(i));
//            }
            System.out.println(operatorStr.substring(m.start(1),m.end(1)));
            operatorStr = operatorStr.replace(pattern,m.group(0));
            m = p.matcher(operatorStr);
        }
        return operatorStr.toString();
    }
}