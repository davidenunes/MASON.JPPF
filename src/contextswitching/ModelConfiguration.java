/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contextswitching;

import java.io.File;

/**
 * Utility Class to wrap all the parameters used in the context switching model
 *
 * @author Davide Nunes
 */
public class ModelConfiguration {

    public int population;
    public int numContexts;
    public double consensusRequired;
    public double[] contextSwitchingProb;
    public File[] networkFiles;

    /**
     * Constructor
     *
     * Throws an exception if the configuration is not valid
     *
     * @param population number of agents populating the model
     * @param numContexts number of social contexts population the model
     * @param consensusRequired consensus ratio required to end the simulation
     * @param contextSwitchingProb the context switching probability for each
     * context
     * @param networkFiles a vector of file descriptors containing the network
     * files
     */
    public ModelConfiguration(int population,
            int numContexts,
            double consensusRequired,
            double[] contextSwitchingProb,
            File[] networkFiles) throws Exception {

        this.population = population;
        this.numContexts = numContexts;
        this.consensusRequired = consensusRequired;
        this.contextSwitchingProb = contextSwitchingProb;
        this.networkFiles = networkFiles;

        validateConfiguration();

    }

    /**
     * Validates the configuration parameters
     * 
     * throws an exception if the configuration parameters are not valid
     * does not validate the network files be careful with that 
     */
    private void validateConfiguration() throws Exception {
        if (population <= 0) {
            throw new Exception("Invalid population: must be a positive value");
        }
        if (numContexts <= 0) {
            throw new Exception("Invalid number of contexts: must be a positive value");
        }
        if (consensusRequired < 0.0) {
            throw new Exception("Invalid consensus required: must be a positive value");
        }
        if (contextSwitchingProb == null || networkFiles == null) {
            throw new Exception("Incomplete Configurations: please supply a valid network file array and context switching probability");
        }
        if (contextSwitchingProb.length != networkFiles.length) {
            throw new Exception("Invalid network files and switching probabilities: vectors must have the same size");
        }

    }
}
