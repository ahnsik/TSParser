package test.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import com.aircode.util.LogManager;
public class AppTest {
    private Logger logger = LogManager.getInstance().getLogger(AppTest.class);
    @Test
    public void QueueTest() {               
        ConcurrentLinkedQueue<Integer> testQueue = new ConcurrentLinkedQueue<Integer>();
        class Tester {
            private int  count = 0;
            public int getCount() {
                return count++;
            }
            public void rise() {
                count++;
            }
        }
        final Tester  tester = new Tester();
        Thread t1 = new Thread(new Runnable() {            
            public void run() {
                while(true) {
                    int i = tester.getCount();
                    testQueue.offer(i);                        
                    try {
                        Thread.sleep((long)(Math.random() * 1000));
                    } catch (InterruptedException e) {}
                }
                
            }
        });
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    Integer result = testQueue.poll();
                    logger.info("poll1 : " + result);
                    try {
                        Thread.sleep((long)(Math.random() * 1000));
                    } catch (InterruptedException e) {}
                }
            }
        });
        Thread t4 = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    Integer result = testQueue.poll();
                    logger.info("poll2 : " + result);
                    try {
                        Thread.sleep((long)(Math.random() * 1000));
                    } catch (InterruptedException e) {}
                }
            }
        });
        Thread t3 = new Thread(new Runnable() {            
            public void run() {
                while(true) {
                    int i = tester.getCount();
                    testQueue.offer(i);                    
                    try {
                        Thread.sleep((long)(Math.random() * 1000));
                    } catch (InterruptedException e) {}
                }
                
            }
        });
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        while(true) {}
    }
}
