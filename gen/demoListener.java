// Generated from /Users/cbrew/Documents/GitHub/quadruplet/src/main/antlr4/com/cbrew/demo/demo.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link demoParser}.
 */
public interface demoListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link demoParser#cfgrule}.
	 * @param ctx the parse tree
	 */
	void enterCfgrule(demoParser.CfgruleContext ctx);
	/**
	 * Exit a parse tree produced by {@link demoParser#cfgrule}.
	 * @param ctx the parse tree
	 */
	void exitCfgrule(demoParser.CfgruleContext ctx);
	/**
	 * Enter a parse tree produced by {@link demoParser#cfgrhs}.
	 * @param ctx the parse tree
	 */
	void enterCfgrhs(demoParser.CfgrhsContext ctx);
	/**
	 * Exit a parse tree produced by {@link demoParser#cfgrhs}.
	 * @param ctx the parse tree
	 */
	void exitCfgrhs(demoParser.CfgrhsContext ctx);
	/**
	 * Enter a parse tree produced by {@link demoParser#rhspart}.
	 * @param ctx the parse tree
	 */
	void enterRhspart(demoParser.RhspartContext ctx);
	/**
	 * Exit a parse tree produced by {@link demoParser#rhspart}.
	 * @param ctx the parse tree
	 */
	void exitRhspart(demoParser.RhspartContext ctx);
}