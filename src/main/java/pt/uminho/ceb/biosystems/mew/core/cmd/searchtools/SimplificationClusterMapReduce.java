//package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.regex.Pattern;
//
//import metabolic.cmd.searchtools.ClusterConstants;
//import metabolic.cmd.searchtools.SearchConfiguration;
//import metabolic.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
//import metabolic.optimization.components.OverUnderSolutionSet;
//import metabolic.simplification.SolutionSimplificationMulti;
//import metabolic.simplification.SolutionSimplificationResult;
//import metabolic.simulation.components.GeneChangesList;
//import metabolic.simulation.components.GeneticConditions;
//import metabolic.simulation.components.ReactionChangesList;
//import solvers.lp.CplexParamConfiguration;
//import utilities.datastructures.pair.Pair;
//import utilities.io.Delimiter;
//import utilities.io.MultipleExtensionFileFilter;
//import utilities.io.RegExpFileFilter;
//import analysis.AbstractTask;
//import analysis.AggregationSimplificationTask;
//import analysis.AnalysisControlCenter;
//
//public class SimplificationClusterMapReduce extends AbstractTask {
//	
//	public static final boolean	_debug					= false;
//	public static final String	JAR						= "simplifier.jar";
//	public static final String	CONN					= ClusterConstants.DEFAULT_NAME_CONNECTOR;
//	public static final String	ARG_MERGE				= "--merge";
//	public static final String	ARG_MAP					= "--map";
//	public static final String	ARG_EXEC				= "--exec";
//	public static final String	ARG_REDUCE				= "--reduce";
//	public static final String	ARG_CLEAN				= "--clean";
//	
//	public static int			MAP_CARDINALITY			= 1000;
//	public static final String	OUTPUT_NAME_PART		= "mapReduce#";
//	public static final String	OUTPUT_NAME_PART_SUFFIX	= ".part";
//	
//	public static final String	OUTPUT_NAME				= "aggregated#";
//	public static String		OUTPUT_SUFFIX			= null;
//	
//	protected String			_confFile				= null;
//	
//	public SimplificationClusterMapReduce(String confFile) throws Exception {
//		_confFile = confFile;
//		_configuration = new SearchConfiguration(_confFile);
//		_baseDir = "./";
//		_useSimplified = false;
//		OUTPUT_SUFFIX = "#SIMPLIFIED" + (_configuration.getOptimizationStrategy().isGeneBasedOptimization() ? ".gsss" : ".rsss");
//		
//	}
//	
//	public static void main(String[] args) throws Exception {
//		CplexParamConfiguration.setDoubleParam("EpRHS", 1e-9);
//		CplexParamConfiguration.setDoubleParam("TiLim", 20.0);
//		
//		int numArgs = args.length;
//		
//		SimplificationClusterMapReduce _instance = null;
//		
//		if (numArgs < 2) {
//			return;
//		} else {
//			_instance = new SimplificationClusterMapReduce(args[1]);
//		}
//		
//		if (args[0].equalsIgnoreCase(ARG_MERGE)) {
//			_instance.processHashMerge();
//		}
//		
//		if (args[0].equalsIgnoreCase(ARG_EXEC)) {
//			int part = Integer.parseInt(args[2].trim());
//			int lower = Integer.parseInt(args[3].trim());
//			int higher = Integer.parseInt(args[4].trim());
//			_instance.processExec(part, lower, higher);
//		}
//		
//		if (args[0].equalsIgnoreCase(ARG_MAP)) {
//			if (numArgs == 3) MAP_CARDINALITY = Integer.parseInt(args[2]);
//			_instance.processMap();
//		}
//		
//		if (args[0].equalsIgnoreCase(ARG_REDUCE)) {
//			_instance.processReduce();
//		}
//		
//		if (args[0].equalsIgnoreCase(ARG_CLEAN)) {
//			_instance.processClean();
//		}
//		
//	}
//	
//	private void processClean() {
//		RegExpFileFilter FILTER_AGG = new RegExpFileFilter(Pattern.compile("^aggregated#.part(.*)"));
//		RegExpFileFilter FILTER_MAP = new RegExpFileFilter(Pattern.compile("^mapReduce#.*"));
//		RegExpFileFilter FILTER_SUB = new RegExpFileFilter(Pattern.compile("^submitAllParts#.*"));
//		
//		Map<String, List<Pair<Integer, String>>> aggregatedPaths = computeAggregatedPaths();
//		
//		Map<String, Map<String, List<Integer>>> descendants = computeDirectDescendantsPaths(aggregatedPaths);
//		
//		Set<File> files2del = new HashSet<File>(); 
//		
//		for (String outputpath : descendants.keySet()) {
//			
//			ArrayList<Integer> states = new ArrayList<Integer>();
//			for (String inpath : descendants.get(outputpath).keySet())
//				states.addAll(descendants.get(outputpath).get(inpath));
//			
//			for (String inpath : descendants.get(outputpath).keySet()) {
//							
//				File[] files = new File(inpath).listFiles(FILTER_AGG);				
//				for(File f : files)
//					files2del.add(f);
//				files = new File(inpath).listFiles(FILTER_MAP);				
//				for(File f : files)
//					files2del.add(f);
//				files = new File(inpath).listFiles(FILTER_SUB);				
//				for(File f : files)
//					files2del.add(f);				
//			}
//		}
//		
//		for(File f : files2del){
//			System.out.println("Removing ["+f.getAbsolutePath()+"]");
//			f.delete();
//		}
//	}
//
//	private void processHashMerge() throws Exception {
//		AnalysisControlCenter cc = new AnalysisControlCenter(_configuration, _baseDir);
//		cc.build();
//		
//		/**
//		 * AGGREGATION AND SIMPLIFICATION OF MULTIPLE RUNS
//		 */
//		Map<Integer, Path> states = cc.getResultsPaths();
//		for (Integer state : states.keySet()) {
//			AggregationSimplificationTask aggregationTask = new AggregationSimplificationTask(states.get(state).toString(), _configuration, state, true);
//			aggregationTask.setMergeOnlyHashed(true);
//			aggregationTask.execute();
//		}
//	}
//	
//	/*********
//	 * M A P *
//	 ********* 
//	 * 
//	 * @throws Exception
//	 */
//	private void processMap() throws Exception {
//		String filter = _configuration.getOptimizationStrategy().isGeneBasedOptimization() ? "].gsss" : "].rsss";
//		
//		MultipleExtensionFileFilter INPUT_FILTER = new MultipleExtensionFileFilter(filter);
//		
//		Map<String, List<Pair<Integer, String>>> aggregatedPaths = computeAggregatedPaths();
//		
//		Map<String, Map<String, List<Integer>>> descendants = computeDirectDescendantsPaths(aggregatedPaths);
//		
//		List<String> allScriptPaths = new ArrayList<String>();
//		
//		for (String outputpath : descendants.keySet()) {
//			
//			ArrayList<Integer> states = new ArrayList<Integer>();
//			for (String inpath : descendants.get(outputpath).keySet())
//				states.addAll(descendants.get(outputpath).get(inpath));
//			
//			String consensusName = getConsensusName(states);
//			
//			String output_name_base = outputpath + OUTPUT_NAME_PART + consensusName + OUTPUT_NAME_PART_SUFFIX;
//			
//			int partCounter = 0;
//			int solCounter = 0;
//			for (String inpath : descendants.get(outputpath).keySet()) {
//				
//				// SET TOP LEVEL STATE
//				int state = descendants.get(outputpath).get(inpath).get(0);
//				_configuration.setCurrentState(state);
//				
//				String infile = new File(inpath).listFiles(INPUT_FILTER)[0].getAbsolutePath();
//				OverUnderSolutionSet inputSS = new OverUnderSolutionSet();
//				System.out.println("Reading file [" + infile + "]");
//				inputSS.loadFromCSVFile(infile, false);
//				while (solCounter < inputSS.size()) {
//					partCounter++;
//					int increment = Math.min(MAP_CARDINALITY, inputSS.size() - solCounter);
//					int higher = solCounter + increment;
//					createPartScript(output_name_base, consensusName, partCounter, solCounter, higher);
//					solCounter += increment;
//				}
//				
//				String scriptName = outputpath + ClusterConstants.RUN_ALL_PREFIX + "Parts" + CONN + consensusName + ClusterConstants.SCRIPT_SUFFIX;
//				String qsubflags = ((SearchConfiguration) _configuration).getSearchSubmissionTags();
//				FileWriter writer = new FileWriter(scriptName);
//				BufferedWriter bw = new BufferedWriter(writer);
//				bw.write("#!/bin/bash\n" + "DIR=$(dirname $0)\n" + "cd ${DIR}\n" + "X=1\n" + "while [ $X -le " + partCounter + " ]\n" + "do\n" + "\tqsub " + qsubflags + Delimiter.WHITE_SPACE.toString()
//						+ ClusterConstants.replaceShellEscapeCharacters(OUTPUT_NAME_PART + consensusName + OUTPUT_NAME_PART_SUFFIX) + "$X" + ClusterConstants.SCRIPT_SUFFIX + " " + ClusterConstants.NEW_LINE + "\tX=$((X+1))\n" + "done ");
//				
//				bw.flush();
//				writer.flush();
//				bw.close();
//				writer.close();
//				
//				makeExecutable(scriptName);
//				allScriptPaths.add(scriptName);
//			}
//		}
//		
//		String submitScript = _baseDir + ClusterConstants.RUN_ALL_PREFIX + "Parts" + ClusterConstants.SCRIPT_SUFFIX;
//		FileWriter fw = new FileWriter(submitScript);
//		BufferedWriter bw = new BufferedWriter(fw);
//		
//		for (String path : allScriptPaths) {
//			path = ClusterConstants.replaceShellEscapeCharacters(path);
//			bw.append(path);
//			bw.newLine();
//		}
//		
//		makeExecutable(submitScript);
//		bw.flush();
//		fw.flush();
//		bw.close();
//		fw.close();
//		
//	}
//	
//	/*****************
//	 * E X E C U T E *
//	 ***************** 
//	 * 
//	 * @param part
//	 * @param lower
//	 * @param higher
//	 * @throws Exception
//	 */
//	private void processExec(int part, int lower, int higher) throws Exception {
//		
//		String output = OUTPUT_NAME + OUTPUT_NAME_PART_SUFFIX + part;
//		OverUnderSolutionSet aggregated = new OverUnderSolutionSet();
//		
//		String filter = _configuration.getOptimizationStrategy().isGeneBasedOptimization() ? ".gsss" : ".rsss";
//		MultipleExtensionFileFilter INPUT_FILTER = new MultipleExtensionFileFilter(filter);
//		String infile = new File(".").listFiles(INPUT_FILTER)[0].getAbsolutePath();
//		
//		SolutionSimplificationMulti simplification = new SolutionSimplificationMulti(_configuration.getModel(), _configuration.getObjectiveFunctions(), _configuration.getWTReference(), _configuration.getEnvironmentalConditions(),
//				_configuration.getSimulationSolver());
//		
//		OverUnderSolutionSet ss = new OverUnderSolutionSet();
//		ss.loadFromCSVFile(infile, false);
//		System.out.println("Reading file [" + infile + "]");
//		List<SolutionSimplificationResult> simplified = new ArrayList<SolutionSimplificationResult>();
//		
//		for (int i = lower; i < higher; i++) {
//			System.out.println("Dealing with solution [" + i + " / " + higher + "]");
//			GeneticConditions initialSolution = null;
//			
//			if (_configuration.getOptimizationStrategy().isGeneBasedOptimization()) {
//				GeneChangesList gcl = new GeneChangesList();
//				gcl.setFromListPairsIdValue(ss.getSolution(i));
//				try {
//					initialSolution = new GeneticConditions(gcl, (ISteadyStateGeneReactionModel) _configuration.getModel(), _configuration.getOptimizationStrategy().isOverUnderExpressionOptimization());
//					if (_debug) System.out.println("got initial solution = " + initialSolution.toStringOptions(" ", true));
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//			} else {
//				ReactionChangesList rcl = new ReactionChangesList();
//				rcl.setFromListpairsIdValue(ss.getSolution(i));
//				try {
//					initialSolution = new GeneticConditions(rcl, _configuration.getOptimizationStrategy().isOverUnderExpressionOptimization());
//					if (_debug) System.out.println("got initial solution = " + initialSolution.toStringOptions(" ", true));
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//			}
//			
//			SolutionSimplificationResult simplifiedResult = null;
//			if (initialSolution != null) {
//				
//				if (_configuration.getOptimizationStrategy().isGeneBasedOptimization()) {
//					try {
//						if (_debug) System.out.print("start simplification [genes]...");
//						simplifiedResult = simplification.simplifyGenesSolution(initialSolution);
//						if (_debug) System.out.println("done!");
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				} else {
//					try {
//						if (_debug) System.out.print("start simplification [reactions]...");
//						simplifiedResult = simplification.simplifyReactionsSolution(initialSolution);
//						if (_debug) System.out.println("done!");
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			
//			if (_configuration.getOptimizationStrategy().isGeneBasedOptimization() && simplifiedResult != null && simplifiedResult.getSimulationResult() != null && simplifiedResult.getSimulationResult().getGeneList().size() != 0) {
//				if (_debug) System.out.println("got simplified solution = " + simplifiedResult.getSimulationResult().getGeneticConditions().toStringOptions(" ", true));
//				simplified.add(simplifiedResult);
//			} else if (!_configuration.getOptimizationStrategy().isGeneBasedOptimization() && simplifiedResult != null && simplifiedResult.getSimulationResult() != null && simplifiedResult.getSimulationResult().getReactionList().size() != 0) {
//				if (_debug) System.out.println("got simplified solution = " + simplifiedResult.getSimulationResult().getGeneticConditions().toStringOptions(" ", true));
//				simplified.add(simplifiedResult);
//			} else {
//				if (_debug) System.out.println("No simplified solution available... " + ((simplifiedResult == null) ? "simplified is NULL" : "") + ((simplifiedResult.getSimulationResult() == null) ? "simplified is NULL" : ""));
//			}
//			
//			System.out.print("," + i);
//		}
//		System.out.println("... done!");
//		
//		System.out.print("\tmerging [" + infile + " (" + simplified.size() + " solutions)]...");
//		
//		for (SolutionSimplificationResult res : simplified)
//			if (_configuration.getOptimizationStrategy().isGeneBasedOptimization())
//				aggregated.addOrdered(res.getSimulationResult().getGeneList().getPairsList(), res.getFitnesses());
//			else
//				aggregated.addOrdered(res.getSimulationResult().getReactionList().getPairsList(), res.getFitnesses());
//		
//		System.out.println(" done! - " + aggregated.size() + " solutions.");
//		
//		if (_configuration.getOptimizationStrategy().isOverUnderExpressionOptimization())
//			aggregated.saveToCSVFile(output);
//		else
//			aggregated.saveToCSVFileIgnoreExpressionValues(output);
//		System.out.println(" done!");
//		
//	}
//	
//	/***************
//	 * R E D U C E *
//	 *************** 
//	 * 
//	 * @throws Exception
//	 */
//	private void processReduce() throws Exception {
//		
//		RegExpFileFilter INPUT_FILTER_FITS = new RegExpFileFilter(Pattern.compile("^aggregated#.part(.*)"));
//		
//		Map<String, List<Pair<Integer, String>>> aggregatedPaths = computeAggregatedPaths();
//		
//		Map<String, Map<String, List<Integer>>> descendants = computeDirectDescendantsPaths(aggregatedPaths);
//		
//		for (String outputpath : descendants.keySet()) {
//			
//			ArrayList<Integer> states = new ArrayList<Integer>();
//			for (String inpath : descendants.get(outputpath).keySet())
//				states.addAll(descendants.get(outputpath).get(inpath));
//			
//			String consensusName = getConsensusName(states);
//			
//			String output = outputpath + OUTPUT_NAME + consensusName + OUTPUT_SUFFIX;
//			
//			for (String inpath : descendants.get(outputpath).keySet()) {
//				
//				// SET TOP LEVEL STATE
//				int state = descendants.get(outputpath).get(inpath).get(0);
//				_configuration.setCurrentState(state);
//				
//				int numParts = new File(inpath).listFiles(INPUT_FILTER_FITS).length;
//				
//				OverUnderSolutionSet finalSet = new OverUnderSolutionSet();
//				
//				for (int i = 1; i <= numParts; i++) {
//					String in = inpath + "/aggregated#.part" + i;
//					OverUnderSolutionSet partSet = new OverUnderSolutionSet();
//					partSet.loadFromCSVFile(in, false);
//					finalSet = finalSet.merge(partSet);
//				}
//				
//				if (_configuration.getOptimizationStrategy().isOverUnderExpressionOptimization())
//					finalSet.saveToCSVFile(output);
//				else
//					finalSet.saveToCSVFileIgnoreExpressionValues(output);
//				System.out.println(" done!");
//			}
//		}
//		
//	}
//	
//	@Override
//	public void execute() throws Exception {
//		
//	}
//	
//	public static void makeExecutable(String file) throws IOException {
//		Runtime.getRuntime().exec("chmod a+x " + file);
//	}
//	
//	private String createPartScript(String baseName, String consensusName, int part, int lower, int higher) throws IOException {
//		
//		String scriptName = baseName + part + ClusterConstants.SCRIPT_SUFFIX;
//		String confName = consensusName + ClusterConstants.CONFS_SUFFIX;
//		
//		BufferedWriter bw = new BufferedWriter(new FileWriter(scriptName));
//		
//		String JAVA_HOME = ClusterConstants.JAVA_HOME;
//		String JAVA_EXEC = (JAVA_HOME != null) ? (JAVA_HOME += (JAVA_HOME.endsWith("/") ? "bin/" : "/bin/")) : "";
//		String LDLIB = "-Djava.library.path=" + ClusterConstants.LD_LIBRARY_PATH;
//		String REMOTE = (_configuration.isAllowRemoteMonitoring() ? ClusterConstants.REMOTE_MONITOR_STRING : "");
//		
//		String DIRJAR = new File("").getAbsolutePath() + ClusterConstants.DASH + JAR;
//		String EXEC_COMMAND = JAVA_EXEC + "java" + " " + LDLIB + " " + REMOTE + " " + ClusterConstants.LEGACY_MERGE_SORT + " " + ((SearchConfiguration) _configuration).getSearchMaxMemory() + " -jar " + DIRJAR + " " + ARG_EXEC + " " + confName + " "
//				+ part + " " + lower + " " + higher;
//		
//		bw.write(EXEC_COMMAND);
//		bw.flush();
//		bw.close();
//		
//		return scriptName;
//	}
//	
//	public static boolean allZeros(String[] fits) {
//		for (String f : fits)
//			if (Double.parseDouble(f) > 0) return false;
//		
//		return true;
//	}
//	
//}
