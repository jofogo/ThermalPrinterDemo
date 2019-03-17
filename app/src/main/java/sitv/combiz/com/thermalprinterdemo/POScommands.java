package sitv.combiz.com.thermalprinterdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class POScommands {
    private BTSettings mBTSettings;
    private BluetoothSocket mBluetoothSocket;
    private OutputStream printStream = null;

    private Byte chr_linefeed = 0x0A; // Line Feed
    private Byte chr_return = 0x0D; // Carriage Return
    private Byte chr_Esc = 0x1B; // Escape
    private Byte chr_atSign = 0x40; //@
    private Byte chr_exclamation = 0x21; //!
    private byte[] fmt_initialize = {0x1B, 0x40}; // Esc @ = Initialize
    private byte[] fmt_fontSmall = {0x1B, 0x4D, 0x48};
    private byte[] fmt_fontMedium = {0x1B, 0x4D, 0x00};
    private byte[] fmt_fontNormal = {0x1D, 0x21, 0x00};
    private byte[] fmt_fontLarge = {0x1D, 0x21, 0x11};
    private byte[] fmt_fontTitle2x = {0x1D, 0x21, 0x22};
    private byte[] fmt_justifyLeft = {chr_Esc, 0x61, 0x00};
    private byte[] fmt_justifyCenter = {chr_Esc, 0x61, 0x01};
    private byte[] fmt_justifyRight = {chr_Esc, 0x61, 0x02};


    public void setPrintStream(OutputStream printStream) {
        this.printStream = printStream;
    }

    public OutputStream getPrintStream() {
        return printStream;
    }

    private void writeTo(byte[] stream) {
        try {
            getPrintStream().write(stream);
        } catch (IOException ioe) {
            Log.i("Write to Stream", ioe.getMessage());
        }
    }

    private void writeTo(Byte stream) {
        try {
            getPrintStream().write(stream.byteValue());

        } catch (IOException ioe) {
            Log.i("Write to Stream", ioe.getMessage());

        }
    }

    public void alignLeft() {
        writeTo(fmt_justifyLeft);
    }

    public void initialize() {
        writeTo(fmt_initialize);
    }

    public void textSmall(String msg) {
        writeTo(fmt_fontSmall);
        writeTo(msg.getBytes());
    }

    public void textMedium (String msg) {
        writeTo(fmt_fontMedium);
        writeTo(msg.getBytes());
    }

    public void textLarge (String msg) {
        writeTo(fmt_fontLarge);
        writeTo(msg.getBytes());
        writeTo(fmt_fontNormal);

    }

    public void textTitle (String msg) {
        writeTo(fmt_fontLarge);

        byte[] boldOn = {chr_Esc, 0x45, 0x01};
        byte[] boldOff = {chr_Esc, 0x45, 0x00};
        writeTo(boldOn);
        writeTo(fmt_justifyCenter);
        textUnderline(msg, 2);
        writeTo(boldOff);
        writeTo(chr_linefeed);
        writeTo(fmt_fontNormal);
        writeTo(fmt_justifyLeft);
    }
    public void textTitle2 (String msg) {
        writeTo(fmt_fontTitle2x);
        writeTo(fmt_justifyCenter);
        byte[] boldOn = {chr_Esc, 0x45, 0x01};
        byte[] boldOff = {chr_Esc, 0x45, 0x00};
        writeTo(boldOn);
        textUnderline(msg, 2);
        writeTo(boldOff);
        writeTo(chr_linefeed);
        writeTo(fmt_fontNormal);
        writeTo(fmt_justifyLeft);
    }

    public void imgLogo (byte[] img) {
        byte[] imgInit = {0x1B, 0x2A, 33, (byte) 255, 3};

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(imgInit);
            outputStream.write(img);
            byte[] imgFinal = outputStream.toByteArray();
            writeTo(imgFinal);
        } catch (IOException ioe) {

        }

        writeTo(chr_linefeed);
    }

    public void textLF () {
        writeTo(chr_linefeed);
    }

    public void SendDataByte(byte[] data) {
        writeTo(data);
    }

    public void SendDataString(String data) {
        try {

            writeTo(data.getBytes("GBK"));
        } catch (UnsupportedEncodingException uee) {

        }
    }


    public void imgBarcode (String msg) {
        byte[] barcodeModel = {0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x31, 0x00};
        byte[] barcodeSize = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x31};
        byte[] barcodeCorrection = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31};
        byte[] barcodeStore = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31};
