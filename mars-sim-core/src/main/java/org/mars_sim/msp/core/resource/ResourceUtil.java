/**
 * Mars Simulation Project
 * ResourceUtil.java
 * @version 3.1.0 2017-04-07
 * @author Manny Kung
 */

package org.mars_sim.msp.core.resource;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.SimulationConfig;

public class ResourceUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
/*
    public static final int FOOD = 1;
    public static final int WATER = 2;
    public static final int OXYGEN =  3;
    public static final int CO2 = 4;

    public static final int METHANE = 8;
    public static final int ICE = 12;
    public static final int REGOLITH = 142;
    public static final int ROCK_SAMLE = 143;

    public static final int FOOD_WASTE = 16;
    public static final int SOLID_WASTE = 17;
    public static final int GREY_WATER = 19;
    public static final int TABLE_SALT = 23;
    public static final int SODIUM_HYPOCHLORITE = 145;
    public static final int NAPKIN = 150;
*/

    public static final String HYDROGEN = "hydrogen";
    public static final String METHANE = "methane";
    public static final String ICE = "ice";
    public static final String REGOLITH = "regolith";
    public static final String ROCK_SAMLES = "rock samples";
    public static final String SAND = "sand";

    public static final String FOOD_WASTE = "food waste";
    public static final String SOLID_WASTE = "solid waste";
    public static final String GREY_WATER = "grey water";
    public static final String BLACK_WATER = "black water";
    public static final String TABLE_SALT = "table salt";
    public static final String SODIUM_HYPOCHLORITE = "sodium hypochlorite";
    public static final String NAPKIN = "napkin";

    public static final String FERTILIZER = "fertilizer";
    public static final String SOIL = "soil";
    public static final String CROP_WASTE = "crop waste";
    public static final String TOILET_TISSUE = "toilet tissue";

	// Data members.
	//private Set<AmountResource> resources;// = new TreeSet<AmountResource>();
    private static Map<String, AmountResource> amountResourceMap;
    private static Map<Integer, AmountResource> amountResourceIDMap;
    private static Map<Integer, String> IDNameMap;

	private Set<AmountResource> resources;

	// NOTE: This instance is critical during deserialization.
	// When loading the saved sim, amountResourceConfig from the saved sim will be copied over here
	// This way, the newly created amountResourceConfig will be overridden
	// and the resources from the original version of amountResourceConfig will be preserved
	// The drawback is that this won't work if one loads from a saved sim that has an outdated list of Amount Resources
	private AmountResourceConfig amountResourceConfig;

	public static AmountResource foodAR;
	public static AmountResource oxygenAR;
	public static AmountResource waterAR;
	public static AmountResource carbonDioxideAR;

	public static AmountResource hydrogenAR;

    public static AmountResource tableSaltAR;
    public static AmountResource NaClOAR;
    public static AmountResource greyWaterAR;
    public static AmountResource blackWaterAR;

    public static AmountResource foodWasteAR;
    public static AmountResource solidWasteAR;
    public static AmountResource napkinAR;

	public static AmountResource methaneAR;
	public static AmountResource regolithAR;
    public static AmountResource iceAR;
    public static AmountResource rockSamplesAR;
	public static AmountResource sandAR;

    public static AmountResource fertilizerAR;
    public static AmountResource soilAR;
    public static AmountResource cropWasteAR;
    public static AmountResource toiletTissueAR;

	private static AmountResource[] ARs = new AmountResource[20];
	private static int[] ARs_int = new int[20];

	/**
	 * Default Constructor for ResoureUtil
	 */
    private ResourceUtil() {
    	createResourceSet();
    }

	/**
	 * Creates the singleton instance.
	 */
	private static ResourceUtil INSTANCE = new ResourceUtil();

	/**
	 * Gets the singleton instance.
	 * @param instance the singleton instance.
	 */
	public static ResourceUtil getInstance() {
		return INSTANCE;
	}

	/**
	 * Sets the singleton instance.
	 * @param instance the singleton instance.
	 */
	public static void setInstance(ResourceUtil instance) {
  		ResourceUtil.INSTANCE = instance;
	}

    public void initializeNewSim() {
        createMaps();
		createARs();
    }

    public void initializeSavedSim() {
        createMaps();
		createARs();
    }

	public void createResourceSet() {
 		amountResourceConfig = SimulationConfig.instance().getResourceConfiguration();
 		//System.out.println("amountResourceConfig : " + System.identityHashCode(amountResourceConfig));
		resources = amountResourceConfig.getAmountResources();
	}

    public void createMaps() {
		amountResourceMap = new HashMap<String, AmountResource>();
		for (AmountResource resource : resources) {
			amountResourceMap.put(resource.getName(), resource);
		}

		amountResourceIDMap = new HashMap<Integer, AmountResource>();
		for (AmountResource resource : resources) {
			amountResourceIDMap.put(resource.getID(), resource);
		}

		IDNameMap = new HashMap<Integer, String>();
		for (AmountResource resource : resources) {
			IDNameMap.put(resource.getID(), resource.getName());
		}
    }

    public void createARs() {

    	foodAR = findAmountResource(LifeSupportType.FOOD);			// 1
		waterAR = findAmountResource(LifeSupportType.WATER);		// 2
		oxygenAR = findAmountResource(LifeSupportType.OXYGEN);		// 3
		carbonDioxideAR = findAmountResource(LifeSupportType.CO2);	// 4

		hydrogenAR = findAmountResource(HYDROGEN);		// 7
    	methaneAR = findAmountResource(METHANE);			// 8
        soilAR = findAmountResource(SOIL);				// 11
        iceAR = findAmountResource(ICE);					// 12

        cropWasteAR = findAmountResource(CROP_WASTE);	// 14
        foodWasteAR = findAmountResource(FOOD_WASTE);			// 16
        solidWasteAR = findAmountResource(SOLID_WASTE);		// 17

        greyWaterAR = findAmountResource(GREY_WATER);			// 19
        blackWaterAR = findAmountResource(BLACK_WATER);			// 20

        tableSaltAR = findAmountResource(TABLE_SALT); 		// 23

        fertilizerAR = findAmountResource(FERTILIZER);  // 142
    	regolithAR = findAmountResource(REGOLITH);		// 153
        rockSamplesAR = findAmountResource(ROCK_SAMLES);	// 154
      	sandAR = findAmountResource(SAND);		// 155


        NaClOAR = findAmountResource(SODIUM_HYPOCHLORITE);	// 156
        napkinAR = findAmountResource(NAPKIN);				// 161
        toiletTissueAR = findAmountResource(TOILET_TISSUE);	// 162

    }

    public static void printID() {

        ARs = new AmountResource[] {
        		foodAR,
				waterAR,
        		oxygenAR,
				carbonDioxideAR,

				hydrogenAR,
		    	methaneAR,
                soilAR,
		        iceAR,

                cropWasteAR,
		        foodWasteAR,
		        solidWasteAR,

		        greyWaterAR,
		        blackWaterAR,
		        tableSaltAR,

		        fertilizerAR,
		    	regolithAR,
		        rockSamplesAR,

		        NaClOAR,
		        napkinAR,
                toiletTissueAR
        	};

        for (int i=0; i< 20; i++) {
        	//System.out.println(ARs[i].getName());
        //for (AmountResource ar : ARs) {
        	int n = findIDbyAmountResourceName(ARs[i].getName());
        	ARs_int[i] = n;
        	System.out.println(ARs[i] + " : " + ARs_int[i]);
        }

        //for (int i=0; i< 20; i++) {
        //for (int n : ar_int) {
        //	System.out.println(ARs[i] + " : " + ARs_int[i]);
		//}

    }



	/**
	 * Finds an amount resource by id.
	 * @param id the resource's id.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static AmountResource findAmountResource(int id) {
		//count++;
		//if (count%50_000 == 0) System.out.println("# of calls on findAmountResource() : " + count);
		//AmountResource result = null;
		//Map<Integer, AmountResource> map = getAmountResourcesIDMap();
		//result = getAmountResourcesIDMap().get(id);
		//if (result != null) return result;
		//else throw new IllegalStateException("Resource: " + id + " could not be found.");
		return amountResourceIDMap.get(id);
	}


	/**
	 * Finds an amount resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.

	public static AmountResource findAmountResource(String name) {
		count++;
		if (count%50_000 == 0) System.out.println("# of calls on findAmountResource() : " + count);
		//AmountResource result = null;
		Iterator<AmountResource> i = getAmountResources().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			if (resource.getName().equals(name.toLowerCase())) //result = resource;
				return resource;
		}
		return null;
		//if (result != null) return result;
		//else throw new IllegalStateException("Resource: " + name + " could not be found.");
	}
*/

	/**
	 * Finds an amount resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
 */
	public static AmountResource findAmountResource(String name) {
		//count++;
		//if (count%50_000 == 0) System.out.println("# of calls on findAmountResource() : " + count);
/*
		AmountResource result = null;
		Iterator<AmountResource> i = getAmountResources().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			if (resource.getName().equals(name.toLowerCase())) result = resource;
		}
		if (result != null) return result;
		else throw new IllegalStateException("Resource: " + name + " could not be found.");

		//getAmountResources().forEach(r -> {
		//	if (r.getName().equals(name.toLowerCase()))
		//		return r;
		//});

		// 2016-12-08 Using Java 8 stream
		return getAmountResources()
				.stream()
				//.parallelStream()
				.filter(item -> item.getName().equals(name.toLowerCase()))
				.findFirst().orElse(null);//.get();


		AmountResource ar = amountResourceMap.get(name.toLowerCase());
		if (name.equalsIgnoreCase("oxygen")) {
	       	System.out.println("ResourceUtil : findAmountResource()");
			System.out.println("oxygen : " + ar.hashCode());
		}
		return ar;
*/
		return amountResourceMap.get(name.toLowerCase());

	}


	/**
	 * Finds an amount resource by name.
	 * @param name the name of the resource.
	 * @return resource
	 * @throws ResourceException if resource could not be found.
	 */
	public static int findIDbyAmountResourceName(String name) {
		//count++;
		//if (count%50_000 == 0) System.out.println("# of calls on findAmountResource() : " + count);
		//Map<Integer, String> map = getIDNameMap();
		//Object result = null;
		//result = getKeyByValue(getIDNameMap(), name.toLowerCase());
		//if (result != null) return (int) result;
		//else throw new IllegalStateException("Resource: " + name + " could not be found.");
		return (int)(getKeyByValue(getIDNameMap(), name.toLowerCase()));
	}


	/**
	 * Returns the first matched key from a given value in a map for one-to-one relationship
	 * @param map
	 * @param value
	 * @return key
	 */
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}

	/**
	 * Returns a set of keys from a given value in a map using Java 8 stream
	 * @param map
	 * @param value
	 * @return a set of key
	 */
	public static <T, E> Set<T> getKeySetByValue(Map<T, E> map, E value) {
	    return map.entrySet()
	              .stream()
	              .filter(entry -> Objects.equals(entry.getValue(), value))
	              .map(Map.Entry::getKey)
	              .collect(Collectors.toSet());
	}

	/**
	 * Gets an immutable set of all the amount resources.
	 * @return set of amount resources.
	 */
	public Set<AmountResource> getAmountResources() {
		//if (set == null)
		//	set = Collections.unmodifiableSet(resources);
		//return set;
	   	//createResourceSet();
		return resources;
	}

