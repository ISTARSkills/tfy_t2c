/**
 * 
 */
package com.istarindia.android.pojo.trainerworkflow;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author mayank
 *
 */
public class GroupStudentPojo {

	Integer studentId;
	String studentName;
	String imageUrl;
	Boolean status;
	
	@XmlAttribute(name = "student_id", required = false)
	public Integer getStudentId() {
		return studentId;
	}
	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}
	
	@XmlAttribute(name = "student_name", required = false)
	public String getStudentName() {
		return studentName;
	}
	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}
	
	@XmlAttribute(name = "image_url", required = false)
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public GroupStudentPojo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@XmlAttribute(name = "status", required = false)
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}
	
	
	
}
