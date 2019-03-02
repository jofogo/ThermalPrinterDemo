package sitv.combiz.com.thermalprinterdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private String mConnectedDeviceName;
    private String mConnectedDeviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText txtPrinter = (EditText) findViewById(R.id.txtPrinter);
        BTSettings btSettings = new BTSettings();
        btSettings.setBluetoothAdapter();
        if (!btSettings.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else {
            if (btSettings.listPairedDevices() == 0) {

            } else {
                txtPrinter.setText(btSettings.getConnectedDeviceName());
            }
        }
    }


}
