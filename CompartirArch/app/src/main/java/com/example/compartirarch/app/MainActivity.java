package com.example.compartirarch.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 2;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    boolean conectado = false;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice  mBluetoothDevice = null;
    protected BluetoothSocket mySocket = null;
    private InputStream MyInStream;
    private OutputStream MyOutStream;
    Thread t = null;
    byte[] bufferIn = new byte[100];
    byte[] bufferIn_temp = new byte[10];
    int bytesIn=0;
    int bytesIn_tmp=0;
    String strTempIn;
    String strTempIn2;
    String strBufferIn;
    int primera_posi;
    int segunda_posi;
    private boolean flag_t;

    private boolean isBtnDown;


    TextView textViewLog;

    ImageView imgbtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textViewLog =  (TextView)findViewById(R.id.editText1);
        textViewLog.setEnabled(false);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Button button1 = (Button) findViewById(R.id.button1);

        Button btnLed1 = (ToggleButton) findViewById(R.id.toggleButton1);
        btnLed1.setOnClickListener(manejarBoton1);
        Button btnLed2 = (ToggleButton) findViewById(R.id.toggleButton2);
        btnLed2.setOnClickListener(manejarBoton2);
        Button btnLed3 = (ToggleButton) findViewById(R.id.toggleButton3);
        btnLed3.setOnClickListener(manejarBoton3);
        Button btnLed4 = (ToggleButton) findViewById(R.id.toggleButton4);
        btnLed4.setOnClickListener(manejarBoton4);

        imgbtn = (ImageView) findViewById(R.id.imageView1);

        mBluetoothDevice = null;
        isBtnDown = false;

        button1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                conectar();
                button1.setEnabled(false);
                conectado = true;
            }

            private void conectar() {
                try{
                    if (mBluetoothAdapter.isEnabled())
                    {
                        mBluetoothAdapter.startDiscovery();
                        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                        for (BluetoothDevice device : devices) textViewLog.append("\nFound device: " + device);
                        textViewLog.append("\nConnecting");

                        String address ="00:06:66:43:45:20";
                        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
                        textViewLog.append("\n"+mBluetoothDevice.toString() + "  " + mBluetoothDevice.getName());

                        BluetoothSocket tmp = null;

                        try {
                            Method m = mBluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                            tmp = (BluetoothSocket) m.invoke(mBluetoothDevice, Integer.valueOf(1));
                        } catch (Exception e) {
                            textViewLog.append("\n"+"CONNECTION IN THREAD DIDNT WORK");
                        }
                        mySocket = tmp;

                        try {
                            mySocket.connect();
                        } catch (IOException e) {
                            textViewLog.append("\n"+e.getMessage());
                            textViewLog.append("\n"+"CONNECTION IN THREAD DIDNT WORK 2");
                        }

                        try {
                            MyInStream = mySocket.getInputStream();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        if (t==null)
                        {
                            flag_t = true;
                            t = new Thread()
                            {
                                public void run()
                                {
                                    while (true)
                                    {
                                        try {
                                            bytesIn=MyInStream.read(bufferIn);
                                            strTempIn =  new String(bufferIn,0,bytesIn);
                                            strBufferIn +=  strTempIn;
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        messageHandler.sendMessage(Message.obtain(messageHandler, 1));
                                    }
                                };
                            };
                        }
                        t.start();
                        textViewLog.append("\n"+"Conectado con RN42");
                    }
                }catch(Exception e){
                    textViewLog.append("\n"+"Error conectando");
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mBluetoothAdapter != null)
        {

            textViewLog.setText("");
            textViewLog.append("Bluetooth :" +  mBluetoothAdapter.toString());

            if (!mBluetoothAdapter.isEnabled())
            {
                textViewLog.append("Bluetooth is being activated");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }


        }else textViewLog.setText("Device does not support Bluetooth");
    }



    private Handler messageHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch(msg.what) {


                case 1:
                    primera_posi= strBufferIn.indexOf('&') ;
                    if(primera_posi != 0){
                        strBufferIn = strBufferIn.substring(primera_posi);
                    }else{
                        strBufferIn = strBufferIn.substring(0,1);
                    }
                    textViewLog.append("\nComando recibido: "+strBufferIn );
                    if(isBtnDown){
                        imgbtn.setImageResource(R.drawable.btnup);
                        isBtnDown = false;
                    }else{
                        imgbtn.setImageResource(R.drawable.btndown);
                        isBtnDown = true;
                    }
                    break;
            }
        }
    };


    private OnClickListener manejarBoton1 = new OnClickListener(){
        public void onClick(View v) {
            enviarLetra("3");
        }
    };


    private OnClickListener manejarBoton2 = new OnClickListener(){
        public void onClick(View v) {
            enviarLetra("2");
        }
    };


    private OnClickListener manejarBoton3 = new OnClickListener(){
        public void onClick(View v) {
            enviarLetra("1");
        }
    };

    private OnClickListener manejarBoton4 = new OnClickListener(){
        public void onClick(View v) {
            enviarLetra("4");
        }
    };

    @Override
    public void onDestroy() {
        enviarLetra("6");
        try {
            if (MyInStream != null) {
                MyInStream.close();
            }
            if (mySocket != null) {
                mySocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void enviarLetra(String letra){
        if(conectado){
            textViewLog.append("\n"+"Enviando letra "+ letra);

            if (mySocket != null)
            {
                try {
                    MyOutStream = mySocket.getOutputStream();
                } catch (IOException e) {
                    textViewLog.append("\nERROR: "+e.getMessage());
                }

                try {
                    MyOutStream.write((letra+"\r").getBytes());
                } catch (IOException e) {
                    textViewLog.append("\nERROR: "+e.getMessage());
                }

            }else textViewLog.setText("Error enviando letra " +letra);
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }
}
