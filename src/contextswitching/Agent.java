package contextswitching;

import java.util.HashMap;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;

/**
 * This class represents an agent to be scheduled for execution
 * this must be an instance of <code>Steppable</code> in order to be added to the
 * Mason Simulator Schedule, the scheduler basically executes the step method of 
 * each <code>Steppable</code> entity that is added
 * 
 * <p>
 * This represents an agent from the Context Switching model.
 * each agent owns a choice that he has made.
 * the agent also records a memory of the encounters with agents with each 
 * existent choice.
 * 
 * <p>
 * Each time the agent is scheduled for execution it performs an encounter
 * with other agent in the following manner:
 * 
 * <ul>
 * <li> choose an available neighbor from the current social context
 * <li> check is choice and record it in memory
 * <li> if there a chance in the majority of choices observed in the agents 
 * memory, switch the choice to the majority
 * </ul>
 * 
 * @author Davide Nunes
 */
public class Agent implements Steppable {
    //Attributes

    private int id;
    private ContextSwitchingModel model;
    private int choice; //opinion value (from the possible Choices.getAllChoices();)
    private HashMap<Integer, Integer> memory;

    /**
     * Constructor
     * creates an agent with the given id value and no memory 
     * of choices observed
     * 
     * @param id the representative id for the agent
     */
    public Agent(int id, ContextSwitchingModel model) {
        memory = new HashMap<Integer, Integer>();
        choice = 0;
        int choices [] = Choices.getAllChoices();
        for(int op : choices)  //no choices in memory at the beginning
            memory.put(op, 0);
        
        this.id = id;
        this.model = model; //saves a reference of the model for future access
    }

    /**
     * Returns the current choice 0 or 1
     * @see Choices 
     * @return opinion Integer
     */
    public int getOpinion() {
        return choice;
    }

    /**
     * Sets the current choice to a given value
     * 
     * @return opinion Integer a value for the opinion
     */
    public void setOpinion(int choice) {
        this.choice = choice;
    }

    /**
     * Agent behaviour to be executed in each step
     * @param state SimState - current model state
     */
    @Override
    public void step(SimState state){
        //gets an active partner from the current context
        Agent partner = getActiveNeighbor(); 
        //System.out.println("PARTNER: "+partner);
        if (partner != null) {              //some partner exists
            performEncounter(partner);      //update opinions
            this.model.incNumEncounters();  //update the number of encounters
        }      
        contextSwitching(); 
    }

    /**
     * A Human readable representation of the Context Switching Agent
     * in the following form
     * 
     * (Agent id:value choice: value context: value)
     * 
     * @return agentString
     */
    @Override
    public String toString() {      
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("Agent ");
        sb.append("id:");
        sb.append(id);
        sb.append(" choice:");
        sb.append(choice);
        sb.append(" context:").append(model.getContextIndexOf(this));
        sb.append(")");

        return sb.toString();
    }

    /**
     * The agent's hashCode is constructed 
     * with its ID, it is important that all the agents have
     * a unique set of IDS
     * 
     * @return 
     */
    @Override
    public int hashCode() {
        return this.id;
    }

    /**
     * Agents are equal if they have the same ID
     * 
     * @param obj other agent
     * 
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Agent other = (Agent) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    
    /*
     * Get Active Neighbour
     * 
     * Selects an active neighbour from the current context 
     * if this agent exists, returns null otherwise
     * 
     * @return partner Agent / null
     */
    private Agent getActiveNeighbor() {
        Bag activeNeighbours = model.getActiveNeighbors(this);
        Agent result = null;
        if (!activeNeighbours.isEmpty()) {
            int partner = model.random.nextInt(activeNeighbours.numObjs);
            result = (Agent) activeNeighbours.objs[partner];
        }
        return result;
    }

    /**
     * Perform Encounter
     * 
     * performs an encounter with another agent 
     * updating the opinion values by a majority rule
     * 
     * 1. get the partner's opinion
     * 2. update the opinions seen in memory
     * 3. if the agent saw that opinion more than its current opinion 
     *      ->  switch opinion to this value
     * 
     * @param partner Agent - the agent with wich the current agent will perform
     * the encounter.
     */
    private void performEncounter(Agent partner) {
        int otherOpinion = partner.getOpinion();
        int otherInMemory = memory.get(otherOpinion);
        otherInMemory++;
        memory.put(otherOpinion, otherInMemory);

        //only consider switching if opinion is different
        if (otherOpinion != choice) {
            int currentSeen = memory.get(choice);
            if (otherInMemory > currentSeen)//change if the one is bigger than the other
                choice = otherOpinion;     
        }
    }

    
    /**
     * Context Switching 
     * 
     * Checks for the model switching probability for the current context
     * switch with that probability
     * 
     */
    private void contextSwitching() {
        double switchProb = model.getSwitchingProb(this);
        if (model.random.nextDouble() < switchProb) {       
            model.switchContextOf(this);         
        }
    }
    
   
}
