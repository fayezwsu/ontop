package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.net.URI;

import inf.unibz.it.dl.domain.NamedConcept;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.VariableTerm;
import inf.unibz.it.ucq.parser.exception.QueryParseException;


/**
 * A filter that receives a String str during construction and returns
 * a match if the input mapping contains str in the id field.
 * 
 * @author mariano
 *
 */
public class MappingIDLikeTextFilter implements TreeModelFilter<OBDAMappingAxiom> {

	String str = "";
	
	public MappingIDLikeTextFilter(String str) {
		this.str = str;
	}
	
	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter#match(java.lang.Object)
	 */
	@Override
	public boolean match(OBDAMappingAxiom object) {
		if (object.getId().indexOf(str) != -1) {
			return true;
		}
		return false;
	}
}
