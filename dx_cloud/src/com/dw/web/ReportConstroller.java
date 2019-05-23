package com.dw.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.service.jta.platform.internal.SynchronizationRegistryBasedSynchronizationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dw.common.AdminSession;
import com.dw.common.FiledProcFilter;
import com.dw.common.LogFilter;
import com.dw.interceptor.MySessionContext;
import com.dw.model.Organization;
import com.dw.model.ResultMessage;
import com.dw.model.ResultMessage2;
import com.dw.model.ResultMessage4;
import com.dw.model.Role;
import com.dw.model.User;
import com.dw.servce.IUserService;
import com.dw.servce.ILogService;
import com.dw.servce.IReportService;
import com.dw.util.HttpRequest;
import com.dw.util.PasswordUtil;
import com.dw.util.exportReportTemplete;
import com.google.gson.Gson;

@Controller
public class ReportConstroller {

	public final static String FIX_ALL = "固网总计";
	public final static String OTH_ALL = "移网总计";

	@Autowired
	IUserService adminService;
	@Autowired
	IReportService resSer;
	@Autowired
	ILogService loginService;

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/reportTable/{reportEN}", produces = "text/html;charset=UTF-8", params = "fieldNameEN")
	public void getReportInfoByParam(@PathVariable String reportEN, HttpServletRequest request,
			HttpServletResponse response, @RequestParam("fieldVal") String fieldVal,
			@RequestParam("fieldNameEN") String fieldNameEN) {
		// 转码：encodeURI('内容')
		// 解码：new String(fieldNameEN.getBytes("iso8859-1"),"UTF-8");
		Gson json = new Gson();

		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
				Map maplist = new HashMap();
				maplist.put("code", 0);
				maplist.put("msg", "没有通过访问权限");
				String jsonstr = json.toJson(maplist);
				out1.print(jsonstr);
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 获取稽核报表基本信息
		Map<String, String> map = new HashMap<String, String>();
		map.put("report_Name_EN", reportEN);
		Map<String, String> sqlMap = resSer.getReportRunSql(map);
		boolean jsonP = false;
		String cb = request.getParameter("audit");
		if (cb != null) {
			jsonP = true;
			response.setContentType("text/javascript");
		} else {
			response.setContentType("application/x-json");
		}
		PrintWriter out;
		try {
			out = response.getWriter();
			if (jsonP) {
				out.write(cb + "(");
			}
			if (sqlMap != null && sqlMap.size() > 0) {
				String sql = "";
				// 如果该需求为特定需求 那么typechart =1 typechart：1分省 2全国 3其他
				String typechart = request.getParameter("typechart");
				if (typechart != null && "2".equals(typechart)) {
					sql = sqlMap.get("run_all_conent_sql");
				} else {
					sql = sqlMap.get("run_conent_sql");
				}
				StringBuffer sbsql = new StringBuffer(sql);
				// String fval=new String(fieldVal.getBytes("iso8859-1"),"UTF-8");

				String fval = fieldVal;
				FiledProcFilter instance = new FiledProcFilter();
				if (fieldNameEN != null && !"".equals(fieldNameEN)) {
					String[] nameStr = fieldNameEN.replaceAll("'", "").split(",");
					String[] fvalStr = fval.replaceAll("'", "").split(",");
					for (int i = 0; i < nameStr.length; i++) {
						// 判断查询字段是否存在
						map = new HashMap<String, String>();
						map.put("report_ID", sqlMap.get("report_ID"));
						map.put("field_Name_EN", nameStr[i]);
						List<Map<String, String>> ss = resSer.getReportFeldsQuery(map);
						String oper;
						if ((fvalStr.length > i) && (fvalStr[i] != null)) {
							oper = (String) ((Map) ss.get(0)).get("field_operator");
							if (fvalStr[i].indexOf("-") >= 0) {
								String[] qulist = fvalStr[i].split("-");
								// star_变量名 开始 end_变量名结束
								// dts表示where后面不需要做追加
								if (nameStr[i].equals("dt") || nameStr[i].equals("day_id") || nameStr[i].equals("month")
										|| nameStr[i].equals("dts")) {
									if (nameStr[i].equals("dts")) {
										sbsql = new StringBuffer(sbsql.toString()
												.replace("${star_" + nameStr[i].trim() + "}", qulist[0]));
										sbsql = new StringBuffer(sbsql.toString()
												.replace("${end_" + nameStr[i].trim() + "}", qulist[1]));
									} else {
										sbsql.append("  and " + nameStr[i] + ">=" + qulist[0]);
										sbsql.append("  and " + nameStr[i] + "<=" + qulist[1]);
										sbsql = new StringBuffer(sbsql.toString()
												.replace("${star_" + nameStr[i].trim() + "}", qulist[0]));
										sbsql = new StringBuffer(sbsql.toString()
												.replace("${end_" + nameStr[i].trim() + "}", qulist[1]));
									}
								}
							} else {
								sbsql.append("  and " + nameStr[i] + oper + "'" + fvalStr[i] + "' ");
								sbsql = new StringBuffer(
										sbsql.toString().replace("${" + nameStr[i].trim() + "}", fvalStr[i]));
							}
						} else {
							oper = (String) ((Map) ss.get(0)).get("filed_def_operator");
							sbsql.append(" and " + nameStr[i] + "  " + oper + "  ("
									+ ((String) ((Map) ss.get(0)).get("field_def_sql")) + ")");
						}

					}
				}
				if (typechart != null && "all".equals(typechart)) {
					sbsql.append(" " + sqlMap.get("run_all_end_sql"));
				} else {
					sbsql.append(" " + sqlMap.get("run_end_sql"));
				}
				Map maplist = new HashMap();
				String sql1 = "select concat(count(*),'') rows from (" + sbsql.toString() + ") a";
				List<Map<String, String>> countdata = null;
				// 获取报表数据
				List<Map<String, String>> repData = null;
				String pageNo = request.getParameter("pageNo");
				String pageSize = request.getParameter("pageSize");
				String order = request.getParameter("order");
				if (null != order && !"".equals(order) && !"null".equals(order)) {
					sql = "select a.* from (" + sbsql.toString() + ") a where 1 = 1 " + order;
				}
				try {
					if (pageNo != null && !"".equals(pageNo) && !"null".equals(pageNo) && pageSize != null
							&& !"".equals(pageSize) && !"null".equals(pageSize)) {
						countdata = resSer.getReportDataBySql(sql.toString());
						repData = resSer.getReportDataBySql(instance.getPageSql(sbsql.toString(), pageNo, pageSize));
					} else {
						repData = resSer.getReportDataBySql(sbsql.toString());
					}
				} catch (Exception e) {
					maplist.put("msg1", "查询的sql data出现问题" + e.getMessage());
				}
				String fileName = request.getParameter("fileName") + "";
				if (null != fileName && !"".equals(fileName) && !"null".equals(fileName)) {
					fileName = fileName.replace("'", "");
					map = new HashMap<String, String>();
					map.put("report_ID", sqlMap.get("report_ID"));
					List<Map<String, String>> repHead = resSer.getReportTableHeadList(map);
					exportReportTemplete.returnXSSFWorkbookM(response, fileName, repHead, repData);//field_name_CN field_name_EN
				} else {
					if (countdata != null && countdata.size() > 0)
						maplist.put("total", countdata.get(0).get("rows"));
					// 获取报表数据
					setMETHOD(request, reportEN);// 进行初始化 日志参数
					// 获取报表表头信息
					map = new HashMap<String, String>();
					map.put("report_ID", sqlMap.get("report_ID"));

					List<Map<String, String>> repHead = resSer.getReportTableHeadList(map);
					maplist.put("head", repHead);
					if (typechart != null && "1".equals(typechart)) {
						try {
							maplist.put("data", getPronJson(repData));
						} catch (Exception e) {
							maplist.put("data", repData);
							maplist.put("msg1", "数据没有省份字段不能进行分组请把参数 typechart=1 删除!");
						}
					} else {
						maplist.put("data", repData);
					}
					String jsonstr = json.toJson(maplist);
					out.print(jsonstr);
				}
			}
			if (jsonP) {
				out.write(");");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			upLogMethod(request);
		}
	}

	/**
	 * 新页面一样，只是路径不一样
	 * 
	 * @param reportEN
	 * @param request
	 * @param response
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/newreportTable/{reportEN}", produces = "text/html;charset=UTF-8", params = "fieldNameEN")
	public void getNewReportInfoByParam(@PathVariable String reportEN, HttpServletRequest request,
			HttpServletResponse response, @RequestParam("fieldVal") String fieldVal,
			@RequestParam("fieldNameEN") String fieldNameEN) {

		// 判断接口是否注册
		String requestUrl = request.getContextPath() + request.getServletPath();
		ResultMessage result = new ResultMessage();
		Gson json = new Gson();

		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}

		if (null == request.getParameter("iframe") && !checkInterFaceExist(requestUrl, request)) {
			result.setCode("2");
			result.setMsg("存在非法访问，操作已禁止");
			try {
				PrintWriter out = response.getWriter();
				out.print(json.toJson(result));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// 转码：encodeURI('内容')
			// 解码：new String(fieldNameEN.getBytes("iso8859-1"),"UTF-8");
			// 获取稽核报表基本信息
			Map<String, String> map = new HashMap<String, String>();
			map.put("report_Name_EN", reportEN);
			Map<String, String> sqlMap = resSer.getReportRunSql(map);
			boolean jsonP = false;
			String cb = request.getParameter("audit");
			if (cb != null) {
				jsonP = true;
				response.setContentType("text/javascript");
			} else {
				response.setContentType("application/x-json");
			}
			PrintWriter out;
			try {
				out = response.getWriter();
				if (jsonP) {
					out.write(cb + "(");
				}
				if (sqlMap != null && sqlMap.size() > 0) {
					String sql = "";
					// 如果该需求为特定需求 那么typechart =1 typechart：1分省 2全国 3其他
					String typechart = request.getParameter("typechart");
					// 判断该typechar下的值该用户是否有权限
					/*
					 * if(null != typechart && !checkJurExist(typechart,request)){
					 * result.setCode("2"); result.setMsg("存在非法访问，操作已禁止"); out.print(
					 * json.toJson(result)); }else {
					 */
					if (typechart != null && "2".equals(typechart)) {
						sql = sqlMap.get("run_all_conent_sql");
					} else {
						sql = sqlMap.get("run_conent_sql");
					}
					StringBuffer sbsql = new StringBuffer(sql);
					// String fval=new String(fieldVal.getBytes("iso8859-1"),"UTF-8");
					String fval = fieldVal;
					if (fieldNameEN != null && !"".equals(fieldNameEN)) {
						String[] nameStr = fieldNameEN.replaceAll("'", "").split(",");
						String[] fvalStr = fval.replaceAll("'", "").split(",");

						// 判断该key下的值该角色是否有权限
						/*
						 * for(int i=0;i<nameStr.length;i++){ if(nameStr[i].equals("dt") ||
						 * nameStr[i].equals("day_id") || nameStr[i].equals("month") ||
						 * nameStr[i].equals("dts")){ continue; }else{ String keyValue=fvalStr[i];
						 * if(null != typechart && !checkJurExist(typechart,request)){
						 * result.setCode("2"); result.setMsg("存在非法访问，操作已禁止"); break; } } }
						 */
						FiledProcFilter instance = new FiledProcFilter();
						if (!"2".equals(result.getCode())) {
							for (int i = 0; i < nameStr.length; i++) {
								// 判断查询字段是否存在
								map = new HashMap<String, String>();
								map.put("report_ID", sqlMap.get("report_ID"));
								map.put("field_Name_EN", nameStr[i]);
								List<Map<String, String>> ss = resSer.getReportFeldsQuery(map);
								String oper;
								if ((fvalStr.length > i) && (fvalStr[i] != null)) {
									oper = (String) ((Map) ss.get(0)).get("field_operator");
									if (fvalStr[i].indexOf("-") >= 0) {
										String[] qulist = fvalStr[i].split("-");
										// star_变量名 开始 end_变量名结束
										// dts表示where后面不需要做追加
										if (nameStr[i].equals("dt") || nameStr[i].equals("day_id")
												|| nameStr[i].equals("month") || nameStr[i].equals("dts")) {
											if (nameStr[i].equals("dts")) {
												sbsql = new StringBuffer(sbsql.toString()
														.replace("${star_" + nameStr[i].trim() + "}", qulist[0]));
												sbsql = new StringBuffer(sbsql.toString()
														.replace("${end_" + nameStr[i].trim() + "}", qulist[1]));
											} else {
												sbsql.append("  and " + nameStr[i] + ">=" + qulist[0]);
												sbsql.append("  and " + nameStr[i] + "<=" + qulist[1]);
												sbsql = new StringBuffer(sbsql.toString()
														.replace("${star_" + nameStr[i].trim() + "}", qulist[0]));
												sbsql = new StringBuffer(sbsql.toString()
														.replace("${end_" + nameStr[i].trim() + "}", qulist[1]));
											}
										}
									} else {
										sbsql.append("  and " + nameStr[i] + oper + "'" + fvalStr[i] + "' ");
										sbsql = new StringBuffer(
												sbsql.toString().replace("${" + nameStr[i].trim() + "}", fvalStr[i]));
									}
								} else {
									oper = (String) ((Map) ss.get(0)).get("filed_def_operator");
									sbsql.append(" and " + nameStr[i] + "  " + oper + "  ("
											+ ((String) ((Map) ss.get(0)).get("field_def_sql")) + ")");
								}
							}
						}
						if (typechart != null && "all".equals(typechart)) {
							sbsql.append(" " + sqlMap.get("run_all_end_sql"));
						} else {
							sbsql.append(" " + sqlMap.get("run_end_sql"));
						}
						Map maplist = new HashMap();
						String sql1 = "select concat(count(*),'') rows from (" + sbsql.toString() + ") a";
						List<Map<String, String>> countdata = null;
						// 获取报表数据
						List<Map<String, String>> repData = null;
						String pageNo = request.getParameter("pageNo");
						String pageSize = request.getParameter("pageSize");
						String order = request.getParameter("order");
						if (null != order && !"".equals(order) && !"null".equals(order)) {
							sql = "select a.* from (" + sbsql.toString() + ") a where 1 = 1 " + order;
						}
						try {
							if (pageNo != null && !"".equals(pageNo) && !"null".equals(pageNo) && pageSize != null
									&& !"".equals(pageSize) && !"null".equals(pageSize)) {
								countdata = resSer.getReportDataBySql(sql.toString());
								repData = resSer.getReportDataBySql(instance.getPageSql(sbsql.toString(), pageNo, pageSize));
							} else {
								repData = resSer.getReportDataBySql(sbsql.toString());
							}
						} catch (Exception e) {
							maplist.put("msg1", "查询的sql data出现问题" + e.getMessage());
						}
						String fileName = request.getParameter("fileName") + "";
						if (null != fileName && !"".equals(fileName) && !"null".equals(fileName)) {
							fileName = fileName.replace("'", "");
							map = new HashMap<String, String>();
							map.put("report_ID", sqlMap.get("report_ID"));
							List<Map<String, String>> repHead = resSer.getReportTableHeadList(map);
							exportReportTemplete.returnXSSFWorkbookM(response, fileName, repHead, repData);//field_name_CN field_name_EN
						} else {
							if (countdata != null && countdata.size() > 0)
								maplist.put("total", countdata.get(0).get("rows"));
							setMETHOD(request, reportEN);// 进行初始化 日志参数
							// 获取报表表头信息
							map = new HashMap<String, String>();
							map.put("report_ID", sqlMap.get("report_ID"));
							List<Map<String, String>> repHead = resSer.getReportTableHeadList(map);
							maplist.put("head", repHead);
							maplist.put("code", "1");
							maplist.put("msg", "查询成功");

							if (typechart != null && "1".equals(typechart)) {
								maplist.put("data", getPronJson(repData));
							} else {
								maplist.put("data", repData);
							}
						}
						if (!"1".equals(maplist.get("code"))) {
							maplist.put("code", 2);
							maplist.put("msg", "查询失败");
							maplist.put("data", null);
						}
						String jsonstr = json.toJson(maplist);
						out.print(jsonstr);

					} // if (result != 2)

					// }// else
				}
				if (jsonP) {
					out.write(");");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				upLogMethod(request);
			}

		} // else

	}

	/**
	 * 新修改，通过地址进行映射
	 * 
	 * @param reportEN
	 * @param request
	 * @param response
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/getReportTable/{requestUrlMapping}", produces = "text/html;charset=UTF-8", params = "fieldNameEN")
	public void getRportByAddressMapping(@PathVariable String requestUrlMapping, HttpServletRequest request,
			HttpServletResponse response, @RequestParam("fieldVal") String fieldVal,
			@RequestParam("fieldNameEN") String fieldNameEN) {

		// 判断接口是否注册
		String requestUrl = request.getContextPath() + request.getServletPath();
		ResultMessage result = new ResultMessage();
		Gson json = new Gson();
		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}

		MySessionContext context = MySessionContext.getInstance();
		if (context.getInvalidated(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "用户登录超时");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}

		if (null == request.getParameter("iframe") && !checkInterFaceExist(requestUrl, request)) {
			result.setCode("2");
			result.setMsg("存在非法访问，操作已禁止");
			try {
				PrintWriter out = response.getWriter();
				out.print(json.toJson(result));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// 转码：encodeURI('内容')
			// 解码：new String(fieldNameEN.getBytes("iso8859-1"),"UTF-8");
			// 获取稽核报表基本信息
			Map<String, String> requestUrlmap = new HashMap<String, String>();
			String requestType1 = "";
			String requestType2 = "";
			Boolean flag = false;
			if (null != fieldNameEN && null != fieldVal) {
				String[] nameStr = fieldNameEN.replaceAll("'", "").split(",");
				String[] fvalStr = fieldVal.replaceAll("'", "").split(",");
				for (int i = 0; i < nameStr.length; i++) {
					if ("requestType1".equals(nameStr[i])) {
						requestType1 = fvalStr[i];
						flag = true;
						continue;
					} else if ("requestType2".equals(nameStr[i])) {
						requestType2 = fvalStr[i];
						flag = true;
						continue;
					}
				}
			}
			if (!flag) {
				result.setCode("2");
				result.setMsg("请求类型参数有误");
				try {
					PrintWriter out = response.getWriter();
					out.print(json.toJson(result));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			requestUrlmap.put("requestUrlMapping", requestUrlMapping);
			requestUrlmap.put("requestType1", requestType1);
			requestUrlmap.put("requestType2", requestType2);

			Map<String, String> UrlReportMap = resSer.getUrlReportMapping(requestUrlmap);
			String reportEN = "";
			if (null == UrlReportMap) {
				result.setCode("2");
				result.setMsg("未找到映射关系表");
				try {
					PrintWriter out = response.getWriter();
					out.print(json.toJson(result));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				reportEN = UrlReportMap.get("table_url") + "";
			}

			if (!"2".equals(result.getCode())) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("report_Name_EN", reportEN);
				Map<String, String> sqlMap = resSer.getReportRunSql(map);
				boolean jsonP = false;
				String cb = request.getParameter("audit");
				if (cb != null) {
					jsonP = true;
					response.setContentType("text/javascript");
				} else {
					response.setContentType("application/x-json");
				}
				PrintWriter out = null;
				String jsonstr = null;
				String fileName = null;
				try {
					out = response.getWriter();
					if (jsonP) {
						out.write(cb + "(");
					}
					if (sqlMap != null && sqlMap.size() > 0) {
						String sql = "";
						// 如果该需求为特定需求 那么typechart =1 typechart：1分省 2全国 3其他
						String typechart = request.getParameter("typechart");
						// 判断该typechar下的值该用户是否有权限
						/*
						 * if(null != typechart && !checkJurExist(typechart,request)){
						 * result.setCode("2"); result.setMsg("存在非法访问，操作已禁止"); out.print(
						 * json.toJson(result)); }else {
						 */
						if (typechart != null && "2".equals(typechart)) {
							sql = sqlMap.get("run_all_conent_sql");
						} else {
							sql = sqlMap.get("run_conent_sql");
						}
						StringBuffer sbsql = new StringBuffer(sql);
						String fval = fieldVal;
						if (fieldNameEN != null && !"".equals(fieldNameEN)) {
							String[] nameStr = fieldNameEN.replaceAll("'", "").split(",");
							String[] fvalStr = fval.replaceAll("'", "").split(",");
							// 判断该key下的值该角色是否有权限
							for (int i = 0; i < nameStr.length; i++) {
								if (nameStr[i].equals("dt") || nameStr[i].equals("day_id") || nameStr[i].equals("month")
										|| nameStr[i].equals("dts") || "requestType1".equals(nameStr[i])
										|| "requestType2".equals(nameStr[i])) {
									continue;
								} else {
									String keyValue = fvalStr[i];
									if (null != keyValue && !checkJurExist(keyValue, request)) {
										result.setCode("2");
										result.setMsg("存在非法访问，操作已禁止");
										break;
									}
								}
							}
							if (!"2".equals(result.getCode())) {
								for (int i = 0; i < nameStr.length; i++) {
									if ("requestType1".equals(nameStr[i]) || "requestType2".equals(nameStr[i])) {
										continue;
									}
									// 判断查询字段是否存在
									map = new HashMap<String, String>();
									map.put("report_ID", sqlMap.get("report_ID"));
									map.put("field_Name_EN", nameStr[i]);
									List<Map<String, String>> ss = resSer.getReportFeldsQuery(map);
									String oper;
									if ((fvalStr.length > i) && (fvalStr[i] != null)) {
										oper = (String) ((Map) ss.get(0)).get("field_operator");
										if (fvalStr[i].indexOf("-") >= 0) {
											String[] qulist = fvalStr[i].split("-");
											// star_变量名 开始 end_变量名结束
											// dts表示where后面不需要做追加
											if (nameStr[i].equals("dt") || nameStr[i].equals("day_id")
													|| nameStr[i].equals("month") || nameStr[i].equals("dts")) {
												if (nameStr[i].equals("dts")) {
													sbsql = new StringBuffer(sbsql.toString()
															.replace("${star_" + nameStr[i].trim() + "}", qulist[0]));
													sbsql = new StringBuffer(sbsql.toString()
															.replace("${end_" + nameStr[i].trim() + "}", qulist[1]));
												} else {
													sbsql.append("  and " + nameStr[i] + ">=" + qulist[0]);
													sbsql.append("  and " + nameStr[i] + "<=" + qulist[1]);
													sbsql = new StringBuffer(sbsql.toString()
															.replace("${star_" + nameStr[i].trim() + "}", qulist[0]));
													sbsql = new StringBuffer(sbsql.toString()
															.replace("${end_" + nameStr[i].trim() + "}", qulist[1]));
												}
											}
										} else {
											sbsql.append("  and " + nameStr[i] + oper + "'" + fvalStr[i] + "' ");
											sbsql = new StringBuffer(sbsql.toString()
													.replace("${" + nameStr[i].trim() + "}", fvalStr[i]));
										}
									} else {
										oper = (String) ((Map) ss.get(0)).get("filed_def_operator");
										sbsql.append(" and " + nameStr[i] + "  " + oper + "  ("
												+ ((String) ((Map) ss.get(0)).get("field_def_sql")) + ")");
									}
								}

								if (typechart != null && "all".equals(typechart)) {
									sbsql.append(" " + sqlMap.get("run_all_end_sql"));
								} else {
									String runendsql = sqlMap.get("run_end_sql") + "";
									if (!"".equals(runendsql) && !"null".equals(runendsql) && null != runendsql) {
										sbsql.append(" " + sqlMap.get("run_end_sql"));
									}
								}
								Map maplist = new HashMap();
								String sql1 = "select concat(count(*),'') rows from (" + sbsql.toString() + ") a";
								List<Map<String, String>> countdata = null;
								// 获取报表数据
								List<Map<String, String>> repData = null;

								String pageNo = request.getParameter("pageNo");
								String pageSize = request.getParameter("pageSize");
								String order = request.getParameter("sort");

								if (null != pageNo && !"".equals(pageNo) && !"null".equals(pageNo) && null != pageSize
										&& !"".equals(pageSize) && !"null".equals(pageSize)) {
									String nowNum = String.valueOf(Integer.valueOf(pageNo) * Integer.valueOf(pageSize));

									sbsql = new StringBuffer("select * from (").append(sbsql).append(") tab ");
									if (null != order && !"".equals(order) && !"null".equals(order)) {
										sbsql.append(" " + order);
									}
									countdata = resSer.getReportDataBySql(sql1.toString());
									sbsql.append(" limit " + nowNum + "," + pageSize);
								} else if (null != order && !"".equals(order) && !"null".equals(order)) {
									sbsql = new StringBuffer("select * from (").append(sbsql)
											.append(") tab where 1 = 1" + order);
								}

								// 获取报表数据
								repData = resSer.getReportDataBySql(sbsql.toString());
								if (countdata != null && countdata.size() > 0)
									maplist.put("total", countdata.get(0).get("rows"));
								setMETHOD(request, requestUrlMapping);// 进行初始化 日志参数
								fileName = request.getParameter("fileName") + "";
								List<Map<String, String>> repHead = null;
								if (null != fileName && !"".equals(fileName) && !"null".equals(fileName)) {
									// 获取报表表头信息
									fileName = fileName.replace("'", "");
									map = new HashMap<String, String>();
									map.put("report_ID", sqlMap.get("report_ID"));
									repHead = resSer.getReportTableHeadList(map);
//              			        		    		HSSFWorkbook wb = exportReportTemplete.getHSSFWorkbookWithOtherMap(fileName, repHead, repData, null);
									setDOWNMETHOD(request, requestUrlMapping);// 进行初始化 下载日志参数
									exportReportTemplete.returnXSSFWorkbookM(response, fileName, repHead, repData);
//            			           		 			exportReportTemplete.ReturnResponse(response, fileName+".xls", wb);
								} else {
									maplist.put("code", "1");
									maplist.put("msg", "查询成功");

									if (typechart != null && "1".equals(typechart)) {
										try {
											maplist.put("data", getPronJson(repData));
										} catch (Exception e) {
											maplist.put("data", repData);
											maplist.put("msg1", "数据没有省份字段不能进行分组请把参数 typechart=1 删除!");
										}
									} else {
										maplist.put("data", repData);
									}
									if (!"1".equals(maplist.get("code"))) {
										maplist.put("code", 2);
										maplist.put("msg", "查询失败");
										maplist.put("data", null);
									}
									jsonstr = json.toJson(maplist);
									out.print(jsonstr);
								}
							} else {
								out.print(json.toJson(result));
							}
						} // if (result != 2)

						// }// else
					}
