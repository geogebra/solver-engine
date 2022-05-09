// Generated from /Users/arno/GeoGebra/Projects/solver-engine/src/main/antlr/expressions/Expression.g4 by ANTLR 4.10.1
package org.geogebra.solver.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(ExpressionParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#sum}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSum(ExpressionParser.SumContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#firstTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFirstTerm(ExpressionParser.FirstTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#otherTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherTerm(ExpressionParser.OtherTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#product}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProduct(ExpressionParser.ProductContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#explicitProduct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitProduct(ExpressionParser.ExplicitProductContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#implicitProduct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplicitProduct(ExpressionParser.ImplicitProductContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#nonNumericFactor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonNumericFactor(ExpressionParser.NonNumericFactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#fraction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFraction(ExpressionParser.FractionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactor(ExpressionParser.FactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#power}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPower(ExpressionParser.PowerContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#bracket}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBracket(ExpressionParser.BracketContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(ExpressionParser.AtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#nonNumericAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonNumericAtom(ExpressionParser.NonNumericAtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#naturalNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNaturalNumber(ExpressionParser.NaturalNumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(ExpressionParser.VariableContext ctx);
}