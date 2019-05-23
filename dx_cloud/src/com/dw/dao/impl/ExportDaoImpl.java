package com.dw.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.dw.common.FieldPublicFilter;
import com.dw.common.base.TempJdbcDao;
import com.dw.dao.IExportDao;
import com.dw.model.ExportMessage;

@Service("exportDao")
public class ExportDaoImpl extends TempJdbcDao implements IExportDao {

	@Override
	public List<Map<String, Object>> test(ExportMessage message) {
		List<Map<String, Object>> columnNames = null;
		String sql = " select dt 账期,pro_nm ,total,null_num,null_rate,only_user,typ from fix_null_onlyuser where dt = '20190307' ";
		columnNames = this.getColumnNames(sql);
		return columnNames;
	}

	@Override
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getFieldsMap(Map<String, Object> map) {
		HashMap<String, Object> reqFieldMap = new HashMap<String, Object>();
		String sql = "SELECT ";
		List<String> field = (List<String>) map.get("field");
		String Fields = field.toString().substring(1, field.toString().length()-1);
		sql += Fields + " FROM ";
		String tableName = (String) map.get("tableName");
		sql += tableName + " WHERE 1 = 1 ";
		Map<String, String> whereMap = (Map<String, String>) map.get("where");
		String groupString = "";
		if (whereMap != null) {
			for (String code : whereMap.keySet()) {
				sql += " and " + code + " = '" + whereMap.get(code) + "'";
				groupString += groupString == "" ? groupString : "," + groupString;
			}
		}
		sql += groupString;
		List<Map<String, String>> querylist = this.querylist(sql);
		for (int i = 0; i < field.size(); i++) {
			String code = field.get(i);
			HashSet<String> requeSet = new HashSet<String>();
			for (int j = 0; j < querylist.size(); j++) {
				Map<String, String> map2 = querylist.get(j);
				requeSet.add(map2.get(code));
			}
			reqFieldMap.put(code, new ArrayList<String>(requeSet));
		}
		return reqFieldMap;
	}

	@Override
	public List<Map<String, Object>> getData(ExportMessage message, String sql) throws Exception {
		List<Map<String, Object>> querylist = null;
		Map<String, Object> req = message.getReq();
		if (!req.containsKey("fileName") && req.containsKey("start") && req.containsKey("limit")) {
			int start = Integer.parseInt(req.get("start").toString());
			int limit = Integer.parseInt(req.get("limit").toString());
			sql = this.buildPageSqlMySql(sql, start, limit);
		}
		querylist = this.querylist(sql);
		return querylist;
	}
	
	@Override
	public String getCount(ExportMessage message, String sql) throws Exception {
		String total = "";
		total = this.buildCountSqlMysql(sql);
		return total;
	}
	
	@Override
	public String getSqls(ExportMessage message) throws Exception{
		String sql = " select dt 账期,pro_nm 省份,total 记录数 ,null_num 空值数,null_rate 空值率,only_user 唯一用户数,typ 类型 from fix_null_onlyuser where dt = '20190307' ";
		return sql;
	}

	@Override
	public void getFieldHeadHibenate(ExportMessage message,String sql) throws Exception {
		this.getColumnNames(sql);
	}

	@Override
	public String getSqlGuest(ExportMessage message) throws Exception {
		String sql = "";
		return null;
	}

	@Override
	public String getSqlGuestcall(ExportMessage message) throws Exception {
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = " select " + 
				" a.dt 账期," + 
				instance.getpvtoC("pro_nm") + "  省份," + 
				" a.tb_nm 表名," + 
				" a.total 记录数," + 
				" round((a.total-b.total_cnt)/b.total_cnt*100,2) 记录数占比 " + 
				" from dal_bdcsc_null a ," + 
				" (select sum(total)/cnt total_cnt from (select a.dt ,a.pro_nm prov_id,a.total " + 
				" from dal_bdcsc_null a " + 
				" where a.tb_nm='dal_bdcsc_fix_opp_nbr_dur_info_special_msk_d'" + 
				" and a.dt >= '${star_dts}' " + 
				" and a.dt <= '${end_dts}'" + 
				" group by a.dt,a.pro_nm)a,(select count(1) cnt from (select dt from dal_bdcsc_null where tb_nm='dal_bdcsc_fix_opp_nbr_dur_info_special_msk_d'" + 
				" and dt >= '${star_dts}' " + 
				" and dt <= '${end_dts}' group by dt ) aa )b)b" + 
				" where a.tb_nm='dal_bdcsc_fix_opp_nbr_dur_info_special_msk_d'" + 
				" and a.dt >= '${star_dts}' " + 
				" and a.dt <= '${end_dts}' group by a.dt ";
		return sql;
	}

	@Override
	public String getSqlGuestcallUserProportion(ExportMessage message) throws Exception {
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "select a.dt 账期," + 
				instance.getpvtoC("pro_nm") + " 省份," + 
				" a.tb_nm 表名," + 
				" b.mdn_cnt 用户数," + 
				" b.other_party_cnt  oth_party用户数," + 
				" round(b.mdn_cnt/a.total,2) mdn_rate用户数占比," + 
				" round(b.other_party_cnt/a.total,2) oth_party用户数占比" + 
				" from dal_bdcsc_null a LEFT JOIN dal_acqcust_callrecord_col_cnt b" + 
				" ON a.tb_nm=b.tb_en and a.dt=b.dt and a.pro_nm=b.prov_id" + 
				" where a.tb_nm='dal_bdcsc_fix_opp_nbr_dur_info_special_msk_d'" + 
				" and a.dt >= '${star_dts}'" + 
				" and a.dt <= '${end_dts}' group by a.dt ";
		
		return sql;
	}

	@Override
	public String getSqlGuestcallAbnormalTotal(ExportMessage message) throws Exception {
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "select " + 
				" a.dt 账期," + 
				instance.getpvtoC("pro_nm") + " 省份," + 
				" a.tb_nm 表名," + 
				" b.mdn_oparty  mdn_oparty记录数," + 
				" b.oparty_mdn  oparty_mdn记录数," + 
				" round(b.mdn_oparty/a.total,4) mdn_oparty记录数占比," + 
				" round(b.oparty_mdn/a.total,4) oparty_mdn记录数占比" + 
				" from dal_bdcsc_null a LEFT JOIN dal_acqcust_callrecord_col_cnt b" + 
				" ON a.tb_nm=b.tb_en and a.dt=b.dt and a.pro_nm=b.prov_id" + 
				" where a.tb_nm='dal_bdcsc_fix_opp_nbr_dur_info_special_msk_d'" + 
				" and a.dt >= '${star_dts}'" + 
				" and a.dt <= '${end_dts}' group by a.dt ";
		
		return sql;
	}
	
	@Override
	public String getSqlIDMappingIDsystem(ExportMessage message) throws Exception {
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql=" select a.dt 账期," +
				instance.getpvtoC("prov_id") + 
				" 省份,a.cnt2 ID累积量,a.all_total ID总量,a.all_raie ID累积量占比 from (" + 
				" SELECT a.dt,'allpv' prov_id,sum(a.cnt2)cnt2,sum(a.all_total) all_total,concat(round(sum(a.cnt2)/sum(a.all_total)*100,2),'%') all_raie" + 
				" FROM " + 
				" (SELECT a.dt,a.prov_id,sum(a.cnt1) cnt1,sum(a.cnt2) cnt2,b.all_total" + 
				" FROM " + 
				" bs_covered_rate a," + 
				" (SELECT SUM(oth_zy_num)+SUM(wb_zy_num) all_total FROM crm_users_count WHERE typ='1' AND tb_nm='dwi_sev_user_main_info_m' AND dt=(SELECT max(dt) FROM crm_users_count WHERE typ='1' AND tb_nm='dwi_sev_user_main_info_m' GROUP BY tb_nm)" + 
				" ) b WHERE a.dt>='${star_dts}' AND a.dt<='${end_dts}' group by dt,a.prov_id" + 
				" ) a," + 
				" (SELECT dt,prov_id,cnt1 FROM bs_user_count a WHERE a.dt>='${star_dts}' AND a.dt<='${end_dts}') b WHERE a.dt=b.dt AND a.prov_id=b.prov_id group by dt" + 
				" union all" + 
				" SELECT a.dt, a.prov_id,sum(a.cnt2) cnt2,sum(a.all_total) all_total,concat(round(sum(a.cnt2)/sum(a.all_total)*100,2),'%') all_raie" + 
				" FROM " + 
				" (SELECT a.dt,a.prov_id,sum(a.cnt1) cnt1,sum(a.cnt2) cnt2,b.all_total" + 
				" FROM " + 
				" bs_covered_rate a," + 
				" (SELECT SUM(oth_zy_num)+SUM(wb_zy_num) all_total FROM crm_users_count WHERE typ='1' AND tb_nm='dwi_sev_user_main_info_m' AND dt=(SELECT max(dt) FROM crm_users_count WHERE typ='1' AND tb_nm='dwi_sev_user_main_info_m' GROUP BY tb_nm)" + 
				" ) b WHERE a.dt>='${star_dts}' AND a.dt<='${end_dts}' group by dt,prov_id" + 
				" ) a," + 
				" (SELECT dt,prov_id,cnt1 FROM bs_user_count a WHERE a.dt>='${star_dts}' AND a.dt<='${end_dts}') b WHERE a.dt=b.dt AND a.prov_id=b.prov_id group by dt,prov_id" + 
				" )a" + 
				" where 1 = 1 ";
		return sql;
	}

	@Override
	public String getSqlLocationFusionMergelocationCitysource(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql= "SELECT dt AS '账期'," + instance.getpvtoC("prov_id") + " AS '省份',count_nm0 AS '不一致记录数',count_nm1 AS '一致记录数',dis_users_nm0 AS '不一致用户数',dis_users_nm1 AS '一致用户数',count_nm_rate0 AS '不一致记录数占比',count_nm_rate1 AS '一致记录数占比',dis_users_nm_rate0 AS '不一致用户数占比',dis_users_nm_rate1 AS '一致用户数占比' FROM ( " +
                "SELECT dts AS dt,city_id AS prov_id,MAX(CASE end_tmp_field WHEN '0' THEN count_nm ELSE 0 END) AS count_nm0,MAX(CASE end_tmp_field WHEN '1' THEN count_nm ELSE 0 END) AS count_nm1,MAX(CASE end_tmp_field WHEN '0' THEN dis_users_nm ELSE 0 END) AS dis_users_nm0,MAX(CASE end_tmp_field WHEN '1' THEN dis_users_nm ELSE 0 END) AS dis_users_nm1,MAX(CASE end_tmp_field WHEN '0' THEN count_nm_rate ELSE 0 END) AS count_nm_rate0,MAX(CASE end_tmp_field WHEN '1' THEN count_nm_rate ELSE 0 END) AS count_nm_rate1,MAX(CASE end_tmp_field WHEN '0' THEN dis_users_nm_rate ELSE 0 END) AS dis_users_nm_rate0,MAX(CASE end_tmp_field WHEN '1' THEN dis_users_nm_rate ELSE 0 END) AS dis_users_nm_rate1 FROM ( " +
                "SELECT*FROM ( " +
                "SELECT a.dts,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,a.end_tmp_field,count_nm,users_nm,dis_users_nm,round(count_nm/count_nm_sum*100,2) AS count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,2) AS dis_users_nm_rate FROM ( " +
                "SELECT sd_date AS dts,'allpv' AS city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) AS end_tmp_field,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_cityid_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_5' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1)) a INNER JOIN ( " +
                "SELECT sd_date AS dts,'allpv' AS city_id,hive_table_name,data_source,kpi_id,SUM(count_nm) AS count_nm_sum,SUM(users_nm) AS users_nm_sum,SUM(dis_users_nm) AS dis_users_nm_sum FROM auto_cityid_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_5' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,data_source,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id AND a.data_source=b.data_source WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_5' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}') tmp UNION ALL ( " +
                "SELECT a.dts,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,2) AS count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,2) AS dis_users_nm_rate FROM ( " +
                "SELECT sd_date AS dts,substr(substring_index(end_tmp_field,'_',-1),1,3) AS city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) AS end_tmp_field,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_cityid_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_5' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,3),substring_index(end_tmp_field,'_',1),hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date AS dts,hive_table_name,kpi_id,SUM(count_nm) AS count_nm_sum,SUM(users_nm) AS users_nm_sum,SUM(dis_users_nm) AS dis_users_nm_sum FROM auto_cityid_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_5' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_5' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}')) tmp GROUP BY dts,city_id,hive_table_name,kpi_id,data_source) tmp WHERE 1 = 1";

		return sql;
	}

