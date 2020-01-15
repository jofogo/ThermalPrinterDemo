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

public class PrinterSettings {
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



    public void imgBarcode (String msg) {
        byte[] code = getBarCommand(msg,1,3,8);
        writeTo(new byte[]{0x1b, 0x61, 0x00});
        writeTo(code);

        writeTo(chr_linefeed);
    }

    public void imgBitmap(Bitmap bmp){

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        byte data[]=new byte[1024*10];
        data[0] = 0x1D;
        data[1] = 0x2A;
        data[2] =(byte)( (width - 1)/ 8 + 1);
        data[3] =(byte)( (height - 1)/ 8 + 1);
        byte k = 0;
        int position = 4;
        int i;
        int j;
        byte temp = 0;
        for(i = 0; i <width;  i++){

            for(j = 0; j < height; j++){
                if(bmp.getPixel(i, j) != -1){
                    temp |= (0x80 >> k);
                } // end if
                k++;
                if(k == 8){
                    data[position++] = temp;
                    temp = 0;
                    k = 0;
                } // end if k
            }// end for j
            if(k % 8 != 0){
                data[position ++] = temp;
                temp = 0;
                k = 0;
            }

        }

        if( width% 8 != 0){
            i =   height/ 8;
            if(height % 8 != 0) i++;
            j = 8 - (width % 8);
            for(k = 0; k < i*j; k++){
                data[position++] = 0;
            }
        }
        writeTo(data);
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



}
