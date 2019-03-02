package sitv.combiz.com.thermalprinterdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class BTSettings {
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mConnectedDevice;
    BluetoothSocket mBluetoothSocket = null;
    String UUID_for_HoinBTPrinter = "00001101-0000-1000-8000-00805f9b34fb";
    UUID mUUID = null;

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void setBluetoothAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setConnectedDevice(BluetoothDevice connectedDevice) {
        mConnectedDevice = connectedDevice;
    }

    public BluetoothDevice getConnectedDevice() {
        return mConnectedDevice;
    }

    private String getConnectedDeviceName() {
        return mConnectedDevice.getName();
    }

    private String getConnectedDeviceAddress() {
        return mConnectedDevice.getAddress();
    }
    public String getConnectDevicePrettyName() {
        return getConnectedDeviceName() + "\n[" + getConnectedDeviceAddress() + "]";
    }

    public void setUUID(UUID UUID) {
        mUUID = UUID;
    }

    public UUID getUUID() {
        return mUUID;
    }

    public BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }

    private void setBluetoothSocket() {
        if (mConnectedDevice!= null) {
            stopScanning();
            try {
                mBluetoothSocket = getConnectedDevice().createRfcommSocketToServiceRecord(getUUID());
            } catch (IOException ioe) {
                Log.i("Socket Connection Error","Could not establish a socket connection!");
            }

        } else {
            mBluetoothSocket = null;
        }
    }

    public Boolean isBluetoothConnected() {
        if (getBluetoothSocket().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public void startBluetoothConnection() {
        try {
            if (mBluetoothSocket!=null) {
                mBluetoothSocket.connect();
                if (isBluetoothConnected()) {
                    Log.i("Connection Status", "Successfully established a connection to BT device!");
                } else {
                    Log.i("Connection Status", "Failed to establish connection!");
                }
            }
        } catch (IOException ioe) {
            Log.i("Socket Open Error", "Socket could not be opened");
        }

    }

    public void closeBluetoothConnection() {
        try {
            if (mBluetoothSocket!=null) {
                mBluetoothSocket.close();
            }
        } catch (IOException ioe) {
            Log.i("Socket Close Error", "Socket is already closed");
        }
    }

    public Boolean isBluetoothEnabled() {
        try {
            if(getBluetoothAdapter()==null) {
                return false;
            } else {
                if (getBluetoothAdapter().isEnabled()) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            Log.i(this.toString(),"Could not read Bluetooth Adapter!");
            return false;
        }

    }

    public int listPairedDevices() {
        try {
            Set<BluetoothDevice> pairedDevices = getBluetoothAdapter().getBondedDevices();
            if (pairedDevices.size() > 0) {
                Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
                ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(getBluetoothAdapter(), null);
                for (BluetoothDevice device : pairedDevices) {
                    String deviceMajorClass = getMajorDeviceClass(device.getBluetoothClass().getMajorDeviceClass());
                    int deviceClass = device.getBluetoothClass().getDeviceClass();
                    String uuid = device.getUuids()[0].toString();
                    Log.i("BT Device Paired" , device.getName() + " : " + device.getAddress() + " - "
//                            + deviceMajorClass + "." + deviceClass);
                                + uuid);
                    /* BT Printer appears to be of IMAGING BT Major Device Class
                    *  For our purposes, limit results to Major Device Class = IMAGING */
                    if (uuid.equals(UUID_for_HoinBTPrinter)) {
                        setConnectedDevice(device);
                        setUUID(device.getUuids()[0].getUuid());
                        setBluetoothSocket();
                    }
                }
                return pairedDevices.size();
            }
        } catch (Exception e) {
            Log.i(this.toString(), "Could not access paired devices list");
        }
        return 0;
    }

    private String getMajorDeviceClass(int majorDeviceClass) {
        switch (majorDeviceClass) {

            case BluetoothClass.Device.Major.PERIPHERAL:
                return "PERIPHERAL";
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return "AUDIO_VIDEO";
            case BluetoothClass.Device.Major.HEALTH:
                return "HEALTH";
            case BluetoothClass.Device.Major.COMPUTER:
                return "COMPUTER";
            case BluetoothClass.Device.Major.MISC:
                return "MISC";
            case BluetoothClass.Device.Major.IMAGING:
                return "IMAGING";
            case BluetoothClass.Device.Major.NETWORKING:
                return "NETWORKING";
            case BluetoothClass.Device.Major.PHONE:
                return "PHONE";
            case BluetoothClass.Device.Major.TOY:
                return "TOY";
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                return "UNCATEGORIZED";
            case BluetoothClass.Device.Major.WEARABLE:
                return "WEARABLE";

            default:
                return "UNKNOWN";
        }
    }

    private void stopScanning() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    private void flushData() {
        try {
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
                mBluetoothSocket = null;
            }

            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.cancelDiscovery();
            }

        } catch (Exception e) {

        }
    }


/*    public void scanForDevices() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            }
        }
    }*/
}
