package util.test.filesystemmonitor;

//import java.io.IOException;
//import java.nio.file.FileSystem;
//import java.nio.file.FileSystems;
//import java.nio.file.Path;
//import java.nio.file.StandardWatchEventKinds;
//import java.nio.file.WatchEvent;
//import java.nio.file.WatchKey;
//import java.nio.file.WatchService;


public class WatchServiceTest {
    public static void main(String args[]) throws InterruptedException {
//        try {
//            FileSystem fs = FileSystems.getDefault();
//            WatchService ws = null;
//            try {
//                ws = fs.newWatchService();
//            } catch (IOException ex) {
//                Logger.getLogger(WatchServiceTest.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            Path path = fs.getPath(args[0]);
//            path.register(ws, 
//            		      StandardWatchEventKinds.ENTRY_CREATE, 
//            		      StandardWatchEventKinds.ENTRY_MODIFY, 
//            		      StandardWatchEventKinds.OVERFLOW, 
//            		      StandardWatchEventKinds.ENTRY_DELETE);
//                         
//            while (true) {
//	            WatchKey k = ws.take();
//	
//	            List<WatchEvent<?>> events = k.pollEvents();
//	            System.err.println("Got "+events.size()+" events");
//	            for (WatchEvent object : events) {
//	            	
//	                if (object.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
//	                    System.out.println("Modify: " + object.context().toString());
//	                }
//	                else if (object.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
//	                    System.out.println("Delete: " + object.context().toString());
//	                }
//	                else if (object.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
//	                    System.out.println("Created: " + object.context().toString());
//	                }
//	            }
//	            
//	            k.reset();
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(WatchService.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
