package com.istarindia.android.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "session")
public class SessionPOJO {

	private Integer id;
	private String name;
	private String description;
	private String imageURL;
	private Integer orderId;
	private Integer progress;

	
	private List<ConcreteItemPOJO> lessons = new ArrayList<ConcreteItemPOJO>();



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

	@XmlAttribute(name = "orderId", required = false)
	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	@XmlAttribute(name = "progress", required = false)
	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	@XmlElement(name = "lessons", required = false)
	public List<ConcreteItemPOJO> getLessons() {
		return lessons;
	}

	public void setLessons(List<ConcreteItemPOJO> lessons) {
		this.lessons = lessons;
	}

	
}
