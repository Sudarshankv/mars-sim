/**
 * Mars Simulation Project
 * MalfunctionEvent.java
 * @version 3.1.0 2017-09-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;

/**
 * This class represents the historical action of a Malfunction occurring or
 * being resolved.
 */
public class MalfunctionEvent
extends HistoricalEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

//    private Person handyman;
//    
	/**
	 * Create an event associated to a Malfunction.
	 * @param entity Malfunctionable entity with problem.
	 * @param malfunction Problem that has occurred.
	 * @param actor The person/robot who causes or witnesses it.
	 * @param location the place where the malfunction took place.
	 * @param fixed Is the malfunction resolved.
	 */
	public MalfunctionEvent(Malfunctionable entity, Malfunction malfunction, Object actor, String location, boolean fixed) {
		super(
			HistoricalEventCategory.MALFUNCTION,
			(fixed ? EventType.MALFUNCTION_FIXED : EventType.MALFUNCTION_REPORTED),
			entity,
			actor,
			location,
			malfunction.getName()
		);
	}
	
//    public Person getHandyman() {
//    	return handyman;
//    }
//
//    public void setHandyman(Person p) {
//    	handyman = p;
//    }
    
}