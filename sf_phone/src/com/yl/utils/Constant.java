package com.yl.utils;

import com.esri.core.geometry.Envelope;

public class Constant {
	public static final String PHOME_IMEI = "enKey";//手机设备码
	public static final String ZD_ID = "zdKey";//终端编码
	public static final String NAME = "SFPHONE";
	public static final long[] VIBRATE= new long[]{1000,1000,1000,1000,1000};
	public static final String PARAMETER = "parameter";
	public static final String METHOD = "method";
	public static final String BASE_URL = "http://192.168.10.8:8080/address/MobileAndroidServlet";
	public static final int VERSION = 1;
	public static final Envelope ENVELOP = new Envelope(1.840143345682222E7,
			3387776.6804067073, 1.8421721842934374E7,
			3399322.373820019);

}
