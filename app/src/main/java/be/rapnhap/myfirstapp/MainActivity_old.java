package be.rapnhap.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.EnvironmentCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity_old extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "be.rapnhap.myfirstapp.MESSAGE";
    private static final String LOG_TAG = "Log Tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "My first app - debug");

    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) throws IOException {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();

        // log what is done in a String
        String logString = "Log writing of files. " + "\r\n";

        // write to file - app specific
        // Not found on my tablet
        // ------------------------------------------------------------------------------------------------------------------
        File file = new File(this.getFilesDir(), "textpath");
        //File file = new File("C:\\", "filename");
        String filename = "myfile";
        //String fileContents = "Hello world!";
        try (FileOutputStream fos = this.openFileOutput(filename, Context.MODE_PRIVATE)) {
            logString = logString + "myfile saved to " + file.toString() + "\r\n";
            fos.write(logString.getBytes());
            fos.flush();
            fos.close();
            Toast.makeText(MainActivity_old.this, "Successfully saved", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity_old.this, "ERROR - " + e.getMessage() , Toast.LENGTH_SHORT).show();
        }

        // write file to external storage
        // not found on my tablet in internal storage/Android/data/be.rapnhap.myfirstapp/files
        // ------------------------------------------------------------------------------------------------------------------
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "RapnHap");
            /*File root = this.getExternalFilesDir(null);
            if (!root.exists()) {
                root.mkdirs();
            }

             */
            //File root = new File("/sdcard/RapnHap/");
            File gpxfile = new File(root, "/myPOSFile.txt");
            FileWriter writer = new FileWriter(gpxfile);
            logString = logString + "/myPOSFile.txt saved to " + gpxfile.toString() + "\r\n";
            writer.append(logString);
            writer.flush();
            writer.close();
            Toast.makeText(this, "Saved myPOSFile.txt", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            logString = logString + e.getMessage() + "\r\n";
            Toast.makeText(MainActivity_old.this, "ERROR - " + e.getMessage() , Toast.LENGTH_SHORT).show();
        }

        // 8 nov
        // ----------------------------------------------------------------
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
           String mounted = "Yes";
            Toast.makeText(MainActivity_old.this, "SD card is mounted" , Toast.LENGTH_SHORT).show();
            logString = logString + "SD Card is mounted" + "\r\n";
        } else {
            Toast.makeText(MainActivity_old.this, "SD card is NOT mounted" , Toast.LENGTH_SHORT).show();
            logString = logString + "SD Card is NOT mounted" + "\r\n";
        }

        // helloworld
        // ------------------------------------------------------------------------------------------------

        String filename1 = "helloworld";
        File sdcard = Environment.getExternalStorageDirectory();
        File f = new File(sdcard, filename1);
        try {
            FileOutputStream fos1 = new FileOutputStream(f);
            //String data = "Hello world, how do you do?";
            fos1.write(logString.getBytes());
            logString = logString + "helloworld written to " + sdcard.toString() + "\r\n";

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity_old.this, "ERROR - " + e.getMessage() , Toast.LENGTH_SHORT).show();
        }

        //
        try
        {
            FileOutputStream fos =
                    openFileOutput("myfile.txt", getApplicationContext().MODE_PRIVATE);
            fos.write(logString.getBytes());
            fos.close();
            logString = logString + "myfile.txt written to " + fos.toString()+ "\r\n";
            }
        catch (Exception e)
        {
            // Do something, not just logging
            e.printStackTrace();
            Toast.makeText(MainActivity_old.this, "ERROR - " + e.getMessage() , Toast.LENGTH_SHORT).show();
        }


        // path -- this is the android > data > br.
        // found on my tablet in internal storage/Android/data/be.rapnhap.myfirstapp/files
        // --------------------------------------------------------------------------------------------------
        File path = this.getExternalFilesDir(null);

        Toast.makeText(MainActivity_old.this, "path: " + path , Toast.LENGTH_SHORT).show();

        File file1 = new File(path, "my-file-name2.txt");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            logString += "my-file-name2.txt written to " + file1.toString() + "\r\n";

            String[] stringArray = getExternalStorageDirectories();
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < stringArray.length; i++) {
                sb.append(stringArray[i]);
            }
            String str = sb.toString();
            /*
            String str = "temp";
             */

            logString += "getExternalStorageDirectories()"        + str + "\r\n";
            String logString1 = new String(logString);
            stream.write(logString.getBytes());
        } finally {
            stream.close();
        }

        // test file 3
        //----------------------------------------------------------
        //File path3 = this.getDir("/storage/6463-3031/RAPNHAP", 1); // CRASHES
        // File path3 = this.getExternalFilesDir(null); // same as file2
        //File path3 = new File("/storage/sdcard1"); // CRASHES
        File path3 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        File file3 = new File(path3, "my-file-name3.txt");
        FileOutputStream stream3 = null;
        try {
            stream3 = new FileOutputStream(file3);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            logString += "my-file-name3.txt written to " + file3.toString() + "\r\n";
            String logString1 = new String(logString);
            stream3.write(logString.getBytes());
        } finally {
            stream3.close();
        }

        // test file 4
        //----------------------------------------------------------
        File[] path4 = new File[1];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            path4 = this.getExternalFilesDirs("external");
        }
        //ExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        File file4 = new File(path4[1], "my-file-name4.txt");
        FileOutputStream stream4 = null;
        try {
            stream4 = new FileOutputStream(file4);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            logString += "my-file-name4.txt TRIES write to " + file4.toString() + "\r\n";
            String logString1 = new String(logString);
            stream4.write(logString.getBytes());
            logString += "my-file-name4.txt written to " + file4.toString() + "\r\n";
            // check if we can add to the same file
            stream4.write(logString.getBytes());

        } finally {
            stream4.close();
        }

        //8.
        //-----------------------------------------------------------
                File[] path5 = new File[1];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            path5 = this.getExternalFilesDirs("external");
        }


        /*File path5 = this.getExternalFilesDir(null);
        if (!path5.exists()) {
            path5.mkdirs();
        }

         */
        //File path5 = new File("/sdcard/RapnHap/");
        File file5 = new File(path5[1], "/my-file-name5a.txt");
        FileWriter writer = new FileWriter(file5, true);
        logString = logString + "/my-file-name5a.txt saved to " + file5.toString() + "\r\n";
        writer.append(logString);
        writer.flush();
        writer.close();

        // Samsung info
        // ----------------------------------------------------------------------------------------------
        File dir = new File(Environment.getExternalStorageDirectory(),
                "SamsungDirectoryName" );
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        String myFilePath = dir.getAbsolutePath() + File.separator + "YourFileName";

        // Now use this file path with *FileOutputStream *to write data in your text
        // file. You can find a lot of guidelines on the internet.
        FileOutputStream streamS = null;
        try {
            streamS = new FileOutputStream(file4);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            logString += "my-file-name4.txt TRIES write to " + file4.toString() + "\r\n";
            String logString1 = new String(logString);
            streamS.write(logString.getBytes());
            logString += "my-file-name4.txt written to " + file4.toString() + "\r\n";
            // check if we can add to the same file
            streamS.write(logString.getBytes());

        } finally {
            streamS.close();
        }



        message = message + " - " + logString;
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