//        		    	 if (jsonP) {
//        		    	     out.write(");");
//        		    	 }
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					upLogMethod(request);
					// 在不是权限的问题和前提下 -- 当查询出现错误并且不是下载 后走这个
					if (!"2".equals(result.getCode()) && null == jsonstr
							&& (null == fileName || "".equals(fileName) || "null".equals(fileName))) {
						Map maplist = new HashMap();
						maplist.put("code", 2);
						maplist.put("msg", "查询失败");
						maplist.put("data", null);
						out.print(json.toJson(maplist));
					}
					if (jsonP) {
						out.write(");");
					}
				}
			}
		} // else

	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/reportTable/{reportEN}", produces = "text/html;charset=UTF-8")
	public void getReportParamlist(@PathVariable String reportEN, HttpServletRequest request,
			HttpServletResponse response) {
		Gson json = new Gson();
		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}

		Map<String, String> map = new HashMap<String, String>();
		map.put("report_Name_EN", reportEN);
		// 获取查询报表字段信息
		List<Map<String, String>> repQueryList = resSer.getReportFeldsQuery(map);
		List<Map> repql = null;
		boolean jsonP = false;
		String cb = request.getParameter("audit");
		if (cb != null) {
			jsonP = true;
			response.setContentType("text/javascript;charset=utf-8");
		} else {
			response.setContentType("application/x-json;charset=utf-8");
		}
		PrintWriter out;
		try {
			out = response.getWriter();
			if (jsonP) {
				out.write(cb + "(");
			}
			if (repQueryList != null && repQueryList.size() > 0) {
				repql = new ArrayList<Map>();
				Map reMap = null;
				for (Map<String, String> map2 : repQueryList) {
					reMap = new HashMap();
					reMap.put("dtVal", map2.get("field_name_CN") + "");
					reMap.put("dt", map2.get("field_name_EN") + "");
					String resql = map2.get("field_sql") + "";
					List<Map<String, String>> repData = resSer.getReportDataBySql(resql);
					setMETHOD(request, reportEN);// 进行初始化 日志参数
					reMap.put("data", repData);
					repql.add(reMap);
				}
				String aa = json.toJson(repql);
				out.print(aa);
			}
			if (jsonP) {
				out.write(");");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			upLogMethod(request);
		}
	}

	/*
	 * 新查询参数最大，最小值 dt等
	 * 
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/getReportTableFiled/{requestUrlMapping}", produces = "text/html;charset=UTF-8", params = "fieldNameEN")
	public void getReportTableMapping(@PathVariable String requestUrlMapping, HttpServletRequest request,
			HttpServletResponse response, @RequestParam("fieldVal") String fieldVal,
			@RequestParam("fieldNameEN") String fieldNameEN) {
		Gson json = new Gson();
		Map<String, String> map = new HashMap<String, String>();
		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}

		// 判断接口是否注册
		String requestUrl = request.getContextPath() + request.getServletPath();
		ResultMessage result = new ResultMessage();
		if (null == request.getParameter("iframe") && !checkInterFaceExist(requestUrl, request)) {
			result.setCode("2");
			result.setMsg("存在非法访问，操作已禁止");
			try {
				PrintWriter out = response.getWriter();
				out.print(json.toJson(result));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Map<String, String> requestUrlmap = new HashMap<String, String>();
			String requestType1 = "";
			String requestType2 = "";
			Boolean flag = false;
			if (null != fieldNameEN && null != fieldVal) {
				String[] nameStr = fieldNameEN.replaceAll("'", "").split(",");
				String[] fvalStr = fieldVal.replaceAll("'", "").split(",");
				for (int i = 0; i < nameStr.length; i++) {
					if ("requestType1".equals(nameStr[i])) {
						requestType1 = fvalStr[i];
						flag = true;
						continue;
					} else if ("requestType2".equals(nameStr[i])) {
						requestType2 = fvalStr[i];
						flag = true;
						continue;
					}
				}
			}
			if (!flag) {
				result.setCode("2");
				result.setMsg("请求类型参数有误");
				try {
					PrintWriter out = response.getWriter();
					out.print(json.toJson(result));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			requestUrlmap.put("requestUrlMapping", requestUrlMapping);
			requestUrlmap.put("requestType1", requestType1);
			requestUrlmap.put("requestType2", requestType2);

			Map<String, String> UrlReportMap = resSer.getUrlReportMapping(requestUrlmap);
			String reportEN = "";
			if (null == UrlReportMap) {
				result.setCode("2");
				result.setMsg("未找到映射关系表");
				try {
					PrintWriter out = response.getWriter();
					out.print(json.toJson(result));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				reportEN = UrlReportMap.get("table_url") + "";
				map.put("report_Name_EN", reportEN);
				// 获取查询报表字段信息
				List<Map<String, String>> repQueryList = resSer.getReportFeldsQuery(map);
				List<Map> repql = null;
				boolean jsonP = false;
				String cb = request.getParameter("audit");
				if (cb != null) {
					jsonP = true;
					response.setContentType("text/javascript;charset=utf-8");
				} else {
					response.setContentType("application/x-json;charset=utf-8");
				}
				PrintWriter out;
				try {
					out = response.getWriter();
					if (jsonP) {
						out.write(cb + "(");
					}
					if (repQueryList != null && repQueryList.size() > 0) {
						repql = new ArrayList<Map>();
						Map reMap = null;
						for (Map<String, String> map2 : repQueryList) {
							reMap = new HashMap();
							reMap.put("dtVal", map2.get("field_name_CN") + "");
							reMap.put("dt", map2.get("field_name_EN") + "");
							String resql = map2.get("field_sql") + "";
							if ("".equals(resql) || null == resql || "null".equals(resql)) {
								continue;
							}
							List<Map<String, String>> repData = resSer.getReportDataBySql(resql);
							setMETHOD(request, requestUrlMapping);// 进行初始化 日志参数
							reMap.put("data", repData);
							repql.add(reMap);
						}
						String aa = json.toJson(repql);
						out.print(aa);
					}
					if (jsonP) {
						out.write(");");
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					upLogMethod(request);
				}

			}
		}

	}

	/**
	 * 新页面和原来一致，路径不一样
	 * 
	 * @param reportEN
	 * @param request
	 * @param response
	 */

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/newreportTable/{reportEN}", produces = "text/html;charset=UTF-8")
	public void getNewReportParamlist(@PathVariable String reportEN, HttpServletRequest request,
			HttpServletResponse response) {
		Gson json = new Gson();
		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}
		// 判断接口是否注册
		String requestUrl = request.getContextPath() + request.getServletPath();
		ResultMessage result = new ResultMessage();
		if (null == request.getParameter("iframe") && !checkInterFaceExist(requestUrl, request)) {
			result.setCode("2");
			result.setMsg("存在非法访问，操作已禁止");
			try {
				PrintWriter out = response.getWriter();
				out.print(json.toJson(result));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {

			Map<String, String> map = new HashMap<String, String>();
			map.put("report_Name_EN", reportEN);
			// 获取查询报表字段信息
			List<Map<String, String>> repQueryList = resSer.getReportFeldsQuery(map);
			List<Map> repql = null;
			boolean jsonP = false;
			String cb = request.getParameter("audit");
			if (cb != null) {
				jsonP = true;
				response.setContentType("text/javascript;charset=utf-8");
			} else {
				response.setContentType("application/x-json;charset=utf-8");
			}
			PrintWriter out;
			try {
				out = response.getWriter();
				if (jsonP) {
					out.write(cb + "(");
				}
				if (repQueryList != null && repQueryList.size() > 0) {
					repql = new ArrayList<Map>();
					Map reMap = null;
					for (Map<String, String> map2 : repQueryList) {
						reMap = new HashMap();
						reMap.put("dtVal", map2.get("field_name_CN") + "");
						reMap.put("dt", map2.get("field_name_EN") + "");
						String resql = map2.get("field_sql") + "";
						List<Map<String, String>> repData = resSer.getReportDataBySql(resql);
						setMETHOD(request, reportEN);// 进行初始化 日志参数
						reMap.put("data", repData);
						repql.add(reMap);
					}
					String aa = json.toJson(repql);
					out.print(aa);
				}
				if (jsonP) {
					out.write(");");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				upLogMethod(request);
			}
		} // else
	}

	private Map<String, List<Map<String, String>>> getPronJson(List<Map<String, String>> data) throws Exception{
		if (data != null && data.size() > 0) {
			List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
			Map<String, List<Map<String, String>>> mapstr = new LinkedHashMap<String, List<Map<String, String>>>();
			String pro_name = "";
			for (Map<String, String> map : data) {
				pro_name = map.get("pro_nm") + "";
				if (mapstr.get(pro_name) == null) {
					mapstr.put(pro_name, retList);
					retList = new ArrayList<Map<String, String>>();
				}
				List<Map<String, String>> list = mapstr.get(pro_name);
				if (list != null) {
					list.add(map);
				} else {
					retList.add(map);
					mapstr.put(pro_name, retList);
					retList = new ArrayList<Map<String, String>>();
				}
			}
			return mapstr;
		}
		return null;
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/reportParam/dateQueryParamAll", produces = "text/html;charset=UTF-8")
	public void getReportParamReturn(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("typValue") String typValue) {
		Map<String, String> map = new HashMap<String, String>();
		Gson json = new Gson();

		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}
		boolean jsonP = false;
		String cb = request.getParameter("audit");
		if (cb != null) {
			jsonP = true;
			response.setContentType("text/javascript;charset=utf-8");
		} else {
			response.setContentType("application/x-json;charset=utf-8");
		}
		PrintWriter out;
		try {
			out = response.getWriter();
			if (jsonP) {
				out.write(cb + "(");
			}
			if (!"".equals(typValue) && typValue != null) {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				Date date = format.parse(typValue);
				Calendar cal = Calendar.getInstance();
				String week_star_dt = "";
				String week_end_dt = "";
				String week_q_star_dt = "";
				String week_q_end_dt = "";
				String month_star_dt = "";
				String month_end_dt = "";
				String quarter_star_dt = "";
				String quarter_end_dt = "";

				cal.setTime(date);
				cal.add(Calendar.WEEK_OF_YEAR, -1);// 一周
				cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				week_star_dt = format.format(cal.getTime());
				cal.add(Calendar.DAY_OF_MONTH, +6);// 取当前日期的后一天.
				week_end_dt = format.format(cal.getTime());
				cal.clear();
				cal.setTime(date);
				cal.add(Calendar.WEEK_OF_YEAR, -1);// 一周
				cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
				week_q_star_dt = format.format(cal.getTime());
				cal.add(Calendar.DAY_OF_MONTH, +6);// 取当前日期的后一天.
				week_q_end_dt = format.format(cal.getTime());
				cal.clear();
				String todata = format.format(cal.getInstance().getTime());
				if (todata.substring(0, 6).equals(typValue.substring(0, 6))) {
					SimpleDateFormat format1 = new SimpleDateFormat("yyyyMM");
					date = format1.parse(typValue.substring(0, 6));
					cal.setTime(date);
					cal.add(Calendar.MONTH, -1);
					month_star_dt = format1.format(cal.getTime());
					month_end_dt = format1.format(cal.getTime());
				} else {
					month_star_dt = todata.substring(0, 6);
					month_end_dt = todata.substring(0, 6);
				}
				cal.clear();
				cal.setTime(date);
				int currentMonth = cal.get(Calendar.MONTH) - 1;
				if (currentMonth >= 1 && currentMonth <= 3) {
					cal.set(Calendar.MONTH, 2);
					cal.set(Calendar.DATE, 31);
				} else if (currentMonth >= 4 && currentMonth <= 6) {
					cal.set(Calendar.MONTH, 5);
					cal.set(Calendar.DATE, 30);
				} else if (currentMonth >= 7 && currentMonth <= 9) {
					cal.set(Calendar.MONTH, 8);
					cal.set(Calendar.DATE, 30);
				} else if (currentMonth >= 10 && currentMonth <= 12) {
					cal.set(Calendar.MONTH, 11);
					cal.set(Calendar.DATE, 31);
				}
				quarter_end_dt = format.format(cal.getTime()).substring(0, 6);
				cal.setTime(cal.getTime());
				cal.add(Calendar.MONTH, -2);
				quarter_star_dt = format.format(cal.getTime()).substring(0, 6);
				map.put("week_star_dt", week_star_dt);
				map.put("week_end_dt", week_end_dt);
				map.put("week_q_star_dt", week_q_star_dt);
				map.put("week_q_end_dt", week_q_end_dt);
				map.put("month_star_dt", month_star_dt);
				map.put("month_end_dt", month_end_dt);
				map.put("quarter_star_dt", quarter_star_dt);
				map.put("quarter_end_dt", quarter_end_dt);
				String jsonstr = json.toJson(map);
				out.print(jsonstr);
			}
			if (jsonP) {
				out.write(");");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用户、客户报表
	 * 
	 * @param
	 * @throws Exception
	 */

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/crmportDetail/new_report_crm_user_count", produces = "text/html;charset=UTF-8")
	public void getReportCrmStatistical(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("parameter") String parameter) {
		Map<String, String> map = new HashMap<String, String>();
		Gson json = new Gson();

		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}
		boolean jsonP = false;
		String cb = request.getParameter("audit");
		if (cb != null) {
			jsonP = true;
			response.setContentType("text/javascript;charset=utf-8");
		} else {
			response.setContentType("application/x-json;charset=utf-8");
		}
		PrintWriter out;
		try {
			out = response.getWriter();
			if (jsonP) {
				out.write(cb + "(");
			}
			ResultMessage4 result = new ResultMessage4();
			// 判断接口是否注册
			String requestUrl = request.getContextPath() + request.getServletPath();
			if (null == request.getParameter("iframe") && !checkInterFaceExist(requestUrl, request)) {
				result.setCode("2");
				result.setMsg("存在非法访问，操作已禁止");
				try {
					out = response.getWriter();
					out.print(json.toJson(result));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				JSONObject obj = new JSONObject().fromObject(parameter);
				Iterator params = obj.keys();
				Map pKeyValue = new HashMap();
				while (params.hasNext()) {
					String param_key = params.next().toString();// 获得相应的key，value
					String param_value = obj.getString(param_key);
					if (param_key.equals("dt")) {
						pKeyValue.put(param_key, param_value);
						continue;
					} else {
						if (!checkJurExist(param_value, request)) {
							result.setCode("2");
							result.setMsg("存在非法访问，操作已禁止");
							break;
						} else {
							pKeyValue.put(param_key, param_value);
						}
					}
				}
				if (!"2".equals(result.getCode())) {
					List<Map<String, String>> cucresult = resSer.reportCrmUserCount(pKeyValue);
					List<Map> headresult = resSer.reportCrmUserCountHead(pKeyValue);
					Map<String, List<Map<String, String>>> presult = getPronJson(cucresult);
					result.setCode("1");
					result.setMsg("查询成功");
					result.setData(presult);
					result.setHead(headresult);
				}
				out.print(json.toJson(result));
				if (jsonP) {
					out.write(");");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用户、客户报表导出
	 * 
	 * @param
	 * @throws Exception
	 */

	@SuppressWarnings({ "unchecked", "static-access", "rawtypes" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/crmportDetail/export_report_crm_user_count", produces = "text/html;charset=UTF-8")
	public void exportReportCrm(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("parameter") String parameter) {
		Gson json = new Gson();

		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}
		try {
			ResultMessage result = new ResultMessage();
			// 判断接口是否注册
			String requestUrl = request.getContextPath() + request.getServletPath();
			if (null == request.getParameter("iframe") && !checkInterFaceExist(requestUrl, request)) {
				result.setCode("2");
				result.setMsg("存在非法访问，操作已禁止");
			} else {
				JSONObject obj = new JSONObject().fromObject(parameter);
				Iterator params = obj.keys();
				Map pKeyValue = new HashMap();
				while (params.hasNext()) {
					String param_key = params.next().toString();// 获得相应的key，value
					String param_value = obj.getString(param_key);
					if (param_key.equals("dt") || param_key.equals("fileName")) {// 账期和文件名不进行判断
						pKeyValue.put(param_key, param_value);
						continue;
					} else {
						if (!checkJurExist(param_value, request)) {
							result.setCode("2");
							result.setMsg("存在非法访问，操作已禁止");
							break;
						} else {
							pKeyValue.put(param_key, param_value);
						}
					}
				}
				if (!"2".equals(result.getCode())) {

					List<Map> cucresult = resSer.reportCrmUserCount(pKeyValue);
					for (Map p : cucresult) {
						if (p.get("typ").toString().equals("1")) {
							p.put("typ", "用户数");
						} else if (p.get("typ").toString().equals("2")) {
							p.put("typ", "客户数");
						} else {
							p.put("typ", "-");
						}
					}
					List<Map> headresult = resSer.reportCrmUserCountHead(pKeyValue);
					String fileName = "未命名";
					if (null != pKeyValue.get("fileName") && "" != pKeyValue.get("fileName")) {
						fileName = pKeyValue.get("fileName").toString();
					}
//		 				HSSFWorkbook wb = exportReportTemplete.getHSSFWorkbook(fileName, headresult, cucresult, null);
					exportReportTemplete.returnXSSFWorkbook(response, fileName, headresult, cucresult);
					setMETHOD(request, "export_report_crm_user_count");
					setDOWNMETHOD(request, "export_report_crm_user_count");
//		 				exportReportTemplete.ReturnResponse(response, fileName+".xls", wb);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			upLogMethod(request);
		}
	}

	/**
	 * 通过key值判断是否存在非法权限
	 * 
	 */

	protected Boolean checkJurExist(String key, HttpServletRequest request) {// true 存在，false 不存在，非法
		try {
			if (null != request.getParameter("iframe")
					&& Boolean.parseBoolean(request.getParameter("iframe").toString()))
				return true;
			AdminSession sessionuser = getUserSessionUtil.getUserSession(request);// 通过session取值
			String roleId = sessionuser.getRole();
			Boolean result = resSer.checkJurExist(roleId, key);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 通过key值判断是否存在非法权限
	 * 
	 */

	protected Boolean checkInterFaceExist(String url, HttpServletRequest request) {// true 存在，false 不存在，非法
		try {
			if (null != request.getParameter("iframe")
					&& Boolean.parseBoolean(request.getParameter("iframe").toString()))
				return true;
			AdminSession sessionuser = getUserSessionUtil.getUserSession(request);// 通过session取值
			String roleId = sessionuser.getRole();
			Boolean result = resSer.checkInterFaceExist(roleId, url);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 天源迪科验证Url
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/reportTable/urlcheck", produces = "text/html;charset=UTF-8")
	public void checkInterFaceExistDk(String url, HttpServletRequest request, HttpServletResponse response) {// true
																												// 存在，false
																												// 不存在，非法
		Gson json = new Gson();

		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}
		boolean jsonP = false;
		String cb = request.getParameter("audit");
		if (cb != null) {
			jsonP = true;
			response.setContentType("text/javascript");
		} else {
			response.setContentType("application/x-json");
		}
		PrintWriter out;
		try {
			out = response.getWriter();
			if (jsonP) {
				out.write(cb + "(");
			}
			Map<String, String> map = new HashMap<String, String>();
			String sessionId = request.getParameter("loginname");
			MySessionContext myc = MySessionContext.getInstance();
			HttpSession sess = myc.getSession(sessionId);
			if (null == sess) {
				map.put("code", "0");
				map.put("msg", "用户登录已失效");
			} else {
				AdminSession sessionuser = ((AdminSession) sess.getAttribute("UserSession"));
				String roleId = sessionuser.getRole();
				Boolean result = resSer.checkInterFaceExist(roleId, url);
				if (result == false) {
					map.put("code", "2");
					map.put("msg", "非法访问，操作已禁止");
				} else {
					map.put("code", "1");
					map.put("msg", "操作成功");
				}
			}
			out.print(json.toJson(map));
			if (jsonP) {
				out.write(");");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 验证权限
	 */

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/reportTable/jurcheck", produces = "text/html;charset=UTF-8")
	public void checkJurExistDk(String jur, HttpServletRequest request, HttpServletResponse response) {// true 存在，false
																										// 不存在，非法
		Gson json = new Gson();

		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}
		boolean jsonP = false;
		String cb = request.getParameter("audit");
		if (cb != null) {
			jsonP = true;
			response.setContentType("text/javascript");
		} else {
			response.setContentType("application/x-json");
		}
		PrintWriter out;
		try {
			out = response.getWriter();
			if (jsonP) {
				out.write(cb + "(");
			}
			Map<String, String> map = new HashMap<String, String>();
			String sessionId = request.getParameter("loginname");
			MySessionContext myc = MySessionContext.getInstance();
			HttpSession sess = myc.getSession(sessionId);
			if (null == sess) {
				map.put("code", "0");
				map.put("msg", "用户登录已失效");
			} else {
				AdminSession sessionuser = ((AdminSession) sess.getAttribute("UserSession"));
				String roleId = sessionuser.getRole();
				Boolean result = resSer.checkJurExist(roleId, jur);
				if (result == false) {
					map.put("code", "2");
					map.put("msg", "非法访问，操作已禁止");
				} else {
					map.put("code", "1");
					map.put("msg", "操作成功");
				}
			}
			out.print(json.toJson(map));
			if (jsonP) {
				out.write(");");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 断传次数，时间段导出
	 */

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/exportReport/export_report_crm_user_count", produces = "text/html;charset=UTF-8")
	public void exportProvBreakNumAndTime(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
		Map<String, String> map = new HashMap<String, String>();
		Gson json = new Gson();

		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}
		boolean jsonP = false;
		try {
			ResultMessage result = new ResultMessage();
			// 判断接口是否注册
			String requestUrl = request.getContextPath() + request.getServletPath();
			List<Map> title = new ArrayList();
			if (null == request.getParameter("iframe") && !checkInterFaceExist(requestUrl, request)) {
				Map p1 = new HashMap();
				p1.put("field_name_CN", "错误代码");
				p1.put("field_name_EN", "code");

				Map p2 = new HashMap();
				p2.put("field_name_CN", "错误信息");
				p2.put("field_name_EN", "msg");
				title.add(p1);
				title.add(p2);
				Map p3 = new HashMap();
				p3.put("code", "2");
				p3.put("msg", "存在非法访问，操作已禁止");

				List errorList = new ArrayList();
				errorList.add(p3);

// 					 HSSFWorkbook wb = exportReportTemplete.getHSSFWorkbook("断传信息详情", title, errorList, null);
				setMETHOD(request, "export_report_crm_user_count");
				setDOWNMETHOD(request, "export_report_crm_user_count");
				exportReportTemplete.returnXSSFWorkbook(response, "断传信息详情_网间结算详单", title, errorList);
//   		 			 exportReportTemplete.ReturnResponse(response, "断传信息详情_网间结算详单"+".xls", wb);
			} else {
				Map p = new HashMap();
				p.put("startTime", startTime.replace("'", ""));
				p.put("endTime", endTime.replace("'", ""));
				List<Map> dataList = resSer.exportProvBreakNumAndTime(p);
				result.setCode("1");
				result.setMsg("查询成功");
				result.setData(dataList);

				if (!"1".equals(result.getCode())) {
					result.setCode("2");
					result.setMsg("未知错误");
					result.setData(null);
				} else {
					Map p1 = new HashMap();
					p1.put("field_name_CN", "省份");
					p1.put("field_name_EN", "prov_name");

					Map p5 = new HashMap();
					p5.put("field_name_CN", "账期");
					p5.put("field_name_EN", "breakDate");

					Map p2 = new HashMap();
					p2.put("field_name_CN", "断传开始时间");
					p2.put("field_name_EN", "breakStartTime");

					Map p3 = new HashMap();
					p3.put("field_name_CN", "断传结束时间");
					p3.put("field_name_EN", "breakEndTime");

					Map p4 = new HashMap();
					p4.put("field_name_CN", "断传时长（分钟）");
					p4.put("field_name_EN", "breakTimeLength");
					title.add(p1);
					title.add(p5);
					title.add(p2);
					title.add(p3);
					title.add(p4);
//        		     HSSFWorkbook wb = exportReportTemplete.getHSSFWorkbook("断传信息详情", title, dataList, null);
					setMETHOD(request, "export_report_crm_user_count");
					setDOWNMETHOD(request, "export_report_crm_user_count");
					exportReportTemplete.returnXSSFWorkbook(response, "断传信息详情_网间结算详单_" + startTime + "-" + endTime,
							title, dataList);
//   		 			 exportReportTemplete.ReturnResponse(response, "断传信息详情_网间结算详单_"+startTime+"-"+endTime+".xls", wb);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			upLogMethod(request);
		}
	}

	// 按小时统计 report_oth_fix_monitor_count
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/newreport/report_oth_fix_monitor_count", produces = "text/html;charset=UTF-8")
	public void report_oth_fix_monitor_count(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("dataType") String dataType) {
		Gson json = new Gson();

		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}
		PrintWriter out;
		response.setContentType("application/x-json");
		try {
			out = response.getWriter();
			ResultMessage result = new ResultMessage();
			AdminSession sessionuser = getUserSessionUtil.getUserSession(request);
			String requestUrl = request.getContextPath() + request.getServletPath();
			if (null == request.getParameter("iframe") && !checkInterFaceExist(requestUrl, request)) {
				result.setCode("2");
				result.setMsg("存在非法访问，操作已禁止");
				try {
					out.print(json.toJson(result));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				String dt = sdf.format(new Date());
				Map p = new HashMap();
				p.put("fixType", dataType);
				p.put("dt", dt);
				List<Map> resutList = resSer.report_oth_fix_monitor_count(p);
				setMETHOD(request, "report_oth_fix_monitor_count");
				result.setCode("1");
				result.setMsg("查询成功");
				result.setData(resutList);
				out.print(json.toJson(result));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			upLogMethod(request);
		}
	}

	/**
	 * 断传次数，时间段导出（按小时）
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/exportReport/export_report_oth_fix_monitor_count", produces = "text/html;charset=UTF-8")
	public void export_report_oth_fix_monitor_count(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime,
			@RequestParam("dataType") String dataType) {
		Map<String, String> map = new HashMap<String, String>();
		Gson json = new Gson();

		// 在外嵌的时候进行判断 是否有权限
		if (null != request.getParameter("iframe") && Boolean.parseBoolean(request.getParameter("iframe").toString())
				&& !isIframeCook(request)) {
			PrintWriter out1 = null;
			try {
				out1 = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map maplist = new HashMap();
			maplist.put("code", 0);
			maplist.put("msg", "没有通过访问权限");
			String jsonstr = json.toJson(maplist);
			out1.print(jsonstr);
			return;
		}
		boolean jsonP = false;
		try {
			ResultMessage result = new ResultMessage();
			// 判断接口是否注册
			String requestUrl = request.getContextPath() + request.getServletPath();
			List<Map> title = new ArrayList();
			if (null == request.getParameter("iframe") && !checkInterFaceExist(requestUrl, request)) {
				Map p1 = new HashMap();
				p1.put("field_name_CN", "错误代码");
				p1.put("field_name_EN", "code");

				Map p2 = new HashMap();
				p2.put("field_name_CN", "错误信息");
				p2.put("field_name_EN", "msg");
				title.add(p1);
				title.add(p2);
				Map p3 = new HashMap();
				p3.put("code", "2");
				p3.put("msg", "存在非法访问，操作已禁止");
				List errorList = new ArrayList();
				errorList.add(p3);
//   					 HSSFWorkbook wb = exportReportTemplete.getHSSFWorkbook("断传信息详情", title, errorList, null);
				setMETHOD(request, "export_report_crm_user_count");
				setDOWNMETHOD(request, "export_report_crm_user_count");
				exportReportTemplete.returnXSSFWorkbook(response, "断传信息详情", title, errorList);
//    		 		 exportReportTemplete.ReturnResponse(response, "断传信息详情"+".xls", wb);
			} else {
				Map p = new HashMap();
				p.put("startTime", startTime.replace("'", ""));
				p.put("endTime", endTime.replace("'", ""));
				p.put("dataType", dataType);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				String dt = sdf.format(new Date());
				p.put("dt", dt);
				List<Map> dataList = resSer.export_report_oth_fix_monitor_count(p);
				result.setCode("1");
				result.setMsg("查询成功");
				result.setData(dataList);

				if (!"1".equals(result.getCode())) {
					result.setCode("2");
					result.setMsg("未知错误");
					result.setData(null);
				} else {
					Map p1 = new HashMap();
					p1.put("field_name_CN", "省份");
					p1.put("field_name_EN", "prov_name");

					Map p6 = new HashMap();
					p6.put("field_name_CN", "账期");
					p6.put("field_name_EN", "breakDate");

					Map p2 = new HashMap();
					p2.put("field_name_CN", "断传开始时间");
					p2.put("field_name_EN", "breakStartTime");

					Map p3 = new HashMap();
					p3.put("field_name_CN", "断传结束时间");
					p3.put("field_name_EN", "breakEndTime");

					Map p4 = new HashMap();
					p4.put("field_name_CN", "断传时长（小时）");
					p4.put("field_name_EN", "breakTimeLength");

					Map p5 = new HashMap();
					p5.put("field_name_CN", "数据类型");
					p5.put("field_name_EN", "data_ch");

					title.add(p1);
					title.add(p6);
					title.add(p2);
					title.add(p3);
					title.add(p4);
					title.add(p5);
//          		         HSSFWorkbook wb = exportReportTemplete.getHSSFWorkbook("断传信息详情", title, dataList, null);
					String fileName = "";
					if (("FIX_ALL").equals(dataType)) {
						fileName = FIX_ALL;
					} else if (("OTH_ALL").equals(dataType)) {
						fileName = OTH_ALL;
					}
					setMETHOD(request, "export_report_crm_user_count");
					setDOWNMETHOD(request, "export_report_crm_user_count");
					exportReportTemplete.returnXSSFWorkbook(response,
							"断传信息详情_" + fileName + "_" + startTime + "-" + endTime, title, dataList);
//     		 			 exportReportTemplete.ReturnResponse(response, "断传信息详情_"+fileName+"_"+startTime+"-"+endTime+".xls", wb);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			upLogMethod(request);
		}
	}

	/**
	 * @DESC 进行是否是外嵌进行判断
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Boolean isIframeCook(HttpServletRequest request) {
		String menuId = request.getParameter("menuId");
		String roleId = request.getParameter("roleId");
		String JSESSIONID = request.getParameter("JSESSIONID");
		try {
			if (menuId.equals("") || roleId.equals("") || JSESSIONID.equals(""))
				return false;

			String url = getProperties("config.properties", "iframeEmbedUrl");
			String param = "menuId=" + menuId + "&roleId=" + roleId + "&JSESSIONID=" + JSESSIONID;
			System.out.println("进入验证!");
			String r = null;
			try {
				r = HttpRequest.sendGet(url, param);
//    			 r = "{\"hasRight\":\"1\"}";
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			System.out.println("进行外部接口验证权限 \t" + r);
			Gson gson = new Gson();
			Map<String, String> map = new HashMap<String, String>();
			map = gson.fromJson(r, map.getClass());
			if (null != map && "1".equals(map.get("hasRight"))) {
				return true;
			}
			;
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println();
		return false;
	}

	// mETHOD 方法 名称
	public void setMETHOD(HttpServletRequest request, String mETHOD) {
		try {
			AdminSession sessionuser = getUserSessionUtil.getUserSession(request);
			LogFilter logf = new LogFilter();
			try {
				String jsessionid = sessionuser == null ? null : sessionuser.getJsSessionId();
				if (jsessionid != null) {
					MySessionContext myc = MySessionContext.getInstance();
					HttpSession sess = myc.getSession(jsessionid);
					String mODULE = loginService.getModuleCode(mETHOD);

					logf.setMODULE(mODULE);// 记录模块
					logf.setMETHOD(mETHOD);// 记录方法

					System.out.println("log 查看存储之前: " + logf.toString());
					sess.setAttribute("LogFilter", logf);
				}
			} catch (Exception e) {
				sessionuser = null;
				System.out.println("用户已经被注销!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 下载方法名称
	public void setDOWNMETHOD(HttpServletRequest request, String dOWNMETHOD) {
		try {
			AdminSession sessionuser = getUserSessionUtil.getUserSession(request);
			LogFilter logf;
			try {
				String jsessionid = sessionuser == null ? null : sessionuser.getJsSessionId();

				if (jsessionid != null) {
					MySessionContext myc = MySessionContext.getInstance();
					HttpSession sess = myc.getSession(jsessionid);

					logf = ((LogFilter) sess.getAttribute("LogFilter"));
					logf.setMETHOD(null);// 如果是进行下载则查询的method不进行记录
					logf.setDOWNMETHOD(dOWNMETHOD);

					System.out.println("log 下载存储之前: " + logf.toString());
					sess.setAttribute("LogFilter", logf);
				}
			} catch (Exception e) {
				sessionuser = null;
				System.out.println("用户已经被注销!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void upLogMethod(HttpServletRequest request) {
		LogFilter logf;
		try {
			AdminSession sessionuser = getUserSessionUtil.getUserSession(request);
			HashMap<String, String> logMessing = new HashMap<String, String>();
			try {
				String jsessionid = sessionuser == null ? null : sessionuser.getJsSessionId();

				MySessionContext myc = MySessionContext.getInstance();
				HttpSession sess = myc.getSession(jsessionid);

				logf = ((LogFilter) sess.getAttribute("LogFilter"));

				logMessing.put("log_JSESSIONID", jsessionid);

				if (null != logf.getMETHOD() && !"".equals(logf.getMETHOD())) {
					logMessing.put("log_method", logf.getMETHOD());
				}
				if (null != logf.getDOWNMETHOD() && !"".equals(logf.getDOWNMETHOD())) {
					logMessing.put("log_downmethod", logf.getDOWNMETHOD());
				}
				if (null != logf.getMODULE() && !"".equals(logf.getMODULE())) {
					logMessing.put("log_module", logf.getMODULE());
				}
			} catch (Exception e) {
				sessionuser = null;
				System.out.println("用户已经被注销!");
			}
			System.out.println("======写入日志内容 " + logMessing.toString());
			loginService.upLogMethod(logMessing);
			System.out.println("======写入结束 " + logMessing.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 获取 Properties
	public static String getProperties(String filePath, String keyWord) {
		Properties prop = null;
		String value = null;
		try {
			prop = PropertiesLoaderUtils.loadAllProperties(filePath);
			value = prop.getProperty(keyWord);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}

	public void getDownLoadDataRole(HttpServletRequest request) {
		try {
			AdminSession sessionuser = getUserSessionUtil.getUserSession(request);
			String roleId = sessionuser.getRole();
			int userId = sessionuser.getId();
			String userName = sessionuser.getLoginName();
			Map<String, String> result = new HashMap<String, String>();
			result.put("roleId", String.valueOf(roleId));
			result.put("userId", String.valueOf(userId));
			result.put("userName", String.valueOf(userName));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}