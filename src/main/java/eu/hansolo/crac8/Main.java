package eu.hansolo.crac8;

import org.crac.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;


public class Main implements Resource {
    private static final String     NAMES_FILE        = "names.json";
    private static final long       MILLISECOND_IN_NS = 1_000_000;
    private              List<Name> allNames;
    private static       long       startTime;


    public Main() {
        System.out.println("Start without CRaC");

        Core.getGlobalContext().register(Main.this);

        init();

        printRandomGirlNames(5);

        printRandomBoyNames(5);

        System.out.println("Time to first response         -> " + ((System.nanoTime() - startTime) / MILLISECOND_IN_NS) + "ms");
        System.out.println("Total number of loaded classes -> " + ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        System.out.println("Total time of compilation      -> " + ManagementFactory.getCompilationMXBean().getTotalCompilationTime() + "ms");
    }

    private void init() {
        allNames = loadNames();
    }


    @Override public void beforeCheckpoint(Context<? extends Resource> context) throws Exception { }

    @Override public void afterRestore(Context<? extends Resource> context) throws Exception {
        System.out.println("Start using CRaC");

        startTime = System.nanoTime();

        printRandomGirlNames(5);

        printRandomBoyNames(5);

        System.out.println("Time to first response         -> " + ((System.nanoTime() - startTime) / MILLISECOND_IN_NS) + "ms");
        System.out.println("Total number of loaded classes -> " + ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        System.out.println("Total time of compilation      -> " + ManagementFactory.getCompilationMXBean().getTotalCompilationTime() + "ms");
    }


    private List<Name> getRandomNames(final List<Name> allNames, final int numberOfRandomNames, final Gender gender) {
        if (numberOfRandomNames > allNames.size()) { throw new IllegalArgumentException("Given numberOfRandomNames cannot be greater than number of all names"); }
        if (numberOfRandomNames < 1) { throw new IllegalArgumentException("numberOfRandomNames should at least be 1"); }

        final List<Name> namesByGivenGender = allNames.stream().filter(name -> gender == name.getGender()).collect(Collectors.toList());
        if (namesByGivenGender.isEmpty()) { return new ArrayList<>(); }

        final Random    rnd         = new Random();
        final Set<Name> randomNames = new HashSet<>();
        while(randomNames.size() < numberOfRandomNames) {
            randomNames.add(namesByGivenGender.get(rnd.nextInt(namesByGivenGender.size() - 1)));
        }
        return new ArrayList<>(randomNames);
    }

    private void printRandomGirlNames(final int amount) {
        final List<Name> randomGirlNames = getRandomNames(allNames, amount, Gender.FEMALE);
        Collections.sort(randomGirlNames, Comparator.comparing(Name::getFirstName));
        final List<String> orderedFirstNames = randomGirlNames.stream().map(name -> name.getFirstName()).collect(Collectors.toList());

        System.out.println("\n" + amount + " random names for girls:");
        orderedFirstNames.forEach(firstName -> System.out.println(firstName));
        System.out.println();
    }

    private void printRandomBoyNames(final int amount) {
        final List<Name> randomBoyNames = getRandomNames(allNames, amount, Gender.MALE);
        Collections.sort(randomBoyNames, Comparator.comparing(Name::getFirstName));
        final List<String> orderedFirstNames = randomBoyNames.stream().map(name -> name.getFirstName()).collect(Collectors.toList());

        System.out.println("\n" + amount + " random names for boys:");
        orderedFirstNames.forEach(firstName -> System.out.println(firstName));
        System.out.println();
    }

    private List<Name> loadNames() {
        final long       start      = System.nanoTime();
        final List<Name> namesFound = new ArrayList<>();
        try(JsonReader jsonReader = new JsonReader(new InputStreamReader(Main.class.getResourceAsStream(NAMES_FILE), StandardCharsets.UTF_8))) {
            Gson gson = new GsonBuilder().create();
            jsonReader.beginArray();
            while (jsonReader.hasNext()){
                NameDto nameDto = gson.fromJson(jsonReader, NameDto.class);
                namesFound.add(new Name(nameDto.toString()));
            }
            jsonReader.endArray();
        }  catch (IOException e) {
            return namesFound;
        }

        System.out.println("Loading " + namesFound.size() + " names took " + ((System.nanoTime() - start) / MILLISECOND_IN_NS) + "ms");
        return namesFound;
    }


    public static void main(String[] args) {
        startTime = System.nanoTime();
        final long currentTime = System.currentTimeMillis();
        final long vmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        System.out.println("JVM startup time -> " + (currentTime - vmStartTime) + "ms");
        new Main();

        // If commandline argument 'keeprunning' , start thread to keep JVM running to be able to create checkpoint from other shell
        if (null != args && args.length > 0) {
            if (args[0].equals("keeprunning")) {
                try { while (true) { Thread.sleep(1000); } } catch (InterruptedException e) { }
            }
        }
    }
}