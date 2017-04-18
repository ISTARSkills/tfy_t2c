package com.istarindia.android.pojo;

import java.util.ArrayList;
import java.util.Collections;

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
	private Integer rank;
	private Double userPoints;
	private Double totalPoints;	
	private Double accuracy;
	private ArrayList<ModulePOJO> modules = new ArrayList<ModulePOJO>();
	
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
	
	@XmlAttribute(name = "rank", required = false)
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	
	@XmlAttribute(name = "userPoints", required = false)
	public Double getUserPoints() {
		return userPoints;
	}
	public void setUserPoints(Double userPoints) {
		this.userPoints = userPoints;
	}
	
	@XmlAttribute(name = "totalPoints", required = false)
	public Double getTotalPoints() {
		return totalPoints;
	}
	public void setTotalPoints(Double totalPoints) {
		this.totalPoints = totalPoints;
	}
	
	@XmlAttribute(name = "accuracy", required = false)
	public Double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}
		
	public CoursePOJO sortModulesAndAssignStatus(){		
		CoursePOJO coursePOJO = this;		
		Collections.sort(coursePOJO.getModules());
		
		String courseStatus = "COMPLETE";
		
		for(ModulePOJO module: coursePOJO.getModules()){
			if(module.getStatus().equals("INCOMPLETE")){
				courseStatus = "INCOMPLETE";
			}
		}
		coursePOJO.setStatus(courseStatus);	
	return coursePOJO;
	}
}
