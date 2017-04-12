package com.istarindia.android.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "questionResponse")
public class QuestionResponsePOJO {

	private Integer questionId;
	private List<Integer> options;
	private Integer duration;
	
	@XmlAttribute(name = "questionId", required = false)
	public Integer getQuestionId() {
		return questionId;
	}
	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}
	
	@XmlAttribute(name = "options", required = false)
	public List<Integer> getOptions() {
		return options;
	}
	public void setOptions(List<Integer> options) {
		this.options = options;
	}
	
	@XmlAttribute(name = "duration", required = false)
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
