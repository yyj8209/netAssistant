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
				String msgText =edtSend.getText().toString();//ȡ�ñ༭�����������������
				if(msgText.length()<=0)
				{
					Toast.makeText(mContext, "�������ݲ���Ϊ�գ�", Toast.LENGTH_SHORT).show();
				}
				else
				{
					try 
					{				    	
				    	mPrintWriterServer.print(msgText);//���͸�������
				    	mPrintWriterServer.flush();
					}
					catch (Exception e) 
					{
						// TODO: handle exception
						Toast.makeText(mContext, "�����쳣��" + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			}
			else
			{
				Toast.makeText(mContext, "û������", Toast.LENGTH_SHORT).show();
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
				  edtReceiver.append("��Ϣ: "+recvMessageServer);	// ˢ��
			  }
			  else if(msg.what == 1)
			  {
				  edtReceiver.append("Client: ");	// ˢ��

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
					btnsetserver.setText("��������������");
					edtReceiver.setText("��������Ϣ:\n");
				}
				else
				{
					serverRuning = true;
					mThreadServer = new Thread(mcreateRunnable);
					mThreadServer.start();
					btnsetserver.setText("�رշ�����");
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

	                //�ȴ��ͷ����� 
	                mSocketServer = serverSocket.accept();	                	               
	                
	                //���ܿͷ�������BufferedReader����
	                mBufferedReaderServer = new BufferedReader(new InputStreamReader(mSocketServer.getInputStream()));
	                //���ͷ��˷�������
	                mPrintWriterServer = new PrintWriter(mSocketServer.getOutputStream(),true);
	                //mPrintWriter.println("������Ѿ��յ����ݣ�");

	                Message msg = new Message();
	                msg.what = 0;
	                recvMessageServer="client�Ѿ������ϣ�\n";
	                mHandler.sendMessage(msg);
	                
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					Message msg = new Message();
	                msg.what = 0;
	                recvMessageServer="���ִ���" + e.getMessage() + e.toString() + "\n";//��Ϣ����
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
							recvMessageServer=getInfoBuff(buffer, count) + "\n";//��Ϣ����
							Message msg = new Message();
			                                msg.what = 0;
							mHandler.sendMessage(msg);
						}
					}
					catch (Exception e)
					{
						recvMessageServer= "�����쳣:" + e.getMessage() + "\n";//��Ϣ����
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
								recvMessageServer= "����IP��ַ��"+inetAddress.getHostAddress()+"�˿ں�:"
								+ serverSocket.getLocalPort()+ "\n";	
								
							}
						}
					}
				}
			} 
		
			catch (SocketException ex) {
				recvMessageServer= "��ȡIP��ַ�쳣:" + ex.getMessage() + "\n";//��Ϣ����
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
