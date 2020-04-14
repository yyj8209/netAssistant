package com.example.assitant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;   
import java.io.OutputStream;  
import java.io.InputStreamReader;
import java.io.OutputStreamWriter; 
import java.io.PrintWriter;
import java.util.Enumeration;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset; 

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;  
import android.os.Handler; 
import android.os.Looper;  
import android.os.Message; 
import android.os.StrictMode;
import android.util.Log;  
import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;  

public class serverActivity extends Activity { 
	 private Button btnsetserver;
     private Button btnsend2;
     private Button btnclean2;

     EditText edtSend;
     private EditText edtReceiver;
     private Context mContext;
     private String tag = "MainActivity";

	    InputStream in;
	    PrintWriter printWriter = null;
	    BufferedReader reader;

	    private Socket mSocketServer = null;
	
		private  String recvMessageServer = "";
	
        
         static BufferedReader mBufferedReaderServer= null;
	     static PrintWriter mPrintWriterServer= null;
	     static BufferedReader mBufferedReaderClient= null;
	     static PrintWriter mPrintWriterClient= null;
         private Thread mThreadServer = null;
	     Thread receiverThread;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.servermain);
	    mContext = this;
	    
		edtSend = (EditText) this.findViewById(R.id.id_edt_sendArea2);
		edtReceiver = (EditText) findViewById(R.id.id_edt_jieshou2);
		
		btnsend2= (Button) findViewById(R.id.id_btn_send2);
		btnsend2.setOnClickListener(btnsendlistener);
		btnsetserver = (Button) findViewById(R.id.id_btn_setServer);
		btnsetserver.setOnClickListener(btnsetserverlistener);
		btnclean2= (Button) findViewById(R.id.id_btn_clean2);
        btnclean2.setOnClickListener(btncleanlistener);
	}
private OnClickListener btncleanlistener=new OnClickListener(){
	 @Override
	  public void onClick(View arg0) {
		 edtSend.setText("");
	 }
	
};
private OnClickListener btnsendlistener= new OnClickListener(){
  @Override
  public void onClick(View arg0) {
			// TODO Auto-generated method stub				
			if ( serverRuning && mSocketServer!=null ) 
			{
				String msgText =edtSend.getText().toString();//取得编辑框中我们输入的内容
				if(msgText.length()<=0)
				{
					Toast.makeText(mContext, "发送内容不能为空！", Toast.LENGTH_SHORT).show();
				}
				else
				{
					try 
					{				    	
				    	mPrintWriterServer.print(msgText);//发送给服务器
				    	mPrintWriterServer.flush();
					}
					catch (Exception e) 
					{
						// TODO: handle exception
						Toast.makeText(mContext, "发送异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			}
			else
			{
				Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			}
           }
		} ;

Handler mHandler = new Handler()
	{										
		  public void handleMessage(Message msg)										
		  {											
			  super.handleMessage(msg);			
			  if(msg.what == 0)
			  {
				  edtReceiver.append("信息: "+recvMessageServer);	// 刷新
			  }
			  else if(msg.what == 1)
			  {
				  edtReceiver.append("Client: ");	// 刷新

			  }
		  }									
	 };

  private ServerSocket serverSocket = null;
  private boolean serverRuning = false;
  private OnClickListener btnsetserverlistener= new OnClickListener(){

  @Override
  public void onClick(View arg0) {
				// TODO Auto-generated method stub				
				if (serverRuning) 
				{
					serverRuning = false;
					
					try {
						if(serverSocket!=null)
						{
							serverSocket.close();
							serverSocket = null;
						}
						if(mSocketServer!=null)
						{
							mSocketServer.close();
							mSocketServer = null;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mThreadServer.interrupt();
					btnsetserver.setText("启动服务器功能");
					edtReceiver.setText("服务器信息:\n");
				}
				else
				{
					serverRuning = true;
					mThreadServer = new Thread(mcreateRunnable);
					mThreadServer.start();
					btnsetserver.setText("关闭服务器");
				}
			}
};

private Runnable mcreateRunnable= new Runnable() 
		{
			public void run()
			{				
				try {
					serverSocket = new ServerSocket(0);
					
					SocketAddress address = null;	
					if(!serverSocket.isBound())	
					{
						serverSocket.bind(address, 0);
					}
					
					
					getLocalIpAddress();

	                //等待客服连接 
	                mSocketServer = serverSocket.accept();	                	               
	                
	                //接受客服端数据BufferedReader对象
	                mBufferedReaderServer = new BufferedReader(new InputStreamReader(mSocketServer.getInputStream()));
	                //给客服端发送数据
	                mPrintWriterServer = new PrintWriter(mSocketServer.getOutputStream(),true);
	                //mPrintWriter.println("服务端已经收到数据！");

	                Message msg = new Message();
	                msg.what = 0;
	                recvMessageServer="client已经连接上！\n";
	                mHandler.sendMessage(msg);
	                
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					Message msg = new Message();
	                msg.what = 0;
	                recvMessageServer="出现错误" + e.getMessage() + e.toString() + "\n";//消息换行
					mHandler.sendMessage(msg);
					return;
				}
				char[] buffer = new char[256];
				int count = 0;
				while(serverRuning)
				{
					try
					{
						
						if((count = mBufferedReaderServer.read(buffer))>0);
						{						
							recvMessageServer=getInfoBuff(buffer, count) + "\n";//消息换行
							Message msg = new Message();
			                                msg.what = 0;
							mHandler.sendMessage(msg);
						}
					}
					catch (Exception e)
					{
						recvMessageServer= "接收异常:" + e.getMessage() + "\n";//消息换行
						Message msg = new Message();
		                                msg.what = 0;
						mHandler.sendMessage(msg);
						return;
					}
				}
			}
		};


      public String getLocalIpAddress() 
		{
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
				{
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();enumIpAddr.hasMoreElements();) 
					{
						InetAddress inetAddress = enumIpAddr.nextElement();
						
						{
							
							{
								recvMessageServer= "本地IP地址："+inetAddress.getHostAddress()+"端口号:"
								+ serverSocket.getLocalPort()+ "\n";	
								
							}
						}
					}
				}
			} 
		
			catch (SocketException ex) {
				recvMessageServer= "获取IP地址异常:" + ex.getMessage() + "\n";//消息换行
				Message msg = new Message();
                                 msg.what = 0;
				mHandler.sendMessage(msg);
			}
			Message msg = new Message();
                        msg.what = 0;
			mHandler.sendMessage(msg);
			return null;
		}

  
          private String getInfoBuff(char[] buff, int count)
		{
			char[] temp = new char[count];
			for(int i=0; i<count; i++)
			{
				temp[i] = buff[i];
			}
			return new String(temp);
		}
          
}
