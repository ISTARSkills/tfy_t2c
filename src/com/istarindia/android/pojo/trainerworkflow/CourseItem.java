/**
 * 
 */
package com.istarindia.android.pojo.trainerworkflow;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author mayank
 *
 */
public class CourseItem {

	Integer itemId;
	String itemName;
	Integer orderId;
	String itemType;
	String itemUrl;
	public CourseItem() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@XmlAttribute(name = "item_id", required = false)
	public Integer getItemId() {
		return itemId;
	}
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}
	
	@XmlAttribute(name = "item_name", required = false)
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
	@XmlAttribute(name = "order_id", required = false)
	public Integer getOrderId() {
		return orderId;
	}
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	@XmlAttribute(name = "item_type", required = false)
	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	@XmlAttribute(name = "item_url", required = false)
	public String getItemUrl() {
		return itemUrl;
	}

	public void setItemUrl(String itemUrl) {
		this.itemUrl = itemUrl;
	}
	
	
	
}
