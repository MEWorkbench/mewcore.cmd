package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration.OptimizationConfiguration;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;
import cern.colt.Arrays;

public class SearchConfiguration extends OptimizationConfiguration {
	
	private static final long	serialVersionUID	= 1L;
	
	public static final int		DEFAULT_MAX_MEMORY	= 1024;
	
	public static final String	SEARCH_WALLTIME		= "search.walltime";
	
	public static final String	SEARCH_QUEUE		= "search.queue";
	
	public static final String	SEARCH_FORCE_NODE	= "search.forcenode";
	
	public static final String	SEARCH_NUM_NODES	= "search.numnodes";
	
	public static final String	SEARCH_NUM_CORES	= "search.numcores";
	
	public static final String	SEARCH_MAX_MEMORY	= "search.maxmemory";
	
	public static final String	NUMBER_RUNS			= "search.numruns";
	
	public SearchConfiguration(
		String properties)
		throws Exception {
		super(properties);
		analyzeSearchProperties();
	}
	
	private void analyzeSearchProperties() throws Exception {
		if (!containsKey(SEARCH_WALLTIME) && !containsKey(SEARCH_QUEUE))
			throw new Exception("Illegal SearchProperties definition. Must specify EITHER one of [" + SEARCH_QUEUE + "] or [" + SEARCH_WALLTIME + "] properties.");
		
//		if (containsKey(SEARCH_WALLTIME) && containsKey(SEARCH_QUEUE))
//			throw new Exception("Illegal SearchProperties definition. Must specify ONLY one of [" + SEARCH_QUEUE + "] or [" + SEARCH_WALLTIME + "] properties.");
		
		if (containsKey(SEARCH_WALLTIME)) {
			try {
				Walltime.valueOf(getProperty(SEARCH_WALLTIME).toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new Exception("Illegal SearchProperties definition. Property [" + SEARCH_WALLTIME + "] must take one of these values " + Arrays.toString(Walltime.values()));
			}
		}
		
		if (containsKey(SEARCH_QUEUE)) {
			try {
				SearchQueue.valueOf(getProperty(SEARCH_QUEUE).toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new Exception("Illegal SearchProperties definition. Property [" + SEARCH_QUEUE + "] must take one of these values " + Arrays.toString(SearchQueue.values()));
			}
		}
		
		if (containsKey(SEARCH_FORCE_NODE)) {
			try {
				SearchNode.valueOf(getProperty(SEARCH_FORCE_NODE).toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new Exception("Illegal SearchProperties definition. Property [" + SEARCH_FORCE_NODE + "] must take one of these values " + Arrays.toString(SearchNode.values()));
			}
		}
		
		if (containsKey(SEARCH_NUM_CORES)) {
			String numCores = getProperty(SEARCH_NUM_CORES);
			try {
				Integer.parseInt(numCores);
			} catch (NumberFormatException e) {
				throw new Exception("Illegal SearchProperties definion. Property [" + SEARCH_NUM_CORES + "] must be an integer.");
			}
		}
		
		if (containsKey(SEARCH_NUM_NODES)) {
			String numNodes = getProperty(SEARCH_NUM_NODES);
			try {
				Integer.parseInt(numNodes);
			} catch (NumberFormatException e) {
				throw new Exception("Illegal SearchProperties definion. Property [" + SEARCH_NUM_NODES + "] must be an integer.");
			}
		}
		
		if (containsKey(SEARCH_MAX_MEMORY)) {
			String numNodes = getProperty(SEARCH_MAX_MEMORY);
			try {
				Integer.parseInt(numNodes);
			} catch (NumberFormatException e) {
				throw new Exception("Illegal SearchProperties definion. Property [" + SEARCH_MAX_MEMORY + "] must be an integer (megabytes).");
			}
		}
	}
	
	public Walltime getSearchWallTime() {
		String tag = getProperty(SEARCH_WALLTIME,currentState,true);
		if (tag != null)
			return Walltime.valueOf(tag.toUpperCase());
		return null;
	}
	
	public SearchQueue getSearchQueue() {
		String tag = getProperty(SEARCH_QUEUE,currentState,true);
		if (tag != null)
			return SearchQueue.valueOf(tag.toUpperCase());
		return null;
	}
	
	public SearchNode getSearchForcedNode() {
		String tag = getProperty(SEARCH_FORCE_NODE,currentState,true);
		if (tag != null)
			return SearchNode.valueOf(tag.toUpperCase());
		else
			return null;
	}
	
	public String getSearchNumberOfNodes() {
		return getProperty(SEARCH_NUM_NODES,currentState,true);
	}
	
	public String getSearhNumberOfCores() {
		return getProperty(SEARCH_NUM_CORES,currentState,true);
	}
	
	public String getSearchMaxMemory() {
		String ret = "-Xmx";
		
		if (containsKey(SEARCH_MAX_MEMORY))
			ret += getProperty(SEARCH_MAX_MEMORY,currentState,true) + "m";
		else
			ret += DEFAULT_MAX_MEMORY + "m";
		
		return ret;
	}
	
	public String getNumberOfRuns() {
		return getProperty(NUMBER_RUNS,currentState,true);
	}
	
	public String getSearchSubmissionTags() {
		String finalTag = "-d.";
		
		Walltime walltime = getSearchWallTime();
		if (walltime != null)
			finalTag += Delimiter.WHITE_SPACE + walltime.getFlags();
		
		SearchQueue queue = getSearchQueue();
		if (queue != null)
			finalTag += Delimiter.WHITE_SPACE + queue.getFlags();
		
		List<String> nodeTagList = new ArrayList<String>();
		String numNodes = getSearchNumberOfNodes();
		String numCores = getSearhNumberOfCores();
		SearchNode forceNode = getSearchForcedNode();
		
		if (numNodes != null)
			nodeTagList.add("nodes=" + numNodes);
		if (numCores != null)
			nodeTagList.add("ppn=" + numCores);
		if (forceNode != null)
			nodeTagList.add(forceNode.getFlags());
		
		String nodeTag = StringUtils.concat(":", nodeTagList);
		
		if (!nodeTagList.isEmpty())
			finalTag += Delimiter.WHITE_SPACE + "-l" + Delimiter.WHITE_SPACE + nodeTag;
		
		return finalTag;
	}

}