//        byte[] barcodeTest = {chr_Esc, 0x90, };
        //1D		28		6B		03		00		31		51		m
        byte[] barcodePrint = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30};
        writeTo(fmt_initialize);
        //writeTo(barcodeModel);
        //writeTo(barcodeSize);
        //writeTo(barcodeCorrection);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(barcodeStore);
            outputStream.write(msg.getBytes());
            byte[] imgBarcodeStore = outputStream.toByteArray();
            writeTo(imgBarcodeStore);
            writeTo(chr_linefeed);
            writeTo(barcodePrint);
        } catch (IOException ioe) {

        }
        writeTo(chr_linefeed);
    }

    public void textUnderline (String msg, int thickness) {
        if (thickness >= 2) {
            thickness = 2;
        } else if (thickness<=1) {
            thickness = 1;
        } else {
            thickness = 1;
        }

        byte[] thicknessOn1 = {chr_Esc, 0x2D,0x01};
        byte[] thicknessOn2 = {chr_Esc, 0x2D,0x02};
        byte[] thicknessOff = {chr_Esc, 0x2D,0x00};

        if (thickness==2) {
            writeTo(thicknessOn2);
        } else {
            writeTo(thicknessOn1);
        }
        writeTo(msg.getBytes());
        writeTo(thicknessOff);

    }

    public void textItalics (String msg) {
        byte[] italicsOn = {chr_Esc, 0x34, 0x01};
        byte[] italicsOff = {chr_Esc, 0x34, 0x00};
        writeTo(italicsOn);
        writeTo(msg.getBytes());
        writeTo(italicsOff);
    }

    public void textBold (String msg) {
        byte[] boldOn = {chr_Esc, 0x45, 0x01};
        byte[] boldOff = {chr_Esc, 0x45, 0x00};
        writeTo(boldOn);
        writeTo(msg.getBytes());
        writeTo(boldOff);
    }

    public void textReverse(String msg) {
        byte[] reverseOn = {0x1D, 0x42, 0x01};
        byte[] reverseOff = {0x1D, 0x42, 0x01};
        writeTo(reverseOn);
        writeTo(msg.getBytes());
        writeTo(reverseOff);
    }

    public void writeBarCode() {} {

    }

    public static byte[] getBarCommand(String str, int nVersion, int nErrorCorrectionLevel,
                                       int nMagnification){

        if(nVersion<0 | nVersion >19 | nErrorCorrectionLevel<0 | nErrorCorrectionLevel > 3
                | nMagnification < 1 | nMagnification > 8){
            return null;
        }

        byte[] bCodeData = null;
        try
        {
            bCodeData = str.getBytes("GBK");

        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return null;
        }

        byte[] command = new byte[bCodeData.length + 7];

        command[0] = 27;
        command[1] = 90;
        command[2] = ((byte)nVersion);
        command[3] = ((byte)nErrorCorrectionLevel);
        command[4] = ((byte)nMagnification);
        command[5] = (byte)(bCodeData.length & 0xff);
        command[6] = (byte)((bCodeData.length & 0xff00) >> 8);
        System.arraycopy(bCodeData, 0, command, 7, bCodeData.length);

        return command;
    }


    public static byte[] getCodeBarCommand(String str, int nType, int nWidthX, int nHeight,
                                           int nHriFontType, int nHriFontPosition){

        if (nType < 0x41 | nType > 0x49 | nWidthX < 2 | nWidthX > 6
                | nHeight < 1 | nHeight > 255 | str.length() == 0)
            return null;

        byte[] bCodeData = null;
        try
        {
            bCodeData = str.getBytes("GBK");

        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return null;
        }

        byte[] command = new byte[bCodeData.length + 16];

        command[0] = 29;
        command[1] = 119;
        command[2] = ((byte)nWidthX);
        command[3] = 29;
        command[4] = 104;
        command[5] = ((byte)nHeight);
        command[6] = 29;
        command[7] = 102;
        command[8] = ((byte)(nHriFontType & 0x01));
        command[9] = 29;
        command[10] = 72;
        command[11] = ((byte)(nHriFontPosition & 0x03));
        command[12] = 29;
        command[13] = 107;
        command[14] = ((byte)nType);
        command[15] = (byte)(byte) bCodeData.length;
        System.arraycopy(bCodeData, 0, command, 16, bCodeData.length);


        return command;
    }


}
