package com.istarindia.android.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "skillReport")
public class SkillReportPOJO{

	private Integer id;
	private String name;
	private Double totalPoints = 0.0;
	private Double userPoints = 0.0;
	private Double percentage = 0.0;
	private List<SkillReportPOJO> skills;
	
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
	
	@XmlAttribute(name = "totalPoints", required = false)
	public Double getTotalPoints() {
		return totalPoints;
	}
	public void setTotalPoints(Double totalPoints) {
		this.totalPoints = Math.round(totalPoints*100.0)/100.0;
	}
	
	@XmlAttribute(name = "userPoints", required = false)
	public Double getUserPoints() {
		return userPoints;
	}
	public void setUserPoints(Double userPoints) {
		this.userPoints = Math.round(userPoints*100.0)/100.0;
	}
	
	@XmlAttribute(name = "userPoints", required = false)
	public Double getPercentage() {
		return percentage;
	}
	
	@XmlAttribute(name = "skills", required = false)
	public List<SkillReportPOJO> getSkills() {
		return skills;
	}
	public void setSkills(List<SkillReportPOJO> skills) {
		this.skills = skills;
	}
	
	public void calculateTotalPoints() {
		Double totalPoints = 0.0;
		
		for(SkillReportPOJO temp : this.skills){
			totalPoints = totalPoints + temp.getTotalPoints();
		}		
		this.totalPoints = Math.round(totalPoints*100.0)/100.0;
	}
	
	public void calculateUserPoints() {
		Double userPoints = 0.0;
		
		for(SkillReportPOJO temp : this.skills){
			userPoints = userPoints + temp.getUserPoints();
		}		
		this.userPoints = Math.round(userPoints*100.0)/100.0;
	}
	
	public void calculatePercentage() {
		
		if(this.totalPoints > 0){
		this.percentage = (double) (Math.round((((double) this.userPoints)/this.totalPoints)*100.0*100.0)/100.0);
		}else{
			this.percentage = 0.0;
		}
	}
}
