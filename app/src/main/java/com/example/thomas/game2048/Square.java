package com.example.thomas.game2048;


import android.graphics.Color;

public class Square {
	
	private int value;
	private int row;
	private int column;

	private boolean canMerge;

	public Square(int value, int row, int column){
		this.value = value;
		this.row = row;
		this.column = column;
		this.canMerge = true;
	}

	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getColor() {
		switch(value){
			case 0:
				return Color.rgb(204, 192, 179);
			case 2:
				return Color.rgb(238, 228, 218);
			case 4:
				return Color.rgb(237, 224, 200);
			case 8:
				return Color.rgb(240, 177, 124);
			case 16:
				return Color.rgb(245, 148, 99);
			case 32:
				return Color.rgb(245, 124, 95);
			case 64:
				return Color.rgb(248, 85, 59);
			case 128:
				return Color.rgb(236, 204, 117);
			case 256:
				return Color.rgb(237, 204, 97);
			case 512:
				return Color.rgb(237, 200, 80);
			case 1024:
				return Color.rgb(226, 185, 19);
			default:
				return Color.rgb(236, 197, 0);
		}
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColumn() {
		return column;
	}
	
	public boolean isEmpty(){
		return value == 0;
	}

	public boolean isCanMerge() {
		return canMerge;
	}

	public void setCanMerge(boolean canMerge) {
		this.canMerge = canMerge;
	}
}
