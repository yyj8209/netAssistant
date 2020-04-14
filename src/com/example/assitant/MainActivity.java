
package com.example.assitant;

import android.widget.Button;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.view.View.OnClickListener;

public class MainActivity extends Activity{
	private Button btnclient;
	private Button btnserver;
@Override
protected void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
	setContentView(R.layout.choosedemo);
	Button btnclient=(Button)findViewById(R.id.id_btn_client);
	btnclient.setOnClickListener(new OnClickListener(){
	@Override
	public void onClick(View v){
		Intent intent=new Intent(MainActivity.this,clientActivity.class);
		startActivity(intent);

	}
	});
	
Button btnserver=(Button)findViewById(R.id.id_btn_server);
btnserver.setOnClickListener(new OnClickListener(){
@Override
public void onClick(View v){
	Intent intent=new Intent(MainActivity.this,serverActivity.class);
	startActivity(intent);

}
});
}	
}
	
	
	
	
	
	
	
