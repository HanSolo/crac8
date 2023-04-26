module eu.hansolo.crac8 {
    requires java.management;

    requires com.google.gson;
    requires org.crac;

    opens eu.hansolo.crac8 to com.google.gson;

    exports eu.hansolo.crac8;
}