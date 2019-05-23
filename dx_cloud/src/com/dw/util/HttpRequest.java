package com.dw.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.json.JSONException;


public class HttpRequest {
	 /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
        	String r = URLEncoder.encode(param, "UTF-8");//增加 UTF-8 的URL 的修改
            String urlNameString = url + "?" + param;
            System.out.println("url1:：：：："+urlNameString);
            URL realUrl = new URL(urlNameString);
            System.out.println("url2:：：：："+urlNameString+"realUrl2::::"+realUrl);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
           //设置cookie
           // connection.setRequestProperty("Cookie","htl=htl");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));  
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            System.out.println("url"+url+"realUrl:"+realUrl);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    } 
    public static void main(String[] args) throws JSONException {
		//String s=HttpRequest.sendGet("http://10.37.31.24:8899/BDOP/auditRights/buttonRequset", "roleId=1&menuId=1003");
		//String s=HttpRequest.sendGet("http://10.37.31.24:8899/BDOP/auditRights/buttonRequset", "roleId=1&Pid=881");
		//String s=HttpRequest.sendGet("http://10.37.31.24:8899/BDOP/auditRights/provinceRequest", "roleId=1&buttonId=217");
		//String s=HttpRequest.sendGet("http://10.37.31.24:8899/BDOP/auditRights/checkRights", "roleId=1&buttonId=313&secRight=2");
		//http://localhost:8086/BDOP/auditRights/checkRights?roleId=1&menuId=226,230&provs=843;831,832,833
		//String s=HttpRequest.sendGet("http://172.18.0.117:8080/dx_cloud/userlogin/htl.do", "password=qwert");
//    	String s1=HttpRequest.sendGet("http://localhost:8080/dx_cloud/userlogin/htl", "password=1");
//    	 System.out.println("sss1"+s1);
//    	String s=HttpRequest.sendGet("http://localhost:8080/dx_cloud/menpower/menbuttonquery", "roleid=1&menuid=881");
//        System.out.println("sss"+s);
     //   String s2 =HttpRequest.sendGet("http://111.235.158.226:8250/bssadt/dx_cloud/userlogin/checklogin", "loginname=aa");
    	   String s2 =HttpRequest.sendGet("http://localhost:8888/dx_cloud/userlogin/checklogin", "loginname=aa");
        System.out.println("sss2"+s2);
	}
}
