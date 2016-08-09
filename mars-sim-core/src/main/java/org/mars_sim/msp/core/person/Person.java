/**
 * Mars Simulation Project
 * Person.java
 * @version 3.08 2015-04-24
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobHistory;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.medical.MedicalAid;
import org.mars_sim.msp.core.reportingAuthority.FindingLife;
import org.mars_sim.msp.core.reportingAuthority.MarsSocietyMissionControl;
import org.mars_sim.msp.core.reportingAuthority.NASAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.SettlingMars;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Medical;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * The Person class represents a person on Mars. It keeps track of everything
 * related to that person and provides information about him/her.
 */
public class Person
extends Unit
implements VehicleOperator, MissionMember, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    /* default logger. */
	private static transient Logger logger = Logger.getLogger(Person.class.getName());
    /** The base carrying capacity (kg) of a person. */
    private final static double BASE_CAPACITY = 60D;

    // Data members
    private boolean bornOnMars;
    /** True if person is dead and buried. */
    private boolean isBuried;
    /** The age of a person */
    private int age;
    
	private int solCache = 1;
			
    /** The height of the person (in cm). */
    private double height;
    /** The height of the person (in kg). */ 
    private double weight;
    
    /** Settlement X location (meters) from settlement center. */
    private double xLoc;
    /** Settlement Y location (meters) from settlement center. */
    private double yLoc;
    /** The birthplace of the person. */
    private String birthplace;
    /** The person's name. */
    private String name;
    
    private String bloodType;
    /** The person's achievement in scientific fields. */
    private Map<ScienceType, Double> scientificAchievement;

    private Map<Integer, Gene> paternal_chromosome;
    private Map<Integer, Gene> maternal_chromosome;

    private int[] emotional_states;

    private LifeSupportType support;

    /** The gender of the person (male or female). */
    private PersonGender gender;
    /** The birth time of the person. */
    private EarthClock birthTimeStamp;
    /** The settlement the person is currently associated with. */
    private Settlement associatedSettlement;
    /** Manager for Person's natural attributes. */
    private NaturalAttributeManager attributes;

    private PersonalityManager personalityManager;

    /** Person's mind. */
    private Mind mind;
    /** Person's physical condition. */
    private PhysicalCondition health;
    private Building diningBuilding;
    private Cooking kitchenWithMeal;
    private PreparingDessert kitchenWithDessert;
    private PersonConfig config;// = SimulationConfig.instance().getPersonConfiguration();
    private Favorite favorite;
    private TaskSchedule taskSchedule;
    private JobHistory jobHistory;
    private Settlement buriedSettlement;
    private Role role;
    private Preference preference;

    private ReportingAuthority ra;

    private Building quarters;
    
    private Point2D bed;
    
    private MarsClock marsClock;
    
    private EarthClock earthClock;
    
    private MasterClock masterClock;
    
    /**
     * Constructs a Person object at a given settlement.
     * @param name the person's name
     * @param gender {@link PersonGender} the person's gender
     * @param birthplace the location of the person's birth
     * @param settlement {@link Settlement} the settlement the person is at
     * @throws Exception if no inhabitable building available at settlement.
     */
    public Person(String name, PersonGender gender, boolean bornOnMars, String birthplace, Settlement settlement) {
        //logger.info("Person's constructor is in " + Thread.currentThread().getName() + " Thread");
    	// Use Unit constructor
        super(name, settlement.getCoordinates());  
        super.setDescription(settlement.getName());

        // Initialize data members
        this.name = name;
        this.bornOnMars = bornOnMars;
        this.xLoc = 0D;
        this.yLoc = 0D;
        this.gender = gender;
        this.birthplace = birthplace;
        this.associatedSettlement = settlement;

        masterClock = Simulation.instance().getMasterClock();
        marsClock = masterClock.getMarsClock();
        earthClock = masterClock.getEarthClock();
        
        config = SimulationConfig.instance().getPersonConfiguration();

        //if (Simulation.instance().getMasterClock() != null)
        //	if (marsClock == null)
        //		marsClock = Simulation.instance().getMasterClock().getMarsClock();
 
        String birthTimeString = createBirthTimeString();

        birthTimeStamp = new EarthClock(birthTimeString);
        attributes = new NaturalAttributeManager(this);

        // 2015-02-27 Added JobHistory
        jobHistory = new JobHistory(this);
        mind = new Mind(this);
        isBuried = false;
        health = new PhysicalCondition(this);
        scientificAchievement = new HashMap<ScienceType, Double>(0);

        // 2015-02-27 Added Favorite class
        favorite = new Favorite(this);

        // 2015-04-28 Added Role class
        role = new Role(this);

        // 2015-03-19 Added TaskSchedule class
        taskSchedule = new TaskSchedule(this);

        // 2015-06-07 Added Preference
        preference = new Preference(this);
        //preference.initializePreference();

        //2016-01-13 Set up chromosomes
        paternal_chromosome = new HashMap<>();
        maternal_chromosome = new HashMap<>();
        setupChromosomeMap();
        
        // Put person in proper building.
        settlement.getInventory().storeUnit(this);
        BuildingManager.addToRandomBuilding(this, settlement);

        support = getLifeSupportType();

        assignReportingAuthority();
    }

    
    //2016-01-13 Added setupChromosomeMap()
    public void setupChromosomeMap() {

    	if (bornOnMars) {
    		
    	}
    	else {

    		// Biochemistry: id 0 - 19
    		setupBloodType();
			
			// Physical Characteristics: id 20 - 39
			setupHeight();
			setupWeight();

			// Personality traits: id 40 - 59
			setupTrait();
    	}
		
    }
    
    
    // 2016-01-12 Added setupTrait()
    public void setupTrait() {
       	int ID = 40;
    	boolean dominant = false;
    	  	
        // Set inventory total mass capacity based on the person's strength.
        int strength = attributes.getAttribute(NaturalAttribute.STRENGTH);
        getInventory().addGeneralCapacity(BASE_CAPACITY + strength);
        
        
    	int rand = RandomUtil.getRandomInt(100);
    	
		Gene trait1_G = new Gene(this, ID, "Trait 1", true, dominant, "Introvert", rand);
		paternal_chromosome.put(ID, trait1_G);
	
    }
    
    // 2016-01-12 Added setupBloodType()
    public void setupBloodType() {
       	int ID = 1;
    	boolean dominant = false;
		
		String dad_bloodType = null;
		int rand = RandomUtil.getRandomInt(2);
		if (rand == 0) {
			dad_bloodType = "A";
			dominant = true;
		}
		else if (rand == 1) {
			dad_bloodType = "B";
			dominant = true;
		}
		else if (rand == 2) {
			dad_bloodType = "O";
			dominant = false;
		}	
		
		// Biochemistry 0 - 19
		Gene dad_bloodType_G = new Gene(this, ID, "Blood Type", true, dominant, dad_bloodType, 0);	
		paternal_chromosome.put(ID, dad_bloodType_G);

		
		String mom_bloodType = null;
		rand = RandomUtil.getRandomInt(2);
		if (rand == 0) {
			mom_bloodType = "A";
			dominant = true;
		}
		else if (rand == 1) {
			mom_bloodType = "B";
			dominant = true;
		}
		else if (rand == 2) {
			mom_bloodType = "O";
			dominant = false;
		}	
		
		Gene mom_bloodType_G = new Gene(this, 0, "Blood Type", false, dominant, mom_bloodType, 0);
		maternal_chromosome.put(0, mom_bloodType_G);

		
		if (dad_bloodType.equals("A") && mom_bloodType.equals("A"))
			bloodType = "A";
		else if (dad_bloodType.equals("A") && mom_bloodType.equals("B"))
			bloodType = "AB";
		else if (dad_bloodType.equals("A") && mom_bloodType.equals("O"))
			bloodType = "A";
		else if (dad_bloodType.equals("B") && mom_bloodType.equals("A"))
			bloodType = "AB";
		else if (dad_bloodType.equals("B") && mom_bloodType.equals("B"))
			bloodType = "B";
		else if (dad_bloodType.equals("B") && mom_bloodType.equals("O"))
			bloodType = "B";
		else if (dad_bloodType.equals("O") && mom_bloodType.equals("A"))
			bloodType = "A";
		else if (dad_bloodType.equals("O") && mom_bloodType.equals("B"))
			bloodType = "B";
		else if (dad_bloodType.equals("O") && mom_bloodType.equals("O"))
			bloodType = "O";
		
    }
    
    // 2016-01-12 Added setupHeight()
    public void setupHeight() {
       	int ID = 20;
    	boolean dominant = false;
    	
        double dad_height = (this.gender == PersonGender.MALE ?
                156 + (RandomUtil.getRandomInt(22) + RandomUtil.getRandomInt(22)) :
                    146 + (RandomUtil.getRandomInt(15) + RandomUtil.getRandomInt(15))
                );       
        // Set height of person as gender-correlated curve.
        double mom_height = (this.gender == PersonGender.MALE ?
                156 + (RandomUtil.getRandomInt(22) + RandomUtil.getRandomInt(22)) :
                    146 + (RandomUtil.getRandomInt(15) + RandomUtil.getRandomInt(15))
                );      
	
    	Gene dad_height_G = new Gene(this, ID, "Height", true, dominant, null, dad_height);
    	paternal_chromosome.put(ID, dad_height_G);
		
    	Gene mom_height_G = new Gene(this, ID, "Height", false, dominant, null, mom_height);
    	maternal_chromosome.put(ID, mom_height_G);
    	
        height = (dad_height + mom_height) / 2D;      

    }
    

    // 2016-01-12 Added setupWeight()
    public void setupWeight() {
    	int ID = 21;

    	boolean dominant = false;
    	
        double dad_weight = 56D + (RandomUtil.getRandomInt(100) + RandomUtil.getRandomInt(100))/10D;   
        double mom_weight = 56D + (RandomUtil.getRandomInt(100) + RandomUtil.getRandomInt(100))/10D;
	
    	Gene dad_weight_G = new Gene(this, ID, "Weight", true, dominant, null, dad_weight);
    	paternal_chromosome.put(ID, dad_weight_G);
		
    	Gene mom_weight_G = new Gene(this, ID, "Weight", false, dominant, null, mom_weight);
    	maternal_chromosome.put(ID, mom_weight_G);
    	
    	// Set base mass of person from 58 to 76, peaking at 67 kg.
        weight = (dad_weight + mom_weight) / 2D;   
        setBaseMass(weight);

    }
    
    // 2015-10-05 added setupReportingAuthority()
    public void assignReportingAuthority() {

    	if (ra == null) {
	    	// if he's an NASA astronaut, set mission agenda to FindingLife as follows:
	        //ra = new NASAMissionControl();
	        //ra.setMissionAgenda(new FindingLife());
    		ra = new MarsSocietyMissionControl();
    		ra.setMissionAgenda(new SettlingMars());
    	}
    }

    public ReportingAuthority getReportingAuthority() {
    	return ra;
    }

    public Preference getPreference() {
    	return preference;
    }

    /**
     * Sets the role for a person.
     */
    // 2015-04-28 Added setRole()
	public void setRole(RoleType type) {

		if (type == RoleType.MAYOR) {
			getRole().setNewRoleType(type);
			Job job = JobManager.getJob("Manager");
            if (job != null) {
                mind.setJob(job, true, JobManager.SETTLEMENT, JobAssignmentType.APPROVED, JobManager.SETTLEMENT);
            }
		}
		else
			getRole().setNewRoleType(type);
	}


    /**
     * Gets the instance of Role for a person.
     */
    // 2015-04-28 Added getRole()
	public Role getRole() {
		return role;
	}

    /**
     * Gets the instance of JobHistory for a person.
     */
    // 2015-02-27 Added getJobHistory()
    public JobHistory getJobHistory() {
    	return jobHistory;
    }

    /**
     * Gets the instance of Favorite for a person.
     */
    public Favorite getFavorite() {
    	return favorite;
    }

    /**
     * Gets the instance of the task schedule for a person.
     */
    public TaskSchedule getTaskSchedule() {
    	return taskSchedule;
    }

    /**
     * Create a string representing the birth time of the person.
     * @return birth time string.
     */
    private String createBirthTimeString() {
        // Set a birth time for the person
        int year = 2003 + RandomUtil.getRandomInt(10)
                + RandomUtil.getRandomInt(10);
        int month = RandomUtil.getRandomInt(11) + 1;
        int day;
        if (month == 2) {
            if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                day = RandomUtil.getRandomInt(28) + 1;
            } else {
                day = RandomUtil.getRandomInt(27) + 1;
            }
        } else {
            if (month % 2 == 1) {
                day = RandomUtil.getRandomInt(30) + 1;
            } else {
                day = RandomUtil.getRandomInt(29) + 1;
            }
        }
        // TODO: find out why sometimes day = 0 as seen on
        if (day == 0) {
        	logger.warning( name + "'s date of birth is on the day 0th. Incremementing to the 1st.");
        	day = 1;
        }

        int hour = RandomUtil.getRandomInt(23);
        int minute = RandomUtil.getRandomInt(59);
        int second = RandomUtil.getRandomInt(59);

        return month + "/" + day + "/" + year + " " + hour + ":"
        + minute + ":" + second;
    }

    /**
     * @return {@link LocationSituation} the person's location
     */
    public LocationSituation getLocationSituation() {
        if (isBuried)
            return LocationSituation.BURIED;
        else {
            Unit container = getContainerUnit();
            if (container == null)
                return LocationSituation.OUTSIDE;
            else if (container instanceof Settlement)
                return LocationSituation.IN_SETTLEMENT;
            else if (container instanceof Vehicle)
                return LocationSituation.IN_VEHICLE;
        }
        return null;
    }

    /**
     * Gets the person's X location at a settlement.
     * @return X distance (meters) from the settlement's center.
     */
    public double getXLocation() {
        return xLoc;
    }

    /**
     * Sets the person's X location at a settlement.
     * @param xLocation the X distance (meters) from the settlement's center.
     */
    public void setXLocation(double xLocation) {
        this.xLoc = xLocation;
    }

    /**
     * Gets the person's Y location at a settlement.
     * @return Y distance (meters) from the settlement's center.
     */
    public double getYLocation() {
        return yLoc;
    }

    /**
     * Sets the person's Y location at a settlement.
     * @param yLocation
     */
    public void setYLocation(double yLocation) {
        this.yLoc = yLocation;
    }

    /**
     * Get settlement person is at, null if person is not at a settlement
     * @return the person's settlement
     */
    // 2015-12-04 Changed getSettlement() to fit the original specs of the Location Matrix
    public Settlement getSettlement() {
        if (getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
     	   Settlement settlement = (Settlement) getContainerUnit();
     	   return settlement;
        }

        else if (getLocationSituation() == LocationSituation.OUTSIDE)
     	   return null;

        else if (getLocationSituation() == LocationSituation.IN_VEHICLE) {
     	   Vehicle vehicle = (Vehicle) getContainerUnit();
     	   // Note: a vehicle's container unit may be null if it's outside a settlement
     	   Settlement settlement = (Settlement) vehicle.getContainerUnit();
     	   return settlement;
        }

        else if (getLocationSituation() == LocationSituation.BURIED) {
     	   return null;
        }

        else {
     	   System.err.println("Error in determining " + getName() + "'s getSettlement() ");
     	   return null;
        }
    }


    /**
     * Get vehicle person is in, null if person is not in vehicle
     *
     * @return the person's vehicle
     */
    public Vehicle getVehicle() {
        if (getLocationSituation() == LocationSituation.IN_VEHICLE)
            return (Vehicle) getContainerUnit();
        else
            return null;
    }

    /**
     * Sets the unit's container unit. Overridden from Unit class.
     * @param containerUnit the unit to contain this unit.
     */
    public void setContainerUnit(Unit containerUnit) {
        super.setContainerUnit(containerUnit);
    }

    /**
     * Bury the Person at the current location. The person is removed from any
     * containing Settlements or Vehicles. The body is fixed at the last
     * location of the containing unit.
     */
    public void buryBody() {
        Unit containerUnit = getContainerUnit();
        if (containerUnit != null) {
            containerUnit.getInventory().retrieveUnit(this);
        }
        isBuried = true;
        setAssociatedSettlement(null);
    }

    /**
     * Person has died. Update the status to reflect the change and remove this
     * Person from any Task and remove the associated Mind.
     */
    void setDead() {
        mind.setInactive();
        buryBody();
    }

    /**
     * Person can take action with time passing
     * @param time amount of time passing (in millisols).
     */
    public void timePassing(double time) {
    	//System.out.println("Container Unit : " + this.getContainerUnit());
		//logger.info("Person's timePassing() is in " + Thread.currentThread().getName() + " Thread");

       	// 2015-06-29 Added calling taskSchedule
    	//taskSchedule.timePassing(time);

        // If Person is dead, then skip
        if (health.getDeathDetails() == null) {

            support = getLifeSupportType();
            // Pass the time in the physical condition first as this may
            // result in death.
            
            // if alive
            if (health.timePassing(time, support, config)) {

            	// 2015-06-29 Added calling preference
            	preference.timePassing(time);

            	try {
	                // Mental changes with time passing.
	                mind.timePassing(time);
            	} catch(Exception ex)  {
        			ex.printStackTrace();
        		}         		

        		// check for the passing of each day
        		int solElapsed = MarsClock.getSolOfYear(marsClock);

        		if (solCache != solElapsed) {
        			// 2016-04-20 Added updating a person's age
        			age = getAge();
        			solCache = solElapsed;
        		}
            }
            else {
                // Person has died as a result of physical condition
                setDead();
                LivingAccommodations accommodations = (LivingAccommodations) quarters.getFunction(
                        BuildingFunction.LIVING_ACCOMODATIONS);        
                accommodations.getBedMap().remove(this);
                quarters = null;
                bed = null;
                
            }
        }

    }

    /**
     * Returns a reference to the Person's natural attribute manager
     * @return the person's natural attribute manager
     */
    public NaturalAttributeManager getNaturalAttributeManager() {
        return attributes;
    }


    /**
     * Returns a reference to the Person's Personality manager
     * @return the person's Personality manager
     */
    //2015-12-12 Added
    public PersonalityManager getPersonalityManager() {
        return personalityManager;
    }




    /**
     * Get the performance factor that effect Person with the complaint.
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceRating() {
        return health.getPerformanceFactor();
    }

    /**
     * Returns a reference to the Person's physical condition
     * @return the person's physical condition
     */
    public PhysicalCondition getPhysicalCondition() {
        return health;
    }

    /**
     * Find a medical aid according to the current location.
     * @return Accessible aid.
     */
    MedicalAid getAccessibleAid() {
        MedicalAid found = null;

        LocationSituation location = getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = getSettlement();
            List<Building> infirmaries = settlement.getBuildingManager().getBuildings(BuildingFunction.MEDICAL_CARE);
            if (infirmaries.size() > 0) {
                int rand = RandomUtil.getRandomInt(infirmaries.size() - 1);
                Building foundBuilding = infirmaries.get(rand);
                found = (MedicalAid) foundBuilding.getFunction(BuildingFunction.MEDICAL_CARE);
            }
        }
        if (location  == LocationSituation.IN_VEHICLE) {
            Vehicle vehicle = getVehicle();
            if (vehicle instanceof Medical) {
                found = ((Medical) vehicle).getSickBay();
            }
        }

        return found;
    }

    /**
     * Returns the person's mind
     * @return the person's mind
     */
    public Mind getMind() {
        return mind;
    }

    /**
     * Returns the person's age
     * @return the person's age
     */
    // 2016-04-20 Corrected the use of the day of month
    public int getAge() {
        //EarthClock earthClock = Simulation.instance().getMasterClock().getEarthClock();
        int age = earthClock.getYear() - birthTimeStamp.getYear() - 1;
        if (earthClock.getMonth() >= birthTimeStamp.getMonth())
        	if (earthClock.getDayOfMonth() >= birthTimeStamp.getDayOfMonth()) 
               	age++;

        return age;
    }

    /**
     * Returns the person's height in cm
     * @return the person's height
     */
    public double getHeight() {
        return height;
    }


    /**
     * Returns the person's birth date
     * @return the person's birth date
     */
    public String getBirthDate() {
        return birthTimeStamp.getDateString();
    }

    /**
     * Get the LifeSupport system supporting this Person. This may be from the
     * Settlement, Vehicle or Equipment.
     * @return Life support system.
     */
    private LifeSupportType getLifeSupportType() {

        LifeSupportType result = null;
        List<LifeSupportType> lifeSupportUnits = new ArrayList<LifeSupportType>();

        Settlement settlement = getSettlement();
        if (settlement != null) {
            lifeSupportUnits.add(settlement);
        }
        else {
            Vehicle vehicle = getVehicle();
            if ((vehicle != null) && (vehicle instanceof LifeSupportType)) {

                if (BuildingManager.getBuilding(vehicle) != null) {
                    lifeSupportUnits.add(vehicle.getSettlement());
                }
                else {
                    lifeSupportUnits.add((LifeSupportType) vehicle);
                }
            }
        }

        // Get all contained units.
        Iterator<Unit> i = getInventory().getContainedUnits().iterator();
        while (i.hasNext()) {
            Unit contained = i.next();
            if (contained instanceof LifeSupportType) {
                lifeSupportUnits.add((LifeSupportType) contained);
            }
        }

        // Get first life support unit that checks out.
        Iterator<LifeSupportType> j = lifeSupportUnits.iterator();
        while (j.hasNext() && (result == null)) {
            LifeSupportType goodUnit = j.next();
            if (goodUnit.lifeSupportCheck()) {
                result = goodUnit;
            }
        }

        // If no good units, just get first life support unit.
        if ((result == null) && (lifeSupportUnits.size() > 0)) {
            result = lifeSupportUnits.get(0);
        }

        return result;
    }

    /**
     * Person consumes given amount of food.
     * @param amount the amount of food to consume (in kg)
     * @param takeFromInv is food taken from local inventory?
     */
    public void consumeFood(double amount, boolean takeFromInv) {
        if (takeFromInv) {
        	// takeFrom Inv is true if meal == null, meaning that the person is on an excursion
            //System.out.println(this.getName() + " is calling consumeFood() in Person.java");
        	health.consumeFood(amount, getContainerUnit());
        }
        else { 	// The person is in a settlement, a cookedMeal has been eaten
        		// no need to call health.consumeFood()
            //health.consumeFood(amount);
        }
    }

