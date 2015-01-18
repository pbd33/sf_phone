package com.yl.http;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


import android.app.Activity;
import android.widget.Toast;

import com.yl.utils.Constant;

public class HttpManager implements Runnable {
	private List<BasicNameValuePair> p;
	private String url;
	private String m;
	private CallBackHandler c;
	public HttpManager(List<BasicNameValuePair> param, String baseUrl,
			String method, CallBackHandler callBack) {
		this.p = param;
		if (baseUrl == null || "".equals(baseUrl)) {
			baseUrl = Constant.BASE_URL;
		}
		this.url = baseUrl;
		if (method == null || "".equals(method)) {
			method = "post";
		}
		this.m = method;
		this.c = callBack;
	}

	@Override
	public void run() {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = null;
		if ("post".equalsIgnoreCase(m)) {
			try {
				HttpPost postMethod = new HttpPost(url);
				postMethod.setEntity(new UrlEncodedFormEntity(p, "utf-8"));
				response = httpClient.execute(postMethod);
			} catch (Exception e) {
			}

		} else if ("get".equalsIgnoreCase(m)) {
			try {
				String param = URLEncodedUtils.format(p, "UTF-8");
				HttpGet getMethod = new HttpGet(url + "?" + param);
				response = httpClient.execute(getMethod);
			} catch (Exception e) {
			}
		} else {
			throw new RuntimeException("未定义方式");
		}
		if(c!=null)
			c.callBack(response);

	}

}
