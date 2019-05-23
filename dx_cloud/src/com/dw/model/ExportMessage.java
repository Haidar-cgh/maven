package com.dw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.metamodel.domain.Superclass;

public class ExportMessage implements Serializable {
	private static final long serialVersionUID = 6461342140496395279L;
	private String method = null;
	private HashMap<String, Object> req = new HashMap<String, Object>();
	private List<Map<String, Object>> header = new ArrayList<Map<String, Object>>();
	private List<ExportMessage> messageobj = new ArrayList<ExportMessage>();
	private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
	private List<Class> paramType = new ArrayList<Class>();
	private String fieldNameEN = "";
	private String fieldVal = "";
	private String order = "";
	private String msg = "";
	private Integer code = 1;
	
	
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getFieldNameEN() {
		return fieldNameEN;
	}
	public void setFieldNameEN(String fieldNameEN) {
		try {
			this.fieldNameEN = fieldNameEN;
			String[] key = fieldNameEN.split(",");
			String[] val = fieldVal.split(",");
			HashMap<String, Object> reqsHashMap= new HashMap<String, Object>();
			for (int i = 0; i < key.length; i++) {
				reqsHashMap.put(key[i], val[i]);
			}
			this.setReq(reqsHashMap);
		} catch (Exception e) {
		}
	}
	public String getFieldVal() {
		return fieldVal;
	}
	public void setFieldVal(String fieldVal) {
		try {
			String[] key = fieldNameEN.split(",");
			String[] val = fieldVal.split(",");
			HashMap<String, Object> reqsHashMap= new HashMap<String, Object>();
			for (int i = 0; i < key.length; i++) {
				reqsHashMap.put(key[i], val[i]);
			}
			this.setReq(reqsHashMap);
		} catch (Exception e) {
		}
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public HashMap<String, Object> getReq() {
		return req;
	}
	public void setReq(HashMap<String, Object> req) {
		this.req = req;
	}
	public List<Map<String, Object>> getHeader() {
		return header;
	}
	public void setHeader(List<Map<String, Object>> header) {
		this.header = header;
	}
	public List<Map<String, Object>> getData() {
		return data;
	}
	public void setData(List<Map<String, Object>> data) {
		this.data = data;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public List<Class> getParamType() {
		return paramType;
	}
	public void setParamType(List<Class> paramType) {
		this.paramType = paramType;
	}
	@Override
	public String toString() {
		return "ExportMessage [\nmethod=" + method + ", \nreq=" + req + ", \nheader=" + header + ", \nmessageobj=" + messageobj
				+ ", \ndata=" + data + ", \nparamType=" + paramType + ", \nfieldNameEN=" + fieldNameEN + ", \nfieldVal="
				+ fieldVal + ", \norder=" + order + ", \nmsg=" + msg + ", \ncode=" + code + "]";
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
		
	}
}
