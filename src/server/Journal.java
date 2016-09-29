package server;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

import util.Privileges;

public class Journal {
    private String patient;
    private String nurse;
    private String doctor;
    private String division;
    private File file;

    public Journal(String patient, String doctor, String nurse, String division, File file) {
        this.patient = patient;
        this.doctor = doctor;
        this.nurse = nurse;
        this.division = division;
        this.file = file;
    }
    
    public String getDoctor(){
    	return doctor;
    }
    public String getNurse(){
    	return nurse;
    }

    public boolean getAccess(User user, Privileges request) {
        if (!user.hasPrivilege(request)) {
            return false;
        }
        if (user.getUserType().equals(Doctor.class.getSimpleName())) {
            boolean b = user.identifyUser(doctor) || user.belongsTo(division);
            return b;
        } else if (user.getUserType().equals(Nurse.class.getSimpleName())) {
            return user.identifyUser(nurse) || user.belongsTo(division);
        } else if (user.getUserType().equals(Patient.class.getSimpleName())) {
            return user.identifyUser(patient);
        } else if (user.getUserType().equals(Government.class.getSimpleName())) {
            return true;
        }
        return false;
    }
    /**
     * Appends the String s to the file of the journal. This does not remove previous entered information.
     *
     * @param s
     * @return false if the append failed or file didn't exist.
     */
    public boolean appendToJournal(String s) {
        try {
            Files.write(Paths.get(file.getPath()), s.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean deleteJournal() {
        return file.delete();

    }

    public long length() {
        return file.length();
    }

    /**
     * Retrieves all data from the file.
     *
     * @return String containing the data of the file.
     * @throws FileNotFoundException
     */
    public String getData() throws FileNotFoundException {
        Scanner scan = new Scanner(file);
        String s = scan.useDelimiter("\\A").next();
        scan.close();
        return s;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof File) {
            return file.getName().equals(((File) o).getName());
        }
        return false;
    }

}
