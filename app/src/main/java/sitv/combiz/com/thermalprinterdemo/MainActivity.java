package sitv.combiz.com.thermalprinterdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECTION_LOST = 6;
    public static final int MESSAGE_UNABLE_CONNECT = 7;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CHOOSE_BMP = 3;
    private static final int REQUEST_CAMERA = 4;

    private static final int QR_WIDTH = 350;
    private static final int QR_HEIGHT = 350;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private Button btnTestConnection;
    private Button btnPrint;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the services
    private BTSettings mService = null;

    BTSettings btSettings;
    POScommands pt;
    OutputStream mOutputStream = null;
    Boolean canPrint = false;
    byte FONT_TYPE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText txtPrinter = (EditText) findViewById(R.id.txtPrinter);
        btnTestConnection = (Button) findViewById(R.id.btnTestConnection);
        btnPrint = (Button) findViewById(R.id.btnPrint);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available on this device",
                    Toast.LENGTH_LONG).show();
            finish();
        }



        //btSettings = new BTSettings();
 /*       btSettings.setBluetoothAdapter();
        stateBTMissing();
        if (!btSettings.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else {
            if (btSettings.listPairedDevices() == 0) {
                *//*Intent BTIntent = new Intent(getApplicationContext(), BTSettingsActivity.class);
                this.startActivityForResult(BTIntent, BTSettingsActivity.REQUEST_CONNECT_BT);*//*
            }
            if (btSettings.getConnectedDevice()!=null) {
                btSettings.startBluetoothConnection();
                if (btSettings.isBluetoothConnected()) {
                    canPrint = true;
                    pt = new POScommands();
                    txtPrinter.setText(btSettings.getConnectDevicePrettyName());
                    setOutputStream();
                    pt.setPrintStream(getOutputStream());
                    stateBTFound();

                }

            }

        }*/
    }

    @Override
    public void onStart() {
        super.onStart();

        // If Bluetooth is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the session
        } else {
            if (mService == null) {
                mService = new BTSettings(this, mHandler);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:{
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    if (BluetoothAdapter.checkBluetoothAddress(address)) {
                        BluetoothDevice device = mBluetoothAdapter
                                .getRemoteDevice(address);
                        // Attempt to connect to the device
                        mService.connect(device);
                    }
                }
                break;
            }
            case REQUEST_ENABLE_BT:{
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a session
                    stateBTFound();
                } else {
                    // User did not enable Bluetooth or an error occured
                    stateBTMissing();
                    finish();
                }
                break;
            }
/*            case REQUEST_CHOSE_BMP:{
                if (resultCode == Activity.RESULT_OK){
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaColumns.DATA };

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(picturePath, opts);
                    opts.inJustDecodeBounds = false;
                    if (opts.outWidth > 1200) {
                        opts.inSampleSize = opts.outWidth / 1200;
                    }
                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath, opts);
                    if (null != bitmap) {
                        imageViewPicture.setImageBitmap(bitmap);
                    }
                }else{
                    Toast.makeText(this, getString(R.string.msg_statev1), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_CAMER:{
                if (resultCode == Activity.RESULT_OK){
                    handleSmallCameraPhoto(data);
                }else{
                    Toast.makeText(this, getText(R.string.camer), Toast.LENGTH_SHORT).show();
                }
                break;
            }*/
        }
    }


    private void setOutputStream () {
        if (btSettings.isBluetoothConnected()) {
            try {
                mOutputStream = btSettings.getBluetoothSocket().getOutputStream();
            } catch (IOException ioe) {
                Log.i("Printer Status", "Could not establish connection to printer!");
            }
        }
    }

    private void closeOutputStream() {
        try {
            mOutputStream.close();
        }catch (IOException ioe) {
            mOutputStream = null;
        }
    }

    private OutputStream getOutputStream() {
        return mOutputStream;
    }

    private void stateBTFound() {
        btnTestConnection.setEnabled(true);
        btnPrint.setEnabled(true);
        btnPrint.setVisibility(View.VISIBLE);
    }

    private void stateBTMissing() {
        btnTestConnection.setEnabled(false);
        btnPrint.setEnabled(false);
        btnPrint.setVisibility(View.INVISIBLE);
    }

    private byte[] getLogo() {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return imageBytes;
        /*
        String encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);*/
    }

    public void printTest(View view) {
        if (canPrint && getOutputStream() != null) {
            try {
                String msg = "";
                pt.initialize();
                pt.textTitle("Combiz TestPrint");
                //pt.imgLogo(getLogo());
                pt.textTitle2("Combiz");
                byte[] code = pt.getBarCommand("http://da2001.com",1,3,8);
                pt.SendDataByte(new byte[]{0x1b, 0x61, 0x00});
                pt.SendDataByte(code);
                pt.textSmall("Small text");
                pt.textLF();
                pt.textMedium("Medium text");
                pt.textLF();
                pt.textLarge("Large text");
                pt.textLF();
                pt.textBold(" Bold text");
                pt.textItalics(" Italicized text");
                pt.textUnderline(" Underlined text", 1);
                pt.textLF();
                pt.textReverse("This is a sample print message @Combiz2019");
                pt.textLF();
                pt.textLF();
                pt.textLF();
                getOutputStream().flush();
                Log.i("Print Status", "Test print successful!");

            } catch (IOException ioe) {
                Log.i("Print Status", "Printer connected, but could not test print!");
            }
        } else {
            Log.i("Print Status", "Printer not connected. Could not test print!");
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BTSettings.STATE_CONNECTED:
                            Toast.makeText(MainActivity.this, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            break;
                        case BTSettings.STATE_CONNECTING:
                            Toast.makeText(MainActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                            break;
                        case BTSettings.STATE_LISTEN:
                        case BTSettings.STATE_NONE:
                            Toast.makeText(MainActivity.this, "Not connected!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:

                    break;
                case MESSAGE_READ:

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
                case MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), "Device connection was lost",
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(getApplicationContext(), "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (mService != null) {

            if (mService.getState() == BTSettings.STATE_NONE) {
                // Start the Bluetooth services
                mService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth services
        if (mService != null)
            mService.stop();
    }

}
