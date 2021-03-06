package it.unibz.krdb.obda.utils;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.parser.SQL99Lexer;
import it.unibz.krdb.obda.parser.SQL99Parser;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.api.QueryTree;

/**
 * Contains the target query and query tree (parsed source part, sql) of a mapping
 * 
 * This is in a separate class, such that the parsing can be done before metadata extraction,
 * but independently of mapping analysis.
 * 
 * @author Dag Hovland
 *
 */
public class ParsedMapping {

	QueryTree sourceQueryTree;
	OBDAMappingAxiom axiom;
	
	public ParsedMapping(OBDAMappingAxiom axiom, SQLQueryTranslator translator){
		this.axiom = axiom;
		OBDASQLQuery sourceQuery = (OBDASQLQuery) axiom.getSourceQuery();

		// Construct the SQL query tree from the source query
		QueryTree queryTree = translator.constructQueryTreeNoView(sourceQuery.toString());
		this.sourceQueryTree = queryTree;
	}
	
	/**
	 * This returns the querytree constructed from the source query
	 * @return
	 */
	public QueryTree getSourceQueryTree(){
		return this.sourceQueryTree;
	}
	
	
	
	/**
	 * This returns the source query
	 * @return
	 */
	public OBDAQuery getSourceQuery(){
		return axiom.getSourceQuery();
	}
	
	/**
	 * This returns the same target query as in the original axiom / mapping
	 * @return
	 */
	public CQIE getTargetQuery(){
		return (CQIE) axiom.getTargetQuery();
	}
	
	/**
	 * 	This is the same as axiom.getId() on the mapping that was parsed
	 * to create this object
	 * @return
	 */
	public String getId(){
		return axiom.getId();
	}
	
	@Override
	public String toString(){
		return axiom.toString();
	}
	

}
