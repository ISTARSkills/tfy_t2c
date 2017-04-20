package com.istarindia.android.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "assessment")
public class AssessmentPOJO {

	private Integer id;
	private String type;
	private String name;
	private String category;
	private Integer durationInMinutes;
	private Double points;
	private List<QuestionPOJO> questions = new ArrayList<QuestionPOJO>();
	
	public AssessmentPOJO(){
		
	}
	
	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlAttribute(name = "type", required = false)
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@XmlAttribute(name = "name", required = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute(name = "category", required = false)
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	@XmlAttribute(name = "durationInMinutes", required = false)
	public Integer getDurationInMinutes() {
		return durationInMinutes;
	}
	public void setDurationInMinutes(Integer durationInMinutes) {
		this.durationInMinutes = durationInMinutes;
	}
	
	@XmlAttribute(name = "points", required = false)
	public Double getPoints() {
		return points;
	}
	public void setPoints(Double points) {
		this.points = points;
	}
	
	@XmlElement(name = "questions", required = false)
	public List<QuestionPOJO> getQuestions() {
		return questions;
	}
	public void setQuestions(List<QuestionPOJO> questions) {
		this.questions = questions;
	}
}
