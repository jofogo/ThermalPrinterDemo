package sitv.combiz.com.thermalprinterdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private Button btnSetEncoding;
    private Button btnTestConnection;
    private Button btnPrint;
    BTSettings btSettings;
    PrinterSettings pt;
    OutputStream mOutputStream = null;
    Boolean canPrint = false;
    byte FONT_TYPE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText txtPrinter = (EditText) findViewById(R.id.txtPrinter);
        btnSetEncoding = (Button) findViewById(R.id.btnSetEncoding);
        btnTestConnection = (Button) findViewById(R.id.btnTestConnection);
        btnPrint = (Button) findViewById(R.id.btnPrint);
        btSettings = new BTSettings();
        btSettings.setBluetoothAdapter();
        stateBTMissing();
        if (!btSettings.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else {
            if (btSettings.listPairedDevices() == 0) {
                /*Intent BTIntent = new Intent(getApplicationContext(), BTSettingsActivity.class);
                this.startActivityForResult(BTIntent, BTSettingsActivity.REQUEST_CONNECT_BT);*/
            }
            if (btSettings.getConnectedDevice()!=null) {
                btSettings.startBluetoothConnection();
                if (btSettings.isBluetoothConnected()) {
                    canPrint = true;
                    pt = new PrinterSettings();
                    txtPrinter.setText(btSettings.getConnectDevicePrettyName());
                    setOutputStream();
                    pt.setPrintStream(getOutputStream());
                    stateBTFound();

                }

            }

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
        btnSetEncoding.setEnabled(true);
        btnTestConnection.setEnabled(true);
        btnPrint.setEnabled(true);
        btnPrint.setVisibility(View.VISIBLE);
    }

    private void stateBTMissing() {
        btnSetEncoding.setEnabled(false);
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
                pt.imgBarcode("https://combizcorp.com:2096");
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
                getOutputStream().flush();
                Log.i("Print Status", "Test print successful!");

            } catch (IOException ioe) {
                Log.i("Print Status", "Printer connected, but could not test print!");
            }
        } else {
            Log.i("Print Status", "Printer not connected. Could not test print!");
        }
    }

    @Override
    protected void onDestroy() {
        closeOutputStream();
        btSettings.closeBluetoothConnection();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        closeOutputStream();
        btSettings.closeBluetoothConnection();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        btSettings.startBluetoothConnection();
        setOutputStream();
        super.onRestart();
    }
}
