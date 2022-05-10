// Generated from /Users/arno/GeoGebra/Projects/solver-engine/src/main/antlr/expressions/Expression.g4 by ANTLR 4.10.1
package org.geogebra.solver.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExpressionParser}.
 */
public interface ExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(ExpressionParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(ExpressionParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#sum}.
	 * @param ctx the parse tree
	 */
	void enterSum(ExpressionParser.SumContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#sum}.
	 * @param ctx the parse tree
	 */
	void exitSum(ExpressionParser.SumContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#firstTerm}.
	 * @param ctx the parse tree
	 */
	void enterFirstTerm(ExpressionParser.FirstTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#firstTerm}.
	 * @param ctx the parse tree
	 */
	void exitFirstTerm(ExpressionParser.FirstTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#otherTerm}.
	 * @param ctx the parse tree
	 */
	void enterOtherTerm(ExpressionParser.OtherTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#otherTerm}.
	 * @param ctx the parse tree
	 */
	void exitOtherTerm(ExpressionParser.OtherTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#explicitProduct}.
	 * @param ctx the parse tree
	 */
	void enterExplicitProduct(ExpressionParser.ExplicitProductContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#explicitProduct}.
	 * @param ctx the parse tree
	 */
	void exitExplicitProduct(ExpressionParser.ExplicitProductContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#implicitProduct}.
	 * @param ctx the parse tree
	 */
	void enterImplicitProduct(ExpressionParser.ImplicitProductContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#implicitProduct}.
	 * @param ctx the parse tree
	 */
	void exitImplicitProduct(ExpressionParser.ImplicitProductContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#firstFactor}.
	 * @param ctx the parse tree
	 */
	void enterFirstFactor(ExpressionParser.FirstFactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#firstFactor}.
	 * @param ctx the parse tree
	 */
	void exitFirstFactor(ExpressionParser.FirstFactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#otherFactor}.
	 * @param ctx the parse tree
	 */
	void enterOtherFactor(ExpressionParser.OtherFactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#otherFactor}.
	 * @param ctx the parse tree
	 */
	void exitOtherFactor(ExpressionParser.OtherFactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#fraction}.
	 * @param ctx the parse tree
	 */
	void enterFraction(ExpressionParser.FractionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#fraction}.
	 * @param ctx the parse tree
	 */
	void exitFraction(ExpressionParser.FractionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#power}.
	 * @param ctx the parse tree
	 */
	void enterPower(ExpressionParser.PowerContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#power}.
	 * @param ctx the parse tree
	 */
	void exitPower(ExpressionParser.PowerContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#bracket}.
	 * @param ctx the parse tree
	 */
	void enterBracket(ExpressionParser.BracketContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#bracket}.
	 * @param ctx the parse tree
	 */
	void exitBracket(ExpressionParser.BracketContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(ExpressionParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(ExpressionParser.AtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#nonNumericAtom}.
	 * @param ctx the parse tree
	 */
	void enterNonNumericAtom(ExpressionParser.NonNumericAtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#nonNumericAtom}.
	 * @param ctx the parse tree
	 */
	void exitNonNumericAtom(ExpressionParser.NonNumericAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#naturalNumber}.
	 * @param ctx the parse tree
	 */
	void enterNaturalNumber(ExpressionParser.NaturalNumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#naturalNumber}.
	 * @param ctx the parse tree
	 */
	void exitNaturalNumber(ExpressionParser.NaturalNumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(ExpressionParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(ExpressionParser.VariableContext ctx);
}