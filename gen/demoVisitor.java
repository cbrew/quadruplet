// Generated from /Users/cbrew/Documents/GitHub/quadruplet/src/main/antlr4/com/cbrew/demo/demo.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link demoParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface demoVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link demoParser#cfgrule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCfgrule(demoParser.CfgruleContext ctx);
	/**
	 * Visit a parse tree produced by {@link demoParser#cfgrhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCfgrhs(demoParser.CfgrhsContext ctx);
	/**
	 * Visit a parse tree produced by {@link demoParser#rhspart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRhspart(demoParser.RhspartContext ctx);
}