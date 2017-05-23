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
public class GroupPojo {

	Integer groupId;
	String groupName;
	Integer stuCount;
	ArrayList<GroupStudentPojo> students;
	
	@XmlAttribute(name = "group_id", required = false)
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	
	@XmlAttribute(name = "group_name", required = false)
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	@XmlAttribute(name = "stu_count", required = false)
	public Integer getStuCount() {
		return stuCount;
	}
	public void setStuCount(Integer stuCount) {
		this.stuCount = stuCount;
	}
	@XmlElement(name = "students", required = false)
	public ArrayList<GroupStudentPojo> getStudents() {
		return students;
	}
	public void setStudents(ArrayList<GroupStudentPojo> students) {
		this.students = students;
	}
	public GroupPojo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
