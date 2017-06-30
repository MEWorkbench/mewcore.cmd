package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.jecoli.algorithm.AlgorithmTypeEnum;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.IterationListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.terminationcriteria.NumFunctionEvaluationsListenerHybridTerminationCriteria;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.tracker.EvolutionTrackerFile;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.tracker.IEvolutionTracker;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.InsertionStrategy;
import pt.uminho.ceb.biosystems.jecoli.algorithm.multiobjective.archive.components.ProcessingStrategy;
import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.SimulationMethodsEnum;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.IObjectiveFunction;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidFieldException;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.InvalidObjectiveFunctionConfiguration;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.objectivefunctions.ObjectiveFunctionsFactory;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

public class OptimizationConfiguration extends SimulationConfiguration {
	
	private static final long			serialVersionUID					= 1L;
																			
	protected ObjectiveFunctionsFactory	ofFactory;
										
	private static final Pattern		OVER_UNDER_STRATEGY_PATT			= Pattern.compile("^[G|R]OU(.*)");
																			
	private static final Pattern		TERM_FE_PATT						= Pattern.compile("[fF][eE]\\(([0-9]+?)\\)");
																			
	private static final Pattern		TERM_IT_PATT						= Pattern.compile("[iI][tT]\\(([0-9]+?)\\)");
																			
	private static final Pattern		OF_PATTERN							= Pattern.compile("([A-Za-z0-9_]+\\d?)\\((.+)\\)");
																			
	private static final Pattern		LINK_PATTERN						= Pattern.compile("LINK\\((.+?)\\s*,\\s*(.+?)\\)");
																			
	private static final Pattern		TRACKER_FILE						= Pattern.compile("^FILE");
																			
	private static final Pattern		MAX_MEM_PATTERN						= Pattern.compile("(\\d+)([KMG])");
																			
	private static final Pattern		OU_RANGE_PATTERN					= Pattern.compile("\\s*\\[\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\]\\s*");
																			
	private static final String			OF_DELIMITER						= Delimiter.SEMICOLON.toString();
																			
	private static final String			SWAPS_MAP_DELIMITER					= Delimiter.COMMA.toString();
																			
	private static final int			ARCHIVE_DEFAULT_SIZE				= 100;
	
	private static final int			DEFAULT_EXPONENT_BASE				= 2;
																			
	private static final int			DEFAULT_MAX_THREADS					= 1;
																			
	public static final String			OPT_PREFIX							= "optimization";
																			
	public static final String			OPT_STRATEGY						= "optimization.strategy";
																			
	public static final String			OPT_STRATEGY_OU_RANGE				= "optimization.strategy.ou.range";
																			
	public static final String			OPT_STRATEGY_OU_2STEP				= "optimization.strategy.ou.2step";
	
	public static final String			OPT_STRATEGY_OU_EXP_BASE			= "optimization.strategy.ou.expBase";
																			
	public static final String			OPT_STRATEGY_SWAP_MAP				= "optimization.strategy.swap.map";
																			
	public static final String			OPT_STRATEGY_SWAP_MAX				= "optimization.strategy.swap.maxswaps";
																			
	public static final String			OPT_ALGORITHM						= "optimization.algorithm";
																			
	public static final String			OPT_ALGORITHM_EA_RECOMBINATION		= "optimization.algorithm.ea.recombination";
																			
	public static final String			OPT_ALGORITHM_EA_OP_PROBABILITIES	= "optimization.algorithm.ea.operatorprobabilities";
																			
	public static final String			OPT_ALG_MAXTHREADS					= "optimization.algorithm.maxthreads";
																			
	public static final String			OPT_ALG_TERM						= "optimization.algorithm.termination";
																			
	public static final String			OPT_ALG_TRACKER						= "optimization.algorithm.tracker";
																			
	public static final String			OPT_OBJ_FUNC						= "optimization.objectivefunction";
																			
	public static final String			OPT_SOL_VARSIZE						= "optimization.solution.varsize";
																			
	public static final String			OPT_SOL_MAXSIZE						= "optimization.solution.maxsize";
																			
	public static final String			OPT_SOL_MINSIZE						= "optimization.solution.minsize";
																			
	public static final String			OPT_ARCHIVE_SIZE					= "optimization.archive.size";
																			
