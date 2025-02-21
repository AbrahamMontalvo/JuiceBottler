
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Plant implements Runnable {
    // How long do we want to run the juice processing (5 seconds)
    public static final long PROCESSING_TIME = 5 * 1000;

    // Number of plants running concurrently (our data parallelization)
    private static final int NUM_PLANTS = 4;

    // Each plant gets three workers: a peeler, a juicer, and a bottler
    private Worker peeler;
    private Worker juicer;
    private Worker bottler;

    // Each plant gets a queue for each of the states of the Orange class (except bottled, because once an Orange is juiced, we're done with it)
    private BlockingQueue<Orange> fetched = new LinkedBlockingQueue<Orange>();
    private BlockingQueue<Orange> peeled = new LinkedBlockingQueue<Orange>();
    private BlockingQueue<Orange> juiced = new LinkedBlockingQueue<Orange>();
    private BlockingQueue<Orange> bottled = new LinkedBlockingQueue<Orange>();

    public static void main(String[] args) {
        // Startup the plants
        Plant[] plants = new Plant[NUM_PLANTS];
        for (int i = 0; i < NUM_PLANTS; i++) {
            plants[i] = new Plant(i+1);
            plants[i].startPlant();
        }

        // Give the plants time to do work
        delay(PROCESSING_TIME, "Plant malfunction");

        // Stop the plant, and wait for it to shutdown
        for (Plant p : plants) {
            p.stopPlant();
        }
        for (Plant p : plants) {
            p.waitToStop();
        }

        // Summarize the results
        int totalProvided = 0;
        int totalProcessed = 0;
        int totalBottles = 0;
        int totalWasted = 0;
        for (int i = 0; i < NUM_PLANTS; i++) {
            // Gather the results from each of the plants
            totalProvided += plants[i].getProvidedOranges();
            totalProcessed += plants[i].getProcessedOranges();
            totalBottles += plants[i].getBottles();
            totalWasted += plants[i].getWaste();
        }

        System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
        System.out.println("Created " + totalBottles +
                           " bottles, wasted " + totalWasted + " oranges");
    }

    private static void delay(long time, String errMsg) {
        long sleepTime = Math.max(1, time);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println(errMsg);
        }
    }

    // How many oranges must be juiced to fill 1 bottle
    public final int ORANGES_PER_BOTTLE = 3;

    private final Thread thread;
    private int orangesProvided;
    private int orangesProcessed;
    private volatile boolean timeToWork;

    // Constructor that initializes the threads and worker instances
    Plant(int threadNum) {
        orangesProvided = 0;
        orangesProcessed = 0;
        thread = new Thread(this, "Plant[" + threadNum + "]");

        peeler = new Worker(threadNum, 1, peeled);
        juicer = new Worker(threadNum, 2, juiced);
        bottler = new Worker(threadNum, 3, bottled);

    }

    // Start all threads
    public void startPlant() {
        timeToWork = true;
        thread.start();
        peeler.startWorker();
        juicer.startWorker();
        bottler.startWorker();
    }

    // Stop all threads
    public void stopPlant() {
        timeToWork = false;
        peeler.stopWorker();
        juicer.stopWorker();
        bottler.stopWorker();
    }

    public void waitToStop() {
        try {
            thread.join();
            peeler.waitToStop();
            juicer.waitToStop();
            bottler.waitToStop();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    @Override
    public void run() {
        System.out.print(Thread.currentThread().getName() + " Processing oranges");
        // Check if the queues have Oranges for each worker to process
        while (timeToWork) {
            fetched.add(new Orange());
            if(!fetched.isEmpty()){
                peeler.get(fetched.remove());
            }
            if(!peeled.isEmpty()){
                juicer.get(peeled.remove());
            }
            if(!juiced.isEmpty()){
                bottler.get(juiced.remove());
            }
            orangesProvided++;
            System.out.print(".");
        }
        System.out.println("");
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    // Gets number of oranges generated
    public int getProvidedOranges() {
        return orangesProvided;
    }

    // Gets number of oranges processed
    public int getProcessedOranges() {
        orangesProcessed = bottled.size();
        return orangesProcessed;
    }

    // Gets number of full bottles of juice
    public int getBottles() {
        return orangesProcessed / ORANGES_PER_BOTTLE;
    }

    // Get number of oranges wasted
    public int getWaste() {
        return orangesProcessed % ORANGES_PER_BOTTLE;
    }
}
