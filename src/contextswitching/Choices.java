/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contextswitching;

/**
 *
 * @author Davide Nunes
 */
public class Choices {
    public static final int NUM_OPINIONS = 2;
    private static int[] opinions = initOpinions();
    
    /**
     * Returns all the possible opinions for this model
     */ 
    public static int[] getAllChoices(){
        return opinions;
    }
    
    private static int[] initOpinions(){
        int[] op = new int[NUM_OPINIONS];
        for(int i = 0; i<NUM_OPINIONS; i++)
            op[i] = i;
        return op;
    }
    
}
