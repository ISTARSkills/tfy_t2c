package com.istarindia.android.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "skillReport")
public class SkillReportPOJO{

	private Integer id;
	private String name;
	private String description;
	private String itemType;
	private Integer itemId;
	private String imageURL;
	private Double totalPoints = 0.0;
	private Double userPoints = 0.0;
	private Double percentage = 0.0;
	private String message;
	private List<SkillReportPOJO> skills;
	
	public SkillReportPOJO(){
		
	}
	
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
	
	@XmlAttribute(name = "itemType", required = false)
	public String getItemType() {
		return itemType;
	}
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	
	@XmlAttribute(name = "itemId", required = false)
	public Integer getItemId() {
		return itemId;
	}
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
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
	
	@XmlAttribute(name = "percentage", required = false)
	public Double getPercentage() {
		return percentage;
	}
	
	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}
	
	@XmlAttribute(name = "imageURL", required = false)	
	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}


	@XmlElement(name = "skills", required = false)
	public List<SkillReportPOJO> getSkills() {
		return skills;
	}
	public void setSkills(List<SkillReportPOJO> skills) {
		this.skills = skills;
	}
	
	@XmlElement(name = "message", required = false)
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void generateMessage(){		
		if(this.skills!=null){
			this.message = this.userPoints+ "/" + this.totalPoints + " - " + this.skills.size() + " subskills";
		}
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
