package eu.hansolo.crac8;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class Name {
    private String firstName;
    private Gender gender;


    public Name(final String firstName, final Gender gender) {
        this.firstName = firstName;
        this.gender    = gender;
    }
    public Name(final String nameJson) {
        if (null == nameJson || nameJson.isEmpty()) { throw new IllegalArgumentException("name json cannot be null or empty"); }
        final Gson       gson = new Gson();
        final JsonObject json = gson.fromJson(nameJson, JsonObject.class);

        final String firstName = json.get("first_name").getAsString();
        final Gender gender    = Gender.fromText(json.get("gender").getAsString());

        this.firstName = firstName;
        this.gender    = gender;
    }

    public final String getFirstName() { return this.firstName; }

    public final Gender getGender() { return this.gender; }

    @Override public String toString() {
        return new StringBuilder().append("{")
                                  .append("\"").append("first_name").append("\":\"").append(this.firstName).append("\",")
                                  .append("\"").append("gender").append("\":\"").append(this.gender.name().toLowerCase()).append("\"")
                                  .append("}")
                                  .toString().replaceAll("\\\\", "");
    }
}
