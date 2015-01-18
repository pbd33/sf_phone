package com.yl.alarm;


import android.location.Location;
public class AlarmNotice {
	
	//比较当前监控终端的位置与上一次得到的监控终端的位置，如果上次的位置为null
	public static boolean compare(AlarmBean bean,Location location){
//		if(bean.getSpeed()<1){
//			return false;
//		}else {
//			float result = bean.getSpeed()-location.getSpeed();
//			if(Math.abs(result)<5&&bean.getDis()<1000){
//				return false;
//			}else{
//				return true;
//			}
//		}
		return true;
	}
}
