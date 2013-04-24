package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.util.Log;

public class StorageAccess {
	
	private static final String TAG = StorageAccess.class.getSimpleName();
	private static boolean mExternalStorageAvailable = false;
	private static boolean mExternalStorageWriteable = false;
	
	
	public static String readFileFromSDCard(String strFilename) {
		String aBuffer = "";
		
		if(StorageAccess.checkStorage() && StorageAccess.mExternalStorageAvailable == true) {
			try {
				File directory = Environment.getExternalStorageDirectory();
				
				File myFile = new File(directory, strFilename);
				FileInputStream fIn = new FileInputStream(myFile);
				BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
				String aDataRow = "";
				
				while ((aDataRow = myReader.readLine()) != null) {
					aBuffer += aDataRow + "\n";
				}
				myReader.close();
			}
			catch (Exception e) {
				Log.e(TAG, "Could not read from file " + e.getMessage());
			} 
		}
		return aBuffer;
	}
	
	public static void writeFileToSDCard(String strFilename, String strContent) {
		
		if(StorageAccess.checkStorage() && StorageAccess.mExternalStorageWriteable == true) {
			File directory = null;
			directory = Environment.getExternalStorageDirectory();
	
			try {
		        File myFile = new File(directory, strFilename);
				myFile.createNewFile();
				
				FileOutputStream fOut = new FileOutputStream(myFile, true);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				
				myOutWriter.append(strContent);
				
				myOutWriter.close();
				fOut.close();    
			} 
			catch (Exception e) {
			    Log.e(TAG, "Could not write file " + e.getMessage());
			}
		}
	}
	
	public static boolean checkStorage() {
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    StorageAccess.mExternalStorageAvailable = StorageAccess.mExternalStorageWriteable = true;
		    return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
			StorageAccess.mExternalStorageAvailable = true;
			StorageAccess.mExternalStorageWriteable = false;
			return true;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
			StorageAccess.mExternalStorageAvailable = StorageAccess.mExternalStorageWriteable = false;
			return false;
		}
	}
}
