/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contextswitching.grid;

import contextswitching.ContextSwitchingModel;
import contextswitching.ModelConfiguration;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.client.JPPFResultCollector;
import org.jppf.client.event.TaskResultEvent;
import org.jppf.client.event.TaskResultListener;
import org.jppf.server.protocol.JPPFTask;

/**
 *
 * Executes the exploration of a parameter space in an available JPPF grid
 * system This Grid runner submits multiple jobs at the time to the grid and
 * collects the results asynchronously
 *
 * @author Davide Nunes
 */
public class ContextSwitchingParallelGridRunner {

    private static JPPFClient jppfClient = null;
    private static final int NUM_RUNS = 30; //number of runs
    private static final int JOBS_IN_PARALLEL = 2;
    private static Iterator<ModelConfiguration> parameterSpaceIT;

    /**
     * Constructor
     */
    public ContextSwitchingParallelGridRunner() {
    }

    /**
     * Initializes the JPPF Client
     */
    public void initJPPFClient() {
        // create the JPPFClient. This constructor call causes JPPF to read the configuration file
        jppfClient = new JPPFClient();
    }

    public static void main(String[] args) {
        try {


            //create a runner instance
            ContextSwitchingParallelGridRunner runner = new ContextSwitchingParallelGridRunner();
            runner.initJPPFClient();

            /**
             * *************************************************
             * PARAMETER SPACE DEFINITION fixed networks, population and
             * consensus required, span the switching probabilities
             * *************************************************
             */
            File[] networks = new File[]{
                new File("NetworkFiles/barabasi_500.np"),
                new File("NetworkFiles/regular_500.np"),
                new File("NetworkFiles/ws_500.np")
            };
            int numAgents = 500;
            //networks are loaded according to number of agents
            int numContexts = 3;
            double consensusRequired = 0.8;

            //construct the parameter space
            LinkedList<ModelConfiguration> ps = new LinkedList<ModelConfiguration>();
            for (double c1 = 0.0; c1 <= 1.0; c1 += 0.05) {//span of the switching probabilities
                for (double c2 = 0.0; c2 <= 1.0; c2 += 0.05) {
                    for (double c3 = 0.0; c3 <= 1.0; c3 += 0.05) {
                        ps.addLast(new ModelConfiguration(numAgents, numContexts, consensusRequired, new double[]{c1, c2, c3}, networks));
                    }
                }
            }

            System.out.println("PARAMETER SPACE DEFINED");

            parameterSpaceIT = ps.iterator();
            /**
             * For each configuration 1. create a context switching model 2.
             * configure the model with the current configuration 3. Create a
             * grid job with NUM_RUNS tasks with the same model instance 4.
             * Submit the Job 5. Collect the results and repeat from 1.
             */
            
            //this is a result collector helpful to recover the task results asynchronously
            JPPFResultCollector collector = new JPPFResultCollector(NUM_RUNS);
            
            //submit JOBS_IN_PARALLEL at first and set this class as the listener
            for (int i = 0; i < JOBS_IN_PARALLEL; i++) {
                if (parameterSpaceIT.hasNext()) {
                    runner.executeNonBlockingJob(createJob(parameterSpaceIT.next(), NUM_RUNS), collector);

                }
            }
            
            //wait for jobs to finish collect the results and submit another job
            while(parameterSpaceIT.hasNext()){
                List<JPPFTask> results = collector.waitForResults();
                //do something with the results
                runner.executeNonBlockingJob(createJob(parameterSpaceIT.next(), NUM_RUNS), collector);
            }
            //done



        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        } finally {
            //close the client
            if (jppfClient != null) {
                jppfClient.close();
            }
        }
    }

    /**
     * Creates a model consisting of numRuns tasks with the same model, this is
     * runs numRuns times the same configuration
     *
     * @param model ContextSwitchingModel - the model to be runned
     * @param numRuns number of repetitions for this configuration
     *
     * @return job JPPFJob - a newly created job
     */
    private static JPPFJob createJob(ModelConfiguration config, int numRuns)
            throws JPPFException {

        ContextSwitchingModel model = new ContextSwitchingModel(System.currentTimeMillis());//create a new model and provide a random seed
        model.configureModel(config);

        //initialize the model with the current configuration
        model.initializeModel();
        // create a JPPF job
        JPPFJob job = new JPPFJob();

        // add a task to the job.
        for (int i = 0; i < numRuns; i++) {
            job.addTask(new SimulationTask(model));
        }
        return job;
    }

    /**
     * Submits a job to the grid, The results will be received by a Task Result
     * Listener This is used to submit multiple jobs in parallel
     *
     * @param job JPPFJob the job to be submitted
     *
     * @throws Exception
     */
    private void executeNonBlockingJob(JPPFJob job, TaskResultListener listener) throws Exception {
        // set the job in blocking mode.
        job.setBlocking(false);

        // Submit the job and wait until the results are returned.
        // The results are returned as a list of JPPFTask instances,
        // in the same order as the one in which the tasks where initially added the job.
        System.out.println("Job submited");
        job.setResultListener(listener);
        jppfClient.submit(job); //simulation results
    }
}
