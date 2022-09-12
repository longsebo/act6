/**
 * 
 */
package com.activiti.utils;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

//import com.dfhc.ISystemConstant;
//import org.quickbundle.project.RmProjectHelper;

/**
 * 文件/目录操作工具类
 * 
 * @author longsebo
 */
public class FileOperateHelper {
	/**
	 * 回车换行
	 */
	public static final String CRLF = "\r\n";
	/**
	 * UTF-8字符集
	 */
	public static final String ENCODE_UTF_8 = "UTF-8";	

	/**
	 * 新建目录
	 * 
	 * @param folderPath
	 *            String 如 c:/fqf
	 * @return boolean
	 * @throws Exception
	 */
	public static void newFolder(String folderPath) throws Exception {

		String filePath = folderPath;
		filePath = filePath.toString();
		File myFilePath = new File(filePath);

		if (!myFilePath.exists()) {
			if (!myFilePath.mkdirs()) {
				throw new Exception("createDir" + folderPath + " fail");
			}
		}
	}

	/**
	 * 新建文件
	 * 
	 * @param filePathAndName
	 *            String 文件路径及名称 如c:/fqf.txt
	 * @param fileContent
	 *            String 文件内容
	 * @return boolean
	 * @throws IOException
	 * @throws Exception
	 */
	public static void newFile(String filePathAndName, String fileContent)
			throws IOException, Exception {

		// 如果为空指针,则不写数据到文件
		if (fileContent != null) {
			FileOutputStream out = new FileOutputStream(filePathAndName);
			try {
				out.write(fileContent.getBytes());
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} else {
			File f = new File(filePathAndName);
			f.createNewFile();
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param filePathAndName
	 *            String 文件路径及名称 如c:/fqf.txt
	 * @param fileContent
	 *            String
	 * @return boolean
	 * @throws Exception
	 */
	public static void delFile(String filePathAndName) throws Exception {
		String filePath = filePathAndName;
		filePath = filePath.toString();
		File myDelFile = new File(filePath);
		if (myDelFile.exists()) {
			if (!myDelFile.delete()) {
				throw new Exception("deleteFile" + filePathAndName + " fail!");
			}
		}
	}

	/**
	 * 删除文件夹
	 * 
	 * @param filePathAndName
	 *            String 文件夹路径及名称 如c:/fqf
	 * @param fileContent
	 *            String
	 * @return boolean
	 * @throws Exception
	 */
	public static void delFolder(String folderPath) throws Exception {
		delAllFile(folderPath); // 删除完里面所有内容
		String filePath = folderPath;
		filePath = filePath.toString();
		File myFilePath = new File(filePath);
		if (!myFilePath.delete()) { // 删除空文件夹
			throw new Exception("deleteFile " + folderPath + " fail");
		}
	}

	/**
	 * 删除文件夹里面的所有文件
	 * 
	 * @param path
	 *            String 文件夹路径 如 c:/fqf
	 * @throws Exception
	 */
	public static void delAllFile(String path) throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		if(tempList!=null){
			for (int i = 0; i < tempList.length; i++) {
				if (path.endsWith(File.separator)) {
					temp = new File(path + tempList[i]);
				} else {
					temp = new File(path + File.separator + tempList[i]);
				}
				if (temp.isFile()) {
					if (!temp.delete()) {
						throw new Exception("deleteFile" + temp.getName() + " fail");
					}
				}
				if (temp.isDirectory()) {
					delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
					delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				}
			}
		}
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 * @throws Exception
	 */
	public static void copyFile(String oldPath, String newPath)
			throws Exception {

		int bytesum = 0;
		int byteread = 0;
		File oldfile = new File(oldPath);
		if (oldfile.exists()) { // 文件存在时
			InputStream inStream = new FileInputStream(oldPath); // 读入原文件
			FileOutputStream fs = new FileOutputStream(newPath);
			byte[] buffer = new byte[10240];
			try {
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					fs.write(buffer, 0, byteread);
				}
			} finally {
				if (inStream != null){
					inStream.close();
				}
				if (fs != null){
					fs.close();
				}
			}
		}
	}

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf/ff
	 * @return boolean
	 * @throws IOException
	 * @throws Exception
	 */
	public static void copyFolder(String oldPath, String newPath)
			throws IOException, Exception {
		File mkdirsfile = new File(newPath);
		if (!mkdirsfile.mkdirs()) {
			throw new Exception("createDir" + newPath + " fail!");
		}

		File a = new File(oldPath);
		String[] file = a.list();
		File temp = null;
		if(file!=null){
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}
	
				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath + "/"
							+ (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		}
	}

	/**
	 * 移动文件到指定目录
	 * 
	 * @param oldPath
	 *            String 如：c:/fqf.txt
	 * @param newPath
	 *            String 如：d:/fqf.txt
	 * @throws Exception
	 */
	public static void moveFile(String oldPath, String newPath)
			throws Exception {
		copyFile(oldPath, newPath);
		delFile(oldPath);

	}

	/**
	 * 移动文件到指定目录
	 * 
	 * @param oldPath
	 *            String 如：c:/fqf.txt
	 * @param newPath
	 *            String 如：d:/fqf.txt
	 * @throws IOException
	 * @throws Exception
	 */
	public static void moveFolder(String oldPath, String newPath)
			throws IOException, Exception {
		copyFolder(oldPath, newPath);
		delFolder(oldPath);

	}

	/**
	 * 判断是否为目录
	 * 
	 * @param path
	 *            目录或者文件名
	 * @return 是目录返回true;否则返回false
	 */
	public static boolean isFolder(String path) {
		if (path != null) {
			File file = new File(path);
			return file.isDirectory();
		} else{
			return false;
		}
	}

	/**
	 * 判断目录是否存在
	 * 
	 * @param path
	 *            目录或者文件名
	 * @return 是目录返回true;否则返回false
	 */
	public static boolean isExists(String path) {
		if (path != null) {
			File file = new File(path);
			return file.exists();
		} else{
			return false;
		}
	}

	/**
	 * 搜索目录以及下级目录文件
	 * 
	 * @param path
	 * @param logFileFilter
	 * @return
	 */
	public static List<String> searchFile(String path, FilenameFilter fileFilter) {
		List<String> retList = new ArrayList<String>();
		if (isFolder(path)) {
			File file = new File(path);
			File[] subFiles = file.listFiles(fileFilter);
			if(subFiles!=null){
				for (int i = 0; i < subFiles.length; i++) {
					if (subFiles[i].isFile()) {
						retList.add(subFiles[i].getAbsolutePath());
					} else {
						retList.addAll(searchFile(subFiles[i].getAbsolutePath(),
								fileFilter));
					}
				}
			}
		}
		return retList;
	}

	/**
	 * 根据文件名获取文件路径
	 * 
	 * @param fullPathFileName
	 * @return
	 */
	public static String getPath(String fullPathFileName) {
		int pos;
		pos = fullPathFileName.lastIndexOf("\\");
		if (pos == -1) {
			pos = fullPathFileName.lastIndexOf("/");
			if (pos == -1) {
				return fullPathFileName;
			}
		}else{
			//查找最靠后的/或\\
			int pos1 = fullPathFileName.lastIndexOf("/");
			if(pos1>pos){
				pos = pos1;
			}
		}
		return fullPathFileName.substring(0, pos);
	}


	/**
	 * 获取文件长度
	 * 
	 * @param fullPathFileName
	 * @return
	 * @throws Exception
	 */
	public static long getFileLength(String fullPathFileName) throws Exception {
		File file = new File(fullPathFileName);
		try {
			if (file.exists()) {
				return file.length();
			} else {
				return 0L;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 获取子目录列表,不递归
	 * 
	 * @param parentPath
	 *            父目录
	 * @return
	 * @throws Exception
	 */
	public static List<String> getSubPath(String parentPath) throws Exception {
		List<String> subPaths = new ArrayList<String>();
		if (parentPath == null || parentPath.length() == 0){
			throw new Exception("parentPathIsEmpty");
		}
		File file = null;
		file = new File(parentPath);
		File[] files = file.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					subPaths.add(files[i].getAbsolutePath());
				}
			}
		}
		return subPaths;
	}

	/**
	 * 获取最末级目录
	 * 
	 * @param path
	 * @return
	 */
	public static String getLastLevelPath(String path) {
		int pos;
		pos = path.lastIndexOf('\\');
		if (pos == -1) {
			pos = path.lastIndexOf('/');
			if (pos == -1) {
				return path;
			} else {
				return path.substring(pos + 1);
			}
		} else {
			int pos1;
			pos1 = path.lastIndexOf('/');
			if (pos1 == -1) {
				return path.substring(pos + 1);
			} else {
				if (pos1 > pos) {
					return path.substring(pos1 + 1);
				} else {
					return path.substring(pos + 1);
				}
			}
		}

	}

	/**
	 * @description 将指定文件内容根据指定编码写入返回字符串
	 * @param file
	 * @param encoding
	 * @return 包含文件内容的字符串
	 * @throws Exception
	 */
	public static String readFileToString(File file, String encoding)
			throws Exception {
		try {
			return FileUtils.readFileToString(file, encoding);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	/**
	 * @description 将指定字符串根据指定编码写入文件
	 * @param file
	 * @param data
	 * @param encoding
	 * @throws Exception
	 */
	public static void writeStringToFile(File file, String data, String encoding)
			throws Exception {
		FileUtils.writeStringToFile(file, data, encoding);
	}



	/**
	 * @description 查找指定路径directory中含有extensions[]关键字的文件集合
	 * @param directory
	 *            指定查找路径
	 * @param extensions
	 *            查找关键子 e.g {"java","xml"} ; null表示查询所有文件
	 * @param recursive
	 *            是否查询子文件夹
	 * @return 指定路径directory中含有extensions[]关键字的文件集合
	 */
	public static Collection<File> listFiles(File directory,
			String extensions[], boolean recursive) {
		return FileUtils.listFiles(directory, extensions, recursive);
	}

	public static List<String> getFileNamesForLinux(String path) {
		List<String> fileNames = new ArrayList<String>();
		try {
			String command = "ls " + path;
			BufferedReader bufReader = null;
			Process process = null;
			InputStreamReader file = null;
			InputStream in = null;
			try {
				String outputString;
				process = Runtime.getRuntime().exec(command);
				in = process.getInputStream();
				file = new InputStreamReader(in, "UTF-8");
				bufReader = new BufferedReader(file);
				while ((outputString = bufReader.readLine()) != null) {
					System.out.println(outputString);
					// 转为utf-8
					// String convString = new
					// String(outputString.getBytes(),ISystemConstant.EPP_CHARSET);
					// System.out.println("convrt filename:"+convString);
					fileNames.add(outputString);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if (file != null) {
						file.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if (bufReader != null) {
						bufReader.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if (process != null) {
						process.destroy();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileNames;
	}

	/**
	 * 从绝对文件路径获取文件名
	 * 
	 * @param fullFileName
	 * @return 文件名
	 */
	public static String getFileName(String fullFileName) {
		if (fullFileName == null || fullFileName.trim().length() == 0){
			return "";
		}
		int lastpos;
		lastpos = getLastPathIndicatePos(fullFileName);
		if (lastpos == -1) {
			return fullFileName;
		}
		return fullFileName.substring(lastpos + 1);
	}

	/**
	 * 获取最后路径标识位置
	 * 
	 * @param fullFileName
	 *            全文件路径
	 * @return 返回最后标识位置
	 */
	private static int getLastPathIndicatePos(String fullFileName) {
		int lastpos;
		lastpos = fullFileName.lastIndexOf('\\');
		if (lastpos == -1) {
			lastpos = fullFileName.lastIndexOf('/');
		} else {
			int lastpos1 = fullFileName.lastIndexOf('/');
			if (lastpos1 > lastpos) {
				lastpos = lastpos1;
			}
		}

		return lastpos;
	}

	/**
	 * 获取文件名后缀
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileSuffix(String fileName) {
		if (fileName != null) {
			int pos;
			pos = fileName.lastIndexOf(".");
			if (pos == -1) {
				return "";
			} else {
				return fileName.substring(pos);
			}
		} else {
			return null;
		}
	}

	/**
	 * 使用linux命令获取文件名
	 * 
	 * @param path
	 * @return
	 */

	/**
	 * 获取文件名前缀.例如c:\1.txt返回1
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFilePrefix(String fileName) {
		if (fileName != null) {
			String simpleFileName;
			simpleFileName = getFileName(fileName);
			int pos;
			pos = simpleFileName.lastIndexOf(".");
			if (pos == -1) {
				return simpleFileName;
			} else {
				return simpleFileName.substring(0, pos);
			}
		} else {
			return null;
		}
	}
	/**
	 * 判断最后那段是否完整
	 * @param nativeSegment
	 * @return  完整返回true;否则返回false
	 */
	private static boolean lastSegmentIsComplete(String nativeSegment,String splitStr) {
		int pos = nativeSegment.lastIndexOf(splitStr);
		if(pos==-1){
			return false;
		}
		//截取从pos到结尾
		String remainder = nativeSegment.substring(pos+1);
		Pattern isCompletePattern;
		
		String regEx="[A-Za-z0-9]+";
		isCompletePattern = Pattern.compile(regEx);
		
		Matcher matcher = isCompletePattern.matcher(remainder);
		// 查找字符串中是否有数字或字符,如果有则不完整；否则认为完整
		return !matcher.find();
		
	}
	/**
	 * 获取文件列表
	 */
	public static List<MultipartFile>   listMultipartFile(HttpServletRequest request){
		List<MultipartFile>  listFile=null;
	    //将当前上下文初始化给  CommonsMutipartResolver （多部分解析器）
	    CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(request.getSession().getServletContext());
	    //检查form中是否有enctype="multipart/form-data"
	    if(multipartResolver.isMultipart(request)){
	    	//将request变成多部分request
	        MultipartHttpServletRequest multiRequest=(MultipartHttpServletRequest)request;
	        //获取multiRequest 中所有的文件名
	        Iterator<String> iter=multiRequest.getFileNames();
	        if(iter.hasNext()){
	        	listFile=new ArrayList<MultipartFile>();
	        }
	        while(iter.hasNext()){
	        	//一次遍历所有文件
	            MultipartFile file=multiRequest.getFile(iter.next().toString());
	            if(file!=null){
	            	listFile.add(file);
	            }
	        }		
	    }
	    
	    return listFile;
	}
	
	/**
	 * 获取WebContent目录
	 * @return
	 */
	
	public static String getRealPath(HttpServletRequest request){
		return request.getSession().getServletContext().getRealPath("\\"); 
	}
	/**
	 * 判断输入流是否为图片文件流
	 * @param stream
	 * @return
	 */
	public static boolean isImage(InputStream stream){
		try {  
		    Image image=ImageIO.read(stream);  
		    if (image == null) {  
		        return false;  
		    }  
		    image = null;
		    return true;
		} catch(IOException ex) {  
		    return true;  
		}  		
	}

	  //按行读取文本文件
	  public static List<String> readLines(File f, String encoding) throws IOException{
		  return FileUtils.readLines(f, encoding);
	  }	
}


