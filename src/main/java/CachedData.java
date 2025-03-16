import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class CachedData {
  private Object data;
  private volatile boolean cacheValid;
  private final ReadWriteLock rwl =
    new ReentrantReadWriteLock();
  
  private final Lock r = rwl.readLock();
  
  private final Lock w = rwl.writeLock();
  
  void processCachedData() {
    // Acquire read lock
    r.lock();
    if (!cacheValid) {
      // Release read lock, as read lock upgrade is not allowed
      r.unlock();
      // Acquire write lock
      w.lock();
      try {
        // Recheck status  
        if (!cacheValid) {
          data = getData();
          cacheValid = true;
        }
        // Downgrade to a read lock before releasing the write lock
        // Downgrading is allowed
        r.lock();
      } finally {
        // Release the write lock
        w.unlock(); 
      }
    }
    // Still holding the read lock here
    try {use(data);} 
    finally {r.unlock();}
  }

  public void invalidateCache() {
    // Acquire read lock
    r.lock();
    if (cacheValid) {
      // Release read lock, as read lock upgrade is not allowed
      r.unlock();
      // Acquire write lock
      w.lock();
      try {
        // Recheck status
        if (cacheValid) {
          cacheValid = false;
        }
        // Downgrade to a read lock before releasing the write lock
        // Downgrading is allowed
        r.lock();
      } finally {
        // Release the write lock
        w.unlock();
      }
    }

    r.unlock();
  }

  /*
  Fake implementation long operation
   */
  private Object getData() {
    System.out.println("<<<< Richiesti nuovi dati per cache <<<<");
    long start = System.currentTimeMillis();
    try {
      while (true) {
        Thread.sleep(200);
        System.out.print(".");
        if ((System.currentTimeMillis() - start) >= 5 * 1000)
          break;
      }
    } catch (InterruptedException e) {}
    System.out.println("\n<<<< Caricati nuovi dati nella cache <<<<");
    return new Date();
  }

  private void use(Object data) {
      System.out.println( Thread.currentThread().getName() + " USING DATA: " + data);
  }
}