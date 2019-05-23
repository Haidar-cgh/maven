package com.dw.model;

import java.util.List;

public class RootMenu implements Cloneable{
	
	
	private Integer id;
	private Integer menu_index;
	private String menu_id;
	private String menu_name;
 	private String menu_desc;
	private String menu_url; 
	private String menu_level;
	private String menu_id_parent;
	private String create_time;
	private String update_time;
	private String menu_icon_code;
	private Integer menu_check;

	private List child_list;
	
	
	
	
	 
	public Integer getMenu_check() {
		return menu_check;
	}
	public void setMenu_check(Integer menu_check) {
		this.menu_check = menu_check;
	}
	public List getChild_list() {
		return child_list;
	}
	public void setChild_list(List child_list) {
		this.child_list = child_list;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	 
	public String getMenu_id() {
		return menu_id;
	}
	public void setMenu_id(String menu_id) {
		this.menu_id = menu_id;
	}
	public String getMenu_name() {
		return menu_name;
	}
	public void setMenu_name(String menu_name) {
		this.menu_name = menu_name;
	}
	public String getMenu_desc() {
		return menu_desc;
	}
	public void setMenu_desc(String menu_desc) {
		this.menu_desc = menu_desc;
	}
	public String getMenu_url() {
		return menu_url;
	}
	public void setMenu_url(String menu_url) {
		this.menu_url = menu_url;
	}
	public String getMenu_level() {
		return menu_level;
	}
	public void setMenu_level(String menu_level) {
		this.menu_level = menu_level;
	}
	public String getMenu_id_parent() {
		return menu_id_parent;
	}
	public void setMenu_id_parent(String menu_id_parent) {
		this.menu_id_parent = menu_id_parent;
	}
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public String getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}
	public Integer getMenu_index() {
		return menu_index;
	}
	public void setMenu_index(Integer menu_index) {
		this.menu_index = menu_index;
	}
	public String getMenu_icon_code() {
		return menu_icon_code;
	}
	public void setMenu_icon_code(String menu_icon_code) {
		this.menu_icon_code = menu_icon_code;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		Object object = super.clone();
		return object;

	}
	
}
