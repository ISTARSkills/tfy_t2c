/**
 * 
 */
package com.istarindia.android.pojo.task;

import javax.xml.bind.annotation.XmlAttribute;

import com.istarindia.android.pojo.TaskSummaryPOJO;

/**
 * @author mayank
 *
 */
public class ClassRoomSessionTask extends TaskSummaryPOJO {

	Double lattitude;
	Double longitude;
	Integer durationHours;
	Integer durationMinutes;
	String groupName;
	Integer classRoomId;
	String classRoomName;
	String time;
	
	
	
	public ClassRoomSessionTask() {
		super();
		// TODO Auto-generated constructor stub
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
	
	
	
	
}
