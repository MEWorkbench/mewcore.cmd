package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration.OptimizationConfiguration;
import pt.uminho.ceb.biosystems.mew.core.utils.SmartProperties;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.tree.generictree.TreeNode;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;
import pt.uminho.ceb.biosystems.mew.utilities.java.StringUtils;

public class LocalConfigurationGenerator {
	
	public final static String		JAR		= ClusterConstants.JAR_FILE_NAME;
	public final static String		CONN	= ClusterConstants.DEFAULT_NAME_CONNECTOR;
	
	protected OptimizationConfiguration	_configuration;
	protected ClusterRunner			_runner;
	protected int					_numRuns;
	protected String				_baseName;
	protected String				_baseDir;
		
	
	public LocalConfigurationGenerator(
		OptimizationConfiguration configuration,
		int numRuns)
		throws Exception {
		_configuration = configuration;
		_numRuns = numRuns;
		_baseDir = "./";
	}
	
	public void build() throws Exception {
		if (_configuration.getCombinationsString() == null) {
			throw new Exception("Illegal generator configuration. Generator configurations must always define a [" + SmartProperties.SPECIAL_COMBINE + "] entry.");
		} else {
			
			int numStates = _configuration.getNumberOfStates();
			List<String> allScriptPaths = new ArrayList<String>();
			for (int i = 0; i < numStates; i++) {
				List<TreeNode<Pair<String, String>>> path = _configuration.getPossibleStatesPaths().get(i);
				String incrementalPath = _baseDir;
				for (TreeNode<Pair<String, String>> node : path) {
					Pair<String, String> elem = node.getElement();
					if (elem != null) {
						incrementalPath += elem.getB() + ClusterConstants.DASH;
						Path iopath = new File(incrementalPath).toPath();
						boolean exists = Files.exists(iopath, LinkOption.NOFOLLOW_LINKS);
						if (!exists) {
							System.out.println("Directory [" + incrementalPath + "] inexistent. Creating...");
							Files.createDirectory(iopath);
						}
					}
				}
				
				String baseName = getBaseName(path);
				createStateConfiguration(incrementalPath, baseName, i);
				List<String> scriptPaths = createStateScripts(incrementalPath, baseName);
				allScriptPaths.addAll(scriptPaths);
			}
			String submitScript = _baseDir + ClusterConstants.RUN_ALL_PREFIX + ClusterConstants.SCRIPT_SUFFIX;
			FileWriter fw = new FileWriter(submitScript);
			BufferedWriter bw = new BufferedWriter(fw);
			
			for (String path : allScriptPaths) {
				path = ClusterConstants.replaceShellEscapeCharacters(path);
				bw.append(path);
				bw.newLine();
			}
			
			makeExecutable(submitScript);
			bw.flush();
			fw.flush();
			bw.close();
			fw.close();
			
		}
	}
	

	
	private String createStateConfiguration(String path, String baseName, int state) throws Exception {
		String confPath = path + baseName + ".conf";
		FileWriter fw = new FileWriter(confPath);
		BufferedWriter bw = new BufferedWriter(fw);
		_configuration.store(bw, baseName, state);
		bw.close();
		fw.close();
		return confPath;
	}
	
	private String getBaseName(List<TreeNode<Pair<String, String>>> path) {
		List<String> tags = new ArrayList<String>();
		String modelName = _configuration.getModelName();
		tags.add(modelName);
		
		List<String> variableTags = new ArrayList<String>();
		for (TreeNode<Pair<String, String>> node : path) {
			Pair<String, String> elem = node.getElement();
			if (elem != null)
				variableTags.add(elem.getB());
		}
		String vartag = "[" + StringUtils.concat(Delimiter.COMMA.toString(), variableTags) + "]";
		tags.add(vartag);
		
		return StringUtils.concat(CONN, tags);
	}		
	
	private List<String> createStateScripts(String path, String baseName) throws Exception {
		
		List<String> paths = new ArrayList<String>();
		for (int i = 1; i <= _numRuns; i++) {
			
			String scriptName = path + baseName + CONN + "run" + i + ClusterConstants.SCRIPT_SUFFIX;
			String confName = baseName + ClusterConstants.CONFS_SUFFIX;
			
			FileWriter writer = new FileWriter(scriptName);
			BufferedWriter bw = new BufferedWriter(writer);
			
			String JAVA_HOME = ClusterConstants.JAVA_HOME;
			String JAVA_EXEC = (JAVA_HOME != null) ? (JAVA_HOME += (JAVA_HOME.endsWith("/") ? "bin/" : "/bin/")) : "";
			String LDLIB = "-Djava.library.path=" + ClusterConstants.LD_LIBRARY_PATH;
			String REMOTE = (_configuration.isAllowRemoteMonitoring() ? ClusterConstants.REMOTE_MONITOR_STRING: "");
			
			String DIRJAR = new File("").getAbsolutePath() + ClusterConstants.DASH + JAR;
			String EXEC_COMMAND = "nohup" + " " + JAVA_EXEC + "java" + " " + LDLIB + " " + REMOTE + " " + ClusterConstants.LEGACY_MERGE_SORT + " " + _configuration.getMaxMem() + " -jar " + DIRJAR + " "
					+ ClusterConstants.RUN_ARG + " " + confName + " " + i  +" &";
					
			
			bw.write(EXEC_COMMAND);
			bw.flush();
			writer.flush();
			bw.close();
			writer.close();
			
			makeExecutable(scriptName);
			paths.add(scriptName);
		}		
		
		return paths;
	}
	
	/**
	 * @return the _baseDir
	 */
	public String getBaseDir() {
		return _baseDir;
	}
	
	/**
	 * @param _baseDir
	 *            the _baseDir to set
	 */
	public void setBaseDir(String _baseDir) {
		this._baseDir = _baseDir;
	}
	
	public static void makeExecutable(String file) throws IOException {
		Runtime.getRuntime().exec("chmod a+x " + file);
	}	
}
