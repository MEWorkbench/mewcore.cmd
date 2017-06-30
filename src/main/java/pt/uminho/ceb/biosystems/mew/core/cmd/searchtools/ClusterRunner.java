package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.jecoli.algorithm.AlgorithmTypeEnum;
import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration.OptimizationConfiguration;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.controlcenter.StrainOptimizationControlCenter;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.optimizationresult.IStrainOptimizationResultSet;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.JecoliOptimizationProperties;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.java.TimeUtils;

public class ClusterRunner {
	
	private OptimizationConfiguration	_configuration	= null;
	private int							_run			= 0;
	private String						_baseName		= null;
														
	public ClusterRunner(OptimizationConfiguration configuration) throws Exception {
		_configuration = configuration;
	}
	
	public ClusterRunner(OptimizationConfiguration configuration, int run, String baseName) throws Exception {
		_configuration = configuration;
		_run = run;
		_baseName = baseName;
	}
	
	public void run() throws Exception {
		
		//legacy support for mergeSort when using java 7
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
//		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
//		CplexParamConfiguration.setWarningStream(null);
//		
		_baseName = (_baseName == null) ? generateOutputFileName(_configuration) + ClusterConstants.DEFAULT_NAME_CONNECTOR + "run" : _baseName + ClusterConstants.DEFAULT_NAME_CONNECTOR + "run" + _run;
		
		StrainOptimizationControlCenter cc = new StrainOptimizationControlCenter();
		
		ISteadyStateModel model = _configuration.getModel();
		EnvironmentalConditions envConditions = _configuration.getEnvironmentalConditions();
		Boolean isMaximization = true; // always maximization, objective functions will deal with specific objective senses on their own
		Boolean isOverUnder2stepApproach = _configuration.isOverUnder2stepApproach();
		String solver = _configuration.getSimulationSolver();
		Map<String, Double> of = new HashMap<>();
		of.put(model.getBiomassFlux(), 1.0);
		
		Map<String, Map<String, Object>> simulationConfiguration = new HashMap<String, Map<String, Object>>();
		for (String method : _configuration.getSimulationMethod()) {
			Map<String, Object> methodConf = new HashMap<>();
			methodConf.put(SimulationProperties.METHOD_ID, method);
			methodConf.put(SimulationProperties.MODEL, model);
			methodConf.put(SimulationProperties.ENVIRONMENTAL_CONDITIONS, envConditions);
			methodConf.put(SimulationProperties.IS_MAXIMIZATION, isMaximization);
			methodConf.put(SimulationProperties.SOLVER, solver);
			methodConf.put(SimulationProperties.OVERUNDER_2STEP_APPROACH, isOverUnder2stepApproach);
			methodConf.put(SimulationProperties.OBJECTIVE_FUNCTION, of);
			simulationConfiguration.put(method, methodConf);
		}
		
		GenericConfiguration genericConfiguration = new GenericConfiguration();
		genericConfiguration.setProperty(GenericOptimizationProperties.STEADY_STATE_MODEL, model);
		genericConfiguration.setProperty(GenericOptimizationProperties.STEADY_STATE_GENE_REACTION_MODEL, model);
		genericConfiguration.setProperty(GenericOptimizationProperties.MAX_SET_SIZE, _configuration.getMaxSize());
		
		genericConfiguration.setProperty(JecoliOptimizationProperties.IS_VARIABLE_SIZE_GENOME, _configuration.isVariableSize());
		
		genericConfiguration.setProperty(GenericOptimizationProperties.NOT_ALLOWED_IDS, _configuration.getOptimizationCriticalIDs());
		genericConfiguration.setProperty(GenericOptimizationProperties.OPTIMIZATION_STRATEGY, _configuration.getOptimizationStrategy());
		genericConfiguration.setProperty(GenericOptimizationProperties.MAP_OF2_SIM, _configuration.getObjectiveFunctions());
		genericConfiguration.setProperty(GenericOptimizationProperties.SIMULATION_CONFIGURATION, simulationConfiguration);
		
		genericConfiguration.setProperty(JecoliOptimizationProperties.TERMINATION_CRITERIA, _configuration.getTerminationCriteria());
		
		genericConfiguration.setProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_BASE_NAME, _baseName);
		genericConfiguration.setProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_INSERT_EVENT_TYPE, _configuration.getOptimizationArchiveInsertionEventType());
		genericConfiguration.setProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_INSERT_FILTER, _configuration.getOptimizationArchiveInsertionFilter());
		genericConfiguration.setProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_PROCESSING_STRATEGY, _configuration.getOptimizationArchiveProcessingStrategy());
		genericConfiguration.setProperty(JecoliOptimizationProperties.ARCHIVE_MANAGER_RESIMULATE_WHEN_FINISH, _configuration.getOptimizationArchiveResimulateWhenFinish());
		
		int[] recombinationParams = _configuration.getEARecombinationParameters();
		if (recombinationParams != null && recombinationParams.length == 4) {
			genericConfiguration.setProperty(JecoliOptimizationProperties.POPULATION_SIZE, recombinationParams[0]);
			genericConfiguration.setProperty(JecoliOptimizationProperties.NUMBER_OF_SURVIVORS, recombinationParams[1]);
			genericConfiguration.setProperty(JecoliOptimizationProperties.OFFSPRING_SIZE, recombinationParams[2]);
			genericConfiguration.setProperty(JecoliOptimizationProperties.ELITISM, recombinationParams[3]);
		}
		
		double[] operatorProbs = _configuration.getEAOperatorProbabilities();
		if (operatorProbs != null && operatorProbs.length >= 2) {
			genericConfiguration.setProperty(JecoliOptimizationProperties.CROSSOVER_PROBABILITY, operatorProbs[0]);
			genericConfiguration.setProperty(JecoliOptimizationProperties.MUTATION_PROBABILITY, operatorProbs[1]);
			if (operatorProbs.length == 4) {
				genericConfiguration.setProperty(JecoliOptimizationProperties.GROW_PROBABILITY, operatorProbs[2]);
				genericConfiguration.setProperty(JecoliOptimizationProperties.SHRINK_PROBABILITY, operatorProbs[3]);
			}
			if (operatorProbs.length == 5) {
				genericConfiguration.setProperty(JecoliOptimizationProperties.MUTATION_RADIUS_PERCENTAGE, operatorProbs[4]);
			}
		}
		
		genericConfiguration.setProperty(GenericOptimizationProperties.OPTIMIZATION_ALGORITHM, _configuration.getAlgorithm().toString());
		genericConfiguration.setProperty(JecoliOptimizationProperties.OU_RANGE, _configuration.getOURange());
		genericConfiguration.setProperty(JecoliOptimizationProperties.OU_EXPONENT_BASE, _configuration.getOUExponentBase());
		
		// redirect java output
		if (_configuration.isRedirectOutput()) {
			System.out.println("redirecting logging output to " + _baseName + ".log");
			System.out.println("redirecting error output to " + _baseName + ".err");
			File file = new File(_baseName + ".log");
			FileOutputStream fos = new FileOutputStream(file);
			PrintStream ps = new PrintStream(fos);
			System.setOut(ps);
			
			File file_err = new File(_baseName + ".err");
			FileOutputStream fos_err = new FileOutputStream(file_err);
			PrintStream ps_err = new PrintStream(fos_err);
			System.setErr(ps_err);
		}
		
