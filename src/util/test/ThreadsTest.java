package util.test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import util.Timer;
import util.dbg.Logger;


/**
 * Tässä oli tarkoitus testata tehostaako usean threadin käyttö laskentaa
 * kun koneessa on enemmän kuin yksi core. 
 * Vastaus on kyllä ja ei. Kun käytettiin jaettua resurssia (Math.random),
 * usean threadin käyttö oli selkeästi tehottomampaa ilmeisesti synkronoinnin 
 * takia. Kun tehtiin päivityksiä HashMap:iin (kullakin threadilla oma) jossa 
 * oli kerrallaan 100 alkiota, threadien lisääminen niin ikään hidasti toimintaa
 * selkeästi. Kun vaan incrementoitiin jotain counteria, oli usean threadin käyttö
 * hyvinkin tehokasta. Samoin kun vaihdettiinkin käsiteltäväksi tietorakenteeksi
 * bitset (koko n 350 000 bittiä, eli n. 50 kb), jota sitten päiviteltiin suht 
 * ennustettavalla tavalla, toimi threadien jako prosessoreille vähintään
 * tyydyttävästi (1380, 725, 517, 440) millis 1,2,3 ja 4 prosessorilla,
 * respekviisisesti (sic).
 * 
 * Summa summarum: jos tosiaan käytetään pelkkää prossua ja muistin käsittelyä
 * on rajoitetusti, on threaditus hyvä kun on useita prossuja (tai vaikka 2).
 * Jos sen sijaan vähänkään enemmän muistia käpistellään (muista jaetuista 
 * resursseista puhumattakaan), voi hyöty olla hyvinkin kyseenalainen, tai jopa
 * helposti negatiivinenkin.
 *  
 * @author leronen
 *
 * keywords: multi-core, multi-processor, thread, cpu
 */
public class ThreadsTest {
            
    public static void main(String[] args) throws Exception {
        int totalLoad = Integer.parseInt(args[0]);
        int numWorkers = Integer.parseInt(args[1]);
        new ThreadsTest().run(totalLoad, numWorkers);        
    }
    
    private void run(int totalLoad, int numWorkers) throws Exception {
        Timer.startTiming("Total");

        List<Thread> workers = new ArrayList<Thread>();        
        int loadPerWorker = totalLoad / numWorkers;
        
        for (int i=1; i<= numWorkers; i++) {
            Thread t = new Thread(new Worker("t"+i, loadPerWorker));
            workers.add(t);
            Logger.info("Starting t"+i);
            t.start();            
        }
        
        for (Thread t: workers) {
            Logger.info("Joining "+t);
            t.join();
            Logger.info("Joined "+t);
        }                       
        
        Timer.endTiming("Total");
        Timer.logToStdErr();
    }
    
    
    private class Worker implements Runnable {
        
        String name;
        int load;
        BitSet set = new BitSet();
        
        Worker(String pName,
               int pLoad) {
            name = pName;
            load = pLoad;
        }
        
        public String toString() {
            return name;
        }
        
        public void run() {                                              
            Logger.info("Worker \""+name+"\" starting...");
            Timer.startTiming("Worker: "+name);
                        
            for (int i=0; i<load; i++) {
                set.set((i+100)%345212, true);
                set.set(i%345212, false);
            }
                                   
            Logger.info("Worker \"+name+\" done. (in the end, set contains "+set.cardinality()+" elements).");
            Timer.endTiming("Worker: "+name);    
        }          
    }
}
