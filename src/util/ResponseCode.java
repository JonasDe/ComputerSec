package util;
/**
 * Created by Tank on 2/15/2016.
 */
public enum ResponseCode {
    Success, Failure, FileNotFound, FileNotCreated;

    public static ResponseCode fromInteger(int x) {
        switch (x) {
            case 0:
                return Success;
            case 1:
                return Failure;
            case 2:
                return FileNotFound;
            case 3:
                return FileNotCreated;
        }
        return null;
    }

    public String toString() {
        switch (this) {
            case Success:
                return "0";
            case Failure:
                return "1";
            case FileNotFound:
                return "2";
            case FileNotCreated:
                return "3";
            default:
                return "-1";
        }

    }
}
