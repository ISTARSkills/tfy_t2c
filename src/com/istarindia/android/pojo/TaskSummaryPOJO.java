package com.istarindia.android.pojo;

import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "task")
public class TaskSummaryPOJO {

	private Integer id;
	private String header;
	private String title;
	private String description;	
	private String itemType;
	private Integer itemId;
	private Integer duration;
	private String imageURL;
	private String status;
	private Timestamp date;
	private String messageForCompletedTasks;
	private String messageForIncompleteTasks;
	
	
	/*related to assessment task starts here*/
	private Integer numberOfQuestions;
	private Integer itemPoints;
	private Integer itemCoins;
	/*related to assessment task ends here*/
	
	/*related to classroom task starts here*/
	private Double lattitude;
	private Double longitude;
	private Integer durationHours;
	private Integer durationMinutes;
	private String groupName;
	private Integer classRoomId;
	private String classRoomName;
	private String time;
	/*related to classroom task ends here*/
	public TaskSummaryPOJO(){
		
	}

	
	@XmlAttribute(name = "lattitude", required = false)
	public Double getLattitude() {
		return lattitude;
	}
	public void setLattitude(Double lattitude) {
		this.lattitude = lattitude;
	}
	
	@XmlAttribute(name = "longitude", required = false)
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	
	@XmlAttribute(name = "duration_hours", required = false)
	public Integer getDurationHours() {
		return durationHours;
	}
	public void setDurationHours(Integer durationHours) {
		this.durationHours = durationHours;
	}
	
	@XmlAttribute(name = "duration_minutes", required = false)
	public Integer getDurationMinutes() {
		return durationMinutes;
	}
	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}
	@XmlAttribute(name = "group_name", required = false)
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	@XmlAttribute(name = "class_room_id", required = false)
	public Integer getClassRoomId() {
		return classRoomId;
	}
	public void setClassRoomId(Integer classRoomId) {
		this.classRoomId = classRoomId;
	}
	@XmlAttribute(name = "class_room_name", required = false)
	public String getClassRoomName() {
		return classRoomName;
	}
	public void setClassRoomName(String classRoomName) {
		this.classRoomName = classRoomName;
	}
	
	@XmlAttribute(name = "time", required = false)
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	

	@XmlAttribute(name = "numberOfQuestions", required = false)
	public Integer getNumberOfQuestions() {
		return numberOfQuestions;
	}


	public void setNumberOfQuestions(Integer numberOfQuestions) {
		this.numberOfQuestions = numberOfQuestions;
	}

	@XmlAttribute(name = "itemPoints", required = false)
	public Integer getItemPoints() {
		return itemPoints;
	}


	public void setItemPoints(Integer itemPoints) {
		this.itemPoints = itemPoints;
	}

	@XmlAttribute(name = "itemCoins", required = false)
	public Integer getItemCoins() {
		return itemCoins;
	}


	public void setItemCoins(Integer itemCoins) {
		this.itemCoins = itemCoins;
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

	@XmlAttribute(name = "status", required = false)
	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}

	@XmlAttribute(name = "date", required = false)
	public Timestamp getDate() {
		return date;
	}


	public void setDate(Timestamp date) {
		this.date = date;
	}

	@XmlAttribute(name = "messageForCompletedTasks", required = false)
	public String getMessageForCompletedTasks() {
		return messageForCompletedTasks;
	}

	public void setMessageForCompletedTasks(String messageForCompletedTasks) {
		this.messageForCompletedTasks = messageForCompletedTasks;
	}

	@XmlAttribute(name = "messageForIncompleteTasks", required = false)
	public String getMessageForIncompleteTasks() {
		return messageForIncompleteTasks;
	}

	public void setMessageForIncompleteTasks(String messageForIncompleteTasks) {
		this.messageForIncompleteTasks = messageForIncompleteTasks;
	}

	
	
	
	
	
}
