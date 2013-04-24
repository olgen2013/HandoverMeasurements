package utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public class Logger {

	private static Logger instance = null;
	private String strFileName = "NONE";
	private boolean fistLine = false;

	private Logger() {}

	private static Logger getInstance() {
		if(instance == null) {
			instance = new Logger();
		}
		return instance;
	}

	public static void createLogFile(String appModeLable){
		Logger logger = Logger.getInstance();
		// create log-file name, log file will be created implicit in the log-method
		logger.strFileName = logger.createFileName(appModeLable);
	}

	public static void log(JSONObject input){
		Logger logger = Logger.getInstance();
		if(logger.fistLine == false){
			StorageAccess.writeFileToSDCard(logger.strFileName, logger.createLogHeader(input));
			StorageAccess.writeFileToSDCard(logger.strFileName, logger.createLogMessage(input));
			logger.fistLine = true;
		}
		else{
			StorageAccess.writeFileToSDCard(logger.strFileName, logger.createLogMessage(input));	
		}
	}

	public static void close(){
		instance = null;
	}

	private String createFileName(String appModeLable) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SS", Locale.GERMANY);
		String stringRepresetnation = sdf.format(date);

		return appModeLable + stringRepresetnation + ".csv";
	}

	private  String createLogHeader(JSONObject jo) {
		
		String header = "";
		boolean comma = false;
		Iterator<String> keys = jo.keys();

		while( keys.hasNext() ){
			String key = (String)keys.next();
			if (comma == false){
				header = header + key;
				comma = true;
			}
			else
				header = header + "," + key;				
		}
		return header + "\n";
	}	

	private String createLogMessage(JSONObject jo) {
		
		String instance = "";
		boolean comma = false;
		Iterator<String> keys = jo.keys();
		
		while( keys.hasNext() ){
			try {
				String key = (String)keys.next();
				if (comma == false){
					instance = instance + jo.get(key);
					comma = true;
				}
				else
					instance = instance  + "," + jo.get(key);				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return instance  + "\n";
	}	
}
