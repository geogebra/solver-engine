var w = Object.defineProperty;
var F = (e, t, r) => t in e ? w(e, t, { enumerable: !0, configurable: !0, writable: !0, value: r }) : e[t] = r;
var g = (e, t, r) => (F(e, typeof t != "symbol" ? t + "" : t, r), r);
const L = "https://solver.geogebra.net/main/api/v1", B = {
  headers: { "Content-Type": "application/json" }
}, M = {
  curriculum: "",
  precision: 3
};
class j {
  constructor(t = L, r = B, s = M) {
    g(this, "baseUrl");
    g(this, "defaultHeaders");
    g(this, "defaultContext");
    this.baseUrl = t, this.defaultHeaders = r, this.defaultContext = s;
  }
  /** Get a transformation for each applicable plan. */
  async selectPlans(t, r = "latex", s = this.defaultContext) {
    return fetch(`${this.baseUrl}/selectPlans`, {
      ...this.defaultHeaders,
      method: "POST",
      body: JSON.stringify({ input: t, format: r, context: s })
    }).then((n) => n.json());
  }
  /* Apply plan to input and return the transformation. */
  async applyPlan(t, r, s = "latex", n = this.defaultContext) {
    return fetch(`${this.baseUrl}/plans/${r}/apply`, {
      ...this.defaultHeaders,
      method: "POST",
      body: JSON.stringify({ input: t, format: s, context: n })
    }).then((o) => o.json());
  }
  /** Get a list of all available plans. */
  async listPlans() {
    return fetch(`${this.baseUrl}/plans`, this.defaultHeaders).then((t) => t.json());
  }
  /** Get solver version info. */
  async versionInfo() {
    return fetch(`${this.baseUrl}/versionInfo`, this.defaultHeaders).then(
      (t) => t.json()
    );
  }
}
const te = new j();
function R(e, t = ".") {
  const [r, ...s] = e;
  let n, o = [];
  typeof r == "string" ? n = r : [n, ...o] = r;
  let a;
  if (s.length === 0 && n !== "FiniteSet") {
    const u = n;
    u.match(/^[+\-0-9]/) ? a = { type: "Number", value: u, path: t } : u === "UNDEFINED" ? a = { type: "UNDEFINED", path: t } : u === "INFINITY" ? a = { type: "INFINITY", path: t } : u === "REALS" ? a = { type: "REALS", path: t } : a = { type: "Variable", value: u, path: t };
  } else
    a = {
      type: n,
      args: s.map((u, c) => R(u, `${t}/${c}`)),
      path: t
    };
  return o.length && (a.decorators = o), a;
}
function C(e) {
  var t, r;
  return e.type === "UNDEFINED" || e.type === "INFINITY" || e.type === "REALS" ? (t = e.decorators) != null && t.length ? [[e.type, ...e.decorators]] : [e.type] : e.type === "Number" || e.type === "Variable" ? (r = e.decorators) != null && r.length ? [[e.value, ...e.decorators]] : [e.value] : [
    e.decorators ? [e.type, ...e.decorators] : e.type,
    ...e.args.map((s) => C(s))
  ];
}
const O = {
  mulSymbol: " \\times ",
  divSymbol: " \\div "
};
function A(e, t, r) {
  return $(
    e,
    null,
    { ...O, ...t },
    r || ((s, n) => n)
  );
}
function $(e, t, r, s) {
  const n = (a, u) => $(a, u, r, s), o = (a) => s(e, Y(a, e.decorators), t);
  switch (e.type) {
    case "Number":
      return o(G(e));
    case "Variable":
      return o(e.value);
    case "Sum":
      return o(
        e.args.map((a, u) => {
          var c;
          return u !== 0 && ((c = a.decorators) != null && c.length || !["Minus", "PlusMinus"].includes(a.type)) ? (
            // non-leading "+" or bracketed expression, need to fill in the "+" manually
            `+${n(a, e)}`
          ) : n(a, e);
        }).join("")
      );
    case "Plus":
      return o(`+${n(e.args[0], e)}`);
    case "Minus":
      return o(`-${n(e.args[0], e)}`);
    case "PlusMinus":
      return o(`\\pm ${n(e.args[0], e)}`);
    case "Product":
      return o(
        e.args.map((a, u) => u === 0 || a.type === "DivideBy" ? n(a, e) : `${r.mulSymbol}${n(a, e)}`).join("")
      );
    case "ImplicitProduct":
      return o(e.args.map((a) => n(a, e)).join(""));
    case "DivideBy":
      return o(`${r.divSymbol}${n(e.args[0], e)}`);
    case "Fraction":
      return o(`\\frac{${n(e.args[0], e)}}{${n(e.args[1], e)}}`);
    case "MixedNumber":
      return o(
        `${n(e.args[0], e)}\\frac{${n(e.args[1], e)}}{${n(e.args[2], e)}}`
      );
    case "Power":
      return o(`{${n(e.args[0], e)}}^{${n(e.args[1], e)}}`);
    case "SquareRoot":
      return o(`\\sqrt{${n(e.args[0], e)}}`);
    case "Root":
      return o(`\\sqrt[{${n(e.args[1], e)}}]{${n(e.args[0], e)}}`);
    case "Equation":
      return r.align ? o(`${n(e.args[0], e)} & = & ${n(e.args[1], e)}`) : o(`${n(e.args[0], e)} = ${n(e.args[1], e)}`);
    case "EquationSystem": {
      const a = { ...r, align: !0 };
      return o(
        `\\left\\{\\begin{array}{rcl}
` + e.args.map((u) => "  " + $(u, e, a, s) + `\\\\
`).join("") + "\\end{array}\\right."
      );
    }
    case "EquationUnion": {
      const a = { ...r, align: !1 };
      return o(
        e.args.map((u) => $(u, e, a, s)).join(", ")
      );
    }
    case "UNDEFINED":
      return o("\\text{undefined}");
    case "INFINITY":
      return o("\\infty");
    case "REALS":
      return o("\\mathbb{R}");
    case "LessThan":
      return o(`${n(e.args[0], e)} < ${n(e.args[1], e)}`);
    case "GreaterThan":
      return o(`${n(e.args[0], e)} > ${n(e.args[1], e)}`);
    case "LessThanEqual":
      return o(`${n(e.args[0], e)} \\leq ${n(e.args[1], e)}`);
    case "GreaterThanEqual":
      return o(`${n(e.args[0], e)} \\geq ${n(e.args[1], e)}`);
    case "Solution":
      return o(`${n(e.args[0], e)} \\in ${n(e.args[1], e)}`);
    case "FiniteSet":
      return o(
        e.args.length === 0 ? "\\emptyset" : `\\left\\{${e.args.map((a) => n(a, e)).join(", ")}\\right\\}`
      );
    case "OpenInterval":
      return `\\left( ${n(e.args[0], e)}, ${n(e.args[1], e)} \\right)`;
    case "ClosedInterval":
      return `\\left[ ${n(e.args[0], e)}, ${n(e.args[1], e)} \\right]`;
    case "OpenClosedInterval":
      return `\\left( ${n(e.args[0], e)}, ${n(e.args[1], e)} \\right]`;
    case "ClosedOpenInterval":
      return `\\left[ ${n(e.args[0], e)}, ${n(e.args[1], e)} \\right)`;
  }
}
function G(e) {
  const [t, r] = e.value.split("[");
  return r !== void 0 ? `${t}\\overline{${r.slice(0, -1)}}` : t;
}
function Y(e, t) {
  return t ? t.reduce((r, s) => s === "RoundBracket" ? `\\left(${r}\\right)` : s === "SquareBracket" ? `\\left[${r}\\right]` : s === "CurlyBracket" ? `\\left\\{${r}\\right\\}` : r, e) : e;
}
function H(e) {
  const t = /^\s*(UNDEFINED|REALS|\\text\{[Uu]ndefined\}|\\mathbb\{R\})/, r = /^\s*((([0-9]*(\.[0-9]*|[0-9]+))([eE][-+]?[0-9]+)?)|∞|Infinity)/, s = new RegExp("^\\s*\\{(\\??[A-Za-z0-9_]*)\\:"), n = new RegExp("^\\s*([a-zA-Z₀₁₂₃₄₅₆₇₈₉⬚])"), o = new RegExp("^\\s*(sqrt|root|Solution)"), a = new RegExp("^\\s*([α-ωΑ-Ω])"), u = new RegExp('^\\s*"([^"]+)"'), c = new RegExp("^\\s*\\\\text{(.*?)}"), y = /^\s*(\[\.|\.\]|\{\.|\.\}|>=|<=|\*\*|[-–+×*/÷=><();,^{}[\]|_±⁰¹²³⁴⁵⁶⁷⁸⁹ⁿ:≥≤√])/, p = new RegExp("^\\s*(\\\\[^A-Za-z0-9\\s]|\\\\[A-Za-z]+)"), x = new RegExp("^(\\s+)"), f = [], D = e.length;
  for (let l = 0; l < D; ) {
    const h = e.substring(l);
    let i;
    if (i = x.exec(h))
      l += i[0].length;
    else if (i = t.exec(h))
      f.push({
        type: "symbol",
        value: i[1].replace(/\\text\{[Uu]ndefined\}/, "UNDEFINED"),
        from: l + (i[0].length - i[1].length),
        to: l + i[0].length
      }), l += i[0].length;
    else if (i = r.exec(h))
      f.push({
        type: "number",
        value: i[1].replace("∞", "Infinity"),
        from: l + (i[0].length - i[1].length),
        to: l + i[0].length
      }), l += i[0].length;
    else if (i = s.exec(h))
      f.push({ type: "operator", value: "{", from: l, to: l + 1 }), f.push({ type: "name", value: i[1], from: l + 1, to: l + i[1].length + 1 }), f.push({
        type: "operator",
        value: ":",
        from: l + i[0].length - 1,
        to: l + i[0].length
      }), l += i[0].length;
    else if (i = o.exec(h))
      f.push({
        type: "operator",
        value: i[1],
        // will be something like "sin"
        from: l + (i[0].length - i[1].length),
        to: l + i[0].length
      }), l += i[0].length;
    else if (i = n.exec(h))
      f.push({
        type: "name",
        value: i[1],
        from: l + (i[0].length - i[1].length),
        to: l + i[0].length
      }), l += i[0].length;
    else if (i = a.exec(h))
      f.push({
        type: "name",
        value: i[1],
        from: l + (i[0].length - i[1].length),
        to: l + i[0].length
      }), l += i[0].length;
    else if (i = u.exec(h))
      f.push({
        type: "name",
        value: i[1],
        from: l + (i[0].length - i[1].length),
        to: l + i[0].length
      }), l += i[0].length;
    else if (i = c.exec(h)) {
      const I = l + i.index + i[0].indexOf(i[1]);
      f.push({ type: "name", value: i[1], from: I, to: I + i[1].length }), l += i[0].length;
    } else if (i = y.exec(h))
      f.push({
        type: "operator",
        value: i[1],
        from: l + (i[0].length - i[1].length),
        to: l + i[0].length
      }), l += i[0].length;
    else if (i = p.exec(h))
      f.push({
        type: "latex",
        value: i[1],
        from: l + (i[0].length - i[1].length),
        to: l + i[0].length
      }), l += i[0].length;
    else
      throw "can't tokenize '" + h + "'";
  }
  return f.filter((l) => l.value !== "\\left" && l.value !== "\\right");
}
function b(e, t) {
  throw { name: "SyntaxError", message: e, object: t };
}
class z {
  constructor(t, r) {
    g(this, "id");
    g(this, "value");
    g(this, "lbp");
    this.id = this.value = t, this.lbp = r;
  }
}
class V extends z {
  constructor() {
    super(...arguments);
    g(this, "nud", () => b(`Undefined nud for ${this.id}`, this));
    g(this, "led", () => b(`Undefined led for ${this.id}`, this));
  }
}
class N {
  constructor(t) {
    g(this, "def", {});
    g(this, "symbolTable");
    this.symbolTable = t;
  }
  find_or_define(t) {
    let r = this.def[t.value];
    return r && typeof r != "function" || (r = Object.create(this.symbolTable["(name)"]), r.lpb = 0, this.def[t.value] = r), r;
  }
}
class Z {
  constructor(t) {
    g(this, "symbolTable", {});
    g(this, "token", null);
    g(this, "tokens", []);
    g(this, "token_nr", 0);
    g(this, "scope", new N(this.symbolTable));
    this.registerSymbol("(end)"), t.forEach((r) => r(this));
  }
  /// Read and return the next token.
  /// If `id` is passed, the method will throw an error if the current token
  /// has not the same (expected) id. If `optional` is passed, the method will
  /// only advance to the next token if the current token has the same id as
  /// the passed one. In case the id differs, the method just returns without
  /// doing anything.
  advance(t, r = !1) {
    var c;
    if (t && ((c = this.token) == null ? void 0 : c.id) !== t) {
      if (r)
        return;
      b("Expected '" + t + "'.", this.token);
    }
    if (this.token_nr >= this.tokens.length) {
      this.token = this.symbolTable["(end)"];
      return;
    }
    const s = this.tokens[this.token_nr];
    this.token_nr += 1;
    const n = s.value, o = s.type;
    let a;
    o === "name" ? a = this.scope.find_or_define(s) : o === "operator" ? (a = this.symbolTable[n], a || b(`Unknown operator "${n}".`, s)) : o === "latex" ? (a = this.symbolTable[n], a || b(`Unknown latex command "${n}".`, s)) : o === "number" ? a = this.symbolTable["(number)"] : o === "symbol" ? a = this.symbolTable[n] ?? this.symbolTable["(symbol)"] : b("Unexpected token.", s), a || b("Couldn't find token.", s);
    const u = Object.create(a);
    return u.from = s.from, u.to = s.to, u.value = n, u.type = o, this.token = u, this.token;
  }
  /// If a token is passed as second parameter, no initial advance() is called.
  expression(t, r = null) {
    if (!this.token)
      throw new Error("Parser error: Missing token.");
    let s = r || this.token;
    r || this.advance();
    let n = s.nud();
    for (; t < this.token.lbp; )
      s = this.token, this.advance(), n = s.led(n);
    return n;
  }
  /// Setup the symbol table (usually before the actual parsing).
  registerSymbol(t, r = 0) {
    let s = this.symbolTable[t];
    return s ? r > s.lbp && (s.lbp = r) : (s = new V(t, r), this.symbolTable[t] = s), s;
  }
  parse(t) {
    if (this.tokens = H(t), this.tokens.length == 0)
      throw "Error: No tokens to parse.";
    this.scope = new N(this.symbolTable), this.token_nr = 0, this.advance();
    const r = this.expression(0);
    return this.advance("(end)"), r;
  }
}
const P = 5, J = 6, S = 10, v = 20, d = 25, E = 30, k = 40, W = {
  registerSum(e) {
    const t = e.registerSymbol("+", S);
    t.nud = () => ({
      type: "Plus",
      args: [e.expression(E)]
    }), t.led = m(e, "Sum");
    for (const r of ["-", "–"]) {
      const s = e.registerSymbol(r, S);
      s.nud = () => ({
        type: "Minus",
        args: [e.expression(E)]
      }), s.led = m(e, "Sum", "Minus");
    }
    for (const r of ["\\pm", "±"]) {
      const s = e.registerSymbol(r, S);
      s.nud = () => ({
        type: "PlusMinus",
        args: [e.expression(E)]
      }), s.led = m(e, "Sum", "PlusMinus");
    }
  },
  registerTimes(e) {
    for (const t of ["*", "\\cdot", "\\times", "×"])
      e.registerSymbol(t, v).led = m(e, "Product");
    for (const t of [":", "\\div", "/"])
      e.registerSymbol(t, v).led = m(
        e,
        "Product",
        "DivideBy"
      );
  },
  registerNumber(e) {
    const t = e.registerSymbol("(number)", d);
    t.nud = function() {
      return { type: "Number", value: this.value };
    }, t.led = m(e, "ImplicitProduct");
    const r = e.registerSymbol("\\overline", 100);
    r.led = function(s) {
      const n = e.expression(100);
      return {
        type: "Number",
        value: `${s.value}[${n.value}]`
      };
    };
  },
  registerVariable(e) {
    const t = e.registerSymbol("(name)", d);
    t.nud = function() {
      return { type: "Variable", value: this.value };
    }, t.led = m(e, "ImplicitProduct");
  },
  registerSymbols(e) {
    e.registerSymbol("(symbol)"), e.registerSymbol("UNDEFINED", d).nud = () => ({
      type: "UNDEFINED"
    }), e.registerSymbol("\\infty", d).nud = () => ({
      type: "INFINITY"
    }), e.registerSymbol("\\mathbb{R}", d).nud = () => ({
      type: "REALS"
    }), e.registerSymbol("\\emptyset", d).nud = () => ({
      type: "FiniteSet",
      args: []
    });
  },
  registerSolution(e) {
    const t = e.registerSymbol("\\in", J);
    t.led = (r) => {
      const s = e.expression(0);
      return { type: "Solution", args: [r, s] };
    };
  },
  registerEquation(e) {
    for (const [t, r] of [
      ["=", "Equation"],
      ["<", "LessThan"],
      ["≤", "LessThanEqual"],
      ["\\leq", "LessThanEqual"],
      [">", "GreaterThan"],
      ["≥", "GreaterThanEqual"],
      ["\\geq", "GreaterThanEqual"]
    ])
      e.registerSymbol(t, P).led = (s) => ({ type: r, args: [s, e.expression(P)] });
  },
  registerBrackets(e) {
    function t(r, s, n, o = !1) {
      const a = e.registerSymbol(r, d);
      e.registerSymbol(s), a.nud = () => {
        if (o && e.advance(s, !0))
          return e.expression(0);
        const u = e.expression(0);
        return e.advance(s), n && (u.decorators ? u.decorators.push(n) : u.decorators = [n]), u;
      }, a.led = m(e, "ImplicitProduct");
    }
    t("\\{", "\\}", "CurlyBracket"), t("[", "]", "SquareBracket"), t("(", ")", "RoundBracket"), t("{", "}", void 0, !0);
  },
  // registerSets(parser: Parser<ExprTree>) {
  //   const set = parser.registerSymbol('{', BP_EQUALS + 1);
  //   parser.registerSymbol(',');
  //   parser.registerSymbol('}');
  //   set.nud = () => {
  //     // parse the elements in the set
  //     if (parser.advance('}', true)) return { type: 'FiniteSet', args: [] };
  //     const args: ExprTree[] = [];
  //     for (;;) {
  //       const element = parser.expression(0);
  //       args.push(element);
  //       if (parser.advance('}', true)) return { type: 'FiniteSet', args };
  //       parser.advance(',');
  //     }
  //   };
  // },
  registerFraction(e) {
    const t = () => {
      const n = e.expression(100), o = e.expression(100);
      return { type: "Fraction", args: [n, o] };
    }, r = (n) => {
      const o = e.expression(100), a = e.expression(100);
      return n.type === "Number" && Number.isInteger(+n.value) && o.type === "Number" && Number.isInteger(+o.value) && a.type === "Number" && Number.isInteger(+a.value) ? { type: "MixedNumber", args: [n, o, a] } : {
        type: "ImplicitProduct",
        args: [n, { type: "Fraction", args: [o, a] }]
      };
    };
    ["\\frac", "\\dfrac", "\\tfrac"].forEach((n) => {
      const o = e.registerSymbol(n, v);
      o.nud = t, o.led = r;
    });
  },
  registerExponent(e) {
    const t = e.registerSymbol("^", k);
    t.led = function(r) {
      const s = e.expression(k - 1);
      return { type: "Power", args: [r, s] };
    };
  },
  registerRoots(e) {
    const t = e.registerSymbol("\\sqrt", d);
    t.nud = function() {
      if (e.advance("[", !0)) {
        const r = e.expression(0);
        return e.advance("]"), {
          type: "Root",
          args: [e.expression(1 / 0), r]
        };
      } else
        return { type: "SquareRoot", args: [e.expression(1 / 0)] };
    }, t.led = t.led = m(e, "ImplicitProduct");
  }
};
function m(e, t, r, s) {
  return function(n) {
    var u, c;
    const o = t === "Sum" ? S : t === "ImplicitProduct" ? d : v;
    let a = t === "ImplicitProduct" ? e.expression(o, this) : e.expression(o);
    if (r && (a = { type: r, args: [a] }), s) {
      const y = s(n, a);
      if (y)
        return y;
    }
    return t === "Sum" && !r && !((u = a.decorators) != null && u.length) && (a.type === "Minus" || a.type === "PlusMinus") && (a.decorators = ["MissingBracket"]), n.type === t && !((c = n.decorators) != null && c.length) ? { type: t, args: [...n.args || [], a] } : { type: t, args: [n, a] };
  };
}
function U(e, t = ".") {
  return "args" in e ? {
    ...e,
    path: t,
    args: e.args.map((r, s) => U(r, `${t}/${s}`))
  } : { ...e, path: t };
}
const Q = new Z(Object.values(W));
function X(e) {
  return U(Q.parse(e));
}
function q(e) {
  const t = (s) => q(s), r = (s) => K(s, e.decorators);
  switch (e.type) {
    case "Number":
      return r(e.value);
    case "Variable":
      return r(e.value);
    case "Sum":
      return r(
        e.args.map(
          (s, n) => {
            var o, a;
            return s.type === "Minus" && !((o = s.decorators) != null && o.length) ? (
              // binary minus
              `-${t(s.args[0])}`
            ) : s.type === "PlusMinus" && !((a = s.decorators) != null && a.length) ? (
              // binary ±
              `+/-${t(s.args[0])}`
            ) : (
              // binary plus
              n === 0 ? t(s) : `+${t(s)}`
            );
          }
        ).join("")
      );
    case "Plus":
      return r(`+${t(e.args[0])}`);
    case "Minus":
      return r(`-${t(e.args[0])}`);
    case "PlusMinus":
      return r(`+/-${t(e.args[0])}`);
    case "Product":
      return r(
        e.args.map((s, n) => n == 0 || s.type === "DivideBy" ? t(s) : `*${t(s)}`).join("")
      );
    case "ImplicitProduct":
      return r(e.args.map((s) => t(s)).join(" "));
    case "DivideBy":
      return `:${t(e.args[0])}`;
    case "Fraction":
      return r(`[${t(e.args[0])} / ${t(e.args[1])}]`);
    case "MixedNumber":
      return r(`[${t(e.args[0])} ${t(e.args[1])} / ${t(e.args[2])}]`);
    case "Power":
      return r(`[${t(e.args[0])} ^ ${t(e.args[1])}]`);
    case "SquareRoot":
      return r(`sqrt[${t(e.args[0])}]`);
    case "Root":
      return r(`root[${t(e.args[0])}, ${t(e.args[1])}]`);
    case "Equation":
      return r(`${t(e.args[0])} = ${t(e.args[1])}`);
    case "EquationSystem":
      return r(e.args.map((s) => t(s)).join(", "));
    case "EquationUnion":
      return r(e.args.map((s) => t(s)).join(" OR "));
    case "UNDEFINED":
      return r("UNDEFINED");
    case "INFINITY":
      return r("INFINITY");
    case "REALS":
      return r("REALS");
    case "LessThan":
      return r(`${t(e.args[0])} < ${t(e.args[1])}`);
    case "GreaterThan":
      return r(`${t(e.args[0])} > ${t(e.args[1])}`);
    case "LessThanEqual":
      return r(`${t(e.args[0])} <= ${t(e.args[1])}`);
    case "GreaterThanEqual":
      return r(`${t(e.args[0])} >= ${t(e.args[1])}`);
    case "Solution":
      return r(`Solution[${t(e.args[0])}, ${t(e.args[1])}]`);
    case "FiniteSet":
      return r(`{${e.args.map((s) => t(s)).join(", ")}}`);
    case "OpenInterval":
      return `(${t(e.args[0])}, ${t(e.args[1])})`;
    case "ClosedInterval":
      return `[${t(e.args[0])}, ${t(e.args[1])}]`;
    case "OpenClosedInterval":
      return `(${t(e.args[0])}, ${t(e.args[1])}]`;
    case "ClosedOpenInterval":
      return `[${t(e.args[0])}, ${t(e.args[1])})`;
  }
}
function K(e, t) {
  return t ? t.reduce((r, s) => s === "RoundBracket" ? `(${r})` : s === "SquareBracket" ? `[.${r}.]` : s === "CurlyBracket" ? `{.${r}.}` : r, e) : e;
}
function re(e) {
  return (t, r, s = null) => {
    const n = e[t.path];
    if (!n)
      return r;
    let o = "";
    return ((s == null ? void 0 : s.type) === "Sum" || (s == null ? void 0 : s.type) === "Product") && (s == null ? void 0 : s.args[0]) !== t && (o = "{}"), `{\\color{${n}}${o}${r}}`;
  };
}
function se(e, t, r = !0) {
  const s = {}, n = {};
  r && (e = e.filter((o) => o.type !== "Shift"));
  for (const [o, a] of e.entries()) {
    const u = t[o % t.length];
    for (const c of a.fromPaths)
      s[c] = u;
    for (const c of a.toPaths)
      n[c] = u;
  }
  return [s, n];
}
function ne(e, t) {
  return A(R(e), t);
}
function oe(e) {
  return q(X(e));
}
function _(e) {
  return e.length === 0 ? "." : `./${e.join("/")}`;
}
function ae(e) {
  return e === "." ? [] : (e.startsWith("./") && (e = e.substring(2)), e.split("/").map((t) => +t));
}
function ie(e, t, r, s = !0) {
  function n(o, a, u = 0) {
    var y;
    if (r.length === u)
      return s ? {
        ...T(a, _(r)),
        ...(y = o.decorators) != null && y.length ? { decorators: o.decorators.slice() } : null
      } : T(a, _(r));
    const c = r[u];
    if (!("args" in o) || c >= o.args.length)
      throw new Error("Invalid path");
    return {
      ...o,
      args: o.args.map((p, x) => x === c ? n(p, a, u + 1) : p)
    };
  }
  return n(e, t);
}
function T(e, t = "") {
  return {
    ...e,
    path: t + e.path.slice(1),
    ..."args" in e ? { args: e.args.map((r) => T(r, t)) } : null
  };
}
export {
  te as api,
  _ as arrayToPath,
  re as coloringTransformer,
  se as createColorMaps,
  ne as jsonToLatex,
  R as jsonToTree,
  oe as latexToSolver,
  X as latexToTree,
  ae as pathToArray,
  ie as substituteTree,
  C as treeToJson,
  A as treeToLatex,
  q as treeToSolver
};
