package util;
import java.io.*;
import java.util.Arrays;
import javax.security.cert.X509Certificate;

import server.Doctor;
import server.Government;
import server.Journal;
import server.Nurse;
import server.Patient;
import server.User;
import server.Variables;

/**
 * Created by Tank on 2/11/2016.
 */
public class Parser {
	public static String[] parseLine(String filename) {
		return filename.split("\\$");
	}

	// public static StringBuilder getJournalList() {
	// StringBuilder sb = new StringBuilder();
	// File temp = new File(Variables.JOURNAL_FOLDER);
	// for (File f : temp.listFiles()) {
	// sb.append(f.getName() + "\n");
	// }
	// return sb;
	// }
	public static StringBuilder arrayToString(String[] array) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i] + " ");
		}
		return sb;
	}

	public static StringBuilder createFieldStructure(String[] array) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; (i < array.length); i++) {
			// if(i < Variables.FIELDS.length-1){

			sb.append(array[i] + "\n");
		}
		// else sb.append(array[i]+ " ");

		return sb;
	}
	// for (int i = Variables.FIELDS.length; i < array.length; i++) {
	// sb.append(array[i]+ " ");
	// }

	public static String formatNewLine(String[] response) {
		String data;
		for (int i = 0; i < response.length; i++) {
			response[i] = response[i].replaceAll("\\r?\\n", "\\$").trim();
		}
		data = Parser.arrayToString(response).toString();
		return data;
	}

	public static User generateUserFromCert(X509Certificate cert) {
		User user = null;
		String certFields = cert.getSubjectDN().getName();
		String name = certFields.substring(certFields.indexOf("CN=") + 3, certFields.indexOf(','));
		String issuerFields = cert.getIssuerDN().getName();
		String issuerType = issuerFields.substring(issuerFields.indexOf("CN=") + 3, issuerFields.indexOf(','));
		// System.out.println("This is the registered issuertype: " +
		// issuerType);
		switch (issuerType) {
		case "Doctors":
			String division = extractDivision(certFields);
			// System.out.println("This is a Doctor! " + " Name: " + name + "
			// Division: " + division );
			user = new Doctor(name, division);
			break;
		case "Nurses":
			String divisionNurse = extractDivision(certFields);
			// System.out.println("This is a Nurse! " + " Name: " + name + "
			// Division: " + divisionNurse );
			user = new Nurse(name, divisionNurse);
			break;
		case "Government":
			// System.out.println("This is a Gov! " + " Name: " + name);
			user = new Government(name);
			break;
		case "Patients":
			// System.out.println("This is a Noob! " + " Name: " + name);
			user = new Patient(name);
			break;
		}
		return user;

	}

	private static String extractDivision(String certFields) {
		String divToEnd = certFields.substring(certFields.lastIndexOf("OU=") + 3, certFields.length());
		String division = divToEnd.substring(0,
				(divToEnd.indexOf(',') == -1) ? divToEnd.length() : divToEnd.indexOf(','));
		return division;
	}

	public static boolean printToFile(String[] filedata) {
		try {
			PrintWriter pw = new PrintWriter(new File(Variables.JOURNAL_FOLDER + filedata[0]));
			StringBuilder sb = Parser.createFieldStructure(Arrays.copyOfRange(filedata, 1, filedata.length));
			
			pw.println(sb.toString());
			pw.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	// public static boolean
	public static Journal createJournalFromFile(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String patient, doctor, nurse, division;
		try {
			br.skip(Variables.FIELDS[0].length());
			patient = br.readLine().trim();
			br.skip(Variables.FIELDS[1].length());
			doctor = br.readLine().trim();
			br.skip(Variables.FIELDS[2].length());
			nurse = br.readLine().trim();
			br.skip(Variables.FIELDS[3].length());
			division = br.readLine().trim();
		} catch (NullPointerException e) {
			br.close();
			return null;
		}
		br.close();
		return new Journal(patient, doctor, nurse, division, new File(filename));
	}

	public static String[] listJournals() {
		File folder = new File(Variables.JOURNAL_FOLDER);
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (files != null) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < files.length; i++) {
					sb.append(files[i].getName() + " ");
				}
				return new String[] { ResponseCode.Success.toString(), sb.length() + "", sb.toString() };
			}
		}
		return new String[] { ResponseCode.FileNotFound.toString() };
	}

}
