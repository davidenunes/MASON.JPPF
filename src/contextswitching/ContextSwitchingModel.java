package contextswitching;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import javax.swing.JOptionPane;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * A context switching model implemented in MASON
 *
 * The model stores the networks loaded from the configuration provided, the
 * agent population and the Agent positions in the contexts
 *
 *
 * @author Davide Nunes
 */
public class ContextSwitchingModel extends SimState implements Runnable {
    private static final int STEP_LIMIT = 10000;

    private int numNetworks;                    //number of social contexts
    private int numEncounters;                  //number of encounters during simulation
    private int population;                     //number of agents in the population
    private double consensusRequired;           //consensus required for the simulation to stop       
    private Network[] networks;                 //networks referent to the social contexts
    private Continuous2D[] space;               //A 2D space field required to represent to agents in a 2D space
    double[] contextSwitching;
    private Bag agentPool;                      //maintains the Agent Pool
    private HashMap<Agent, Integer> agentLocation;
    File[] networksToBeLoaded;
    private boolean initialised;

    /**
     * Constructor
     *
     * @param seed random seed used to generate random numbers
     */
    public ContextSwitchingModel(long seed) {
        super(seed);
        initialised = false;
        numEncounters = 0;

    }

    public Network[] getNetworkFields() {
        return networks;
    }

    public Continuous2D[] getSpaceFields() {
        return space;
    }

    /**
     * SETTERS AND GETTERS
     */
    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getNumContexts() {
        return numNetworks;
    }

    public void setNumContexts(int numNetworks) {
        this.numNetworks = numNetworks;
    }

    public double getConsensusRequired() {
        return consensusRequired;
    }

    public void setConsensusRequired(double consensusRequired) {
        this.consensusRequired = consensusRequired;
    }

    public int getNumEncounters() {
        return numEncounters;
    }

    public void incNumEncounters() {
        numEncounters++;
    }

    /**
     * Configure the model
     *
     * @param population Integer - number of agents to be created
     * @param numNetworks Integer - number of network layers to be constructed
     * @param consensusRequired Double - consensus required for a simulation run
     * to stop
     * @param contextSwitching Double[] - vector of probabilities to switch a
     * context for each existing context
     * @param networksToBeLoaded File[] - network files to be loaded to the
     * layers
     *
     * @return model ContextSwitchingModel - the instance of the model to be
     * created
     */
    public ContextSwitchingModel configureModel(ModelConfiguration config) {
        this.population = config.population;
        this.numNetworks = config.numContexts;
        this.consensusRequired = config.consensusRequired;
        this.contextSwitching = config.contextSwitchingProb;
        this.networksToBeLoaded = config.networkFiles;
        return this;
    }

    /**
     * Must be called before start, meaning before the simulation starts one
     * must: configureModel(...) initializeModel();
     *
     * run the simulation
     *
     * Configures the model according to the parameters specified with the
     * configureModel(...) method
     *
     * This should be done before dispatching the model to the grid as some
     * configurations require reading local files
     *
     * @return model ContextSwitchingModel the initialized model ready to run
     */
    public ContextSwitchingModel initializeModel() {
        networks = new Network[numNetworks];
        space = new Continuous2D[numNetworks];



        agentPool = new Bag(population);
        agentLocation = new HashMap<Agent, Integer>(population);

        for (int i = 0; i < population; i++)//populate the agent pool
        {
            agentPool.add(new Agent(i, this));
        }

        initContexts();  //distribute agents by Networks evenly

        initSpace();    //distribute agents by the space field

        configInitialChoiceDist();
        configInitialContextDist();

        initialised = true;

        return this;
    }

    public boolean isInitialised() {
        return initialised;
    }

    /**
     * Configure and populate contexts
     */
    private void initContexts() {
        //init networks
        for (int i = 0; i < numNetworks; i++)//init network and space object
        {
            networks[i] = new Network(false);
        }


        //populate networks
        for (int i = 0; i < numNetworks; i++) {
            for (Object agent : agentPool) {
                networks[i].addNode(agent);
            }
        }

        //load networks
        loadNetworks();
    }

    /**
     * Configure agent location on space to show the networks
     */
    private void initSpace() {
        for (int i = 0; i < numNetworks; i++) {//init network and space object
            space[i] = new Continuous2D(1.0, 200, 200);
            for (Object agent : agentPool)//put the agents in the space 2D plane
            {
                space[i].setObjectLocation(agent,
                        new Double2D(
                        random.nextDouble() * space[i].getWidth() * 0.9,
                        random.nextDouble() * space[i].getHeight() * 0.9));
            }
        }


    }

    private void loadNetworks() {
        for (int i = 0; i < numNetworks; i++) {
            loadNetwork(networksToBeLoaded[i], networks[i]);
        }
    }

