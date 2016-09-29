package server;
import util.Privileges;

public class Government extends User{

	public Government(String id) {
		super(id);
		priv = new Privileges[]{Privileges.Read, Privileges.Delete, Privileges.List};
	}
	
}