	@Override
	public String getSqlLocationFusionMergelocationSpeeding(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dts '账期'," + instance.getpvtoC("prov_id") + " '省份'," + instance.getcttoC("city_id") + " '地市',count_nm '记录数',dis_users_nm '用户数',concat(round(count_nm_rate,2),'%') '记录数占比',concat(round(dis_users_nm_rate,2),'%') '用户数占比' FROM ( " +
                "SELECT*FROM ( " +
                "SELECT a.dts,'allpv' prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,a.end_tmp_field,count_nm,users_nm,dis_users_nm,round(count_nm/count_nm_sum*100,4) count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,4) dis_users_nm_rate FROM ( " +
                "SELECT sd_date dts,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_speeding_citysource_rate WHERE substring_index(end_tmp_field,'_',1)='1' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1)) a INNER JOIN ( " +
                "SELECT sd_date dts,'allct' city_id,hive_table_name,data_source,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_speeding_citysource_rate GROUP BY sd_date,hive_table_name,data_source,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id AND a.data_source=b.data_source WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_6' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}') AS tmp UNION ALL ( " +
                "SELECT a.dts,a.prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,4) count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,4) dis_users_nm_rate FROM ( " +
                "SELECT sd_date dts,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_speeding_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_6' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,3),substring_index(end_tmp_field,'_',1),hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date dts,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_speeding_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_6' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_6' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}') UNION ALL ( " +
                "SELECT a.dts,a.prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,4) count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,4) dis_users_nm_rate FROM ( " +
                "SELECT sd_date dts,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,substr(substring_index(end_tmp_field,'_',-1),1,5) city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_speeding_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_6' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,5),substring_index(end_tmp_field,'_',1),hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date dts,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_speeding_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_6' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_6' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}')) tmp WHERE end_tmp_field='1' ";
		return sql;
	}

	@Override
	public String getSqlLocationFusionMergelocationPp(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份'," + instance.getcttoC("city_id") + " '地市',count_nm '记录数',dis_users_nm '用户数',concat(round(count_nm_rate,2),'%') '记录数占比' FROM ( " +
                "SELECT*FROM ( " +
                "SELECT a.dts dt,'allpv' prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,count_nm,users_nm,dis_users_nm,round(count_nm/count_nm_sum*100,2) count_nm_rate FROM ( " +
                "SELECT sd_date dts,'allct' city_id,hive_table_name,kpi_id,data_source,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date dts,'allct' city_id,hive_table_name,data_source,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,data_source,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id AND a.data_source=b.data_source WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_7' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}') AS tmp UNION ALL ( " +
                "SELECT a.dts dt,a.prov_id,'allct' city_id,a.hive_table_name,a.kpi_id,a.data_source,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,2) count_nm_rate FROM ( " +
                "SELECT sd_date dts,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,hive_table_name,kpi_id,data_source,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,3),hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date dts,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_7' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}') UNION ALL ( " +
                "SELECT a.dts dt,a.prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,2) count_nm_rate FROM ( " +
                "SELECT sd_date dts,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,substr(substring_index(end_tmp_field,'_',-1),1,5) city_id,hive_table_name,kpi_id,data_source,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,5),hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date dts,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_7' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}')) tmp WHERE 1=1 ";
 
		return sql;
	}

	@Override
	public String getSqlLocationFusionMergelocationCityid(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份'," + instance.getcttoC("city_id") + " '地市',kpi_id '数据类型',hive_table_name '表名',tmp_count01 'OIDD城市个数',count_nm01 'OIDD记录数',dis_users_nm01 'OIDD用户数',tmp_count02 '3gDPI城市个数',count_nm02 '3gDPI记录数',dis_users_nm02 '3gDPI用户数',tmp_count03 'DDR城市个数',count_nm03 'DDR记录数',dis_users_nm03 'DDR用户数',tmp_count04 'WCDR城市个数',count_nm04 'WCDR记录数',dis_users_nm04 'WCDR用户数',tmp_count05 '4gDPI城市个数',count_nm05 '4gDPI记录数',dis_users_nm05 '4gDPI用户数' FROM ( " +
                "SELECT dt dt,prov_id,city_id,kpi_id,hive_table_name,MAX(CASE data_source WHEN '01' THEN tmp_count ELSE 0 END) tmp_count01,MAX(CASE data_source WHEN '01' THEN count_nm ELSE 0 END) count_nm01,MAX(CASE data_source WHEN '01' THEN dis_users_nm ELSE 0 END) dis_users_nm01,MAX(CASE data_source WHEN '02' THEN tmp_count ELSE 0 END) tmp_count02,MAX(CASE data_source WHEN '02' THEN count_nm ELSE 0 END) count_nm02,MAX(CASE data_source WHEN '02' THEN dis_users_nm ELSE 0 END) dis_users_nm02,MAX(CASE data_source WHEN '03' THEN tmp_count ELSE 0 END) tmp_count03,MAX(CASE data_source WHEN '03' THEN count_nm ELSE 0 END) count_nm03,MAX(CASE data_source WHEN '03' THEN dis_users_nm ELSE 0 END) dis_users_nm03,MAX(CASE data_source WHEN '04' THEN tmp_count ELSE 0 END) tmp_count04,MAX(CASE data_source WHEN '04' THEN count_nm ELSE 0 END) count_nm04,MAX(CASE data_source WHEN '04' THEN dis_users_nm ELSE 0 END) dis_users_nm04,MAX(CASE data_source WHEN '05' THEN tmp_count ELSE 0 END) tmp_count05,MAX(CASE data_source WHEN '05' THEN count_nm ELSE 0 END) count_nm05,MAX(CASE data_source WHEN '05' THEN dis_users_nm ELSE 0 END) dis_users_nm05 FROM ( " +
                "SELECT sd_date dt,'allpv' prov_id,'allct' city_id,kpi_id,hive_table_name,data_source,count(DISTINCT (city_id)) tmp_count,sum(count_nm) count_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cityid_rate WHERE kpi_id='2_1_10' AND hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,data_source UNION ALL " +
                "SELECT sd_date dt,substr(city_id,1,3) prov_id,'allct' city_id,kpi_id,hive_table_name,data_source,count(DISTINCT (city_id)) city_id_num,sum(count_nm) count_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cityid_rate WHERE kpi_id='2_1_10' AND hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,data_source,substr(city_id,1,3) UNION ALL " +
                "SELECT sd_date dt,substr(city_id,1,3) prov_id,city_id,kpi_id,hive_table_name,data_source,count(DISTINCT (city_id)) city_id_num,sum(count_nm) count_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cityid_rate WHERE kpi_id='2_1_10' AND hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,data_source,city_id) t GROUP BY t.dt,t.city_id,t.hive_table_name,t.kpi_id) tmp WHERE 1=1 ";
 
		return sql;
	}

	@Override
	public String getSqlLocationFusionMergelocationVlatility(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份',hive_table_name '表名',kpi_id '数据类型'," + instance.getcttoC("city_id") + " '地市',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',count_nm_volatility '记录数波动率',dis_users_nm '用户数',dis_users_nm_volatility '用户数波动率' FROM ( " +
                "SELECT*FROM ( " +
                "SELECT t.sd_date dt,substr(city_id,1,3) prov_id,t.hive_table_name,'1_0_8' kpi_id,city_id,t.data_source,t.end_tmp_field,t.tmp_count,t.count_nm,t.count_nm_volatility,t.users_nm,t.users_nm_volatility,t.dis_users_nm,t.dis_users_nm_volatility FROM auto_user_citysource_rate_volatility t WHERE t.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND t.kpi_id='2_0_8' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND length(city_id)=5 AND city_id<> 'allpv') tmp UNION ALL ( " +
                "SELECT t.sd_date dt,city_id prov_id,t.hive_table_name,t.kpi_id,'allct' city_id,t.data_source,t.end_tmp_field,t.tmp_count,t.count_nm,t.count_nm_volatility,t.users_nm,t.users_nm_volatility,t.dis_users_nm,t.dis_users_nm_volatility FROM auto_user_citysource_rate_volatility t WHERE t.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND t.kpi_id='1_0_8' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}')) tmp WHERE 1=1 ";
 
		return sql;

	}

	@Override
	public String getSqlLocationFusionprovSourceVolatility(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期',hive_table_name '表名',kpi_id '数据类型'," + instance.getpvtoC("prov_id") + " '省份'," + instance.getcttoC("city_id") + " '地市',count_nm01 'OIDD记录数',count_nm02 '3gDPI记录数',count_nm03 'DDR记录数',count_nm04 'WCDR记录数',count_nm05 '4gDPI记录数',dis_users_nm01 'OIDD用户数',dis_users_nm02 '3gDPI用户数',dis_users_nm03 'DDR用户数',dis_users_nm04 'WCDR用户数',dis_users_nm05 '4gDPI用户数',concat(count_nm_volatility01,'%') 'OIDD记录数波动率',concat(count_nm_volatility02,'%') '3gDPI记录数波动率',concat(count_nm_volatility03,'%') 'DDR记录数波动率',concat(count_nm_volatility04,'%') 'WCDR记录数波动率',concat(count_nm_volatility05,'%') '4gDPI记录数波动率',concat(dis_users_nm_volatility01,'%') 'OIDD用户数波动率',concat(dis_users_nm_volatility02,'%') '3gDPI用户数波动率',concat(dis_users_nm_volatility03,'%') 'DDR用户数波动率',concat(dis_users_nm_volatility04,'%') 'WCDR用户数波动率',concat(dis_users_nm_volatility05,'%') '4gDPI用户数波动率' FROM ( " +
                "SELECT dt,hive_table_name,kpi_id,prov_id,city_id,MAX(CASE data_source WHEN '01' THEN count_nm ELSE 0 END) 'count_nm01',MAX(CASE data_source WHEN '02' THEN count_nm ELSE 0 END) 'count_nm02',MAX(CASE data_source WHEN '03' THEN count_nm ELSE 0 END) 'count_nm03',MAX(CASE data_source WHEN '04' THEN count_nm ELSE 0 END) 'count_nm04',MAX(CASE data_source WHEN '05' THEN count_nm ELSE 0 END) 'count_nm05',MAX(CASE data_source WHEN '01' THEN dis_users_nm ELSE 0 END) 'dis_users_nm01',MAX(CASE data_source WHEN '02' THEN dis_users_nm ELSE 0 END) 'dis_users_nm02',MAX(CASE data_source WHEN '03' THEN dis_users_nm ELSE 0 END) 'dis_users_nm03',MAX(CASE data_source WHEN '04' THEN dis_users_nm ELSE 0 END) 'dis_users_nm04',MAX(CASE data_source WHEN '05' THEN dis_users_nm ELSE 0 END) 'dis_users_nm05',MAX(CASE data_source WHEN '01' THEN count_nm_volatility ELSE 0 END) 'count_nm_volatility01',MAX(CASE data_source WHEN '02' THEN count_nm_volatility ELSE 0 END) 'count_nm_volatility02',MAX(CASE data_source WHEN '03' THEN count_nm_volatility ELSE 0 END) 'count_nm_volatility03',MAX(CASE data_source WHEN '04' THEN count_nm_volatility ELSE 0 END) 'count_nm_volatility04',MAX(CASE data_source WHEN '05' THEN count_nm_volatility ELSE 0 END) 'count_nm_volatility05',MAX(CASE data_source WHEN '01' THEN dis_users_nm_volatility ELSE 0 END) 'dis_users_nm_volatility01',MAX(CASE data_source WHEN '02' THEN dis_users_nm_volatility ELSE 0 END) 'dis_users_nm_volatility02',MAX(CASE data_source WHEN '03' THEN dis_users_nm_volatility ELSE 0 END) 'dis_users_nm_volatility03',MAX(CASE data_source WHEN '04' THEN dis_users_nm_volatility ELSE 0 END) 'dis_users_nm_volatility04',MAX(CASE data_source WHEN '05' THEN dis_users_nm_volatility ELSE 0 END) 'dis_users_nm_volatility05' FROM ( " +
                "SELECT*FROM ( " +
                "SELECT t.sd_date dt,t.hive_table_name,'1_1_8' kpi_id,substr(city_id,1,3) prov_id,city_id,t.data_source,t.end_tmp_field,t.tmp_count,t.count_nm,t.count_nm_volatility,t.users_nm,t.users_nm_volatility,t.dis_users_nm,t.dis_users_nm_volatility FROM auto_user_citysource_rate_volatility t WHERE t.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND t.kpi_id='2_1_8' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND length(city_id)=5 AND city_id<> 'allpv') tmp UNION ALL ( " +
                "SELECT t.sd_date dt,t.hive_table_name,t.kpi_id,city_id prov_id,'allct' city_id,t.data_source,t.end_tmp_field,t.tmp_count,t.count_nm,t.count_nm_volatility,t.users_nm,t.users_nm_volatility,t.dis_users_nm,t.dis_users_nm_volatility FROM auto_user_citysource_rate_volatility t WHERE t.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND t.kpi_id='1_1_8' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}')) tmp GROUP BY dt,hive_table_name,kpi_id,prov_id,city_id) tmp WHERE 1=1";

			return sql;
	}

	@Override
	public String LocationFusionMergelocation24timeslot(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份',`time` '时段',tmp_count '地市个数',count_nm '记录数',dis_users_nm '用户数' FROM ( " +
                "SELECT*FROM ( " +
                "SELECT sd_date dt,'allpv' prov_id,hive_table_name,kpi_id,data_source,end_tmp_field time,sum(tmp_count) tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_timeslot_rate WHERE hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND kpi_id='2_0_11' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field) AS tmp UNION ALL " +
                "SELECT sd_date dt,city_id prov_id,hive_table_name,kpi_id,data_source,end_tmp_field time,sum(tmp_count) tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_timeslot_rate WHERE hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND kpi_id='2_0_11' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,end_tmp_field UNION ALL " +
                "SELECT sd_date dt,'allpv' prov_id,hive_table_name,kpi_id,data_source,'all' time,sum(tmp_count) tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_timeslot_rate WHERE hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND kpi_id='2_0_11' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source UNION ALL " +
                "SELECT sd_date dt,city_id prov_id,hive_table_name,kpi_id,data_source,'all' time,sum(tmp_count) tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_timeslot_rate WHERE hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND kpi_id='2_0_11' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source) tmp WHERE 1=1 ";
			return sql;
	}

	@Override
	public String LocationFusionMergelocationSourceNumRate(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份'," + instance.getcttoC("city_id") + " '地市',hive_table_name '表名',kpi_id '数据类型',count_nm01 'OIDD记录数',count_nm02 '3gDPI记录数',count_nm03 'DDR记录数',count_nm04 'WCDR记录数',count_nm05 '4gDPI记录数',count_nm_rate01 'OIDD记录数占比',count_nm_rate02 '3gDPI记录数占比',count_nm_rate03 'DDR记录数占比',count_nm_rate04 'WCDR记录数占比',count_nm_rate05 '4gDPI记录数占比',dis_users_nm01 'OIDD用户数',dis_users_nm02 '3gDPI用户数',dis_users_nm03 'DDR用户数',dis_users_nm04 'WCDR用户数',dis_users_nm05 '4gDPI用户数',dis_users_nm_rate01 'OIDD用户数占比',dis_users_nm_rate02 '3gDPI用户数占比',dis_users_nm_rate03 'DDR用户数占比',dis_users_nm_rate04 'WCDR用户数占比',dis_users_nm_rate05 '4gDPI用户数占比' FROM ( " +
                "SELECT dt,prov_id,city_id,hive_table_name,kpi_id,MAX(CASE data_source WHEN '01' THEN count_nm ELSE 0 END) count_nm01,MAX(CASE data_source WHEN '02' THEN count_nm ELSE 0 END) count_nm02,MAX(CASE data_source WHEN '03' THEN count_nm ELSE 0 END) count_nm03,MAX(CASE data_source WHEN '04' THEN count_nm ELSE 0 END) count_nm04,MAX(CASE data_source WHEN '05' THEN count_nm ELSE 0 END) count_nm05,MAX(CASE data_source WHEN '01' THEN count_nm_rate ELSE 0 END) count_nm_rate01,MAX(CASE data_source WHEN '02' THEN count_nm_rate ELSE 0 END) count_nm_rate02,MAX(CASE data_source WHEN '03' THEN count_nm_rate ELSE 0 END) count_nm_rate03,MAX(CASE data_source WHEN '04' THEN count_nm_rate ELSE 0 END) count_nm_rate04,MAX(CASE data_source WHEN '05' THEN count_nm_rate ELSE 0 END) count_nm_rate05,MAX(CASE data_source WHEN '01' THEN users_nm_sum ELSE 0 END) users_nm_sum01,MAX(CASE data_source WHEN '02' THEN users_nm_sum ELSE 0 END) users_nm_sum02,MAX(CASE data_source WHEN '03' THEN users_nm_sum ELSE 0 END) users_nm_sum03,MAX(CASE data_source WHEN '04' THEN users_nm_sum ELSE 0 END) users_nm_sum04,MAX(CASE data_source WHEN '05' THEN users_nm_sum ELSE 0 END) users_nm_sum05,MAX(CASE data_source WHEN '01' THEN dis_users_nm ELSE 0 END) dis_users_nm01,MAX(CASE data_source WHEN '02' THEN dis_users_nm ELSE 0 END) dis_users_nm02,MAX(CASE data_source WHEN '03' THEN dis_users_nm ELSE 0 END) dis_users_nm03,MAX(CASE data_source WHEN '04' THEN dis_users_nm ELSE 0 END) dis_users_nm04,MAX(CASE data_source WHEN '05' THEN dis_users_nm ELSE 0 END) dis_users_nm05,MAX(CASE data_source WHEN '01' THEN dis_users_nm_rate ELSE 0 END) dis_users_nm_rate01,MAX(CASE data_source WHEN '02' THEN dis_users_nm_rate ELSE 0 END) dis_users_nm_rate02,MAX(CASE data_source WHEN '03' THEN dis_users_nm_rate ELSE 0 END) dis_users_nm_rate03,MAX(CASE data_source WHEN '04' THEN dis_users_nm_rate ELSE 0 END) dis_users_nm_rate04,MAX(CASE data_source WHEN '05' THEN dis_users_nm_rate ELSE 0 END) dis_users_nm_rate05 FROM ( " +
                "SELECT*FROM ( " +
                "SELECT a.sd_date dt,'allpv' prov_id,'allct' city_id,a.hive_table_name,'2_1_8' kpi_id,a.data_source,concat(sum(count_nm),'') count_nm,round(sum(count_nm)/count_nm_sum_all*100,2) count_nm_rate,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm,concat(round(sum(dis_users_nm)/dis_users_nm_sum_all*100,2),'') dis_users_nm_rate FROM auto_user_citysource_rate a INNER JOIN ( " +
                "SELECT sd_date,hive_table_name,kpi_id,sum(count_nm) count_nm_sum_all,sum(users_nm) users_nm_sum_all,sum(dis_users_nm) dis_users_nm_sum_all FROM auto_user_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' AND a.kpi_id='1_1_8' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.sd_date=b.sd_date AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='1_1_8' GROUP BY a.sd_date,a.hive_table_name,a.kpi_id,a.data_source) AS tmp1 UNION ALL ( " +
                "SELECT a.sd_date dt,a.city_id prov_id,'allct' city_id,a.hive_table_name,'2_1_8' kpi_id,a.data_source,count_nm,concat(round(count_nm/count_nm_sum*100,2),'') count_nm_rate,users_nm,dis_users_nm,concat(round(dis_users_nm/dis_users_nm_sum*100,2),'') dis_users_nm_rate FROM auto_user_citysource_rate a INNER JOIN ( " +
                "SELECT sd_date,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_citysource_rate WHERE hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND kpi_id='1_1_8' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.sd_date=b.sd_date AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='1_1_8') UNION ALL ( " +
                "SELECT a.sd_date dt,substr(a.city_id,1,3) prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,count_nm,concat(round(count_nm/count_nm_sum*100,2),'') count_nm_rate,users_nm,dis_users_nm,concat(round(dis_users_nm/dis_users_nm_sum*100,2),'') dis_users_nm_rate FROM auto_user_citysource_rate a INNER JOIN ( " +
                "SELECT sd_date,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' AND a.kpi_id='2_1_8' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.sd_date=b.sd_date AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='2_1_8')) tmp WHERE dt>='${star_dts}' AND dt<='${end_dts}' GROUP BY dt,city_id,hive_table_name,kpi_id) tmp WHERE 1=1";

				return sql;
	}

	@Override
	public String LocationFusionMergelocation_mskInterval(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份'," + instance.getcttoC("city_id") + " '地市',hive_table_name '表名',kpi_id '数据类型',count_interval1 '区间为1的记录数',count_interval2 '区间为2-10的记录数',count_interval3 '区间为10-24的记录数',count_interval4 '区间为24-48的记录数',count_interval5 '区间大于48的记录数',"
				+ "dis_user_interval1 '区间为1的用户数',dis_user_interval2 '区间为2-10的用户数',dis_user_interval3 '区间为10-24的用户数',dis_user_interval4 '区间为24-48的用户数',dis_user_interval5 '区间大于48的用户数' FROM ( " +
                "SELECT t.dts dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,MAX(CASE end_tmp_field WHEN '1' THEN count_nm ELSE 0 END) count_interval1,MAX(CASE end_tmp_field WHEN '2-10' THEN count_nm ELSE 0 END) count_interval2,MAX(CASE end_tmp_field WHEN '10-24' THEN count_nm ELSE 0 END) count_interval3,MAX(CASE end_tmp_field WHEN '24-48' THEN count_nm ELSE 0 END) count_interval4,MAX(CASE end_tmp_field WHEN '>48' THEN count_nm ELSE 0 END) count_interval5,MAX(CASE end_tmp_field WHEN '1' THEN dis_users_nm ELSE 0 END) dis_user_interval1,MAX(CASE end_tmp_field WHEN '2-10' THEN dis_users_nm ELSE 0 END) dis_user_interval2,MAX(CASE end_tmp_field WHEN '10-24' THEN dis_users_nm ELSE 0 END) dis_user_interval3,MAX(CASE end_tmp_field WHEN '24-48' THEN dis_users_nm ELSE 0 END) dis_user_interval4,MAX(CASE end_tmp_field WHEN '>48' THEN dis_users_nm ELSE 0 END) dis_user_interval5 FROM ( " +
                "SELECT*FROM ( " +
                "SELECT a.dts,a.prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,count_nm,users_nm,dis_users_nm,round(count_nm/count_nm_sum*100,2) count_nm_rate FROM ( " +
                "SELECT sd_date dts,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_interval_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_12' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1)) a INNER JOIN ( " +
                "SELECT sd_date dts,'allpv' prov_id,'allct' city_id,hive_table_name,data_source,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_interval_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_12' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,data_source,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id AND a.data_source=b.data_source WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_12' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}') tmp UNION ALL ( " +
                "SELECT a.dts,a.prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,count_nm,users_nm,dis_users_nm,round(count_nm/count_nm_sum*100,2) count_nm_rate FROM ( " +
                "SELECT sd_date dts,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_interval_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_12' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),substr(substring_index(end_tmp_field,'_',-1),1,3)) a INNER JOIN ( " +
                "SELECT sd_date dts,hive_table_name,data_source,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_interval_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_12' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,data_source,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id AND a.data_source=b.data_source WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_12' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}') UNION ALL ( " +
                "SELECT a.dts,a.prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,count_nm,users_nm,dis_users_nm,round(count_nm/count_nm_sum*100,2) count_nm_rate FROM ( " +
                "SELECT sd_date dts,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,substr(substring_index(end_tmp_field,'_',-1),1,5) city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_interval_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_12' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),substr(substring_index(end_tmp_field,'_',-1),1,5)) a INNER JOIN ( " +
                "SELECT sd_date dts,hive_table_name,data_source,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_interval_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_12' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,data_source,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id AND a.data_source=b.data_source WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_0_12' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}')) t WHERE t.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND t.kpi_id='0_0_12' GROUP BY t.dts,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source) tmp WHERE 1=1 ";

				return sql;
	}

	@Override
	public String LocationFusionMergelocationUpdateInterval(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份',count_nm1 '[0,10)位置更新记录数',dis_users_nm1 '[0,10}位置更新用户数',count_nm2 '[10,20)位置更新记录数',dis_users_nm2 '[10,20}位置更新用户数',count_nm3 '[20,30)位置更新记录数',dis_users_nm3 '[20,30)位置更新用户数',count_nm4 '[30,40)位置更新记录数',dis_users_nm4 '[30,40)位置更新用户数',count_nm5 '[40,60)位置更新记录数',dis_users_nm5 '[40,60)位置更新用户数',count_nm6 '[60,90)位置更新记录数',dis_users_nm6 '[60,90)位置更新用户数',count_nm7 '[90,120)位置更新记录数',dis_users_nm7 '[90,120)位置更新用户数',count_nm8 '[120,max)位置更新记录数',dis_users_nm8 '[120,max)位置更新用户数' FROM ( " +
                "SELECT dt,prov_id,concat(SUM(CASE end_tmp_field WHEN '0-10' THEN count_nm ELSE 0 END),'') count_nm1,concat(SUM(CASE end_tmp_field WHEN '0-10' THEN dis_users_nm ELSE 0 END),'') dis_users_nm1,concat(SUM(CASE end_tmp_field WHEN '10-20' THEN count_nm ELSE 0 END),'') count_nm2,concat(SUM(CASE end_tmp_field WHEN '10-20' THEN dis_users_nm ELSE 0 END),'') dis_users_nm2,concat(SUM(CASE end_tmp_field WHEN '20-30' THEN count_nm ELSE 0 END),'') count_nm3,concat(SUM(CASE end_tmp_field WHEN '20-30' THEN dis_users_nm ELSE 0 END),'') dis_users_nm3,concat(SUM(CASE end_tmp_field WHEN '30-40' THEN count_nm ELSE 0 END),'') count_nm4,concat(SUM(CASE end_tmp_field WHEN '30-40' THEN dis_users_nm ELSE 0 END),'') dis_users_nm4,concat(SUM(CASE end_tmp_field WHEN '40-60' THEN count_nm ELSE 0 END),'') count_nm5,concat(SUM(CASE end_tmp_field WHEN '40-60' THEN dis_users_nm ELSE 0 END),'') dis_users_nm5,concat(SUM(CASE end_tmp_field WHEN '60-90' THEN count_nm ELSE 0 END),'') count_nm6,concat(SUM(CASE end_tmp_field WHEN '60-90' THEN dis_users_nm ELSE 0 END),'') dis_users_nm6,concat(SUM(CASE end_tmp_field WHEN '90-120' THEN count_nm ELSE 0 END),'') count_nm7,concat(SUM(CASE end_tmp_field WHEN '90-120' THEN dis_users_nm ELSE 0 END),'') dis_users_nm7,concat(SUM(CASE end_tmp_field WHEN '>=120' THEN count_nm ELSE 0 END),'') count_nm8,concat(SUM(CASE end_tmp_field WHEN '>=120' THEN dis_users_nm ELSE 0 END),'') dis_users_nm8 FROM ( " +
                "SELECT dts dt,city_id prov_id,end_tmp_field,count_nm,dis_users_nm FROM ( " +
                "SELECT sd_date dts,'allpv' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_update_interval_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${star_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND t.kpi_id='1_0_13' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) AS tmp UNION ALL ( " +
                "SELECT sd_date dt,substr(city_id,1,3) prov_id,end_tmp_field,sum(count_nm) count_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_update_interval_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${star_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND t.kpi_id='1_0_13' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count)) t WHERE 1=1 GROUP BY t.dt,prov_id) tmp WHERE 1=1 ";

				return sql;
	}

	@Override
	public String LocationFusionMergelocationRangeProvid(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份',hive_table_name '表名',data_source '数据源类型',kpi_id '数据类型',count_nm1 '区间为1的记录数',dis_users_nm1 '区间为1的用户数',count_nm2 '区间为2的记录数',dis_users_nm2 '区间为2的用户数',count_nm3 '区间为3的记录数',dis_users_nm3 '区间为3的用户数',count_nm4 '区间为4的记录数',dis_users_nm4 '区间为4的用户数',count_nm5 '区间为5的记录数',dis_users_nm5 '区间为5的用户数',count_nm6 '区间为6的记录数',dis_users_nm6 '区间为6的用户数',count_nm7 '区间为7的记录数',dis_users_nm7 '区间为7的用户数',count_nm8 '区间为8的记录数',dis_users_nm8 '区间为8的用户数',count_nm9 '区间为9的记录数',dis_users_nm9 '区间为9的用户数',count_nm10 '区间为10的记录数',dis_users_nm10 '区间为10的用户数',count_nm11 '区间为11的记录数',dis_users_nm1 '区间为11的用户数',count_nm12 '区间为12的记录数',dis_users_nm12 '区间为12的用户数' FROM ( " +
                "SELECT t.sd_date dt,'allpv' prov_id,t.hive_table_name,t.data_source,t.kpi_id,MAX(CASE end_tmp_field WHEN '1' THEN count_nm ELSE 0 END) count_nm1,MAX(CASE end_tmp_field WHEN '1' THEN users_nm ELSE 0 END) users_nm1,MAX(CASE end_tmp_field WHEN '1' THEN dis_users_nm ELSE 0 END) dis_users_nm1,MAX(CASE end_tmp_field WHEN '2' THEN count_nm ELSE 0 END) count_nm2,MAX(CASE end_tmp_field WHEN '2' THEN users_nm ELSE 0 END) users_nm2,MAX(CASE end_tmp_field WHEN '2' THEN dis_users_nm ELSE 0 END) dis_users_nm2,MAX(CASE end_tmp_field WHEN '3' THEN count_nm ELSE 0 END) count_nm3,MAX(CASE end_tmp_field WHEN '3' THEN users_nm ELSE 0 END) users_nm3,MAX(CASE end_tmp_field WHEN '3' THEN dis_users_nm ELSE 0 END) dis_users_nm3,MAX(CASE end_tmp_field WHEN '4' THEN count_nm ELSE 0 END) count_nm4,MAX(CASE end_tmp_field WHEN '4' THEN users_nm ELSE 0 END) users_nm4,MAX(CASE end_tmp_field WHEN '4' THEN dis_users_nm ELSE 0 END) dis_users_nm4,MAX(CASE end_tmp_field WHEN '5' THEN count_nm ELSE 0 END) count_nm5,MAX(CASE end_tmp_field WHEN '5' THEN users_nm ELSE 0 END) users_nm5,MAX(CASE end_tmp_field WHEN '5' THEN dis_users_nm ELSE 0 END) dis_users_nm5,MAX(CASE end_tmp_field WHEN '6' THEN count_nm ELSE 0 END) count_nm6,MAX(CASE end_tmp_field WHEN '6' THEN users_nm ELSE 0 END) users_nm6,MAX(CASE end_tmp_field WHEN '6' THEN dis_users_nm ELSE 0 END) dis_users_nm6,MAX(CASE end_tmp_field WHEN '7' THEN count_nm ELSE 0 END) count_nm7,MAX(CASE end_tmp_field WHEN '7' THEN users_nm ELSE 0 END) users_nm7,MAX(CASE end_tmp_field WHEN '7' THEN dis_users_nm ELSE 0 END) dis_users_nm7,MAX(CASE end_tmp_field WHEN '8' THEN count_nm ELSE 0 END) count_nm8,MAX(CASE end_tmp_field WHEN '8' THEN users_nm ELSE 0 END) users_nm8,MAX(CASE end_tmp_field WHEN '8' THEN dis_users_nm ELSE 0 END) dis_users_nm8,MAX(CASE end_tmp_field WHEN '9' THEN count_nm ELSE 0 END) count_nm9,MAX(CASE end_tmp_field WHEN '9' THEN users_nm ELSE 0 END) users_nm9,MAX(CASE end_tmp_field WHEN '9' THEN dis_users_nm ELSE 0 END) dis_users_nm9,MAX(CASE end_tmp_field WHEN '10-20' THEN count_nm ELSE 0 END) count_nm10,MAX(CASE end_tmp_field WHEN '10-20' THEN users_nm ELSE 0 END) users_nm10,MAX(CASE end_tmp_field WHEN '10-20' THEN dis_users_nm ELSE 0 END) dis_users_nm10,MAX(CASE end_tmp_field WHEN '20-30' THEN count_nm ELSE 0 END) count_nm11,MAX(CASE end_tmp_field WHEN '20-30' THEN users_nm ELSE 0 END) users_nm11,MAX(CASE end_tmp_field WHEN '20-30' THEN dis_users_nm ELSE 0 END) dis_users_nm11,MAX(CASE end_tmp_field WHEN '>=30' THEN count_nm ELSE 0 END) count_nm12,MAX(CASE end_tmp_field WHEN '>=30' THEN users_nm ELSE 0 END) users_nm12,MAX(CASE end_tmp_field WHEN '>=30' THEN dis_users_nm ELSE 0 END) dis_users_nm12 FROM auto_user_range_provid_rate t WHERE t.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND t.kpi_id='0_0_14' GROUP BY t.sd_date,t.city_id,t.hive_table_name,t.data_source,t.kpi_id) tmp WHERE dt>='${star_dts}' AND dt<='${end_dts}' ";

				return sql;
	}

	@Override
	public String LocationFusionMergelocationRangeCityid(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub

		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份'," + instance.getcttoC("city_id") + " '地市',count_nm1 '区间为1记录数',dis_users_nm1 '区间为1用户数',count_nm1 '区间为2记录数',dis_users_nm1 '区间为2用户数',count_nm1 '区间为3记录数',dis_users_nm1 '区间为3用户数',count_nm1 '区间为4记录数',dis_users_nm1 '区间为4用户数',count_nm1 '区间为5记录数',dis_users_nm1 '区间为5用户数',count_nm1 '区间为6记录数',dis_users_nm1 '区间为6用户数',count_nm1 '区间为7记录数',dis_users_nm1 '区间为7用户数',count_nm1 '区间为8记录数',dis_users_nm1 '区间为8用户数',count_nm1 '区间为9记录数',dis_users_nm1 '区间为9用户数',count_nm1 '区间为[10,20)记录数',dis_users_nm1 '区间为[10,20)用户数',count_nm1 '区间为[20,30)记录数',dis_users_nm1 '区间为[20,30)用户数',count_nm1 '区间为[30,max)记录数',dis_users_nm1 '区间为[30,max)用户数' FROM ( " +
                "SELECT t.sd_date dt,'allpv' prov_id,'allct' city_id,MAX(CASE end_tmp_field WHEN '1' THEN count_nm ELSE 0 END) count_nm1,MAX(CASE end_tmp_field WHEN '1' THEN dis_users_nm ELSE 0 END) dis_users_nm1,MAX(CASE end_tmp_field WHEN '2' THEN count_nm ELSE 0 END) count_nm2,MAX(CASE end_tmp_field WHEN '2' THEN dis_users_nm ELSE 0 END) dis_users_nm2,MAX(CASE end_tmp_field WHEN '3' THEN count_nm ELSE 0 END) count_nm3,MAX(CASE end_tmp_field WHEN '3' THEN dis_users_nm ELSE 0 END) dis_users_nm3,MAX(CASE end_tmp_field WHEN '4' THEN count_nm ELSE 0 END) count_nm4,MAX(CASE end_tmp_field WHEN '4' THEN dis_users_nm ELSE 0 END) dis_users_nm4,MAX(CASE end_tmp_field WHEN '5' THEN count_nm ELSE 0 END) count_nm5,MAX(CASE end_tmp_field WHEN '5' THEN dis_users_nm ELSE 0 END) dis_users_nm5,MAX(CASE end_tmp_field WHEN '6' THEN count_nm ELSE 0 END) count_nm6,MAX(CASE end_tmp_field WHEN '6' THEN dis_users_nm ELSE 0 END) dis_users_nm6,MAX(CASE end_tmp_field WHEN '7' THEN count_nm ELSE 0 END) count_nm7,MAX(CASE end_tmp_field WHEN '7' THEN dis_users_nm ELSE 0 END) dis_users_nm7,MAX(CASE end_tmp_field WHEN '8' THEN count_nm ELSE 0 END) count_nm8,MAX(CASE end_tmp_field WHEN '8' THEN dis_users_nm ELSE 0 END) dis_users_nm8,MAX(CASE end_tmp_field WHEN '9' THEN count_nm ELSE 0 END) count_nm9,MAX(CASE end_tmp_field WHEN '9' THEN dis_users_nm ELSE 0 END) dis_users_nm9,MAX(CASE end_tmp_field WHEN '10-20' THEN count_nm ELSE 0 END) count_nm10,MAX(CASE end_tmp_field WHEN '10-20' THEN dis_users_nm ELSE 0 END) dis_users_nm10,MAX(CASE end_tmp_field WHEN '20-30' THEN count_nm ELSE 0 END) count_nm11,MAX(CASE end_tmp_field WHEN '20-30' THEN dis_users_nm ELSE 0 END) dis_users_nm11,MAX(CASE end_tmp_field WHEN '>=30' THEN count_nm ELSE 0 END) count_nm12,MAX(CASE end_tmp_field WHEN '>=30' THEN dis_users_nm ELSE 0 END) dis_users_nm12 FROM auto_user_range_cityid_rate t WHERE t.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND t.kpi_id='2_0_15' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY t.sd_date,t.city_id,t.hive_table_name,t.data_source,t.kpi_id) tmp WHERE 1=1";

			
			return sql;
	}

	@Override
	public String LocationFusionMergelocationPP(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期',prov_id '省份',city_id '地市',count_nm01 'OIDD记录数',dis_users_nm01 'OIDD用户数',count_nm_rate01 'OIDD记录数占比',count_nm02 '3gDPI记录数',dis_users_nm02 '3gDPI用户数',count_nm_rate02 '3gDPI记录数占比',count_nm03 'DDR记录数',dis_users_nm03 'DDR用户数',count_nm_rate03 'DDR记录数占比',count_nm04 'WCDR记录数',dis_users_nm04 'WCDR用户数',count_nm_rate04 'WCDR记录数占比',count_nm05 '4gDPI记录数',dis_users_nm05 '4gDPI用户数',count_nm_rate05 '4gDPI记录数占比' FROM ( " +
                "SELECT dt,prov_id,city_id,MAX(CASE data_source WHEN '01' THEN count_nm ELSE 0 END) count_nm01,MAX(CASE data_source WHEN '01' THEN dis_users_nm ELSE 0 END) dis_users_nm01,MAX(CASE data_source WHEN '01' THEN concat(count_nm_rate,'%') ELSE 0 END) count_nm_rate01,MAX(CASE data_source WHEN '02' THEN count_nm ELSE 0 END) count_nm02,MAX(CASE data_source WHEN '02' THEN dis_users_nm ELSE 0 END) dis_users_nm02,MAX(CASE data_source WHEN '02' THEN concat(count_nm_rate,'%') ELSE 0 END) count_nm_rate02,MAX(CASE data_source WHEN '03' THEN count_nm ELSE 0 END) count_nm03,MAX(CASE data_source WHEN '03' THEN dis_users_nm ELSE 0 END) dis_users_nm03,MAX(CASE data_source WHEN '03' THEN concat(count_nm_rate,'%') ELSE 0 END) count_nm_rate03,MAX(CASE data_source WHEN '04' THEN count_nm ELSE 0 END) count_nm04,MAX(CASE data_source WHEN '04' THEN dis_users_nm ELSE 0 END) dis_users_nm04,MAX(CASE data_source WHEN '04' THEN concat(count_nm_rate,'%') ELSE 0 END) count_nm_rate04,MAX(CASE data_source WHEN '05' THEN count_nm ELSE 0 END) count_nm05,MAX(CASE data_source WHEN '05' THEN dis_users_nm ELSE 0 END) dis_users_nm05,MAX(CASE data_source WHEN '05' THEN concat(count_nm_rate,'%') ELSE 0 END) count_nm_rate05 FROM ( " +
                "SELECT a.dts dt,'allpv' prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,count_nm,users_nm,dis_users_nm,round(count_nm/count_nm_sum*100,2) count_nm_rate FROM ( " +
                "SELECT sd_date dts,'allct' city_id,hive_table_name,kpi_id,data_source,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date dts,'allct' city_id,hive_table_name,data_source,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,data_source,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id AND a.data_source=b.data_source WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_7' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}' UNION ALL " +
                "SELECT a.dts dt,a.prov_id,'allct' city_id,a.hive_table_name,a.kpi_id,a.data_source,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,2) count_nm_rate FROM ( " +
                "SELECT sd_date dts,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,hive_table_name,kpi_id,data_source,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,3),hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date dts,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_7' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}' UNION ALL " +
                "SELECT a.dts dt,a.prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,2) count_nm_rate FROM ( " +
                "SELECT sd_date dts,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,substr(substring_index(end_tmp_field,'_',-1),1,5) city_id,hive_table_name,kpi_id,data_source,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,5),hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date dts,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_ping_pong_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_7' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_7' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}') t GROUP BY dt,prov_id,city_id) a WHERE 1=1 ";

			return sql;
	}

	@Override
	public String LocationFusionMergelocationSpeeding(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份'," + instance.getcttoC("city_id") + " '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',count_nm '记录数',dis_users_nm '用户数',concat(round(count_nm_rate,2),'%') '记录数占比',concat(round(dis_users_nm_rate,2),'%') '用户数占比' FROM ( " +
                "SELECT*FROM ( " +
                "SELECT a.dts dt,'allpv' prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,a.end_tmp_field,count_nm,users_nm,dis_users_nm,round(count_nm/count_nm_sum*100,4) AS count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,4) AS dis_users_nm_rate FROM ( " +
                "SELECT sd_date AS dts,'allct' AS city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) AS end_tmp_field,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_speeding_citysource_rate WHERE substring_index(end_tmp_field,'_',1)='1' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1)) a INNER JOIN ( " +
                "SELECT sd_date AS dts,'allct' AS city_id,hive_table_name,data_source,kpi_id,SUM(count_nm) AS count_nm_sum,SUM(users_nm) AS users_nm_sum,SUM(dis_users_nm) AS dis_users_nm_sum FROM auto_speeding_citysource_rate GROUP BY sd_date,hive_table_name,data_source,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id AND a.data_source=b.data_source WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_6' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}') tmp UNION ALL ( " +
                "SELECT a.dts dt,a.prov_id,'allct' city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,4) AS count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,4) AS dis_users_nm_rate FROM ( " +
                "SELECT sd_date AS dts,substr(substring_index(end_tmp_field,'_',-1),1,3) AS prov_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) AS end_tmp_field,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_speeding_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_6' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,3),substring_index(end_tmp_field,'_',1),hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date AS dts,hive_table_name,kpi_id,SUM(count_nm) AS count_nm_sum,SUM(users_nm) AS users_nm_sum,SUM(dis_users_nm) AS dis_users_nm_sum FROM auto_speeding_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_6' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_6' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}') UNION ALL ( " +
                "SELECT a.dts dt,a.prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,4) AS count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,4) AS dis_users_nm_rate FROM ( " +
                "SELECT sd_date AS dts,substr(substring_index(end_tmp_field,'_',-1),1,3) AS prov_id,substr(substring_index(end_tmp_field,'_',-1),1,5) AS city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) AS end_tmp_field,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_speeding_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_6' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,5),substring_index(end_tmp_field,'_',1),hive_table_name,kpi_id,data_source) a INNER JOIN ( " +
                "SELECT sd_date AS dts,hive_table_name,kpi_id,SUM(count_nm) AS count_nm_sum,SUM(users_nm) AS users_nm_sum,SUM(dis_users_nm) AS dis_users_nm_sum FROM auto_speeding_citysource_rate a WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_6' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE a.hive_table_name='dwi_m.dwi_res_regn_mergelocation_msk_d' AND a.kpi_id='0_1_6' AND a.dts>='${star_dts}' AND a.dts<='${end_dts}')) tmp WHERE end_tmp_field='1'";


			return sql;
	}

	@Override
	public String StoppingPointStaypointPPAllNum(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ "hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_ping_pong_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND substring_index(end_tmp_field,'_',1)='1' AND t.kpi_id='0_0_7' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),tmp_count) AS tmp UNION ALL ("
				+ " SELECT sd_date dt,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_ping_pong_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND substring_index(end_tmp_field,'_',1)='1' AND length(SUBSTRING_INDEX(end_tmp_field,'_',-1))=5 AND t.kpi_id='0_0_7' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,3),hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),tmp_count)) tmp WHERE end_tmp_field='1' AND  1 = 1 ";
			return sql;
	}

	@Override
	public String StoppingPointStaypointPPNum(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date AS dt,substr(SUBSTRING_INDEX(end_tmp_field,'_',-1),1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,SUBSTRING_INDEX(end_tmp_field,'_',1) AS end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_ping_pong_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_7' AND SUBSTRING_INDEX(end_tmp_field,'_',1)='1' GROUP BY sd_date,substr(SUBSTRING_INDEX(end_tmp_field,'_',-1),1,3),hive_table_name,kpi_id,data_source,SUBSTRING_INDEX(end_tmp_field,'_',1),tmp_count) tmp UNION ALL ("
				+ " SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,SUBSTRING_INDEX(end_tmp_field,'_',1) AS end_tmp_field,SUM(tmp_count) AS tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_ping_pong_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_7' AND SUBSTRING_INDEX(end_tmp_field,'_',1)='1' GROUP BY sd_date,hive_table_name,kpi_id,data_source,SUBSTRING_INDEX(end_tmp_field,'_',1),tmp_count) UNION ALL ("
				+ " SELECT sd_date AS dt,substr(SUBSTRING_INDEX(end_tmp_field,'_',-1),1,3) prov_id,substr(SUBSTRING_INDEX(end_tmp_field,'_',-1),1,5) city_id,hive_table_name,kpi_id,data_source,SUBSTRING_INDEX(end_tmp_field,'_',1) AS end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_ping_pong_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND SUBSTRING_INDEX(end_tmp_field,'_',1)='1' AND length(SUBSTRING_INDEX(end_tmp_field,'_',-1))=5 AND t.kpi_id='0_0_7')) tmp WHERE 1=1 ";
			return sql;
	}

	@Override
	public String StoppingPointStaypointDNum(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT dt,prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,hive_table_name,kpi_id,data_source,'no' end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_stay_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_17' GROUP BY sd_date,hive_table_name,kpi_id,data_source,tmp_count) AS tmp UNION ALL ("
				+ " SELECT sd_date dt,substr(end_tmp_field,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,'no' end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_stay_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_17' GROUP BY sd_date,substr(end_tmp_field,1,3),hive_table_name,kpi_id,data_source,tmp_count) UNION ALL ("
				+ " SELECT sd_date dt,substr(end_tmp_field,1,3),substr(end_tmp_field,1,5),hive_table_name,kpi_id,data_source,'no' end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_stay_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_17')) tmp WHERE  1 = 1 ";
			return sql;
	}

	@Override
	public String StoppingPointStaypoint24hourNum(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub

		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT a.dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',a.time '时段',a.count_nm '记录数',a.dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) time,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_stay_rangetime_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_16' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),tmp_count) AS tmp UNION ALL ("
				+ " SELECT sd_date dts,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_stay_rangetime_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_16' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,3),hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),tmp_count) UNION ALL ("
				+ " SELECT sd_date dts,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,substr(substring_index(end_tmp_field,'_',-1),1,5) city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_stay_rangetime_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_16')) a WHERE 1=1 ";
				return sql;
	}

	@Override
	public String StoppingPointStaypointInterval(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',tmp_count,count_nm1 '区间为1的记录数',dis_users_nm1 '区间为1的用户数',count_nm2 '区间为2-10的记录数',dis_users_nm2 '区间为2-10的用户数',count_nm3 '区间为11-24的记录数',dis_users_nm3 '区间为11-24的用户数',count_nm4 '区间为25-48的记录数',dis_users_nm4 '区间为25-48的用户数',count_nm5 '区间大于49的记录数',dis_users_nm5 '区间大于49的用户数' FROM ("
				+ " SELECT t.dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source,t.tmp_count,MAX(CASE end_tmp_field WHEN '1' THEN count_nm ELSE 0 END) count_nm1,MAX(CASE end_tmp_field WHEN '1' THEN users_nm ELSE 0 END) users_nm1,MAX(CASE end_tmp_field WHEN '1' THEN dis_users_nm ELSE 0 END) dis_users_nm1,MAX(CASE end_tmp_field WHEN '2-10' THEN count_nm ELSE 0 END) count_nm2,MAX(CASE end_tmp_field WHEN '2-10' THEN users_nm ELSE 0 END) users_nm2,MAX(CASE end_tmp_field WHEN '2-10' THEN dis_users_nm ELSE 0 END) dis_users_nm2,MAX(CASE end_tmp_field WHEN '11-24' THEN count_nm ELSE 0 END) count_nm3,MAX(CASE end_tmp_field WHEN '11-24' THEN users_nm ELSE 0 END) users_nm3,MAX(CASE end_tmp_field WHEN '11-24' THEN dis_users_nm ELSE 0 END) dis_users_nm3,MAX(CASE end_tmp_field WHEN '25-48' THEN count_nm ELSE 0 END) count_nm4,MAX(CASE end_tmp_field WHEN '25-48' THEN users_nm ELSE 0 END) users_nm4,MAX(CASE end_tmp_field WHEN '25-48' THEN dis_users_nm ELSE 0 END) dis_users_nm4,MAX(CASE end_tmp_field WHEN '>=49' THEN count_nm ELSE 0 END) count_nm5,MAX(CASE end_tmp_field WHEN '>=49' THEN users_nm ELSE 0 END) users_nm5,MAX(CASE end_tmp_field WHEN '>=49' THEN dis_users_nm ELSE 0 END) dis_users_nm5 FROM ("
				+ " SELECT*FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_stay_interval_rate t WHERE t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.kpi_id='0_0_18' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),tmp_count) AS tmp UNION ALL ("
				+ " SELECT sd_date dt,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_stay_interval_rate t WHERE t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_18' AND t.sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),tmp_count) UNION ALL ("
				+ " SELECT sd_date dt,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,substr(substring_index(end_tmp_field,'_',-1),1,5) city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_stay_interval_rate t WHERE t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_18' AND t.sd_date>='${star_dts}' AND sd_date<='${end_dts}'"
				+ " )) tmp WHERE 1 = 1) t WHERE 1 = 1 GROUP BY t.dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source) tmp WHERE 1 = 1 ";
			return sql;
	}

	@Override
	public String StoppingPointStaypointSixhours(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,'no' AS end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_stay_sixhours_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_19' GROUP BY sd_date,hive_table_name,kpi_id,data_source,tmp_count) tmp UNION ALL ("
				+ " SELECT sd_date AS dt,substr(end_tmp_field,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,'no' AS end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_stay_sixhours_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_19' GROUP BY sd_date,substr(end_tmp_field,1,3),hive_table_name,kpi_id,data_source,tmp_count) UNION ALL ("
				+ " SELECT sd_date AS dt,substr(end_tmp_field,1,3) prov_id,substr(end_tmp_field,1,5) city_id,hive_table_name,kpi_id,data_source,'no' AS end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_stay_sixhours_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_19')) tmp WHERE 1=1 ";
			return sql;
	}

	@Override
	public String StoppingPointStaypointPoint(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT t.sd_date dt,t.city_id,t.hive_table_name,t.kpi_id,t.data_source,t.end_tmp_field,t.tmp_count,t.count_nm,t.users_nm,t.dis_users_nm FROM auto_user_stay_point_rate t WHERE t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_20') tmp WHERE dt>='${star_dts}' AND dt<='${end_dts}' ";
		return sql;
	}

	@Override
	public String StoppingPointStaypointNogrid(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(tmp_count,'_',1) AS end_tmp_field,'no' AS tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_stay_nogrid_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND substring_index(tmp_count,'_',1)='1' AND t.kpi_id='0_0_21' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(tmp_count,'_',1)) tmp UNION ALL ("
				+ " SELECT sd_date AS dt,substr(substring_index(tmp_count,'_',-1),1,3) AS prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) AS end_tmp_field,'no' AS tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_stay_nogrid_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_21' GROUP BY sd_date,substr(substring_index(tmp_count,'_',-1),1,3),hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1)) UNION ALL ("
				+ " SELECT sd_date AS dt,substr(substring_index(tmp_count,'_',-1),1,3) AS prov_id,substr(substring_index(tmp_count,'_',-1),1,5) AS city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) AS end_tmp_field,'no' AS tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_stay_nogrid_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_21')) tmp WHERE 1=1 ";
			return sql;
	}

	@Override
	public String StoppingPointStaypointNopoint(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,count_nm tmp_count,sum(tmp_count) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_stay_nopoint_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_22' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),count_nm) AS tmp UNION ALL ("
				+ " SELECT sd_date dt,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,count_nm tmp_count,sum(tmp_count) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_stay_nopoint_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_22' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,3),hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),count_nm) UNION ALL ("
				+ " SELECT sd_date dt,substr(substring_index(end_tmp_field,'_',-1),1,3),substr(substring_index(end_tmp_field,'_',-1),1,5),hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,count_nm tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_stay_nopoint_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND substring_index(end_tmp_field,'_',1)='1' AND t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_22')) tmp WHERE 1 = 1 ";
			return sql;
	}

	@Override
	public String StoppingPointStaypointRangetime(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',tmp_count,count_nm1 '区间为5的记录数',dis_users_nm1 '区间为5的用户数',count_nm2 '区间为5-10的记录数',dis_users_nm2 '区间为5-10的用户数',count_nm3 '区间为10-30的记录数',dis_users_nm3 '区间为10-30的用户数',count_nm4 '区间为30-60的记录数',dis_users_nm4 '区间为30-60的用户数',count_nm5 '区间为60-120的记录数',dis_users_nm5 '区间为60-120的用户数',count_nm6 '区间大于120的记录数',dis_users_nm6 '区间大于120的用户数' FROM ("
				+ " SELECT t.dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source,t.tmp_count,MAX(CASE end_tmp_field WHEN '5' THEN count_nm ELSE 0 END) count_nm1,MAX(CASE end_tmp_field WHEN '5' THEN users_nm ELSE 0 END) users_nm1,MAX(CASE end_tmp_field WHEN '5' THEN dis_users_nm ELSE 0 END) dis_users_nm1,MAX(CASE end_tmp_field WHEN '5-10' THEN count_nm ELSE 0 END) count_nm2,MAX(CASE end_tmp_field WHEN '5-10' THEN users_nm ELSE 0 END) users_nm2,MAX(CASE end_tmp_field WHEN '5-10' THEN dis_users_nm ELSE 0 END) dis_users_nm2,MAX(CASE end_tmp_field WHEN '10-30' THEN count_nm ELSE 0 END) count_nm3,MAX(CASE end_tmp_field WHEN '10-30' THEN users_nm ELSE 0 END) users_nm3,MAX(CASE end_tmp_field WHEN '10-30' THEN dis_users_nm ELSE 0 END) dis_users_nm3,MAX(CASE end_tmp_field WHEN '30-60' THEN count_nm ELSE 0 END) count_nm4,MAX(CASE end_tmp_field WHEN '30-60' THEN users_nm ELSE 0 END) users_nm4,MAX(CASE end_tmp_field WHEN '30-60' THEN dis_users_nm ELSE 0 END) dis_users_nm4,MAX(CASE end_tmp_field WHEN '60-120' THEN count_nm ELSE 0 END) count_nm5,MAX(CASE end_tmp_field WHEN '60-120' THEN users_nm ELSE 0 END) users_nm5,MAX(CASE end_tmp_field WHEN '60-120' THEN dis_users_nm ELSE 0 END) dis_users_nm5,MAX(CASE end_tmp_field WHEN '>=120' THEN count_nm ELSE 0 END) count_nm6,MAX(CASE end_tmp_field WHEN '>=120' THEN users_nm ELSE 0 END) users_nm6,MAX(CASE end_tmp_field WHEN '>=120' THEN dis_users_nm ELSE 0 END) dis_users_nm6 FROM ("
				+ " SELECT*FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_stay_rangetime_rate t WHERE t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.kpi_id='0_0_23' GROUP BY sd_date,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),tmp_count) AS tmp UNION ALL ("
				+ " SELECT sd_date dt,substr(substring_index(end_tmp_field,'_',-1),1,3) prod_id,'allct' city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_stay_rangetime_rate t WHERE t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_23' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(substring_index(end_tmp_field,'_',-1),1,3),hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1),tmp_count) UNION ALL ("
				+ " SELECT sd_date dt,substr(substring_index(end_tmp_field,'_',-1),1,3) prov_id,substr(substring_index(end_tmp_field,'_',-1),1,5) city_id,hive_table_name,kpi_id,data_source,substring_index(end_tmp_field,'_',1) end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_stay_rangetime_rate t WHERE t.hive_table_name='dwi_m.dwi_res_regn_staypoint_msk_d' AND t.kpi_id='0_0_23' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}')) tmp WHERE dt>='${star_dts}' AND dt<='${end_dts}') t WHERE 1=1 GROUP BY t.dt,t.prov_Id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source) tmp WHERE 1=1 ";
				return sql;
	}

	@Override
	public String CrossDomainCity(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',count_nm '记录数',count_nm_volatility '记录数波动率',users_nm '用户数',users_nm_volatility '用户数波动率' FROM ("
				+ " SELECT t.sd_date AS dt,t.hive_table_name,t.kpi_id,'allpv' prov_id,'allct' city_id,t.data_source,t.end_tmp_field,t.tmp_count,t.count_nm,t.count_nm_volatility,t.users_nm,t.users_nm_volatility,t.dis_users_nm,t.dis_users_nm_volatility FROM auto_user_citysource_rate_volatility t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='0_0_8' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}') tmp WHERE 1=1 ";
		return sql;

	}

	@Override
	public String CrossDomainCityRangetime(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',tmp_count,count_nm1 '区间为0-1的记录数',dis_users_nm1 '区间为0-1的用户数',count_nm2 '区间为1-2的记录数',dis_users_nm2 '区间为1-2的用户录数',count_nm3 '区间为2-3的记录数',dis_users_nm3 '区间为2-3的用户数',count_nm4 '区间为3-4的记录数',dis_users_nm4 '区间为3-4的用户数',count_nm5 '区间为4-5的记录数',dis_users_nm5 '区间为4-5的用户数',count_nm6 '区间为5-6的记录数',dis_users_nm6 '区间为5-6的用户数',count_nm7 '区间为6-7的记录数',dis_users_nm7 '区间为6-7的用户数',count_nm8 '区间为7-8的记录数',dis_users_nm8 '区间为7-8的用户数',count_nm9 '区间为8-9的记录数',dis_users_nm9 '区间为8-9的用户数',count_nm10 '区间为9-10的记录数',dis_users_nm10 '区间为9-10的用户数',count_nm11 '区间为10-11的记录数',dis_users_nm11 '区间为10-11的用户数',count_nm12 '区间为11-12的记录数',dis_users_nm12 '区间为11-12的用户数',count_nm13 '区间为12-13的记录数',dis_users_nm13 '区间为12-13的用户数',count_nm14 '区间为13-14的记录数',dis_users_nm14 '区间为13-14的用户数',count_nm15 '区间为14-15的记录数',dis_users_nm15 '区间为14-15的用户数',count_nm16 '区间为15-16的记录数',dis_users_nm16 '区间为15-16的用户数',count_nm17 '区间为16-17的记录数',dis_users_nm17 '区间为16-17的用户数',count_nm18 '区间为17-18的记录数',dis_users_nm18 '区间为17-18的用户数',count_nm19 '区间为18-19的记录数',dis_users_nm19 '区间为18-19的用户数',count_nm20 '区间为19-20的记录数',dis_users_nm20 '区间为19-20的用户数',count_nm21 '区间为20-21的记录数',dis_users_nm21 '区间为20-21的用户数',count_nm22 '区间为21-22的记录数',dis_users_nm22 '区间为21-22的用户数',count_nm23 '区间为22-23的记录数',dis_users_nm23 '区间为22-23的用户数',count_nm24 '区间为23-24的记录数',dis_users_nm24 '区间为23-24的用户数',count_nm25 '区间为24-48的记录数',dis_users_nm25 '区间为24-48的用户数' FROM ("
				+ " SELECT dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source,t.tmp_count,MAX(CASE end_tmp_field WHEN '0-1' THEN count_nm ELSE 0 END) AS count_nm1,MAX(CASE end_tmp_field WHEN '0-1' THEN users_nm ELSE 0 END) AS users_nm1,MAX(CASE end_tmp_field WHEN '0-1' THEN dis_users_nm ELSE 0 END) AS dis_users_nm1,MAX(CASE end_tmp_field WHEN '1-2' THEN count_nm ELSE 0 END) AS count_nm2,MAX(CASE end_tmp_field WHEN '1-2' THEN users_nm ELSE 0 END) AS users_nm2,MAX(CASE end_tmp_field WHEN '1-2' THEN dis_users_nm ELSE 0 END) AS dis_users_nm2,MAX(CASE end_tmp_field WHEN '2-3' THEN count_nm ELSE 0 END) AS count_nm3,MAX(CASE end_tmp_field WHEN '2-3' THEN users_nm ELSE 0 END) AS users_nm3,MAX(CASE end_tmp_field WHEN '2-3' THEN dis_users_nm ELSE 0 END) AS dis_users_nm3,MAX(CASE end_tmp_field WHEN '3-4' THEN count_nm ELSE 0 END) AS count_nm4,MAX(CASE end_tmp_field WHEN '3-4' THEN users_nm ELSE 0 END) AS users_nm4,MAX(CASE end_tmp_field WHEN '3-4' THEN dis_users_nm ELSE 0 END) AS dis_users_nm4,MAX(CASE end_tmp_field WHEN '4-5' THEN count_nm ELSE 0 END) AS count_nm5,MAX(CASE end_tmp_field WHEN '4-5' THEN users_nm ELSE 0 END) AS users_nm5,MAX(CASE end_tmp_field WHEN '4-5' THEN dis_users_nm ELSE 0 END) AS dis_users_nm5,MAX(CASE end_tmp_field WHEN '5-6' THEN count_nm ELSE 0 END) AS count_nm6,MAX(CASE end_tmp_field WHEN '5-6' THEN users_nm ELSE 0 END) AS users_nm6,MAX(CASE end_tmp_field WHEN '5-6' THEN dis_users_nm ELSE 0 END) AS dis_users_nm6,MAX(CASE end_tmp_field WHEN '6-7' THEN count_nm ELSE 0 END) AS count_nm7,MAX(CASE end_tmp_field WHEN '6-7' THEN users_nm ELSE 0 END) AS users_nm7,MAX(CASE end_tmp_field WHEN '6-7' THEN dis_users_nm ELSE 0 END) AS dis_users_nm7,MAX(CASE end_tmp_field WHEN '7-8' THEN count_nm ELSE 0 END) AS count_nm8,MAX(CASE end_tmp_field WHEN '7-8' THEN users_nm ELSE 0 END) AS users_nm8,MAX(CASE end_tmp_field WHEN '7-8' THEN dis_users_nm ELSE 0 END) AS dis_users_nm8,MAX(CASE end_tmp_field WHEN '8-9' THEN count_nm ELSE 0 END) AS count_nm9,MAX(CASE end_tmp_field WHEN '8-9' THEN users_nm ELSE 0 END) AS users_nm9,MAX(CASE end_tmp_field WHEN '8-9' THEN dis_users_nm ELSE 0 END) AS dis_users_nm9,MAX(CASE end_tmp_field WHEN '9-10' THEN count_nm ELSE 0 END) AS count_nm10,MAX(CASE end_tmp_field WHEN '9-10' THEN users_nm ELSE 0 END) AS users_nm10,MAX(CASE end_tmp_field WHEN '9-10' THEN dis_users_nm ELSE 0 END) AS dis_users_nm10,MAX(CASE end_tmp_field WHEN '10-11' THEN count_nm ELSE 0 END) AS count_nm11,MAX(CASE end_tmp_field WHEN '10-11' THEN users_nm ELSE 0 END) AS users_nm11,MAX(CASE end_tmp_field WHEN '10-11' THEN dis_users_nm ELSE 0 END) AS dis_users_nm11,MAX(CASE end_tmp_field WHEN '11-12' THEN count_nm ELSE 0 END) AS count_nm12,MAX(CASE end_tmp_field WHEN '11-12' THEN users_nm ELSE 0 END) AS users_nm12,MAX(CASE end_tmp_field WHEN '11-12' THEN dis_users_nm ELSE 0 END) AS dis_users_nm12,MAX(CASE end_tmp_field WHEN '12-13' THEN count_nm ELSE 0 END) AS count_nm13,MAX(CASE end_tmp_field WHEN '12-13' THEN users_nm ELSE 0 END) AS users_nm13,MAX(CASE end_tmp_field WHEN '12-13' THEN dis_users_nm ELSE 0 END) AS dis_users_nm13,MAX(CASE end_tmp_field WHEN '13-14' THEN count_nm ELSE 0 END) AS count_nm14,MAX(CASE end_tmp_field WHEN '13-14' THEN users_nm ELSE 0 END) AS users_nm14,MAX(CASE end_tmp_field WHEN '13-14' THEN dis_users_nm ELSE 0 END) AS dis_users_nm14,MAX(CASE end_tmp_field WHEN '14-15' THEN count_nm ELSE 0 END) AS count_nm15,MAX(CASE end_tmp_field WHEN '14-15' THEN users_nm ELSE 0 END) AS users_nm15,MAX(CASE end_tmp_field WHEN '14-15' THEN dis_users_nm ELSE 0 END) AS dis_users_nm15,MAX(CASE end_tmp_field WHEN '15-16' THEN count_nm ELSE 0 END) AS count_nm16,MAX(CASE end_tmp_field WHEN '15-16' THEN users_nm ELSE 0 END) AS users_nm16,MAX(CASE end_tmp_field WHEN '15-16' THEN dis_users_nm ELSE 0 END) AS dis_users_nm16,MAX(CASE end_tmp_field WHEN '16-17' THEN count_nm ELSE 0 END) AS count_nm17,MAX(CASE end_tmp_field WHEN '16-17' THEN users_nm ELSE 0 END) AS users_nm17,MAX(CASE end_tmp_field WHEN '16-17' THEN dis_users_nm ELSE 0 END) AS dis_users_nm17,MAX(CASE end_tmp_field WHEN '17-18' THEN count_nm ELSE 0 END) AS count_nm18,MAX(CASE end_tmp_field WHEN '17-18' THEN users_nm ELSE 0 END) AS users_nm18,MAX(CASE end_tmp_field WHEN '17-18' THEN dis_users_nm ELSE 0 END) AS dis_users_nm18,MAX(CASE end_tmp_field WHEN '18-19' THEN count_nm ELSE 0 END) AS count_nm19,MAX(CASE end_tmp_field WHEN '18-19' THEN users_nm ELSE 0 END) AS users_nm19,MAX(CASE end_tmp_field WHEN '18-19' THEN dis_users_nm ELSE 0 END) AS dis_users_nm19,MAX(CASE end_tmp_field WHEN '19-20' THEN count_nm ELSE 0 END) AS count_nm20,MAX(CASE end_tmp_field WHEN '19-20' THEN users_nm ELSE 0 END) AS users_nm20,MAX(CASE end_tmp_field WHEN '19-20' THEN dis_users_nm ELSE 0 END) AS dis_users_nm20,MAX(CASE end_tmp_field WHEN '20-21' THEN count_nm ELSE 0 END) AS count_nm21,MAX(CASE end_tmp_field WHEN '20-21' THEN users_nm ELSE 0 END) AS users_nm21,MAX(CASE end_tmp_field WHEN '20-21' THEN dis_users_nm ELSE 0 END) AS dis_users_nm21,MAX(CASE end_tmp_field WHEN '21-22' THEN count_nm ELSE 0 END) AS count_nm22,MAX(CASE end_tmp_field WHEN '21-22' THEN users_nm ELSE 0 END) AS users_nm22,MAX(CASE end_tmp_field WHEN '21-22' THEN dis_users_nm ELSE 0 END) AS dis_users_nm22,MAX(CASE end_tmp_field WHEN '22-23' THEN count_nm ELSE 0 END) AS count_nm23,MAX(CASE end_tmp_field WHEN '22-23' THEN users_nm ELSE 0 END) AS users_nm23,MAX(CASE end_tmp_field WHEN '22-23' THEN dis_users_nm ELSE 0 END) AS dis_users_nm23,MAX(CASE end_tmp_field WHEN '23-24' THEN count_nm ELSE 0 END) AS count_nm24,MAX(CASE end_tmp_field WHEN '23-24' THEN users_nm ELSE 0 END) AS users_nm24,MAX(CASE end_tmp_field WHEN '23-24' THEN dis_users_nm ELSE 0 END) AS dis_users_nm24,MAX(CASE end_tmp_field WHEN '24-48' THEN count_nm ELSE 0 END) AS count_nm25,MAX(CASE end_tmp_field WHEN '24-48' THEN users_nm ELSE 0 END) AS users_nm25,MAX(CASE end_tmp_field WHEN '24-48' THEN dis_users_nm ELSE 0 END) AS dis_users_nm25,MAX(CASE end_tmp_field WHEN '>=48' THEN count_nm ELSE 0 END) AS count_nm26,MAX(CASE end_tmp_field WHEN '>=48' THEN users_nm ELSE 0 END) AS users_nm26,MAX(CASE end_tmp_field WHEN '>=48' THEN dis_users_nm ELSE 0 END) AS dis_users_nm26 FROM ("
				+ " SELECT*FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_od_rangetime_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_24' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) tmp UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) AS prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_od_rangetime_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_24' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_rangetime_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_24' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}')) tmp) t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_24' GROUP BY t.dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source) tmp WHERE 1=1 ";
		return sql;
	}

	@Override
	public String CrossDomainCityDistance(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',tmp_count,count_nm1 '区间为0-100的记录数',dis_users_nm1 '区间为0-100的用户数',count_nm2 '区间为100-200的记录数',dis_users_nm2 '区间为100-200的用户数',count_nm3 '区间为200-300的记录数',dis_users_nm3 '区间为200-300的用户数',count_nm4 '区间为300-400的记录数',dis_users_nm4 '区间为300-400的用户数',count_nm5 '区间为400-500的记录数',dis_users_nm5 '区间为400-500的用户数',count_nm6 '区间为500-600的记录数',dis_users_nm6 '区间为500-600的用户数',count_nm7 '区间为600-700的记录数',dis_users_nm7 '区间为600-700的用户数',count_nm8 '区间为700-800的记录数',dis_users_nm8 '区间为700-800的用户数',count_nm9 '区间为800-900的记录数',dis_users_nm9 '区间为800-900的用户数',count_nm10 '区间为900-1000的记录数',dis_users_nm10 '区间为900-1000的用户数',count_nm11 '区间为1000-2000的记录数',dis_users_nm11 '区间为1000-2000的用户数',count_nm12 '区间大于2000的记录数',dis_users_nm12 '区间大于2000的用户数' FROM ("
				+ " SELECT t.dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source,t.tmp_count,MAX(CASE end_tmp_field WHEN '0-100' THEN count_nm ELSE 0 END) count_nm1,MAX(CASE end_tmp_field WHEN '0-100' THEN users_nm ELSE 0 END) users_nm1,MAX(CASE end_tmp_field WHEN '0-100' THEN dis_users_nm ELSE 0 END) dis_users_nm1,MAX(CASE end_tmp_field WHEN '100-200' THEN count_nm ELSE 0 END) count_nm2,MAX(CASE end_tmp_field WHEN '100-200' THEN users_nm ELSE 0 END) users_nm2,MAX(CASE end_tmp_field WHEN '100-200' THEN dis_users_nm ELSE 0 END) dis_users_nm2,MAX(CASE end_tmp_field WHEN '200-300' THEN count_nm ELSE 0 END) count_nm3,MAX(CASE end_tmp_field WHEN '200-300' THEN users_nm ELSE 0 END) users_nm3,MAX(CASE end_tmp_field WHEN '200-300' THEN dis_users_nm ELSE 0 END) dis_users_nm3,MAX(CASE end_tmp_field WHEN '300-400' THEN count_nm ELSE 0 END) count_nm4,MAX(CASE end_tmp_field WHEN '300-400' THEN users_nm ELSE 0 END) users_nm4,MAX(CASE end_tmp_field WHEN '300-400' THEN dis_users_nm ELSE 0 END) dis_users_nm4,MAX(CASE end_tmp_field WHEN '400-500' THEN count_nm ELSE 0 END) count_nm5,MAX(CASE end_tmp_field WHEN '400-500' THEN users_nm ELSE 0 END) users_nm5,MAX(CASE end_tmp_field WHEN '400-500' THEN dis_users_nm ELSE 0 END) dis_users_nm5,MAX(CASE end_tmp_field WHEN '500-600' THEN count_nm ELSE 0 END) count_nm6,MAX(CASE end_tmp_field WHEN '500-600' THEN users_nm ELSE 0 END) users_nm6,MAX(CASE end_tmp_field WHEN '500-600' THEN dis_users_nm ELSE 0 END) dis_users_nm6,MAX(CASE end_tmp_field WHEN '600-700' THEN count_nm ELSE 0 END) count_nm7,MAX(CASE end_tmp_field WHEN '600-700' THEN users_nm ELSE 0 END) users_nm7,MAX(CASE end_tmp_field WHEN '600-700' THEN dis_users_nm ELSE 0 END) dis_users_nm7,MAX(CASE end_tmp_field WHEN '700-800' THEN count_nm ELSE 0 END) count_nm8,MAX(CASE end_tmp_field WHEN '700-800' THEN users_nm ELSE 0 END) users_nm8,MAX(CASE end_tmp_field WHEN '700-800' THEN dis_users_nm ELSE 0 END) dis_users_nm8,MAX(CASE end_tmp_field WHEN '800-900' THEN count_nm ELSE 0 END) count_nm9,MAX(CASE end_tmp_field WHEN '800-900' THEN users_nm ELSE 0 END) users_nm9,MAX(CASE end_tmp_field WHEN '800-900' THEN dis_users_nm ELSE 0 END) dis_users_nm9,MAX(CASE end_tmp_field WHEN '900-1000' THEN count_nm ELSE 0 END) count_nm10,MAX(CASE end_tmp_field WHEN '900-1000' THEN users_nm ELSE 0 END) users_nm10,MAX(CASE end_tmp_field WHEN '900-1000' THEN dis_users_nm ELSE 0 END) dis_users_nm10,MAX(CASE end_tmp_field WHEN '1000-2000' THEN count_nm ELSE 0 END) count_nm11,MAX(CASE end_tmp_field WHEN '1000-2000' THEN users_nm ELSE 0 END) users_nm11,MAX(CASE end_tmp_field WHEN '1000-2000' THEN dis_users_nm ELSE 0 END) dis_users_nm11,MAX(CASE end_tmp_field WHEN '>=2000' THEN count_nm ELSE 0 END) count_nm12,MAX(CASE end_tmp_field WHEN '>=2000' THEN users_nm ELSE 0 END) users_nm12,MAX(CASE end_tmp_field WHEN '>=2000' THEN dis_users_nm ELSE 0 END) dis_users_nm12 FROM ("
				+ " SELECT*FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_distance_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.sd_date>='${star_dts}' AND t.sd_date<='${end_dts}' AND t.kpi_id='2_0_25' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) AS tmp UNION ALL ("
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_distance_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_25' AND t.sd_date>='${star_dts}' AND t.sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) UNION ALL ("
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_distance_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_25' AND t.sd_date>='${star_dts}' AND t.sd_date<='${end_dts}')) tmp WHERE dt>='${star_dts}' AND dt<='${end_dts}') t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_25' GROUP BY t.dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source) tmp WHERE 1=1 ";
			return sql;
	}

	@Override
	public String CrossDomainCityOut(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,'no' end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_out_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_26' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,tmp_count) AS tmp UNION ALL ("
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_out_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_26')) tmp WHERE dt>='${star_dts}' AND dt<='${end_dts}'";
			return sql;
	}

	@Override
	public String CrossDomainCityIn(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,'no' end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_in_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_27' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,tmp_count) AS tmp1 UNION ALL ("
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,'no' end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_in_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_27')) tmp WHERE dt>='${star_dts}' AND dt<='${end_dts}'";
			return sql;
	}

	@Override
	public String CrossDomainCityXor(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_od_xor_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_28' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) tmp UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_od_xor_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_28' GROUP BY sd_date,prov_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_xor_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_28')) tmp WHERE dt>='${star_dts}' AND dt<='${end_dts}'";
			return sql;
	}

	@Override
	public String CrossDomainCityRange(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = " SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',end_tmp_field,`dis_users_nm其他-其他` '其他-其他用户数',`dis_users_nm其他-飞机` '其他-飞机用户数',`dis_users_nm其他-高铁` '其他-高铁用户数',`dis_users_nm其他-火车` '其他-火车用户数',`dis_users_nm飞机-其他` '飞机-其他用户数',`dis_users_nm飞机-飞机` '飞机-飞机用户数',`dis_users_nm飞机-高铁` '飞机-高铁用户数',`dis_users_nm飞机-火车` '飞机-火车用户数',`dis_users_nm高铁-其他` '高铁-其他用户数',`dis_users_nm高铁-飞机` '高铁-飞机用户数',`dis_users_nm高铁-高铁` '高铁-高铁用户数',`dis_users_nm高铁-火车` '高铁-火车用户数',`dis_users_nm火车-其他` '火车-其他用户数',`dis_users_nm火车-飞机` '火车-飞机用户数',`dis_users_nm火车-高铁` '火车-高铁用户数',`dis_users_nm火车-火车` '火车-火车用户数' FROM ("
				+ " SELECT dt,prov_id,city_id,hive_table_name,kpi_id,end_tmp_field,MAX(CASE od_means WHEN '0-0' THEN dis_users_nm ELSE 0 END) 'dis_users_nm其他-其他',MAX(CASE od_means WHEN '0-1' THEN dis_users_nm ELSE 0 END) 'dis_users_nm其他-飞机',MAX(CASE od_means WHEN '0-2' THEN dis_users_nm ELSE 0 END) 'dis_users_nm其他-高铁',MAX(CASE od_means WHEN '0-3' THEN dis_users_nm ELSE 0 END) 'dis_users_nm其他-火车',MAX(CASE od_means WHEN '1-0' THEN dis_users_nm ELSE 0 END) 'dis_users_nm飞机-其他',MAX(CASE od_means WHEN '1-1' THEN dis_users_nm ELSE 0 END) 'dis_users_nm飞机-飞机',MAX(CASE od_means WHEN '1-2' THEN dis_users_nm ELSE 0 END) 'dis_users_nm飞机-高铁',MAX(CASE od_means WHEN '1-3' THEN dis_users_nm ELSE 0 END) 'dis_users_nm飞机-火车',MAX(CASE od_means WHEN '2-0' THEN dis_users_nm ELSE 0 END) 'dis_users_nm高铁-其他',MAX(CASE od_means WHEN '2-1' THEN dis_users_nm ELSE 0 END) 'dis_users_nm高铁-飞机',MAX(CASE od_means WHEN '2-2' THEN dis_users_nm ELSE 0 END) 'dis_users_nm高铁-高铁',MAX(CASE od_means WHEN '2-3' THEN dis_users_nm ELSE 0 END) 'dis_users_nm高铁-火车',MAX(CASE od_means WHEN '3-0' THEN dis_users_nm ELSE 0 END) 'dis_users_nm火车-其他',MAX(CASE od_means WHEN '3-1' THEN dis_users_nm ELSE 0 END) 'dis_users_nm火车-飞机',MAX(CASE od_means WHEN '3-2' THEN dis_users_nm ELSE 0 END) 'dis_users_nm火车-高铁',MAX(CASE od_means WHEN '3-3' THEN dis_users_nm ELSE 0 END) 'dis_users_nm火车-火车' FROM ("
				+ " SELECT t.dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source,substring_index(t.end_tmp_field,'_',1) od_means,substring_index(t.end_tmp_field,'_',-1) end_tmp_field,t.tmp_count,t.count_nm,t.users_nm,t.dis_users_nm FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_range_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_30' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) AS tmp UNION ALL ("
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_range_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_30' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) UNION ALL ("
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_range_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_city_od_msk_d' AND t.kpi_id='2_0_30')) t WHERE dt>='${star_dts}' AND dt<='${end_dts}') tmp GROUP BY dt,prov_id,city_id,hive_table_name,kpi_id,end_tmp_field) tmp WHERE 1=1 ";
				return sql;
	}

	@Override
	public String CrossDomaiCityMsk_d(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',count_nm '记录数',count_nm_volatility '记录数波动率',dis_users_nm '用户数',dis_users_nm_volatility '用户数波率' FROM ("
				+ " SELECT t.sd_date dt,CASE WHEN city_id='allpv' THEN 'allpv' WHEN char_length(city_id)=3 THEN city_id ELSE substring(city_id,1,3) END 'prov_id',CASE WHEN city_id='allpv' THEN 'allct' WHEN char_length(city_id)=3 THEN 'allct' ELSE city_id END 'city_id',t.count_nm,t.count_nm_volatility,t.users_nm,t.users_nm_volatility,t.dis_users_nm,t.dis_users_nm_volatility FROM auto_user_citysource_rate_volatility t "
				+ " WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_8' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}') tmp WHERE 1=1 ";
				return sql;
	}

	@Override
	public String CrossDomainCityMsk_D_Rangetime(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',tmp_count,count_nm1 '区间为0-1记录数',dis_users_nm1 '区间为0-1用户数',count_nm2 '区间为1-2记录数',dis_users_nm2 '区间为1-2用户数',count_nm3 '区间为2-3记录数',dis_users_nm3 '区间为2-3用户数',count_nm4 '区间为3-4记录数',dis_users_nm4 '区间为3-4用户数',count_nm4 '区间为4-5记录数',dis_users_nm3 '区间为4-5用户数',count_nm6 '区间为5-6记录数',dis_users_nm6 '区间为5-6用户数',count_nm5 '区间为6-7记录数',dis_users_nm5 '区间为6-7用户数',count_nm8 '区间为7-8记录数',dis_users_nm8 '区间为7-8用户数',count_nm7 '区间为8-9记录数',dis_users_nm7 '区间为8-9用户数',count_nm10 '区间为9-10记录数',dis_users_nm10 '区间为9-10用户数',count_nm9 '区间为10-11记录数',dis_users_nm9 '区间为10-11用户数',count_nm12 '区间为11-12记录数',dis_users_nm12 '区间为11-12用户数',count_nm11 '区间为12-13记录数',dis_users_nm11 '区间为12-13用户数',count_nm14 '区间为13-14记录数',dis_users_nm14 '区间为13-14用户数',count_nm13 '区间为14-15记录数',dis_users_nm13 '区间为14-15用户数',count_nm16 '区间为15-16记录数',dis_users_nm16 '区间为15-16用户数',count_nm15 '区间为16-17记录数',dis_users_nm15 '区间为16-17用户数',count_nm18 '区间为17-18记录数',dis_users_nm18 '区间为17-18用户数',count_nm17 '区间为18-19记录数',dis_users_nm17 '区间为18-19用户数',count_nm20 '区间为19-20记录数',dis_users_nm20 '区间为19-20用户数',count_nm19 '区间为20-21记录数',dis_users_nm19 '区间为20-21用户数',count_nm22 '区间为21-22记录数',dis_users_nm22 '区间为21-22用户数',count_nm21 '区间为22-23记录数',dis_users_nm21 '区间为22-23用户数',count_nm24 '区间为23-24记录数',dis_users_nm24 '区间为23-24用户数',count_nm23 '区间为24-48记录数',dis_users_nm23 '区间为24-48用户数',count_nm26 '区间大于48记录数',dis_users_nm26 '区间大于48用户数' FROM ("
				+ " SELECT dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source,t.tmp_count,MAX(CASE end_tmp_field WHEN '0-1' THEN count_nm ELSE 0 END) AS count_nm1,MAX(CASE end_tmp_field WHEN '0-1' THEN users_nm ELSE 0 END) AS users_nm1,MAX(CASE end_tmp_field WHEN '0-1' THEN dis_users_nm ELSE 0 END) AS dis_users_nm1,MAX(CASE end_tmp_field WHEN '1-2' THEN count_nm ELSE 0 END) AS count_nm2,MAX(CASE end_tmp_field WHEN '1-2' THEN users_nm ELSE 0 END) AS users_nm2,MAX(CASE end_tmp_field WHEN '1-2' THEN dis_users_nm ELSE 0 END) AS dis_users_nm2,MAX(CASE end_tmp_field WHEN '2-3' THEN count_nm ELSE 0 END) AS count_nm3,MAX(CASE end_tmp_field WHEN '2-3' THEN users_nm ELSE 0 END) AS users_nm3,MAX(CASE end_tmp_field WHEN '2-3' THEN dis_users_nm ELSE 0 END) AS dis_users_nm3,MAX(CASE end_tmp_field WHEN '3-4' THEN count_nm ELSE 0 END) AS count_nm4,MAX(CASE end_tmp_field WHEN '3-4' THEN users_nm ELSE 0 END) AS users_nm4,MAX(CASE end_tmp_field WHEN '3-4' THEN dis_users_nm ELSE 0 END) AS dis_users_nm4,MAX(CASE end_tmp_field WHEN '4-5' THEN count_nm ELSE 0 END) AS count_nm5,MAX(CASE end_tmp_field WHEN '4-5' THEN users_nm ELSE 0 END) AS users_nm5,MAX(CASE end_tmp_field WHEN '4-5' THEN dis_users_nm ELSE 0 END) AS dis_users_nm5,MAX(CASE end_tmp_field WHEN '5-6' THEN count_nm ELSE 0 END) AS count_nm6,MAX(CASE end_tmp_field WHEN '5-6' THEN users_nm ELSE 0 END) AS users_nm6,MAX(CASE end_tmp_field WHEN '5-6' THEN dis_users_nm ELSE 0 END) AS dis_users_nm6,MAX(CASE end_tmp_field WHEN '6-7' THEN count_nm ELSE 0 END) AS count_nm7,MAX(CASE end_tmp_field WHEN '6-7' THEN users_nm ELSE 0 END) AS users_nm7,MAX(CASE end_tmp_field WHEN '6-7' THEN dis_users_nm ELSE 0 END) AS dis_users_nm7,MAX(CASE end_tmp_field WHEN '7-8' THEN count_nm ELSE 0 END) AS count_nm8,MAX(CASE end_tmp_field WHEN '7-8' THEN users_nm ELSE 0 END) AS users_nm8,MAX(CASE end_tmp_field WHEN '7-8' THEN dis_users_nm ELSE 0 END) AS dis_users_nm8,MAX(CASE end_tmp_field WHEN '8-9' THEN count_nm ELSE 0 END) AS count_nm9,MAX(CASE end_tmp_field WHEN '8-9' THEN users_nm ELSE 0 END) AS users_nm9,MAX(CASE end_tmp_field WHEN '8-9' THEN dis_users_nm ELSE 0 END) AS dis_users_nm9,MAX(CASE end_tmp_field WHEN '9-10' THEN count_nm ELSE 0 END) AS count_nm10,MAX(CASE end_tmp_field WHEN '9-10' THEN users_nm ELSE 0 END) AS users_nm10,MAX(CASE end_tmp_field WHEN '9-10' THEN dis_users_nm ELSE 0 END) AS dis_users_nm10,MAX(CASE end_tmp_field WHEN '10-11' THEN count_nm ELSE 0 END) AS count_nm11,MAX(CASE end_tmp_field WHEN '10-11' THEN users_nm ELSE 0 END) AS users_nm11,MAX(CASE end_tmp_field WHEN '10-11' THEN dis_users_nm ELSE 0 END) AS dis_users_nm11,MAX(CASE end_tmp_field WHEN '11-12' THEN count_nm ELSE 0 END) AS count_nm12,MAX(CASE end_tmp_field WHEN '11-12' THEN users_nm ELSE 0 END) AS users_nm12,MAX(CASE end_tmp_field WHEN '11-12' THEN dis_users_nm ELSE 0 END) AS dis_users_nm12,MAX(CASE end_tmp_field WHEN '12-13' THEN count_nm ELSE 0 END) AS count_nm13,MAX(CASE end_tmp_field WHEN '12-13' THEN users_nm ELSE 0 END) AS users_nm13,MAX(CASE end_tmp_field WHEN '12-13' THEN dis_users_nm ELSE 0 END) AS dis_users_nm13,MAX(CASE end_tmp_field WHEN '13-14' THEN count_nm ELSE 0 END) AS count_nm14,MAX(CASE end_tmp_field WHEN '13-14' THEN users_nm ELSE 0 END) AS users_nm14,MAX(CASE end_tmp_field WHEN '13-14' THEN dis_users_nm ELSE 0 END) AS dis_users_nm14,MAX(CASE end_tmp_field WHEN '14-15' THEN count_nm ELSE 0 END) AS count_nm15,MAX(CASE end_tmp_field WHEN '14-15' THEN users_nm ELSE 0 END) AS users_nm15,MAX(CASE end_tmp_field WHEN '14-15' THEN dis_users_nm ELSE 0 END) AS dis_users_nm15,MAX(CASE end_tmp_field WHEN '15-16' THEN count_nm ELSE 0 END) AS count_nm16,MAX(CASE end_tmp_field WHEN '15-16' THEN users_nm ELSE 0 END) AS users_nm16,MAX(CASE end_tmp_field WHEN '15-16' THEN dis_users_nm ELSE 0 END) AS dis_users_nm16,MAX(CASE end_tmp_field WHEN '16-17' THEN count_nm ELSE 0 END) AS count_nm17,MAX(CASE end_tmp_field WHEN '16-17' THEN users_nm ELSE 0 END) AS users_nm17,MAX(CASE end_tmp_field WHEN '16-17' THEN dis_users_nm ELSE 0 END) AS dis_users_nm17,MAX(CASE end_tmp_field WHEN '17-18' THEN count_nm ELSE 0 END) AS count_nm18,MAX(CASE end_tmp_field WHEN '17-18' THEN users_nm ELSE 0 END) AS users_nm18,MAX(CASE end_tmp_field WHEN '17-18' THEN dis_users_nm ELSE 0 END) AS dis_users_nm18,MAX(CASE end_tmp_field WHEN '18-19' THEN count_nm ELSE 0 END) AS count_nm19,MAX(CASE end_tmp_field WHEN '18-19' THEN users_nm ELSE 0 END) AS users_nm19,MAX(CASE end_tmp_field WHEN '18-19' THEN dis_users_nm ELSE 0 END) AS dis_users_nm19,MAX(CASE end_tmp_field WHEN '19-20' THEN count_nm ELSE 0 END) AS count_nm20,MAX(CASE end_tmp_field WHEN '19-20' THEN users_nm ELSE 0 END) AS users_nm20,MAX(CASE end_tmp_field WHEN '19-20' THEN dis_users_nm ELSE 0 END) AS dis_users_nm20,MAX(CASE end_tmp_field WHEN '20-21' THEN count_nm ELSE 0 END) AS count_nm21,MAX(CASE end_tmp_field WHEN '20-21' THEN users_nm ELSE 0 END) AS users_nm21,MAX(CASE end_tmp_field WHEN '20-21' THEN dis_users_nm ELSE 0 END) AS dis_users_nm21,MAX(CASE end_tmp_field WHEN '21-22' THEN count_nm ELSE 0 END) AS count_nm22,MAX(CASE end_tmp_field WHEN '21-22' THEN users_nm ELSE 0 END) AS users_nm22,MAX(CASE end_tmp_field WHEN '21-22' THEN dis_users_nm ELSE 0 END) AS dis_users_nm22,MAX(CASE end_tmp_field WHEN '22-23' THEN count_nm ELSE 0 END) AS count_nm23,MAX(CASE end_tmp_field WHEN '22-23' THEN users_nm ELSE 0 END) AS users_nm23,MAX(CASE end_tmp_field WHEN '22-23' THEN dis_users_nm ELSE 0 END) AS dis_users_nm23,MAX(CASE end_tmp_field WHEN '23-24' THEN count_nm ELSE 0 END) AS count_nm24,MAX(CASE end_tmp_field WHEN '23-24' THEN users_nm ELSE 0 END) AS users_nm24,MAX(CASE end_tmp_field WHEN '23-24' THEN dis_users_nm ELSE 0 END) AS dis_users_nm24,MAX(CASE end_tmp_field WHEN '24-48' THEN count_nm ELSE 0 END) AS count_nm25,MAX(CASE end_tmp_field WHEN '24-48' THEN users_nm ELSE 0 END) AS users_nm25,MAX(CASE end_tmp_field WHEN '24-48' THEN dis_users_nm ELSE 0 END) AS dis_users_nm25,MAX(CASE end_tmp_field WHEN '>=48' THEN count_nm ELSE 0 END) AS count_nm26,MAX(CASE end_tmp_field WHEN '>=48' THEN users_nm ELSE 0 END) AS users_nm26,MAX(CASE end_tmp_field WHEN '>=48' THEN dis_users_nm ELSE 0 END) AS dis_users_nm26 FROM ("
				+ " SELECT*FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_od_rangetime_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_24' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) tmp UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_od_rangetime_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_24' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,prov_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_rangetime_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_24' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}')) tmp) t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_24' GROUP BY t.dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source) tmp WHERE 1=1 ";
				return sql;
	}

	@Override
	public String CrossDomainCityMsk_D_Distance(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',tmp_count,count_nm1 '区间为0-100记录数',dis_users_nm1 '区间为0-100用户数',count_nm2 '区间为100-200记录数',dis_users_nm2 '区间为100-200用户数',count_nm3 '区间为200-300记录数',dis_users_nm3 '区间为200-300用户数',count_nm4 '区间为300-400记录数',dis_users_nm4 '区间为300-400用户数',count_nm5 '区间为400-500记录数',dis_users_nm5 '区间为400-500用户数',count_nm6 '区间为500-600记录数',dis_users_nm6 '区间为500-600用户数',count_nm7 '区间为600-700记录数',dis_users_nm7 '区间为600-700用户数',count_nm8 '区间为700-800记录数',dis_users_nm8 '区间为700-800用户数',count_nm9 '区间为800-900记录数',dis_users_nm9 '区间为800-900用户数',count_nm10 '区间为900-1000记录数',dis_users_nm10 '区间为900-1000用户数',count_nm11 '区间为1000-2000记录数',dis_users_nm11 '区间为1000-2000用户数',count_nm12 '区间大于2000记录数',dis_users_nm12 '区间大于2000用户数' FROM ("
				+ " SELECT t.dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source,t.tmp_count,MAX(CASE end_tmp_field WHEN '0-100' THEN count_nm ELSE 0 END) count_nm1,MAX(CASE end_tmp_field WHEN '0-100' THEN users_nm ELSE 0 END) users_nm1,MAX(CASE end_tmp_field WHEN '0-100' THEN dis_users_nm ELSE 0 END) dis_users_nm1,MAX(CASE end_tmp_field WHEN '100-200' THEN count_nm ELSE 0 END) count_nm2,MAX(CASE end_tmp_field WHEN '100-200' THEN users_nm ELSE 0 END) users_nm2,MAX(CASE end_tmp_field WHEN '100-200' THEN dis_users_nm ELSE 0 END) dis_users_nm2,MAX(CASE end_tmp_field WHEN '200-300' THEN count_nm ELSE 0 END) count_nm3,MAX(CASE end_tmp_field WHEN '200-300' THEN users_nm ELSE 0 END) users_nm3,MAX(CASE end_tmp_field WHEN '200-300' THEN dis_users_nm ELSE 0 END) dis_users_nm3,MAX(CASE end_tmp_field WHEN '300-400' THEN count_nm ELSE 0 END) count_nm4,MAX(CASE end_tmp_field WHEN '300-400' THEN users_nm ELSE 0 END) users_nm4,MAX(CASE end_tmp_field WHEN '300-400' THEN dis_users_nm ELSE 0 END) dis_users_nm4,MAX(CASE end_tmp_field WHEN '400-500' THEN count_nm ELSE 0 END) count_nm5,MAX(CASE end_tmp_field WHEN '400-500' THEN users_nm ELSE 0 END) users_nm5,MAX(CASE end_tmp_field WHEN '400-500' THEN dis_users_nm ELSE 0 END) dis_users_nm5,MAX(CASE end_tmp_field WHEN '500-600' THEN count_nm ELSE 0 END) count_nm6,MAX(CASE end_tmp_field WHEN '500-600' THEN users_nm ELSE 0 END) users_nm6,MAX(CASE end_tmp_field WHEN '500-600' THEN dis_users_nm ELSE 0 END) dis_users_nm6,MAX(CASE end_tmp_field WHEN '600-700' THEN count_nm ELSE 0 END) count_nm7,MAX(CASE end_tmp_field WHEN '600-700' THEN users_nm ELSE 0 END) users_nm7,MAX(CASE end_tmp_field WHEN '600-700' THEN dis_users_nm ELSE 0 END) dis_users_nm7,MAX(CASE end_tmp_field WHEN '700-800' THEN count_nm ELSE 0 END) count_nm8,MAX(CASE end_tmp_field WHEN '700-800' THEN users_nm ELSE 0 END) users_nm8,MAX(CASE end_tmp_field WHEN '700-800' THEN dis_users_nm ELSE 0 END) dis_users_nm8,MAX(CASE end_tmp_field WHEN '800-900' THEN count_nm ELSE 0 END) count_nm9,MAX(CASE end_tmp_field WHEN '800-900' THEN users_nm ELSE 0 END) users_nm9,MAX(CASE end_tmp_field WHEN '800-900' THEN dis_users_nm ELSE 0 END) dis_users_nm9,MAX(CASE end_tmp_field WHEN '900-1000' THEN count_nm ELSE 0 END) count_nm10,MAX(CASE end_tmp_field WHEN '900-1000' THEN users_nm ELSE 0 END) users_nm10,MAX(CASE end_tmp_field WHEN '900-1000' THEN dis_users_nm ELSE 0 END) dis_users_nm10,MAX(CASE end_tmp_field WHEN '1000-2000' THEN count_nm ELSE 0 END) count_nm11,MAX(CASE end_tmp_field WHEN '1000-2000' THEN users_nm ELSE 0 END) users_nm11,MAX(CASE end_tmp_field WHEN '1000-2000' THEN dis_users_nm ELSE 0 END) dis_users_nm11,MAX(CASE end_tmp_field WHEN '>=2000' THEN count_nm ELSE 0 END) count_nm12,MAX(CASE end_tmp_field WHEN '>=2000' THEN users_nm ELSE 0 END) users_nm12,MAX(CASE end_tmp_field WHEN '>=2000' THEN dis_users_nm ELSE 0 END) dis_users_nm12 FROM ("
				+ " SELECT*FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,'allpv' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_distance_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.kpi_id='2_0_25' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) AS tmp UNION ALL ("
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_distance_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_25' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) UNION ALL ("
				+ " SELECT sd_date dt,substr(city_id,1,3),substr(city_id,1,5),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_distance_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.kpi_id='2_0_25')) tmp WHERE dt>='${star_dts}' AND dt<='${end_dts}') t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_25' GROUP BY t.dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source) tmp WHERE 1=1 ";
				return sql;
	}

	@Override
	public String CrossDomainCityMsk_D_Out(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,'no' AS end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_od_out_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_26' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,tmp_count) tmp UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_out_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_26') UNION ALL ("
				+ " SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,'no' AS end_tmp_field,sum(tmp_count),SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_od_out_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_26' GROUP BY sd_date,hive_table_name,kpi_id,data_source,tmp_count)) tmp WHERE 1=1";
				return sql;
	}

	@Override
	public String CrossDomainCityMsk_D_In(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub

FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,'no' end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_in_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_27' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,tmp_count UNION ALL "
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,'no' end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_in_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_27' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,tmp_count UNION ALL "
				+ " SELECT sd_date dt,substr(city_id,1,3),substr(city_id,1,5),hive_table_name,kpi_id,data_source,'no' end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_in_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_27') AS tmp WHERE dt>='${star_dts}' AND dt<='${end_dts}'";
			return sql;
	}

	@Override
	public String CrossDomainCityMsk_D_xor(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',count_nm '记录数',dis_users_nm '用户数' FROM ("
				+" SELECT*FROM ("
				+" SELECT sd_date dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_xor_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_28' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) AS tmp UNION ALL ("
				+" SELECT sd_date dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_od_xor_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_28' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) UNION ALL ("
				+" SELECT sd_date dt,substr(city_id,1,3),substr(city_id,1,5),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_od_xor_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_county_od_msk_d' AND t.kpi_id='2_0_28' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}')) tmp WHERE 1=1";
			return sql;
	}

	@Override
	public String CrossDomainHistoryAccessCityMsk_D(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = " SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',count_nm '记录数',dis_users_nm '用户数',count_nm_rate '记录数占比',dis_users_nm_rate '用户数占比' FROM ("
				+" SELECT*FROM ("
				+" SELECT a.dts dt,'allpv' prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,a.end_tmp_field,count_nm,users_nm,dis_users_nm,round(count_nm/count_nm_sum*100,2) count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,2) dis_users_nm_rate FROM ("
				+" SELECT sd_date dts,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.end_tmp_field='1' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field) a INNER JOIN ("
				+" SELECT sd_date dts,'allct' city_id,hive_table_name,data_source,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.end_tmp_field='1' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,data_source,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id AND a.data_source=b.data_source WHERE 1=1 AND a.end_tmp_field='1') tmp UNION ALL ("
				+" SELECT a.dts dt,a.prov_id,'allct' city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,2) count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,2) dis_users_nm_rate FROM ("
				+" SELECT sd_date dts,substr(city_id,1,3) prov_id,hive_table_name,kpi_id,data_source,end_tmp_field end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.end_tmp_field='1' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),end_tmp_field,hive_table_name,kpi_id,data_source) a INNER JOIN ("
				+" SELECT sd_date dts,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.end_tmp_field='1' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE 1=1 AND a.kpi_id='2_0_31' AND a.end_tmp_field='1') UNION ALL ("
				+" SELECT a.dts dt,a.prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,2) count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,2) dis_users_nm_rate FROM ("
				+" SELECT sd_date dts,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,5),end_tmp_field,hive_table_name,kpi_id,data_source) a INNER JOIN ("
				+" SELECT sd_date dts,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE 1=1 AND a.end_tmp_field='1')) tmp WHERE 1=1 ";
			
			return sql;
	}

	@Override
	public String CrossDomainHistoryAccessCityMsk_D_PP(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = " SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cross_regional_pp_city_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_history_access_city_msk_d' AND t.kpi_id='2_0_32' AND end_tmp_field='1' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) AS tmp UNION ALL ("
				+ " SELECT sd_date dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cross_regional_pp_city_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_history_access_city_msk_d' AND t.kpi_id='2_0_32' AND end_tmp_field='1' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) UNION ALL ("
				+ " SELECT sd_date dt,substr(city_id,1,3),city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_cross_regional_pp_city_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_history_access_city_msk_d' AND t.kpi_id='2_0_32' AND end_tmp_field='1' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}')) tmp WHERE 1=1";
				return sql;
	}

	@Override
	public String CrossDomainHistoryAccessCityMsk_D_NUM(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_history_access_city_msk_d' AND t.kpi_id='2_0_8' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count ORDER BY sd_date,SUM(count_nm) DESC) tmp UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_history_access_city_msk_d' AND t.kpi_id='2_0_8' GROUP BY sd_date,prov_id,city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count ORDER BY sd_date,SUM(count_nm) DESC) UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_history_access_city_msk_d' AND t.kpi_id='2_0_8' GROUP BY sd_date,prov_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count ORDER BY sd_date,SUM(count_nm) DESC) ORDER BY count_nm) tmp WHERE 1=1 ";
				return sql;
	}

