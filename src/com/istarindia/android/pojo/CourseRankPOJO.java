package com.istarindia.android.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "courseRank")
public class CourseRankPOJO {

	private Integer id;
	private String name;
	private String description;
	private String imageURL;
	private List<StudentRankPOJO> allStudentRanks = new ArrayList<StudentRankPOJO>();
	
	public CourseRankPOJO(){
		
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
	
	@XmlAttribute(name = "imageURL", required = false)
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	
	@XmlElement(name = "allStudentRanks", required = false)
	public List<StudentRankPOJO> getAllStudentRanks() {
		return allStudentRanks;
	}
	public void setAllStudentRanks(List<StudentRankPOJO> allStudentRanks) {
		this.allStudentRanks = allStudentRanks;
	}
	
	
}