	public static final String			OPT_ARCHIVE_INSERTION_EVENT_TYPE	= "optimization.archive.insertioneventtype";
																			
	public static final String			OPT_ARCHIVE_INSERTION_FILTER		= "optimization.archive.insertionfilter";
																			
	public static final String			OPT_ARCHIVE_PROCESSING_STRATEGY		= "optimization.archive.processingstrategy";
																			
	public static final String			OPT_ARCHIVE_RESIMULATE_WHEN_FINISH	= "optimization.archive.resimulatewhenfinish";
																			
	public static final String			OPT_RUN								= "optimization.run";
																			
	public static final String			OPT_MAX_MEM							= "optimization.maxmem";
																			
	public static final String			REDIRECT_OUTPUT						= "optimization.redirect.output";
																			
	public static final String			OPT_MANUAL_CRITICALS				= "optimization.critical.manual";
																			
	public static final String			ALLOW_REMOTE_MONITOR				= "optimization.remote";
																			
	public OptimizationConfiguration(String properties) throws Exception {
		super(properties);
		analyzeOptimizationProperties();
		ofFactory = new ObjectiveFunctionsFactory();
	}
	
	protected void analyzeOptimizationProperties() throws Exception {
		
		if (!containsKey(OPT_ALGORITHM))
			throw new Exception("Illegal OptimizationProperties definition. Must define a [" + OPT_ALGORITHM + "] property.");
			
		if (!containsKey(OPT_OBJ_FUNC))
			throw new Exception("Illegal OptimizationProperties definition. Must define a [" + OPT_OBJ_FUNC + "] property.");
			
		if (!containsKey(OPT_ALG_TERM))
			throw new Exception("Illegal OptimizationProperties definition. Must define a [" + OPT_ALG_TERM + "] property.");
			
		if (!containsKey(OPT_SOL_MAXSIZE))
			throw new Exception("Illegal OptimizationProperties definition. Must define a [" + OPT_SOL_MAXSIZE + "] property.");
			
		if (!containsKey(OPT_STRATEGY))
			throw new Exception("Illegal OptimizationProperties definition. Must define a [" + OPT_STRATEGY + "] property.");
		else {
			String strategy = getOptimizationStrategy();
			switch (strategy) {
				case "RKRS":
					if (!containsKey(OPT_STRATEGY_SWAP_MAP))
						throw new Exception("Illegal OptimizationProperties definition. Strategy [" + strategy + "] requires a [" + OPT_STRATEGY_SWAP_MAP + "] property.");
					if (!containsKey(OPT_STRATEGY_SWAP_MAX))
						throw new Exception("Illegal OptimizationProperties definition. Strategy [" + strategy + "] requires a [" + OPT_STRATEGY_SWAP_MAX + "] property.");
					break;
				default:
					break;
			}
		}
		
		if (containsKey(REDIRECT_OUTPUT)) {
			String redirect = getProperty(REDIRECT_OUTPUT);
			try {
				Boolean.parseBoolean(redirect);
			} catch (Exception e) {
				throw new Exception("Illegal SearchProperties definion. Property [" + REDIRECT_OUTPUT + "] must be a boolean (true/false).");
			}
		}
	}
	
	public AlgorithmTypeEnum getAlgorithm() {
		String tag = getProperty(OPT_ALGORITHM, currentState, true);
		if (tag != null) {
			return AlgorithmTypeEnum.valueOf(tag.trim().toUpperCase());
		} else
			return null;
	}
	
	public String getOptimizationStrategy() {
		String tag = getProperty(OPT_STRATEGY, currentState, true);
		if (tag != null)
			return tag.trim();
		else
			return null;
	}
	
	public IndexedHashMap<IObjectiveFunction, String> getObjectiveFunctions() throws Exception {
		String[] ofList = getProperty(OPT_OBJ_FUNC, currentState, true).split(OF_DELIMITER);
		for (String s : ofList)
			System.out.println(s);
		if (ofList.length == 0)
			throw new InvalidFieldException("ObjectiveFunction", "At least one Objective function must be provided!", new ArrayIndexOutOfBoundsException(-1));
			
		IndexedHashMap<IObjectiveFunction, String> objFunctions = new IndexedHashMap<IObjectiveFunction, String>();
		
		for (String ofInfo : ofList) {
			Pair<IObjectiveFunction, String> of = processOFString(ofInfo.trim());
			objFunctions.put(of.getValue(), of.getPairValue());
		}
		
		return objFunctions;
	}
	
