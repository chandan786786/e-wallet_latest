package com.bih.nic.e_wallet.Tvsprinter;import android.app.Activity;import android.app.ProgressDialog;import android.bluetooth.BluetoothAdapter;import android.bluetooth.BluetoothDevice;import android.content.BroadcastReceiver;import android.content.Context;import android.content.Intent;import android.content.IntentFilter;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.util.Log;import android.view.View;import android.widget.AdapterView;import android.widget.ListView;import android.widget.TextView;import android.widget.Toast;import androidx.annotation.Nullable;import androidx.appcompat.app.AppCompatActivity;import com.bih.nic.e_wallet.R;import com.bih.nic.e_wallet.SessionManager.BlueToothBean;import com.bih.nic.e_wallet.SessionManager.ClsUtils;import com.bih.nic.e_wallet.SessionManager.StatisticsAdapter;import com.bih.nic.e_wallet.utilitties.CommonPref;import java.util.ArrayList;import HPRTAndroidSDK.HPRTPrinterHelper;public class Activity_DeviceList extends AppCompatActivity {    // ---------------------------------------------------    public static String ErrorMessage;    TextView btnSearch;    ListView lvBTDevices;    SharedPrefClass session;    BluetoothAdapter btAdapt;    // OutputStream mOutputStream;    protected static final String TAG = "MainActivity";    String btDev_str;    public static String toothAddress = null;    String address = "",name="";    public ArrayList<BlueToothBean> list = new ArrayList<BlueToothBean>();    private ProgressDialog pd;    private Thread thread;    Handler handler_bt = new Handler() {        public void handleMessage(Message msg) {            // Log.e("", "1msg:" + msg.what);            if (msg.what == 0) {                try {                    session.CreateLoginSession(name, address);//					HPRTPrinterHelper.PrintText("打印测试1234567890\n");                 //   Intent mIntent = new Intent(Activity_DeviceList.this, PrintHomeActivity.class);                    pd.dismiss();// 关闭ProgressDialog                 //   startActivity(mIntent);                    finish();                } catch (Exception e) {                    // TODO Auto-generated catch block                    e.printStackTrace();                }            } else {                /*Toast.makeText(getApplicationContext(),                        "Failed", Toast.LENGTH_SHORT).show();*/                pd.dismiss();// 关闭ProgressDialog                thread = new Thread(new Runnable() {                    @Override                    public void run() {                        // TODO Auto-generated method stub                        try {                            int portOpen = HPRTPrinterHelper.PortOpen("Bluetooth," + CommonPref.getPrinterMacAddress(Activity_DeviceList.this));                            message = new Message();                            message.what = portOpen;                            handler_bt.sendMessage(message);//                            Log.e("", "msg:"+portOpen);                        } catch (Exception e) {                            // TODO Auto-generated catch block                            e.printStackTrace();                        }                    }                });                thread.start();            }//        	 Intent intent = new Intent();//            intent.putExtra("is_connected", ((msg.what==0)?"OK":"NO"));//            intent.putExtra("BTAddress", toothAddress);//            setResult(HPRTPrinterHelper.ACTIVITY_CONNECT_BT, intent);        }        ;    };    private Message message;    StatisticsAdapter sAdapter;    @Override    protected void onCreate(@Nullable  Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_devicelist);        // if(!ListBluetoothDevice())finish();        ErrorMessage = "";        // ---------------------------------------------------        btnSearch = (TextView) this.findViewById(R.id.btnSearch);        btnSearch.setOnClickListener(new ClickEvent());        ArrayList<String> mList = new ArrayList<String>();        session = new SharedPrefClass(Activity_DeviceList.this);        // ListView及其数据源 适配器        lvBTDevices = (ListView) this.findViewById(R.id.listView1);//		adtDevices = new ArrayAdapter<String>(this,//				android.R.layout.simple_list_item_1, lstDevices);////		lvBTDevices.setAdapter(adtDevices);//		lvBTDevices.setOnItemClickListener(new ItemClickEvent());        sAdapter = new StatisticsAdapter();        sAdapter.init(Activity_DeviceList.this);        sAdapter.setList(list);        lvBTDevices.setAdapter(sAdapter);        lvBTDevices.setOnItemClickListener(new ItemClickEvent());        btAdapt = BluetoothAdapter.getDefaultAdapter();// 初始化本机蓝牙功能        // 注册Receiver来获取蓝牙设备相关的结果        String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";        IntentFilter intent = new IntentFilter();        intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);        intent.addAction(ACTION_PAIRING_REQUEST);        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);        registerReceiver(searchDevices, intent);        lvBTDevices.setVerticalScrollBarEnabled(false);        lvBTDevices.setFastScrollEnabled(false);        findViewById(R.id.back_btList).setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View view) {                Activity_DeviceList.this.finish();            }        });        new Handler().postDelayed(new Runnable() {            @Override            public void run() {                searchDevice();            }        }, 1000);    }    private BroadcastReceiver searchDevices = new BroadcastReceiver() {        public void onReceive(Context context, Intent intent) {            String action = intent.getAction();            Bundle b = intent.getExtras();            Object[] lstName = b.keySet().toArray();            // 显示所有收到的消息及其细节            for (int i = 0; i < lstName.length; i++) {                String keyName = lstName[i].toString();                Log.e(keyName, String.valueOf(b.get(keyName)));            }            BluetoothDevice device = null;            // 搜索设备时，取得设备的MAC地址            if (BluetoothDevice.ACTION_FOUND.equals(action)) {                device = intent                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);                if (device.getBondState() == BluetoothDevice.BOND_NONE) {//					String str = device.getName() + "|"+"\n" + device.getAddress();//					if (lstDevices.indexOf(str) == -1)// 防止重复添加//						lstDevices.add(str); // 获取设备名称和mac地址//					adtDevices.notifyDataSetChanged();                    list.add(new BlueToothBean(device.getName(), device.getAddress()));                    sAdapter.setList(list);                    lvBTDevices.setAdapter(sAdapter);                    sAdapter.notifyDataSetChanged();                    try {                        ClsUtils.setPin(device.getClass(), device, "0000");                    } catch (Exception e) {                        // TODO Auto-generated catch block                        e.printStackTrace();                    }                    try {                        ClsUtils.cancelPairingUserInput(device.getClass(),                                device);                    } catch (Exception e) {                        // TODO Auto-generated catch block                        e.printStackTrace();                    }                }            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {                device = intent                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);                switch (device.getBondState()) {                    case BluetoothDevice.BOND_BONDING:                        Log.e("BlueToothTestActivity", "BlueToothTestActivity......");                        break;                    case BluetoothDevice.BOND_BONDED:                        Log.e("BlueToothTestActivity", "BlueToothTestActivity");                        // connect(device);//连接设备                        break;                    case BluetoothDevice.BOND_NONE:                        Log.e("BlueToothTestActivity", "BlueToothTestActivity");                    default:                        break;                }            }        }    };    class ItemClickEvent implements AdapterView.OnItemClickListener {        @Override        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,                                long arg3) {            CommonPref.setPrinterType(Activity_DeviceList.this,"T");            if (btAdapt.isDiscovering())                btAdapt.cancelDiscovery();//			String str = lstDevices.get(arg2);//			String[] values = str.split("\\|");            address = list.get(arg2).getBTMac();            name = list.get(arg2).getBtName();            BluetoothDevice btDev = btAdapt.getRemoteDevice(address);            try {//                btDev_str = btDev.toString();                toothAddress = btDev_str;                CommonPref.setPrinterMacAddress(Activity_DeviceList.this,toothAddress);                CommonPref.setPrinterType(Activity_DeviceList.this,"T");                //LoginActivity.devName=list.get(arg2).getBtName();                pd = ProgressDialog.show(Activity_DeviceList.this, "Please Wait", "Connecting");                thread = new Thread(new Runnable() {                    @Override                    public void run() {                        // TODO Auto-generated method stub                        try {                            int portOpen = HPRTPrinterHelper.PortOpen("Bluetooth," + btDev_str);                            message = new Message();                            message.what = portOpen;                            handler_bt.sendMessage(message);                          Log.e("", "msg:"+portOpen);                        } catch (Exception e) {                            // TODO Auto-generated catch block                            e.printStackTrace();                        }                    }                });                thread.start();                Log.e("", "地址：" + btDev.toString());            } catch (Exception e) {                e.printStackTrace();            }        }    }    public String byteToString(byte[] b, int size) {        byte high, low;        byte maskHigh = (byte) 0xf0;        byte maskLow = 0x0f;        StringBuffer buf = new StringBuffer();        for (int i = 0; i < size; i++) {            high = (byte) ((b[i] & maskHigh) >> 4);            low = (byte) (b[i] & maskLow);            buf.append(findHex(high));            buf.append(findHex(low));            buf.append(" ");        }        return buf.toString();    }    private char findHex(byte b) {        int t = new Byte(b).intValue();        t = t < 0 ? t + 16 : t;        if ((0 <= t) && (t <= 9)) {            return (char) (t + '0');        }        return (char) (t - 10 + 'A');    }    class ClickEvent implements View.OnClickListener {        @Override        public void onClick(View v) {            if (v == btnSearch)// 搜索蓝牙设备，在BroadcastReceiver显示结果            {                searchDevice();            }        }    }    private void searchDevice() {        if (btAdapt.getState() == BluetoothAdapter.STATE_OFF) {// 如果蓝牙还没开启            Toast.makeText(Activity_DeviceList.this, "Searching", Toast.LENGTH_SHORT).show();            return;        }        if (btAdapt.isDiscovering())            btAdapt.cancelDiscovery();        list.clear();        Object[] lstDevice = btAdapt.getBondedDevices().toArray();        for (int i = 0; i < lstDevice.length; i++) {            BluetoothDevice device = (BluetoothDevice) lstDevice[i];//			String str = "" + device.getName() + "|"+"\n"//					+ device.getAddress();//			lstDevices.add(str); // 获取设备名称和mac地址//			adtDevices.notifyDataSetChanged();            list.add(new BlueToothBean(device.getName(), device.getAddress()));            sAdapter.setList(list);            lvBTDevices.setAdapter(sAdapter);            sAdapter.notifyDataSetChanged();        }        btAdapt.startDiscovery();    }    @Override    protected void onDestroy() {        this.unregisterReceiver(searchDevices);        super.onDestroy();    }}