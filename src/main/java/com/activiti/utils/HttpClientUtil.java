/**
 * 
 */
package com.activiti.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.activiti.IAct6Constant;
import com.activiti.model.ActExtAPIVo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
//import org.quickbundle.project.RmProjectHelper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


/**
 * HttpClient工具类
 * 
 * @author dfhc
 */
public class HttpClientUtil {
    /**
     * 根据api 值对象执行http请求
     * @param apiVo
     * @param values
     * @return
     * @throws Exception
     */
    public static String executeApi(ActExtAPIVo apiVo, Map<String, Object> values) throws Exception {
        // 定义httpClient的实例
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String retVal;
        try {
            HttpRequestBase httpRequest;
            String method = apiVo.getMethod();
            if(method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
                httpRequest=new HttpPost(apiVo.getCommitService());
            }else if(method.equalsIgnoreCase(HttpGet.METHOD_NAME)){
                httpRequest = new HttpGet(apiVo.getCommitService());
            }else if(method.equalsIgnoreCase(HttpPut.METHOD_NAME)){
                httpRequest = new HttpPut(apiVo.getCommitService());
            }else if(method.equalsIgnoreCase(HttpDelete.METHOD_NAME)){
                httpRequest = new HttpDelete(apiVo.getCommitService());
            }else
                throw new Exception("不支持的提交方式:"+method);
            //设置参数或表体
            if(IAct6Constant.REQUEST_PARAMETER_FORMAT_PARAMETER.equalsIgnoreCase(apiVo.getRequestParameterFormat())){
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				//接口定义残数
            	if(StringUtils.isNotEmpty(apiVo.getRequestParameterSet()) && !IAct6Constant.EMPTY_JSON_ARRAY.equals(apiVo.getRequestParameterSet())) {
					JSONArray jsonArray = JSON.parseArray(apiVo.getRequestParameterSet());
            		for(int i=0;i<jsonArray.size();i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);

						String name = jsonObject.getString(IAct6Constant.API_TABLE_PROPERTY_NAME);
						String value = jsonObject.getString(IAct6Constant.API_TABLE_PROPERTY_VALUE);
						nvps.add(new BasicNameValuePair(name, value == null ? "" : value.toString()));
					}
				}
            	//业务参数
				if(!values.isEmpty()){
					Iterator<String> itKeys = values.keySet().iterator();
					while (itKeys.hasNext()) {
						String key = itKeys.next();
						Object val = values.get(key);
						nvps.add(new BasicNameValuePair(key, val == null ? "" : val.toString()));
					}
				}
                if(!nvps.isEmpty()){
                    if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
                        ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(nvps,
                                apiVo.getContentEncode()));
                    } else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
                        //参数拼接到url后面
                        String url = formatUrl(apiVo, nvps);
                        //改变url
                        httpRequest.setURI(new URI(url));
                    } else if (method.equalsIgnoreCase(HttpPut.METHOD_NAME)) {
                        ((HttpPut) httpRequest).setEntity(new UrlEncodedFormEntity(nvps,
                                apiVo.getContentEncode()));
                    } else if (method.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
                        httpRequest = new HttpDelete(apiVo.getCommitService());
                        ((HttpDelete) httpRequest).setURI(new URI(formatUrl(apiVo, nvps)));
                    }
                }
            }else {
                if(StringUtils.isNotEmpty(apiVo.getBodyData())) {
                    if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
                        ((HttpPost) httpRequest).setEntity(new StringEntity(apiVo.getBodyData(),
                                apiVo.getContentEncode()));
                    } else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
                        throw new Exception("不支持bodyData 方式!");
                    } else if (method.equalsIgnoreCase(HttpPut.METHOD_NAME)) {
                        ((HttpPut) httpRequest).setEntity(new StringEntity(apiVo.getBodyData(),
                                apiVo.getContentEncode()));
                    } else if (method.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
                        throw new Exception("不支持bodyData 方式!");
                    }
                }
            }

            //设置表头
            if(StringUtils.isNotEmpty(apiVo.getRequestHeadSet()) && !IAct6Constant.EMPTY_JSON_ARRAY.equals(apiVo.getRequestHeadSet())) {
                JSONArray jsonArray = JSON.parseArray(apiVo.getRequestHeadSet());
                for(int i=0;i<jsonArray.size();i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String name = jsonObject.getString(IAct6Constant.API_TABLE_PROPERTY_NAME);
					String value = jsonObject.getString(IAct6Constant.API_TABLE_PROPERTY_VALUE);
					httpRequest.setHeader(name, value);
				}
            }
            //如果是BodyData 且配置ContentType,则设置表头
            if(IAct6Constant.REQUEST_PARAMETER_FORMAT_BODY_DATA.equalsIgnoreCase(apiVo.getRequestParameterFormat()) &&
                    StringUtils.isNotEmpty(apiVo.getContentType())){
                httpRequest.setHeader(IAct6Constant.CONTENT_TYPE,apiVo.getContentType());
            }
            CloseableHttpResponse response2 = httpclient.execute(httpRequest);

            try {
                //RmProjectHelper.logError("系统调试",response2.getStatusLine());
                HttpEntity entity2 = (HttpEntity) response2.getEntity();
                if(entity2!=null){
                    retVal = EntityUtils.toString(entity2, "UTF-8");
                }else{
                    retVal = null;
                }
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume( entity2);
                //验证应答是否正确，报错抛异常
                if(IAct6Constant.RESPOND_TEXT.equalsIgnoreCase(apiVo.getRespondTestField())){
                    if(retVal!=null){
                        if(!retVal.contains(apiVo.getCorrectAnswerMatches())){
                            //尝试提取错误消息
                            String errMsgText = RegExUtils.getValue(retVal,apiVo.getRespondMessageRegExp(),1);
                            if(StringUtils.isNotEmpty(errMsgText))
                                throw new Exception(errMsgText);
                            else
                                throw new Exception("调用接口"+apiVo.getServiceName()+"失败！无法提取到错误信息。正确应答匹配字符串："+apiVo.getCorrectAnswerMatches()+",提取响应错误消息正则表达式:"+apiVo.getRespondMessageRegExp());
                        }
                    }else
                        throw new Exception("调用接口"+apiVo.getServiceName()+"失败！返回值为空指针!");
                    //如果设置了提取业务主键正则表达式，则从返回值提取
                    if(StringUtils.isNotEmpty(apiVo.getBusinesskeyRegExp()) && retVal!=null){
                        return RegExUtils.getValue(retVal,apiVo.getBusinesskeyRegExp(),1);
                    }else
                        return retVal;
                }else{
                    StringBuilder builder = new StringBuilder();
                    //循环将头部信息拼装为字符串，然后提取
                    for(Header header:response2.getAllHeaders()){
                        builder.append("\"").append(header.getName()).append("\":\"").append(header.getValue()).append("\"\r\n");
                    }
                    if(builder.length()>0){
                        String header = builder.toString();
                        if(!header.contains(apiVo.getCorrectAnswerMatches())){
                            //尝试提取错误消息
                            String errMsgText = RegExUtils.getValue(header,apiVo.getRespondMessageRegExp(),1);
                            if(StringUtils.isNotEmpty(errMsgText))
                                throw new Exception(errMsgText);
                            else
                                throw new Exception("调用接口"+apiVo.getServiceName()+"失败！无法提取到错误消息.正确应答匹配字符串："+apiVo.getCorrectAnswerMatches()+",提取响应错误消息正则表达式:"+apiVo.getRespondMessageRegExp());
                        }
                        //如果设置了提取业务主键正则表达式，则从返回值提取
                        if(StringUtils.isNotEmpty(apiVo.getBusinesskeyRegExp())){
                            return RegExUtils.getValue(header,apiVo.getBusinesskeyRegExp(),1);
                        }else
                            return header;
                    }else
                        throw new Exception("调用接口"+apiVo.getServiceName()+"失败！头部返回值为空!");
                }
            } finally {
                response2.close();
            }
        } finally {
            httpclient.close();
        }

    }

    private static String formatUrl(ActExtAPIVo apiVo, List<NameValuePair> nvps) throws IOException {
        String url = apiVo.getCommitService();
        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nvps,
                apiVo.getContentEncode());
        ByteArrayInputStream inputStream = null;
        byte rbytes[] = null;
        try {
            inputStream = (ByteArrayInputStream) urlEncodedFormEntity.getContent();
            rbytes = new byte[(int) urlEncodedFormEntity.getContentLength()];
            inputStream.read(rbytes);
        }finally {
            if(inputStream!=null){
                inputStream.close();
            }
        }
        String parameter = new String(rbytes);
        if(url.endsWith("?")){
            url = url+"&"+parameter;
        }else {
            url = url +"?"+parameter;
        }
        return url;
    }

    /**
     * 使用post body提交到url
     *
     * @param url  目标url
     * @param  headMap 表头Map
     * @param bodyData 表体数据
     * @throws Exception
     */
	public static String postBody(String url, HashMap<String, String> headMap,String bodyData) throws Exception {
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String retVal;
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(new StringEntity(bodyData,
					IAct6Constant.ENCODE_UTF_8));
			//设置表头
			for (String key : headMap.keySet()) {
				httpPost.setHeader(key, headMap.get(key));
			}

			CloseableHttpResponse response2 = httpclient.execute(httpPost);

			try {
				//RmProjectHelper.logError("系统调试",response2.getStatusLine());
				HttpEntity entity2 = (HttpEntity) response2.getEntity();
				if(entity2!=null){
					retVal = EntityUtils.toString(entity2, "UTF-8");
				}else{
					retVal = null;
				}
				// do something useful with the response body
				// and ensure it is fully consumed
				EntityUtils.consume( entity2);
				return retVal;
			} finally {
				response2.close();
			}
		} finally {
			httpclient.close();
		}


	}

	/**
	 * 使用post 提交到url
	 * 
	 * @param url  目标url
	 * @param bodyData 表体数据
	 * @throws Exception
	 */
	public static String post(String url, Map<String, String> bodyData)
			throws Exception {
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String retVal;
		try {
			HttpPost httpPost = new HttpPost(url);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			Iterator<String> itKeys = bodyData.keySet().iterator();
			while (itKeys.hasNext()) {
				String key = itKeys.next();
				String val = bodyData.get(key);
				nvps.add(new BasicNameValuePair(key, val));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nvps,
					IAct6Constant.ENCODE_UTF_8));
			CloseableHttpResponse response2 = httpclient.execute(httpPost);
			
			try {
				//RmProjectHelper.logError("系统调试",response2.getStatusLine());
				HttpEntity entity2 = (HttpEntity) response2.getEntity();
				if(entity2!=null){
					retVal = EntityUtils.toString(entity2, "UTF-8");
				}else{
					retVal = null;
				}
				// do something useful with the response body
				// and ensure it is fully consumed
				EntityUtils.consume( entity2);
				return retVal;
			} finally {
				response2.close();
			}
		} finally {
			httpclient.close();
		}

	}

	/**
	 * 使用post 提交到url
	 *
	 * @param url  目标url
	 * @param bodyData 表体数据
	 * @throws Exception
	 */
	public static String postMapObject(String url, Map<String, Object> bodyData)
			throws Exception {
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String retVal;
		try {
			HttpPost httpPost = new HttpPost(url);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			Iterator<String> itKeys = bodyData.keySet().iterator();
			while (itKeys.hasNext()) {
				String key = itKeys.next();
				String val = bodyData.get(key)==null?"":bodyData.get(key).toString();
				nvps.add(new BasicNameValuePair(key, val));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nvps,
					IAct6Constant.ENCODE_UTF_8));
			CloseableHttpResponse response2 = httpclient.execute(httpPost);

			try {
				//RmProjectHelper.logError("系统调试",response2.getStatusLine());
				HttpEntity entity2 = (HttpEntity) response2.getEntity();
				if(entity2!=null){
					retVal = EntityUtils.toString(entity2, "UTF-8");
				}else{
					retVal = null;
				}
				// do something useful with the response body
				// and ensure it is fully consumed
				EntityUtils.consume( entity2);
				return retVal;
			} finally {
				response2.close();
			}
		} finally {
			httpclient.close();
		}

	}


	public  static String get(String url) throws IOException{
		
		String returnVal="";
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet=new HttpGet(url);
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();//设置请求和传输超时时间
			httpGet.setConfig(requestConfig);
			try {
				CloseableHttpResponse response2 = httpclient.execute(httpGet);//执行请求
				//RmProjectHelper.logError("response2:", response2);
				HttpEntity entity2 = (HttpEntity) response2.getEntity();
				if(entity2!=null){					
					returnVal = EntityUtils.toString(entity2, "UTF-8");					
				}else{
					returnVal = null;
				}
				
			} catch (ClientProtocolException e) {	
				e.printStackTrace();
				//RmProjectHelper.logError("retVal", e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				//RmProjectHelper.logError("retVal", e.getMessage());				
			}finally{
				if(httpclient!=null){
					httpclient.close();
				}
				
			}
			
		
		return returnVal;
		
	}

