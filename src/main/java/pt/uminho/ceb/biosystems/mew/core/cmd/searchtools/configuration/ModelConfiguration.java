package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.ContainerSource;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.utils.SmartProperties;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;

public class ModelConfiguration extends SmartProperties {
	
	private static final long	serialVersionUID		= 1L;
	
	public static final String	MODEL_PREFIX			= "model";
	
	public static final String	MODEL_NAME				= "model.name";
	
	public static final String	MODEL_VERSION			= "model.version";
	
	public static final String	MODEL_SOURCE			= "model.source";
	
	public static final String	MODEL_FILE				= "model.file";
	
	public static final String	MODEL_REACTIONS			= "model.reactions";
	
	public static final String	MODEL_METABOLITES		= "model.metabolites";
	
	public static final String	MODEL_MATRIX			= "model.matrix";
	
	public static final String	MODEL_GENES				= "model.genes";
	
	public static final String	MODEL_CRIT_GENES		= "model.critical.genes";
	
	public static final String	MODEL_CRIT_REACTIONS	= "model.critical.reactions";
	
	public static final String	MODEL_BIOMASS_REACTION	= "model.biomass";
	
	public static final String	MODEL_COFACTORS			= "model.cofactors";
	
	private Container			_container				= null;
	
	private ISteadyStateModel	_model					= null;
	
	public ModelConfiguration(String properties) throws Exception {
		super(properties);
		analyzeModelProperties();
	}
	
	private void analyzeModelProperties() throws Exception {
		
		if (!containsKey(MODEL_NAME)) throw new Exception("Illegal ModelProperties definition. Must define a [" + MODEL_NAME + "] property.");
		
		if (!containsKey(MODEL_VERSION)) throw new Exception("Illegal ModelProperties definition. Must define a [" + MODEL_VERSION + "] property.");
		
		if (!containsKey(MODEL_SOURCE))
			throw new Exception("Illegal ModelProperties definition. Must define a [" + MODEL_SOURCE + "] property.");
		else {
			ContainerSource model_source = ContainerSource.valueOf(getProperty(MODEL_SOURCE));
			
			switch (model_source) {
				case SBML:
					if (!containsKey(MODEL_FILE)) throw new Exception("Illegal ModelProperties definition. Model source [" + MODEL_SOURCE + "] requires a property [" + MODEL_FILE + "].");
					break;
				case METATOOL:
					if (!containsKey(MODEL_FILE)) throw new Exception("Illegal ModelProperties definition. Model source [" + MODEL_SOURCE + "] requires a property [" + MODEL_FILE + "].");
					break;
				case BIO_OPT:
					if (!containsKey(MODEL_FILE)) throw new Exception("Illegal ModelProperties definition. Model source [" + MODEL_SOURCE + "] requires a property [" + MODEL_FILE + "].");
					break;
				case SPARSE_FLAT_FILES: {
					if (!containsKey(MODEL_REACTIONS)) throw new Exception("Illegal ModelProperties definition. Model source [" + MODEL_SOURCE + "] requires a property [" + MODEL_REACTIONS + "].");
					if (!containsKey(MODEL_METABOLITES)) throw new Exception("Illegal ModelProperties definition. Model source [" + MODEL_SOURCE + "] requires a property [" + MODEL_METABOLITES + "].");
					if (!containsKey(MODEL_MATRIX)) throw new Exception("Illegal ModelProperties definition. Model source [" + MODEL_SOURCE + "] requires a property [" + MODEL_MATRIX + "].");
				}
				default:
					break;
			}
		}
		
	}
	
	public Container getContainer() {
//		if (_container == null) {
			ContainerSource containerSource = getModelSource();
			IndexedHashMap<String, Object> map = new IndexedHashMap<String, Object>();
			switch (containerSource) {
				case SBML:
					map.put(MODEL_FILE, getModelFile());
				case SPARSE_FLAT_FILES: {
					map.put(MODEL_REACTIONS, getModelReactionsFile());
					map.put(MODEL_METABOLITES, getModelMetabolitesFile());
					map.put(MODEL_MATRIX, getModelMatrixFile());
					map.put(MODEL_GENES, getModelGenes());
				}
				case BIO_OPT:
					map.put(MODEL_FILE, getModelFile());
				case METATOOL:
					map.put(MODEL_FILE, getModelFile());
				default:
					break;
			}
			_container = containerSource.read(getModelName(), map);
			_container.setBiomassId(getModelBiomass());
//		}
		return _container;
	}
	
	public ISteadyStateModel getModel() throws InvalidSteadyStateModelException {
//		if (_model == null) {
			Container container = getContainer();
			Set<String> toRemove = container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
			container.removeMetabolites(toRemove);
			container.putDrainsInReactantsDirection();
			
			ISteadyStateModel model = ContainerConverter.convert(container);
			model.setBiomassFlux(getModelBiomass());
			_model = model;
//		}
		return _model;
	}
	
	public ContainerSource getModelSource() {
		return ContainerSource.valueOf(getProperty(MODEL_SOURCE, currentState, true));
	}
	
	public String getModelName() {
		return getProperty(MODEL_NAME, currentState, true);
	}
	
	public String getModelVersion() {
		return getProperty(MODEL_VERSION, currentState, true);
	}
	
	public String getModelFile() {
		return getProperty(MODEL_FILE, currentState, true);
	}
	
	public String getModelReactionsFile() {
		return getProperty(MODEL_REACTIONS, currentState, true);
	}
	
	public String getModelMetabolitesFile() {
		return getProperty(MODEL_METABOLITES, currentState, true);
	}
	
	public String getModelMatrixFile() {
		return getProperty(MODEL_MATRIX, currentState, true);
	}
	
	public String getModelGenes() {
		return getProperty(MODEL_GENES, currentState, true);
	}
	
	public String getModelCriticalReactionsFile() {
		return getProperty(MODEL_CRIT_REACTIONS, currentState, true);
	}
	
	public String getModelCriticalGenesFile() {
		return getProperty(MODEL_CRIT_GENES, currentState, true);
	}
	
	public String getModelBiomass() {
		return getProperty(MODEL_BIOMASS_REACTION, currentState, true);
	}
	
	public Set<String> getModelCofactors() throws IOException {
		String tag = getProperty(MODEL_COFACTORS, currentState, true);
		if (tag != null && !tag.isEmpty()) {
			FileReader fr = new FileReader(tag);
			BufferedReader br = new BufferedReader(fr);
			Set<String> toret = new HashSet<String>();
			int iline = 0;
			while (br.ready()) {
				String cofactor = br.readLine().trim();
				if (_container.getMetabolite(cofactor) == null) {
					br.close();
					throw new IOException(tag + ":line " + iline + " [" + cofactor + "] is not a valid co-factor for model [" + getModelName() + "]");
				} else
					toret.add(cofactor);
				iline++;
			}
			br.close();
			return toret;
		}
		return null;
	}
}