    /**
     * Distribute agents by context
     */
    private void configInitialContextDist() {

        //equal number of agents by context
        Bag allAgents = new Bag(agentPool);

        int agentsPerContext = population / numNetworks;

        
        for (int c = 1; c <= numNetworks; c++) {//divide the agents bu numContexts 
            int added = 0;
            int toBeAdded = 0;

            if (c == numNetworks)//for the last context add the remainder of the agents (if population % numNetwork != 0)
            {
                toBeAdded = allAgents.numObjs;
            } else {
                toBeAdded = agentsPerContext;
            }

            while (added < toBeAdded && !allAgents.isEmpty()) {

                int nextID = 0;
                if (allAgents.numObjs > 1) {
                    nextID = random.nextInt(allAgents.numObjs);
                }

                Agent agent = (Agent) allAgents.get(nextID);
                agentLocation.put(agent, c - 1);//put current agent in context i



                allAgents.remove(agent);
                added++;
            }
        
        }
        


    }

    /**
     * Distribute Choices evenly
     */
    private void configInitialChoiceDist() {
        Bag allAgents = new Bag(agentPool);

        int agentsPerOpinion = population / Choices.NUM_OPINIONS;
        for (int opinion : Choices.getAllChoices()) {
            int added = 0;
            while (added < agentsPerOpinion && !allAgents.isEmpty()) {

                int nextID = 0;
                if (allAgents.numObjs > 1) {
                    nextID = random.nextInt(allAgents.numObjs);
                }

                Agent agent = (Agent) allAgents.get(nextID);

                agent.setOpinion(opinion);
                allAgents.remove(agent);
                added++;
            }

        }
    }

    /**
     * Loads a network file into the contexts
     *
     * @param networkFile
     * @param context
     */
    private void loadNetwork(File networkFile, Network context) {
        try {
            Scanner scanner = new Scanner(networkFile);
            boolean done = false;
            while (scanner.hasNextInt()) {
                int id1 = scanner.nextInt();
                int id2 = scanner.nextInt();

                if (id1 > agentPool.numObjs
                        || id2 > agentPool.numObjs) {
                    done = true;
                }
                if (!done) {
                    //create edge in the network
                    Object agent1 = agentPool.get(id1);
                    Object agent2 = agentPool.get(id2);

                    context.addEdge(agent1, agent2, null);

                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Invalid Network File");
            System.out.println(ex.getMessage());
        }

    }

    /**
     * ************************************************************************
     * Model Utilities
     * ***********************************************************************
     */
    public int getContextIndexOf(Agent agent) {
        int index = 0;
        try {
            index = agentLocation.get(agent);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, agentLocation.containsKey(agent));

        }
        return index;
    }

    /**
     * Returns all the neighbours from a given agent the neighbours are from the
     * network representing the social context where the given agent is
     * currently located
     *
     * @param agent
     * @return
     */
    public HashSet<Agent> getNeighbors(Agent agent) {
        HashSet<Agent> neighbors = new HashSet<Agent>();
        int context = getContextIndexOf(agent);

        Bag outEdges = networks[context].getEdges(agent, null);


        for (int i = 0; i < outEdges.numObjs; i++) {
            Edge edge = (Edge) outEdges.objs[i];
            neighbors.add((Agent) edge.getOtherNode(agent));
        }

        return neighbors;
    }

    /**
     * Returns the neighbours of an agent that are active in its current context
     *
     * @param agent the agent we want neighbours from
     * @return
     */
    public Bag getActiveNeighbors(Agent agent) {
        HashSet<Agent> neighbors = getNeighbors(agent);
        Bag actives = new Bag();

        int context = getContextIndexOf(agent);

        for (Agent neighbor : neighbors) {
            int neighborContext = getContextIndexOf(neighbor);
            if (context == neighborContext) {
                actives.add(neighbor);
            }
        }
        return actives;
    }

    public int[] getOpinionCout() {
        int[] counts = new int[Choices.NUM_OPINIONS];

        for (int i = 0; i < agentPool.numObjs; i++) {
            Agent agent = (Agent) agentPool.objs[i];
            counts[agent.getOpinion()]++;
        }

        return counts;
    }

    public boolean consensusReached() {
        int[] count = getOpinionCout();
        for (int c : count) {
            if (c / (population * 1.0) >= consensusRequired) {
                return true;
            }
        }
        return false;
    }

    public double getSwitchingProb(Agent agent) {
        int index = getContextIndexOf(agent);
        return contextSwitching[index];
    }

    public void switchContextOf(Agent agent) {
        int current = getContextIndexOf(agent);
        int next = current;
        while (next == current) {
            next = random.nextInt(numNetworks);
        }
        agentLocation.put(agent, next);
    }

    /**
     * ************************************************************************
     * Model Start and Run
     * ***********************************************************************
     * if a GUI is attached this is called when the start button is pressed
     */
    @Override
    public void start() {
        super.start();
        numEncounters = 0;

        //add agents to the schedule
        for (Object agent : agentPool) {
            schedule.scheduleRepeating((Agent) agent);
        }
    }

    @Override
    public void run() {
        start();

        do {
            if (!schedule.step(this)) {
                break;//exhausted nothing to do in schedule
            }
            System.out.println("Simulation cicle: " + schedule.getSteps());

        } while (!consensusReached() && schedule.getSteps() < STEP_LIMIT);

        finish();
        System.out.println("Simulation ended at step: " + schedule.getSteps());

    }

    /**
     * Executed when the model finish
     */
    @Override
    public void finish() {
        super.finish();
        super.schedule.clear();
    }
}