	public Map<Map<String,Object>,String> getObjectiveFunctionConfigurations() throws Exception{
		String[] ofList = getProperty(OPT_OBJ_FUNC, currentState, true).split(OF_DELIMITER);
		if (ofList.length == 0){
			throw new InvalidFieldException("ObjectiveFunction", "At least one Objective function must be provided!", new ArrayIndexOutOfBoundsException(-1));			
		}
		
		Map<Map<String,Object>,String> ofConfs = new HashMap<Map<String,Object>,String>();
		
		for (String ofInfo : ofList) {
			Pair<Map<String,Object>, String> of = processOFStringConf(ofInfo.trim());
			ofConfs.put(of.getValue(), of.getPairValue());
		}
		
		return ofConfs;
	}
	
	private Pair<IObjectiveFunction, String> processOFString(String ofString) throws Exception {
		Matcher matcher = LINK_PATTERN.matcher(ofString);
		if (matcher.matches()) {
			String simMethodTag = matcher.group(1);
			String simMethod = getSimulationMethod(simMethodTag);
			String objFuncTag = matcher.group(2);
			
			IObjectiveFunction ofIN = processOFParams(objFuncTag);
			return new Pair<IObjectiveFunction, String>(ofIN, simMethod);
		} else
			throw new InvalidObjectiveFunctionConfiguration(
					"Objective functions incorrectly linked to simulation methods. " + "Must follow this syntax LINK([simMethod1], OF1(param,...); LINK([simMethod2], OF2(param,...))");
	}
	
	private Pair<Map<String,Object>, String> processOFStringConf(String ofString) throws Exception {
		Matcher matcher = LINK_PATTERN.matcher(ofString);
		if (matcher.matches()) {
			String simMethodTag = matcher.group(1);
			String simMethod = getSimulationMethod(simMethodTag);
			String objFuncTag = matcher.group(2);
			
			Map<String,Object> ofIN = processOFParamsConf(objFuncTag);
			return new Pair<Map<String,Object>, String>(ofIN, simMethod);
		} else
			throw new InvalidObjectiveFunctionConfiguration(
					"Objective functions incorrectly linked to simulation methods. " + "Must follow this syntax LINK([simMethod1], OF1(param,...); LINK([simMethod2], OF2(param,...))");
	}
	
	private IObjectiveFunction processOFParams(String ofString) throws InvalidObjectiveFunctionConfiguration {
		Matcher matcher = OF_PATTERN.matcher(ofString);
		matcher.matches();
		//		System.out.println(ofString);
		String ofTag = matcher.group(1);
		String ofArgs = matcher.group(2);
		//		ObjectiveFunctionType oft = ObjectiveFunctionType.valueOf(ofTag);
		String[] argsList = ofArgs.split(Delimiter.COMMA.toString());
		Object[] args = new Object[argsList.length];
		for (int i = 0; i < argsList.length; i++) {
			String trimmedArg = argsList[i].trim();
			Object processedParameter = processParam(trimmedArg);
			if (processedParameter == null) {
				throw new InvalidObjectiveFunctionConfiguration("Objective function argument [" + trimmedArg + "] is invalid. Either invoked method does not exist or it returned a null result!");
			} else {
				System.out.println("PARAM: " + processedParameter.toString());
				args[i] = processedParameter;
			}
		}
		
		IObjectiveFunction of = ofFactory.getObjectiveFunction(ofTag.trim(), args);
		return of;
	}
	
	private Map<String,Object> processOFParamsConf(String ofString) throws InvalidObjectiveFunctionConfiguration {
		Matcher matcher = OF_PATTERN.matcher(ofString);
		matcher.matches();
		String ofTag = matcher.group(1);
		String ofArgs = matcher.group(2);
		String[] argsList = ofArgs.split(Delimiter.COMMA.toString());
		Object[] args = new Object[argsList.length];
		for (int i = 0; i < argsList.length; i++) {
			String trimmedArg = argsList[i].trim();
			Object processedParameter = processParam(trimmedArg);
			if (processedParameter == null) {
				throw new InvalidObjectiveFunctionConfiguration("Objective function argument [" + trimmedArg + "] is invalid. Either invoked method does not exist or it returned a null result!");
			} else {
				System.out.println("PARAM: " + processedParameter.toString());
				args[i] = processedParameter;
			}
		}
		
		IObjectiveFunction of = ofFactory.getObjectiveFunction(ofTag.trim(), args);
		Map<String,Object> ofConf = of.copyConfiguration();
		return ofConf;
	}
	
