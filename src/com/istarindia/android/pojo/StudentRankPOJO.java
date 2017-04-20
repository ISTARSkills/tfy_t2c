package com.istarindia.android.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "studentRank")
public class StudentRankPOJO implements Comparable<StudentRankPOJO>{

	private Integer id;
	private String name;
	private String imageURL;
	private Integer batchRank;
	private Integer points;
	private Integer coins;
	
	public StudentRankPOJO(){
		
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
	
	@XmlAttribute(name = "imageURL", required = false)
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	
	@XmlAttribute(name = "batchRank", required = false)
	public Integer getBatchRank() {
		return batchRank;
	}
	public void setBatchRank(Integer batchRank) {
		this.batchRank = batchRank;
	}
	
	@XmlAttribute(name = "points", required = false)
	public Integer getPoints() {
		return points;
	}
	public void setPoints(Integer points) {
		this.points = points;
	}
	
	@XmlAttribute(name = "coins", required = false)
	public Integer getCoins() {
		return coins;
	}
	public void setCoins(Integer coins) {
		this.coins = coins;
	}
	
	public int compareTo(StudentRankPOJO thatStudentRankPOJO){
		return this.batchRank-thatStudentRankPOJO.getBatchRank();
	}
}
