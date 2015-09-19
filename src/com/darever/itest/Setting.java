package com.darever.itest;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Setting extends Activity {
	private Button btnsave, btnreset;
	private EditText edtip;
	private EditText edtport;
	SharedPreferences sp;
	private String TAG="=Setting=";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		btnsave = (Button) findViewById(R.id.save);
		btnreset = (Button) findViewById(R.id.reset);
		edtip = (EditText) findViewById(R.id.editText1);
		edtport = (EditText) findViewById(R.id.editText2);
		sp = this.getSharedPreferences("SP", MODE_PRIVATE);
		edtip.setText(sp.getString("ipstr", ""));
		edtport.setText(sp.getString("port", ""));

		btnsave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG,"开始修改");
				String ip = edtip.getText().toString();//
				String port = edtport.getText().toString();//
				Editor editor = sp.edit();
				editor.putString("ipstr", ip);
				editor.putString("port", port);
				editor.commit();//保存新数据
				Log.i(TAG, "保存成功"+sp.getString("ipstr", "")+";"+sp.getString("port", ""));
				finish();
				System.exit(0);
			}
		});
		
		btnreset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				edtip.setText("");
				edtport.setText("");
			}
		});
	}
}
