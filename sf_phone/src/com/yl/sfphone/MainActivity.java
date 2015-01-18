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
	public static String enKey = "";// �ֻ����豸��
	private Vibrator mVibrator = null;// ��
	private MediaPlayer mgr = null;// ����
	private MapView map = null;// ��ͼ
	private ImageButton img = null;// ��λ��ť
	private ImageButton reg = null;// ע�����ն˰�ť
	private ImageButton alarm = null;// �澯��ť
	private ImageButton cancleAlarm = null;// ȡ���澯
	private ToggleButton tb = null;// ������ð�ť
	private ImageButton fw = null;// ʹ��ͼ�ص���ʼλ�á�
	private GraphicsLayer personLayer = null;// ��ǰ�����ڵ�λ��
	private GraphicsLayer zdLayer = null;// ��ǰ����ն����ڵ�λ�á�
	private PictureMarkerSymbol locationSymbol = null;// ������λ�õ���ʽ
	private PictureMarkerSymbol zdSymbol = null;// ������λ�õ���ʽ
	private boolean isAlarm = false;// �Ƿ����澯
	private boolean moning = true;// �Ƿ����ʵʱ���
	private int count_net = 0;
	private String mSg = "";
	private Handler mHandler = new Handler();//
	// ---------��λ���-----------------
	// ͨ��network��ȡlocation
	private String networkProvider = LocationManager.NETWORK_PROVIDER;
	// ͨ��gps��ȡlocation
	private String GpsProvider = LocationManager.GPS_PROVIDER;
	private Location location = null;
	private LocationManager lm;
	private LocationListener locationListener;
	private OnClickListener clickListener;

	/**
	 * ��ʾ��Ϣ
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
			// ��ͼ
			map = (MapView) findViewById(R.id.map);
			// ���õ�ͼ�ı���ɫ
			map.setBackgroundColor(Color.WHITE);
			// �õ�ϵͳ�е����ݴ洢����
			spf = getSharedPreferences(Constant.NAME, Activity.MODE_PRIVATE);
			@SuppressWarnings("deprecation")
			Object init = getLastNonConfigurationInstance();
			if (init != null) {
				map.restoreState((String) init);
			}
			clickListener = new ClickListener();
			// ��ȡ�豸����
			enKey = spf.getString(Constant.PHOME_IMEI, "");
			// ���豸����û�б��棬��ô����ͨ��ϵͳ��ȡ�����浽��Ӧ����
			if ("".equals(enKey)) {
				enKey = Build.class.getDeclaredField("SERIAL").get(Build.class)
						.toString();
				spf.edit().putString(Constant.PHOME_IMEI, enKey).commit();
			}
			// ���һ��ͼ�㵽��ͼ�ϣ����������ͼ���������λ����Ϣ
			personLayer = new GraphicsLayer(this);
			zdLayer = new GraphicsLayer(this);
			map.addLayer(personLayer);
			map.addLayer(zdLayer);
			// ����һ����ʾ��ʽ
			locationSymbol = new PictureMarkerSymbol(this.getResources()
					.getDrawable(R.drawable.location));
			// �ն��ڵ�ͼ�������ʾ��ʽ
			zdSymbol = new PictureMarkerSymbol(this.getResources().getDrawable(
					R.drawable.location));
			// ע���ն˼�ذ�ť
			reg = (ImageButton) findViewById(R.id.reg);
			// ע�����
			reg.setOnClickListener(clickListener);
			// ��λ��ť
			img = (ImageButton) findViewById(R.id.dw);
			// ��ʼ��λ
			initLocation(true);
			img.setOnClickListener(clickListener);
			// �ֶ��澯��ť
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
			showMessage("ϵͳ����!");
			finish();
		}

	}

	public Object onRetainNonConfigurationInstance() {
		return map.retainState();
	}

	/**
	 * �ӷ���˻�ȡ���ݲ��ҽ����յ����û���Ϣ�ͳ�����Ϣ��ʾ�ڵ�ͼ����
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
						showMessage("������������");
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
						Log.e(TAG, "JSONת���쳣");
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
					// ��isAlarmΪfalse��ʱ��Ž��б����׶δ���
					if (!isAlarm) {
						isAlarm = true;
						// ��ȷ��Ϊ���������ֻ�����+����
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
							// �ϴ������������澯��Ϣ
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
	 *  ��
	 * */
	public void startShake() {
		mVibrator = (Vibrator) getApplication().getSystemService(
				Service.VIBRATOR_SERVICE);
		mVibrator.vibrate(Constant.VIBRATE, 0);
	}

	/**
	 *ֹͣ�� 
	 * */ 
	public void stopShake() {
		if (mVibrator != null)
			mVibrator.cancel();
	}

	/**
	 *  ���壬���ʹ�þ���ģʽ���޷�����
	 * */
	public void playSound() {
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		try {
			if (mgr == null) {
				mgr = new MediaPlayer();
				mgr.setDataSource(MainActivity.this, uri);
				mgr.setLooping(true); // ѭ������
				mgr.prepare();
				mgr.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * ֹͣ����
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
			// �����˳��Ի���
			AlertDialog isExit = new AlertDialog.Builder(this).create();
			// ���öԻ������
			isExit.setTitle("ϵͳ��ʾ");
			// ���öԻ�����Ϣ
			isExit.setMessage("ȷ��Ҫ�˳���");
			// ���ѡ��ť��ע�����
			isExit.setButton("ȷ��",(DialogInterface.OnClickListener)clickListener );
			isExit.setButton2("ȡ��", (DialogInterface.OnClickListener)clickListener);
			// ��ʾ�Ի���
			isExit.show();
		}

		return false;

	}

	/**
	 *  ��ȡlocation����
	 * */
	private void initLocation(boolean flag) {
		// ���ϵͳ������� LocationManager ���� ����������ôд ���ÿ���
		if (lm == null)
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// ���ȼ�� ͨ��network �ܷ���location����
		if (startLocation(GpsProvider,flag)) {
			updateLocation(location,flag);
		} else
		// ͨ��gps �ܷ���location����
		if (startLocation(networkProvider,flag)) {
			updateLocation(location,flag);
		} else {
			// ����������ַ��������ܻ��location���� ����ʾ������Ϣ
			showMessage("û�д�GPS�豸");
		}
	}

	/**
	 * ͨ������ ��ȡLocation���� ���Location����Ϊ�� �򷵻� true ���Ҹ�ֵ��ȫ�ֱ��� location ���Ϊ�� ����false
	 * ����ֵ��ȫ�ֱ���location
	 * 
	 * @param provider
	 * @param mContext
	 * @param flag 
	 * @return
	 */
	private boolean startLocation(String provider,final boolean flag) {
		Location location = lm.getLastKnownLocation(provider);

		// λ�ü�����
		locationListener = new LocationListener() {
			// ��λ�øı�ʱ����
			@Override
			public void onLocationChanged(Location location) {
				if (moning) {
					Log.i(TAG, "���ڸ���λ����Ϣ������");
					updateLocation(location,flag);
				}

			}

			// ProviderʧЧʱ����
			@Override
			public void onProviderDisabled(String arg0) {
				System.out.println(arg0);
			}

			// Provider����ʱ����
			@Override
			public void onProviderEnabled(String arg0) {
				System.out.println(arg0);
			}

			// Provider״̬�ı�ʱ����
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				System.out.println("onStatusChanged");
			}
		};
		if(flag){
			// 3�����һ�Σ�����λ�ñ仯
			lm.requestLocationUpdates(provider, 1000, 0, locationListener);
		}
		

		// ���Location����Ϊ�� �򷵻� true ���Ҹ�ֵ��ȫ�ֱ��� location
		// ���Ϊ�� ����false ����ֵ��ȫ�ֱ���location
		if (location != null) {
			this.location = location;
			return true;
		}
		return false;

	}

	/**
	 * ����λ����Ϣ 
	 * */ 
	private void updateLocation(Location location,boolean flag) {
		if (location != null) {
			markOnMap(personLayer, zdLayer, locationSymbol, zdSymbol, location,flag);
		} else {
			System.out.println("û�л�ȡ����λ����Location");
		}
	}

	protected void onDestroy() {
		// �����activity����ʱ ������ע��location�ļ���
		lm.removeUpdates(locationListener);
		stopPlaySound();
		stopShake();
		finish();
		super.onDestroy();
	}

	/**
	 * �¼�����
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
				showMessage("���ڶ�λ....");
				initLocation(false);
				break;
			case R.id.alarm:
				List<BasicNameValuePair> param = new ArrayList<BasicNameValuePair>();
				param.add(new BasicNameValuePair(Constant.PARAMETER, enKey));
				param.add(new BasicNameValuePair(Constant.PARAMETER, "1"));
				param.add(new BasicNameValuePair(Constant.METHOD, "cancelAlarm"));
				// �ϴ������������澯��Ϣ
				new Thread(new HttpManager(param, null, null, null)).start();
				break;
			case R.id.cancle:
				List<BasicNameValuePair> param1 = new ArrayList<BasicNameValuePair>();
				param1.add(new BasicNameValuePair(Constant.PARAMETER, enKey));
				param1.add(new BasicNameValuePair(Constant.PARAMETER, "0"));
				param1.add(new BasicNameValuePair(Constant.METHOD,
						"cancelAlarm"));
				// �ϴ������������澯��Ϣ
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
					showMessage("ʵʱ����Ѵ�...");
					moning = true;
				} else {
					showMessage("ʵʱ����ѹر�...");
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
			case AlertDialog.BUTTON_POSITIVE:// "ȷ��"��ť�˳�����
				stopPlaySound();
				stopShake();
				if(lm!=null){
					lm.removeUpdates(locationListener);
				}
				finish();
				break;
			case AlertDialog.BUTTON_NEGATIVE:// "ȡ��"�ڶ�����ťȡ���Ի���
				break;
			default:
				break;
			}
			
		}
	}
}