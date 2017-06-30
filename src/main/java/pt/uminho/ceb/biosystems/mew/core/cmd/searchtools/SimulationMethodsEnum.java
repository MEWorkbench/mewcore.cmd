package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;


public enum SimulationMethodsEnum {
	
	FBA{
		public String getSimulationProperty() {
			return SimulationProperties.FBA;
		}
	},
	PFBA{		
		public String getSimulationProperty() {
			return SimulationProperties.PFBA;
		}
	},
	MOMA{
		public String getSimulationProperty() {
			return SimulationProperties.MOMA;
		}		
	},
	LMOMA{
		public String getSimulationProperty() {			
			return SimulationProperties.LMOMA;
		}		
	},
	NLMOMA{
		public String getSimulationProperty() {
			return SimulationProperties.NORM_LMOMA;
		}
	},
	ROOM{
		public String getSimulationProperty() {		
			return SimulationProperties.ROOM;
		}
	},
	MIMBL{		
		public String getSimulationProperty() {
			return SimulationProperties.MIMBL;
		}		
	};
	

	
	public abstract String getSimulationProperty();
	
	public static String getFromString(String method){
		return SimulationMethodsEnum.valueOf(method.toUpperCase()).getSimulationProperty();
	}

}
