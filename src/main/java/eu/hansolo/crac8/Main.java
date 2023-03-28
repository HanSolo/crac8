package eu.hansolo.crac8;

import jdk.crac.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class Main implements Resource {
    private static final Random                         RND              = new Random();
    private static final long                           RUNTIME_IN_NS    = 10_000_000_000l;
    private static final int                            RANGE            = 500_000;
    private static final long                           SECOND_IN_NS     = 1_000_000_000;
    private static final List<String>                   RESULTS_SYNC     = new ArrayList<>();
    private static final List<String>                   RESULTS_ASYNC    = new ArrayList<>();
    private        final List<Integer>                  randomNumberPool = new ArrayList<>();
    private              ExecutorService                executorService  = Executors.newSingleThreadExecutor();
    private        final Callable<Integer>              task;
    private static       long                           startTime;


    public Main() {
        // Define task
        task = () -> {
            while(System.nanoTime() - startTime < RUNTIME_IN_NS) {
                final int     number  = randomNumberPool.get(ThreadLocalRandom.current().nextInt(randomNumberPool.size() - 1));
                final boolean isPrime = isPrimeLoop(number);
                RESULTS_SYNC.add(number + " -> " + isPrime);
            }
            return RESULTS_SYNC.size();
        };

        Core.getGlobalContext().register(Main.this);

        init();

        start();

        startTime = System.nanoTime();
        startAsync();

        System.out.println("Total number of loaded classes -> " + ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        System.out.println("Total time of compilation -> " + ManagementFactory.getCompilationMXBean().getTotalCompilationTime() + "ms");
    }


    @Override public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
        if (!executorService.isShutdown()) { executorService.shutdownNow(); }
    }

    @Override public void afterRestore(Context<? extends Resource> context) throws Exception {
        startTime = System.nanoTime();
        
        final int noOfThreads = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newSingleThreadExecutor();

        RESULTS_SYNC.clear();
        RESULTS_ASYNC.clear();

        start();

        startTime = System.nanoTime();
        startAsync();

        System.out.println("Total number of loaded classes -> " + ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        System.out.println("Total time of compilation -> " + ManagementFactory.getCompilationMXBean().getTotalCompilationTime() + "ms");
    }
    

    private void init() {
        randomNumberPool.clear();
        randomNumberPool.addAll(createRandomNumberPool());
    }

    private void start() {
        try {
            final long numberOfTransactions = this.executorService.submit(task).get();
            System.out.println("Number of sync transcations in " + (RUNTIME_IN_NS / SECOND_IN_NS) + "s -> " + numberOfTransactions);

            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.SECONDS);
            if (!executorService.isShutdown()) { executorService.shutdownNow(); }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Interrupted");
        }
    }

    private void startAsync() {
        while(System.nanoTime() - startTime < RUNTIME_IN_NS) {
            final int number  = randomNumberPool.get(ThreadLocalRandom.current().nextInt(randomNumberPool.size() - 1));
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
                if (System.nanoTime() - startTime < RUNTIME_IN_NS) { RESULTS_ASYNC.add(number + " -> " + result); }
            });
        }
        final long numberOfTransactions = RESULTS_ASYNC.size();
        System.out.println("Number of async transcations in " + (RUNTIME_IN_NS / SECOND_IN_NS) + "s -> " + numberOfTransactions);
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

    private List<Integer> createRandomNumberPool() {
        final List<Integer> randomNumberPool = new ArrayList<>(1_000_000);
        for (int i = 0 ; i < 5_000_000 ; i++) {
            final int number = RND.nextInt(RANGE);
            randomNumberPool.add(number);
        }
        return randomNumberPool;
    }


    public static void main(String[] args) {
        startTime = System.nanoTime();
        final long currentTime = System.currentTimeMillis();
        final long vmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        System.out.println("JVM startup time -> " + (currentTime - vmStartTime) + "ms");
        new Main();

        // Keep JVM running to be able to create checkpoint from other shell
        try { while (true) { Thread.sleep(1000); } } catch (InterruptedException e) { }
    }
}