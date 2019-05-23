package com.dw.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dw.common.AdminSession;
import com.dw.common.FiledProcFilter;
import com.dw.common.LogFilter;
import com.dw.interceptor.MySessionContext;
import com.dw.model.ResultMessage;
import com.dw.servce.ILogService;
import com.dw.servce.IReportService;
import com.dw.servce.IUserService;
import com.dw.util.HttpRequest;
import com.dw.util.exportReportTemplete;
import com.google.gson.Gson;

@Controller
public class Report2Constroller {
	@Autowired
	IUserService adminService;
	@Autowired
	IReportService resSer;
	@Autowired
	ILogService loginService;

	/**
	 * 进行对空值率的数据做出统计
	 * 
	 * @param reportEN
	 * @param request
	 * @param response
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/getReportDirect", produces = "text/html;charset=UTF-8", params = "fieldNameEN")
	public void getRportByAddressMappingDirect(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("fieldVal") String fieldVal, @RequestParam("fieldNameEN") String fieldNameEN) {
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
			out1.print(jsonstr == null ? "null" : jsonstr);
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
			out1.print(jsonstr == null ? "null" : jsonstr);
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
			// 获取稽核报表基本信息
			Map<String, String> requestUrlmap = new HashMap<String, String>();// 用于存在全部的接口
			FiledProcFilter instance = new FiledProcFilter();
			// 进行初始化参数
			instance.setAllpv(false);
			instance.setDbTypechart(null);
			instance.setWhereString("");
			instance.setGroupString("");
			instance.setKpi_id("");
			instance.setDtString(null);
			instance.setPrString(null);
			instance.setCtString(null);
			// 结束赋值
			String[] nameStr = null;// fileName 的List
			String[] fvalStr = null;// fileVal 的List
			String fileName = null;
			if (null != fieldNameEN && null != fieldVal) {
				nameStr = fieldNameEN.replaceAll("'", "").split(",");
				fvalStr = fieldVal.replaceAll("'", "").split(",");
			}
			// 判断该key下的值该角色是否有权限
			if (null != nameStr)
				for (int i = 0; i < nameStr.length; i++) {
					if ("prov_id".equals(nameStr[i]) && "pvct".equals(fvalStr[i])) {// 当传的全部的时候为空编码

					} else {
						requestUrlmap.put(nameStr[i], fvalStr[i]);
					}
					if ("kpi_id".equals(nameStr[i]))
						instance.setKpi_id(fvalStr[i]);
					if (instance.getBl().contains(nameStr[i])) {// 在白名单之中
						continue;
					} else {
						String keyValue = fvalStr[i];
						if (null != keyValue && !checkJurExist(keyValue, request)) {
							result.setCode("2");
							result.setMsg("字段非法访问，操作已禁止");
							break;
						}
					}
				}
			if (null != request.getParameter("where"))// 进行增加一些where
				instance.setWhereString(request.getParameter("where"));
			if (null != request.getParameter("group"))// 进行替换group by 的内容
				instance.setGroupString(request.getParameter("group"));
			if (null != request.getParameter("notrToc"))// 不进行行转列的字段列
				instance.setNotrToc(request.getParameter("notrToc"));
			if (null != request.getParameter("notFtMap"))// 不进行使用配置的逻辑的字段
				instance.setNotFtMap(request.getParameter("notFtMap"));

			instance.setRequestUrlmap(requestUrlmap);
			instance.setPv(resSer.getProvCodeName());
			if (null == instance.getCt()) {
				instance.setCt(resSer.getCityCodeName());
			}
			if (!"2".equals(result.getCode())) {
				String cb = request.getParameter("audit");
				if (cb != null) {
					response.setContentType("text/javascript");
				} else {
					response.setContentType("application/x-json");
				}
				PrintWriter out = null;
				// 如果该需求为特定需求 typechart：1 分省 2全国 默认为 2
				String typechart = "2";
				try {
					typechart = request.getParameter("typechart") == null ? typechart
							: request.getParameter("typechart");
				} catch (Exception e) {
					System.out.println("typechart 默认为 2 全部数据不进行分组 参数typechart 出现问题");
					e.printStackTrace();
				}
				List<Map<String, String>> directFiledMapping = null;
				try {
					out = response.getWriter();
					if (cb != null) {
						out.write(cb + "(");
					}
					if (typechart != null && "1".equals(typechart)) {// 这个是进行留下的接口
					}
					try {
						directFiledMapping = resSer.getDirectFiledMapping(requestUrlmap);
						instance.setDirectFiledName(directFiledMapping);
						try {
							List<Map<String, String>> reportDataBySql = resSer
									.getReportDataBySql(instance.getAllISPvSql());
							if (reportDataBySql.size() == 1) {
								List<String> c = new ArrayList<String>();// 这是全国的标准
								c.add("all");
								c.add("all1");
								c.add("998");
								c.add("allpv");
								String pr = instance.getpr();
								if (c.contains(reportDataBySql.get(0).get(pr))) {
									instance.setAllpv(true);
									System.out.println("这个查询只有全国的数据! ");
								}
							}
						} catch (Exception e) {
							System.out.println("查询是否全国sql 出现问题");
							e.printStackTrace();
						}
						try {
							String isENsql = instance.getIsEN();
							if (null != isENsql) {
								List<Map<String, String>> reportDataBySql2 = resSer.getReportDataBySql(isENsql);
								if (reportDataBySql2.size() > 0) {
									Map<String, String> map = reportDataBySql2.get(0);
									if (instance.getPrString() != "") {
										boolean parseBoolean = Boolean.parseBoolean(map.get(instance.getPrString()));
										instance.setIsPrEN(parseBoolean);
									}
									if (instance.getCtString() != "") {
										boolean parseBoolean = Boolean.parseBoolean(map.get(instance.getCtString()));
										instance.setIsCtEN(parseBoolean);
									}
								}
							}
						} catch (Exception e) {
							System.out.println("查询省份字段 地市字段是否为中文出现问题");
							e.printStackTrace();
						}
						if (directFiledMapping.size() == 0) {
							result.setCode("2");
							result.setMsg("数据库源表中没有信息, 请完善! ");
						}
					} catch (Exception e) {
						result.setCode("2");
						result.setMsg("查询字段的sql出现异常,请处理 !");
						e.printStackTrace();
					}

					if (!"2".equals(result.getCode())) {
						Map maplist = new HashMap();
						List<Map<String, String>> repHead, repData = null;
						long t1 = 0, t2 = 0, t3 = 0, t4 = 0;
						String rows, sql = "";
						t1 = System.currentTimeMillis();
						ArrayList<String> returnsql = instance.getDirectsql();
						List<String> list = new ArrayList<String>(requestUrlmap.keySet());
						t2 = System.currentTimeMillis();
						sql = returnsql.get(0);
						if (list.contains("rToc")) {// 是否进行行转列如果行转列
							String dt = instance.getdt();
							String prov = instance.getpr();
							String city = instance.getct();
							String[] dtt = requestUrlmap.get("dt").split("-");
							String where = "";
							if (null != requestUrlmap.get("prov_id") && !requestUrlmap.get("prov_id").equals("allpv")) {
								where += " AND " + prov + "='" + instance.getPrCodeName() + "'";
							}
							if (null != requestUrlmap.get("city_id") && !requestUrlmap.get("city_id").equals("allct")) {
								where += " AND " + city + "='" + instance.getCtCodeName() + "'";
							}
							List<String> bl = instance.getBl();
							for (int i = 0; i < list.size(); i++) {
								if (!bl.contains(list.get(i)) && !requestUrlmap.get(list.get(i)).toLowerCase().equals("all")) {
									where += " and " + list.get(i) + "='" + requestUrlmap.get(list.get(i)) + "'";
								}
							}
							if (instance != null && instance.getWhereString() != "") {
								where += " and " + instance.getWhereString();
							}
							String sqlString = " select " + requestUrlmap.get("rToc") + " rToc from "
									+ requestUrlmap.get("tableName") + " where 1 = 1 and " + dt + " >= '"
									+ dtt[0].trim() + "' AND " + dt + " <= '" + dtt[1].trim() + "' " + where
									+ " group by " + requestUrlmap.get("rToc");
							List<Map<String, String>> rToCsqlFileds = resSer.getReportDataBySql(sqlString);
							sql = instance.getRTOCSql(rToCsqlFileds, sql);
						}
						try {
							if (list.contains("rToc")) {
								repData = resSer
										.getReportDataBySql("select concat(count(*),'') rows from (" + sql + ") a");
							} else {
								repData = resSer.getReportDataBySql(
										"select concat(count(*),'') rows from (" + returnsql.get(0) + ") a");
							}
						} catch (Exception e) {
							maplist.put("msg1", "查询的sql count出现问题" + e.getMessage());
						}
						try {
							rows = (repData.get(0).get("rows") + "");
							t3 = System.currentTimeMillis();
							// 对字段进行排序
							repHead = new ArrayList<Map<String, String>>();
							List<Map<String, String>> catFL = instance.getHeadList();
							List<String> catF = instance.getFiledName();
							for (int i = 0; i < catF.size(); i++) {
								String CF = catF.get(i);
								for (int j = 0; j < catFL.size(); j++) {
									Map<String, String> m = catFL.get(j);
									if (m.get("field_name_EN").equals(CF)) {
										repHead.add(m);
										catFL.remove(j);
										j--;
									}
								}
							}
							if (catFL.size() > 0)
								repHead.addAll(catFL);
							String isColumn = request.getParameter("isColumn");
							String notColumn = request.getParameter("notColumn");
							if (isColumn != null) {
								ArrayList arrayList = new ArrayList();
								String[] split = isColumn.split(",");
								for (int i = 0; i < split.length; i++) {
									arrayList.add(split[i]);
								}
								repHead = instance.upIsColumn(repHead, arrayList);
							}
							if (notColumn != null) {
								ArrayList arrayList = new ArrayList();
								String[] split = notColumn.split(",");
								for (int i = 0; i < split.length; i++) {
									arrayList.add(split[i]);
								}
								repHead = instance.upNotColumn(repHead, arrayList);
							}
							if ("9".equals(typechart)) {// 进行查询head
								maplist.put("code", "1");
								maplist.put("total", rows);// 进行表的时候的行数据列表
								maplist.put("onlyAll", instance.getAllpv());
								maplist.put("msg", "查询成功");
								maplist.put("Head", repHead);
								String jsonstr = json.toJson(maplist);
								t4 = System.currentTimeMillis();
								out.print(jsonstr);
							} else {
								String pageNo = request.getParameter("pageNo");
								String pageSize = request.getParameter("pageSize");
								String order = request.getParameter("order");
								if (null != order && !"".equals(order) && !"null".equals(order)) {
									sql = "select a.* from (" + sql + ") a where 1 = 1 " + order;
								}
								try {
									if (pageNo != null && !"".equals(pageNo) && !"null".equals(pageNo)
											&& pageSize != null && !"".equals(pageSize) && !"null".equals(pageSize)) {
										repData = resSer.getReportDataBySql(instance.getPageSql(sql, pageNo, pageSize));
									} else {
										repData = resSer.getReportDataBySql(sql);
									}
								} catch (Exception e) {
									maplist.put("msg1", "查询的sql data出现问题" + e.getMessage());
								}
								// 获取报表表头信息
								fileName = request.getParameter("fileName") + "";
								if (null != fileName && !"".equals(fileName) && !"null".equals(fileName)) {
									fileName = fileName.replace("'", "");
									exportReportTemplete.returnXSSFWorkbookM(response, fileName, repHead, repData);
								} else {
									maplist.put("code", "1");
									maplist.put("msg", "查询成功");
									maplist.put("total", rows);// 进行表的时候的行数据列表
									maplist.put("Head", repHead);
									maplist.put("onlyAll", instance.getAllpv());
									if (typechart != null && "1".equals(typechart) && null != instance.getpr()) {// 当存在省份并且有typechart为1的时候进行分组
										try {
											maplist.put("rows", getPronJson(repData, instance.getpr()));
										} catch (Exception e) {
											maplist.put("rows", repData);
											maplist.put("msg1",
													"数据没有" + instance.getpr() + "字段不能进行分组请把参数 typechart=1 删除!");
										}
									} else {
										maplist.put("rows", repData);
									}

									if (!"1".equals(maplist.get("code"))) {
										maplist.put("code", 2);
										maplist.put("msg", "查询失败");
										maplist.put("data", null);
									}
									String jsonstr = json.toJson(maplist);
									t4 = System.currentTimeMillis();
									out.print(jsonstr);
								}
							}
						} catch (Exception e) {
							maplist.put("msg2", "查询的数据为空 出现异常" + e.getMessage());
							e.printStackTrace();
						}
						System.out.println(
								" 加载sql时长: " + (t2 - t1) + " 加载行数和head信息时长: " + (t3 - t2) + "加载数据时长: " + (t4 - t3));
					} else {
						out.print(json.toJson(result));
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
				if (cb != null) {
					out.write(");");
				}
			}
		}
	}

	/**
	 * 进行对空值率的数据做出统计
	 * 
	 * @param reportEN
	 * @param request
	 * @param response
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/getReportNullRate", produces = "text/html;charset=UTF-8", params = "fieldNameEN")
	public void getRportByAddressMapping(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("fieldVal") String fieldVal, @RequestParam("fieldNameEN") String fieldNameEN) {
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
			out1.print(jsonstr == null ? "null" : jsonstr);
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
			out1.print(jsonstr == null ? "null" : jsonstr);
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
			// 获取稽核报表基本信息
			Map<String, String> requestUrlmap = new HashMap<String, String>();// 用于存在全部的接口
			FiledProcFilter instance = new FiledProcFilter();
			// 进行初始化参数
			instance.setAllpv(false);
			instance.setDbTypechart(null);
			instance.setWhereString("");
			instance.setGroupString("");
			instance.setKpi_id("");
			instance.setDtString(null);
			instance.setPrString(null);
			instance.setCtString(null);
			instance.setUnit("%");
			// 结束赋值
			String[] nameStr = null;// fileName 的List
			String[] fvalStr = null;// fileVal 的List
			List<String> filedNames = null;// 存储字段
			String fileName = null;
			if (null != fieldNameEN && null != fieldVal) {
				nameStr = fieldNameEN.replaceAll("'", "").split(",");
				fvalStr = fieldVal.replaceAll("'", "").split(",");
			}
			// 判断该key下的值该角色是否有权限
			if (null != nameStr)
				for (int i = 0; i < nameStr.length; i++) {
					if ("prov_id".equals(nameStr[i]) && "pvct".equals(fvalStr[i])) {// 当传的全部的时候为空编码

					} else {
						requestUrlmap.put(nameStr[i], fvalStr[i]);
					}
					if ("unit".equals(nameStr[i]) && !Boolean.parseBoolean(fvalStr[i])) {// 这个当存在unit
																							// 并且值为false的时候空值率的单位为空;unit不存在or值为true的时候空值率单位为
																							// '%'
						instance.setUnit("");
					} else {
						instance.setUnit("%");
					}
					if ("kpi_id".equals(nameStr[i]))
						instance.setKpi_id(fvalStr[i]);
					if (instance.getBl().contains(nameStr[i])) {// 在白名单之中
						continue;
					} else {
						String keyValue = fvalStr[i];
						if (null != keyValue && !checkJurExist(keyValue, request)) {
							result.setCode("2");
							result.setMsg("字段非法访问，操作已禁止");
							break;
						}
					}
				}
			instance.setRequestUrlmap(requestUrlmap);
			instance.setPv(resSer.getProvCodeName());
			if (null == instance.getCt()) {
				instance.setCt(resSer.getCityCodeName());
			}
			if (!"2".equals(result.getCode())) {
				String cb = request.getParameter("audit");
				if (cb != null) {
					response.setContentType("text/javascript");
				} else {
					response.setContentType("application/x-json");
				}
				PrintWriter out = null;
				// 如果该需求为特定需求 那么typechart =1 typechart：1 分省 2全国 3其他
				String typechart = "2";
				try {
					typechart = request.getParameter("typechart") == null ? typechart
							: request.getParameter("typechart");
				} catch (Exception e) {
					System.out.println("typechart 默认为 2 全部数据不进行分组 参数typechart 出现问题");
					e.printStackTrace();
				}
				try {
					out = response.getWriter();
					if (cb != null) {
						out.write(cb + "(");
					}
					if (typechart != null && "1".equals(typechart)) {// 这个是进行留下的接口

					}
					try {
						List<String> urlFiledMapping = resSer.getUrlFiledMapping(requestUrlmap);
						instance.setFiledName(urlFiledMapping);
						String eg_nm = instance.getBnu();
						filedNames = resSer.getUrlFileds(requestUrlmap, eg_nm);
						instance.setFiledNames(filedNames);
						try {
							List<Map<String, String>> reportDataBySql = resSer
									.getReportDataBySql(instance.getAllISPvSql());
							if (reportDataBySql.size() == 1) {
								List<String> c = new ArrayList<String>();
								c.add("all");
								c.add("all1");
								c.add("998");
								c.add("allpv");
								String pr = instance.getpr();
								if (c.contains(reportDataBySql.get(0).get(pr))) {
									instance.setAllpv(true);
									System.out.println("这个查询只有全国的数据! ");
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							if (null != instance.getpr()) {
								String isENsql = instance.getIsEN();
								if (null != isENsql) {
									List<Map<String, String>> reportDataBySql2 = resSer.getReportDataBySql(isENsql);
									if (reportDataBySql2.size() > 0) {
										Map<String, String> map = reportDataBySql2.get(0);
										if (instance.getPrString() != "") {
											boolean parseBoolean = Boolean
													.parseBoolean(map.get(instance.getPrString()));
											instance.setIsPrEN(parseBoolean);
										}
										if (instance.getCtString() != "") {
											boolean parseBoolean = Boolean
													.parseBoolean(map.get(instance.getCtString()));
											instance.setIsCtEN(parseBoolean);
										}
									}
								}
							}
						} catch (Exception e) {
							System.out.println("查询省份字段 地市字段是否为中文出现问题");
							e.printStackTrace();
						}
						if (filedNames.size() == 0) {
							result.setCode("2");
							result.setMsg("数据为空,请选择其它日期! ");
						}
					} catch (Exception e) {
						result.setCode("2");
						result.setMsg("查询字段的sql出现异常,请处理 !");
						e.printStackTrace();
					}

					if (!"2".equals(result.getCode())) {
						Map maplist = new HashMap();
						long t1 = 0, t2 = 0, t3 = 0, t4 = 0;
						t1 = System.currentTimeMillis();
						List<Map<String, String>> repData = null;
						ArrayList<String> returnsql = instance.getNullsql();
						t2 = System.currentTimeMillis();
						try {
							repData = resSer.getReportDataBySql(
									"select concat(count(*),'') rows from (" + returnsql.get(0) + ") a");
						} catch (Exception e) {
							maplist.put("msg1", "查询的sql count出现问题" + e.getMessage());
						}
						String rows = "";
						try {
							rows = (repData.get(0).get("rows") + "");
							// 对字段进行排序
							List<Map<String, String>> repHead = new ArrayList<Map<String, String>>();
							List<Map<String, String>> catFL = instance.getNUllTableHead();
							List<String> catF = instance.getFiledName();
							for (int i = 0; i < catF.size(); i++) {
								String CF = catF.get(i);
								for (int j = 0; j < catFL.size(); j++) {
									Map<String, String> m = catFL.get(j);
									if (m.get("field_name_EN").equals(CF)) {
										repHead.add(m);
										catFL.remove(j);
										j--;
									}
								}
							}
							if (catFL.size() > 0)
								repHead.addAll(catFL);
							String isColumn = request.getParameter("isColumn");
							String notColumn = request.getParameter("notColumn");
							if (isColumn != null) {
								ArrayList arrayList = new ArrayList();
								String[] split = isColumn.split(",");
								for (int i = 0; i < split.length; i++) {
									arrayList.add(split[i]);
								}
								repHead = instance.upIsColumn(repHead, arrayList);
							} else if (notColumn != null) {
								ArrayList arrayList = new ArrayList();
								String[] split = notColumn.split(",");
								for (int i = 0; i < split.length; i++) {
									arrayList.add(split[i]);
								}
								repHead = instance.upNotColumn(repHead, arrayList);
							}
							t3 = System.currentTimeMillis();
							String sql = returnsql.get(0);
							if ("9".equals(typechart)) {// 进行查询head
								maplist.put("code", "1");
								maplist.put("total", rows);// 进行表的时候的行数据列表
								maplist.put("onlyAll", instance.getAllpv());
								maplist.put("msg", "查询成功");
								maplist.put("Head", repHead);
								String jsonstr = json.toJson(maplist);
								t4 = System.currentTimeMillis();
								out.print(jsonstr);
							} else {
								String pageNo = request.getParameter("pageNo");
								String pageSize = request.getParameter("pageSize");
								String order = request.getParameter("order");
								if (null != order && !"".equals(order) && !"null".equals(order)) {
									sql = "select a.* from (" + sql + ") a where 1 = 1 " + order;
								}
								try {
									if (pageNo != null && !"".equals(pageNo) && !"null".equals(pageNo)
											&& pageSize != null && !"".equals(pageSize) && !"null".equals(pageSize)) {
										repData = resSer.getReportDataBySql(sql + " limit " + pageNo + "," + pageSize);
									} else {
										repData = resSer.getReportDataBySql(sql);
									}
								} catch (Exception e) {
									maplist.put("msg1", "查询的sql data出现问题" + e.getMessage());
								}
								t4 = System.currentTimeMillis();
								// 获取报表表头信息
								fileName = request.getParameter("fileName") + "";
								if (null != fileName && !"".equals(fileName) && !"null".equals(fileName)) {
									fileName = fileName.replace("'", "");
									exportReportTemplete.returnXSSFWorkbookM(response, fileName, repHead, repData);
								} else {
									maplist.put("code", "1");
									maplist.put("msg", "查询成功");
									maplist.put("rows", rows);
									maplist.put("total", rows);// 进行表的时候的行数据列表
									maplist.put("Head", repHead);
									maplist.put("onlyAll", instance.getAllpv());
									if (typechart != null && "1".equals(typechart)) {
										try {
											maplist.put("rows", getPronJson(repData, instance.getpr()));
										} catch (Exception e) {
											maplist.put("rows", repData);
											maplist.put("msg1",
													"数据没有" + instance.getpr() + "字段不能进行分组请把参数 typechart=1 删除!");
										}
									} else {
										maplist.put("rows", repData);
									}

									if (!"1".equals(maplist.get("code"))) {
										maplist.put("code", 2);
										maplist.put("msg", "查询失败");
										maplist.put("data", null);
									}
									String jsonstr = json.toJson(maplist);
									out.print(jsonstr);
								}
							}
						} catch (Exception e) {
							maplist.put("msg1", "数据为空" + e.getMessage());
						}
						System.out.println(
								" 加载sql时长: " + (t2 - t1) + " 加载行数和head信息时长: " + (t3 - t2) + "加载数据时长: " + (t4 - t3));
					} else {
						out.print(json.toJson(result));
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
				if (cb != null) {
					out.write(");");
				}
			}
		}
	}

	/**
	 * 进行源表维护
	 * 
	 * @param request
	 * @param response
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/maintainDS", produces = "text/html;charset=UTF-8")
	public void maintainDS(HttpServletRequest request, HttpServletResponse response) {
		try {
			String table_name = null;
			table_name = request.getParameter("tableName");
			resSer.maintainDS(table_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	private Map<String, List<Map<String, String>>> getPronJson(List<Map<String, String>> data, String filedName)
			throws Exception {
		if (data != null && data.size() > 0) {
			if (filedName == null)
				filedName = "pro_nm";
			List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
			Map<String, List<Map<String, String>>> mapstr = new LinkedHashMap<String, List<Map<String, String>>>();
			for (Map<String, String> map : data) {
				if (mapstr.get(map.get(filedName).toString()) == null) {
					mapstr.put(map.get(filedName), retList);
					retList = new ArrayList<Map<String, String>>();
				}
				List<Map<String, String>> list = mapstr.get(map.get(filedName));
				if (list != null) {
					list.add(map);
				} else {
					retList.add(map);
					mapstr.put(map.get(filedName), retList);
					retList = new ArrayList<Map<String, String>>();
				}
			}
			return mapstr;
		}
		return null;
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
// 			 r = "{\"hasRight\":\"1\"}";
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

//获取 Properties
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

	/**
	 * 进行对固网移网 做出波动率
	 * 
	 * @param reportEN
	 * @param request
	 * @param response
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/getReportVolatility0", produces = "text/html;charset=UTF-8", params = "fieldNameEN")
	public void getMonitorMappingVolatility(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("fieldVal") String fieldVal, @RequestParam("fieldNameEN") String fieldNameEN) {
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
			out1.print(jsonstr == null ? "null" : jsonstr);
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
			out1.print(jsonstr == null ? "null" : jsonstr);
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
			// 获取稽核报表基本信息
			Map<String, String> requestUrlmap = new HashMap<String, String>();// 用于存在全部的接口
			FiledProcFilter instance = new FiledProcFilter();
			String[] nameStr = null;// fileName 的List
			String[] fvalStr = null;// fileVal 的List
			String fileName = null;
			if (null != fieldNameEN && null != fieldVal) {
				nameStr = fieldNameEN.replaceAll("'", "").split(",");
				fvalStr = fieldVal.replaceAll("'", "").split(",");
			}
			// 判断该key下的值该角色是否有权限
			if (null != nameStr)
				for (int i = 0; i < nameStr.length; i++) {
					if ("prov_id".equals(nameStr[i]) && "pvct".equals(fvalStr[i])) {// 当传的全部的时候为空编码

					} else {
						requestUrlmap.put(nameStr[i], fvalStr[i]);
					}
					if (instance.getBl().contains(nameStr[i])) {// 在白名单之中
						continue;
					} else {
						String keyValue = fvalStr[i];
						if (null != keyValue && !checkJurExist(keyValue, request)) {
							result.setCode("2");
							result.setMsg("字段非法访问，操作已禁止");
							break;
						}
					}
				}
			instance.setRequestUrlmap(requestUrlmap);
			if (!"2".equals(result.getCode())) {
				String cb = request.getParameter("audit");
				if (cb != null) {
					response.setContentType("text/javascript");
				} else {
					response.setContentType("application/x-json");
				}
				PrintWriter out = null;
				// 如果该需求为特定需求 那么typechart =1 typechart：1 分省 2全国 3其他
				String typechart = "2";
				try {
					typechart = request.getParameter("typechart") == null ? typechart
							: request.getParameter("typechart");
				} catch (Exception e) {
					System.out.println("typechart 默认为 2 全部数据不进行分组 参数typechart 出现问题");
					e.printStackTrace();
				}
				try {
					out = response.getWriter();
					if (cb != null) {
						out.write(cb + "(");
					}
					String type = "";
					if (typechart != null && "1".equals(typechart)) {// 这个是进行留下的接口
						type = "FIX";
					} else if (typechart != null && "2".equals(typechart)) {
						type = "OTH";
					}
					String dtString = requestUrlmap.get("dt") != null
							? "'" + requestUrlmap.get("dt").split("-")[0] + "'"
							: "";
					String data_en = requestUrlmap.get("data_en");
					type = data_en.equals("all") ? type : data_en;
					String prov_code = requestUrlmap.get("prov_id");
					String time = requestUrlmap.get("time");
					List<Map<String, String>> directFiledMapping = resSer.getDirectFiledMapping(requestUrlmap);
					instance.setDirectFiledName(directFiledMapping);
					String whereString = "";
					try {
						if (prov_code == null && time.equals("all")) {
							whereString = "";
							if (data_en.equals("all"))
								whereString += " and data_en='all'";
							if (!data_en.equals("all"))
								whereString += " and data_en <> 'all'";
						} else if (prov_code == null && !time.equals("all")) {
							whereString = " and last_hour='" + time + ":00'";
							if (data_en.equals("all"))
								whereString += " and data_en='all'";
							if (!data_en.equals("all"))
								whereString += " and data_en <> 'all'";
						} else if (prov_code.equals("allpv") && time.equals("all")) {
							whereString = " and prov_id='all'";
							if (data_en.equals("all"))
								whereString += " and data_en='all'";
							if (!data_en.equals("all"))
								whereString += " and data_en <> 'all'";
						} else if (prov_code.equals("allpv") && !time.equals("all")) {
							whereString = " and prov_id='all' and last_hour='" + time + ":00'";
							if (data_en.equals("all"))
								whereString += " and data_en='all'";
							if (!data_en.equals("all"))
								whereString += " and data_en <> 'all'";
						} else if (!prov_code.equals("allpv") && time.equals("all")) {
							whereString = " and prov_id='" + prov_code + "'";
							if (data_en.equals("all"))
								whereString += " and data_en='all'";
							if (!data_en.equals("all"))
								whereString += " and data_en <> 'all'";
						} else if (!prov_code.equals("allpv") && !time.equals("all")) {
							whereString = " and prov_id='" + prov_code + "' and last_hour='" + time + ":00'";
							if (data_en.equals("all"))
								whereString += " and data_en='all' and last_hour<>'all'";
							if (!data_en.equals("all"))
								whereString += " and data_en <> 'all'";
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (!"2".equals(result.getCode())) {
						Map maplist = new HashMap();
						long t1 = System.currentTimeMillis();
						String sql = " SELECT p.prov_name prov_id,CASE last_hour WHEN 'all' THEN '全部' ELSE last_hour END last_hour,CASE data_en WHEN 'all' THEN '全部' ELSE data_en END data_en,CASE data_ch WHEN 'all' THEN '全部' ELSE data_ch END data_ch,concat(file_num_rate,'%') file_num_rate,concat(file_size_rate,'%') file_size_rate,count,seven_avg_count,ROUND(size/1024,2) size,ROUND(seven_avg_size/10240,2) seven_avg_size,dt FROM ("
								+ " SELECT prov_id,last_hour,data_en,data_ch,file_num_rate,file_size_rate,count,seven_avg_count,size,seven_avg_size,dt FROM ("
								+ " SELECT cur_data.prov_id,cur_data.last_hour last_hour,cur_data.data_en,cur_data.data_ch,ROUND((cur_data.count-seven_avg_data.count)/seven_avg_data.count,2) file_num_rate,ROUND((cur_data.size-seven_avg_data.size)/seven_avg_data.size,2) file_size_rate,cur_data.count count,ROUND(seven_avg_data.count,2) seven_avg_count,ROUND(cur_data.size,2) size,ROUND(seven_avg_data.size,2) seven_avg_size,cur_data.dt FROM ("
								+ " SELECT prov_id,'all' last_hour,SUM(count) count,SUM(size) size,'all' data_en,'all' data_ch,dt FROM oth_fix_file_count WHERE 1=1 AND prov_id='all' AND dt=DATE_FORMAT("
								+ dtString + ",'%Y%m%d') AND typ='b' AND data_en LIKE '" + type
								+ "%' GROUP BY prov_id UNION ALL "
								+ " SELECT prov_id,CONCAT(last_hour,':00') last_hour,SUM(count) count,SUM(size) size,'all' data_en,'all' data_ch,dt FROM oth_fix_file_count WHERE 1=1 AND dt=DATE_FORMAT("
								+ dtString + ",'%Y%m%d') AND typ='b' AND data_en LIKE '" + type
								+ "%' GROUP BY prov_id,last_hour UNION ALL "
								+ " SELECT prov_id,'all' last_hour,SUM(count) count,SUM(size) size,data_en,data_ch,dt FROM oth_fix_file_count WHERE 1=1 AND dt=DATE_FORMAT("
								+ dtString + ",'%Y%m%d') AND typ='b' AND data_en LIKE '" + type
								+ "%' GROUP BY prov_id,data_en UNION ALL "
								+ " SELECT prov_id,CONCAT(last_hour,':00') last_hour,SUM(count) count,SUM(size) size,CASE data_en WHEN prov_id<> 'all' THEN '全部' ELSE data_en END,CASE data_ch WHEN prov_id<> 'all' THEN '全部' ELSE data_ch END,dt FROM oth_fix_file_count WHERE 1=1 AND dt=DATE_FORMAT("
								+ dtString + ",'%Y%m%d') AND typ='b' AND data_en LIKE '" + type
								+ "%' GROUP BY prov_id,last_hour,data_en) cur_data LEFT JOIN ("
								+ " SELECT prov_id,'all' last_hour,SUM(count)/7 count,SUM(size)/7 size,'all' data_en,'all' data_ch FROM oth_fix_file_count WHERE 1=1 AND prov_id='all' AND dt< DATE_FORMAT("
								+ dtString + ",'%Y%m%d') AND dt> DATE_FORMAT(date_add(" + dtString
								+ ",INTERVAL-8 DAY),'%Y%m%d') AND typ='b' AND data_en LIKE '" + type
								+ "%' GROUP BY prov_id UNION ALL "
								+ " SELECT prov_id,CONCAT(last_hour,':00') last_hour,SUM(count)/7 count,SUM(size)/7 size,'all' data_en,'all' data_ch FROM oth_fix_file_count WHERE 1=1 AND dt< DATE_FORMAT("
								+ dtString + ",'%Y%m%d') AND dt> DATE_FORMAT(date_add(" + dtString
								+ ",INTERVAL-8 DAY),'%Y%m%d') AND typ='b' AND data_en LIKE '" + type
								+ "%' GROUP BY prov_id,last_hour UNION ALL "
								+ " SELECT prov_id,'all' last_hour,SUM(count)/7 count,SUM(size)/7 size,data_en,data_ch FROM oth_fix_file_count WHERE 1=1 AND dt< DATE_FORMAT("
								+ dtString
								+ ",'%Y%m%d') AND dt> DATE_FORMAT(date_add('20190315',INTERVAL-8 DAY),'%Y%m%d') AND typ='b' AND data_en LIKE 'FIX%' GROUP BY prov_id,data_en UNION ALL "
								+ " SELECT prov_id,CONCAT(last_hour,':00') last_hour,SUM(count)/7 count,SUM(size)/7 size,data_en,data_ch FROM oth_fix_file_count WHERE 1=1 AND dt< DATE_FORMAT("
								+ dtString + ",'%Y%m%d') AND dt> DATE_FORMAT(date_add(" + dtString
								+ ",INTERVAL-8 DAY),'%Y%m%d') AND typ='b' AND data_en LIKE '" + type
								+ "%' GROUP BY prov_id,last_hour,data_en) seven_avg_data ON cur_data.prov_id=seven_avg_data.prov_id AND cur_data.last_hour=seven_avg_data.last_hour AND cur_data.data_en=seven_avg_data.data_en) a "
								+ "WHERE 1=1" + whereString
								+ ") a LEFT JOIN prov_code_new p ON a.prov_id=p.oth_prov_id";
						List<Map<String, String>> repData = null;
						long t2 = System.currentTimeMillis();
						try {
							repData = resSer.getReportDataBySql("select concat(count(*),'') rows from (" + sql + ") a");
						} catch (Exception e) {
							maplist.put("msg1", "查询的sql count出现问题" + e.getMessage());
						}
						String rows = (repData.get(0).get("rows") + "");

						List<Map<String, String>> repHead = new ArrayList<Map<String, String>>();
						HashMap<String, String> hashMap = null;
						for (int i = 0; i < directFiledMapping.size(); i++) {
							Map<String, String> map = directFiledMapping.get(i);
							hashMap = new HashMap<String, String>();
							hashMap.put("field_name_EN", map.get("field_code"));
							hashMap.put("field_name_CN", map.get("field_name"));
							repHead.add(hashMap);
						}
						hashMap = new HashMap<String, String>();
						hashMap.put("field_name_EN", "file_num_rate");
						hashMap.put("field_name_CN", "文件个数波动率");
						repHead.add(hashMap);
						hashMap = new HashMap<String, String>();
						hashMap.put("field_name_EN", "file_size_rate");
						hashMap.put("field_name_CN", "文件大小波动率");
						repHead.add(hashMap);
						hashMap = new HashMap<String, String>();
						hashMap.put("field_name_EN", "last_hour");
						hashMap.put("field_name_CN", "时段");
						repHead.add(hashMap);
						String isColumn = request.getParameter("isColumn");
						String notColumn = request.getParameter("notColumn");
						if (isColumn != null) {
							ArrayList arrayList = new ArrayList();
							String[] split = isColumn.split(",");
							for (int i = 0; i < split.length; i++) {
								arrayList.add(split[i]);
							}
							repHead = instance.upIsColumn(repHead, arrayList);
						} else if (notColumn != null) {
							ArrayList arrayList = new ArrayList();
							String[] split = notColumn.split(",");
							for (int i = 0; i < split.length; i++) {
								arrayList.add(split[i]);
							}
							repHead = instance.upNotColumn(repHead, arrayList);
						}
						long t3 = System.currentTimeMillis();
						long t4 = 0;
						String pageNo = request.getParameter("pageNo");
						String pageSize = request.getParameter("pageSize");
						String order = request.getParameter("order");
						if (null != order && !"".equals(order) && !"null".equals(order)) {
							sql = "select a.* from (" + sql + ") a where 1 = 1 " + order;
						}
						try {
							if (pageNo != null && !"".equals(pageNo) && !"null".equals(pageNo) && pageSize != null
									&& !"".equals(pageSize) && !"null".equals(pageSize)) {
								repData = resSer.getReportDataBySql(sql + " limit " + pageNo + "," + pageSize);
							} else {
								repData = resSer.getReportDataBySql(sql);
							}
						} catch (Exception e) {
							maplist.put("msg1", "查询的sql data出现问题" + e.getMessage());
						}
						t4 = System.currentTimeMillis();
						// 获取报表表头信息
						fileName = request.getParameter("fileName") + "";
						if (null != fileName && !"".equals(fileName) && !"null".equals(fileName)) {
							fileName = fileName.replace("'", "");
							exportReportTemplete.returnXSSFWorkbookM(response, fileName, repHead, repData);
						} else {
							maplist.put("code", "1");
							maplist.put("msg", "查询成功");
							maplist.put("total", rows);
							maplist.put("Head", repHead);
							maplist.put("onlyAll", instance.getAllpv());
							maplist.put("rows", repData);

							if (!"1".equals(maplist.get("code"))) {
								maplist.put("code", 2);
								maplist.put("msg", "查询失败");
								maplist.put("rows", null);
							}
							String jsonstr = json.toJson(maplist);
							out.print(jsonstr);
						}
						System.out.println(
								" 加载sql时长: " + (t2 - t1) + " 加载行数和head信息时长: " + (t3 - t2) + "加载数据时长: " + (t4 - t3));
					} else {
						out.print(json.toJson(result));
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
				if (cb != null) {
					out.write(");");
				}
			}
		}
	}

	/**
	 * 进行对网间结算做出波动率占比计算
	 * 
	 * @param reportEN
	 * @param request
	 * @param response
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/getReportVolatility1", produces = "text/html;charset=UTF-8", params = "fieldNameEN")
	public void getMonitorMappingVolatility1(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("fieldVal") String fieldVal, @RequestParam("fieldNameEN") String fieldNameEN) {
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
			out1.print(jsonstr == null ? "null" : jsonstr);
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
			out1.print(jsonstr == null ? "null" : jsonstr);
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
			// 获取稽核报表基本信息
			Map<String, String> requestUrlmap = new HashMap<String, String>();// 用于存在全部的接口
			FiledProcFilter instance =new FiledProcFilter();
			String[] nameStr = null;// fileName 的List
			String[] fvalStr = null;// fileVal 的List
			String fileName = null;
			if (null != fieldNameEN && null != fieldVal) {
				nameStr = fieldNameEN.replaceAll("'", "").split(",");
				fvalStr = fieldVal.replaceAll("'", "").split(",");
			}
			// 判断该key下的值该角色是否有权限
			if (null != nameStr)
				for (int i = 0; i < nameStr.length; i++) {
					if ("prov_id".equals(nameStr[i]) && "pvct".equals(fvalStr[i])) {// 当传的全部的时候为空编码

					} else {
						requestUrlmap.put(nameStr[i], fvalStr[i]);
					}
					if (instance.getBl().contains(nameStr[i])) {// 在白名单之中
						continue;
					} else {
						String keyValue = fvalStr[i];
						if (null != keyValue && !checkJurExist(keyValue, request)) {
							result.setCode("2");
							result.setMsg("字段非法访问，操作已禁止");
							break;
						}
					}
				}
			instance.setRequestUrlmap(requestUrlmap);
			if (!"2".equals(result.getCode())) {
				String cb = request.getParameter("audit");
				if (cb != null) {
					response.setContentType("text/javascript");
				} else {
					response.setContentType("application/x-json");
				}
				PrintWriter out = null;
				// 如果该需求为特定需求 那么typechart =1 typechart：1 分省 2全国 3其他
				String typechart = "2";
				try {
					typechart = request.getParameter("typechart") == null ? typechart
							: request.getParameter("typechart");
				} catch (Exception e) {
					System.out.println("typechart 默认为 2 全部数据不进行分组 参数typechart 出现问题");
					e.printStackTrace();
				}
				try {
					out = response.getWriter();
					if (cb != null) {
						out.write(cb + "(");
					}
					String dtString = requestUrlmap.get("dt") != null
							? "'" + requestUrlmap.get("dt").split("-")[0] + "'"
							: "";
					String prov_code = requestUrlmap.get("prov_id");
					String time = requestUrlmap.get("time");
					List<Map<String, String>> directFiledMapping = resSer.getDirectFiledMapping(requestUrlmap);
					instance.setDirectFiledName(directFiledMapping);
					if (!"2".equals(result.getCode())) {
						Map maplist = new HashMap();
						long t1 = System.currentTimeMillis();
						String whereString = "";
						String sqlallString = "";
						if (time != null && !time.equals("all")) {
							whereString += " and time_span = '" + time + "' ";
						}
						if (prov_code == null) {// 全部的情况
							whereString += "";
						} else if (prov_code.equals("allpv")) {// 全国的情况
							whereString += "";
						} else {// 分省的情况
							whereString += " and prov_id='" + prov_code + "'";
						}
						try {
							// 日期汇总 当全国分省时用
							String sqlallString5 = " select cur_data.dt,cur_data.prov_id,'全部' time_span,cur_data.file_num file_num,(CASE WHEN cur_data.file_num=0 THEN '0' WHEN seven_avg_data.file_num=0 THEN '0' ELSE ROUND((cur_data.file_num-seven_avg_data.file_num)/seven_avg_data.file_num,2) END) file_num_rate,cur_data.file_size file_size,(CASE WHEN cur_data.file_size=0 THEN '0' WHEN seven_avg_data.file_size=0 THEN '0' ELSE ROUND((cur_data.file_size-seven_avg_data.file_size)/seven_avg_data.file_size,2) END) file_size_rate FROM ("
									+ " SELECT dt,time_span,prov_id,SUM(file_num) file_num,SUM(file_size) file_size FROM nsd_count_file_num_size WHERE 1=1 AND dt="
									+ dtString + whereString + " GROUP BY dt) cur_data LEFT JOIN ("
									+ " SELECT time_span,prov_id,SUM(file_num)/7 file_num,SUM(file_size)/7 file_size FROM nsd_count_file_num_size WHERE 1=1 "
									+ whereString + " and dt< " + dtString + " AND dt> DATE_SUB(" + dtString
									+ ",INTERVAL 8 DAY)) seven_avg_data ON cur_data.time_span=seven_avg_data.time_span";
							// 日间段进行汇总 分省当分省时用
							String sqlallString2 = " SELECT cur_data.dt,cur_data.prov_id,cur_data.time_span time_span,cur_data.file_num file_num,(CASE WHEN cur_data.file_num=0 THEN '0' WHEN seven_avg_data.file_num=0 THEN '0' ELSE ROUND((cur_data.file_num-seven_avg_data.file_num)/seven_avg_data.file_num,2) END) file_num_rate,cur_data.file_size file_size,(CASE WHEN cur_data.file_size=0 THEN '0' WHEN seven_avg_data.file_size=0 THEN '0' ELSE ROUND((cur_data.file_size-seven_avg_data.file_size)/seven_avg_data.file_size,2) END) file_size_rate FROM ("
									+ " SELECT dt,time_span,prov_id,SUM(file_num) file_num,SUM(file_size) file_size FROM nsd_count_file_num_size WHERE 1=1 AND dt="
									+ dtString + whereString + " GROUP BY dt,time_span) cur_data LEFT JOIN ("
									+ " SELECT time_span,prov_id,SUM(file_num)/7 file_num,SUM(file_size)/7 file_size FROM nsd_count_file_num_size WHERE 1=1 "
									+ whereString + " and dt< " + dtString + " AND dt> DATE_SUB(" + dtString
									+ ",INTERVAL 8 DAY) GROUP BY time_span) seven_avg_data ON cur_data.time_span=seven_avg_data.time_span ";
							// 日期汇总 //当全国全部时用
							String sqlallString1 = " select cur_data.dt,'all' prov_id,'全部' time_span,cur_data.file_num file_num,(CASE WHEN cur_data.file_num=0 THEN '0' WHEN seven_avg_data.file_num=0 THEN '0' ELSE ROUND((cur_data.file_num-seven_avg_data.file_num)/seven_avg_data.file_num,2) END) file_num_rate,cur_data.file_size file_size,(CASE WHEN cur_data.file_size=0 THEN '0' WHEN seven_avg_data.file_size=0 THEN '0' ELSE ROUND((cur_data.file_size-seven_avg_data.file_size)/seven_avg_data.file_size,2) END) file_size_rate FROM ("
									+ " SELECT dt,time_span,prov_id,SUM(file_num) file_num,SUM(file_size) file_size FROM nsd_count_file_num_size WHERE 1=1 AND dt="
									+ dtString + whereString + " GROUP BY dt) cur_data LEFT JOIN ("
									+ " SELECT time_span,prov_id,SUM(file_num)/7 file_num,SUM(file_size)/7 file_size FROM nsd_count_file_num_size WHERE 1=1 "
									+ whereString + " and dt< " + dtString + " AND dt> DATE_SUB(" + dtString
									+ ",INTERVAL 8 DAY)) seven_avg_data ON cur_data.time_span=seven_avg_data.time_span";
							// 日间段进行汇总 当全国 全部的时候用
							String sqlallString4 = " SELECT cur_data.dt,'all' prov_id,cur_data.time_span time_span,cur_data.file_num file_num,(CASE WHEN cur_data.file_num=0 THEN '0' WHEN seven_avg_data.file_num=0 THEN '0' ELSE ROUND((cur_data.file_num-seven_avg_data.file_num)/seven_avg_data.file_num,2) END) file_num_rate,cur_data.file_size file_size,(CASE WHEN cur_data.file_size=0 THEN '0' WHEN seven_avg_data.file_size=0 THEN '0' ELSE ROUND((cur_data.file_size-seven_avg_data.file_size)/seven_avg_data.file_size,2) END) file_size_rate FROM ("
									+ " SELECT dt,time_span,prov_id,SUM(file_num) file_num,SUM(file_size) file_size FROM nsd_count_file_num_size WHERE 1=1 AND dt="
									+ dtString + whereString + " GROUP BY dt,time_span) cur_data LEFT JOIN ("
									+ " SELECT time_span,prov_id,SUM(file_num)/7 file_num,SUM(file_size)/7 file_size FROM nsd_count_file_num_size WHERE 1=1 "
									+ whereString + " and dt< " + dtString + " AND dt> DATE_SUB(" + dtString
									+ ",INTERVAL 8 DAY) GROUP BY time_span) seven_avg_data ON cur_data.time_span=seven_avg_data.time_span ";
							// 全部
							String sqlallString3 = " SELECT cur_data.dt,cur_data.prov_id prov_id,cur_data.time_span time_span,cur_data.file_num file_num,(CASE WHEN cur_data.file_num=0 THEN '0' WHEN seven_avg_data.file_num=0 THEN '0' ELSE ROUND((cur_data.file_num-seven_avg_data.file_num)/seven_avg_data.file_num,2) END) file_num_rate,cur_data.file_size file_size,(CASE WHEN cur_data.file_size=0 THEN '0' WHEN seven_avg_data.file_size=0 THEN '0' ELSE ROUND((cur_data.file_size-seven_avg_data.file_size)/seven_avg_data.file_size,2) END) file_size_rate FROM ("
									+ " SELECT dt,prov_id,time_span,file_num,file_size FROM nsd_count_file_num_size WHERE 1=1 AND dt="
									+ dtString + whereString + ") cur_data LEFT JOIN ("
									+ " SELECT time_span,prov_id,SUM(file_num)/7 file_num,SUM(file_size)/7 file_size FROM nsd_count_file_num_size WHERE 1=1 "
									+ whereString + " and dt<" + dtString + " AND dt> DATE_SUB(" + dtString
									+ ",INTERVAL 8 DAY)group by prov_id,time_span) seven_avg_data ON cur_data.prov_id=seven_avg_data.prov_id AND cur_data.time_span=seven_avg_data.time_span ";
							if (prov_code == null) {// 全部的情况
								if (time != null && !time.equals("all")) {
									sqlallString = sqlallString4 + " union all " + sqlallString1;
								} else {
									sqlallString = sqlallString4 + " union all " + sqlallString1 + " union all "
											+ sqlallString3;
								}
							} else if (prov_code.equals("allpv")) {// 全国的情况
								if (time != null && !time.equals("all")) {
									sqlallString = sqlallString4;
								} else {
									sqlallString = sqlallString1 + " union all " + sqlallString4;
								}
							} else {// 分省的情况
								sqlallString = sqlallString2 + " union all " + sqlallString5;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						String sql = " SELECT a.dt,b.prov_name prov_id,a.time_span,ROUND(a.file_num,2) file_num,concat(ROUND(a.file_num_rate,2),'%') file_num_rate,ROUND(a.file_size/1024,2)file_size,concat(ROUND(a.file_size_rate,2),'%') file_size_rate "
								+ " FROM (" + sqlallString
								+ " ) a left join prov_code_new b on a.prov_id=b.crm_prov_id ";
						List<Map<String, String>> repData = null;
						long t2 = System.currentTimeMillis();
						try {
							repData = resSer.getReportDataBySql("select concat(count(*),'') rows from (" + sql + ") a");
						} catch (Exception e) {
							maplist.put("msg1", "查询的sql count出现问题" + e.getMessage());
						}
						String rows = (repData.get(0).get("rows") + "");
						List<Map<String, String>> repHead = new ArrayList<Map<String, String>>();
						HashMap<String, String> hashMap = null;
						for (int i = 0; i < directFiledMapping.size(); i++) {
							Map<String, String> map = directFiledMapping.get(i);
							hashMap = new HashMap<String, String>();
							hashMap.put("field_name_EN", map.get("field_code"));
							hashMap.put("field_name_CN", map.get("field_name"));
							repHead.add(hashMap);
						}
						hashMap = new HashMap<String, String>();
						hashMap.put("field_name_EN", "file_num_rate");
						hashMap.put("field_name_CN", "文件个数波动率");
						repHead.add(hashMap);
						hashMap = new HashMap<String, String>();
						hashMap.put("field_name_EN", "file_size_rate");
						hashMap.put("field_name_CN", "文件大小波动率");
						repHead.add(hashMap);
						hashMap = new HashMap<String, String>();
						hashMap.put("field_name_EN", "time_span");
						hashMap.put("field_name_CN", "时段");
						repHead.add(hashMap);
						String isColumn = request.getParameter("isColumn");
						String notColumn = request.getParameter("notColumn");
						if (isColumn != null) {
							ArrayList arrayList = new ArrayList();
							String[] split = isColumn.split(",");
							for (int i = 0; i < split.length; i++) {
								arrayList.add(split[i]);
							}
							repHead = instance.upIsColumn(repHead, arrayList);
						} else if (notColumn != null) {
							ArrayList arrayList = new ArrayList();
							String[] split = notColumn.split(",");
							for (int i = 0; i < split.length; i++) {
								arrayList.add(split[i]);
							}
							repHead = instance.upNotColumn(repHead, arrayList);
						}
						long t3 = System.currentTimeMillis();
						long t4 = 0;
						String pageNo = request.getParameter("pageNo");
						String pageSize = request.getParameter("pageSize");
						String order = request.getParameter("order");
						if (null != order && !"".equals(order) && !"null".equals(order)) {
							sql = "select a.* from (" + sql + ") a where 1 = 1 " + order;
						}
						try {
							if (pageNo != null && !"".equals(pageNo) && !"null".equals(pageNo) && pageSize != null
									&& !"".equals(pageSize) && !"null".equals(pageSize)) {
								repData = resSer.getReportDataBySql(sql + " limit " + pageNo + "," + pageSize);
							} else {
								repData = resSer.getReportDataBySql(sql);
							}
						} catch (Exception e) {
							maplist.put("msg1", "查询的sql data出现问题" + e.getMessage());
						}
						t4 = System.currentTimeMillis();
						// 获取报表表头信息
						fileName = request.getParameter("fileName") + "";
						if (null != fileName && !"".equals(fileName) && !"null".equals(fileName)) {
							fileName = fileName.replace("'", "");
							exportReportTemplete.returnXSSFWorkbookM(response, fileName, repHead, repData);
						} else {
							maplist.put("code", "1");
							maplist.put("msg", "查询成功");
							maplist.put("total", rows);
							maplist.put("Head", repHead);
							maplist.put("onlyAll", instance.getAllpv());
							maplist.put("rows", repData);

							if (!"1".equals(maplist.get("code"))) {
								maplist.put("code", 2);
								maplist.put("msg", "查询失败");
								maplist.put("rows", null);
							}
							String jsonstr = json.toJson(maplist);
							out.print(jsonstr);
						}
						System.out.println(
								" 加载sql时长: " + (t2 - t1) + " 加载行数和head信息时长: " + (t3 - t2) + "加载数据时长: " + (t4 - t3));
					} else {
						out.print(json.toJson(result));
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
				if (cb != null) {
					out.write(");");
				}
			}
		}
	}

	/**
	 * 进行对 表中的一个字段做出唯一的值
	 * 
	 * @param reportEN
	 * @param request
	 * @param response
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/getFiledVal", produces = "text/html;charset=UTF-8", params = "fieldNameEN")
	public void getFiledMappingVals(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("fieldVal") String fieldVal, @RequestParam("fieldNameEN") String fieldNameEN) {
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
			out1.print(jsonstr == null ? "null" : jsonstr);
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
			out1.print(jsonstr == null ? "null" : jsonstr);
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
			// 获取稽核报表基本信息
			Map<String, String> requestUrlmap = new HashMap<String, String>();// 用于存在全部的接口
			FiledProcFilter instance = new FiledProcFilter();
			String[] nameStr = null;// fileName 的List
			String[] fvalStr = null;// fileVal 的List
			if (null != fieldNameEN && null != fieldVal) {
				nameStr = fieldNameEN.replaceAll("'", "").split(",");
				fvalStr = fieldVal.replaceAll("'", "").split(",");
			}
			// 判断该key下的值该角色是否有权限
			if (null != nameStr)
				for (int i = 0; i < nameStr.length; i++) {
					requestUrlmap.put(nameStr[i], fvalStr[i]);
					if (instance.getBl().contains(nameStr[i])) {// 在白名单之中
						continue;
					} else {
						String keyValue = fvalStr[i];
						if (null != keyValue && !checkJurExist(keyValue, request)) {
							result.setCode("2");
							result.setMsg("字段非法访问，操作已禁止");
							break;
						}
					}
				}
			if (!"2".equals(result.getCode())) {
				String cb = request.getParameter("audit");
				if (cb != null) {
					response.setContentType("text/javascript");
				} else {
					response.setContentType("application/x-json");
				}
				PrintWriter out = null;
				try {
					out = response.getWriter();
					if (cb != null) {
						out.write(cb + "(");
					}
					String where = "";
					for (String keyString : requestUrlmap.keySet()) {
						where += " and " + keyString + "='" + requestUrlmap.get(keyString) + "'";
					}
					String tableName = request.getParameter("tableName");
					String fileds = request.getParameter("fileds");
					if (!"2".equals(result.getCode())) {
						String sql = " select " + fileds + " from " + tableName + " where 1 = 1 " + where + " group by "
								+ fileds;
						Map maplist = new HashMap();
						List<Map<String, String>> repData = resSer.getReportDataBySql(sql);
						String[] split = fileds.split(",");
						ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
						HashMap<String, String> hashMap = new HashMap<String, String>();
						TreeSet<String> set = new TreeSet<String>();
						for (int i = 0; i < split.length; i++) {
							String field = split[i];
							for (int j = 0; j < repData.size(); j++) {
								Map<String, String> map = repData.get(j);
								set.add(map.get(field));
							}
							Iterator<String> iterator = set.iterator();
							while (iterator.hasNext()) {
								String f = iterator.next();
								hashMap.put(f, f);
							}
							arrayList.add(hashMap);
							hashMap = new HashMap<String, String>();
							maplist.put(field, arrayList);
							arrayList = new ArrayList<HashMap<String, String>>();
						}
						maplist.put("code", "1");
						maplist.put("msg", "查询成功");
						maplist.put("rows", repData);
						String jsonstr = json.toJson(maplist);
						out.print(jsonstr);
					} else {
						out.print(json.toJson(result));
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
				if (cb != null) {
					out.write(");");
				}
			}
		}
	}

}