//	get重载   获取打开页面头信息
public  static String get(String url,HashMap<String, String> headMap) throws IOException{
		
		String returnVal="";
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpGet httpGet=new HttpGet(url);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();//设置请求和传输超时时间
		httpGet.setConfig(requestConfig);
		
		for (String key : headMap.keySet()) {
			  httpGet.setHeader(key, headMap.get(key));
		}
		try {
			 
			
			CloseableHttpResponse response2 = httpclient.execute(httpGet);//执行请求
			
			//RmProjectHelper.logError("response2:", response2);
			HttpEntity entity2 = (HttpEntity) response2.getEntity();
			if(entity2!=null){					
				returnVal = EntityUtils.toString(entity2, "UTF-8");					
			}else{
				returnVal = null;
			}
			
		} catch (ClientProtocolException e) {	
			e.printStackTrace();
			//RmProjectHelper.logError("retVal", e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			//RmProjectHelper.logError("retVal", e.getMessage());				
		}finally{
			
			if(httpclient!=null){
				
				httpclient.close();
			}
			
		}
			
		
		return returnVal;
		
	}
	
	public static Map<String, Object> parseJSON2Map(String bizData) {
		Map<String, Object> ret = new HashMap<String, Object>();
		
    	try{
    		JSONObject bizDataJson = JSONObject.parseObject(bizData);
    		for(Object key:bizDataJson.keySet()){
    			Object value = bizDataJson.get(key);
    			if(value instanceof JSONArray){
    				List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();  
                    Iterator<Object> it = ((JSONArray) value).iterator();

                    while(it.hasNext()){  
                        JSONObject json2 = (JSONObject)it.next();  
                        list.add(parseJSON2Map(json2.toString()));  
                    }  
                    ret.put(String.valueOf(key),list);  
    			}else{
    				ret.put(String.valueOf(key), String.valueOf(value));
    			}
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    		//RmProjectHelper.logError("系统异常", e);
    	}
		return ret;
	}
	
	
	/**
	 * 使用post 提交到url
	 * 
	 * @param url  目标url
	 * @param postDataXML 表体数据
	 * @throws Exception
	 */
	public static String post(String url, String postDataXML)
			throws Exception {
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String retVal;
		try {
			HttpPost httpPost = new HttpPost(url);
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();//设置请求和传输超时时间
			httpPost.setConfig(requestConfig);
		    StringEntity postEntity = new StringEntity(postDataXML, "UTF-8");
	        httpPost.addHeader("Content-Type", "text/xml");
	        httpPost.setEntity(postEntity);
			CloseableHttpResponse response2 = httpclient.execute(httpPost);
			
			try {
				//RmProjectHelper.logError("系统调试",response2.getStatusLine());
				HttpEntity entity2 = (HttpEntity) response2.getEntity();
				if(entity2!=null){
					retVal = EntityUtils.toString(entity2, "UTF-8");
				}else{
					retVal = null;
				}
				// do something useful with the response body
				// and ensure it is fully consumed
				EntityUtils.consume( entity2);
				return retVal;
			} finally {
				response2.close();
			}
		} finally {
			httpclient.close();
		}

	}
	
/**
 * get重载   获取打开页面信息
 * @param url
 * @param headMap 头部属性map
 * @param paramMap 请求参数map
 * @return
 * @throws IOException
 */
public  static String get(String url,Map<String, String> headMap,Map<String,Object> paramMap) throws IOException{
		
		String returnVal="";
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		//构造参数串
		StringBuilder buffer = null;
		if(!(paramMap==null || paramMap.isEmpty())){
			int capacity = paramMap.size() * 30;		//设置表单长度30字节*N个请求参数
			
			//参数不为空，在URL后面添加head（“？”）
	        buffer = new StringBuilder(capacity);
			
			//取出Map里面的请求参数，添加到表单String中。每个参数之间键值对之间用“=”连接，参数与参数之间用“&”连接
	        Iterator<Map.Entry<String, Object>> it = paramMap.entrySet().iterator();
	        while (it.hasNext())
	        {
	            Map.Entry<String, Object> entry = it.next();
	            Object key = entry.getKey();
	            buffer.append(key);
	            buffer.append('=');
	            Object value = entry.getValue();
	            buffer.append(value);
	            if (it.hasNext())
	            {
	                buffer.append("&");
	            }
	        }
		}
		//拼接参数
		if(buffer!=null){
			if(url.contains("?")){
				url = url+"&"+buffer.toString();
			}else{
				url = url+"?"+buffer.toString();
			}
		}
		HttpGet httpGet=new HttpGet(url);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();//设置请求和传输超时时间
		httpGet.setConfig(requestConfig);
		
		for (String key : headMap.keySet()) {
			  httpGet.setHeader(key, headMap.get(key));
		}
		try {
			CloseableHttpResponse response2 = httpclient.execute(httpGet);//执行请求
			
			//RmProjectHelper.logError("response2:", response2);
			HttpEntity entity2 = (HttpEntity) response2.getEntity();
			if(entity2!=null){					
				returnVal = EntityUtils.toString(entity2, "UTF-8");					
			}else{
				returnVal = null;
			}
			
		} catch (ClientProtocolException e) {		
			e.printStackTrace();
			//RmProjectHelper.logError("retVal", e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			//RmProjectHelper.logError("retVal", e.getMessage());				
		}finally{
			
			if(httpclient!=null){
				
				httpclient.close();
			}
			
		}
		return returnVal;
	}
	/**
	 * 使用post 提交参数和文件列表到url
	 * 
	 * @param url  目标url
	 * @param bodyData 表体数据
	 * @param header 头数据
	 * @param fileMap 文件map key为文件控制id，value为文件名
	 * @throws Exception
	 */
	public static String post(String url, Map<String, Object> bodyData,Map<String, String> header,Map<String,String> fileMap)
			throws Exception {
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String retVal;
		try {
			HttpPost httpPost = new HttpPost(url);
			//设置请求头
			if(header!=null){
				Set<String> keyset = header.keySet();
				for(String key:keyset){
					httpPost.setHeader(key, header.get(key));
				}
			}
			//添加普通参数
			MultipartEntityBuilder reqBuilder = MultipartEntityBuilder.create();
			Iterator<String> itKeys = bodyData.keySet().iterator();
			while (itKeys.hasNext()) {
				String key = itKeys.next();
				Object val = bodyData.get(key);
				reqBuilder.addPart(key, new StringBody(val==null?"":val.toString(),ContentType.TEXT_PLAIN));
			}
			//循环添加文件
			if(fileMap!=null){
			   Set<String> fileKeySet = fileMap.keySet();
			   for(String key:fileKeySet){
				   FileBody bin = new FileBody(new File(fileMap.get(key)));
				   reqBuilder.addPart(key,bin);
			   }
			}
			httpPost.setEntity(reqBuilder.build());
			CloseableHttpResponse response2 = httpclient.execute(httpPost);
			
			try {
				//RmProjectHelper.logError("系统调试",response2.getStatusLine());
				HttpEntity entity2 = (HttpEntity) response2.getEntity();
				if(entity2!=null){
					retVal = EntityUtils.toString(entity2, "UTF-8");
				}else{
					retVal = null;
				}
				// do something useful with the response body
				// and ensure it is fully consumed
				EntityUtils.consume( entity2);
				return retVal;
			} finally {
				response2.close();
			}
		} finally {
			httpclient.close();
		}
	
	}
	/**
	 * 使用post 提交参数和流列表到url
	 * 
	 * @param url  目标url
	 * @param map 表体数据
	 * @param header 头数据
	 * @param inStreamMap 输入流map key为文件控制id，value为流对象
	 * @throws Exception
	 */
	public static String postInStream(String url, Map<String, Object> map,Map<String, String> header,Map<String,InputStream> inStreamMap)
			throws Exception {
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String retVal;
		try {
			HttpPost httpPost = new HttpPost(url);
			//设置请求头
			if(header!=null){
				Set<String> keyset = header.keySet();
				for(String key:keyset){
					httpPost.setHeader(key, header.get(key));
				}
			}
			//添加普通参数
			MultipartEntityBuilder reqBuilder = MultipartEntityBuilder.create();
			Iterator<String> itKeys = map.keySet().iterator();
			while (itKeys.hasNext()) {
				String key = itKeys.next();				
				Object val = map.get(key);
				reqBuilder.addPart(key, new StringBody(val==null?"":val.toString(),ContentType.TEXT_PLAIN));
			}
			//循环添加流
			if(inStreamMap!=null){
			   Set<String> fileKeySet = inStreamMap.keySet();
			   for(String key:fileKeySet){
				   //InputStreamBody bin = new InputStreamBody(inStreamMap.get(key),ContentType.DEFAULT_BINARY);
				   InputStreamBody bin = new InputStreamBody(inStreamMap.get(key),key);
				   reqBuilder.addPart(key,bin);
			   }
			}
			httpPost.setEntity(reqBuilder.build());
			CloseableHttpResponse response2 = httpclient.execute(httpPost);
			
			try {
				//RmProjectHelper.logError("系统调试",response2.getStatusLine());
				HttpEntity entity2 = (HttpEntity) response2.getEntity();
				if(entity2!=null){
					retVal = EntityUtils.toString(entity2, "UTF-8");
				}else{
					retVal = null;
				}
				// do something useful with the response body
				// and ensure it is fully consumed
				EntityUtils.consume( entity2);
				return retVal;
			} finally {
				response2.close();
			}
		} finally {
			httpclient.close();
		}
	
	}	
	

	
	
	private Map<String, String> makeHeaderMap(String url,
			String sessionId) {
		Map<String,String> hashMap = new HashMap<String,String>();
		hashMap.put("Host", "127.0.0.1");
		hashMap.put("User-Agent",
				"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:34.0) Gecko/20100101 Firefox/34.0");
		hashMap.put("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		hashMap.put("Accept-Encoding", "gzip, deflate");
		hashMap.put("Accept-Language",
				"zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		hashMap.put("Connection", "keep-alive");
		hashMap.put("Referer", url);
		hashMap.put("Cookie", "JSESSIONID="+sessionId);
		return hashMap;
	}
	
	
	/**
	 * 使用post 提交参数和文件列表到url
	 * 
	 * @param url  目标url
	 * @param bodyData 表体数据
	 * @throws Exception
	 */
	public static String postReportGenerate(String url, Map<String, Object> bodyData)//,Map<String, String> header
			throws Exception {
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String retVal;
		try {
			HttpPost httpPost = new HttpPost(url);
			//设置请求头
//			if(header!=null){
//				Set<String> keyset = header.keySet();
//				for(String key:keyset){
//					httpPost.setHeader(key, header.get(key));
//				}
//			}
			//添加普通参数
			MultipartEntityBuilder reqBuilder = MultipartEntityBuilder.create();
			Iterator<String> itKeys = bodyData.keySet().iterator();
			while (itKeys.hasNext()) {
				String key = itKeys.next();
				Object val = bodyData.get(key);
				reqBuilder.addPart(key, new StringBody(val==null?"":val.toString(),ContentType.TEXT_PLAIN));
			}
			
			httpPost.setEntity(reqBuilder.build());
			CloseableHttpResponse response2 = httpclient.execute(httpPost);
			
			try {
				//RmProjectHelper.logError("系统调试",response2.getStatusLine());
				HttpEntity entity2 = (HttpEntity) response2.getEntity();
				if(entity2!=null){
					retVal = EntityUtils.toString(entity2, "UTF-8");
				}else{
					retVal = null;
				}
				EntityUtils.consume( entity2);
				return retVal;
			} finally {
				response2.close();
			}
		} finally {
			httpclient.close();
		}
	
	}
	
	
	/**
	 * 使用post 提交参数和文件列表到url
	 * 
	 * @param url  目标url
	 * @param bodyData 表体数据
	 * @param header 头数据
	 * @throws Exception
	 */
	public static String postReportGenerate2(String url, Map<String, Object> bodyData,Map<String, String> header)
			throws Exception {
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String retVal;
		try {
			HttpPost httpPost = new HttpPost(url);
			//设置请求头
//			if(header!=null){
//				Set<String> keyset = header.keySet();
//				for(String key:keyset){
//					httpPost.setHeader(key, header.get(key));
//				}
//			}
			//添加普通参数
			MultipartEntityBuilder reqBuilder = MultipartEntityBuilder.create();
			Iterator<String> itKeys = bodyData.keySet().iterator();
			while (itKeys.hasNext()) {
				String key = itKeys.next();
				Object val = bodyData.get(key);
				reqBuilder.addPart(key, new StringBody(val==null?"":val.toString(),ContentType.TEXT_PLAIN));
			}
			
			httpPost.setEntity(reqBuilder.build());
			CloseableHttpResponse response2 = httpclient.execute(httpPost);
			
			try {
				//RmProjectHelper.logError("系统调试",response2.getStatusLine());
				HttpEntity entity2 = (HttpEntity) response2.getEntity();
				if(entity2!=null){
					retVal = EntityUtils.toString(entity2, "UTF-8");
				}else{
					retVal = null;
				}
				EntityUtils.consume( entity2);
				return retVal;
			} finally {
				response2.close();
			}
		} finally {
			httpclient.close();
		}
	
	}
}
