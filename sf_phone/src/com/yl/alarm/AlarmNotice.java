package com.yl.alarm;


import android.location.Location;
public class AlarmNotice {
	
	//�Ƚϵ�ǰ����ն˵�λ������һ�εõ��ļ���ն˵�λ�ã�����ϴε�λ��Ϊnull
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
