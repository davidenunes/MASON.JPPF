package contextswitching.grid;

import contextswitching.ContextSwitchingModel;
import org.jppf.server.protocol.JPPFTask;

/**
 * Class used to wrap a model for deployment over a JPPF grid
 *
 * it should run a model and set the results object upon task completion
 *
 *
 * @author Davide Nunes
 */
public class SimulationTask extends JPPFTask {

    private static final long serialVersionUID = 1L;
    //the model to be executed in this task
    private ContextSwitchingModel model;

    //Constructor
    public SimulationTask(ContextSwitchingModel model) {
        this.model = model;
    }

    @Override
    public void run() {
        try {
            model.setSeed(System.currentTimeMillis());
            //self contained model can be executed in a thread
            Thread t = new Thread(model);
            long beforeExecution = System.nanoTime();
            t.start();
            t.join();	//wait for the model simulation to be finished
            long afterExecution = System.nanoTime();

            setResult((afterExecution - beforeExecution) / Math.pow(10, 9));
            //setResult(model.getNumEncounters());
        } catch (InterruptedException ex) {
            setException(ex);
        }
    }
}
