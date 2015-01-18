package com.yl.http;

import org.apache.http.HttpResponse;

public interface CallBackHandler {
	public void callBack(HttpResponse response);
	
}
