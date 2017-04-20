package com.istarindia.android.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "module")
public class ModulePOJO implements Comparable<ModulePOJO>{

	private Integer id;
	private String name;
	private String description;
	private String status;
	private String imageURL;
	private Integer orderId;
	//private List<LessonPOJO> lessons = new ArrayList<LessonPOJO>();
	private List<SkillReportPOJO> sessionSkills = new ArrayList<SkillReportPOJO>();
	private ArrayList<String> skillObjectives = new ArrayList<String>();
	
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
	
	@XmlAttribute(name = "imageURL", required = false)
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	
	@XmlAttribute(name = "orderId", required = false)
	public Integer getOrderId() {
		return orderId;
	}
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	
/*	@XmlAttribute(name = "lessons", required = false)
	public List<LessonPOJO> getLessons() {
		return lessons;
	}
	public void setLessons(List<LessonPOJO> lessons) {
		this.lessons = lessons;
	}*/
	
	@XmlAttribute(name = "skillObjectives", required = false)
	public ArrayList<String> getSkillObjectives() {
		return skillObjectives;
	}
	public void setSkillObjectives(ArrayList<String> skillObjectives) {
		this.skillObjectives = skillObjectives;
	}
	
	@XmlAttribute(name = "status", required = false)
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@XmlAttribute(name = "sessionSkills", required = false)
	public List<SkillReportPOJO> getSessionSkills() {
		return sessionSkills;
	}
	public void setSessionSkills(List<SkillReportPOJO> sessionSkills) {
		this.sessionSkills = sessionSkills;
	}

	@Override
	public int compareTo(ModulePOJO o) {
		return this.orderId -o.orderId;
	}
}