	@Override
	public String CrossDomainHistoryAccessCityMsk_D1(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',count_nm '记录数',dis_users_nm '用户数',count_nm_rate '记录数占比',dis_users_nm_rate '用户数占比' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT a.dts dt,'allpv' prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,a.end_tmp_field,count_nm,users_nm,dis_users_nm,round(count_nm/count_nm_sum*100,2) count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,2) dis_users_nm_rate FROM ("
				+ " SELECT sd_date dts,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.end_tmp_field='1' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field) a INNER JOIN ("
				+ " SELECT sd_date dts,'allct' city_id,hive_table_name,data_source,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.end_tmp_field='1' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,data_source,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id AND a.data_source=b.data_source WHERE 1=1 AND a.end_tmp_field='1') tmp UNION ALL ("
				+ " SELECT a.dts dt,a.prov_id,'allct' city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,2) count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,2) dis_users_nm_rate FROM ("
				+ " SELECT sd_date dts,substr(city_id,1,3) prov_id,hive_table_name,kpi_id,data_source,end_tmp_field end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.end_tmp_field='1' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),end_tmp_field,hive_table_name,kpi_id,data_source) a INNER JOIN ("
				+ " SELECT sd_date dts,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.end_tmp_field='1' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE 1=1 AND a.kpi_id='2_0_31' AND a.end_tmp_field='1') UNION ALL ("
				+ " SELECT a.dts dt,a.prov_id,a.city_id,a.hive_table_name,a.kpi_id,a.data_source,end_tmp_field,a.count_nm,a.users_nm,a.dis_users_nm,round(a.count_nm/count_nm_sum*100,2) count_nm_rate,round(dis_users_nm/dis_users_nm_sum*100,2) dis_users_nm_rate FROM ("
				+ " SELECT sd_date dts,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field end_tmp_field,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,5),end_tmp_field,hive_table_name,kpi_id,data_source) a INNER JOIN ("
				+ " SELECT sd_date dts,hive_table_name,kpi_id,sum(count_nm) count_nm_sum,sum(users_nm) users_nm_sum,sum(dis_users_nm) dis_users_nm_sum FROM auto_user_cross_regional_rate a WHERE a.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND a.kpi_id='2_0_31' AND a.sd_date>='${star_dts}' AND a.sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id) b ON a.dts=b.dts AND a.hive_table_name=b.hive_table_name AND a.kpi_id=b.kpi_id WHERE 1=1 AND a.end_tmp_field='1')) tmp WHERE 1=1 ";

				return sql;
	}

