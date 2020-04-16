package com.example.assitant;
import java.io.BufferedReader; //import用来引入非本包的类，io是java基础的包�?
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;   //此抽象类表示字节输入流的�?��类的超类
import java.io.OutputStream;  
import java.io.OutputStreamWriter; //要输出的东西通过输出流输出到目的�?
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset; //声明字符集包�?


//引用安卓的类
import android.app.Activity;
import android.os.Bundle;  //用来传�?参数的类 
import android.os.Handler; //消息处理�?
import android.os.Looper;  //循环�?
import android.os.Message; //消息�?
import android.util.Log;   //调试颜色 log.v为黑色，log.d为蓝色，log.i为绿色，log.w橙色�?                                         

          //log.e为红�?
import android.view.View;

import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;  //弹出框，提示信息�?


//创建�?��MainActivity类，该类继承了TabActivity类，并实现了按钮监听事件
public class clientActivity extends Activity implements
		OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clientmain);
	    init();	
	}
        //声明变量，private声明的变量，只有在本类中可以访问
	private EditText edtIP;
	private EditText edtPort;
        EditText edtSend;
	private EditText edtReceiver;

	private Button btnConn;
	private Button btnSend;
    private Button btnClean;
    
  

	private String tag = "MainActivity";

	InputStream in;
	PrintWriter printWriter = null;
	BufferedReader reader;

	Socket mSocket = null;
	public boolean isConnected = false;

	private MyHandler myHandler;

	Thread receiverThread;


        //创建�?��实现Runnale接口的私有MyRecetverRunnable线程
	private class MyReceiverRunnable implements Runnable {

		public void run() {
                  
               //线程要执行的操作
			while (true) {

				Log.i(tag, "---->>client receive....");
				if (isConnected) {
					if (mSocket != null && mSocket.isConnected()) {

						String result = readFromInputStream(in); //通过从输入流读取数据，返回给result

						try {
							if (!result.equals("")) {

								Message msg = new Message();  //获取�?��消息
								msg.what = 1;                 //设置message的what属�?的�?
								Bundle data = new Bundle();
								data.putString("msg", result);
								msg.setData(data);
								myHandler.sendMessage(msg);   //发�?消息
							}

						} catch (Exception e) //捕获异常，要做如下处�?
                                                       {
							Log.e(tag, "--->>read failure!" + e.toString());
						}
					}
				}
				try {
					Thread.sleep(100L);    //线程休眠
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}

	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			receiverData(msg.what);
			if (msg.what == 1) {
				String result = msg.getData().get("msg").toString();
				edtReceiver.append(result);
			    edtReceiver.append("\n");
			}
		}
	}




	private void init() {

		edtIP = (EditText) this.findViewById(R.id.id_edt_inputIP);
		edtPort = (EditText) this.findViewById(R.id.id_edt_inputport);
		edtSend = (EditText) this.findViewById(R.id.id_edt_sendArea);
		edtReceiver = (EditText) findViewById(R.id.id_edt_jieshou);

		
		
		btnSend = (Button) findViewById(R.id.id_btn_send);
		btnSend.setOnClickListener(this);
		btnConn = (Button) findViewById(R.id.id_btn_connClose);
		btnConn.setOnClickListener(this);
        btnClean= (Button) findViewById(R.id.id_btn_clean);
        btnClean.setOnClickListener(this);
        
		myHandler = new MyHandler();
	}
  
	public String readFromInputStream(InputStream in) {
		int count = 0;
		byte[] inDatas = null;
		try {
			while (count == 0) {
				count = in.available();
			}
			inDatas = new byte[count];
			in.read(inDatas);
			return new String(inDatas, "gb2312");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	//监听按钮事件，判断是那个按钮按下，随后执行相应的线程

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		
		case R.id.id_btn_connClose:

			connectThread();
			break;

		case R.id.id_btn_send:

			sendData();
			break;

                case R.id.id_btn_clean:
                        edtSend = (EditText) this.findViewById(R.id.id_edt_sendArea);
                        edtSend.setText("");
                        break;

		}
	}


	private void receiverData(int flag) {

		if (flag == 2) {
			
			receiverThread = new Thread(new MyReceiverRunnable());
			receiverThread.start();

			Log.i(tag, "--->>socket 可以通信!");
			btnConn.setText("已连接");

			isConnected = true;
			
		}

	}

       //数据发送
	private void sendData() {

		
		try {
			final String context = edtSend.getText().toString();

			if (printWriter == null || context == null) {

				if (printWriter == null) {
					showInfo("wrong");
					return;
				}
				if (context == null) {
					showInfo("wrong");
					return;
				}
			}

			new Thread(new Runnable() {
				@Override
				public void run() {

					printWriter.print(context);
					printWriter.flush();
					Log.i(tag, "--->> client send data!");
				}
			}).start();
		} catch (Exception e) {
			Log.e(tag, "--->> send failure!" + e.toString());

		}
	}

      //建立连接按钮的线程
	private void connectThread() {
		if (!isConnected) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					Looper.prepare();
					Log.i(tag, "---->> connect/close server!");

					connectServer(edtIP.getText().toString(), edtPort.getText()
							.toString());
				}
			}).start();
		} else {
			try {
				if (mSocket != null) {
					mSocket.close();
					mSocket = null;
					Log.i(tag, "--->>重新连server.");
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			btnConn.setText("断开连接");
			isConnected = false;
		}
	}
        
     

       //与服务器连接
	private void connectServer(String ip, String port) {
		try {
			Log.e(tag, "--->>start connect  server !" + ip + "," + port);

			mSocket = new Socket(ip, Integer.parseInt(port));
			Log.e(tag, "--->>end connect  server!");

			OutputStream outputStream = mSocket.getOutputStream();

			printWriter = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(outputStream,
							Charset.forName("gb2312"))));
			
			in = mSocket.getInputStream();
			myHandler.sendEmptyMessage(2);

			showInfo("连接服务器成功");
		} catch (Exception e) {
			isConnected = false;
			showInfo("连接服务器失败");
			Log.e(tag, "exception:" + e.toString());
		}

	}
       

	private void showInfo(String msg) {
		Toast.makeText(clientActivity.this, msg, Toast.LENGTH_SHORT).show();

	}
}
