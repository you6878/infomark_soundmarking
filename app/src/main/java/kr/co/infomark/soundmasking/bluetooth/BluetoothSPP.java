package kr.co.infomark.soundmasking.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

@SuppressLint("NewApi")
public class BluetoothSPP {
	private static BluetoothSPP bluetoothSPP;
	// Listener for Bluetooth Status & Connection
	private BluetoothStateListener mBluetoothStateListener = null;
	private OnDataReceivedListener mDataReceivedListener = null;
	private BluetoothConnectionListener mBluetoothConnectionListener = null;
	private AutoConnectionListener mAutoConnectionListener = null;
    
    // Context from activity which call this class
	private Context mContext;
	
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the chat services
    private BluetoothService mChatService = null;

	public void setmDeviceName(String mDeviceName) {
		this.mDeviceName = mDeviceName;
	}

	public void setmDeviceAddress(String mDeviceAddress) {
		this.mDeviceAddress = mDeviceAddress;
	}

	// Name and Address of the connected device
    private String mDeviceName = "";
    private String mDeviceAddress = "";

    private boolean isAutoConnectionEnabled = true;
	public boolean isConnected = false;
	private boolean isConnecting = false;
	private boolean isServiceRunning = false;

    private boolean isAndroid = BluetoothState.DEVICE_OTHER;
	
    private BluetoothConnectionListener bcl;
    private int c = 0;

	public static BluetoothSPP getInstance(Context context) {
		if(bluetoothSPP == null) {
			bluetoothSPP = new BluetoothSPP(context);
		}

		return bluetoothSPP;
	}
	private BluetoothSPP(Context context) {
		mContext = context;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
    public interface BluetoothStateListener {
	    public void onServiceStateChanged(int state);
	}
	
    public interface OnDataReceivedListener {
	    public void onDataReceived(byte[] data, String message);
	}
    
    public interface BluetoothConnectionListener {
	    public void onDeviceConnected(String name, String address);
	    public void onDeviceDisconnected();
	    public void onDeviceConnectionFailed();
	}
    
    public interface AutoConnectionListener {
	    public void onAutoConnectionStarted();
	    public void onNewConnection(String name, String address);
	}
	
	public boolean isBluetoothAvailable() {
        try {
        	if (mBluetoothAdapter == null || mBluetoothAdapter.getAddress().equals(null))
	            return false;
        } catch (NullPointerException e) {
        	 return false;
        }
        return true;
	}
	
	public boolean isBluetoothEnabled() {
		return mBluetoothAdapter.isEnabled();
	}
	
	public boolean isServiceAvailable() {
		return mChatService != null;
	}

	
	public boolean startDiscovery() {
		return mBluetoothAdapter.startDiscovery();
	}
	
	public boolean isDiscovery() {
		return mBluetoothAdapter.isDiscovering();
	}
	
	public boolean cancelDiscovery() {
		return mBluetoothAdapter.cancelDiscovery();
	}
	
	public void setupService() {
		if(mChatService == null){
			mChatService = new BluetoothService(mContext, mHandler);
		}

	}
	
	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}
	
	public int getServiceState() {
		if(mChatService != null) 
			return mChatService.getState();
		else 
			return -1;
	}
	
	public void startService(boolean isAndroid) {
		if (mChatService != null) {
            if (mChatService.getState() == BluetoothState.STATE_NONE) {
            	isServiceRunning = true;
            	mChatService.start(isAndroid);
            	BluetoothSPP.this.isAndroid = isAndroid;
            }
        }
	}
	
	public void stopService() {
		if (mChatService != null) {
        	isServiceRunning = false;
        	mChatService.stop();
        }
		new Handler().postDelayed(new Runnable() {
			public void run() {
		        if (mChatService != null) {
		        	isServiceRunning = false;
		        	mChatService.stop();
		        }
			}
		}, 500);
	}
    
