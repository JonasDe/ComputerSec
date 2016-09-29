package util;

public enum Privileges {
    Write, Read, Delete, Create, List, Unknown;

    public static Privileges fromInteger(int x) {
        switch (x) {
            case 0:
                return Write;
            case 1:
                return Read;
            case 2:
                return Delete;
            case 3:
                return Create;
            case 4:
                return List;
        }
        return null;
    }

    public String toString() {

        switch (this) {
            case Write:
                return 0+"";
            case Read:
                return 1+"";
            case Delete:
                return 2+"";
            case Create:
                return 3+"";
            case List:
                return 4+"";
            default:
                return "";
        }

    }
}
