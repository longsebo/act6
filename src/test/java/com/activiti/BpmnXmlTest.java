package com.activiti;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;
import javax.el.*;
//import javax.ul.*;
import de.odysseus.el.*;
import de.odysseus.el.util.*;
/**
 * bpmn xml文件解析
 */
public class BpmnXmlTest {
    @Test
    public void testXml() throws IOException {
        InputStreamReader isr = null;
        String fileName="d:\\temp\\act6xml.xml";
        char[] buff;
        try {
            isr = new InputStreamReader(new FileInputStream(
                    fileName), "UTF-8");
            buff = new char[4096];
            isr.read(buff);
            String fileContent;
            fileContent = new String(buff);
            String regExp="<process id=\"Process_1\"(.+?)>";
            System.out.println(this.getValue(fileContent,regExp,1));
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(isr!=null){
                isr.close();
            }
        }
    }
    /**
     *   通过正则表达式提取串中值
     * @param orgStr 原始串
     * @param regEx  正则表达式
     * @param num  组索引
     * @return  没有找到，返回空串
     */
    public  String getValue(String orgStr,String regEx,int num) {
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
    @Test
    public void testIntToDecimal(){
        double i = 100;
        double d;
        d= i/100;
        String v = String.valueOf(d);
        System.out.println(v);
    }
    @Test
    public void testBoolPutToMap(){
        Map<String,Object> temp =null;
        temp = new HashMap<String,Object>();

        temp.put(IAct6Constant.PREDEFINED_IS_PASS, false);
        System.out.println(temp);
    }
    @Test
    public void testConditionExp(){
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        context.setVariable(IAct6Constant.PREDEFINED_IS_PASS, factory.createValueExpression("0", String.class));
        ValueExpression e = factory.createValueExpression(context, "${ isPass == 0}", boolean.class);
        Object result=e.getValue(context);
        System.out.println(result);
    }
}
