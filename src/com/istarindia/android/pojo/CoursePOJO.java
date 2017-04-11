package com.istarindia.android.pojo;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "course")
public class CoursePOJO {

	private Integer id;
	private String name;
	private String description;
	private String category;
	private String imageURL;
	private String status;
	private ArrayList<ModulePOJO> modules;
	
	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlAttribute(name = "name", required = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute(name = "description", required = false)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlAttribute(name = "category", required = false)
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	@XmlAttribute(name = "imageURL", required = false)
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	
	@XmlAttribute(name = "status", required = false)
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@XmlAttribute(name = "modules", required = false)
	public ArrayList<ModulePOJO> getModules() {
		return modules;
	}
	public void setModules(ArrayList<ModulePOJO> modules) {
		this.modules = modules;
	}
	
	
}