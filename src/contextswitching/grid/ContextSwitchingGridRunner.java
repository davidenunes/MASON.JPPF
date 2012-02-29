/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contextswitching.grid;

import contextswitching.ContextSwitchingModel;
import contextswitching.ModelConfiguration;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFTask;

/**
 *
 * Executes the exploration of a parameter space in an available JPPF grid
 * system This Grid runner sends one job at the time sequentially
 *
 * for a more advanced runner that submits N jobs in parallel see
 * <code>ContextSwitchingParallelGridRunner</code>
 *
 * @author Davide Nunes
 */
public class ContextSwitchingGridRunner {

    private static JPPFClient jppfClient = null;
    private static final int NUM_RUNS = 30; //number of runs


    /**
     * Constructor
     */
    public ContextSwitchingGridRunner() {
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
            ContextSwitchingGridRunner runner = new ContextSwitchingGridRunner();
            runner.initJPPFClient();

            /**
             * *************************************************
             * PARAMETER SPACE DEFINITION
             * fixed networks, population and consensus required, span the switching probabilities
             **************************************************
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
                    for(double c3 = 0.0; c3 <=1.0; c3 += 0.05)
                        ps.addLast(new ModelConfiguration(numAgents, numContexts, consensusRequired, new double[]{c1, c2, c3}, networks));
                }
            }

            System.out.println("PARAMETER SPACE DEFINED");
            
            
            /**
             * For each configuration
             * 1. create a context switching model
             * 2. configure the model with the current configuration
             * 3. Create a grid job with NUM_RUNS tasks with the same model instance
             * 4. Submit the Job
             * 5. Collect the results and repeat from 1.
             */
            for(ModelConfiguration config : ps){
                ContextSwitchingModel model = new ContextSwitchingModel(System.currentTimeMillis());//create a new model and provide a random seed
                model.configureModel(config);
                
                //initialize the model with the current configuration
                model.initializeModel();
                
                JPPFJob job = createJob(model, NUM_RUNS);
                //execute blocking job wait for the job to terminate and do something with the results
                List<JPPFTask> results = executeBlockingJob(job);
                
                //as an example lets just print the results of the jobs tasks
                System.out.println(results.toString());
    
            }
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
     * Creates a model consisting of numRuns tasks with the same 
     * model, this is runs numRuns times the same configuration
     * 
     * @param model ContextSwitchingModel - the model to be runned
     * @param numRuns number of repetitions for this configuration
     * 
     * @return job JPPFJob - a newly created job 
     */
    private static JPPFJob createJob(ContextSwitchingModel model, int numRuns)
            throws JPPFException {
        // create a JPPF job
        JPPFJob job = new JPPFJob();

        // add a task to the job.
        for (int i = 0; i < numRuns; i++) {
            job.addTask(new SimulationTask(model));
        }
        return job;
    }

    /**
     * Submits a job to the grid, 
     * waits for the results and process the results
     * 
     * @param job JPPFJob the job to be submitted
     * 
     * @throws Exception 
     */
    private static List<JPPFTask> executeBlockingJob(JPPFJob job) throws Exception {
        // set the job in blocking mode.
        job.setBlocking(true);

        // Submit the job and wait until the results are returned.
        // The results are returned as a list of JPPFTask instances,
        // in the same order as the one in which the tasks where initially added the job.
        System.out.println("Job submited wating for results...");

        List<JPPFTask> results = jppfClient.submit(job); //simulation results
        return results;
    }
}
