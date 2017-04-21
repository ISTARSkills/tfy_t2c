package com.istarindia.android.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "assessmentResponse")
public class AssessmentResponsePOJO {

	private Integer id;
	private List<QuestionResponsePOJO> response = new ArrayList<QuestionResponsePOJO>();
		
	public AssessmentResponsePOJO(){
		
	}
	
	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlAttribute(name = "response", required = false)
	public List<QuestionResponsePOJO> getResponse() {
		return response;
	}
	public void setResponse(List<QuestionResponsePOJO> response) {
		this.response = response;
	}
}
