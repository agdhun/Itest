package com.darever.itest;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener {

	// 控件变量定义
		private TextView tv;
		private Button co, ch, tem, hum;

		private String TAG = "===Client===";
	    Handler mhandler;
	    Handler mhandlerSend;
	    boolean isRun = true;
	    EditText edtsendms;
	    Button btnsend;
	    private String sendstr = "";
	    SharedPreferences sp;
	    Button btnSetting;
	    private Context ctx;
	    Socket socket;
	    PrintWriter out;
	    BufferedReader in;
	    SocThread socketThread;
		
		private long exitTime = 0; 

		private String sd, s2;
		private int s1;
		private boolean sn = true;
		
		private static String HUM = "湿度是：";
		private static String TEM = "温度是：";
		private static String CO = "CO相对浓度是:";
		private static String CH4 = "甲烷相对浓度是：";
		private static String UPDATING = "正在更新数据...";
		private static String UPDATED = "更新完毕";
		private static String FAILED = "更新失败";
		private static String hunit = "rh";
		private static String tuint = "\u2103";
		private static String cunit = "ppm";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		ctx = MainActivity.this;
		Test();

		tv = (TextView) findViewById(R.id.textView1);
		co = (Button) findViewById(R.id.co);
		ch = (Button) findViewById(R.id.ch);
		tem = (Button) findViewById(R.id.tem);
		hum = (Button) findViewById(R.id.hum);
		
		mhandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				try {
					Log.i(TAG, "mhandler接收到msg=" + msg.what);
					if (msg.obj != null) {
						String s = msg.obj.toString();
						if (s.trim().length() > 0) {
							Log.i(TAG, "mhandler接收到obj=" + s);
							Log.i(TAG, "开始更新UI");
							if(sd.equals("t"))
							{
								tv.setText(TEM + s +tuint);
							}
							if(sd.equals("h"))
							{
								tv.setText(HUM + s + hunit);
							}
							if(sd.equals("o"))
							{
								if(sn){
									s1 = Integer.parseInt(s) ;
									sn = false;
								}
								else{
									s2 = String.valueOf((Integer.parseInt(s) - s1) * 20);
									tv.setText(CO + s2 + cunit);
									sn = true;
								}
							}
							if(sd.equals("c"))
							{
								if(sn){
									s1 = Integer.parseInt(s) ;
									sn = false;
								}
								else{
									s2 = String.valueOf((Integer.parseInt(s) - s1) * 2);
									tv.setText(CH4 + s2 + cunit);
									sn = true;
								}
							}
							Log.i(TAG, "更新UI完毕");
							Toast.makeText(getApplicationContext(), UPDATED,
								     Toast.LENGTH_SHORT).show();
						} else {
							Log.i(TAG, "没有数据返回不更新");
							Toast.makeText(getApplicationContext(), FAILED,
								     Toast.LENGTH_SHORT).show();
						}
					}
				} catch (Exception ee) {
					Log.i(TAG, "加载过程出现异常");
					ee.printStackTrace();
				}
			}
		};
		mhandlerSend = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					MyLog.i(TAG, "mhandlerSend接收到msg.what=" + msg.what);
					String s = msg.obj.toString();
					if (msg.what == 1) {
						Toast.makeText(getApplicationContext(), "发送成功",
							     Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "发送失败",
							     Toast.LENGTH_SHORT).show();
					}
				} catch (Exception ee) {
					MyLog.i(TAG, "加载过程出现异常");
					ee.printStackTrace();
				}
			}
		};
		startSocket();
		co.setOnClickListener(this);
		ch.setOnClickListener(this);
		tem.setOnClickListener(this);
		hum.setOnClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void startSocket() {
		socketThread = new SocThread(mhandler, mhandlerSend, ctx);
		socketThread.start();
	}

	private void stopSocket() {
		socketThread.isRun = false;
		socketThread.close();
		socketThread = null;
		Log.i(TAG, "Socket已终止");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.e(TAG, "start onStart~~~");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.e(TAG, "start onRestart~~~");
		Test();
		startSocket();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "start onResume~~~");
		Test();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG, "start onPause~~~");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.e(TAG, "start onStop~~~");
		stopSocket();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "start onDestroy~~~");

	}

	// ///////////////////////////////

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		// 向服务器端发送消息
		switch (v.getId()) {
		case R.id.tem:// 温度指令
			sd = "t";
			break;
		case R.id.hum:// 湿度指令
			sd = "h";
			break;
		case R.id.ch:// 甲烷指令
			sd = "c";
			break;
		case R.id.co:// co指令
			sd = "o";
			break;
		default:
			sd = "*";
			break;
		}
		Log.i(TAG, "准备发送数据");
		Toast.makeText(getApplicationContext(), UPDATING,
			     Toast.LENGTH_SHORT).show();
		sendstr = sd.trim();
		socketThread.Send(sendstr);

	}

	private void Test() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// 获取状态
		State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		// 判断wifi已连接的条件
		if (wifi == State.CONNECTED) {
		} else// 如果WIFI未连接或未打开，跳到系统WLAN设置界面
		{
			Intent intent = new Intent();
			intent.setAction("android.settings.WIFI_SETTINGS");
			startActivity(intent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int item_id = item.getItemId();

		switch (item_id) {
		case R.id.setting:
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, Setting.class);
			startActivity(intent);
			break;
		case R.id.about:
			Intent intent_about = new Intent();
			intent_about.setClass(MainActivity.this, About.class);
			startActivity(intent_about);
			break;
		default:
			return false;
		}
		return true;

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
