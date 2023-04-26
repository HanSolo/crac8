package eu.hansolo.crac8;

import java.util.Objects;


public record NameDto (String objectId, String Name, String Gender, String createdAt, String updatedAt) {
    @Override public boolean equals(Object obj) {
        if (obj == this) { return true; }
        if (obj == null || obj.getClass() != this.getClass()) { return false; }
        var that = (NameDto) obj;
        return Objects.equals(this.objectId, that.objectId()) && Objects.equals(this.Name, that.Name()) && Objects.equals(this.Gender, that.Gender());
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
