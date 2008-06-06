/**
 * Mars Simulation Project
 * PowerGrid.java
 * @version 2.84 2008-05-06
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure;
 
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
 
/**
 * The PowerGrid class is a settlement's building power grid.
 */
public class PowerGrid implements Serializable {
    
       private static String CLASS_NAME = 
	    "org.mars_sim.msp.simulation.structure.PowerGrid";
	
       private static Logger logger = Logger.getLogger(CLASS_NAME);
        
	// Unit update events.
	public static final String POWER_MODE_EVENT = "power mode";
	public static final String GENERATED_POWER_EVENT = "generated power";
	public static final String REQUIRED_POWER_EVENT = "required power";
	
    // Statc data members
    public static final String POWER_UP_MODE = "Power up";
    public static final String POWER_DOWN_MODE = "Power down";
        
    // Data members
    private String powerMode;
    private double powerGenerated;
    private double powerRequired;
    private boolean sufficientPower;
    private Settlement settlement;
    
    /**
     * Constructor
     */
    public PowerGrid(Settlement settlement) {
        this.settlement = settlement;
        powerMode = POWER_UP_MODE;
        powerGenerated = 0D;;
        powerRequired = 0D;
        sufficientPower = true;
    }
    
    /**
     * Gets the power grid mode.
     * @return power grid mode string.
     */
    public String getPowerMode() {
    	return powerMode;
    }
    
    /**
     * Sets the power grid mode.
     * @param newPowerMode the new power grid mode.
     */
    public void setPowerMode(String newPowerMode) {
    	if (!powerMode.equals(newPowerMode)) {
    		if (POWER_UP_MODE.equals(newPowerMode)) powerMode = POWER_UP_MODE;
    		else if (POWER_DOWN_MODE.equals(newPowerMode)) powerMode = POWER_DOWN_MODE;
    		settlement.fireUnitUpdate(POWER_MODE_EVENT);
    	}
    }
    
    /**
     * Gets the generated power in the grid.
     * @return power in kW
     */
    public double getGeneratedPower() {
        return powerGenerated;
    }
    
    /**
     * Sets the generated power in the grid.
     * @param newGeneratedPower the new generated power.
     */
    private void setGeneratedPower(double newGeneratedPower) {
    	if (powerGenerated != newGeneratedPower) {
    		powerGenerated = newGeneratedPower;
    		settlement.fireUnitUpdate(GENERATED_POWER_EVENT);
    	}
    }
    
    /**
     * Gets the power required from the grid.
     * @return power in kW
     */
    public double getRequiredPower() {
        return powerRequired;
    }
    
    /**
     * Sets the required power in the grid.
     * @param newRequiredPower the new required power.
     */
    private void setRequiredPower(double newRequiredPower) {
    	if (powerRequired != newRequiredPower) {
    		powerRequired = newRequiredPower;
    		settlement.fireUnitUpdate(REQUIRED_POWER_EVENT);
    	}
    }
    
    /**
     * Checks if there is enough power in the grid for all 
     * buildings to be set to full power.
     * @return true if sufficient power
     */
    public boolean isSufficientPower() {
        return sufficientPower;
    }
    