	@Override
	public String CrossDomainHistoryAccessCityMsk_D_PP1(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = " SELECT dt '账期',"
				+  instance.getpvtoC("prov_id") + " '省份',"
				+  instance.getcttoC("city_id") + " '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm 用户数 FROM ("
				+" SELECT*FROM ("
				+" SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_cross_regional_pp_county_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND t.end_tmp_field='1' AND t.kpi_id='2_0_33' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) tmp UNION ALL ("
				+" SELECT sd_date AS dt,substr(city_id,1,3) AS prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_cross_regional_pp_county_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND t.kpi_id='2_0_33' AND t.end_tmp_field='1' GROUP BY sd_date,prov_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) UNION ALL ("
				+" SELECT sd_date AS dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_cross_regional_pp_county_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_history_access_county_msk_d' AND t.end_tmp_field='1' AND t.kpi_id='2_0_33')) tmp WHERE 1=1";
			return sql;
	}

	@Override
	public String CrossDomainHistoryAccessCityMsk_D_NUM1(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = " SELECT dt '账期',"
				+ instance.getpvtoC("prov_id")+" '省份',"
				+ instance.getcttoC("city_id")+" '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm '用户数' FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_history_access_city_msk_d' AND t.kpi_id='2_0_8' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count ORDER BY sd_date,SUM(count_nm) DESC) tmp UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_history_access_city_msk_d' AND t.kpi_id='2_0_8' GROUP BY sd_date,prov_id,city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count ORDER BY sd_date,SUM(count_nm) DESC) UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_citysource_rate t WHERE sd_date>='${star_dts}' AND sd_date<='${end_dts}' AND t.hive_table_name='dws_m.dws_wdtb_history_access_city_msk_d' AND t.kpi_id='2_0_8' GROUP BY sd_date,prov_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count ORDER BY sd_date,SUM(count_nm) DESC) ORDER BY count_nm) tmp WHERE 1=1 ";
				return sql;
	}

