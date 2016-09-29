package server;
import util.Privileges;

public class Patient extends User{

	public Patient(String id) {
		super(id);
		super.priv = new Privileges[]{Privileges.Read};
	}

}
