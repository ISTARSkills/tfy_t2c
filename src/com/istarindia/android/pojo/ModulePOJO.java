package com.istarindia.android.pojo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "module")
public class ModulePOJO implements Comparable<ModulePOJO>{

	private Integer id;
	private String name;
	private String description;
	private String status;
	private String imageURL;
	private Integer orderId;
	private Set<String> skillObjectives = new HashSet<String>();
	private List<SessionPOJO> sessions = new ArrayList<SessionPOJO>();

	
	public ModulePOJO(){
		
	}
	
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
	
	
	
	
	@XmlElement(name = "sessions", required = false)
	public List<SessionPOJO> getSessions() {
		return sessions;
	}

	public void setSessions(List<SessionPOJO> sessions) {
		this.sessions = sessions;
	}

	@XmlElement(name = "skillObjectives", required = false)
	public Set<String> getSkillObjectives() {
		return skillObjectives;
	}
	public void setSkillObjectives(Set<String> skillObjectives) {
		this.skillObjectives = skillObjectives;
	}
	
	@XmlAttribute(name = "status", required = false)
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	/*public ModulePOJO sortLessonsAndAssignStatus(){		
		ModulePOJO modulePOJO = this;		
		Collections.sort(modulePOJO.getLessons());
		
		String moduleStatus = "COMPLETED";
		
		for(ConcreteItemPOJO cmsession: modulePOJO.getLessons()){
			if((cmsession.getStatus().equals("INCOMPLETE"))){
				moduleStatus = "INCOMPLETE";
			}
		}
		modulePOJO.setStatus(moduleStatus);	
	return modulePOJO;
	}*/
	
	@Override
	public int compareTo(ModulePOJO o) {
		return this.orderId -o.orderId;
	}
}
