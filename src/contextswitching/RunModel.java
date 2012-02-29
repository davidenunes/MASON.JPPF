package contextswitching;

import java.io.File;

/**
 * Simple Class to test the run of a model instance
 *
 * @author Davide Nunes
 */
public class RunModel {

    /**
     * No parameters are needed here
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, Exception {
        ContextSwitchingModel model = new ContextSwitchingModel(System.currentTimeMillis());

        File[] networks = new File[]{
            new File("NetworkFiles/barabasi_500.np"),
            new File("NetworkFiles/regular_500.np")
        };


        /**
         * population - in this case has to be 300 or less due to the network
         * files being used.
         *
         * number of contexts - determine the number of contexts we are
         * considering for this configuration must be equal to the number of
         * network files we are using.
         *
         * consensus required - the consensus required for this model to stop
         * its execution.
         *
         * context switching - probabilities of switching contexts for each
         * context considered.
         *
         */
        ModelConfiguration config = new ModelConfiguration(

                500, //population 
                2, //num contexts
                0.8, //Consensus required to finish the model execution
                new double[]{0.75, 0.25},//context switching 
                networks);

        //configure the model with the configuration above
        model.configureModel(config);
        
        
        //initializes the model with de set configuration
        model.initializeModel();


        //the model is runnable add it to a thread and run the thread
        Thread t = new Thread(model);
        t.start();
        t.join();

        //get the results
        int[] count = model.getOpinionCout();
        System.out.println("num encounters: " + model.getNumEncounters());
        System.out.println("Opinion Count: ");
        System.out.println("(" + count[0] + "," + count[1] + ")");
        System.out.println("Consensus reached? " + model.consensusReached());
    }
}
