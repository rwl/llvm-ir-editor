/*
* generated by Xtext
*/
package com.intel.llvm.ireditor.parser.antlr;

import com.google.inject.Inject;

import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import com.intel.llvm.ireditor.services.LLVM_IRGrammarAccess;

public class LLVM_IRParser extends org.eclipse.xtext.parser.antlr.AbstractAntlrParser {
	
	@Inject
	private LLVM_IRGrammarAccess grammarAccess;
	
	@Override
	protected void setInitialHiddenTokens(XtextTokenStream tokenStream) {
		tokenStream.setInitialHiddenTokens("RULE_WS", "RULE_FILECHECK_COMMENT", "RULE_SL_COMMENT");
	}
	
	@Override
	protected com.intel.llvm.ireditor.parser.antlr.internal.InternalLLVM_IRParser createParser(XtextTokenStream stream) {
		return new com.intel.llvm.ireditor.parser.antlr.internal.InternalLLVM_IRParser(stream, getGrammarAccess());
	}
	
	@Override 
	protected String getDefaultRuleName() {
		return "Model";
	}
	
	public LLVM_IRGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}
	
	public void setGrammarAccess(LLVM_IRGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
	
}
