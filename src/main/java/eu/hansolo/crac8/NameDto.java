package eu.hansolo.crac8;

import java.util.Objects;


public class NameDto {
    private String objectId;
    private String Name;
    private String Gender;
    private String createdAt;
    private String updatedAt;


    public NameDto() {
        this("", "", "", "", "");
    }
    public NameDto(final String objectId, final String Name, final String Gender, final String createdAt, final String updatedAt) {
        this.objectId  = objectId;
        this.Name      = Name;
        this.Gender    = Gender;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getObjectId() { return this.objectId; }
    public void setObjectId(final String objectId) { this.objectId = objectId; }

    public String getName() { return this.Name; }
    public void setName(final String Name) { this.Name = Name; }

    public String getGender() { return this.Gender; }
    public void setGender(final String Gender) { this.Gender = Gender; }

    public String getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return this.updatedAt; }
    public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }


    public Name getNameObj() {
        Name name = new Name(toString());
        return name;
    }

    @Override public boolean equals(Object obj) {
        if (obj == this) { return true; }
        if (obj == null || obj.getClass() != this.getClass()) { return false; }
        var that = (NameDto) obj;
        return Objects.equals(this.objectId, that.getObjectId()) && Objects.equals(this.Name, that.getName()) && Objects.equals(this.Gender, that.getGender());
    }
    @Override public int hashCode() {
        return Objects.hash(this.objectId, this.Name, this.Gender);
    }
    @Override public String toString() {
        return new StringBuilder().append("{")
                                  .append("\"").append("first_name").append("\":\"").append(this.Name).append("\",")
                                  .append("\"").append("gender").append("\":\"").append(this.Gender).append("\"")
                                  .append("}")
                                  .toString().replaceAll("\\\\", "");
    }
}
