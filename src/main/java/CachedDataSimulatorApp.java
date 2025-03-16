import java.util.Random;

public class CachedDataSimulatorApp {

    private static class CachedDataInvalidator implements Runnable {
        private CachedData cacheData;

        public CachedDataInvalidator(CachedData cachedData) {
            this.cacheData = cachedData;
        }

        public void run() {
            while(true) {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName() + " START INVALIDATE CACHE");
                cacheData.invalidateCache();
                System.out.println(Thread.currentThread().getName() + " FINISH INVALIDATE CACHE");
            }
        }
    }

    private static class CachedDataClient implements Runnable {

        private CachedData cacheData;
        private Random rand = new Random();
        public CachedDataClient(CachedData cachedData) {
            this.cacheData = cachedData;
        }

        public void run() {
            while(true) {
                System.out.println(Thread.currentThread().getName() + " REQUEST");
                cacheData.processCachedData();
                System.out.println(Thread.currentThread().getName() + " REQUEST PROCESSED");

                try {
                    Thread.sleep(rand.nextInt(1, 20) * 100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void main(String[] args) {
        CachedData cacheData = new CachedData();

        Thread[] clients = new Thread[10];
        for(int i = 0; i < clients.length; i++ ) {
            clients[i] = new Thread(new CachedDataClient(cacheData));
        }

        for(int i = 0; i < clients.length; i++ ) {
            clients[i].start();
        }

        Thread tCachedDataInvalidator = new Thread(new CachedDataInvalidator(cacheData),
                "CachedDataInvalidator");
        tCachedDataInvalidator.start();

    }
}
