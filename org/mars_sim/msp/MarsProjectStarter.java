/**
 * Mars Simulation Project
 * MarsProjectStarter.java
 * @version 2.85 2008-07-24
 * @author Scott Davis
 */

package org.mars_sim.msp;

import java.io.IOException;

/**
 * MarsProjectStarter is the default main class for the MarsProject.jar executable JAR.
 * It creates a new virtual machine with 256MB memory and logging properties.
 */
public class MarsProjectStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			String commandStr = "java -Xms256m -Xmx256m -Djava.util.logging.config.file=logging.properties" + 
			" -cp MarsProject.jar org.mars_sim.msp.MarsProject";
			System.out.println("Command: " + commandStr);
			Runtime.getRuntime().exec(commandStr);
		}
		catch(IOException e) {
			System.out.println("Error starting MarsProject");
			e.printStackTrace(System.err);
		}
	}
}