	@Override
	public String ResidentialPopulationAttributeWorkplaceVolatility(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+  instance.getpvtoC("prov_id") + " '省份',city_id '地市',count_nm '记录数',count_nm_volatility '记录数波动率',dis_users_nm '用户数',dis_users_nm_volatility '用户数波动率' FROM ("
				+ " SELECT t.sd_date dt,CASE WHEN city_id='allpv' THEN 'allpv' WHEN char_length(city_id)=3 THEN city_id ELSE substring(city_id,1,3) END 'prov_id',CASE WHEN city_id='allpv' THEN 'allct' WHEN char_length(city_id)=3 THEN 'allct' ELSE city_id END 'city_id',t.count_nm,t.count_nm_volatility,t.users_nm,t.users_nm_volatility,t.dis_users_nm,t.dis_users_nm_volatility FROM auto_user_workplace_city_rate_volatility t WHERE t.hive_table_name='dws_m.dws_wdtb_workplace_msk_w' AND t.kpi_id='2_0_37' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}') tmp WHERE 1=1 ";

			return sql;

	}

	@Override
	public String ResidentialPopulationAttributeWorkplaceTracking(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+  instance.getpvtoC("prov_id") + " '省份',"
				+  instance.getcttoC("city_id") + " '地市',hive_table_name '表名',kpi_id '数据类型',data_source '数据源类型',end_tmp_field,tmp_count,count_nm '记录数',dis_users_nm 用户数 FROM ("
				+ " SELECT t.dt,prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.data_source,t.end_tmp_field,t.tmp_count,t.count_nm,t.users_nm,t.dis_users_nm,MAX(CASE end_tmp_field WHEN '33' THEN dis_users_nm ELSE 0 END) AS dis_users_nm33,MAX(CASE end_tmp_field WHEN '66' THEN dis_users_nm ELSE 0 END) AS dis_users_nm66,MAX(CASE end_tmp_field WHEN '99' THEN dis_users_nm ELSE 0 END) AS dis_users_nm99 FROM ("
				+ " SELECT*FROM ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_workplace_city_tracking_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_workplace_msk_w' AND t.kpi_id='2_0_39' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,prov_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) tmp UNION ALL ("
				+ " SELECT sd_date AS dt,substr(city_id,1,3) prov_id,substr(city_id,1,5) city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_workplace_city_tracking_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_workplace_msk_w' AND t.kpi_id='2_0_39' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}') UNION ALL ("
				+ " SELECT sd_date AS dt,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,SUM(count_nm) AS count_nm,SUM(users_nm) AS users_nm,SUM(dis_users_nm) AS dis_users_nm FROM auto_user_workplace_city_tracking_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_workplace_msk_w' AND t.kpi_id='2_0_39' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count)) t WHERE t.hive_table_name='dws_m.dws_wdtb_workplace_msk_w' AND t.kpi_id='2_0_39' GROUP BY t.dt,prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.tmp_count) tmp WHERE 1=1 ";
			return sql;
	}

