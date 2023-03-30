module eu.hansolo.crac8 {
    requires java.management;
    //requires transitive org.crac;
    requires com.google.gson;

    opens eu.hansolo.crac8 to com.google.gson;

    exports eu.hansolo.crac8;
}