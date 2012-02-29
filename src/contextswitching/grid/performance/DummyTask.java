/**
 * Licensed under the MIT license: 
 * 
 * http://www.opensource.org/licenses/mit-license.php 
 */
package contextswitching.grid.performance;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jppf.server.protocol.JPPFTask;

/**
 * Dummy Task that executes for milisecsDuration 
 * (see constructor)
 * 
 * @author Davide Nunes
 */
public class DummyTask extends JPPFTask{
    
    int duration;//duration of the task
    
    public DummyTask(int milisecsDuration){
        this.duration = milisecsDuration;
    }

    /**
     * This method is called for the task execution
     * you can call the setResult method to instantiate the
     * task results to be passed back to the client
     */
    @Override
    public void run() {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ex) {
            Logger.getLogger(DummyTask.class.getName()).log(Level.SEVERE, null, ex);
            setResult("error");
        }
        
        // the result object can be any object, in this case a string
        setResult("done"); 
    }
    
    
    
}
