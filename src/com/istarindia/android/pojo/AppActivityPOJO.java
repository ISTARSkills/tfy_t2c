package com.istarindia.android.pojo;

import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "activity")
public class AppActivityPOJO {

	private Integer id;
	private Integer istarUserId;
	private String userName;
	private String userImage;
	private String userEmail;
	private String userPhone;
	private String type;
	private String message;
	private Timestamp time;
	
	public AppActivityPOJO(){
		
	}

	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlAttribute(name = "istarUserId", required = false)
	public Integer getIstarUserId() {
		return istarUserId;
	}

	public void setIstarUserId(Integer istarUserId) {
		this.istarUserId = istarUserId;
	}

	@XmlAttribute(name = "userName", required = false)
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@XmlAttribute(name = "userImage", required = false)
	public String getUserImage() {
		return userImage;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}
	
	@XmlAttribute(name = "userEmail", required = false)
	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	@XmlAttribute(name = "userPhone", required = false)
	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	@XmlAttribute(name = "type", required = false)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlAttribute(name = "message", required = false)
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@XmlAttribute(name = "time", required = false)
	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}
	
	
	
}
