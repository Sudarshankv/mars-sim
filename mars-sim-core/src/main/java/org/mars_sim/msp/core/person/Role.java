/**
 * Mars Simulation Project
 * Role.java
 * @version 3.08 2015-05-19
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;

import org.mars_sim.msp.core.UnitEventType;

public class Role implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	String name;
	Person person;
	RoleType roleType;

	public Role(Person person) { //, RoleType type) {
		//this.type = type;
		this.person = person;
	}

	/**
	 * Gets the type of role.
	 * @return role type
	 */
	public RoleType getType() {
		return roleType;
	}

	/**
	 * Releases the old role type.
	 * @param role type
	 */
	public void relinquishOldRoleType(RoleType oldType) {
//		RoleType type = getType();
		if (oldType != null) {
		    person.getAssociatedSettlement().getChainOfCommand().releaseRoleTypeMap(oldType);
		}
	}

	/**
	 * Sets new role type.
	 * @param role type
	 */
	public void setNewRoleType(RoleType newType) {
        RoleType oldType = getType();

	    if (newType != oldType) {
//	        relinquishOldRoleType();
//	        System.out.println("New RoleType is "+ type);
	        this.roleType = newType;
	        person.getAssociatedSettlement().getChainOfCommand().addRoleTypeMap(newType);
	        person.fireUnitUpdate(UnitEventType.ROLE_EVENT, newType);
	        relinquishOldRoleType(oldType);
	    }
/*
		if (type == RoleType.SAFETY_SPECIALIST)
			person.getSettlement().getChainOfCommand().addRole(type);
		else if (type == RoleType.ENGINEERING_SPECIALIST)
			person.getSettlement().addEngr();
		else if (type == RoleType.RESOURCE_SPECIALIST)
			person.getSettlement().addResource();
		else if (type == RoleType.MISSION_SPECIALIST)
			person.getSettlement().addMission();
		else if (type == RoleType.SCIENCE_SPECIALIST)
			person.getSettlement().addScience();
		else if (type == RoleType.LOGISTIC_SPECIALIST)
			person.getSettlement().addLogistic();
		else if (type == RoleType.AGRICULTURE_SPECIALIST)
			person.getSettlement().addAgri();
*/
	}

	/**
	 * Override {@link Object#toString()} method.
	 */
	@Override
	public String toString() {
		return roleType.getName();
	}

}
