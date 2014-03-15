/**
 * Mars Simulation Project
 * PerformMathematicalModeling.java
 * @version 3.06 2014-01-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Lab;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A task for performing mathematical modeling in a laboratory for a scientific study.
 */
public class PerformMathematicalModeling
extends Task
implements ResearchScientificStudy, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(PerformMathematicalModeling.class.getName());

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.2D; 

	// TODO Task phase should be an enum.
	private static final String MODELING = "Modeling";

	// Data members.
	/** The scientific study the person is modeling for. */
	private ScientificStudy study;
	/** The laboratory the person is working in. */
	private Lab lab;
	/** The lab's associated malfunction manager. */
	private MalfunctionManager malfunctions;
	/** The research assistant. */
	private Person researchAssistant;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
    public PerformMathematicalModeling(Person person) {
        // Use task constructor.
        super("Perform Mathematical Modeling", person, true, false, STRESS_MODIFIER, 
                true, 10D + RandomUtil.getRandomDouble(30D));
        
        // Determine study.
        study = determineStudy();
        if (study != null) {
            lab = getLocalLab(person);
            if (lab != null) {
                addPersonToLab();
            }
            else {
                logger.info("lab could not be determined.");
                endTask();
            }
        }
        else {
            logger.info("study could not be determined");
            endTask();
        }
        
        // Initialize phase
        addPhase(MODELING);
        setPhase(MODELING);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;
        
        ScienceType mathematics = ScienceType.MATHEMATICS;
        
        // Add probability for researcher's primary study (if any).
        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
        ScientificStudy primaryStudy = studyManager.getOngoingPrimaryStudy(person);
        if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
            if (!primaryStudy.isPrimaryResearchCompleted()) {
                if (mathematics == primaryStudy.getScience()) {
                    try {
                        Lab lab = getLocalLab(person);
                        if (lab != null) {
                            double primaryResult = 50D;
                    
                            // Get lab building crowding modifier.
                            primaryResult *= getLabCrowdingModifier(person, lab);
                    
                            // If researcher's current job isn't related to study science, divide by two.
                            Job job = person.getMind().getJob();
                            if (job != null) {
                                ScienceType jobScience = ScienceType.getJobScience(job);
                                if (primaryStudy.getScience() != jobScience) {
                                    primaryResult /= 2D;
                                }
                            }
                    
                            result += primaryResult;
                        }
                    }
                    catch (Exception e) {
                        logger.severe("getProbability(): " + e.getMessage());
                    }
                }
            }
        }
        
        // Add probability for each study researcher is collaborating on.
        Iterator<ScientificStudy> i = studyManager.getOngoingCollaborativeStudies(person).iterator();
        while (i.hasNext()) {
            ScientificStudy collabStudy = i.next();
            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
                if (!collabStudy.isCollaborativeResearchCompleted(person)) {
                    ScienceType collabScience = collabStudy.getCollaborativeResearchers().get(person);
                    if (mathematics == collabScience) {
                        try {
                            Lab lab = getLocalLab(person);
                            if (lab != null) {
                                double collabResult = 25D;
                        
                                // Get lab building crowding modifier.
                                collabResult *= getLabCrowdingModifier(person, lab);
                        
                                // If researcher's current job isn't related to study science, divide by two.
                                Job job = person.getMind().getJob();
                                if (job != null) {
                                    ScienceType jobScience = ScienceType.getJobScience(job);
                                    if (collabScience != jobScience) {
                                        collabResult /= 2D;
                                    }
                                }
                        
                                result += collabResult;
                            }
                        }
                        catch (Exception e) {
                            logger.severe("getProbability(): " + e.getMessage());
                        }
                    }
                }
            }
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(PerformMathematicalModeling.class);
        }
        
        return result;
    }
    
    /**
     * Gets the crowding modifier for a researcher to use a given laboratory building.
     * @param researcher the researcher.
     * @param lab the laboratory.
     * @return crowding modifier.
     */
    private static double getLabCrowdingModifier(Person researcher, Lab lab) 
            {
        double result = 1D;
        if (researcher.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Building labBuilding = ((Research) lab).getBuilding();  
            if (labBuilding != null) {
                result *= Task.getCrowdingProbabilityModifier(researcher, labBuilding);     
                result *= Task.getRelationshipModifier(researcher, labBuilding);
            }
        }
        return result;
    }
    
    /**
     * Determines the scientific study that will be researched.
     * @return study or null if none available.
     */
    private ScientificStudy determineStudy() {
        ScientificStudy result = null;
        
        ScienceType mathematics = ScienceType.MATHEMATICS;
        
        List<ScientificStudy> possibleStudies = new ArrayList<ScientificStudy>();
        
        // Add primary study if mathematics and in research phase.
        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        ScientificStudy primaryStudy = manager.getOngoingPrimaryStudy(person);
        if (primaryStudy != null) {
            if (ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase()) && 
                    !primaryStudy.isPrimaryResearchCompleted()) {
                if (mathematics == primaryStudy.getScience()) {
                    // Primary study added twice to double chance of random selection.
                    possibleStudies.add(primaryStudy);
                    possibleStudies.add(primaryStudy);
                }
            }
        }
        
        // Add all collaborative studies with mathematics and in research phase.
        Iterator<ScientificStudy> i = manager.getOngoingCollaborativeStudies(person).iterator();
        while (i.hasNext()) {
            ScientificStudy collabStudy = i.next();
            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase()) && 
                    !collabStudy.isCollaborativeResearchCompleted(person)) {
                ScienceType collabScience = collabStudy.getCollaborativeResearchers().get(person);
                if (mathematics == collabScience) {
                    possibleStudies.add(collabStudy);
                }
            }
        }
        
        // Randomly select study.
        if (possibleStudies.size() > 0) {
            int selected = RandomUtil.getRandomInt(possibleStudies.size() - 1);
            result = possibleStudies.get(selected);
        }
        
        return result;
    }
    
    /**
     * Gets a local lab for mathematical modeling.
     * @param person the person checking for the lab.
     * @return laboratory found or null if none.
     */
    private static Lab getLocalLab(Person person) {
        Lab result = null;
        
        LocationSituation location = person.getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
            result = getSettlementLab(person);
        }
        else if (location == LocationSituation.IN_VEHICLE) {
            result = getVehicleLab(person.getVehicle());
        }
        
        return result;
    }
    
    /**
     * Gets a settlement lab for mathematical modeling.
     * @param person the person looking for a lab.
     * @return a valid modeling lab.
     */
    private static Lab getSettlementLab(Person person) {
        Lab result = null;
        
        BuildingManager manager = person.getSettlement().getBuildingManager();
        List<Building> labBuildings = manager.getBuildings(BuildingFunction.RESEARCH);
        labBuildings = getSettlementLabsWithMathematicsSpeciality(labBuildings);
        labBuildings = BuildingManager.getNonMalfunctioningBuildings(labBuildings);
        labBuildings = getSettlementLabsWithAvailableSpace(labBuildings);
        labBuildings = BuildingManager.getLeastCrowdedBuildings(labBuildings);

        if (labBuildings.size() > 0) {
            Map<Building, Double> labBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                    person, labBuildings);
            Building building = RandomUtil.getWeightedRandomObject(labBuildingProbs);
            result = (Research) building.getFunction(BuildingFunction.RESEARCH);
        }
        
        return result;
    }
    
    /**
     * Gets a list of research buildings with available research space from a list of buildings 
     * with the research function.
     * @param buildingList list of buildings with research function.
     * @return research buildings with available lab space.
     */
    private static List<Building> getSettlementLabsWithAvailableSpace(
            List<Building> buildingList) {
        List<Building> result = new ArrayList<Building>();
        
        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            Research lab = (Research) building.getFunction(BuildingFunction.RESEARCH);
            if (lab.getResearcherNum() < lab.getLaboratorySize()) {
                result.add(building);
            }
        }
        
        return result;
    }
    
    /**
     * Gets a list of research buildings with mathematics specialty from a list of 
     * buildings with the research function.
     * @param buildingList list of buildings with research function.
     * @return research buildings with mathematics specialty.
     */
    private static List<Building> getSettlementLabsWithMathematicsSpeciality(
            List<Building> buildingList) {
        List<Building> result = new ArrayList<Building>();
        
        ScienceType mathematicsScience = ScienceType.MATHEMATICS;
        
        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            Research lab = (Research) building.getFunction(BuildingFunction.RESEARCH);
            if (lab.hasSpecialty(mathematicsScience)) {
                result.add(building);
            }
        }
        
        return result;
    }
    
    /**
     * Gets an available lab in a vehicle.
     * Returns null if no lab is currently available.
     * @param vehicle the vehicle
     * @return available lab
     */
    private static Lab getVehicleLab(Vehicle vehicle) {
        
        Lab result = null;
        
        ScienceType mathematicsScience = ScienceType.MATHEMATICS;
        
        if (vehicle instanceof Rover) {
            Rover rover = (Rover) vehicle;
            if (rover.hasLab()) {
                Lab lab = rover.getLab();
                boolean availableSpace = (lab.getResearcherNum() < lab.getLaboratorySize());
                boolean specialty = lab.hasSpecialty(mathematicsScience);
                boolean malfunction = (rover.getMalfunctionManager().hasMalfunction());
                if (availableSpace && specialty && !malfunction) {
                    result = lab;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Adds a person to a lab.
     */
    private void addPersonToLab() {
        
        try {
            LocationSituation location = person.getLocationSituation();
            if (location == LocationSituation.IN_SETTLEMENT) {
                Building labBuilding = ((Research) lab).getBuilding();
                
                // Walk to lab building.
                walkToLabBuilding(labBuilding);
                
                lab.addResearcher();
                malfunctions = labBuilding.getMalfunctionManager();
            }
            else if (location == LocationSituation.IN_VEHICLE) {
                
                // Walk to lab internal location in rover.
                walkToLabLocationInRover((Rover) person.getVehicle());
                
                lab.addResearcher();
                malfunctions = person.getVehicle().getMalfunctionManager();
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "addPersonToLab(): " + e.getMessage());
        }
    }
    
    /**
     * Walk to lab building.
     * @param labBuilding the lab building.
     */
    private void walkToLabBuilding(Building labBuilding) {
        
        // Determine location within lab building.
        // TODO: Use action point rather than random internal location.
        Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(labBuilding);
        Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
                buildingLoc.getY(), labBuilding);
        
        if (Walk.canWalkAllSteps(person, settlementLoc.getX(), settlementLoc.getY(), 
                labBuilding)) {
            
            // Add subtask for walking to lab building.
            addSubTask(new Walk(person, settlementLoc.getX(), settlementLoc.getY(), 
                    labBuilding));
        }
        else {
            logger.fine(person.getName() + " unable to walk to lab building " + 
                    labBuilding.getName());
            endTask();
        }
    }
    
    /**
     * Walk to lab interior location within rover.
     * @param rover the lab rover.
     */
    private void walkToLabLocationInRover(Rover rover) {
        
        // Determine location of lab within rover.
        // TODO: Use action point rather than random internal location.
        Point2D.Double roverLoc = LocalAreaUtil.getRandomInteriorLocation(rover);
        Point2D.Double adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(roverLoc.getX(), 
                roverLoc.getY(), rover);
        
        if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), 
                rover)) {
            
            // Add subtask for walking to lab interior location.
            addSubTask(new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), 
                    rover));
        }
        else {
            logger.fine(person.getName() + " unable to walk to lab interior location in " + 
                    rover.getName());
            endTask();
        }
    }
    
    @Override
    protected void addExperience(double time) {
        // Add experience to mathematics skill
        // (1 base experience point per 20 millisols of modeling time)
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        double newPoints = time / 20D;
        int academicAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.ACADEMIC_APTITUDE);
        newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(ScienceType.MATHEMATICS.getSkill(), newPoints);
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(1);
        results.add(SkillType.MATHEMATICS);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        return manager.getEffectiveSkillLevel(SkillType.MATHEMATICS);
    }
    
    /**
     * Gets the effective mathematical modeling time based on the person's mathematics skill.
     * @param time the real amount of time (millisol) for modeling.
     * @return the effective amount of time (millisol) for modeling.
     */
    private double getEffectiveModelingTime(double time) {
        // Determine effective research time based on the mathematics skill.
        double modelingTime = time;
        int mathematicsSkill = getEffectiveSkillLevel();
        if (mathematicsSkill == 0){
            modelingTime /= 2D;
        }
        else if (mathematicsSkill > 1) {
            modelingTime += modelingTime * (.2D * mathematicsSkill);
        }
        
        // Modify by tech level of laboratory.
        int techLevel = lab.getTechnologyLevel();
        if (techLevel > 0) {
            modelingTime *= techLevel;
        }
        
        // If research assistant, modify by assistant's effective skill.
        if (hasResearchAssistant()) {
            SkillManager manager = researchAssistant.getMind().getSkillManager();
            int assistantSkill = manager.getEffectiveSkillLevel(SkillType.MATHEMATICS);
            if (mathematicsSkill > 0) {
                modelingTime *= 1D + ((double) assistantSkill / (double) mathematicsSkill);
            }
        }
        
        return modelingTime;
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (MODELING.equals(getPhase())) {
            return modelingPhase(time);
        }
        else {
            return time;
        }
    }
    
    /**
     * Performs the mathematical modeling phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double modelingPhase(double time) {
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) {
            endTask();
        }
        
        // Check for laboratory malfunction.
        if (malfunctions.hasMalfunction()) {
            endTask();
        }
        
        // Check if research in study is completed.
        boolean isPrimary = study.getPrimaryResearcher().equals(person);
        if (isPrimary) {
            if (study.isPrimaryResearchCompleted()) {
                endTask();
            }
        }
        else {
            if (study.isCollaborativeResearchCompleted(person)) {
                endTask();
            }
        }
        
        if (isDone()) {
            return time;
        }
        
        // Add modeling work time to study.
        double modelingTime = getEffectiveModelingTime(time);
        if (isPrimary) {
            study.addPrimaryResearchWorkTime(modelingTime);
        }
        else {
            study.addCollaborativeResearchWorkTime(person, modelingTime);
        }
        
        // Add experience
        addExperience(modelingTime);
        
        // Check for lab accident.
        checkForAccident(time);
        
        return 0D;
    }
    
    /**
     * Check for accident in laboratory.
     * @param time the amount of time researching (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Mathematics skill modification.
        int skill = getEffectiveSkillLevel();
        if (skill <= 3) {
            chance *= (4 - skill);
        }
        else {
            chance /= (skill - 2);
        }

        Malfunctionable entity = null;
        if (lab instanceof Research) {
            entity = ((Research) lab).getBuilding();
        }
        else {
            entity = person.getVehicle();
        }
        
        if (entity != null) {
         
            // Modify based on the entity's wear condition.
            chance *= entity.getMalfunctionManager().getWearConditionAccidentModifier();
            
            if (RandomUtil.lessThanRandPercent(chance * time)) {
                logger.info(person.getName() + " has a lab accident while performing " + 
                        "mathematical modeling");
                entity.getMalfunctionManager().accident();
            }
        }
    }
    
    @Override
    public void endTask() {
        super.endTask();
        
        // Remove person from lab so others can use it.
        try {
            if (lab != null) {
                lab.removeResearcher();
            }
        }
        catch(Exception e) {}
    }
    
    @Override
    public ScienceType getResearchScience() {
        return ScienceType.MATHEMATICS;
    }
    
    @Override
    public Person getResearcher() {
        return person;
    }
    
    @Override
    public boolean hasResearchAssistant() {
        return (researchAssistant != null);
    }
    
    @Override
    public Person getResearchAssistant() {
        return researchAssistant;
    }
    
    @Override
    public void setResearchAssistant(Person researchAssistant) {
        this.researchAssistant = researchAssistant;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        study = null;
        lab = null;
        malfunctions = null;
        researchAssistant = null;
    }
}