	@Override
	public String ResidentialPopulationAttributeResidentCityVolatility(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql =
				" SELECT dt '账期',"
				+  instance.getpvtoC("prov_id") + " '省份',"
				+  instance.getcttoC("city_id") + " '地市',count_nm '记录数',count_nm_volatility '记录数占比',dis_users_nm '用户数',dis_users_nm_volatility '用户数占比' FROM ("
				+ " SELECT t.sd_date dt,CASE WHEN city_id='allpv' THEN 'allpv' WHEN char_length(city_id)=3 THEN city_id ELSE substring(city_id,1,3) END 'prov_id',CASE WHEN city_id='allpv' THEN 'allct' WHEN char_length(city_id)=3 THEN 'allct' ELSE city_id END 'city_id',t.tmp_count,t.count_nm,t.count_nm_volatility,t.users_nm,t.users_nm_volatility,t.dis_users_nm,t.dis_users_nm_volatility FROM auto_user_resident_city_rate_volatility t WHERE t.hive_table_name='dws_m.dws_wdtb_resident_msk_w' AND t.kpi_id='2_0_36' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}') tmp WHERE 1=1 ";
			return sql;
	}

	@Override
	public String ResidentialPopulationAttributeResidentTracking(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = "SELECT dt '账期'," + instance.getpvtoC("prov_id") + " '省份'," + instance.getcttoC("city_id") + " '地市',dis_users_nm33 '置信度为33的用户数',dis_users_nm66 '置信度为66的用户数',dis_users_nm99 '置信度为99的用户数' FROM ( " +
                "SELECT t.dts dt,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.tmp_count,MAX(CASE end_tmp_field WHEN '33' THEN dis_users_nm ELSE 0 END) dis_users_nm33,MAX(CASE end_tmp_field WHEN '66' THEN dis_users_nm ELSE 0 END) dis_users_nm66,MAX(CASE end_tmp_field WHEN '99' THEN dis_users_nm ELSE 0 END) dis_users_nm99 FROM ( " +
                "SELECT*FROM ( " +
                "SELECT sd_date dts,substr(city_id,1,3) prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_resident_city_tracking_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_resident_msk_w' AND t.kpi_id='2_0_38' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,substr(city_id,1,3),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count) tmp UNION ALL ( " +
                "SELECT sd_date dts,substr(city_id,1,3),substr(city_id,1,5),hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,count_nm,users_nm,dis_users_nm FROM auto_user_resident_city_tracking_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_resident_msk_w' AND t.kpi_id='2_0_38' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}') UNION ALL ( " +
                "SELECT sd_date dts,'allpv' prov_id,'allct' city_id,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count,sum(count_nm) count_nm,sum(users_nm) users_nm,sum(dis_users_nm) dis_users_nm FROM auto_user_resident_city_tracking_rate t WHERE t.hive_table_name='dws_m.dws_wdtb_resident_msk_w' AND t.kpi_id='2_0_38' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,end_tmp_field,tmp_count)) t WHERE t.hive_table_name='dws_m.dws_wdtb_resident_msk_w' AND t.kpi_id='2_0_38' GROUP BY t.dts,t.prov_id,t.city_id,t.hive_table_name,t.kpi_id,t.tmp_count) tmp WHERE 1=1 ";

			return sql;
	}

