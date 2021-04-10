package be.rapnhap.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import android.app.Activity;
import android.view.View.OnClickListener;

import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.EnvironmentCompat;
import androidx.print.PrintHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// needed for BT
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity<AppCompatActivity> extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "be.rapnhap.myfirstapp.MESSAGE";
    private static final String LOG_TAG = "Log Tag";

    // BT variables
    // will show the statuses like bluetooth open, close or data sent
    TextView myLabel;
    // will enable user to enter any text to be printed
    EditText myTextbox;
    // android built in classes for bluetooth operations
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    // needed for communication to bluetooth device / network
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "My first app - debug");

        // BT code
        try {
            // we are going to have three buttons for specific functions
            Button openButton = (Button) findViewById(R.id.open);
            Button sendButton = (Button) findViewById(R.id.send);
            Button closeButton = (Button) findViewById(R.id.close);

            Button imagePrintButton = (Button) findViewById(R.id.butImagePrint);
            imagePrintButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        doBitmapPrint();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });



            // text label and input box
            myLabel = (TextView) findViewById(R.id.label);
            myTextbox = (EditText) findViewById(R.id.entry);

            // open bluetooth connection
            openButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        findBT();
                        openBT();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // send data typed by the user to be printed
            sendButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        sendData();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // close bluetooth connection
            closeButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        closeBT();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

        }catch(Exception e) {
            e.printStackTrace();
        }



        /** Called when the user taps the Exit button */
        Button exitButton = (Button) findViewById(R.id.button2);
        exitButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
                System.exit(0);
            }
        });
    }

    void doBitmapPrint() {
        PrintHelper bitmapPrinter = new PrintHelper(this);
        bitmapPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.image);
        bitmapPrinter.printBitmap("text",bitmap);
    }


    // this will find a bluetooth printer device
    void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null) {
                myLabel.setText("No bluetooth adapter available");
            }

            if(!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            // found : BC:A5:8B:9E:E1:38 - 04:FE:A1:87:85:AE
            if(pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    if (device.getName().equals("TM-P80_001306")) {
                        mmDevice = device;
                        break;
                    }
                }
            }

            myLabel.setText("Bluetooth device found.");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // tries to open a connection to the bluetooth printer device
    void openBT() throws IOException {
        try {

            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            myLabel.setText("Bluetooth Opened");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * after opening a connection to bluetooth printer device,
     * we have to listen and check if a data were sent to be printed.
     */
    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myLabel.setText(data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this will send text data to be printed by the bluetooth printer
    void sendData() throws IOException {
        try {

            // the text typed by the user
            //msg +=
            //myTextbox.getText().toString();
            //msg += "TEST PRINT\nLine2\n";
            //mmOutputStream.write(msg.getBytes());

            // initialize printer
            mmOutputStream.write(27); // ESC
            mmOutputStream.write('@');

            // GS "!" 0x11 - Select character size: (horizontal (times 3) x vertical (times 3))
            //byte[] bigC = new byte[] { 29, '!', 0x22 };
            //mmOutputStream.write(bigC);
            // GS "!" 0x22 - Select character size: (horizontal (times 2) x vertical (times 2))
            byte[] bigC2 = new byte[] { 29, '!', 0x11 };
            mmOutputStream.write(bigC2);
            // ESC "E" n - Emphasised ON
            byte[] emOn = new byte[] { 27, 'E', 1 };
            mmOutputStream.write(emOn);
            // print text
            String msg = "RAP 'N HAP\n\n";
            mmOutputStream.write(msg.getBytes());
            // ESC "E" n - Emphasised OFF
            byte[] emOff = new byte[] { 27, 'E', 0 };
            mmOutputStream.write(emOff);

            //msg = " BE 0589 860 067  \n";
            //mmOutputStream.write(msg.getBytes());
            msg = " www.rapnhap.be  \n\n\n";
            mmOutputStream.write(msg.getBytes());

            // feed 2 lines
            mmOutputStream.write(27); // ESC
            mmOutputStream.write('d');
            mmOutputStream.write(1);

            //msg = " 1 A Bieslook     2.00  \n";
            //mmOutputStream.write(msg.getBytes());
            //msg = " 3 A Zalm         9.00  \n";
            //mmOutputStream.write(msg.getBytes());
            msg = " 1 B Champignon   3.00  \n";
            mmOutputStream.write(msg.getBytes());

            // ESC "-" n - Underline on 2 dots
            byte[] ulOn = new byte[] { 27, '-', 2 };
            mmOutputStream.write(ulOn);
            msg = " 4 B Appel       12.00  \n";
            mmOutputStream.write(msg.getBytes());

            // ESC "-" n - Underline off
            byte[] ulOff = new byte[] { 27, '-', 0 };
            mmOutputStream.write(ulOff);

            //msg = "              --------  \n";
            //mmOutputStream.write(msg.getBytes());
            msg = " TOTAAL          26.00  \n";
            mmOutputStream.write(msg.getBytes());


            // feed 5 lines
            mmOutputStream.write(27); // ESC
            mmOutputStream.write('d');
            mmOutputStream.write(2);

            // cut paper
            //outputStream.write(29); // GS
            //outputStream.write('V');
            //outputStream.write(48);
            // cut that paper!
            byte[] cutP = new byte[] { 0x1d, 'V', 1 };
            mmOutputStream.write(cutP);

            // tell the user data were sent
            myLabel.setText("Data sent.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // close the connection to bluetooth printer.
    void closeBT() throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            myLabel.setText("Bluetooth Closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Called when the user taps the Send button */
    public void sendMessage(View view) throws IOException {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();

        // TEST : write some data into file
        String fileDate = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String fileName = fileDate + "_POS.txt";
        Boolean fileExists = false;
        String data = null;

        // Get external file directory FOR THIS APP ...
        //-----------------------------------------------------------
        /*
        File[] pathHandle = new File[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pathHandle = this.getExternalFilesDirs("external");
        }
        File fileHandle = new File(pathHandle[1], fileName);
         */


        // Hard-coded directory that this app can access
        //-----------------------------------------------------------
        try {
            // /storage/6463-3031/Rapnhap/ CRASHES
            // ERROR is java.io.FileNotFoundException: /storage/6463-3031/Rapnhap/20201111_POS.txt (Permission denied)
            //File pathHandle = new File("/storage/6463-3031/Rapnhap/");

            // /storage/6463-3031/Android/data/be.rapnhap.myfirstapp/files/external/ SUCCEEDS !
            File pathHandle = new File("/storage/6463-3031/Android/data/be.rapnhap.myfirstapp/files/external/");


            //File pathHandle = new File("/storage/6463-3031/TestSamsung/");
            // java.io.FileNotFoundException: /storage/6463-3031/TestSamsung/20201210_POS.txt (No such file or directory)

            File fileHandle = new File(pathHandle, fileName);

            fileExists = fileHandle.exists();

            FileWriter writer = new FileWriter(fileHandle, true);

            String dataDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

            // write header
            if (!fileExists) {
                data = dataDate + " - Header data in  " + fileHandle.toString() + "\r\n";
                writer.append(data);
            }

            // write data
            dataDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            data = dataDate + " - Details in  " + fileHandle.toString() + "\r\n";
            writer.append(data);

            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Samsung info
        // ----------------------------------------------------------------------------------------------
        File dir = new File(Environment.getExternalStorageDirectory(),
                "SamsungDirectoryName" );
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        String myFilePath = dir.getAbsolutePath() + File.separator + "SamsungFileName";

        // Now use this file path with *FileOutputStream *to write data in your text
        // file. You can find a lot of guidelines on the internet.
        FileOutputStream streamS = null;
        try {
            streamS = new FileOutputStream(myFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            data += "\n\n SAMSUNG hint writes to " + myFilePath.toString() + "\r\n";
            String logString1 = new String(data);
            streamS.write(logString1.getBytes());
            data += "\n\n SAMSUNG hint written to " + myFilePath.toString() + "\r\n";
            // check if we can add to the same file
            streamS.write(logString1.getBytes());

        } finally {
            streamS.close();
        }






        message = message + " - " + data;
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    // ------------------------------


    /* returns external storage paths (directory of external memory card) as array of Strings */
    public String[] getExternalStorageDirectories() {

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = getExternalFilesDirs(null);
            String internalRoot = Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase();

            for (File file : externalDirs) {
                if(file==null) //solved NPE on some Lollipop devices
                    continue;
                String path = file.getPath().split("/Android")[0];

                if(path.toLowerCase().startsWith(internalRoot))
                    continue;

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                }
                else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(path);
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d(LOG_TAG, results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    Log.d(LOG_TAG, results.get(i)+" might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }

}