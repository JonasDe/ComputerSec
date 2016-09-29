package server;
import java.io.*;
import java.util.Arrays;

import util.Parser;
import util.Privileges;
import util.ResponseCode;

/**
 * Created by Tank on 2/11/2016.
 */
public class Authenticator {

	public static final String[] Success = new String[] { ResponseCode.Success.toString() };
	public static final String[] FileNotFound = new String[] { ResponseCode.FileNotFound.toString() };
	public static final String[] FileNotCreated = new String[] { ResponseCode.FileNotCreated.toString() };
	public static final String[] Failure = new String[] { ResponseCode.Failure.toString() };

	public Authenticator() throws IOException {
	}

	private Journal createJournalFromFile(String filename) throws IOException {
		return Parser.createJournalFromFile(filename);
	}

	private boolean createJournalFile(String[] filedata) {
		return Parser.printToFile(filedata);
	}

	private String[] getJournalList() {
		return Parser.listJournals();

	}

	/**
	 * This method authenticates the user and gives the corresponding data that
	 * is requested. The return type is a string array with the data requested.
	 *
	 * @param request
	 *            the request on of enum type Priviliges
	 * @param user
	 *            the user requesting to be authenticated
	 * @param filedata
	 *            filedata on the format {Request, filename, data} where the
	 *            data field depends on the reuqest according to the protocol.
	 * @return a String[] containing the outcome, depending on the protocol. The
	 *         case of Read sends back all the data in the journal.
	 */
	public String[] authenticateAndRetrieveData(Privileges request, User user, String[] filedata) {
		if (!user.hasPrivilege(request))
			return Failure;
		switch (request) {
		case Write:
			return authenticateWrite(request, user, filedata);
		case Read:
			return authenticateRead(request, user, filedata);
		case Delete:
			return authenticateDelete(request, user, filedata);
		case Create:
			return authenticateCreate(request, user, filedata);
		case List:
			return authenticateList(request, user, filedata);
		}
		return Failure;
	}

	private String[] authenticateWrite(Privileges request, User user, String[] filedata) {
		Journal journal = getJournal(filedata);
		if (journal != null) {
			if (journal.getDoctor().equals(user.toString()) || journal.getNurse().equals(user.toString())) {
				return (Parser.printToFile(Arrays.copyOfRange(filedata, 1, filedata.length)) ? Success
						: FileNotCreated);
			} else
				return Failure;
		}
		return FileNotFound;

	}

	private String[] authenticateRead(Privileges request, User user, String[] filedata) {
		Journal journal = getJournal(filedata);
		return (journal == null) ? FileNotFound : (journal.getAccess(user, request) ? readData(journal) : Failure);
	}

	private String[] authenticateDelete(Privileges request, User user, String[] filedata) {
		Journal journal = getJournal(filedata);
		return (journal == null) ? FileNotFound : (journal.deleteJournal() ? Success : FileNotFound);
	}

	private String[] authenticateCreate(Privileges request, User user, String[] filedata) {
		Journal journal = getJournal(filedata);
		if (journal == null)
			return Parser.printToFile(Arrays.copyOfRange(filedata, 1, filedata.length)) ? Success : FileNotCreated;
		return journal.getAccess(user, request) && createJournalFile(Arrays.copyOfRange(filedata, 1, filedata.length))
				? Success : Failure;
	}

	private String[] authenticateList(Privileges request, User user, String[] filedata) {
		return getJournalList();

	}

	private Journal getJournal(String[] filedata) {
		Journal journal = null;
		if (filedata.length > 1) {
			try {
				journal = createJournalFromFile(Variables.JOURNAL_FOLDER + filedata[1].trim());
			} catch (IOException e) {
				journal = null;
			}
		}
		return journal;
	}

	/**
	 * Concatenates the whole journal file.
	 *
	 * @param j
	 *            the Journal Object containing the File with the journal data
	 * @return a String with data
	 */
	private String[] readData(Journal j) {
		String data = "";
		try {
			data = j.getData();
		} catch (FileNotFoundException e) {
			return Authenticator.FileNotFound;
		}
		return new String[] { ResponseCode.Success.toString(), data };
	}
}