	@Override
	public String CRMAcctUserInfoType(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id") +" '省份',eg_nm '统计字段',min_num '最小值',max_num '最大值',per_num '中位数',pin_num '平均值' FROM ("
				+ " SELECT sd_Date dt,city_id prov_id,eg_nm,max_num,min_num,per_num,pin_num FROM auto_user_bill_status WHERE hive_table_name='dwi_m.dwi_act_acct_user_info_msk_m' AND kpi_id='0_0_3' AND sd_Date>='${star_dts}' AND sd_Date<='${end_dts}') tmp WHERE 1=1 ";
			return sql;

	}

	@Override
	public String CRMAcctUserInfoCUT(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql =
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id") + " '省份',count_nm1 '缴费次数1的记录数',count_nm2 '缴费次数[2,10)的记录数',count_nm3 '缴费次数[10,50)的记录数',count_nm4 '缴费次数>=50的记录数',dis_users_nm1 '缴费次数1的用户数',dis_users_nm2 '缴费次数[2,10)的用户数',dis_users_nm3 '缴费次数[10,50)的用户数',dis_users_nm4 '缴费次数>=50的用户数' FROM ("
				+ " SELECT dt,prov_id,eg_nm,MAX(CASE end_tmp_field WHEN '1' THEN count_nm ELSE 0 END) count_nm1,MAX(CASE end_tmp_field WHEN '2-10' THEN count_nm ELSE 0 END) count_nm2,MAX(CASE end_tmp_field WHEN '10-50' THEN count_nm ELSE 0 END) count_nm3,MAX(CASE end_tmp_field WHEN '>=50' THEN count_nm ELSE 0 END) count_nm4,MAX(CASE end_tmp_field WHEN '1' THEN dis_users_nm ELSE 0 END) dis_users_nm1,MAX(CASE end_tmp_field WHEN '2-10' THEN dis_users_nm ELSE 0 END) dis_users_nm2,MAX(CASE end_tmp_field WHEN '10-50' THEN dis_users_nm ELSE 0 END) dis_users_nm3,MAX(CASE end_tmp_field WHEN '>=50' THEN dis_users_nm ELSE 0 END) dis_users_nm4 FROM ("
				+ " SELECT sd_date dt,city_id prov_id,hive_table_name,kpi_id,data_source,eg_nm,block_nm,end_tmp_field,count_nm,line_users_nm dis_users_nm FROM auto_user_bill_bistribution WHERE hive_table_name='dwi_m.dwi_act_acct_user_info_msk_m' AND kpi_id='1_0_4' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,city_id,hive_table_name,kpi_id,data_source,eg_nm,block_nm,end_tmp_field UNION ALL "
				+ " SELECT sd_date dt,'allpv' prov_id,hive_table_name,kpi_id,data_source,eg_nm,block_nm,end_tmp_field,sum(count_nm) count_nm,sum(line_users_nm) dis_users_nm FROM auto_user_bill_bistribution WHERE hive_table_name='dwi_m.dwi_act_acct_user_info_msk_m' AND kpi_id='1_0_4' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,eg_nm,block_nm,end_tmp_field) a WHERE 1=1 GROUP BY dt,prov_id) tmp where 1 = 1 ";
			return sql;
	}

	@Override
	public String CRMAcctUserFeetype(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id") + " '省份',eg_nm '统计字段',min_num '最小值',max_num '最大值',per_num '中位数',pin_num '平均值' FROM ("
				+ " SELECT sd_Date dt,city_id prov_id,eg_nm,max_num,min_num,per_num,pin_num FROM auto_user_bill_status WHERE hive_table_name='dwi_m.dwi_act_acct_user_fee_msk_m' AND kpi_id='0_0_3' AND sd_Date>='${star_dts}' AND sd_Date<='${end_dts}') tmp WHERE 1=1 ";
				return sql;
	}

	@Override
	public String CRMAcctUserFeeCUT(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+ instance.getpvtoC("prov_id") + " '省份',count_nm1 '缴费次数1的记录数',count_nm2 '缴费次数[2,10)的记录数',count_nm3 '缴费次数[10,50)的记录数',count_nm4 '缴费次数>=50的记录数',dis_users_nm1 '缴费次数1的用户数',dis_users_nm2 '缴费次数[2,10)的用户数',dis_users_nm3 '缴费次数[10,50)的用户数',dis_users_nm4 '缴费次数>=50的用户数' FROM ("
				+ " SELECT dt,prov_id,eg_nm,MAX(CASE end_tmp_field WHEN '1' THEN count_nm ELSE 0 END) count_nm1,MAX(CASE end_tmp_field WHEN '2-10' THEN count_nm ELSE 0 END) count_nm2,MAX(CASE end_tmp_field WHEN '10-50' THEN count_nm ELSE 0 END) count_nm3,MAX(CASE end_tmp_field WHEN '>=50' THEN count_nm ELSE 0 END) count_nm4,MAX(CASE end_tmp_field WHEN '1' THEN dis_users_nm ELSE 0 END) dis_users_nm1,MAX(CASE end_tmp_field WHEN '2-10' THEN dis_users_nm ELSE 0 END) dis_users_nm2,MAX(CASE end_tmp_field WHEN '10-50' THEN dis_users_nm ELSE 0 END) dis_users_nm3,MAX(CASE end_tmp_field WHEN '>=50' THEN dis_users_nm ELSE 0 END) dis_users_nm4 FROM ("
				+ " SELECT sd_date dt,city_id prov_id,hive_table_name,kpi_id,data_source,eg_nm,block_nm,end_tmp_field,count_nm,line_users_nm dis_users_nm FROM auto_user_bill_bistribution WHERE hive_table_name='dwi_m.dwi_act_acct_user_fee_msk_m' AND kpi_id='1_0_4' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,city_id,hive_table_name,kpi_id,data_source,eg_nm,block_nm,end_tmp_field UNION ALL "
				+ " SELECT sd_date dt,'allpv' prov_id,hive_table_name,kpi_id,data_source,eg_nm,block_nm,end_tmp_field,sum(count_nm) count_nm,sum(line_users_nm) dis_users_nm FROM auto_user_bill_bistribution WHERE hive_table_name='dwi_m.dwi_act_acct_user_fee_msk_m' AND kpi_id='1_0_4' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}' GROUP BY sd_date,hive_table_name,kpi_id,data_source,eg_nm,block_nm,end_tmp_field"
				+ " ) a WHERE 1=1 GROUP BY dt,prov_id) tmp where 1 = 1";

			return sql;

	}

	@Override
	public String SmallACCUser(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+  instance.getpvtoC("prov_id") + " '省份',only_user '用户数',round((only_user/count_user)*100,2) '用户数占比' FROM (SELECT*FROM ("
				+ " SELECT dt,pro_nm prov_id,only_user,("
				+ " SELECT sum(only_user) FROM ("
				+ " SELECT dt,pro_nm,only_user FROM dal_snail_null a WHERE dt>='${star_dts}' AND dt<='${end_dts}' AND tb_nm='dal_snail_user_info_msk_m' AND 1=1 GROUP BY dt,a.pro_nm) AS tmp) count_user FROM dal_snail_null a WHERE dt>='${star_dts}' AND dt<='${end_dts}' AND tb_nm='dal_snail_user_info_msk_m' AND 1=1 GROUP BY dt,a.pro_nm) AS tmp UNION ALL "
				+ " SELECT dt,'allpv' prov_id,only_user,("
				+ " SELECT sum(only_user) FROM ("
				+ " SELECT dt,pro_nm,only_user FROM dal_snail_null a WHERE dt>='${star_dts}' AND dt<='${end_dts}' AND tb_nm='dal_snail_user_info_msk_m' AND 1=1 GROUP BY dt) AS tmp) count_user FROM dal_snail_null a WHERE dt>='${star_dts}' AND dt<='${end_dts}' AND tb_nm='dal_snail_user_info_msk_m' GROUP BY dt) a WHERE 1=1 ";
				return sql;
	}

	@Override
	public String ResidentialPopulationAttributecityVolatility(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		FieldPublicFilter instance = FieldPublicFilter.getInstance();
		String sql = 
				" SELECT dt '账期',"
				+  instance.getpvtoC("prov_id") + " '省份',"
				+  instance.getcttoC("city_id") + " '地市',count_nm '记录数',count_nm_volatility '记录数波动率',dis_users_nm '用户数',dis_users_nm_volatility '用户数波动率' FROM ("
				+ " SELECT t.sd_date dt,CASE WHEN city_id='allpv' THEN 'allpv' WHEN LENGTH(city_id)=3 THEN city_id ELSE substr(city_id,1,3) END 'prov_id',CASE WHEN city_id='allpv' THEN 'allct' WHEN LENGTH(city_id)=3 THEN 'allct' ELSE city_id END 'city_id',t.count_nm,t.count_nm_volatility,t.dis_users_nm,t.dis_users_nm_volatility FROM auto_user_workplace_city_rate_volatility t WHERE t.hive_table_name='dws_m.dws_wdtb_workplace_msk_w' AND t.kpi_id='2_0_37' AND sd_date>='${star_dts}' AND sd_date<='${end_dts}') tmp WHERE 1=1 ";
			return sql;
	}

	@Override
	public String businessNUllRaito(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		String sql = 
				" SELECT a.*,a.month_interval '时段' FROM ("
				+ " SELECT dt '账期',month_interval,count_total '记录数',MAX(CASE field_name WHEN 'center_lati' THEN count_null ELSE '0' END) center_lati空值数,MAX(CASE field_name WHEN 'center_lati' THEN concat(percent_null,'%') ELSE '0%' END) center_lati空值率,MAX(CASE field_name WHEN 'center_longi' THEN count_null ELSE '0' END) center_longi空值数,MAX(CASE field_name WHEN 'center_longi' THEN concat(percent_null,'%') ELSE '0%' END) center_longi空值率,MAX(CASE field_name WHEN 'city_id' THEN count_null ELSE '0' END) city_id空值数,MAX(CASE field_name WHEN 'city_id' THEN concat(percent_null,'%') ELSE '0%' END) city_id空值率,MAX(CASE field_name WHEN 'county_id' THEN count_null ELSE '0' END) county_id空值数,MAX(CASE field_name WHEN 'county_id' THEN concat(percent_null,'%') ELSE '0%' END) county_id空值率,MAX(CASE field_name WHEN 'grid_id' THEN count_null ELSE '0' END) grid_id空值数,MAX(CASE field_name WHEN 'grid_id' THEN concat(percent_null,'%') ELSE '0%' END) grid_id空值率,MAX(CASE field_name WHEN 'mall_id' THEN count_null ELSE '0' END) mall_id空值数,MAX(CASE field_name WHEN 'mall_id' THEN concat(percent_null,'%') ELSE '0%' END) mall_id空值率,MAX(CASE field_name WHEN 'month_id' THEN count_null ELSE '0' END) month_id空值数,MAX(CASE field_name WHEN 'month_id' THEN concat(percent_null,'%') ELSE '0%' END) month_id空值率,MAX(CASE field_name WHEN 'month_interval' THEN count_null ELSE '0' END) month_interval空值数,MAX(CASE field_name WHEN 'month_interval' THEN concat(percent_null,'%') ELSE '0%' END) month_interval空值率,MAX(CASE field_name WHEN 'population_type' THEN count_null ELSE '0' END) population_type空值数,MAX(CASE field_name WHEN 'population_type' THEN concat(percent_null,'%') ELSE '0%' END) population_type空值率,MAX(CASE field_name WHEN 'prov_id' THEN count_null ELSE '0' END) prov_id空值数,MAX(CASE field_name WHEN 'prov_id' THEN concat(percent_null,'%') ELSE '0%' END) prov_id空值率,MAX(CASE field_name WHEN 'resi_count' THEN count_null ELSE '0' END) resi_count空值数,MAX(CASE field_name WHEN 'resi_count' THEN concat(percent_null,'%') ELSE '0%' END) resi_count空值率,MAX(CASE field_name WHEN 'resi_work_count' THEN count_null ELSE '0' END) resi_work_count空值数,MAX(CASE field_name WHEN 'resi_work_count' THEN concat(percent_null,'%') ELSE '0%' END) resi_work_count空值率,MAX(CASE field_name WHEN 'trade_id' THEN count_null ELSE '0' END) trade_id空值数,MAX(CASE field_name WHEN 'trade_id' THEN concat(percent_null,'%') ELSE '0%' END) trade_id空值率,MAX(CASE field_name WHEN 'work_count' THEN count_null ELSE '0' END) work_count空值数,MAX(CASE field_name WHEN 'work_count' THEN concat(percent_null,'%') ELSE '0%' END) work_count空值率 "
				+ " FROM o2oestate_mall_trade_heatmap_null_rate a "
				+ " WHERE a.tb_nm='o2oestate_trade_heatmap_msk_m' AND a.dt>='${star_dts}' AND a.dt<='${end_dts}' AND count_null<> 'NULL' "
				+ " GROUP BY dt,month_interval,count_total) a WHERE 1=1 ";
		return sql;
	}

	@Override
	public String businessZeroRaito(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		String sql = 
				" SELECT a.*,a.month_interval '时段' FROM ("
				+ " SELECT dt '账期',month_interval,count_total '记录数',MAX(CASE field_name WHEN 'center_lati' THEN count_zero ELSE '0' END) center_lati零值数,MAX(CASE field_name WHEN 'center_lati' THEN concat(percent_zero,'%') ELSE '0%' END) center_lati零值率,MAX(CASE field_name WHEN 'center_longi' THEN count_zero ELSE '0' END) center_longi零值数,MAX(CASE field_name WHEN 'center_longi' THEN concat(percent_zero,'%') ELSE '0%' END) center_longi零值率,MAX(CASE field_name WHEN 'city_id' THEN count_zero ELSE '0' END) city_id零值数,MAX(CASE field_name WHEN 'city_id' THEN concat(percent_zero,'%') ELSE '0%' END) city_id零值率,MAX(CASE field_name WHEN 'county_id' THEN count_zero ELSE '0' END) county_id零值数,MAX(CASE field_name WHEN 'county_id' THEN concat(percent_zero,'%') ELSE '0%' END) county_id零值率,MAX(CASE field_name WHEN 'grid_id' THEN count_zero ELSE '0' END) grid_id零值数,MAX(CASE field_name WHEN 'grid_id' THEN concat(percent_zero,'%') ELSE '0%' END) grid_id零值率,MAX(CASE field_name WHEN 'mall_id' THEN count_zero ELSE '0' END) mall_id零值数,MAX(CASE field_name WHEN 'mall_id' THEN concat(percent_zero,'%') ELSE '0%' END) mall_id零值率,MAX(CASE field_name WHEN 'month_id' THEN count_zero ELSE '0' END) month_id零值数,MAX(CASE field_name WHEN 'month_id' THEN concat(percent_zero,'%') ELSE '0%' END) month_id零值率,MAX(CASE field_name WHEN 'month_interval' THEN count_zero ELSE '0' END) month_interval零值数,MAX(CASE field_name WHEN 'month_interval' THEN concat(percent_zero,'%') ELSE '0%' END) month_interval零值率,MAX(CASE field_name WHEN 'population_type' THEN count_zero ELSE '0' END) population_type零值数,MAX(CASE field_name WHEN 'population_type' THEN concat(percent_zero,'%') ELSE '0%' END) population_type零值率,MAX(CASE field_name WHEN 'prov_id' THEN count_zero ELSE '0' END) prov_id零值数,MAX(CASE field_name WHEN 'prov_id' THEN concat(percent_zero,'%') ELSE '0%' END) prov_id零值率,MAX(CASE field_name WHEN 'resi_count' THEN count_zero ELSE '0' END) resi_count零值数,MAX(CASE field_name WHEN 'resi_count' THEN concat(percent_zero,'%') ELSE '0%' END) resi_count零值率,MAX(CASE field_name WHEN 'resi_work_count' THEN count_zero ELSE '0' END) resi_work_count零值数,MAX(CASE field_name WHEN 'resi_work_count' THEN concat(percent_zero,'%') ELSE '0%' END) resi_work_count零值率,MAX(CASE field_name WHEN 'trade_id' THEN count_zero ELSE '0' END) trade_id零值数,MAX(CASE field_name WHEN 'trade_id' THEN concat(percent_zero,'%') ELSE '0%' END) trade_id零值率,MAX(CASE field_name WHEN 'work_count' THEN count_zero ELSE '0' END) work_count零值数,MAX(CASE field_name WHEN 'work_count' THEN concat(percent_zero,'%') ELSE '0%' END) work_count零值率 "
				+ " FROM o2oestate_mall_trade_heatmap_null_rate a "
				+ " WHERE a.tb_nm='o2oestate_trade_heatmap_msk_m' AND a.dt>='${star_dts}' AND a.dt<='${end_dts}' AND count_null<> 'NULL' GROUP BY dt,month_interval,count_total) a WHERE 1=1 ";
		return sql;
	}

	@Override
	public String businessHeguiRaito(ExportMessage message) throws Exception {
		String sql = 
				" select a.*,a.month_interval '时段' from (SELECT dt '账期',month_interval,count_total '记录数',MAX(CASE field_name WHEN 'center_lati' THEN count_hegui ELSE '0' END) center_lati合规数,MAX(CASE field_name WHEN 'center_lati' THEN concat(percent_hegui,'%') ELSE '0%' END) center_lati合规率,MAX(CASE field_name WHEN 'center_longi' THEN count_hegui ELSE '0' END) center_longi合规数,MAX(CASE field_name WHEN 'center_longi' THEN concat(percent_hegui,'%') ELSE '0%' END) center_longi合规率,MAX(CASE field_name WHEN 'city_id' THEN count_hegui ELSE '0' END) city_id合规数,MAX(CASE field_name WHEN 'city_id' THEN concat(percent_hegui,'%') ELSE '0%' END) city_id合规率,MAX(CASE field_name WHEN 'county_id' THEN count_hegui ELSE '0' END) county_id合规数,MAX(CASE field_name WHEN 'county_id' THEN concat(percent_hegui,'%') ELSE '0%' END) county_id合规率,MAX(CASE field_name WHEN 'grid_id' THEN count_hegui ELSE '0' END) grid_id合规数,MAX(CASE field_name WHEN 'grid_id' THEN concat(percent_hegui,'%') ELSE '0%' END) grid_id合规率,MAX(CASE field_name WHEN 'mall_id' THEN count_hegui ELSE '0' END) mall_id合规数,MAX(CASE field_name WHEN 'mall_id' THEN concat(percent_hegui,'%') ELSE '0%' END) mall_id合规率,MAX(CASE field_name WHEN 'month_id' THEN count_hegui ELSE '0' END) month_id合规数,MAX(CASE field_name WHEN 'month_id' THEN concat(percent_hegui,'%') ELSE '0%' END) month_id合规率,MAX(CASE field_name WHEN 'month_interval' THEN count_hegui ELSE '0' END) month_interval合规数,MAX(CASE field_name WHEN 'month_interval' THEN concat(percent_hegui,'%') ELSE '0%' END) month_interval合规率,MAX(CASE field_name WHEN 'population_type' THEN count_hegui ELSE '0' END) population_type合规数,MAX(CASE field_name WHEN 'population_type' THEN concat(percent_hegui,'%') ELSE '0%' END) population_type合规率,MAX(CASE field_name WHEN 'prov_id' THEN count_hegui ELSE '0' END) prov_id合规数,MAX(CASE field_name WHEN 'prov_id' THEN concat(percent_hegui,'%') ELSE '0%' END) prov_id合规率,MAX(CASE field_name WHEN 'resi_count' THEN count_hegui ELSE '0' END) resi_count合规数,MAX(CASE field_name WHEN 'resi_count' THEN concat(percent_hegui,'%') ELSE '0%' END) resi_count合规率,MAX(CASE field_name WHEN 'resi_work_count' THEN count_hegui ELSE '0' END) resi_work_count合规数,MAX(CASE field_name WHEN 'resi_work_count' THEN concat(percent_hegui,'%') ELSE '0%' END) resi_work_count合规率,MAX(CASE field_name WHEN 'trade_id' THEN count_hegui ELSE '0' END) trade_id合规数,MAX(CASE field_name WHEN 'trade_id' THEN concat(percent_hegui,'%') ELSE '0%' END) trade_id合规率,MAX(CASE field_name WHEN 'work_count' THEN count_hegui ELSE '0' END) work_count合规数,MAX(CASE field_name WHEN 'work_count' THEN concat(percent_hegui,'%') ELSE '0%' END) work_count合规率 "
				+ " FROM o2oestate_mall_trade_heatmap_null_rate a WHERE a.tb_nm='o2oestate_trade_heatmap_msk_m' "
				+ " AND a.dt>='${star_dts}' AND count_hegui<> 'NULL' AND a.dt<='${end_dts}' GROUP BY dt,month_interval,count_total)a where 1 = 1 ";
		return sql;
	}

	@Override
	public String businessNUllmallRaito(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		String sql = 
				" SELECT a.*,a.month_interval '时段' FROM ("
				+ " SELECT dt '账期',month_interval,count_total '记录数',MAX(CASE field_name WHEN 'center_lati' THEN count_null ELSE '0' END) center_lati空值数,MAX(CASE field_name WHEN 'center_lati' THEN concat(percent_null,'%') ELSE '0%' END) center_lati空值率,MAX(CASE field_name WHEN 'center_longi' THEN count_null ELSE '0' END) center_longi空值数,MAX(CASE field_name WHEN 'center_longi' THEN concat(percent_null,'%') ELSE '0%' END) center_longi空值率,MAX(CASE field_name WHEN 'city_id' THEN count_null ELSE '0' END) city_id空值数,MAX(CASE field_name WHEN 'city_id' THEN concat(percent_null,'%') ELSE '0%' END) city_id空值率,MAX(CASE field_name WHEN 'county_id' THEN count_null ELSE '0' END) county_id空值数,MAX(CASE field_name WHEN 'county_id' THEN concat(percent_null,'%') ELSE '0%' END) county_id空值率,MAX(CASE field_name WHEN 'grid_id' THEN count_null ELSE '0' END) grid_id空值数,MAX(CASE field_name WHEN 'grid_id' THEN concat(percent_null,'%') ELSE '0%' END) grid_id空值率,MAX(CASE field_name WHEN 'mall_id' THEN count_null ELSE '0' END) mall_id空值数,MAX(CASE field_name WHEN 'mall_id' THEN concat(percent_null,'%') ELSE '0%' END) mall_id空值率,MAX(CASE field_name WHEN 'month_id' THEN count_null ELSE '0' END) month_id空值数,MAX(CASE field_name WHEN 'month_id' THEN concat(percent_null,'%') ELSE '0%' END) month_id空值率,MAX(CASE field_name WHEN 'month_interval' THEN count_null ELSE '0' END) month_interval空值数,MAX(CASE field_name WHEN 'month_interval' THEN concat(percent_null,'%') ELSE '0%' END) month_interval空值率,MAX(CASE field_name WHEN 'population_type' THEN count_null ELSE '0' END) population_type空值数,MAX(CASE field_name WHEN 'population_type' THEN concat(percent_null,'%') ELSE '0%' END) population_type空值率,MAX(CASE field_name WHEN 'prov_id' THEN count_null ELSE '0' END) prov_id空值数,MAX(CASE field_name WHEN 'prov_id' THEN concat(percent_null,'%') ELSE '0%' END) prov_id空值率,MAX(CASE field_name WHEN 'resi_count' THEN count_null ELSE '0' END) resi_count空值数,MAX(CASE field_name WHEN 'resi_count' THEN concat(percent_null,'%') ELSE '0%' END) resi_count空值率,MAX(CASE field_name WHEN 'resi_work_count' THEN count_null ELSE '0' END) resi_work_count空值数,MAX(CASE field_name WHEN 'resi_work_count' THEN concat(percent_null,'%') ELSE '0%' END) resi_work_count空值率,MAX(CASE field_name WHEN 'trade_id' THEN count_null ELSE '0' END) trade_id空值数,MAX(CASE field_name WHEN 'trade_id' THEN concat(percent_null,'%') ELSE '0%' END) trade_id空值率,MAX(CASE field_name WHEN 'work_count' THEN count_null ELSE '0' END) work_count空值数,MAX(CASE field_name WHEN 'work_count' THEN concat(percent_null,'%') ELSE '0%' END) work_count空值率 "
				+ " FROM o2oestate_mall_trade_heatmap_null_rate a "
				+ " WHERE a.tb_nm='o2oestate_mall_heatmap_msk_m' AND a.dt>='${star_dts}' AND a.dt<='${end_dts}' AND count_null<> 'NULL' "
				+ " GROUP BY dt,month_interval,count_total) a WHERE 1=1";
			return sql;
	}

	@Override
	public String businessZeromallRaito(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		String sql = 
				" SELECT a.*,a.month_interval '时段' FROM ("
				+ " SELECT dt '账期',month_interval,count_total '记录数',MAX(CASE field_name WHEN 'center_lati' THEN count_zero ELSE '0' END) center_lati零值数,MAX(CASE field_name WHEN 'center_lati' THEN concat(percent_zero,'%') ELSE '0%' END) center_lati零值率,MAX(CASE field_name WHEN 'center_longi' THEN count_zero ELSE '0' END) center_longi零值数,MAX(CASE field_name WHEN 'center_longi' THEN concat(percent_zero,'%') ELSE '0%' END) center_longi零值率,MAX(CASE field_name WHEN 'city_id' THEN count_zero ELSE '0' END) city_id零值数,MAX(CASE field_name WHEN 'city_id' THEN concat(percent_zero,'%') ELSE '0%' END) city_id零值率,MAX(CASE field_name WHEN 'county_id' THEN count_zero ELSE '0' END) county_id零值数,MAX(CASE field_name WHEN 'county_id' THEN concat(percent_zero,'%') ELSE '0%' END) county_id零值率,MAX(CASE field_name WHEN 'grid_id' THEN count_zero ELSE '0' END) grid_id零值数,MAX(CASE field_name WHEN 'grid_id' THEN concat(percent_zero,'%') ELSE '0%' END) grid_id零值率,MAX(CASE field_name WHEN 'mall_id' THEN count_zero ELSE '0' END) mall_id零值数,MAX(CASE field_name WHEN 'mall_id' THEN concat(percent_zero,'%') ELSE '0%' END) mall_id零值率,MAX(CASE field_name WHEN 'month_id' THEN count_zero ELSE '0' END) month_id零值数,MAX(CASE field_name WHEN 'month_id' THEN concat(percent_zero,'%') ELSE '0%' END) month_id零值率,MAX(CASE field_name WHEN 'month_interval' THEN count_zero ELSE '0' END) month_interval零值数,MAX(CASE field_name WHEN 'month_interval' THEN concat(percent_zero,'%') ELSE '0%' END) month_interval零值率,MAX(CASE field_name WHEN 'population_type' THEN count_zero ELSE '0' END) population_type零值数,MAX(CASE field_name WHEN 'population_type' THEN concat(percent_zero,'%') ELSE '0%' END) population_type零值率,MAX(CASE field_name WHEN 'prov_id' THEN count_zero ELSE '0' END) prov_id零值数,MAX(CASE field_name WHEN 'prov_id' THEN concat(percent_zero,'%') ELSE '0%' END) prov_id零值率,MAX(CASE field_name WHEN 'resi_count' THEN count_zero ELSE '0' END) resi_count零值数,MAX(CASE field_name WHEN 'resi_count' THEN concat(percent_zero,'%') ELSE '0%' END) resi_count零值率,MAX(CASE field_name WHEN 'resi_work_count' THEN count_zero ELSE '0' END) resi_work_count零值数,MAX(CASE field_name WHEN 'resi_work_count' THEN concat(percent_zero,'%') ELSE '0%' END) resi_work_count零值率,MAX(CASE field_name WHEN 'trade_id' THEN count_zero ELSE '0' END) trade_id零值数,MAX(CASE field_name WHEN 'trade_id' THEN concat(percent_zero,'%') ELSE '0%' END) trade_id零值率,MAX(CASE field_name WHEN 'work_count' THEN count_zero ELSE '0' END) work_count零值数,MAX(CASE field_name WHEN 'work_count' THEN concat(percent_zero,'%') ELSE '0%' END) work_count零值率 "
				+ " FROM o2oestate_mall_trade_heatmap_null_rate a "
				+ " WHERE a.tb_nm='o2oestate_mall_heatmap_msk_m' AND a.dt>='${star_dts}' AND a.dt<='${end_dts}' AND count_null<> 'NULL' GROUP BY dt,month_interval,count_total) a WHERE 1=1 ";
			return sql;
	}

	@Override
	public String businessHeguimallRaito(ExportMessage message) throws Exception {
		// TODO Auto-generated method stub
		String sql = 
				" select a.*,a.month_interval '时段' from (SELECT dt '账期',month_interval,count_total '记录数',MAX(CASE field_name WHEN 'center_lati' THEN count_hegui ELSE '0' END) center_lati合规数,MAX(CASE field_name WHEN 'center_lati' THEN concat(percent_hegui,'%') ELSE '0%' END) center_lati合规率,MAX(CASE field_name WHEN 'center_longi' THEN count_hegui ELSE '0' END) center_longi合规数,MAX(CASE field_name WHEN 'center_longi' THEN concat(percent_hegui,'%') ELSE '0%' END) center_longi合规率,MAX(CASE field_name WHEN 'city_id' THEN count_hegui ELSE '0' END) city_id合规数,MAX(CASE field_name WHEN 'city_id' THEN concat(percent_hegui,'%') ELSE '0%' END) city_id合规率,MAX(CASE field_name WHEN 'county_id' THEN count_hegui ELSE '0' END) county_id合规数,MAX(CASE field_name WHEN 'county_id' THEN concat(percent_hegui,'%') ELSE '0%' END) county_id合规率,MAX(CASE field_name WHEN 'grid_id' THEN count_hegui ELSE '0' END) grid_id合规数,MAX(CASE field_name WHEN 'grid_id' THEN concat(percent_hegui,'%') ELSE '0%' END) grid_id合规率,MAX(CASE field_name WHEN 'mall_id' THEN count_hegui ELSE '0' END) mall_id合规数,MAX(CASE field_name WHEN 'mall_id' THEN concat(percent_hegui,'%') ELSE '0%' END) mall_id合规率,MAX(CASE field_name WHEN 'month_id' THEN count_hegui ELSE '0' END) month_id合规数,MAX(CASE field_name WHEN 'month_id' THEN concat(percent_hegui,'%') ELSE '0%' END) month_id合规率,MAX(CASE field_name WHEN 'month_interval' THEN count_hegui ELSE '0' END) month_interval合规数,MAX(CASE field_name WHEN 'month_interval' THEN concat(percent_hegui,'%') ELSE '0%' END) month_interval合规率,MAX(CASE field_name WHEN 'population_type' THEN count_hegui ELSE '0' END) population_type合规数,MAX(CASE field_name WHEN 'population_type' THEN concat(percent_hegui,'%') ELSE '0%' END) population_type合规率,MAX(CASE field_name WHEN 'prov_id' THEN count_hegui ELSE '0' END) prov_id合规数,MAX(CASE field_name WHEN 'prov_id' THEN concat(percent_hegui,'%') ELSE '0%' END) prov_id合规率,MAX(CASE field_name WHEN 'resi_count' THEN count_hegui ELSE '0' END) resi_count合规数,MAX(CASE field_name WHEN 'resi_count' THEN concat(percent_hegui,'%') ELSE '0%' END) resi_count合规率,MAX(CASE field_name WHEN 'resi_work_count' THEN count_hegui ELSE '0' END) resi_work_count合规数,MAX(CASE field_name WHEN 'resi_work_count' THEN concat(percent_hegui,'%') ELSE '0%' END) resi_work_count合规率,MAX(CASE field_name WHEN 'trade_id' THEN count_hegui ELSE '0' END) trade_id合规数,MAX(CASE field_name WHEN 'trade_id' THEN concat(percent_hegui,'%') ELSE '0%' END) trade_id合规率,MAX(CASE field_name WHEN 'work_count' THEN count_hegui ELSE '0' END) work_count合规数,MAX(CASE field_name WHEN 'work_count' THEN concat(percent_hegui,'%') ELSE '0%' END) work_count合规率 "
				+ " FROM o2oestate_mall_trade_heatmap_null_rate a WHERE a.tb_nm='o2oestate_mall_heatmap_msk_m' "
				+ " AND a.dt>='${star_dts}' AND count_hegui<> 'NULL' AND a.dt<='${end_dts}' GROUP BY dt,month_interval,count_total)a where 1 = 1 ";
			return sql;
	}
	
}
