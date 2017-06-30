package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.SimulationMethodsEnum;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

public class SimulationConfiguration extends ModelConfiguration {
	
	private static final long	serialVersionUID		= 1L;
	
	public static final String	ENV_COND_DELIMITER		= Delimiter.COMMA.toString();
	
	public static final String	SIM_PREFIX				= "simulation";
	
	public static final String	SIM_SOLVER				= "simulation.solver";
	
	public static final String	SIM_METHOD				= "simulation.method";
	
	public static final String	SIM_METHOD_WT_REFERENCE	= "simulation.method.wtreference";
	
	public static final String	SIM_ENV_COND			= "simulation.environmentalconditions";
	
	public static final String	SIM_INIT_GC				= "simulation.initialgeneticcondition";
	
	public static final String 	SIM_DSPP_FIRST_STAGE_EC	= "simulation.method.dspp.firststageec";
	
	public SimulationConfiguration(String properties) throws Exception {
		super(properties);
		analyzeSimulationProperties();
	}
	
	private void analyzeSimulationProperties() throws Exception {
		
		if (!containsKey(SIM_METHOD)) throw new Exception("Illegal SimulationProperties definition. Must define a [" + SIM_METHOD + "] property.");
		
	}
	
	public String getSimulationSolver() {
		String tag = getProperty(SIM_SOLVER, currentState, true);
		if (tag != null)
			return tag.toUpperCase();
		else
			return null;
	}
	
	public List<String> getSimulationMethod() {
		String tag = getProperty(SIM_METHOD, currentState, true);
		String[] tags = tag.split(Delimiter.SEMICOLON.toString());
		ArrayList<String> sim = new ArrayList<String>(tags.length);
		for (String t : tags) {
			t = t.trim();
			sim.add(SimulationMethodsEnum.getFromString(t));
		}
		
		return sim;
	}
	
	public EnvironmentalConditions getEnvironmentalConditions() {
		String file = getProperty(SIM_ENV_COND, currentState, true);
		if (file != null && !file.isEmpty()) {
			try {
				EnvironmentalConditions toret = EnvironmentalConditions.readFromFile(file, ENV_COND_DELIMITER);
				EnvironmentalConditions variation = getEnvironmentalConditionsVariation();
				if (variation != null && variation.getNumberOfEnvironmentalConditions() > 0) {
					toret.addAllReactionConstraints(variation);
				}
				return toret;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}
	
	public EnvironmentalConditions getEnvironmentalConditionsOnly(){
		String file = getProperty(SIM_ENV_COND, currentState, true);
		if (file != null && !file.isEmpty()) {
			try {
				return EnvironmentalConditions.readFromFile(file, ENV_COND_DELIMITER);				
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}
	
	public EnvironmentalConditions getEnvironmentalConditionsVariation() {
		String file = getProperty(SIM_INIT_GC, currentState, true);
		if (file != null && !file.isEmpty()) {
			try {
				return EnvironmentalConditions.readFromFile(file, ENV_COND_DELIMITER);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}
	
	public FluxValueMap getWTReference() {
		String file = getProperty(SIM_METHOD_WT_REFERENCE, currentState, true);
		if (file != null && !file.isEmpty()) {
			try {
				return FluxValueMap.loadFromFile(file, ENV_COND_DELIMITER);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}
	
	public EnvironmentalConditions getDSPPFirstStageEnvironmentalConditions() {
		String file = getProperty(SIM_DSPP_FIRST_STAGE_EC, currentState, true);
		if (file != null && !file.isEmpty()) {
			try {
				return EnvironmentalConditions.readFromFile(file, ENV_COND_DELIMITER);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}
}
