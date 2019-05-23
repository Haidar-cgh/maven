package com.dw.common;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.weaver.ast.Var;

public class FiledProcFilter {
	private List<Map<String, String>> directFiledName;// field_code,field_name,filed_as_Map,filed_type,typechart
	private Map<String, String> directFiledsMap;// [{a.get("field_code"), a.get("field_name")}] 存入的是field的code和name
	private List<String> filedName;// 源表
	private List<String> filedNames;// 用到的字段
	private Map<String, String> requestUrlmap;
	private Map<String, String> provCode;
	private List<Map<String, String>> pv;
	private List<Map<String, String>> ct;
	private Map<String, String> pvMap;
	private Map<String, String> ctMap;
	private Boolean allpv = false;
	private String unit = "%";
	private String dbTypechart = null;
	private String whereString = "";
	private String groupString = "";
	private List<Map<String, String>> headList = null;
	private String dtString = null;
	private String prString = null;
	private String ctString = null;
	private Boolean isPrEN = true;
	private Boolean isCtEN = true;
	private ArrayList<String> notrToc = null;
	private ArrayList<String> notFtMap = null;

	public ArrayList<String> getDirectsql() {
		ArrayList<String> returnsql = new ArrayList<String>();
		try {
			// 获取数据库中表可以匹配到的字段名称
			String pr = prString;
			String ct = ctString;
			String dt = dtString;
			// 进行做出where 条件
			String where = "";
			// filed 字段中的配置
			String dts = "";
			String prs = "";
			String cts = "";
			headList = new ArrayList<Map<String, String>>();
			Map<String, String> map = new HashMap<String, String>();

			if (dt != null && filedName.contains(dt)) {
				dts = dt;
				map = new HashMap<String, String>();
				map.put("field_name_EN", dt);
				map.put("field_name_CN", "账期");
				headList.add(map);
				where += " and " + dt + " <> '' ";
				where += " and " + dt + " <> 'null' ";
				where += " and " + dt + " is not null ";
			}
			if (pr != null && filedName.contains(pr)) {
				prs = isPrEN ? "," + getpvtoC(pr) + " " + pr : "," + pr;
				map = new HashMap<String, String>();
				map.put("field_name_EN", pr);
				map.put("field_name_CN", "省份");
				headList.add(map);
				where += " and " + pr + " <> '' ";
				where += " and " + pr + " <> 'null' ";
				where += " and " + pr + " is not null ";
			}
			if (ct != null && filedName.contains(ct)) {
				cts = isCtEN ? "," + getcttoC(ct) + " " + ct : "," + ct;
				map = new HashMap<String, String>();
				map.put("field_name_EN", ct);
				map.put("field_name_CN", "城市");
				headList.add(map);
				where += " and " + ct + " <> '' ";
				where += " and " + ct + " <> 'null' ";
				where += " and " + ct + " is not null ";
			}
			// 拼接sql的前期工作
			String sql1 = getDirectAll();
			String sql2 = getDirectAllPvCt();
			String sql3 = getDirectAllCt();
			String group = "" + getDirectGroupby();

			String fields = dts + prs + cts;
			String rePCcode = requestUrlmap.get("rePCcode");
			if (null != rePCcode && Boolean.parseBoolean(rePCcode)) {// 当存在这个参数的时候把其它的省份进行进行翻译
				try {
					List<String> keyList = new ArrayList<String>(getRequestUrlmap().keySet());// 获取条件中的参数
					if (null == getDbTypechart()) {// 当无编号的时候进行用字段以及filed_as_Map,field_code两个为依据如果有filed_as_Map不用field_code
						for (int i = 0; i < directFiledName.size(); i++) {
							Map<String, String> map1 = directFiledName.get(i);
							String field_code = map1.get("field_code");
							if (!keyList.contains(field_code) && !field_code.equals(pr) && bpr.contains(field_code)) {// 当不为[省份/地市/条件]中的参数的时候进行写入
								fields += "," + getpvtoC(field_code) + " " + field_code;
								group += group == "" ? field_code : "," + field_code;
							} else if (!keyList.contains(field_code) && !field_code.equals(ct)
									&& bct.contains(field_code)) {
								fields += "," + getcttoC(field_code) + " " + field_code;
								group += group == "" ? field_code : "," + field_code;
							}
						}
					} else {
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				List<String> keyList = new ArrayList<String>(getRequestUrlmap().keySet());// 获取条件中的参数
				if (null == getDbTypechart()) {// 当无编号的时候进行用字段以及filed_as_Map,field_code两个为依据如果有filed_as_Map不用field_code
					for (int i = 0; i < directFiledName.size(); i++) {
						Map<String, String> map1 = directFiledName.get(i);
						String filed_as_Map = map1.get("filed_as_Map");
						String field_code = map1.get("field_code");
						String a = filed_as_Map != null && !filed_as_Map.toLowerCase().equals("null")
								&& !filed_as_Map.equals("") ? filed_as_Map : field_code;
						if (getNotFtMap() != null && getNotFtMap().size() > 0 && getNotFtMap().contains(field_code)) {
							fields += fields == "" ? field_code : "," + field_code;
						} else if (!keyList.contains(field_code) && !getBl().contains(field_code)
								&& !getBpr().contains(field_code) && !getBct().contains(field_code)) {// 当不为[省份/地市/条件]中的参数的时候进行写入
							fields += fields == "" ? a : "," + a;
						}
					}
				} else {

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 获取传入的参数中的部分特殊值
			String pvS = requestUrlmap.get("prov_id");
			String ctS = requestUrlmap.get("city_id");
			System.out.println(pvS + ":pvS======ctS:" + ctS);
			String prCodeName = "";
			String ctCodeName = "";
			try {
				prCodeName = getPrCodeName();
			} catch (Exception e) {
			}
			try {
				ctCodeName = getCtCodeName();
			} catch (Exception e) {
			}

			String isAll = requestUrlmap.get("isAll");

			String isSumm = requestUrlmap.get("isSumm");
			// 进行加载出可以用的字段

			for (String key : requestUrlmap.keySet()) {
				if (!getBl().contains(key)) {// 不在白名单中的其它字段
					fields += fields == "" ? key : "," + key;// 进行装载字段
					map = new HashMap<String, String>();
					map.put("field_name_EN", key);
					map.put("field_name_CN", directFiledsMap.get(key) == null ? key : directFiledsMap.get(key));
					headList.add(map);// 加载请求条件中的字段到 table 的字段名称
				}
			}
			upFields();// 进行对下载的字段名称/table的字段名称进行追加

			// 前期工作完成
			if (!filedName.contains(ct) || null == ctS) {// 不分地市的情况下
				if (getAllpv() || (isSumm != null && !Boolean.parseBoolean(isSumm))) {// 全国或者手动关闭汇总数据
					// 是否是全部全国的数据如果是则进行消除一条sql防止出现两条数据
					sql2 = "";
					sql3 = "";
				}
				if (isAll != null && !Boolean.parseBoolean(isAll)) {// 全国或者手动关闭汇总数据
					// 是否是全部全国的数据如果是则进行消除一条sql防止出现两条数据
					sql1 = "";
				}
				Boolean isallT = false;
				for (String key : requestUrlmap.keySet()) {
					if (!getBl().contains(key)) {// 除了指定的字段之外 其它的字段是条件 当这个条件全部的时候传入的参数是 all
						if (requestUrlmap.get(key).toLowerCase().equals("all")) { // 当进行筛选的时候进行加入where
							isallT = true;// 存在的时候是一个省份多条数据进行省份汇总一份数据
						}
					}
				}
				if (!isallT) {
					sql3 = "";
				}
				if (null != pvS && !pvS.equals("allpv")) {
					sql2 = "";
					where += " and " + prString + "='" + prCodeName + "' ";
				} else if (null != pvS && pvS.equals("allpv") && !getAllpv()) {
					sql1 = "";
					sql3 = "";
				}
				String sqlString = "";
				if (!sql1.equals("")) {
					sqlString = sqlString.equals("") ? sql1 : sqlString + " union all  " + sql1;
				}
				if (!sql2.equals("")) {
					sqlString = sqlString.equals("") ? sql2 : sqlString + " union all  " + sql2;
				}
				if (!sql3.equals("")) {
					sqlString = sqlString.equals("") ? sql3 : sqlString + " union all  " + sql3;
				}
				// 对外开放的参数
				Boolean isWhere = requestUrlmap.get("isWhere") == null ? true
						: Boolean.parseBoolean(requestUrlmap.get("isWhere"));
				if (!getWhereString().equals("") && isWhere)
					where += " and " + getWhereString();
				if (!getGroupString().equals(""))
					group = getGroupString();
				group = group == "" ? "" : " group by "+ group;
				returnsql.add("select " + fields + " from (" + sqlString + ") a where 1=1 " + where + "" + group);
				returnsql.add("(" + sqlString + ")");
			} else {// 分地市的情况
				if (getAllpv() || (isSumm != null && !Boolean.parseBoolean(isSumm))) {// 全国或者手动关闭汇总数据
					sql2 = "";
					sql3 = "";
				}
				if (isAll != null && !Boolean.parseBoolean(isAll)) {// 全部的数据进行手动关闭
					sql1 = "";
				}
				if (pvS == null && ctS.equals("allct")) {// 全部 + 全省 (n + 31 + 1) * dt
					// 由于 sql 已经把 全部 + 省份 + 地市的 已经写好了 不用进行操作
				} else if (pvS == null && !ctS.equals("allct")) { // 全部 + 地市 1(单地市) * dt
					sql2 = "";
					sql3 = "";
					where += " and " + ct + "='" + ctCodeName + "'";
				} else if (pvS.equals("allpv") && ctS.equals("allct")) { // 全国1 (汇总) * dt
					sql1 = "";
					sql3 = "";
				} else if (pvS.equals("allpv") && !ctS.equals("allct")) { // 全国 + 地市 1(单地市) * dt
					sql2 = "";
					sql3 = "";
					where += " and " + ct + "='" + ctCodeName + "'";
				} else if (!pvS.equals("allpv") && ctS.equals("allct")) { // 省份 + 全省 ( 1(省份汇总) + n(地市) ) * dt
					sql2 = "";
					where += " and " + pr + "='" + prCodeName + "'";
				} else if (!pvS.equals("allpv") && !ctS.equals("allct")) { // 省份 + 地市 1(单地市) * dt
					sql2 = "";
					sql3 = "";
					where += " and " + pr + "='" + prCodeName + "' and " + ct + "='" + ctCodeName + "'";
				}
				String sqlString = "";
				if (!sql1.equals("")) {
					sqlString = sqlString.equals("") ? sql1 : sqlString + " union all  " + sql1;
				}
				if (!sql2.equals("")) {
					sqlString = sqlString.equals("") ? sql2 : sqlString + " union all  " + sql2;
				}
				if (!sql3.equals("")) {
					sqlString = sqlString.equals("") ? sql3 : sqlString + " union all  " + sql3;
				}
				// 对外开放的参数
				if (!getWhereString().equals(""))
					where += " and " + getWhereString();
				if (!getGroupString().equals(""))
					group = getGroupString();
				group = group == "" ? "" : " group by "+ group;
				returnsql.add("select " + fields + " from (" + sqlString + ") a where 1=1 " + where + group);
				returnsql.add("(" + sqlString + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnsql;
	}

	public String getRTOCSql(List<Map<String, String>> rToCsqlFileds, String sql) {
		headList = new ArrayList<Map<String, String>>();
		Map<String, String> map = new HashMap<String, String>();
		String pr = prString;
		String ct = ctString;
		String dt = dtString;
		String filedString = "";
		String group = " group by ";
		try {
			if (dt != null && filedName.contains(dt)) {
				map = new HashMap<String, String>();
				map.put("field_name_EN", dt);
				map.put("field_name_CN", "账期");
				headList.add(map);
				filedString += dt;
				group += dt;
			}
			if (pr != null && filedName.contains(pr)) {
				map = new HashMap<String, String>();
				map.put("field_name_EN", pr);
				map.put("field_name_CN", "省份");
				headList.add(map);
				filedString += "," + pr;
				group += "," + pr;
			}
			if (ct != null && filedName.contains(ct)) {
				map = new HashMap<String, String>();
				map.put("field_name_EN", ct);
				map.put("field_name_CN", "城市");
				headList.add(map);
				filedString += "," + ct;
				group += "," + ct;
			}
			List<String> reStrings = new ArrayList<String>(requestUrlmap.keySet());
			for (int i = 0; i < reStrings.size(); i++) {
				if (!getBl().contains(reStrings.get(i))) {
					group += "," + reStrings.get(i);
					filedString += "," + reStrings.get(i);
					map = new HashMap<String, String>();
					map.put("field_name_EN", reStrings.get(i));
					map.put("field_name_CN", directFiledsMap.get(reStrings.get(i)) == null ? reStrings.get(i)
							: directFiledsMap.get(reStrings.get(i)));
					headList.add(map);
				}
			}
			String rTocString = requestUrlmap.get("rToc");
			ArrayList<String> codeArrayList = new ArrayList<String>();
			for (int i = 0; i < directFiledName.size(); i++) {
				Map<String, String> map1 = directFiledName.get(i);
				String a = map1.get("filed_as_Map") != null && !map1.get("filed_as_Map").toLowerCase().equals("null")
						&& !map1.get("filed_as_Map").equals("") ? map1.get("filed_as_Map") : null;
				String code = map1.get("field_code");// 字段编码
				if (getNotrToc() != null && getNotrToc().contains(code) && getNotrToc().contains(code)) {
					filedString += "," + code;
					map = new HashMap<String, String>();
					map.put("field_name_EN", code);
					map.put("field_name_CN", map1.get("field_name"));
					headList.add(map);
				} else if (a != null) {// 当不为[省份/地市/条件]中的参数的时候进行写入
					codeArrayList.add(code);
				} else if (!rTocString.equals(code) && !reStrings.contains(code) && !getBl().contains(code)) {
					group += "," + code;
					filedString += "," + code;
				}
			}
			String copuString = "";
			if (null != requestUrlmap.get("copu")) {
				copuString = requestUrlmap.get("copu");
			}
			for (int j = 0; j < rToCsqlFileds.size(); j++) {
				for (String code : codeArrayList) {
					Map<String, String> map2 = rToCsqlFileds.get(j);
					String nameCN = directFiledsMap.get(code) == null ? map2.get("rToc") + copuString + code
							: map2.get("rToc") + copuString + directFiledsMap.get(code);
					String nameEN = map2.get("rToc") + "" + code;
					filedString += ",MAX(CASE " + rTocString + " WHEN '" + map2.get("rToc") + "' THEN " + code
							+ " ELSE '0' END ) '" + nameEN + "'";
					map = new HashMap<String, String>();
					map.put("field_name_EN", nameEN);
					map.put("field_name_CN", nameCN);
					headList.add(map);
				}
			}
			sql = "select " + filedString + " from (" + sql + ") a where 1 = 1 " + group;
		} catch (Exception e) {
			filedString = "";
			e.printStackTrace();
		}
		return sql;
	}

	/**
	 * 获取ds_XXX_field表中的字段 进行做出group by字段 对没有配置 filed_as_Map 以及hivename字段进行过滤
	 * field_code,field_name,filed_as_Map,filed_type,typechart
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getDirectGroupby() {
		String group = "";
		try {
			for (Iterator iterator = directFiledName.iterator(); iterator.hasNext();) {
				Map<String, String> next = (Map<String, String>) iterator.next();
				String s = next.get("field_code");
				if (null == next.get("filed_as_Map") || next.get("filed_as_Map").equals("")) {
					if (!bhb.contains(s)) {// && bl.contains(s)
						group += group == "" ? s : "," + s;
					}
				}
			}
		} catch (Exception e) {
			group = "";
			e.printStackTrace();
		}
		return group;
	}

	/**
	 * Direct 获取省份的数据
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getDirectAllCt() {
		String sql = "";
		String dt = dtString;
		String pr = prString;
		String ct = ctString;
		String prCodeName = "";
		String ctCodeName = "";
		try {
			prCodeName = getPrCodeName();
		} catch (Exception e) {
		}
		try {
			ctCodeName = getCtCodeName();
		} catch (Exception e) {
		}
		String tb = requestUrlmap.get("tableName");
		String hb = requestUrlmap.get("hiveName");
		String allct = isCtEN ? "allct" : "全省";
		try {
			String Fields = getLDirectToFields();
			String where = "";
			if (dt != null) {// 如果有日期进行增加日期
				Fields = Fields == "" ? dt : dt + "," + Fields;
				String[] dtt = requestUrlmap.get("dt").split("-");
				where += " AND " + dt + " >= '" + dtt[0].trim() + "' AND " + dt + " <= '" + dtt[1].trim() + "'";
			}
			// 装载 group where fields 三个条件
			if (!filedName.contains(ct) && pr != null) {// 不分地市的情况下
				Fields += Fields == "" ? pr : "," + pr;
			} else {// 分地市的情况
				if (pr != null)
					Fields += Fields == "" ? pr : "," + pr;
				if (ct != null)
					Fields += Fields == "" ? "'" + allct + "' " + ct : ",'" + allct + "' " + ct;
			}
			if (null != pr && null != requestUrlmap.get("prov_id") && !requestUrlmap.get("prov_id").equals("allpv")) {
				where += " AND " + pr + "='" + prCodeName + "'";
			}
			if (null != ct && null != requestUrlmap.get("city_id") && !requestUrlmap.get("city_id").equals("allct")) {
				where += " AND " + ct + "='" + ctCodeName + "'";
			}
			for (String key : requestUrlmap.keySet()) {
				if (!getBl().contains(key)) {// 不在白名单中的其它字段
					if (requestUrlmap.get(key).toLowerCase().equals("all")) {
						Fields += Fields == "" ? "'all' " + key : ",'all' " + key;
					} else {
						where += " AND " + key + "='" + requestUrlmap.get(key) + "'";
						Fields += Fields == "" ? key : "," + key;
					}
				}
			}
			if (hb != null) {// 如果有 hive 表增加where
				where += " AND " + getBhb() + "='" + hb + "'";
			}
			String group = " GROUP BY " + dtString + "," + prString;
//			for (Iterator iterator = directFiledName.iterator(); iterator.hasNext();) {
//				Map<String, String> next = (HashMap<String, String>) iterator.next();
//				String s = next.get("field_code");
//				if (null == next.get("filed_as_Map") || next.get("filed_as_Map").equals("")) {
//					if (!getBl().contains(s) && !requestUrlmap.containsKey(s)) {
//						group += "," + s;
//					}
//				}
//			}
			if (!getWhereString().equals(""))
				where += " and " + getWhereString();
			if (!getGroupString().equals(""))
				group += group == "" ? getGroupString() : "," + getGroupString();
			if (tb != null) {// 表名不为空的时候进行拼接进行写入 拼接sql
				sql = "SELECT " + Fields + " FROM " + tb + " WHERE 1 = 1 " + where + group;
			} else {
				sql = "";
			}
		} catch (Exception e) {
			sql = "";
			e.printStackTrace();
		}
		return sql;
	}

	/**
	 * Direct 获取全部全国汇总的数据 这个数据进行数据的汇总
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getDirectAllPvCt() {
		String sql = "";
		String dt = dtString;
		String pr = prString;
		String ct = ctString;
		String tb = requestUrlmap.get("tableName");
		String hb = requestUrlmap.get("hiveName");
		String prCodeName = "";
		String ctCodeName = "";
		try {
			prCodeName = getPrCodeName();
		} catch (Exception e) {
		}
		try {
			ctCodeName = getCtCodeName();
		} catch (Exception e) {
		}
		String allct = isCtEN ? "allct" : "全省";
		String allpv = isPrEN ? "allpv" : "全国";
		try {
			String Fields = getLDirectToFields();
			String where = "";
			// 装载 where,group条件
			if (dt != null) {// 如果有日期进行增加日期
				Fields = Fields == "" ? dt : dt + "," + Fields;
				String[] dtt = requestUrlmap.get("dt").split("-");
				where += " AND " + dt + " >= '" + dtt[0].trim() + "' AND " + dt + " <= '" + dtt[1].trim() + "'";
			}
			// 装载 group where fields 三个条件
			if (!filedName.contains(ct) && pr != null) {// 不分地市的情况下
				Fields += Fields == "" ? "'" + allpv + "' " + pr : ",'" + allpv + "' " + pr;
			} else {// 分地市的情况
				if (pr != null)
					Fields += Fields == "" ? "'" + allpv + "' " + pr : ",'" + allpv + "' " + pr;
				if (ct != null)
					Fields += Fields == "" ? "'" + allct + "' " + ct : ",'" + allct + "' " + ct;
			}
			if (null != pr && null != requestUrlmap.get("prov_id") && !requestUrlmap.get("prov_id").equals("allpv")) {
				where += " AND " + pr + "='" + prCodeName + "'";
			}
			if (null != ct && null != requestUrlmap.get("city_id") && !requestUrlmap.get("city_id").equals("allct")) {
				where += " AND " + ct + "='" + ctCodeName + "'";
			}

			for (String key : requestUrlmap.keySet()) {
				if (!getBl().contains(key)) {// 不在白名单中的其它字段
					if (requestUrlmap.get(key).toLowerCase().equals("all")) {
						Fields += Fields == "" ? "'all' " + key : ",'all' " + key;
					} else {
						where += " AND " + key + "='" + requestUrlmap.get(key) + "'";
						Fields += Fields == "" ? key : "," + key;
					}
				}
			}
			if (hb != null) {// 如果有 hive 表增加where
				where += " AND " + getBhb() + "='" + hb + "'";
			}
			String group = " group by " + dtString;
//			for (Iterator iterator = directFiledName.iterator(); iterator.hasNext();) {
//				Map<String, String> next = (HashMap<String, String>) iterator.next();
//				String s = next.get("field_code");
//				if (null == next.get("filed_as_Map") || next.get("filed_as_Map").equals("")) {
//					if (!getBl().contains(s) && !requestUrlmap.containsKey(s)) {
//						group += "," + s;
//					}
//				}
//			}
			if (!getWhereString().equals(""))
				where += " and " + getWhereString();
			if (!getGroupString().equals(""))
				group += group == "" ? getGroupString() : "," + getGroupString();
			// 表名不为空的时候进行拼接进行写入 拼接sql
			if (tb != null) {
				sql = "SELECT " + Fields + " FROM " + tb + " WHERE 1 = 1 " + where + group;
			} else {
				sql = "";
			}
		} catch (Exception e) {
			sql = "";
			e.printStackTrace();
		}
		return sql;
	}

	/**
	 * Direct 获取全部的数据 当别的筛选向 不为all的时候进行加入筛选向也就是这个条件只对非all的条件起做用
	 * 
	 * @return
	 */
	public String getDirectAll() {
		String sql = "";
		try {
			// 获取 对象中的参数信息
			String dt = dtString;
			String pr = prString;
			String ct = ctString;
			String tb = requestUrlmap.get("tableName");
			String hb = requestUrlmap.get("hiveName");
			String prCodeName = "";
			String ctCodeName = "";
			try {
				prCodeName = getPrCodeName();
			} catch (Exception e) {
			}
			try {
				ctCodeName = getCtCodeName();
			} catch (Exception e) {
			}

			String Fields = getLDirectToFields();
			String where = "";
			// 进行装载 fileds 字段
			// 装载 where 条件
			if (dt != null) {// 如果有日期进行增加日期
				Fields = Fields == "" ? dt : dt + "," + Fields;
				String[] dtt = requestUrlmap.get("dt").split("-");
				where += " AND " + dt + " >= '" + dtt[0].trim() + "' AND " + dt + " <= '" + dtt[1].trim() + "'";
			}
			if (hb != null) {// 如果有 hive 表增加where
				where += " AND " + getBhb() + "='" + hb + "'";
			}
			// 装载 group where fields 三个条件
			if (!filedName.contains(ct) && pr != null) {// 不分地市的情况下
				Fields += Fields == "" ? pr : "," + pr;
			} else {// 分地市的情况
				if (pr != null)
					Fields += Fields == "" ? pr : "," + pr;
				if (ct != null)
					Fields += Fields == "" ? ct : "," + ct;
			}
			if (null != pr && null != requestUrlmap.get("prov_id") && !requestUrlmap.get("prov_id").equals("allpv")) {
				where += " AND " + pr + "='" + prCodeName + "'";
			}
			if (null != ct && null != requestUrlmap.get("city_id") && !requestUrlmap.get("city_id").equals("allct")) {
				where += " AND " + ct + "='" + ctCodeName + "'";
			}
			for (String key : requestUrlmap.keySet()) {
				if (!getBl().contains(key)) {// 除了指定的字段之外 其它的字段是条件 当这个条件全部的时候传入的参数是 all
					Fields += Fields == "" ? key : "," + key;
					if (!requestUrlmap.get(key).toLowerCase().equals("all")) { // 当进行筛选的时候进行加入where
						where += " AND " + key + "='" + requestUrlmap.get(key) + "'";
					}
				}
			}
			if (!getWhereString().equals(""))
				where += " and " + getWhereString();
			// 表名不为空的时候进行拼接进行写入 拼接sql
			if (tb != null) {
				sql = "SELECT " + Fields + " FROM " + tb + " WHERE 1 = 1 " + where + " GROUP BY " + getDirectGroupby();
			} else {
				sql = "";
			}
		} catch (Exception e) {
			sql = "";
			e.printStackTrace();
		}
		return sql;
	}

	/**
	 * Direct 获取查询的字段列 排除条件中的字段 省份/地市/条件 中的参数字段
	 * 
	 * @return
	 */
	public String getLDirectToFields() {
		String field = "";
		try {
			List<String> keyList = new ArrayList<String>(getRequestUrlmap().keySet());// 获取条件中的参数
			if (null == getDbTypechart()) {// 当无编号的时候进行用字段以及filed_as_Map,field_code两个为依据如果有filed_as_Map不用field_code
				for (int i = 0; i < directFiledName.size(); i++) {
					Map<String, String> map = directFiledName.get(i);
					String filed_as_Map = map.get("filed_as_Map");
					String field_code = map.get("field_code");
					String a = filed_as_Map != null && !filed_as_Map.toLowerCase().equals("null")
							&& !filed_as_Map.equals("") ? filed_as_Map : field_code;
					if (getNotFtMap() != null && getNotFtMap().size() > 0 && getNotFtMap().contains(field_code)) {
						field += field == "" ? field_code : "," + field_code;
					} else if (!keyList.contains(field_code) && !getBl().contains(field_code) && !getBpr().contains(field_code)
							&& !getBct().contains(field_code)) {// 当不为[省份/地市/条件]中的参数的时候进行写入
						field += field == "" ? a : "," + a;
					}
				}
				String rePCcode = requestUrlmap.get("rePCcode");
				try {
					if (rePCcode != null && Boolean.parseBoolean(rePCcode) && null == getDbTypechart()) {// 当无编号的时候进行用字段以及filed_as_Map,field_code两个为依据如果有filed_as_Map不用field_code
						for (int i = 0; i < directFiledName.size(); i++) {
							Map<String, String> map1 = directFiledName.get(i);
							String field_code = map1.get("field_code");
							if (!keyList.contains(field_code) && !field_code.equals(prString)
									&& bpr.contains(field_code)) {
								field += "," + field_code;
							} else if (!keyList.contains(field_code) && !field_code.equals(ctString)
									&& bct.contains(field_code)) {
								field += "," + field_code;
							}
						}
					} else {
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {

			}
		} catch (

		Exception e) {
			field = "";
			e.printStackTrace();
		}
		return field;
	}

	/**
	 * 在查询中对字段进行加载 field 如果字段改变 请同 getLDirectToFields 一起改变
	 */
	public void upFields() {
		try {
			HashMap<String, String> headMap = null;
			String tbString = getBhb();
			List<String> keyList = new ArrayList<String>(getRequestUrlmap().keySet());// 获取条件中的参数
			if (null == getDbTypechart()) {// 当无编号的时候进行用字段以及filed_as_Map,field_code两个为依据如果有filed_as_Map不用field_code
				for (int i = 0; i < directFiledName.size(); i++) {
					Map<String, String> map = directFiledName.get(i);
					String code = map.get("field_code");
					if (!code.equals(tbString) && !keyList.contains(code) && !code.equals(prString)
							&& !code.equals(ctString) && !code.equals(dtString)) {// 当不为[省份/地市/条件]中的参数的时候进行写入
						headMap = new HashMap<String, String>();
						headMap.put("field_name_EN", map.get("field_code"));
						headMap.put("field_name_CN", map.get("field_name"));
						headList.add(headMap);
					}
				}
			} else {

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取查询条件下对省份进行过滤的 sql 查询否为 只有全国数据.
	 * 
	 * @return
	 */
	public String getAllISPvSql() {
		String sql = "";
		String dt = dtString;
		String pr = prString;
		String tb = requestUrlmap.get("tableName");
		String hb = requestUrlmap.get("hiveName");
		try {
			if (tb != null && pr != null) {
				String where = "";
				if (hb != null)
					where = " and " + getBhb() + "='" + hb + "'";
				if (dt != null) {
					String[] dtt = requestUrlmap.get("dt").split("-");
					where += " and " + dt + " >= '" + dtt[0].trim() + "' AND " + dt + " <= '" + dtt[1].trim() + "' ";
					where += " and " + pr + " <> '' ";
					where += " and " + pr + " <> 'null' ";
					where += " and " + pr + " is not null ";
				}
				for (String key : requestUrlmap.keySet()) {
					if (!getBl().contains(key)) {// 除了指定的字段之外 其它的字段是条件当这个条件全部的时候传入的参数是不是all
						if (!requestUrlmap.get(key).toUpperCase().equals("ALL")) {
							where += " and " + key + "='" + requestUrlmap.get(key) + "'";
						}
					}
				}
				sql = "select " + pr + " from " + tb + " where 1 = 1 " + where + " group by " + pr;
			} else {
				sql = "select 'cc'";
			}
			return sql;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sql;
	}

	public ArrayList<String> getNullsql() {
		String pr = prString;
		String dt = dtString;
		String ct = ctString;
		String pvS = requestUrlmap.get("prov_id");
		String ctS = requestUrlmap.get("city_id");
		String prCodeName = "";
		String ctCodeName = "";
		if (!getKpi_id().equals("")) {
			prString = "prov_id";
			pr = prString;
		}
		try {
			prCodeName = getPrCodeName();
		} catch (Exception e) {
		}
		try {
			ctCodeName = getCtCodeName();
		} catch (Exception e) {
		}
		String isAll = requestUrlmap.get("isAll");

		String isSumm = requestUrlmap.get("isSumm");
		String sql1 = getNullALL();
		String sql2 = getNullAllPvCt();
		String sql3 = getNullALLCt();
		String sql4 = getNUllLToC();
		String where = "";
		ArrayList<String> returnsqls = new ArrayList<String>();
		if (!filedName.contains(ct) || null == ctS) {// 不分地市的情况下
			if (requestUrlmap.containsKey("prov_id")) {// 全部的情况下
				where = " and a." + pr + "='" + prCodeName + "'";
			}
			if (getAllpv()) {
				sql2 = "";
				where = "";
			} else if (null != pvS && pvS.equals("allpv")) {
				sql1 = "";
			}
			String sqlString = "";
			if (!sql1.equals("")) {
				sqlString = sqlString.equals("") ? sql1 : sqlString + " union all  " + sql1;
			}
			if (!sql2.equals("")) {
				sqlString = sqlString.equals("") ? sql2 : sqlString + " union all  " + sql2;
			}
//			if(!sql3.equals("")) {
//				sqlString = sqlString.equals("") ? sql3 : sqlString + " union all  "+ sql3;
//			}
			returnsqls.add("select " + sql4 + " from (" + sqlString + ") a where 1=1 " + where + " GROUP BY a." + dt
					+ ", a." + pr);
			returnsqls.add("(" + sqlString + ")");
			return returnsqls;
		} else {// 分地市的情况
			if (getAllpv() || (isSumm != null && !Boolean.parseBoolean(isSumm))) {// 全国或者手动关闭汇总数据
				sql2 = "";
				sql3 = "";
			}

			if (isAll != null && !Boolean.parseBoolean(isAll)) {// 全部的数据进行手动关闭
				sql1 = "";
			}

			if (pvS == null && ctS.equals("allct")) {// 全部 + 全省 (n + 31 + 1) * dt
				// 由于 sql 已经把 全部 + 省份 + 地市的 已经写好了 不用进行操作
			} else if (pvS == null && !ctS.equals("allct")) { // 全部 + 地市 1(单地市) * dt
				sql2 = "";
				sql3 = "";
				where += " and " + ct + "='" + ctCodeName + "'";
			} else if (pvS.equals("allpv") && ctS.equals("allct")) { // 全国1 (汇总) * dt
				sql1 = "";
				sql3 = "";
			} else if (pvS.equals("allpv") && !ctS.equals("allct")) { // 全国 + 地市 1(单地市) * dt
				sql2 = "";
				sql3 = "";
				where += " and " + ct + "='" + ctCodeName + "'";
			} else if (!pvS.equals("allpv") && ctS.equals("allct")) { // 省份 + 全省 ( 1(省份汇总) + n(地市) ) * dt
				sql2 = "";
				where += " and " + pr + "='" + prCodeName + "'";
			} else if (!pvS.equals("allpv") && !ctS.equals("allct")) { // 省份 + 地市 1(单地市) * dt
				sql2 = "";
				sql3 = "";
				where += " and " + pr + "='" + prCodeName + "' and " + ct + "='" + ctCodeName + "'";
			}
			String sqlString = "";
			if (!sql1.equals("")) {
				sqlString = sqlString.equals("") ? sql1 : sqlString + " union all  " + sql1;
			}
			if (!sql2.equals("")) {
				sqlString = sqlString.equals("") ? sql2 : sqlString + " union all  " + sql2;
			}
			if (!sql3.equals("")) {
				sqlString = sqlString.equals("") ? sql3 : sqlString + " union all  " + sql3;
			}

			returnsqls.add(" select " + sql4 + " from (" + sqlString + ") a where 1=1 " + where + " GROUP BY a." + dt
					+ ", a." + pr + ", a." + ct);
			returnsqls.add("(" + sqlString + ")");
			return returnsqls;
		}
	}

	/**
	 * 通过这个方法返回 前台请求的信息用做当地市为中文调整的情况
	 * 
	 * @return
	 */
	public String getCtCodeName() {
		String codeString = "";
		if (isCtEN && ctString != null) {// 如果库中为英文则返回英文
			codeString = requestUrlmap.get("city_id");
		} else if (ctString != null) {// 否则返回中文
			try {
				codeString = new String(ctMap.get(requestUrlmap.get("city_id")).getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
		}
		return codeString;
	}

	/**
	 * 通过这个方法返回 前台请求的信息用库中以中文做为值做当省份为中文调整情况
	 * 
	 * @return
	 */
	public String getPrCodeName() {
		String codeString = "";
		if (isPrEN && prString != null) {// 如果库中为英文则返回英文
			codeString = requestUrlmap.get("prov_id");
		} else if (prString != null) {// 否则返回中文
			try {
				codeString = new String(pvMap.get(requestUrlmap.get("prov_id")).getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}

		}
		return codeString;
	}

	/**
	 * 获取空值率的 省份汇总
	 * 
	 * @return
	 */
	public String getNullALLCt() {
		String dt = dtString;
		String pr = prString;
		String ct = ctString;
		String tb = requestUrlmap.get("tableName");
		String hb = requestUrlmap.get("hiveName");
		String allct = isCtEN ? "allct" : "全省";
		String[] dtt = requestUrlmap.get("dt").split("-");
		String cts = pr;
		String kpiString = getKpi_id();
		String eg_nm = getBnu();
		String null_num = "";
		if (eg_nm.equals("field_name")) {
			null_num = "null_sum";
		} else {
			null_num = "null_num";
		}
		String whereString = "";
		if(hb != null) {
			whereString += " and " + getBhb() + "='" + hb + "'";
		}
		whereString += " and a." + dt + " >= '" + dtt[0].trim() + "' AND a."
		+ dt + " <= '" + dtt[1].trim() + "'";
//		String whereString = " and " + getBhb() + "='" + hb + "' and a." + dt + " >= '" + dtt[0].trim() + "' AND a."
//				+ dt + " <= '" + dtt[1].trim() + "'";
		for (String key : requestUrlmap.keySet()) {
			if (!getBl().contains(key)) {// 除了指定的字段之外 其它的字段是条件 当这个条件全部的时候传入的参数是 all
				if (!requestUrlmap.get(key).toLowerCase().equals("all")) { // 当进行筛选的时候进行加入where
					whereString += " AND " + key + "='" + requestUrlmap.get(key) + "'";
				}
			}
		}
		if (filedName.contains("city_id") || filedName.contains("city_nm")) {
			cts += ",'" + allct + "' " + ct;
		}
		if (!kpiString.equals("")) {
			whereString += " and a.kpi_id='" + kpiString + "'";
			tb = " (select a." + dt + ",substr(" + ct + ",1,3)" + pr + ",a." + ct + ",a." + getBhb() + ",a." + eg_nm
					+ ",a.total,a.null_nm null_num,a.null_rate,a.kpi_id from " + tb + " a where  1 = 1 " + whereString
					+ ")";
		}
		String sql = "select a." + dt + ",a." + cts + ",a." + getBhb() + ",a." + eg_nm + ",sum(total) total," + "sum("
				+ null_num + ") null_num,round(sum(" + null_num + ")/sum(total)*100,2) null_rate from " + tb
				+ " a  where 1 = 1" + whereString + " group by a." + dt + ",a." + pr + ",a." + eg_nm;

		return sql;
	}

	/**
	 * 全部数据
	 * 
	 * @return
	 */
	public String getNullALL() {
		String dt = dtString;
		String pr = prString;
		String ct = ctString;
		String tb = requestUrlmap.get("tableName");
		String hb = requestUrlmap.get("hiveName");
		String[] dtt = requestUrlmap.get("dt").split("-");
		String cts = pr;
		String kpiString = getKpi_id();
		String eg_nm = getBnu();
		String whereString = ""; 
		if(hb != null) {
			whereString += " and " + getBhb() + "='" + hb + "'";
		}
		whereString += " and a." + dt + " >= '" + dtt[0].trim() + "' AND a."
		+ dt + " <= '" + dtt[1].trim() + "'";
		for (String key : requestUrlmap.keySet()) {
			if (!getBl().contains(key)) {// 除了指定的字段之外 其它的字段是条件 当这个条件全部的时候传入的参数是 all
				if (!requestUrlmap.get(key).toLowerCase().equals("all")) { // 当进行筛选的时候进行加入where
					whereString += " AND " + key + "='" + requestUrlmap.get(key) + "'";
				}
			}
		}
		if (filedName.contains("city_id") || filedName.contains("city_nm")) {
			cts += ",a." + ct;
		}
		if (!getWhereString().equals(""))
			kpiString += " and " + getWhereString();
		if (!kpiString.equals("")) {
			whereString += " and a.kpi_id='" + kpiString + "'";
			tb = " (select a." + dt + ",substr(" + ct + ",1,3)" + pr + ",a." + ct + ",a." + getBhb() + ",a." + eg_nm
					+ ",a.total,a.null_nm null_num,a.null_rate,a.kpi_id from " + tb + " a where  1 = 1 " + whereString
					+ ")";
		}
		String null_num = "";
		if (eg_nm.equals("field_name")) {
			null_num = "null_sum";
		} else {
			null_num = "null_num";
		}
		String sql = "select a." + dt + ",a." + cts + ",a." + getBhb() + ",a." + eg_nm + ",a.total," + "a." + null_num
				+ ",a.null_rate from " + tb + " a  where 1 = 1 " + whereString;
		return sql;
	}

	/**
	 * 获取空值率的 全国汇总
	 * 
	 * @return
	 */
	public String getNullAllPvCt() {
		String dt = dtString;
		String pr = prString;
		String ct = ctString;
		String tb = requestUrlmap.get("tableName");
		String hb = requestUrlmap.get("hiveName");
		String[] dtt = requestUrlmap.get("dt").split("-");
		String kpiString = getKpi_id();
		String allct = isCtEN ? "allct" : "全省";
		String allpv = isPrEN ? "allpv" : "全国";
		String cts = "'" + allpv + "' " + pr;
		String eg_nm = getBnu();
//		String whereString = " and " + getBhb() + "='" + hb + "' and a." + dt + " >= '" + dtt[0].trim() + "' AND a."
//				+ dt + " <= '" + dtt[1].trim() + "'";
		String whereString = "";
		if(hb != null) {
			whereString += " and " + getBhb() + "='" + hb + "'";
		}
		whereString += " and a." + dt + " >= '" + dtt[0].trim() + "' AND a."
		+ dt + " <= '" + dtt[1].trim() + "'";
		for (String key : requestUrlmap.keySet()) {
			if (!getBl().contains(key)) {// 除了指定的字段之外 其它的字段是条件 当这个条件全部的时候传入的参数是 all
				if (!requestUrlmap.get(key).toLowerCase().equals("all")) { // 当进行筛选的时候进行加入where
					whereString += " AND " + key + "='" + requestUrlmap.get(key) + "'";
				}
			}
		}
		if (filedName.contains("city_id") || filedName.contains("city_nm")) {
			cts += ",'" + allct + "' " + ct;
		}
		if (!kpiString.equals("")) {
			whereString += " and a.kpi_id='" + kpiString + "'";
			tb = " (select a." + dt + ",substr(" + ct + ",1,3)" + pr + ",a." + ct + ",a." + getBhb() + ",a." + eg_nm
					+ ",a.total,a.null_nm null_num,a.null_rate,a.kpi_id from " + tb + " a where  1 = 1 " + whereString
					+ ")";
		}
		String null_num = "";
		if (eg_nm.equals("field_name")) {
			null_num = "null_sum";
		} else {
			null_num = "null_num";
		}
		String sql = "select a." + dt + "," + cts + ",a." + getBhb() + ",a." + eg_nm + ",sum(total) total," + "sum("
				+ null_num + ") null_num,round(sum(" + null_num + ")/sum(total)*100,2) null_rate from " + tb
				+ " a  where 1 = 1 " + whereString + " group by a." + dt + ",a." + eg_nm;
		return sql;
	}

	/**
	 * 初始化
	 * 
	 * @param directFiledMapping
	 */
	public void setDirectFiledName(List<Map<String, String>> directFiledMapping) {
		try {
			List<String> b = new ArrayList<String>();// ("field_code");//field_name
			Map<String, String> hashMap = new HashMap<String, String>();
			for (int i = 0; i < directFiledMapping.size(); i++) {
				Map<String, String> a = directFiledMapping.get(i);
				hashMap.put(a.get("field_code"), a.get("field_name"));
				b.add(a.get("field_code"));
			}
			setDirectFiledsMap(hashMap);
			if (null != directFiledMapping.get(0).get("typechart")
					&& !directFiledMapping.get(0).get("typechart").toLowerCase().equals("null")) {// 如果有数据的处理的编号进行读取进行写入实例中
				setDbTypechart(directFiledMapping.get(0).get("typechart"));
			}

			this.filedName = b;
//			this.filedNames = b;
			this.directFiledName = directFiledMapping;

			// 初始化
			prString = getpr();
			ctString = getct();
			dtString = getdt();
		} catch (Exception e) {

		}
	}

	/**
	 * 指定的表的字段是否为 为英文时为返回true为中文返回false的sql
	 * 
	 * @return sql
	 */
	public String getIsEN() {
		String sql = "";
		String dt = dtString;
		String pr = prString;
		String ct = ctString;
		String tb = requestUrlmap.get("tableName");
		String hb = requestUrlmap.get("hiveName");
		try {
			if (tb != null) {
				String where = "";
				String fieldsString = "";
				String groupString = "";
				if (hb != null)
					where = " and " + getBhb() + "='" + hb + "'";
				if (dt != null) {
					String[] dtt = requestUrlmap.get("dt").split("-");
					where += " and " + dt + " >= '" + dtt[0].trim() + "' AND " + dt + " <= '" + dtt[1].trim() + "' ";
				}
				if (pr != null) {
					groupString += groupString == "" ? pr : "," + pr;
					fieldsString += fieldsString == ""
							? "case " + pr + " REGEXP '[\\u0391-\\uFFE5]' when 1 THEN 'true' ELSE 'false' end " + pr
							: ",case " + pr + " REGEXP '[\\u0391-\\uFFE5]' when 1 THEN 'true' ELSE 'false' end " + pr;
					where += " and " + pr + " <> '' ";
					where += " and " + pr + " <> 'null' ";
					where += " and " + pr + " is not null ";
				}
				if (ct != null) {
					groupString += groupString == "" ? ct : "," + ct;
					fieldsString += fieldsString == ""
							? "case " + ct + " REGEXP '[\\u0391-\\uFFE5]' when 1 THEN 'true' ELSE 'false' end " + ct
							: ",case " + ct + " REGEXP '[\\u0391-\\uFFE5]' when 1 THEN 'true' ELSE 'false' end " + ct;
					where += " and " + ct + " <> '' ";
					where += " and " + ct + " <> 'null' ";
					where += " and " + ct + " is not null ";
				}
				for (String key : requestUrlmap.keySet()) {
					if (!getBl().contains(key)) {// 除了指定的字段之外 其它的字段是条件当这个条件全部的时候传入的参数是不是all
						if (!requestUrlmap.get(key).toUpperCase().equals("ALL")) {
							where += " and " + key + "='" + requestUrlmap.get(key) + "'";
						}
					}
				}
				if (!getWhereString().equals(""))
					where += " and " + getWhereString();

				if (fieldsString != "")
					sql = "select DISTINCT " + fieldsString + " from " + tb + " where 1 = 1 " + where + " group by "
							+ groupString;
			} else {
				sql = "";
			}
			return sql;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sql;
	}

	/**
	 * 进行封装 count
	 * 
	 * @param sql
	 * @return
	 */
	public String getCountSql(String sql) {
		return "select concat(count(*),'') rows from " + sql + " a";
	}

	public List<Map<String, String>> getNUllTableHead() {
		try {
			String dt = dtString;
			String pr = prString;
			String ct = ctString;
			List<Map<String, String>> r = new ArrayList<Map<String, String>>();
			Map<String, String> m = new HashMap<String, String>();

			if (dt != null && filedName.contains(dt)) {
				m = new HashMap<String, String>();
				m.put("field_name_EN", dtString);
				m.put("field_name_CN", "账期");
				r.add(m);
			}
			if (pr != null && filedName.contains(pr)) {
				m = new HashMap<String, String>();
				m.put("field_name_EN", prString);
				m.put("field_name_CN", "省份");
				r.add(m);
			}
			if (ct != null && filedName.contains(ct)) {
				m = new HashMap<String, String>();
				m.put("field_name_EN", ctString);
				m.put("field_name_CN", "地市");
				r.add(m);
			}
			if (filedName.contains("total")) {
				m = new HashMap<String, String>();
				m.put("field_name_EN", "total");
				m.put("field_name_CN", "总记录数");
				r.add(m);
			}
			for (int i = 0; i < filedNames.size(); i++) {
				String f = filedNames.get(i);
				m = new HashMap<String, String>();
				m.put("field_name_EN", f + "_num");
				m.put("field_name_CN", f + "空值数");
				r.add(m);
				m = new HashMap<String, String>();
				m.put("field_name_EN", f + "_null");
				m.put("field_name_CN", f + "空值率");
				r.add(m);
			}
			return r;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * (case when pro_nm="811" then "北京" ...)
	 * 
	 * @param pn
	 * @return
	 */
	public String getpvtoC(String pn) {
		String sql = " (case " + pn;
		String prov_id = requestUrlmap.get("oth_prov_id");
		String id = prov_id != null && Boolean.parseBoolean(prov_id) ? "oth_prov_id" : "prov_id";
		for (int i = 0; i < pv.size(); i++) {
			Map<String, String> a = pv.get(i);
			String code = a.get(id);
			String name = a.get("prov_name"); // new String(a.get("prov_name").getBytes(),inSet);
			sql += " when '" + code + "' then '" + name + "'";
		}
		return sql + " else " + pn + " end) ";
	}

	public String getcttoC(String cn) {
		String sql = " (case " + cn;
		for (int i = 0; i < ct.size(); i++) {
			Map<String, String> a = ct.get(i);
			String code = a.get("latn_id");
			String name = a.get("latn_name");// new String(a.get("latn_name").getBytes(),inSet);
			sql += " when '" + code + "' then '" + name + "'";
		}
		return sql + " else " + cn + " end) ";
	}

	/**
	 * 获取空值率的字段
	 * 
	 * @return
	 */
	public String getNUllLToC() {
		String unit = getUnit();
		String dt = dtString;
		String pr = prString;
		String ct = ctString;
		String cts = "";
		String prs = "";
		String eg_nm = getBnu();
		String null_num = "";
		if (eg_nm.equals("field_name")) {
			null_num = "null_sum";
		} else {
			null_num = "null_num";
		}
		if (filedName.contains(ctString)) {
			cts = isCtEN ? "," + getcttoC(ct) + " " + ct : "," + ct;
		}
		prs = isPrEN ? "," + getpvtoC(pr) + " " + pr : "," + pr;
		String sql = dt + prs + cts + "," + getBhb() + ",total";
		for (int i = 0; i < filedNames.size(); i++) {
			String f = filedNames.get(i);
			sql += ",MAX(CASE " + eg_nm + " WHEN '" + f + "' THEN " + null_num + " ELSE 0 END) " + f + "_num";
			sql += ",MAX(CASE " + eg_nm + " WHEN '" + f + "' THEN concat(null_rate,'" + unit + "') ELSE '0" + unit
					+ "' END) " + f + "_null";
		}
		return sql;
	}

	/**
	 * 进行存储字段并且进行过滤
	 * 
	 * @param filedNames
	 */
	public void setFiledNames(List<String> filedNames) {
		List<String> a = new ArrayList<String>();
		a.add("id");

		List<String> b = new ArrayList<String>();
		for (int i = 0; i < filedNames.size(); i++) {
			String key = filedNames.get(i);
			if (!a.contains(key)) {
				b.add(key);
			}
		}
		this.filedNames = b;

		// 初始化
		dtString = getdt();
		prString = getpr();
		ctString = getct();
	}

	public void setFiledName(List<String> filedName) {
		this.filedName = filedName;
	}

	/**
	 * 获取日期字段名称 当传入的参数不在 bdt 里面的时候,传出 day 这个是数据的问题,如果出现别的情况进行修改
	 * 
	 * @return
	 */
	public String getdt() {
		String a = null;
		for (int i = 0; i < bdt.size(); i++) {
			String k1 = bdt.get(i);
			if (filedName.contains(k1)) {
				a = k1;
				break;
			}
		}
		if (a == null)
			a = "day";
		return a;
	}

	/**
	 * 获取日期字段名称 当传入的参数不在 bdt 里面的时候,传出 day 这个是数据的问题,如果出现别的情况进行修改
	 * 
	 * @return
	 */
	public String getBnu() {
		String a = null;
		for (int i = 0; i < bnu.size(); i++) {
			String k1 = bnu.get(i);
			if (filedName.contains(k1)) {
				a = k1;
				break;
			}
		}
		if (a == null)
			a = "eg_nm";
		return a;
	}

	/**
	 * 获取省份字段名称
	 * 
	 * @return
	 */
	public String getpr() {
		String a = null;
		for (int i = 0; i < bpr.size(); i++) {
			String k1 = bpr.get(i);
			if (filedName.contains(k1)) {
				a = k1;
				break;
			}
		}
		return a;
	}

	/**
	 * 获取地市字段名称
	 * 
	 * @return
	 */
	public String getct() {
		String a = null;
		for (int i = 0; i < bct.size(); i++) {
			String k1 = bct.get(i);
			if (filedName.contains(k1)) {
				a = k1;
				break;
			}
		}
		return a;
	}

	/**
	 * 获取hiveName字段名称
	 * 
	 * @return
	 */
	public String getBhb() {
		String a = null;
		for (int i = 0; i < bhb.size(); i++) {
			String k1 = bhb.get(i);
			if (filedName.contains(k1)) {
				a = k1;
				break;
			}
		}
		return a;
	}

	private List<String> kn = new ArrayList<String>();// filedName
	private List<String> bl = new ArrayList<String>();// filedName
	private List<String> bdt = new ArrayList<String>();// dt
	private List<String> bpr = new ArrayList<String>();// prov_id
	private List<String> bct = new ArrayList<String>();// city_id
	private List<String> bhb = new ArrayList<String>();// hivetable
	private List<String> bnu = new ArrayList<String>();// hivetable
	private String kpi_id = "";

	/**
	 * 进行类的单例化
	 * 
	 * @author cgh
	 */
//	private static class SingletonClassInstance {
//		private static final FiledProcFilter instance = new FiledProcFilter();
//	}

	/**
	 * 初始化字段的可能性
	 */
	public FiledProcFilter() {
		System.out.println("create FiledProcFilter");
		bl = Arrays.asList("tableName", "hiveName", "prov_id", "prov_nm", "pro_nm", "pro_id", "day", "city_id",
				"city_nm", "dt", "day", "day_id", "month", "month_id", "unit", "kpi_id", "sd_date", "isAll", "tb_nm",
				"rToc", "city_name", "city", "prov_name", "prov_num", "pro_code", "tb_ch", "tb_ch_nm", "tb_cn",
				"tb_data", "tb_en_nm", "tb_name", "tb_nm_CN", "tb_nm_EN", "copu", "hive_table_name", "isSumm",
				"o_prov_id", "o_city_id", "rePCcode", "oth_prov_id", "isWhere", "isColumn", "notColumn", "field_name",
				"notrToc", "notFtMap");
		kn = Arrays.asList("tableName", "hiveName");
		bdt = Arrays.asList("day", "dt", "dts", "sd_date", "day_id", "month_id", "month");
		bpr = Arrays.asList("prov_id", "pro_id", "pro_nm", "prov_nm", "prov_name", "prov_num", "pro_code", "o_prov_id",
				"d_prov_id");
		bnu = Arrays.asList("eg_nm", "field_name");
		bct = Arrays.asList("city_id", "city_nm", "city_name", "city", "o_city_id", "d_city_id", "origin_city", "destination_city");
		bhb = Arrays.asList("tb_nm", "field_name", "tb_en", "tb_ch", "tb_ch_nm", "tb_cn", "tb_data", "tb_en_nm",
				"tb_name", "tb_nm_CN", "tb_nm_EN", "hive_table_name");
	}

	/**
	 * 获取实例的接口
	 * 
	 * @return
	 */
//	public static FiledProcFilter getInstance() {
//		return SingletonClassInstance.instance;
//	}

	public List<String> getFiledNames() {
		return filedNames;
	}

	public List<String> getBl() {
		return bl;
	}

	public List<String> getBdt() {
		return bdt;
	}

	public List<String> getBpr() {
		return bpr;
	}

	public List<String> getBct() {
		return bct;
	}

	public String getKpi_id() {
		return kpi_id;
	}

	public void setKpi_id(String kpi_id) {
		this.kpi_id = kpi_id;
	}

	public Map<String, String> getRequestUrlmap() {
		return requestUrlmap;
	}

	/**
	 * 存储 url 中的内容
	 * 
	 * @param requestUrlmap
	 */
	public void setRequestUrlmap(Map<String, String> requestUrlmap) {
		this.requestUrlmap = requestUrlmap;
	}

	public Map<String, String> getProvCode() {
		return provCode;
	}

	public void setProvCode(Map<String, String> provCode) {
		this.provCode = provCode;
	}

	public List<Map<String, String>> getPv() {
		return pv;
	}

	public void setPv(List<Map<String, String>> pv) {
		pvMap = new HashMap<String, String>();
		String oth_prov_id = requestUrlmap.get("oth_prov_id");
		if (null != oth_prov_id && Boolean.parseBoolean(oth_prov_id)) {
			for (Map<String, String> map : pv) {
				pvMap.put(map.get("oth_prov_id"), map.get("prov_name"));
			}
		} else {
			for (Map<String, String> map : pv) {
				pvMap.put(map.get("prov_id"), map.get("prov_name"));
			}
		}
		this.pv = pv;
	}

	public List<Map<String, String>> getCt() {
		return ct;
	}

	public void setCt(List<Map<String, String>> ct) {
		ctMap = new HashMap<String, String>();
		for (Map<String, String> map : ct) {
			ctMap.put(map.get("latn_id"), map.get("latn_name"));
		}
		this.ct = ct;
	}

	public List<String> getFiledName() {
		return filedName;
	}

	public Boolean getAllpv() {
		return allpv;
	}

	public void setAllpv(Boolean allpv) {
		this.allpv = allpv;
	}

	/**
	 * 这个存储了数据库的field表 信息
	 * 
	 * @return
	 */
	public List<Map<String, String>> getDirectFiledName() {
		return directFiledName;
	}

	public void setBhb(List<String> bhb) {
		this.bhb = bhb;
	}

	public List<String> getKn() {
		return kn;
	}

	public void setKn(List<String> kn) {
		this.kn = kn;
	}

	public void setBl(List<String> bl) {
		this.bl = bl;
	}

	public void setBdt(List<String> bdt) {
		this.bdt = bdt;
	}

	public void setBpr(List<String> bpr) {
		this.bpr = bpr;
	}

	public void setBct(List<String> bct) {
		this.bct = bct;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getDbTypechart() {
		return dbTypechart;
	}

	public void setDbTypechart(String dbTypechart) {
		this.dbTypechart = dbTypechart;
	}

	public String getWhereString() {
		return whereString;
	}

	public void setWhereString(String whereString) {
		if (whereString.toLowerCase().indexOf("drop") == -1 && whereString.toLowerCase().indexOf("insert") == -1
				&& whereString.toLowerCase().indexOf("delete") == -1
				&& whereString.toLowerCase().indexOf("update") == -1)
			this.whereString = whereString;
	}

	public String getGroupString() {
		return groupString;
	}

	public void setGroupString(String groupString) {
		if (groupString.toLowerCase().indexOf("drop") == -1 && groupString.toLowerCase().indexOf("insert") == -1
				&& groupString.toLowerCase().indexOf("delete") == -1
				&& groupString.toLowerCase().indexOf("update") == -1)
			this.groupString = groupString;
	}

	public List<Map<String, String>> getHeadList() {
		return headList;
	}

	public void setHeadList(List<Map<String, String>> headList) {
		this.headList = headList;
	}

	/**
	 * 这个存储了数据库的field表 code: name 信息
	 * 
	 * @return
	 */
	public Map<String, String> getDirectFiledsMap() {
		return directFiledsMap;
	}

	public void setDirectFiledsMap(Map<String, String> directFiledsMap) {
		this.directFiledsMap = directFiledsMap;
	}

	public String getDtString() {
		return dtString;
	}

	public void setDtString(String dtString) {
		this.dtString = dtString;
	}

	public String getPrString() {
		return prString;
	}

	public void setPrString(String prString) {
		this.prString = prString;
	}

	public String getCtString() {
		return ctString;
	}

	public void setCtString(String ctString) {
		this.ctString = ctString;
	}

	public Boolean getIsPrEN() {
		return isPrEN;
	}

	public void setIsPrEN(Boolean isPrEN) {
		this.isPrEN = isPrEN;
	}

	public Boolean getIsCtEN() {
		return isCtEN;
	}

	public void setIsCtEN(Boolean isCtEN) {
		this.isCtEN = isCtEN;
	}

	public Map<String, String> getPvMap() {
		return pvMap;
	}

	public void setPvMap(Map<String, String> pvMap) {
		this.pvMap = pvMap;
	}

	public Map<String, String> getCtMap() {
		return ctMap;
	}

	public void setCtMap(Map<String, String> ctMap) {
		this.ctMap = ctMap;
	}

	public List<Map<String, String>> upIsColumn(List<Map<String, String>> Head, List<String> filed) {
		try {
			HashMap<String, String> maps = new HashMap<String, String>();
			List<Map<String, String>> headsList = new ArrayList<Map<String, String>>();
			for (int i = 0; i < filed.size(); i++) {
				String filedCode = filed.get(i);
				Boolean isokBoolean = false;
				for (int j = 0; j < Head.size(); j++) {
					Map<String, String> map = Head.get(j);
					if (map.get("field_name_EN").equals(filedCode)) {
						headsList.add(map);
						Head.remove(j);
						isokBoolean = true;
						j--;
					}
				}
				if (!isokBoolean) {
					for (int i1 = 0; i1 < directFiledName.size(); i1++) {
						Map<String, String> map1 = directFiledName.get(i1);
						String field_code = map1.get("field_code");
						if (filedCode.equals(field_code)) {
							maps.put("field_name_EN", field_code);
							maps.put("field_name_CN", map1.get("field_name"));
							headsList.add(maps);
							maps = new HashMap<String, String>();
						}
					}
				}
			}
			return headsList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Map<String, String>> upNotColumn(List<Map<String, String>> Head, List<String> filed) {
		try {
			List<Map<String, String>> headsList = new ArrayList<Map<String, String>>();
			for (int i = 0; i < Head.size(); i++) {
				Map<String, String> map = Head.get(i);
				if (!filed.contains(map.get("field_name_EN")))
					headsList.add(map);
			}
			return headsList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<String> getNotrToc() {
		return notrToc;
	}

	public void setNotrToc(String notrToc) {
		String[] split = notrToc.split(",");
		this.notrToc = new ArrayList<String>();
		for (int i = 0; i < split.length; i++) {
			String string = split[i];
			this.notrToc.add(string);
		}
	}

	public ArrayList<String> getNotFtMap() {
		return notFtMap;
	}

	public void setNotFtMap(String notFtMap) {
		String[] split = notFtMap.split(",");
		this.notFtMap = new ArrayList<String>();
		for (int i = 0; i < split.length; i++) {
			String string = split[i];
			this.notFtMap.add(string);
		}
	}
	/**
	 * 加上一个total
	 * @param pn
	 * @return
	 */
	public String getPageSql(String sql, String pageNo, String pageSize) {
		return sql + " limit " + pageNo + "," + pageSize;
	}

}