//		System.out.println("IS VARSIZE="+_configuration.isVariableSize());
		// run optimization
		long timestart = System.currentTimeMillis();
		IStrainOptimizationResultSet<?, ?> resultSet = cc.execute(genericConfiguration);
		
		System.out.println("Execution took: " + TimeUtils.formatMillis(System.currentTimeMillis() - timestart));
		
		if (resultSet != null && resultSet.getResultList() != null && !resultSet.getResultList().isEmpty()) {
			resultSet.writeToFile(_baseName + ".ss");
		}
	}
	
	public String generateOutputFileName(OptimizationConfiguration conf) throws Exception {
		
		StringBuffer buff = new StringBuffer();
		String organism = conf.getModelName();
		AlgorithmTypeEnum algorithm = conf.getAlgorithm();
		List<String> simMethods = conf.getSimulationMethod();
		String strat = conf.getOptimizationStrategy();
		IndexedHashMap<IObjectiveFunction, String> objectiveFunctions = conf.getObjectiveFunctions();
		
		buff.append(organism + ClusterConstants.DEFAULT_NAME_CONNECTOR);
		buff.append(algorithm.getShortName() + ClusterConstants.DEFAULT_NAME_CONNECTOR);
		for (String sm : simMethods)
			buff.append(sm + ClusterConstants.DEFAULT_NAME_CONNECTOR);
		buff.append(strat + ClusterConstants.DEFAULT_NAME_CONNECTOR);
		
		for (int i = 0; i < objectiveFunctions.size(); i++) {
			IObjectiveFunction of = objectiveFunctions.getKeyAt(i);
			String ofn = (of.isMaximization() ? "MAX" : "MIN") + "~" + of.getShortString();
			buff.append(ofn + ClusterConstants.DEFAULT_NAME_CONNECTOR);
		}
		
		buff.append("run" + _run);
		
		return buff.toString();
	}
	
	public void setBaseName(String baseName) {
		_baseName = baseName;
	}
	
	public void setRun(int run) {
		_run = run;
	}	
}
