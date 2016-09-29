package server;
import util.Privileges;

public class Doctor extends User{

	public Doctor(String id, String division){
		super(id);
		super.division = division;
		priv = new Privileges[]{Privileges.Read, Privileges.Write, Privileges.Create, Privileges.List};
	}
	
}
