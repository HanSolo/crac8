package eu.hansolo.crac8;

public enum Gender {
    MALE, FEMALE, NEUTRAL;

    public static Gender fromText(final String text) {
        switch (text) {
            case "m", "M", "male", "Male", "MALE", "boy", "Boy", "BOY"          -> { return Gender.MALE;    }
            case "f", "F", "female", "Female", "FEMALE", "girl", "Girl", "GIRL" -> { return Gender.FEMALE;  }
            default                                                             -> { return Gender.NEUTRAL; }
        }
    }
}
