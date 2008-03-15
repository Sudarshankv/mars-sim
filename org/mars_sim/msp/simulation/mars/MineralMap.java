/**
 * Mars Simulation Project
 * MineralMap.java
 * @version 2.84 2008-03-15
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.mars;

import java.util.Map;

import org.mars_sim.msp.simulation.Coordinates;

/**
 * Interface for mineral maps of Mars.
 */
public interface MineralMap {
	
	// Mineral Types
	public static final String HEMATITE = "Hematite";

    /**
     * Gets the mineral concentration at a given location.
     * @param mineralType the mineral type (see MineralMap.java)
     * @param location the coordinate location.
     * @return percentage concentration (0 to 100.0)
     */
    public double getMineralConcentration(String mineralType, Coordinates location);
    
    /**
     * Gets all of the mineral concentrations at a given location.
     * @param location the coordinate location.
     * @return map of mineral types and percentage concentration (0 to 100.0)
     */
    public Map<String, Double> getAllMineralConcentration(Coordinates location);
}