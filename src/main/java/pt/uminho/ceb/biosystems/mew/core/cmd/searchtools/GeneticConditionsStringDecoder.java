package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

import pt.uminho.ceb.biosystems.jecoli.algorithm.components.representation.IRepresentation;
import pt.uminho.ceb.biosystems.jecoli.algorithm.components.solution.decoder.ISolutionDecoder;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.strainoptimizationalgorithms.jecoli.components.decoder.ISteadyStateDecoder;

public class GeneticConditionsStringDecoder implements ISolutionDecoder<IRepresentation, String> {
	
	private static final long		serialVersionUID	= 1L;
	
	protected ISteadyStateDecoder	_decoder			= null;
	
	public GeneticConditionsStringDecoder(ISteadyStateDecoder decoder) {
		_decoder = decoder;
	}
	
	public Object deepCopy() throws Exception {
		return null;
	}
	
	@Override
	public String decode(IRepresentation representation) throws Exception {
		GeneticConditions gc = _decoder.decode(representation);
		String decoded = null;
		if (!gc.isOverUnder())
			decoded = gc.toStringOptions(" ", true);
		else
			decoded = gc.toStringOptions(" ", false);
		
		return decoded;
	}
	
}
