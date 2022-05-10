// Generated from /Users/arno/GeoGebra/Projects/solver-engine/src/main/antlr/expressions/Expression.g4 by ANTLR 4.10.1
package org.geogebra.solver.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ExpressionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		NATNUM=10, VARIABLE=11, WHITESPACE=12;
	public static final int
		RULE_expr = 0, RULE_sum = 1, RULE_otherTerm = 2, RULE_explicitProduct = 3, 
		RULE_implicitProduct = 4, RULE_firstFactor = 5, RULE_otherFactor = 6, 
		RULE_fraction = 7, RULE_power = 8, RULE_bracket = 9, RULE_atom = 10, RULE_nonNumericAtom = 11, 
		RULE_mixedNumber = 12, RULE_naturalNumber = 13, RULE_variable = 14;
	private static String[] makeRuleNames() {
		return new String[] {
			"expr", "sum", "otherTerm", "explicitProduct", "implicitProduct", "firstFactor", 
			"otherFactor", "fraction", "power", "bracket", "atom", "nonNumericAtom", 
			"mixedNumber", "naturalNumber", "variable"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'+'", "'-'", "'*'", "'['", "'/'", "']'", "'^'", "'('", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "NATNUM", 
			"VARIABLE", "WHITESPACE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Expression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ExpressionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ExprContext extends ParserRuleContext {
		public SumContext sum() {
			return getRuleContext(SumContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30);
			sum();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SumContext extends ParserRuleContext {
		public ExplicitProductContext first;
		public OtherTermContext otherTerm;
		public List<OtherTermContext> rest = new ArrayList<OtherTermContext>();
		public ExplicitProductContext explicitProduct() {
			return getRuleContext(ExplicitProductContext.class,0);
		}
		public List<OtherTermContext> otherTerm() {
			return getRuleContexts(OtherTermContext.class);
		}
		public OtherTermContext otherTerm(int i) {
			return getRuleContext(OtherTermContext.class,i);
		}
		public SumContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sum; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterSum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitSum(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitSum(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SumContext sum() throws RecognitionException {
		SumContext _localctx = new SumContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_sum);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			((SumContext)_localctx).first = explicitProduct();
			setState(36);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0 || _la==T__1) {
				{
				{
				setState(33);
				((SumContext)_localctx).otherTerm = otherTerm();
				((SumContext)_localctx).rest.add(((SumContext)_localctx).otherTerm);
				}
				}
				setState(38);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OtherTermContext extends ParserRuleContext {
		public Token sign;
		public ExplicitProductContext explicitProduct() {
			return getRuleContext(ExplicitProductContext.class,0);
		}
		public OtherTermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_otherTerm; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterOtherTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitOtherTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitOtherTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OtherTermContext otherTerm() throws RecognitionException {
		OtherTermContext _localctx = new OtherTermContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_otherTerm);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(39);
			((OtherTermContext)_localctx).sign = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==T__0 || _la==T__1) ) {
				((OtherTermContext)_localctx).sign = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(40);
			explicitProduct();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExplicitProductContext extends ParserRuleContext {
		public ImplicitProductContext implicitProduct;
		public List<ImplicitProductContext> products = new ArrayList<ImplicitProductContext>();
		public List<ImplicitProductContext> implicitProduct() {
			return getRuleContexts(ImplicitProductContext.class);
		}
		public ImplicitProductContext implicitProduct(int i) {
			return getRuleContext(ImplicitProductContext.class,i);
		}
		public ExplicitProductContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_explicitProduct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterExplicitProduct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitExplicitProduct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitExplicitProduct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExplicitProductContext explicitProduct() throws RecognitionException {
		ExplicitProductContext _localctx = new ExplicitProductContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_explicitProduct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(42);
			((ExplicitProductContext)_localctx).implicitProduct = implicitProduct();
			((ExplicitProductContext)_localctx).products.add(((ExplicitProductContext)_localctx).implicitProduct);
			setState(47);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2) {
				{
				{
				setState(43);
				match(T__2);
				setState(44);
				((ExplicitProductContext)_localctx).implicitProduct = implicitProduct();
				((ExplicitProductContext)_localctx).products.add(((ExplicitProductContext)_localctx).implicitProduct);
				}
				}
				setState(49);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImplicitProductContext extends ParserRuleContext {
		public FirstFactorContext first;
		public OtherFactorContext otherFactor;
		public List<OtherFactorContext> others = new ArrayList<OtherFactorContext>();
		public FirstFactorContext firstFactor() {
			return getRuleContext(FirstFactorContext.class,0);
		}
		public List<OtherFactorContext> otherFactor() {
			return getRuleContexts(OtherFactorContext.class);
		}
		public OtherFactorContext otherFactor(int i) {
			return getRuleContext(OtherFactorContext.class,i);
		}
		public ImplicitProductContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_implicitProduct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterImplicitProduct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitImplicitProduct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitImplicitProduct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImplicitProductContext implicitProduct() throws RecognitionException {
		ImplicitProductContext _localctx = new ImplicitProductContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_implicitProduct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			((ImplicitProductContext)_localctx).first = firstFactor();
			setState(54);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__3) | (1L << T__7) | (1L << VARIABLE))) != 0)) {
				{
				{
				setState(51);
				((ImplicitProductContext)_localctx).otherFactor = otherFactor();
				((ImplicitProductContext)_localctx).others.add(((ImplicitProductContext)_localctx).otherFactor);
				}
				}
				setState(56);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FirstFactorContext extends ParserRuleContext {
		public FirstFactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_firstFactor; }
	 
		public FirstFactorContext() { }
		public void copyFrom(FirstFactorContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class FirstFactorWithSignContext extends FirstFactorContext {
		public Token sign;
		public FirstFactorContext factor;
		public FirstFactorContext firstFactor() {
			return getRuleContext(FirstFactorContext.class,0);
		}
		public FirstFactorWithSignContext(FirstFactorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterFirstFactorWithSign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitFirstFactorWithSign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitFirstFactorWithSign(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FirstFactorWithoutSignContext extends FirstFactorContext {
		public MixedNumberContext mixedNumber() {
			return getRuleContext(MixedNumberContext.class,0);
		}
		public FractionContext fraction() {
			return getRuleContext(FractionContext.class,0);
		}
		public PowerContext power() {
			return getRuleContext(PowerContext.class,0);
		}
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public FirstFactorWithoutSignContext(FirstFactorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterFirstFactorWithoutSign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitFirstFactorWithoutSign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitFirstFactorWithoutSign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FirstFactorContext firstFactor() throws RecognitionException {
		FirstFactorContext _localctx = new FirstFactorContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_firstFactor);
		int _la;
		try {
			setState(65);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
				_localctx = new FirstFactorWithSignContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(57);
				((FirstFactorWithSignContext)_localctx).sign = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==T__0 || _la==T__1) ) {
					((FirstFactorWithSignContext)_localctx).sign = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(58);
				((FirstFactorWithSignContext)_localctx).factor = firstFactor();
				}
				break;
			case T__3:
			case T__7:
			case NATNUM:
			case VARIABLE:
				_localctx = new FirstFactorWithoutSignContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(63);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
				case 1:
					{
					setState(59);
					mixedNumber();
					}
					break;
				case 2:
					{
					setState(60);
					fraction();
					}
					break;
				case 3:
					{
					setState(61);
					power();
					}
					break;
				case 4:
					{
					setState(62);
					atom();
					}
					break;
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OtherFactorContext extends ParserRuleContext {
		public PowerContext power() {
			return getRuleContext(PowerContext.class,0);
		}
		public NonNumericAtomContext nonNumericAtom() {
			return getRuleContext(NonNumericAtomContext.class,0);
		}
		public OtherFactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_otherFactor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterOtherFactor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitOtherFactor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitOtherFactor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OtherFactorContext otherFactor() throws RecognitionException {
		OtherFactorContext _localctx = new OtherFactorContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_otherFactor);
		try {
			setState(69);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__3:
				enterOuterAlt(_localctx, 1);
				{
				setState(67);
				power();
				}
				break;
			case T__7:
			case VARIABLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(68);
				nonNumericAtom();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FractionContext extends ParserRuleContext {
		public ExprContext num;
		public ExprContext den;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public FractionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fraction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterFraction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitFraction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitFraction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FractionContext fraction() throws RecognitionException {
		FractionContext _localctx = new FractionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_fraction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			match(T__3);
			setState(72);
			((FractionContext)_localctx).num = expr();
			setState(73);
			match(T__4);
			setState(74);
			((FractionContext)_localctx).den = expr();
			setState(75);
			match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PowerContext extends ParserRuleContext {
		public AtomContext base;
		public ExprContext exp;
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public PowerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_power; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterPower(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitPower(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitPower(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PowerContext power() throws RecognitionException {
		PowerContext _localctx = new PowerContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_power);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			match(T__3);
			setState(78);
			((PowerContext)_localctx).base = atom();
			setState(79);
			match(T__6);
			setState(80);
			((PowerContext)_localctx).exp = expr();
			setState(81);
			match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BracketContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public BracketContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bracket; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterBracket(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitBracket(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitBracket(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BracketContext bracket() throws RecognitionException {
		BracketContext _localctx = new BracketContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_bracket);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			match(T__7);
			setState(84);
			expr();
			setState(85);
			match(T__8);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AtomContext extends ParserRuleContext {
		public NonNumericAtomContext nonNumericAtom() {
			return getRuleContext(NonNumericAtomContext.class,0);
		}
		public NaturalNumberContext naturalNumber() {
			return getRuleContext(NaturalNumberContext.class,0);
		}
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_atom);
		try {
			setState(89);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__7:
			case VARIABLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(87);
				nonNumericAtom();
				}
				break;
			case NATNUM:
				enterOuterAlt(_localctx, 2);
				{
				setState(88);
				naturalNumber();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonNumericAtomContext extends ParserRuleContext {
		public BracketContext bracket() {
			return getRuleContext(BracketContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public NonNumericAtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonNumericAtom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterNonNumericAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitNonNumericAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitNonNumericAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonNumericAtomContext nonNumericAtom() throws RecognitionException {
		NonNumericAtomContext _localctx = new NonNumericAtomContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_nonNumericAtom);
		try {
			setState(93);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__7:
				enterOuterAlt(_localctx, 1);
				{
				setState(91);
				bracket();
				}
				break;
			case VARIABLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(92);
				variable();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MixedNumberContext extends ParserRuleContext {
		public NaturalNumberContext integer;
		public NaturalNumberContext num;
		public NaturalNumberContext den;
		public List<NaturalNumberContext> naturalNumber() {
			return getRuleContexts(NaturalNumberContext.class);
		}
		public NaturalNumberContext naturalNumber(int i) {
			return getRuleContext(NaturalNumberContext.class,i);
		}
		public MixedNumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mixedNumber; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterMixedNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitMixedNumber(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitMixedNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MixedNumberContext mixedNumber() throws RecognitionException {
		MixedNumberContext _localctx = new MixedNumberContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_mixedNumber);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			match(T__3);
			setState(96);
			((MixedNumberContext)_localctx).integer = naturalNumber();
			setState(97);
			((MixedNumberContext)_localctx).num = naturalNumber();
			setState(98);
			match(T__4);
			setState(99);
			((MixedNumberContext)_localctx).den = naturalNumber();
			setState(100);
			match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NaturalNumberContext extends ParserRuleContext {
		public TerminalNode NATNUM() { return getToken(ExpressionParser.NATNUM, 0); }
		public NaturalNumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_naturalNumber; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterNaturalNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitNaturalNumber(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitNaturalNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NaturalNumberContext naturalNumber() throws RecognitionException {
		NaturalNumberContext _localctx = new NaturalNumberContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_naturalNumber);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			match(NATNUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableContext extends ParserRuleContext {
		public TerminalNode VARIABLE() { return getToken(ExpressionParser.VARIABLE, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			match(VARIABLE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\fk\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0005\u0001#\b\u0001\n\u0001\f\u0001&\t\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0005\u0003.\b\u0003\n\u0003\f\u00031\t\u0003\u0001\u0004\u0001\u0004"+
		"\u0005\u00045\b\u0004\n\u0004\f\u00048\t\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005@\b\u0005"+
		"\u0003\u0005B\b\u0005\u0001\u0006\u0001\u0006\u0003\u0006F\b\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\n\u0001\n\u0003\nZ\b\n\u0001\u000b\u0001\u000b\u0003\u000b^\b"+
		"\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\r"+
		"\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0000\u0000\u000f\u0000\u0002"+
		"\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u0000"+
		"\u0001\u0001\u0000\u0001\u0002e\u0000\u001e\u0001\u0000\u0000\u0000\u0002"+
		" \u0001\u0000\u0000\u0000\u0004\'\u0001\u0000\u0000\u0000\u0006*\u0001"+
		"\u0000\u0000\u0000\b2\u0001\u0000\u0000\u0000\nA\u0001\u0000\u0000\u0000"+
		"\fE\u0001\u0000\u0000\u0000\u000eG\u0001\u0000\u0000\u0000\u0010M\u0001"+
		"\u0000\u0000\u0000\u0012S\u0001\u0000\u0000\u0000\u0014Y\u0001\u0000\u0000"+
		"\u0000\u0016]\u0001\u0000\u0000\u0000\u0018_\u0001\u0000\u0000\u0000\u001a"+
		"f\u0001\u0000\u0000\u0000\u001ch\u0001\u0000\u0000\u0000\u001e\u001f\u0003"+
		"\u0002\u0001\u0000\u001f\u0001\u0001\u0000\u0000\u0000 $\u0003\u0006\u0003"+
		"\u0000!#\u0003\u0004\u0002\u0000\"!\u0001\u0000\u0000\u0000#&\u0001\u0000"+
		"\u0000\u0000$\"\u0001\u0000\u0000\u0000$%\u0001\u0000\u0000\u0000%\u0003"+
		"\u0001\u0000\u0000\u0000&$\u0001\u0000\u0000\u0000\'(\u0007\u0000\u0000"+
		"\u0000()\u0003\u0006\u0003\u0000)\u0005\u0001\u0000\u0000\u0000*/\u0003"+
		"\b\u0004\u0000+,\u0005\u0003\u0000\u0000,.\u0003\b\u0004\u0000-+\u0001"+
		"\u0000\u0000\u0000.1\u0001\u0000\u0000\u0000/-\u0001\u0000\u0000\u0000"+
		"/0\u0001\u0000\u0000\u00000\u0007\u0001\u0000\u0000\u00001/\u0001\u0000"+
		"\u0000\u000026\u0003\n\u0005\u000035\u0003\f\u0006\u000043\u0001\u0000"+
		"\u0000\u000058\u0001\u0000\u0000\u000064\u0001\u0000\u0000\u000067\u0001"+
		"\u0000\u0000\u00007\t\u0001\u0000\u0000\u000086\u0001\u0000\u0000\u0000"+
		"9:\u0007\u0000\u0000\u0000:B\u0003\n\u0005\u0000;@\u0003\u0018\f\u0000"+
		"<@\u0003\u000e\u0007\u0000=@\u0003\u0010\b\u0000>@\u0003\u0014\n\u0000"+
		"?;\u0001\u0000\u0000\u0000?<\u0001\u0000\u0000\u0000?=\u0001\u0000\u0000"+
		"\u0000?>\u0001\u0000\u0000\u0000@B\u0001\u0000\u0000\u0000A9\u0001\u0000"+
		"\u0000\u0000A?\u0001\u0000\u0000\u0000B\u000b\u0001\u0000\u0000\u0000"+
		"CF\u0003\u0010\b\u0000DF\u0003\u0016\u000b\u0000EC\u0001\u0000\u0000\u0000"+
		"ED\u0001\u0000\u0000\u0000F\r\u0001\u0000\u0000\u0000GH\u0005\u0004\u0000"+
		"\u0000HI\u0003\u0000\u0000\u0000IJ\u0005\u0005\u0000\u0000JK\u0003\u0000"+
		"\u0000\u0000KL\u0005\u0006\u0000\u0000L\u000f\u0001\u0000\u0000\u0000"+
		"MN\u0005\u0004\u0000\u0000NO\u0003\u0014\n\u0000OP\u0005\u0007\u0000\u0000"+
		"PQ\u0003\u0000\u0000\u0000QR\u0005\u0006\u0000\u0000R\u0011\u0001\u0000"+
		"\u0000\u0000ST\u0005\b\u0000\u0000TU\u0003\u0000\u0000\u0000UV\u0005\t"+
		"\u0000\u0000V\u0013\u0001\u0000\u0000\u0000WZ\u0003\u0016\u000b\u0000"+
		"XZ\u0003\u001a\r\u0000YW\u0001\u0000\u0000\u0000YX\u0001\u0000\u0000\u0000"+
		"Z\u0015\u0001\u0000\u0000\u0000[^\u0003\u0012\t\u0000\\^\u0003\u001c\u000e"+
		"\u0000][\u0001\u0000\u0000\u0000]\\\u0001\u0000\u0000\u0000^\u0017\u0001"+
		"\u0000\u0000\u0000_`\u0005\u0004\u0000\u0000`a\u0003\u001a\r\u0000ab\u0003"+
		"\u001a\r\u0000bc\u0005\u0005\u0000\u0000cd\u0003\u001a\r\u0000de\u0005"+
		"\u0006\u0000\u0000e\u0019\u0001\u0000\u0000\u0000fg\u0005\n\u0000\u0000"+
		"g\u001b\u0001\u0000\u0000\u0000hi\u0005\u000b\u0000\u0000i\u001d\u0001"+
		"\u0000\u0000\u0000\b$/6?AEY]";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}