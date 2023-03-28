package eu.hansolo.crac8;

import jdk.crac.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class Main implements Resource {
    private static final long               RUNTIME_IN_NS = 10_000_000_000l;
    private static final int                RANDOM_RANGE  = 25_000;
    private static final int                RANGE         = 100_000_000;
    private static final long               SECOND_IN_NS  = 1_000_000_000;
    private final        Callable<Integer>  randomTask;
    private final        ArrayList<Integer> randomNumberPool;
    private final        Callable<Integer>  task;
    private final        ArrayList<Integer> numberPool;
    private              ExecutorService    executorService;
    private static       long               startTime;


    public Main() {
        Core.getGlobalContext().register(Main.this);

        randomNumberPool = createRandomNumberPool();
        numberPool       = createNumberPool();
        executorService  = Executors.newSingleThreadExecutor();
        randomTask       = () -> {
            final List<String> results = new ArrayList<>();
            while(System.nanoTime() - startTime < RUNTIME_IN_NS) {
                final int     number  = randomNumberPool.get(ThreadLocalRandom.current().nextInt(randomNumberPool.size() - 1));
                final boolean isPrime = isPrimeLoop(number);
                results.add(number + " -> " + isPrime);
            }
            return results.size();
        };
        task             = () -> {
            final ArrayList<String> results = new ArrayList<>(RANGE);
            int counter = 0;
            while(System.nanoTime() - startTime < RUNTIME_IN_NS) {
                final int     number  = numberPool.get(counter);
                final boolean isPrime = isPrimeLoop(number);
                results.add(number + " -> " + isPrime);
                counter++;
            }
            return results.size();
        };

        System.out.println("Start without CRaC");
        start();

        startTime = System.nanoTime();
        startAsync();

        System.out.println("Total number of loaded classes     -> " + ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        System.out.println("Total time of compilation          -> " + ManagementFactory.getCompilationMXBean().getTotalCompilationTime() + "ms");
    }


    @Override public void beforeCheckpoint(Context<? extends Resource> context) throws Exception { }

    @Override public void afterRestore(Context<? extends Resource> context) throws Exception {
        System.out.println("Start using CRaC");

        startTime = System.nanoTime();

        executorService = Executors.newSingleThreadExecutor();

        start();

        startTime = System.nanoTime();
        startAsync();

        System.out.println("Total number of loaded classes     -> " + ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        System.out.println("Total time of compilation          -> " + ManagementFactory.getCompilationMXBean().getTotalCompilationTime() + "ms");
    }
    

    private void start() {
        try {
            //final long numberOfTransactions = this.executorService.submit(randomTask).get();
            final long numberOfTransactions = this.executorService.submit(task).get();
            System.out.println("Number of sync transactions in " + (RUNTIME_IN_NS / SECOND_IN_NS) + "s  -> " + String.format(Locale.US, "%,d", numberOfTransactions));

            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.SECONDS);
            if (!executorService.isShutdown()) { executorService.shutdownNow(); }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Interrupted");
        }
    }

    private void startAsync() {
        final ArrayList<String> results = new ArrayList<>(RANGE);
        int counter = 0;
        while(System.nanoTime() - startTime < RUNTIME_IN_NS) {
            //final int number  = randomNumberPool.get(ThreadLocalRandom.current().nextInt(randomNumberPool.size() - 1));
            final int number  = numberPool.get(counter);
            CompletableFuture<Boolean> cpf = CompletableFuture.supplyAsync(() -> {
                if (number < 1) { return false; }
                boolean isPrime = Boolean.TRUE;
                for (long n = number; n > 0; n--) {
                    if (n == number || n == 1 || number % n != 0) { continue; }
                    isPrime = Boolean.FALSE;
                    break;
                }
                return isPrime;
            });
            cpf.thenAccept(result -> {
                if (System.nanoTime() - startTime < RUNTIME_IN_NS) { results.add(number + " -> " + result); }
            });
            counter++;
        }
        final long numberOfTransactions = results.size();
        System.out.println("Number of async transactions in " + (RUNTIME_IN_NS / SECOND_IN_NS) + "s -> " + String.format(Locale.US, "%,d", numberOfTransactions));
    }

    private boolean isPrimeLoop(final long number) {
        if (number < 1) { return false; }
        boolean isPrime = true;
        for (long n = number ; n > 0 ; n--) {
            if (n == number || n == 1 || number % n != 0) { continue; }
            isPrime = false;
            break;
        }
        return isPrime;
    }

    private ArrayList<Integer> createRandomNumberPool() {
        final Random rnd = new Random();
        final ArrayList<Integer> randomNumberPool = new ArrayList<>(5_000_000);
        for (int i = 0 ; i < 5_000_000 ; i++) {
            final int number = rnd.nextInt(RANDOM_RANGE);
            randomNumberPool.add(number);
        }
        return randomNumberPool;
    }

    private ArrayList<Integer> createNumberPool() {
        final ArrayList<Integer> numberPool = new ArrayList<>(RANGE);
        for (int i = 0; i < RANGE; i++) {
            numberPool.add(i);
        }
        return numberPool;
    }


    public static void main(String[] args) {
        startTime = System.nanoTime();
        final long currentTime = System.currentTimeMillis();
        final long vmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        System.out.println("JVM startup time -> " + (currentTime - vmStartTime) + "ms");
        new Main();

        // Keep JVM running to be able to create checkpoint from other shell
        if (null != args && args.length > 0) {
            if (args[0].equals("keeprunning")) {
                try { while (true) { Thread.sleep(1000); } } catch (InterruptedException e) { }
            }
        }
    }
}