//    /**
//     * Person consumes given amount of food.
//     * @param amount the amount of food to consume (in kg)
//     * @param takeFromInv
//      */
//    // 2014-11-28 Added consumeDessert()
//    public void consumeDessert(double amount, boolean takeFromInv) {
//        if (takeFromInv) {
//        	// takeFrom Inv is true if meal == null, meaning that the person is on an excursion
//            //System.out.println(this.getName() + " is is calling consumeDessert() in Person.java");
//            health.consumeDessert(amount, getContainerUnit());
//        }
//    }

    /**
     * Person consumes given amount of water.
     * @param amount the amount of water to consume (in kg)
     * @param takeFromInv is water taken from local inventory?

    //2014-11-06 ****NOT USED **** Added consumeWater()
    public void consumeLiquid(double amount, boolean takeFromInv) {
        if (takeFromInv) {
            health.consumeLiquid(amount, getContainerUnit());
        }
        else {
            health.consumeLiquid(amount);
        }
    }
    */
    /**
     * Gets the gender of the person.
     * @return the gender
     */
    public PersonGender getGender() {
        return gender;
    }

    /**
     * Gets the birthplace of the person
     * @return the birthplace
     * @deprecated
     * TODO internationalize the place of birth for display in user interface.
     */
    public String getBirthplace() {
        return birthplace;
    }

    /**
     * Gets the person's local group of people (in building or rover)
     * @return collection of people in person's location.
     */
    public Collection<Person> getLocalGroup() {
        Collection<Person> localGroup = new ConcurrentLinkedQueue<Person>();

        if (getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Building building = BuildingManager.getBuilding(this);
            if (building != null) {
                if (building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
                   LifeSupport lifeSupport = (LifeSupport)
                            building.getFunction(BuildingFunction.LIFE_SUPPORT);
                    localGroup = new ConcurrentLinkedQueue<Person>(lifeSupport.getOccupants());
                }
            }
        } else if (getLocationSituation() == LocationSituation.IN_VEHICLE) {
            Crewable crewableVehicle = (Crewable) getVehicle();
            localGroup = new ConcurrentLinkedQueue<Person>(crewableVehicle.getCrew());
        }

        if (localGroup.contains(this)) {
            localGroup.remove(this);
        }
        return localGroup;
    }

    /**
     * Checks if the vehicle operator is fit for operating the vehicle.
     * @return true if vehicle operator is fit.
     */
    public boolean isFitForOperatingVehicle() {
        return !health.hasSeriousMedicalProblems();
    }

    /**
     * Gets the name of the vehicle operator
     * @return vehicle operator name.
     */
    public String getOperatorName() {
        return getName();
    }

    /**
     * Gets the settlement the person is currently associated with.
     * @return associated settlement or null if none.
     */
    public Settlement getAssociatedSettlement() {
        return associatedSettlement;
    }

    /**
     * Sets the associated settlement for a person.
     * @param newSettlement the new associated settlement or null if none.
     */
    public void setAssociatedSettlement(Settlement newSettlement) {
        if (associatedSettlement != newSettlement) {
            Settlement oldSettlement = associatedSettlement;
            associatedSettlement = newSettlement;
            fireUnitUpdate(UnitEventType.ASSOCIATED_SETTLEMENT_EVENT, associatedSettlement);
            if (oldSettlement != null) {
            	setBuriedSettlement(oldSettlement);
                oldSettlement.fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT, this);
            }
            if (newSettlement != null) {
                newSettlement.fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_PERSON_EVENT, this);
            }
            
            if (associatedSettlement == null)
            	super.setDescription("Dead");            	
            else
            	// TODO: what is the potential use of description for a person ?
            	// Why is it set to associatedSettlement.getName() ?
            	super.setDescription(associatedSettlement.getName());
        }
    }

    /**
     * Sets the associated settlement for a person.
     * @param settlement
     */
    public void setBuriedSettlement(Settlement settlement) {
    	buriedSettlement = settlement;
    }

    public Settlement getBuriedSettlement() {
    	return buriedSettlement;
    }

    /**
     * Gets the person's achievement credit for a given scientific field.
     * @param science the scientific field.
     * @return achievement credit.
     */
    public double getScientificAchievement(ScienceType science) {
        double result = 0D;
        if (scientificAchievement.containsKey(science)) {
            result = scientificAchievement.get(science);
        }
        return result;
    }

    /**
     * Gets the person's total scientific achievement credit.
     * @return achievement credit.
     */
    public double getTotalScientificAchievement() {
        double result = 0d;
        for (double value : scientificAchievement.values()) {
            result += value;
        }
        return result;
    }

    /**
     * Add achievement credit to the person in a scientific field.
     * @param achievementCredit the achievement credit.
     * @param science the scientific field.
     */
    public void addScientificAchievement(double achievementCredit, ScienceType science) {
        if (scientificAchievement.containsKey(science)) {
            achievementCredit += scientificAchievement.get(science);
        }
        scientificAchievement.put(science, achievementCredit);
    }


    public void setDiningBuilding(Building diningBuilding) {
    	this.diningBuilding = diningBuilding;
    }

    public Building getDiningBuilding() {
    	return diningBuilding;
    }

    public void setKitchenWithMeal(Cooking kitchen) {
    	this.kitchenWithMeal = kitchen;
    }

    public Cooking getKitchenWithMeal() {
    	return kitchenWithMeal;
    }

    public void setKitchenWithDessert(PreparingDessert kitchen) {
    	this.kitchenWithDessert = kitchen;
    }

    public PreparingDessert getKitchenWithDessert() {
    	return kitchenWithDessert;
    }

	/**
	  * Gets the building the person is located at
	  * Returns null if outside of a settlement
	  * @return building
	  */
	// 2015-05-18 Added getBuildingLocation()
    public Building getBuildingLocation() {
        Building result = null;
        if (getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = getSettlement().getBuildingManager();
            result = manager.getBuildingAtPosition(getXLocation(), getYLocation());
            //List<Building> buildings = manager.getBuildings();
            //Iterator<Building> i = buildings.iterator();
            // while (i.hasNext()) {
            //	Building b = i.next();
            //	String buildingType = b.getBuildingType();
            //}
        }

        return result;
    }



    @Override
    public String getTaskDescription() {
        return getMind().getTaskManager().getTaskDescription(true);
    }

    @Override
    public void setMission(Mission newMission) {
        getMind().setMission(newMission);
    }

	@Override
	public void setShiftType(ShiftType shiftType) {
		taskSchedule.setShiftType(shiftType);
	}

	public double getFatigue() {
		return health.getFatigue();
	}

	public double getStress() {
		return health.getStress();
	}

	public int[] getBestKeySleepHabit() {
		return health.getBestKeySleepHabit();
	}

	public void updateValueSleepHabit(int millisols, boolean updateType) {
		health.updateValueSleepHabit(millisols, updateType);
	}

	//2015-12-12 Added setEmotionalStates()
	public void setEmotionalStates(int[] states) {
		emotional_states = states;
	}

	public Building getQuarters() {
		return quarters; 
	}

	public void setQuarters(Building quarters) {
		this.quarters = quarters; 
	}

	public Point2D getBed() {
		return bed; 
	}

	public void setBed(Point2D bed) {
		this.bed = bed; 
	}

	
    @Override
    public void destroy() {
        super.destroy();
        attributes.destroy();
        attributes = null;
        mind.destroy();
        mind = null;
        health.destroy();
        health = null;
        gender = null;
        birthTimeStamp = null;
        associatedSettlement = null;
        scientificAchievement.clear();
        scientificAchievement = null;
    }
}