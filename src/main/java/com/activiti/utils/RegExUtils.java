/**
 * 
 */
package com.activiti.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Administrator
 * Jmeter 的正则表达式函数太难用
 */
public class RegExUtils {
    /**
     *   通过正则表达式提取串中值
     * @param orgStr 原始串
     * @param regEx  正则表达式
     * @param num  组索引
     * @return  没有找到，返回空串
     */
     public static String getValue(String orgStr,String regEx,int num) {
         Pattern pattern = Pattern.compile(regEx);
         Matcher matcher = pattern.matcher(orgStr);
         int count =0;
         
         while(matcher.find() && count++<=num){
             
//             if(matcher.groupCount()>=num){
//                   System.out.println("match result:"+ matcher.group(num));
//                   return matcher.group(num);
//             }else {
//                 System.out.println("group num:"+num+" out range!");
//                 return "";
//             }
             if(count==num) {
                 if(matcher.groupCount()>=1) {
                     return matcher.group(1);
                 }else {
                     return matcher.group(0);
                 }
             }
         }
         
         System.out.println("no found!");
         return "";
     }

    /**
     *  通过正则表达式提取串中值
     * @param orgStr 原始串
     * @param regEx  正则表达式
     * @return  没有找到，返回空串
     */
    public static List<String> getValue(String orgStr, String regEx) {
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(orgStr);
        List<String> retList = new ArrayList<>();
        while(matcher.find() ){
                if(matcher.groupCount()>=1) {
                    retList.add(matcher.group(1));
                }else {
                    retList.add(matcher.group(0));
                }
        }

        return retList;
    }
}   
