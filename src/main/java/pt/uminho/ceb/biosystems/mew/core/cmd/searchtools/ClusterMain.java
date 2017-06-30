package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

import java.io.File;
import java.io.IOException;

import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration.OptimizationConfiguration;

public class ClusterMain {
	
	public static void main(String... args) throws Exception {
		
		int numArgs = args.length;
		
		if (numArgs == 0) {
			help();
			return;
		}
		
		if (args[0].equalsIgnoreCase(ClusterConstants.HELP_ARG)) {
			help();
			return;
		}
		
		if (args[0].equalsIgnoreCase(ClusterConstants.RUN_ARG)) {
			if (numArgs < 2) {
				help();
				return;
			} else if (numArgs == 2) {
				processRunner(args[1]);
			} else if (numArgs == 3) {
				processRunner(args[1], args[2]);
			} else {
				help();
				return;
			}
		}
		
		if (args[0].equalsIgnoreCase(ClusterConstants.GENERATE_ARG)) {
			if (numArgs < 3) {
				help();
				return;
			} else if (numArgs == 3) {
				processGeneration(args[1], args[2]);
			} else {
				help();
				return;
			}			
		}
		
		if(args[0].equalsIgnoreCase(ClusterConstants.LOCAL_ARG)) {
			if (numArgs < 3) {
				help();
				return;
			} else if (numArgs == 3) {
				processLocal(args[1], args[2]);
			} else {
				help();
				return;
			}
		}
		
		if (args[0].equalsIgnoreCase(ClusterConstants.MISSING_ARG)) {
			if (numArgs < 3) {
				help();
				return;
			} else if (numArgs == 3) {
				processMissing(args[1], args[2]);
			} else {
				help();
				return;
			}			
		}
		
	}
	
	private static void processRunner(String confFile,
			String run) throws Exception {
		OptimizationConfiguration configuration = new OptimizationConfiguration(confFile);
		int runint = Integer.parseInt(run);
		String basename = new File(confFile).getName().replaceAll(ClusterConstants.CONFS_SUFFIX, "");
		
		ClusterRunner runner = new ClusterRunner(configuration, runint, basename);
		try {
			runner.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	private static void processRunner(String confFile) throws Exception {
		OptimizationConfiguration configuration = new OptimizationConfiguration(confFile);
		ClusterRunner runner = new ClusterRunner(configuration);
		try {
			runner.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	private static void processGeneration(String baseConfiguration,
			String numRuns) throws Exception {
		
		SearchConfiguration configuration = new SearchConfiguration(baseConfiguration);
		Integer numberOfRuns = Integer.parseInt(numRuns);
		
		ClusterConfigurationGenerator generator = new ClusterConfigurationGenerator(configuration, numberOfRuns);
		
		try {
			generator.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	private static void processLocal(String baseConfiguration,
			String numRuns) throws Exception {
		
		OptimizationConfiguration configuration = new OptimizationConfiguration(baseConfiguration);
		Integer numberOfRuns = Integer.parseInt(numRuns);
		
		LocalConfigurationGenerator generator = new LocalConfigurationGenerator(configuration, numberOfRuns);
		
		try {
			generator.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	private static void processMissing(String baseConfiguration,
			String numRuns) throws Exception {
		
		SearchConfiguration configuration = new SearchConfiguration(baseConfiguration);
		Integer numberOfRuns = Integer.parseInt(numRuns);
		
		ClusterConfigurationGenerator generator = new ClusterConfigurationGenerator(configuration, numberOfRuns);
		
		try {
			generator.missing();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	public static void help() {
		System.out.println(ClusterConstants.USAGE_HELP);
		System.out.println(ClusterConstants.USAGE_RUN);
		System.out.println(ClusterConstants.USAGE_GENERATE);
	}
	
}
