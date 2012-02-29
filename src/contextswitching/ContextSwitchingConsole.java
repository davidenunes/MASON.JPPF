package contextswitching;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import sim.display.Console;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;

/**
 * Basic GUI console that allows the model to be executed, stopped, loaded from
 * a checkpoint and allows for the inspection of basic model properties
 *
 * @author Davide Nunes
 */
public class ContextSwitchingConsole extends GUIState {

    public static void main(String[] args) {
        ContextSwitchingConsole consoleGUI = new ContextSwitchingConsole();

        Console console = new Console(consoleGUI);
        console.setVisible(true);
    }

    /**
     * Constructors
     * 
     * Basically constructs a console with a new simulation model as base
     */
    public ContextSwitchingConsole() {
        super(new ContextSwitchingModel(System.currentTimeMillis()));

    }

    public ContextSwitchingConsole(SimState state) {
        super(state);

    }

    /**
     * Overridden to provide a custom Model inspector
     * @see ModelProperties
     * 
     * this inspector allows for the inspection of global model properties
     * like the number of total agent encounters performed
     * the current state of the choices taken by the agents, etc
     * 
     * @return model inspector ModelProperties  
     */
    @Override
    public Object getSimulationInspectedObject() {
        return new ModelProperties(state);
    }

    /**
     * Builds an inspector from <code>getSimulationInspectedObject()</code>
     * which allow us to provide a custom object that inspects the state of the
     * simulation model and add new measures there
     * 
     * @return modelInspector
     */
    @Override
    public Inspector getInspector() {
        Inspector insp = super.getInspector();
        insp.setVolatile(true);
        return insp;
    }

    /**
     * Name of the model to be displayed in the GUI
     * 
     * @return name String name of this model  
     */
    public static String getName() {
        return "Context Switching Console";
    }

    /**
     * MASON MODEL Description
     * 
     * @return htmlDescription a simple HTML description to be displayed in the 
     * GUI 
     */
    public static Object getInfo() {
        return "<h3>Context Switching Model</h3>"
                + "<p>A simple consensus game in which agents interact in "
                + "multiple social contexts represented by social networks.</p>"
                + "<p> In this model the agents try to achieve an arbitrary consensus "
                + "by playing a simple majority game with memory of encounters."
                + "<p> The Model is set to a sample configuration (see reset() method)" ;

    }

    /**
     * This controls what happens in a step of the simulation
     * we override this method to provide a new stop criteria
     * which is the fact that the agents reach the specified required consensus
     */
    @Override
    public boolean step() {

        boolean stop;
        ContextSwitchingModel model = (ContextSwitchingModel) state;


        if (model.consensusReached()) {
            stop = true;
        } else {
            stop = super.step();
        }
        return stop;
    }

    /**
     * Sets up the model to a sample configuration
     * @throws Exception 
     */
    private void reset() throws Exception {

        ContextSwitchingModel model = (ContextSwitchingModel) state;
        File[] networks = new File[]{
            new File("NetworkFiles/barabasi_500.np"),
            new File("NetworkFiles/barabasi_500_2.np"),
            new File("NetworkFiles/regular_500.np")
        };


        ModelConfiguration config = new ModelConfiguration(
                500, 3, 0.9, new double[]{0.75, 0.75, 0.5}, networks);



        model.configureModel(config);
        model.initializeModel();


    }

    /**
     * Start overridden in order to configure the model every time we
     */
    @Override
    public void start() {
        try {
            reset();
        } catch (Exception ex) {
            Logger.getLogger(ContextSwitchingConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.start();
    }

    /**
     * Object to be used as an inspector of this model
     * allows for monitoring and setting different model parameters
     * 
     * 
     * get methods are mapped to the interface 
     * set method allow for parameter to be edited by the user
     */
    public class ModelProperties {

        int lastNumEnc = 0;
        ContextSwitchingModel model;
        
        
        public ModelProperties(SimState sstate) {
            this.model = (ContextSwitchingModel) sstate;

        }

        public int getNumOpinion1() {
            if (model.isInitialised()) {
                return model.getOpinionCout()[0];
            }
            return -1;
        }

        public int getNumOpinion2() {
            if (model.isInitialised()) {
                return model.getOpinionCout()[1];
            }
            return -1;
        }

        public int getNumEncounters() {
            if (model.isInitialised()) {
                return model.getNumEncounters();
            }
            return 0;
        }

        public double getAvgEncountersPerCycle() {

            if (model.isInitialised()) {

                double avg = model.getNumEncounters() / (model.schedule.getSteps() * 1.0);
                return avg;
            }
            return 0;
        }

        public String getOpinionCount() {
            if (model.isInitialised()) {
                return Arrays.toString(model.getOpinionCout());
            }
            return null;
        }
    }
}
