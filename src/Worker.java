import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Worker implements Runnable {
    // How long do we want to run the juice processing

    /**
     * @param time - length of time we want to sleep the thread
     * @param errMsg - error message thrown when we slepe the thread and it throws the InterruptedException
     * @return void
     */
    private static void delay(long time, String errMsg) {
        long sleepTime = Math.max(1, time);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println(errMsg);
        }
    }

    public final int ORANGES_PER_BOTTLE = 3;
    private final Thread thread;
    private LinkedBlockingQueue<Orange> providedOranges = new LinkedBlockingQueue<Orange>();
    private LinkedBlockingQueue<Orange> processedOranges = new LinkedBlockingQueue<Orange>();
    private volatile boolean timeToWork;
    private Queue<Orange> nextQueue;
    private int workNum;


    // Instantiate worker
    Worker(int threadNum, int workerNum, Queue<Orange> nxtQueue) {
        thread = new Thread(this, "Worker[" + threadNum + "]");
        nextQueue = nxtQueue;
        workNum = workerNum;
    }

    // Start worker
    /**
     * Method that starts the Worker thread
     */
    public void startWorker() {
        timeToWork = true;
        thread.start();
    }

    // Stop worker
    /**
    * Method that stops the worker thread 
    */
    public void stopWorker() {
        timeToWork = false;
    }

    /**
     * Method that waits for the thread to stops after receiving the signal to stop
     */
    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    // Runs the thread itself, monitoring the timeToWork variable and processing Oranges if there are any unprocessed
    @Override
    /**
     * Runs the thread itself, monitoring the timeToWork boolean, and taking Oranges from the queue, processing its next state, and then adding it * * to the next queue.
     */
    public void run() {
        System.out.print(Thread.currentThread().getName() + " Processing oranges");
        while (timeToWork) {
            if(!providedOranges.isEmpty()){
                Orange o = providedOranges.remove();
                o.runProcess();
                processedOranges.add(o);
                System.out.print(".");
            }
            if(!processedOranges.isEmpty()){
                nextQueue.add(processedOranges.remove());
            }
        }
        System.out.println("");
        System.out.println(Thread.currentThread().getName() + " #" + workNum + " Done");
    }

    // Gets an Orange popped from the front of a queue and, once done processing the Orange(s), puts it in the next queue
    /**
     * Gets the oranges from the queue in the Plant class
     */
    public void get(Orange o) {
        providedOranges.add(o);
    }
}