	private Object processParam(String trimmedArg) {
		Matcher m = INTERNAL_METHOD_PATTERN.matcher(trimmedArg);
		if (m.matches()) {
			String methodName = m.group(1).trim();
			Method method = null;
			try {
				method = this.getClass().getMethod(methodName, null);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			if (method == null) {
				return null;
			} else {
				Object res = null;
				try {
					res = method.invoke(this, null);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return res;
			}
		} else {
			return trimmedArg;
		}
	}
	
	private String getSimulationMethod(String sm) throws Exception {
		SimulationMethodsEnum smconstant = Enum.valueOf(SimulationMethodsEnum.class, sm.toUpperCase());
		
		if (smconstant != null)
			return smconstant.getSimulationProperty();
		else
			throw new Exception("Simulation method [" + sm + "] could not be resolved. Available ones are " + Arrays.toString(SimulationMethodsEnum.values()));
	}
	
	public ITerminationCriteria getTerminationCriteria() throws Exception {
		String termString = getProperty(OPT_ALG_TERM, currentState, true);
		
		ITerminationCriteria termination = null;
		
		Matcher m = TERM_FE_PATT.matcher(termString);
		if (m.matches()) {
			int value = Integer.parseInt(m.group(1));
			termination = new NumFunctionEvaluationsListenerHybridTerminationCriteria(value);
			return termination;
		} else {
			m = TERM_IT_PATT.matcher(termString);
			if (m.matches()) {
				int value = Integer.parseInt(m.group(1));
				termination = new IterationListenerHybridTerminationCriteria(value);
				return termination;
			} else {
				throw new Exception("Invalid termination criteria property [" + termString + "].");
			}
		}
	}
	
	public boolean isVariableSize() {
		return Boolean.valueOf(getProperty(OPT_SOL_VARSIZE));
	}
	
	public int getMaxSize() {
		return Integer.valueOf(getProperty(OPT_SOL_MAXSIZE, currentState, true));
	}
	
	public int getMinSize() {
		String prop = getProperty(OPT_SOL_MINSIZE, currentState, true);
		if (prop != null) {
			return Integer.valueOf(getProperty(OPT_SOL_MINSIZE, currentState, true));
		} else {
			return 1;
		}
	}
	
	public int getMaxSwaps() {
		String prop = getProperty(OPT_STRATEGY_SWAP_MAX, currentState, true);
		if (prop != null)
			return Integer.valueOf(getProperty(OPT_STRATEGY_SWAP_MAX, currentState, true));
		else
			return -1;
	}
	
	public int[] getEARecombinationParameters() {
		String prop = getProperty(OPT_ALGORITHM_EA_RECOMBINATION, currentState, true);
		if (prop != null) {
			String[] tokens = prop.split(",");
			int[] toret = new int[tokens.length];
			for (int i = 0; i < tokens.length; i++) {
				toret[i] = Integer.valueOf(tokens[i].trim());
			}
			return toret;
		} else
			return null;
	}
	
	public double[] getEAOperatorProbabilities() {
		String prop = getProperty(OPT_ALGORITHM_EA_OP_PROBABILITIES, currentState, true);
		if (prop != null) {
			String[] tokens = prop.split(",");
			double[] toret = new double[tokens.length];
			for (int i = 0; i < tokens.length; i++) {
				toret[i] = Double.valueOf(tokens[i].trim());
			}
			return toret;
		} else
			return null;
	}
	
	public static boolean isGeneBased(String strategy) {
		return strategy.startsWith("G");
	}
	
	public boolean isGeneBased() {
		String strategy = getOptimizationStrategy();
		return isGeneBased(strategy);
	}
	
	public static boolean isOverUnder(String strategy) {
		return OVER_UNDER_STRATEGY_PATT.matcher(strategy).matches();
	}
	
	public boolean isOverUnder() {
		String strategy = getOptimizationStrategy();
		return isOverUnder(strategy);
	}
	
	public List<String> getOptimizationCriticalIDs() throws Exception {
		String strategy = getOptimizationStrategy();
		ArrayList<String> critical = new ArrayList<String>();
		String criticalFile = null;
		System.out.print("Loading critical ");
		if (isGeneBased(strategy)) {
			criticalFile = getModelCriticalGenesFile();
			System.out.print("genes ");
		} else {
			criticalFile = getModelCriticalReactionsFile();
			System.out.print("reactions ");
		}
		
		if (criticalFile != null) {
			System.out.print("[" + criticalFile + "]...");
			FileReader fr = new FileReader(criticalFile);
			BufferedReader br = new BufferedReader(fr);
			
			int i = 0;
			while (br.ready()) {
				String str = br.readLine().trim();
				critical.add(str);
				i++;
			}
			
			br.close();
			fr.close();
			System.out.println("done with " + i + " criticals!");
		}
		
		List<String> manuals = getOptimizationManualCriticalIDs();
		if (manuals != null && !manuals.isEmpty()) {
			int total = critical.size() + manuals.size();
			System.out.println("Merging criticals, total = " + total + " ... done!");
			critical.addAll(manuals);
		}
		
		return critical;
	}
	
	public List<String> getOptimizationManualCriticalIDs() throws Exception {
		ArrayList<String> critical = new ArrayList<String>();
		String criticalFile = getProperty(OPT_MANUAL_CRITICALS, currentState, true);
		System.out.print("Loading manual criticals ");
		if (criticalFile != null && !criticalFile.isEmpty()) {
			System.out.print("[" + criticalFile + "]...");
			FileReader fr = new FileReader(criticalFile);
			BufferedReader br = new BufferedReader(fr);
			
			int i = 0;
			while (br.ready()) {
				String str = br.readLine().trim();
				critical.add(str);
				i++;
			}
			
			br.close();
			fr.close();
			System.out.println("done with " + i + " manual criticals!");
		} else
			System.out.println("... not found!");
			
		return critical;
	}
	
	public Map<String, List<String>> getSwapsMap() throws Exception {
		HashMap<String, List<String>> toret = null;
		String swapsFile = getProperty(OPT_STRATEGY_SWAP_MAP, currentState, true);
		System.out.print("Loading swaps map ");
		if (swapsFile != null && !swapsFile.isEmpty()) {
			toret = new HashMap<String, List<String>>();
			System.out.print("[" + swapsFile + "]...");
			BufferedReader br = new BufferedReader(new FileReader(swapsFile));
			
			int i = 0;
			while (br.ready()) {
				String str = br.readLine().trim();
				String tokens[] = str.split(SWAPS_MAP_DELIMITER);
				if (tokens.length < 2) {
					br.close();
					throw new Exception("\nLoading swaps map file [" + swapsFile + "] at line " + i + ". Lines must always contain at least two elements separated by [" + SWAPS_MAP_DELIMITER + "].");
				} else {
					String original = tokens[0];
					List<String> swapList = new ArrayList<String>();
					for (int j = 1; j < tokens.length; j++)
						swapList.add(tokens[j]);
						
					toret.put(original, swapList);
				}
				i++;
			}
			
			br.close();
			System.out.println("done with " + i + " possible swaps!");
		} else
			System.out.println("... not found!");
			
		return toret;
	}
	
	public int getMaxThreads() {
		if (containsKey(OPT_ALG_MAXTHREADS))
			return Integer.valueOf(getProperty(OPT_ALG_MAXTHREADS, currentState, true));
		else
			return DEFAULT_MAX_THREADS;
	}
	
	public int getOptimizationArchiveMaxSize() {
		if (containsKey(OPT_ARCHIVE_SIZE))
			return Integer.parseInt(getProperty(OPT_ARCHIVE_SIZE, currentState, true));
		else
			return ARCHIVE_DEFAULT_SIZE;
	}
	
	public InsertionStrategy getOptimizationArchiveInsertionEventType() {
		if (containsKey(OPT_ARCHIVE_INSERTION_EVENT_TYPE)) {
			InsertionStrategy insertionEventType = InsertionStrategy.valueOf(getProperty(OPT_ARCHIVE_INSERTION_EVENT_TYPE, currentState, true));
			return insertionEventType;
		} else {
			return null;
		}
	}
	
	public InsertionStrategy getOptimizationArchiveInsertionFilter() {
		if (containsKey(OPT_ARCHIVE_INSERTION_FILTER)) {
			InsertionStrategy insertionFilter = InsertionStrategy.valueOf(getProperty(OPT_ARCHIVE_INSERTION_FILTER, currentState, true));
			return insertionFilter;
		} else {
			return null;
		}
	}
	
	public ProcessingStrategy getOptimizationArchiveProcessingStrategy() {
		if (containsKey(OPT_ARCHIVE_PROCESSING_STRATEGY)) {
			ProcessingStrategy processingStragegy = ProcessingStrategy.valueOf(getProperty(OPT_ARCHIVE_PROCESSING_STRATEGY, currentState, true));
			return processingStragegy;
		} else {
			return null;
		}
	}
	
	public Boolean getOptimizationArchiveResimulateWhenFinish() {
		if (containsKey(OPT_ARCHIVE_RESIMULATE_WHEN_FINISH)) {
			Boolean resimulate = Boolean.parseBoolean(getProperty(OPT_ARCHIVE_RESIMULATE_WHEN_FINISH, currentState, true));
			return resimulate;
		} else {
			return null;
		}
	}
	
	public IEvolutionTracker<?> getEvolutionTracker() throws Exception {
		String tag = getProperty(OPT_ALG_TRACKER, currentState, true);
		if (tag != null) {
			Matcher m = TRACKER_FILE.matcher(tag);
			if (m.matches()) {
				return new EvolutionTrackerFile<>();
			} else
				throw new Exception("Invalid evolution tracker property[" + tag + "]. Must be one of [FILE,BD]");
		} else
			return null;
			
	}
	
	public Pair<Integer, Integer> getOURange() throws Exception {
		String tag = getProperty(OPT_STRATEGY_OU_RANGE, currentState, true);
		if (tag != null) {
			Matcher m = OU_RANGE_PATTERN.matcher(tag);
			if (m.matches()) {
				int min = Integer.parseInt(m.group(1));
				int max = Integer.parseInt(m.group(2));
				if (max < min)
					throw new Exception("Invalid over/under range property " + tag + ". Max value must be greater than min value.");
				else
					return new Pair<Integer, Integer>(min, max);
			} else
				throw new Exception("Invalid over/under range property " + tag + ". Format must be [ (-) min,max]");
		} else
			return null;
	}
	
	public Integer getOUExponentBase() throws Exception {
		if (containsKey(OPT_STRATEGY_OU_EXP_BASE))
			return Integer.parseInt(getProperty(OPT_STRATEGY_OU_EXP_BASE, currentState, true));
		else
			return DEFAULT_EXPONENT_BASE;
	}
	
	public boolean isRedirectOutput() {
		if (!containsKey(REDIRECT_OUTPUT))
			return true;
		else
			return Boolean.parseBoolean(getProperty(REDIRECT_OUTPUT));
	}
	
	public boolean isOverUnder2stepApproach() {
		if (!containsKey(OPT_STRATEGY_OU_2STEP))
			return false;
		else
			return Boolean.parseBoolean(getProperty(OPT_STRATEGY_OU_2STEP));
	}
	
	public boolean isAllowRemoteMonitoring() {
		if (!containsKey(ALLOW_REMOTE_MONITOR))
			return false;
		else
			return Boolean.parseBoolean(getProperty(ALLOW_REMOTE_MONITOR));
	}
	
	public String getMaxMem() throws Exception {
		String mem = getProperty(OPT_MAX_MEM, currentState, true);
		String toret = null;
		if (mem != null) {
			Matcher m = MAX_MEM_PATTERN.matcher(mem);
			if (m.matches()) {
				String val = m.group(1);
				String kmg = m.group(2);
				toret = "-Xmx" + val + kmg;
			} else
				throw new Exception("Illegal property definition [" + OPT_MAX_MEM + "] does not follow pattern: <int>[KMG])");
		}
		return toret;
	}
	
	public static void main(String[] args) {
		String[] tests = new String[] { "RK", "GK", "ROU", "GOU", "ROURS", "GOUGS", "RKRS" };
		
		for (String test : tests) {
			System.out.println("Gene based opt: " + test + "=" + isGeneBased(test));
			System.out.println("Over/under based opt: " + test + "=" + isOverUnder(test));
			System.out.println();
		}
	}
	
}
