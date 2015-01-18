package com.yl.sfphone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.yl.alarm.AlarmBean;
import com.yl.alarm.AlarmNotice;
import com.yl.http.CallBackHandler;
import com.yl.http.HttpManager;
import com.yl.utils.Constant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private SharedPreferences spf = null;
	private static String TAG = "MainActivity";
	public static String enKey = "";// 手机的设备码
	private Vibrator mVibrator = null;// 震动
	private MediaPlayer mgr = null;// 响铃
	private MapView map = null;// 地图
	private ImageButton img = null;// 定位按钮
	private ImageButton reg = null;// 注册监控终端按钮
	private ImageButton alarm = null;// 告警按钮
	private ImageButton cancleAlarm = null;// 取消告警
	private ToggleButton tb = null;// 监控设置按钮
	private ImageButton fw = null;// 使地图回到初始位置。
	private GraphicsLayer personLayer = null;// 当前人所在的位置
	private GraphicsLayer zdLayer = null;// 当前监控终端所在的位置。
	private PictureMarkerSymbol locationSymbol = null;// 人所在位置的样式
	private PictureMarkerSymbol zdSymbol = null;// 车所在位置的样式
	private boolean isAlarm = false;// 是否开启告警
	private boolean moning = true;// 是否进行实时监控
	private int count_net = 0;
	private String mSg = "";
	private Handler mHandler = new Handler();//
	// ---------定位相关-----------------
	// 通过network获取location
	private String networkProvider = LocationManager.NETWORK_PROVIDER;
	// 通过gps获取location
	private String GpsProvider = LocationManager.GPS_PROVIDER;
	private Location location = null;
	private LocationManager lm;
	private LocationListener locationListener;
	private OnClickListener clickListener;

	/**
	 * 提示信息
	 * */ 
	private void showMessage(String msg) {
		mSg = msg;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(getApplicationContext(), mSg,
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			// 地图
			map = (MapView) findViewById(R.id.map);
			// 设置地图的背景色
			map.setBackgroundColor(Color.WHITE);
			// 得到系统中的数据存储对象
			spf = getSharedPreferences(Constant.NAME, Activity.MODE_PRIVATE);
			@SuppressWarnings("deprecation")
			Object init = getLastNonConfigurationInstance();
			if (init != null) {
				map.restoreState((String) init);
			}
			clickListener = new ClickListener();
			// 获取设备编码
			enKey = spf.getString(Constant.PHOME_IMEI, "");
			// 当设备编码没有保存，那么就先通过系统获取并保存到本应用中
			if ("".equals(enKey)) {
				enKey = Build.class.getDeclaredField("SERIAL").get(Build.class)
						.toString();
				spf.edit().putString(Constant.PHOME_IMEI, enKey).commit();
			}
			// 添加一个图层到地图上，方便后面向图层上面添加位置信息
			personLayer = new GraphicsLayer(this);
			zdLayer = new GraphicsLayer(this);
			map.addLayer(personLayer);
			map.addLayer(zdLayer);
			// 定义一个显示样式
			locationSymbol = new PictureMarkerSymbol(this.getResources()
					.getDrawable(R.drawable.location));
			// 终端在地图上面的显示样式
			zdSymbol = new PictureMarkerSymbol(this.getResources().getDrawable(
					R.drawable.location));
			// 注册终端监控按钮
			reg = (ImageButton) findViewById(R.id.reg);
			// 注册监听
			reg.setOnClickListener(clickListener);
			// 定位按钮
			img = (ImageButton) findViewById(R.id.dw);
			// 开始定位
			initLocation(true);
			img.setOnClickListener(clickListener);
			// 手动告警按钮
			alarm = (ImageButton) findViewById(R.id.alarm);
			alarm.setOnClickListener(clickListener);
			cancleAlarm = (ImageButton) findViewById(R.id.cancle);
			cancleAlarm.setOnClickListener(clickListener);
			tb = (ToggleButton) findViewById(R.id.mon);
			tb.setOnCheckedChangeListener((OnCheckedChangeListener) clickListener);
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(10 * 1000 * 60);
							if (!isAlarm) {
								isAlarm = true;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
			fw = (ImageButton) findViewById(R.id.fw);
			fw.setOnClickListener(clickListener);
		} catch (Exception e) {
			showMessage("系统出错!");
			finish();
		}

	}

	public Object onRetainNonConfigurationInstance() {
		return map.retainState();
	}

	/**
	 * 从服务端获取数据并且将接收到的用户信息和车辆信息显示在地图上面
	 * */ 
	public void markOnMap(final GraphicsLayer gLayer,
			final GraphicsLayer zdLayer, final PictureMarkerSymbol personPms,
			PictureMarkerSymbol zdPms, final Location location,final boolean flag) {
		List<BasicNameValuePair> param = new ArrayList<BasicNameValuePair>();
		param.add(new BasicNameValuePair(Constant.PARAMETER, location
				.getLongitude() + ""));
		param.add(new BasicNameValuePair(Constant.PARAMETER, location
				.getLatitude() + ""));
		param.add(new BasicNameValuePair(Constant.PARAMETER, enKey));
		param.add(new BasicNameValuePair(Constant.METHOD, "dataExchange"));
		new Thread(new HttpManager(param, null, null, new CallBackHandler() {
			@SuppressWarnings("unchecked")
			@Override
			public void callBack(HttpResponse response) {
				if (response == null) {
					if (count_net < 1) {
						showMessage("请检查网络连接");
						count_net = count_net + 1;
					}
					return;
				}
				HttpEntity entity = response.getEntity();
				try {
					String temp = EntityUtils.toString(entity);
					if (temp == null || "".equals(temp)) {
						return;
					}
					Map<String, Map<String, Object>> result = null;
					try {
						result = (Map<String, Map<String, Object>>) JSON
								.parse(temp);
					} catch (Exception e) {
						e.printStackTrace();
						result = null;
						Log.e(TAG, "JSON转换异常");
						return;
					}
					double p_x = 0;
					try {
						p_x = Double.parseDouble(result.get("value").get(
								"phoneX")
								+ "");
					} catch (Exception e) {
						p_x = 0;
					}
					double p_y = 0;
					try {
						p_y = Double.parseDouble(result.get("value").get(
								"phoneY")
								+ "");
					} catch (Exception e) {
						p_y = 0;
					}
					Point p = new Point(p_x, p_y);
					Graphic g = new Graphic(p, personPms);
					gLayer.clear();
					gLayer.addGraphic(g);
					if(!flag){
						map.centerAt(p);
					}
						
					double t_x = 0;
					try {
						t_x = Double.parseDouble(result.get("value").get(
								"termX")
								+ "");
					} catch (Exception e) {
						t_x = 0;
					}
					double t_y = 0;
					try {
						t_y = Double.parseDouble(result.get("value").get(
								"termY")
								+ "");
					} catch (Exception e) {
						t_y = 0;
					}
					float speed = 0;
					try {
						speed = Float.parseFloat(result.get("value").get(
								"speed")
								+ "");
					} catch (Exception e) {
						speed = 0;
					}
					float dis = 0;
					try {
						dis = Float.parseFloat(result.get("value").get(
								"string_4")
								+ "");
					} catch (Exception e) {
						dis = 0;
					}

					AlarmBean alarmBean = new AlarmBean(t_x, t_y, speed, dis);
					Point zp = new Point(t_x, t_y);
					Graphic zg = new Graphic(zp, zdSymbol);
					zdLayer.clear();
					zdLayer.addGraphic(zg);
					// 当isAlarm为false的时候才进行报警阶段处理
					if (!isAlarm) {
						isAlarm = true;
						// 当确定为报警后开启手机的震动+闹铃
						boolean flag = AlarmNotice.compare(alarmBean, location);
						if (flag) {
							startShake();
							playSound();
							List<BasicNameValuePair> param = new ArrayList<BasicNameValuePair>();
							param.add(new BasicNameValuePair(
									Constant.PARAMETER, enKey));
							param.add(new BasicNameValuePair(
									Constant.PARAMETER, "1"));
							param.add(new BasicNameValuePair(Constant.METHOD,
									"cancelAlarm"));
							// 上传到服务器，告警信息
							new Thread(new HttpManager(param, null, null, null))
									.start();
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
					if(lm!=null)
						lm.removeUpdates(locationListener);
				}
			}
		})).start();
	}

	/**
	 *  震动
	 * */
	public void startShake() {
		mVibrator = (Vibrator) getApplication().getSystemService(
				Service.VIBRATOR_SERVICE);
		mVibrator.vibrate(Constant.VIBRATE, 0);
	}

	/**
	 *停止震动 
	 * */ 
	public void stopShake() {
		if (mVibrator != null)
			mVibrator.cancel();
	}

	/**
	 *  响铃，如果使用静音模式将无法响铃
	 * */
	public void playSound() {
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		try {
			if (mgr == null) {
				mgr = new MediaPlayer();
				mgr.setDataSource(MainActivity.this, uri);
				mgr.setLooping(true); // 循环播放
				mgr.prepare();
				mgr.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 停止响铃
	 * */ 
	public void stopPlaySound() {
		if (mgr != null) {
			mgr.pause();
			mgr = null;
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 创建退出对话框
			AlertDialog isExit = new AlertDialog.Builder(this).create();
			// 设置对话框标题
			isExit.setTitle("系统提示");
			// 设置对话框消息
			isExit.setMessage("确定要退出吗？");
			// 添加选择按钮并注册监听
			isExit.setButton("确定",(DialogInterface.OnClickListener)clickListener );
			isExit.setButton2("取消", (DialogInterface.OnClickListener)clickListener);
			// 显示对话框
			isExit.show();
		}

		return false;

	}

	/**
	 *  获取location对象
	 * */
	private void initLocation(boolean flag) {
		// 获得系统及服务的 LocationManager 对象 这个代码就这么写 不用考虑
		if (lm == null)
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// 首先检测 通过network 能否获得location对象
		if (startLocation(GpsProvider,flag)) {
			updateLocation(location,flag);
		} else
		// 通过gps 能否获得location对象
		if (startLocation(networkProvider,flag)) {
			updateLocation(location,flag);
		} else {
			// 如果上面两种方法都不能获得location对象 则显示下列信息
			showMessage("没有打开GPS设备");
		}
	}

	/**
	 * 通过参数 获取Location对象 如果Location对象为空 则返回 true 并且赋值给全局变量 location 如果为空 返回false
	 * 不赋值给全局变量location
	 * 
	 * @param provider
	 * @param mContext
	 * @param flag 
	 * @return
	 */
	private boolean startLocation(String provider,final boolean flag) {
		Location location = lm.getLastKnownLocation(provider);

		// 位置监听器
		locationListener = new LocationListener() {
			// 当位置改变时触发
			@Override
			public void onLocationChanged(Location location) {
				if (moning) {
					Log.i(TAG, "正在更新位置信息。。。");
					updateLocation(location,flag);
				}

			}

			// Provider失效时触发
			@Override
			public void onProviderDisabled(String arg0) {
				System.out.println(arg0);
			}

			// Provider可用时触发
			@Override
			public void onProviderEnabled(String arg0) {
				System.out.println(arg0);
			}

			// Provider状态改变时触发
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				System.out.println("onStatusChanged");
			}
		};
		if(flag){
			// 3秒更新一次，忽略位置变化
			lm.requestLocationUpdates(provider, 1000, 0, locationListener);
		}
		

		// 如果Location对象为空 则返回 true 并且赋值给全局变量 location
		// 如果为空 返回false 不赋值给全局变量location
		if (location != null) {
			this.location = location;
			return true;
		}
		return false;

	}

	/**
	 * 更新位置信息 
	 * */ 
	private void updateLocation(Location location,boolean flag) {
		if (location != null) {
			markOnMap(personLayer, zdLayer, locationSymbol, zdSymbol, location,flag);
		} else {
			System.out.println("没有获取到定位对象Location");
		}
	}

	protected void onDestroy() {
		// 当这个activity销毁时 在这里注销location的监听
		lm.removeUpdates(locationListener);
		stopPlaySound();
		stopShake();
		finish();
		super.onDestroy();
	}

	/**
	 * 事件处理
	 * 
	 * */
	class ClickListener implements OnClickListener, OnCheckedChangeListener,DialogInterface.OnClickListener {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.fw:
				map.setExtent(Constant.ENVELOP);
				map.centerAt(Constant.ENVELOP.getCenter());
				break;
			case R.id.reg:
				Intent i = new Intent(MainActivity.this, RegistSb.class);
				i.putExtra(Constant.ZD_ID, enKey);
				startActivityForResult(i, 0);
				break;
			case R.id.dw:
				showMessage("正在定位....");
				initLocation(false);
				break;
			case R.id.alarm:
				List<BasicNameValuePair> param = new ArrayList<BasicNameValuePair>();
				param.add(new BasicNameValuePair(Constant.PARAMETER, enKey));
				param.add(new BasicNameValuePair(Constant.PARAMETER, "1"));
				param.add(new BasicNameValuePair(Constant.METHOD, "cancelAlarm"));
				// 上传到服务器，告警信息
				new Thread(new HttpManager(param, null, null, null)).start();
				break;
			case R.id.cancle:
				List<BasicNameValuePair> param1 = new ArrayList<BasicNameValuePair>();
				param1.add(new BasicNameValuePair(Constant.PARAMETER, enKey));
				param1.add(new BasicNameValuePair(Constant.PARAMETER, "0"));
				param1.add(new BasicNameValuePair(Constant.METHOD,
						"cancelAlarm"));
				// 上传到服务器，告警信息
				new Thread(new HttpManager(param1, null, null, null)).start();
				isAlarm = false;
				stopPlaySound();
				stopShake();
				break;
			case R.id.mon:
				break;
			default:
				break;
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton b, boolean f) {
			int id = b.getId();
			switch (id) {
			case R.id.mon:
				boolean flag = tb.isChecked();
				if (flag) {
					showMessage("实时监控已打开...");
					moning = true;
				} else {
					showMessage("实时监控已关闭...");
					moning = false;
				}
				break;
			default:
				break;
			}
		}

		@Override
		public void onClick(DialogInterface arg0, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
				stopPlaySound();
				stopShake();
				if(lm!=null){
					lm.removeUpdates(locationListener);
				}
				finish();
				break;
			case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
				break;
			default:
				break;
			}
			
		}
	}
}