/* An example method
	private Set<T> intersection(Collection<T> first, Collection<T> second) {
		// intersection with an empty collection is empty
		if (isNullOrEmpty(first) || isNullOrEmpty(second))
			return new HashSet<>();

		return first.stream()
				.filter(second::contains)
				.collect(Collectors.toSet());
	}
*/

	/**
	 * gets a sorted map of all amount resource names by calling
	 * {@link AmountResourceConfig#getAmountResourcesMap()}.
	 * @return {@link Map}<{@link Integer}, {@link String}>
	 */
	public static Map<Integer, String> getIDNameMap() {
		return IDNameMap;
	}

	/**
	 * gets a sorted map of all amount resources by calling
	 * {@link AmountResourceConfig#getAmountResourcesIDMap()}.
	 * @return {@link Map}<{@link Integer},{@link AmountResource}>
	 */
	public static Map<Integer, AmountResource> getAmountResourcesIDMap() {
		return amountResourceIDMap;
	}

	/**
	 * gets a sorted map of all amount resources by calling
	 * {@link AmountResourceConfig#getAmountResourcesMap()}.
	 * @return {@link Map}<{@link String},{@link AmountResource}>
	 */
	public static Map<String,AmountResource> getAmountResourcesMap() {
		return amountResourceMap;
	}

	/**
	 * convenience method that calls {@link #getAmountResources()} and
	 * turns the result into an alphabetically ordered list of strings.
	 * @return {@link List}<{@link String}>
	 */
	public List<String> getAmountResourcesSortedList() {
		List<String> resourceNames = new ArrayList<String>();
		Iterator<AmountResource> i = resources.iterator();
		while (i.hasNext()) {
			resourceNames.add(i.next().getName().toLowerCase());
		}
		Collections.sort(resourceNames);
		return resourceNames;
	}

	/**
	 * Gets the hash code value.
	 */
	//public int hashCode() {
	//	return hashcode;
	//}


    /**
     * Prevents the singleton pattern from being destroyed
     * at the time of serialization
     * @return SimulationConfig instance

    protected Object readResolve() throws ObjectStreamException {
		System.out.println("amountResourceConfig :\t" + amountResourceConfig);
    	return INSTANCE;
    }
    */
/*
    private void writeObject(java.io.ObjectOutputStream out)
    	     throws IOException {

    }

    private void readObject(java.io.ObjectInputStream in)
    	     throws IOException, ClassNotFoundException {

    }

    private void readObjectNoData()
    	     throws ObjectStreamException {

    }
*/
	public void destroy() {
		resources = null;
		amountResourceMap = null;
		amountResourceIDMap = null;
		IDNameMap = null;
	}


}