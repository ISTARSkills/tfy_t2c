package com.istarindia.android.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cmsession")
public class CmsessionPOJO implements Comparable<CmsessionPOJO>{

	private Integer id;
	private String type;
	private LessonPOJO lesson;
	private AssessmentPOJO assessment;
	private String status;
	private Integer orderId = 0;
	
	public CmsessionPOJO(){
		
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

	@XmlAttribute(name = "lesson", required = false)
	public LessonPOJO getLesson() {
		return lesson;
	}

	public void setLesson(LessonPOJO lesson) {
		this.lesson = lesson;
	}

	@XmlAttribute(name = "assessment", required = false)
	public AssessmentPOJO getAssessment() {
		return assessment;
	}

	public void setAssessment(AssessmentPOJO assessment) {
		this.assessment = assessment;
	}

	@XmlAttribute(name = "item", required = false)
	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	@XmlAttribute(name = "status", required = false)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public int compareTo(CmsessionPOJO o) {
		return this.orderId -o.orderId;
	}
}
