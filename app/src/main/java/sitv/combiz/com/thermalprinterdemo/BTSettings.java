package sitv.combiz.com.thermalprinterdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

public class BTSettings {
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mConnectedDevice;

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void setBluetoothAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public String getConnectedDeviceName() {
        return mConnectedDevice.getName();
    }

    public String getConnectedDeviceAddress() {
        return mConnectedDevice.getAddress();
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
                for (BluetoothDevice device : pairedDevices) {
                    mConnectedDevice = device;
                    Log.i("BT Device Paired" , device.getName() + " : " + device.getAddress());
                }
                return pairedDevices.size();
            }
        } catch (Exception e) {
            Log.i(this.toString(), "Could not access paired devices list");
        }
        return 0;
    }
}
