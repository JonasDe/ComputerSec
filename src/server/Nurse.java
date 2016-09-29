package server;
import util.Privileges;

public class Nurse extends User {

	public Nurse(String id, String division) {
		super(id);
		super.division = division;
		priv = new Privileges[]{Privileges.Read, Privileges.Write, Privileges.List};
	}



}
