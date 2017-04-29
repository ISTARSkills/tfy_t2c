package com.istarindia.android.pojo;

import java.sql.Timestamp;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.internal.txw2.annotation.XmlCDATA;

@XmlRootElement(name = "notification")
public class NotificationPOJO {

	private Integer id;
	private String message;
	private String status;
	private String imageURL;
	private Timestamp time;
	private String itemType;
	private Integer itemId;
	private HashMap<String, Object> item = new HashMap<String, Object>();
	
	public NotificationPOJO(){
		
	}
	
	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlCDATA
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@XmlAttribute(name = "status", required = false)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@XmlAttribute(name = "imageURL", required = false)
	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
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

	@XmlAttribute(name = "time", required = false)
	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	@XmlAttribute(name = "item", required = false)
	public HashMap<String, Object> getItem() {
		return item;
	}

	public void setItem(HashMap<String, Object> item) {
		this.item = item;
	}
}
