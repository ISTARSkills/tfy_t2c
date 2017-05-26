package com.istarindia.android.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "interview")
public class AppInterviewPOJO {

	private Integer id;
	private Integer taskId;
	private String interviewURL;
	private Integer meetingId;
	private String meetingPassword;
	private String status;
	private String stageName;
	
	public AppInterviewPOJO(){
		
	}

	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlAttribute(name = "taskId", required = false)
	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	@XmlAttribute(name = "interviewURL", required = false)
	public String getInterviewURL() {
		return interviewURL;
	}

	public void setInterviewURL(String interviewURL) {
		this.interviewURL = interviewURL;
	}

	@XmlAttribute(name = "meetingId", required = false)
	public Integer getMeetingId() {
		return meetingId;
	}

	public void setMeetingId(Integer meetingId) {
		this.meetingId = meetingId;
	}

	@XmlAttribute(name = "meetingPassword", required = false)
	public String getMeetingPassword() {
		return meetingPassword;
	}

	public void setMeetingPassword(String meetingPassword) {
		this.meetingPassword = meetingPassword;
	}

	@XmlAttribute(name = "status", required = false)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@XmlAttribute(name = "stageName", required = false)
	public String getStageName() {
		return stageName;
	}

	public void setStageName(String stageName) {
		this.stageName = stageName;
	}
	
	
}
