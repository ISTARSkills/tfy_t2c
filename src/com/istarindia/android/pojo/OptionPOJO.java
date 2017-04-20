package com.istarindia.android.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.internal.txw2.annotation.XmlCDATA;

@XmlRootElement(name = "option")
public class OptionPOJO {

	private Integer id;
	private String text;
	
	public OptionPOJO(){
		
	}
	
	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlCDATA
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}	
}
