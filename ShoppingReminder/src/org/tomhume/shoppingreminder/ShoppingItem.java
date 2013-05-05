package org.tomhume.shoppingreminder;

public class ShoppingItem {
	private String itemCode;
	private String itemName;
	
	public ShoppingItem(String c, String n) {
		this.itemCode = c;
		this.itemName = n;
	}
	
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
	
}
