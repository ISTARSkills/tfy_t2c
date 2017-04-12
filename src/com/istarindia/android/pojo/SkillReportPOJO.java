package com.istarindia.android.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "skillReport")
public class SkillReportPOJO {

	private Integer id;
	private String name;
	private Integer totalPoints;
	private Integer userPoints;
	private Integer percentage;
	private List<SkillReportPOJO> skills = new ArrayList<SkillReportPOJO>();
	
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
	public Integer getTotalPoints() {
		return totalPoints;
	}
	public void setTotalPoints(Integer totalPoints) {
		this.totalPoints = totalPoints;
	}
	
	@XmlAttribute(name = "userPoints", required = false)
	public Integer getUserPoints() {
		return userPoints;
	}
	public void setUserPoints(Integer userPoints) {
		this.userPoints = userPoints;
	}
	
	@XmlAttribute(name = "userPoints", required = false)
	public Integer getPercentage() {
		return percentage;
	}
	public void setPercentage(Integer percentage) {
		this.percentage = percentage;
	}
	@XmlAttribute(name = "skills", required = false)
	public List<SkillReportPOJO> getSkills() {
		return skills;
	}
	public void setSkills(List<SkillReportPOJO> skills) {
		this.skills = skills;
	}
}
