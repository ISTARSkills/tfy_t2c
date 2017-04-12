package com.istarindia.android.pojo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dashboardCard")
public class DashboardCard implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer id;
	private String header;
	private String title;
	private String description;
	private Integer numberOfQuestions;
	private Integer itemExperience;
	private Integer itemCoins;
	private String itemType;
	private Integer itemId;
	private Integer duration;
	private String imageURL;
	private String videoURL;
	private String challengerName;
	private Integer challengerRank;
	private Integer challengerExperience;

	//Lesson, Video
	public DashboardCard(Integer id, String header, String title, String description, String imageURL, String videoURL, String itemType,
			Integer itemId) {
		this.id = id;
		this.header = header;
		this.title = title;
		this.description = description;
		this.imageURL = imageURL;
		this.videoURL = videoURL;
		this.itemType = itemType;
		this.itemId = itemId;
	}
	
	//Game, Job
	public DashboardCard(Integer id, String header, String title, String description, String imageURL, String itemType,
			Integer itemId) {
		this.id = id;
		this.header = header;
		this.title = title;
		this.description = description;
		this.imageURL = imageURL;
		this.itemType = itemType;
		this.itemId = itemId;
	}

	//Challenge
	public DashboardCard(Integer id, String header, String title, String description, String imageURL,
			Integer numberOfQuestions, Integer itemExperience, Integer duration, Integer itemCoins, String itemType,
			Integer itemId, Integer challengerExperience, Integer challengerRank) {
		this.id = id;
		this.header = header;
		this.title = title;
		this.description = description;
		this.duration = duration;
		this.imageURL = imageURL;
		this.numberOfQuestions = numberOfQuestions;
		this.itemExperience = itemExperience;
		this.itemCoins = itemCoins;
		this.itemType = itemType;
		this.itemId = itemId;
		this.challengerExperience = challengerExperience;
		this.challengerRank = challengerRank;
	}

	//Assessment
	public DashboardCard(Integer id, String header, String title, String description, String imageURL,
			Integer numberOfQuestions, Integer duration, Integer itemExperience, Integer itemCoins, String itemType,
			Integer itemId) {
		this.id = id;
		this.header = header;
		this.title = title;
		this.description = description;
		this.duration = duration;
		this.numberOfQuestions = numberOfQuestions;
		this.itemExperience = itemExperience;
		this.itemCoins = itemCoins;
		this.itemType = itemType;
		this.itemId = itemId;
		this.imageURL = "/root/talentify/assessment.png";
	}

	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlAttribute(name = "header", required = false)
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	@XmlAttribute(name = "title", required = false)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@XmlAttribute(name = "description", required = false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlAttribute(name = "numberOfQuestions", required = false)
	public Integer getNumberOfQuestions() {
		return numberOfQuestions;
	}

	public void setNumberOfQuestions(Integer numberOfQuestions) {
		this.numberOfQuestions = numberOfQuestions;
	}

	@XmlAttribute(name = "itemExperience", required = false)
	public Integer getItemExperience() {
		return itemExperience;
	}

	public void setItemExperience(Integer itemExperience) {
		this.itemExperience = itemExperience;
	}

	@XmlAttribute(name = "itemCoins", required = false)
	public Integer getItemCoins() {
		return itemCoins;
	}

	public void setItemCoins(Integer itemCoins) {
		this.itemCoins = itemCoins;
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

	@XmlAttribute(name = "duration", required = false)
	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	@XmlAttribute(name = "imageURL", required = false)
	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	
	@XmlAttribute(name = "videoURL", required = false)
	public String getVideoURL() {
		return videoURL;
	}

	public void setVideoURL(String videoURL) {
		this.videoURL = videoURL;
	}

	@XmlAttribute(name = "challengerName", required = false)
	public String getChallengerName() {
		return challengerName;
	}

	public void setChallengerName(String challengerName) {
		this.challengerName = challengerName;
	}

	@XmlAttribute(name = "challengerRank", required = false)
	public Integer getChallengerRank() {
		return challengerRank;
	}

	public void setChallengerRank(Integer challengerRank) {
		this.challengerRank = challengerRank;
	}

	@XmlAttribute(name = "challengerExperience", required = false)
	public Integer getChallengerExperience() {
		return challengerExperience;
	}

	public void setChallengerExperience(Integer challengerExperience) {
		this.challengerExperience = challengerExperience;
	}

}
