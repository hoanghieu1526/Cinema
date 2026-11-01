package enums;

public enum UserGender {
    FEMALE(0), 
    MALE(1);   

    private final int dbValue;

    UserGender(int dbValue) {
        this.dbValue = dbValue;
    }

    public int getDbValue() {
        return dbValue;
    }

    public static UserGender fromDbValue(int value) {
        return (value == 1) ? MALE : FEMALE;
    }

    public static UserGender fromString(String text) {
        if (text == null) return FEMALE; 
        String cleaned = text.trim();
        if (cleaned.equalsIgnoreCase("MALE") || cleaned.equals("1") || cleaned.equalsIgnoreCase("true")) {
            return MALE;
        }
        return FEMALE;
    }
}
