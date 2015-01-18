package com.yl.alarm;

public class AlarmBean {
	private double x;
	private double y;
	private float speed;
	private float dis;
	public AlarmBean(double t_x, double t_y,float sd,float d) {
		this.x = t_x;
		this.y = t_y; 
		this.speed = sd;
		this.dis = d;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public float getSpeed() {
		return speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	public float getDis() {
		return dis;
	}
	public void setDis(float dis) {
		this.dis = dis;
	}
	
	
	
}