    /**
     * Time passing for power grid.
     *
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) throws BuildingException {
        
        // Clear and recalculate power
        double tempPowerGenerated = 0D;
        double tempPowerRequired = 0D;
        
        BuildingManager manager = settlement.getBuildingManager();
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine(settlement.getName() + " power situation: ");
        }
        
        // Determine total power generated by buildings.
        Iterator<Building> iPow = manager.getBuildings(PowerGeneration.NAME).iterator();
        while (iPow.hasNext()) {
        	Building building = iPow.next();
            PowerGeneration gen = (PowerGeneration) building.getFunction(PowerGeneration.NAME);
            tempPowerGenerated += gen.getGeneratedPower();
            // logger.info(((Building) gen).getName() + " generated: " + gen.getGeneratedPower());
        }
        setGeneratedPower(tempPowerGenerated);
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Total power generated: " + powerGenerated);
        }
        List<Building> buildings = manager.getBuildings();
        
        if (powerMode.equals(POWER_UP_MODE)) {
        	// Determine total power used by buildings when set to full power mode.
        	Iterator<Building> iUsed = buildings.iterator();
        	while (iUsed.hasNext()) {
            	Building building = iUsed.next();
            	building.setPowerMode(Building.FULL_POWER);
            	tempPowerRequired += building.getFullPowerRequired();
            	
            	if(logger.isLoggable(Level.FINE)) {
            	    logger.fine(building.getName() + " full power used: " + 
            	    		building.getFullPowerRequired());
            	}
        	}
        	setRequiredPower(tempPowerRequired);
        	
        	if(logger.isLoggable(Level.FINE)) {
        	    logger.fine("Total full power required: " + powerRequired);
        	}
        }
        else if (powerMode.equals(POWER_DOWN_MODE)) {
			// Determine total power used by buildings when set to power down mode.
			Iterator<Building> iUsed = buildings.iterator();
			while (iUsed.hasNext()) {
				Building building = iUsed.next();
			    building.setPowerMode(Building.POWER_DOWN);
				tempPowerRequired += building.getPoweredDownPowerRequired();
				
				if(logger.isLoggable(Level.FINE)) {
				logger.fine(building.getName() + " power down power used: " + 
						building.getPoweredDownPowerRequired());
				}
			}
			setRequiredPower(tempPowerRequired);
			
			if(logger.isLoggable(Level.FINE)) {
			    logger.fine("Total power down power required: " + powerRequired);
			}
        }
        
        // Check if there is enough power generated to fully supply each building.
        if (getRequiredPower() <= getGeneratedPower()) {
            sufficientPower = true;
        }
        else {
            sufficientPower = false;
            double neededPower = getRequiredPower() - getGeneratedPower();
            
            // Reduce each building's power mode to low power until 
            // required power reduction is met.
            if (!powerMode.equals(POWER_DOWN_MODE)) {
            	Iterator<Building> iLowPower = buildings.iterator();
            	while (iLowPower.hasNext() && (neededPower > 0D)) {
                	Building building = iLowPower.next();
                	if (!powerSurplus(building, Building.FULL_POWER)) {
                		building.setPowerMode(Building.POWER_DOWN);
                		neededPower -= building.getFullPowerRequired() - 
                        	building.getPoweredDownPowerRequired();
                	}
            	}
            }
            
            // If power needs are still not met, turn off the power to each 
            // uninhabitable building until required power reduction is met.
            if (neededPower > 0D) {
                Iterator<Building> iNoPower = buildings.iterator();
                while (iNoPower.hasNext() && (neededPower > 0D)) {
                    Building building = iNoPower.next();
                    if (!powerSurplus(building, Building.POWER_DOWN) && 
                    		!(building.hasFunction(LifeSupport.NAME))) {
                        building.setPowerMode(Building.NO_POWER);
                        neededPower -= building.getPoweredDownPowerRequired();
                    }
                }
            }
            
            // If power needs are still not met, turn off the power to each inhabitable building 
            // until required power reduction is met.
            if (neededPower > 0D) {
                Iterator<Building> iNoPower = buildings.iterator();
                while (iNoPower.hasNext() && (neededPower > 0D)) {
                    Building building = iNoPower.next();
                    if (!powerSurplus(building, Building.POWER_DOWN) && 
                        	building.hasFunction(LifeSupport.NAME)) {
                        building.setPowerMode(Building.NO_POWER);
                        neededPower -= building.getPoweredDownPowerRequired();
                    }
                }
            }
        }
    }  
    
    /**
     * Checks if building generates more power 
     * than it uses in a given power mode.
     *
     * @param building the building
     * @param mode the building's power mode to check.
     * @return true if building supplies more power than it uses.
     * throws BuildingException if error in power generation.
     */
    private boolean powerSurplus(Building building, String mode) throws BuildingException {
        double generated = 0D;
        if (building.hasFunction(PowerGeneration.NAME)) {
        	PowerGeneration powerGeneration = 
        		(PowerGeneration) building.getFunction(PowerGeneration.NAME);
        	generated = powerGeneration.getGeneratedPower(); 
        }
            
        double used = 0D;
        if (mode.equals(Building.FULL_POWER)) used = building.getFullPowerRequired();
        else if (mode.equals(Building.POWER_DOWN)) used = building.getPoweredDownPowerRequired();
        
        if (generated > used) return true;
        else return false;
    }
}