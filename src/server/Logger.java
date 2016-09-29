package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import util.Privileges;
import util.ResponseCode;

public class Logger {
	private static Logger logger = new Logger("audit.txt");
	
	public static Logger getLogger() {return logger;}

	private File outputFile;
	private BufferedWriter writer;
	
	public Logger(String outputPath) {
		outputFile = new File(outputPath);
		try {
			writer = new BufferedWriter(new FileWriter(outputFile,true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void auditConnection(String clientName, ResponseCode code) throws IOException {
		if (code == ResponseCode.Success)
			writer.write(getTimestamp() + "Client \""+ clientName +"\" successfully connected\n");
		else if (code == ResponseCode.Failure)
			writer.write(getTimestamp() + "Client \""+ clientName +"\" tried to connect but failed authentication\n");
		writer.flush();
	}

	public void auditAction(String clientName, String fileName, Privileges request, ResponseCode code) throws IOException {
		String result;
		switch (code) {
		case Success:
			result = " - success";
			break;
		case Failure:
			result = " - failed";
			break;
		case FileNotCreated:
			result = " - failed (file not created)";
			break;
		case FileNotFound:
			result = " - failed (file not found)";
			break;
		default:
			result = " - result unknown";
			break;
		}
		
		String action;
		switch (request) {
    case Write:
    	action = "wrote to";
    	break;
    case Read:
    	action = "read";
    	break;
    case Delete:
    	action = "deleted";
    	break;
    case Create:
    	action = "created";
    	break;
    case List:
    	writer.write(getTimestamp() + "Client \""+ clientName 
    			+ "\" listed files" + result + "\n");
    	writer.flush();
    	return;
    case Unknown:
    default:
    	writer.write(getTimestamp() + "Client \""+ clientName 
    			+ "\" performed unknown action"+ result + "\n");
    	writer.flush();
    	return;
		}
		
  	writer.write(getTimestamp() + "Client \""+ clientName +"\" " + action 
  			+ " file \"" + fileName + "\" "+ result + "\n");
		writer.flush();
	}
	
	public void auditDisconnection(String name) throws IOException {
		writer.write(getTimestamp() + "Client \"" + name + "\" disconnected");
		writer.flush();
	}

	private String getTimestamp() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second + " ";
	}
}