    public void setDeviceTarget(boolean isAndroid) {
    	stopService();
    	startService(isAndroid);
    	BluetoothSPP.this.isAndroid = isAndroid;
    }
	
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothState.MESSAGE_WRITE:
                break;
            case BluetoothState.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf);
                if(readBuf != null && readBuf.length > 0) {
                	if(mDataReceivedListener != null)
                		mDataReceivedListener.onDataReceived(readBuf, readMessage);
                }
                break;
            case BluetoothState.MESSAGE_DEVICE_NAME:
                mDeviceName = msg.getData().getString(BluetoothState.DEVICE_NAME);
                mDeviceAddress = msg.getData().getString(BluetoothState.DEVICE_ADDRESS);
            	if(mBluetoothConnectionListener != null)
            		mBluetoothConnectionListener.onDeviceConnected(mDeviceName, mDeviceAddress);
                isConnected = true;
                break;
            case BluetoothState.MESSAGE_TOAST:
                Toast.makeText(mContext, msg.getData().getString(BluetoothState.TOAST)
                		, Toast.LENGTH_SHORT).show();
                break;
            case BluetoothState.MESSAGE_STATE_CHANGE:

                if(msg.arg1 != BluetoothState.STATE_CONNECTED) {
                	if(mBluetoothConnectionListener != null)
                		mBluetoothConnectionListener.onDeviceDisconnected();
                    isConnected = false;
                }
                break;
            }
        }
    };
    
    public void stopAutoConnect() {
    	isAutoConnectionEnabled = false;
    }
    
    public void connect(Intent data) {
        String address = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device);
    }
    
    public void connect(String address) {
		if(mChatService == null){
			setupService();
		}
		if(!address.isEmpty()){
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			mChatService.connect(device);
		}
    }
    
    public void disconnect() {
    	if(mChatService != null) {
        	isServiceRunning = false;
        	mChatService.stop();
            if(mChatService.getState() == BluetoothState.STATE_NONE) {
            	isServiceRunning = true;
            	mChatService.start(BluetoothSPP.this.isAndroid);
            }
        }
    }
    
    public void setBluetoothStateListener (BluetoothStateListener listener) {
    	mBluetoothStateListener = listener;
    }
    
    public void setOnDataReceivedListener (OnDataReceivedListener listener) {
    	mDataReceivedListener = listener;
    }
    
    public void setBluetoothConnectionListener (BluetoothConnectionListener listener) {
    	mBluetoothConnectionListener = listener;
    }
    
    public void setAutoConnectionListener(AutoConnectionListener listener) {
    	mAutoConnectionListener = listener;
    }
    
    public void enable() {
    	mBluetoothAdapter.enable();
    }
    
    public void send(byte[] data) {
    	if(mChatService.getState() == BluetoothState.STATE_CONNECTED)
    		mChatService.write(data);
    }
    
    public void send(String data) {
		if(mChatService == null){
			return;
		}
    	if(mChatService.getState() == BluetoothState.STATE_CONNECTED)
    		mChatService.write(data.getBytes());
    }
    
    public String getConnectedDeviceName() {
    	return mDeviceName;
    }
    
    public String getConnectedDeviceAddress() {
    	return mDeviceAddress;
    }
    
    public String[] getPairedDeviceName() {
    	int c = 0;
    	Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();  
    	String[] name_list = new String[devices.size()];
        for(BluetoothDevice device : devices) {  
        	name_list[c] = device.getName();
        	c++;
        }  
        return name_list;
    }
    
    public String[] getPairedDeviceAddress() {
    	int c = 0;
    	Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();  
    	String[] address_list = new String[devices.size()];
        for(BluetoothDevice device : devices) {  
        	address_list[c] = device.getAddress();
        	c++;
        }  
        return address_list;
    }


    public void autoConnect(String name,String address) {



        	if(mAutoConnectionListener != null)
        		mAutoConnectionListener.onAutoConnectionStarted();
	    	final ArrayList<String> arr_filter_address = new ArrayList<String>();
	    	final ArrayList<String> arr_filter_name = new ArrayList<String>();
	    	String[] arr_name = getPairedDeviceName();
	    	String[] arr_address = getPairedDeviceAddress();

	    	bcl = new BluetoothConnectionListener() {
				public void onDeviceConnected(String name, String address) {
					bcl = null;

				}

				public void onDeviceDisconnected() {


				}
				public void onDeviceConnectionFailed() {

				}
	    	};

	    	setBluetoothConnectionListener(bcl);
	    	c = 0;
        	if(mAutoConnectionListener != null)
        		mAutoConnectionListener.onNewConnection(arr_name[c], arr_address[c]);
        	if(arr_filter_address.size() > 0)
        		connect(arr_filter_address.get(c));
        	else {

			}
//        		Toast.makeText(mContext, "Device name mismatch", Toast.LENGTH_SHORT).show();

    }
}
