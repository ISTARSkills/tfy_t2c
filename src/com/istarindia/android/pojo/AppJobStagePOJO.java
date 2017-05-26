package com.istarindia.android.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "interview")
public class AppJobStagePOJO {

	private Integer id;
	private Integer taskId;
	private String stageName;
	private String status;
	private String description;
	private String type;
	
	public AppJobStagePOJO(){
		
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
	
	@XmlAttribute(name = "stageName", required = false)
	public String getStageName() {
		return stageName;
	}
	public void setStageName(String stageName) {
		this.stageName = stageName;
	}
	
	@XmlAttribute(name = "status", required = false)
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@XmlAttribute(name = "description", required = false)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlAttribute(name = "type", required = false)
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
