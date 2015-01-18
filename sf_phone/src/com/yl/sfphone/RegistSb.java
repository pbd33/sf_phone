package com.yl.sfphone;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;

import com.yl.http.CallBackHandler;
import com.yl.http.HttpManager;
import com.yl.utils.Constant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegistSb extends Activity{
//	private String TAG = "RegistSb";
	EditText sbm;
	Button btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zd_setting);
		sbm = (EditText) findViewById(R.id.sbm);
		btn = (Button) findViewById(R.id.reg);
		Intent intent = getIntent();
		String oldZdKey = intent.getStringExtra(Constant.ZD_ID);
		sbm.setText(oldZdKey);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String sb = sbm.getText().toString().trim();
				if("".equals(sb)){
					Toast.makeText(RegistSb.this, "请输入设备码", Toast.LENGTH_SHORT).show();
				}else{
					getSharedPreferences(Constant.NAME, Activity.MODE_PRIVATE).edit().putString(Constant.ZD_ID, sb).commit();
					new Regist().execute();
				}
			}
		});
	}

	
	
	class Regist extends AsyncTask<Integer,Integer,Integer>{
		private ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = new ProgressDialog(RegistSb.this);
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.setCancelable(true);
			mDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					Regist.this.cancel(true);
				}
			});
			mDialog.setMessage("正在注册...");
			mDialog.show();
		}
		@Override
		protected Integer doInBackground(Integer... arg0) {
			String sb = sbm.getText().toString().trim();
			List<BasicNameValuePair> param = new ArrayList<BasicNameValuePair>(); 
			param.add(new BasicNameValuePair("enKey", MainActivity.enKey));
			param.add(new BasicNameValuePair("code", sb));
			
			new Thread(new HttpManager(param, null, null, new CallBackHandler() {
				@Override
				public void callBack(HttpResponse response) {
//					mDialog.hide();
				}
			})).start();
			return null;
		}
		
		
	}
}


