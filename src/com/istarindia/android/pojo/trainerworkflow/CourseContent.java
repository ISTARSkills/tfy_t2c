/**
 * 
 */
package com.istarindia.android.pojo.trainerworkflow;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author mayank
 *
 */
public class CourseContent {

	Integer currentItemOrderId;
	Integer nextItemOrderId;
	Integer previousItemOrderId;
	ArrayList<CourseItem> items;
	String contentUrl;
	Integer courseId;
	Integer currentItemSlideId;
	
	
	@XmlAttribute(name = "course_id", required = false)
	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}

	@XmlAttribute(name = "current_slide_id", required = false)
	public Integer getCurrentItemSlideId() {
		return currentItemSlideId;
	}

	public void setCurrentItemSlideId(Integer currentItemSlideId) {
		this.currentItemSlideId = currentItemSlideId;
	}
	
	
	public CourseContent() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@XmlAttribute(name = "current_item_order_id", required = false)
	public Integer getCurrentItemOrderId() {
		return currentItemOrderId;
	}


	public void setCurrentItemOrderId(Integer currentItemOrderId) {
		this.currentItemOrderId = currentItemOrderId;
	}

	@XmlAttribute(name = "next_item_order_id", required = false)
	public Integer getNextItemOrderId() {
		return nextItemOrderId;
	}


	public void setNextItemOrderId(Integer nextItemOrderId) {
		this.nextItemOrderId = nextItemOrderId;
	}

	@XmlAttribute(name = "previous_item_order_id", required = false)
	public Integer getPreviousItemOrderId() {
		return previousItemOrderId;
	}


	public void setPreviousItemOrderId(Integer previousItemOrderId) {
		this.previousItemOrderId = previousItemOrderId;
	}


	@XmlElement(name = "items", required = false)
	public ArrayList<CourseItem> getItems() {
		return items;
	}
	public void setItems(ArrayList<CourseItem> items) {
		this.items = items;
	}

	@XmlAttribute(name = "content_url", required = false)
	public String getContentUrl() {
		return contentUrl;
	}

	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}
	
	
	
	
}
