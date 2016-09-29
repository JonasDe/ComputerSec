package server;
import util.Privileges;

public abstract class User {
    private String id;
    protected Privileges[] priv;
    protected String division;

    public User(String id) {
        this.id = id;
        this.division = "";
    }

    public Privileges[] getPrivileges() {
        return priv;
    }

    public boolean hasPrivilege(Privileges request) {
        for (int i = 0; i < priv.length; i++) {
            if (priv[i].equals(request)) {
                return true;
            }
        }
        return false;
    }

    public boolean identifyUser(String user) {
        return id.trim().equals(user.trim());
    }


    public String getUserType() {
        return getClass().getSimpleName();
    }

    public String toString() {
        return id;
    }

    public boolean belongsTo(String division) {
        return this.division.equals(division);
    }

}
