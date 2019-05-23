package com.dw.servce;


import java.util.List;
import java.util.Map;

import com.dw.common.FieldPublicFilter;
import com.dw.model.ExportMessage;
import com.google.inject.spi.Message;

public interface IExportService {
	
	public void test(ExportMessage message);
	
	/**
	 * 获取 tableheadconfig.properties对应的sql
	 * @param message
	 * @throws Exception 
	 */
	public ExportMessage getFieldHead(ExportMessage message) throws Exception;

	//获取sql 的字段的表头
	public ExportMessage getFieldHeadHibenate(ExportMessage message) throws Exception;
	/**
	 * 进行封装到 message 的 data 中
	 * @param message
	 * @param sql
	 * @throws Exception
	 */
	public List<Map<String, Object>> getData(ExportMessage message)throws Exception;
	/**
	 *  获取 FieldPublicFilter 实例
	 * @return
	 */
	public FieldPublicFilter getFieldPublicFilter();
	
	/**
	 * 获取 行数
	 * @param message
	 * @return
	 * @throws Exception
	 */
	ExportMessage getCount(ExportMessage message) throws Exception;

	public void findExport(ExportMessage message) throws Exception;
}
