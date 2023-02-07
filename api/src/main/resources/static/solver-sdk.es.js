var F = Object.defineProperty;
var L = (e, t, s) => t in e ? F(e, t, { enumerable: !0, configurable: !0, writable: !0, value: s }) : e[t] = s;
var g = (e, t, s) => (L(e, typeof t != "symbol" ? t + "" : t, s), s);
const B = "https://solver.geogebra.net/main/api/v1.0-alpha0", M = {
  headers: { "Content-Type": "application/json" }
}, C = {
  curriculum: "",
  precision: 3
};
class j {
  constructor(t = B, s = M, r = C) {
    g(this, "baseUrl");
    g(this, "defaultHeaders");
    g(this, "defaultContext");
    this.baseUrl = t, this.defaultHeaders = s, this.defaultContext = r;
  }
  /** Get a transformation for each applicable plan. */
  async selectPlans(t, s = "latex", r = this.defaultContext) {
    return fetch(`${this.baseUrl}/selectPlans`, {
      ...this.defaultHeaders,
      method: "POST",
      body: JSON.stringify({ input: t, format: s, context: r })
    }).then((n) => n.json());
  }
  /* Apply plan to input and return the transformation. */
  async applyPlan(t, s, r = "latex", n = this.defaultContext) {
    return fetch(`${this.baseUrl}/plans/${s}/apply`, {
      ...this.defaultHeaders,
      method: "POST",
      body: JSON.stringify({ input: t, format: r, context: n })
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
  const [s, ...r] = e;
  let n, o = [];
  typeof s == "string" ? n = s : [n, ...o] = s;
  let i;
  if (r.length === 0 && n !== "FiniteSet") {
    const u = n;
    u.match(/^[+\-0-9]/) ? i = { type: "Number", value: u, path: t } : u === "UNDEFINED" ? i = { type: "UNDEFINED", path: t } : u === "INFINITY" ? i = { type: "INFINITY", path: t } : u === "REALS" ? i = { type: "REALS", path: t } : i = { type: "Variable", value: u, path: t };
  } else
    i = {
      type: n,
      args: r.map((u, c) => R(u, `${t}/${c}`)),
      path: t
    };
  return o.length && (i.decorators = o), i;
}
function O(e) {
  var t, s;
  return e.type === "UNDEFINED" || e.type === "INFINITY" || e.type === "REALS" ? (t = e.decorators) != null && t.length ? [[e.type, ...e.decorators]] : [e.type] : e.type === "Number" || e.type === "Variable" ? (s = e.decorators) != null && s.length ? [[e.value, ...e.decorators]] : [e.value] : [
    e.decorators ? [e.type, ...e.decorators] : e.type,
    ...e.args.map((r) => O(r))
  ];
}
const A = {
  mulSymbol: " \\times ",
  divSymbol: " \\div "
};
function G(e, t, s) {
  return D(
    e,
    { ...A, ...t },
    s || ((r, n) => n)
  );
}
function D(e, t, s) {
  const r = (o) => D(o, t, s), n = (o) => s(e, x(o, e.decorators));
  switch (e.type) {
    case "Number":
      return s(e, x(Y(e), e.decorators));
    case "Variable":
      return s(e, x(e.value, e.decorators));
    case "Sum":
      return n(
        e.args.map(
          (o, i) => {
            var u, c;
            return o.type === "Minus" && !((u = o.decorators) != null && u.length) ? (
              // binary minus
              `-${r(o.args[0])}`
            ) : o.type === "PlusMinus" && !((c = o.decorators) != null && c.length) ? (
              // binary ±
              `\\pm ${r(o.args[0])}`
            ) : (
              // binary plus
              i === 0 ? r(o) : `+${r(o)}`
            );
          }
        ).join("")
      );
    case "Plus":
      return n(`+${r(e.args[0])}`);
    case "Minus":
      return n(`-${r(e.args[0])}`);
    case "PlusMinus":
      return n(`\\pm ${r(e.args[0])}`);
    case "Product":
      return n(
        e.args.map(
          (o, i) => i == 0 || o.type === "DivideBy" ? r(o) : `${t.mulSymbol}${r(o)}`
        ).join("")
      );
    case "ImplicitProduct":
      return n(e.args.map((o) => r(o)).join(""));
    case "DivideBy":
      return `${t.divSymbol}${r(e.args[0])}`;
    case "Fraction":
      return n(`\\frac{${r(e.args[0])}}{${r(e.args[1])}}`);
    case "MixedNumber":
      return n(`${r(e.args[0])}\\frac{${r(e.args[1])}}{${r(e.args[2])}}`);
    case "Power":
      return n(`{${r(e.args[0])}}^{${r(e.args[1])}}`);
    case "SquareRoot":
      return n(`\\sqrt{${r(e.args[0])}}`);
    case "Root":
      return n(`\\sqrt[${r(e.args[1])}]{${r(e.args[0])}}`);
    case "Equation":
      return n(`${r(e.args[0])} = ${r(e.args[1])}`);
    case "UNDEFINED":
      return n("\\text{undefined}");
    case "INFINITY":
      return n("\\infty");
    case "REALS":
      return n("\\mathbb{R}");
    case "LessThan":
      return n(`${r(e.args[0])} < ${r(e.args[1])}`);
    case "GreaterThan":
      return n(`${r(e.args[0])} > ${r(e.args[1])}`);
    case "LessThanEqual":
      return n(`${r(e.args[0])} \\leq ${r(e.args[1])}`);
    case "GreaterThanEqual":
      return n(`${r(e.args[0])} \\geq ${r(e.args[1])}`);
    case "Solution":
      return n(`${r(e.args[0])} \\in ${r(e.args[1])}`);
    case "FiniteSet":
      return n(
        e.args.length === 0 ? "\\emptyset" : `\\left\\{${e.args.map((o) => r(o)).join(", ")}\\right\\}`
      );
    case "OpenInterval":
      return `\\left( ${r(e.args[0])}, ${r(e.args[1])} \\right)`;
    case "ClosedInterval":
      return `\\left[ ${r(e.args[0])}, ${r(e.args[1])} \\right]`;
    case "OpenClosedInterval":
      return `\\left( ${r(e.args[0])}, ${r(e.args[1])} \\right]`;
    case "ClosedOpenInterval":
      return `\\left[ ${r(e.args[0])}, ${r(e.args[1])} \\right)`;
  }
}
function Y(e) {
  const [t, s] = e.value.split("[");
  return s !== void 0 ? `${t}\\overline{${s.slice(0, -1)}}` : t;
}
function x(e, t) {
  return t ? t.reduce((s, r) => r === "RoundBracket" ? `\\left(${s}\\right)` : r === "SquareBracket" ? `\\left[${s}\\right]` : r === "CurlyBracket" ? `\\left\\{${s}\\right\\}` : s, e) : e;
}
function H(e) {
  const t = /^\s*(UNDEFINED|REALS|\\text\{[Uu]ndefined\}|\\mathbb\{R\})/, s = /^\s*((([0-9]*(\.[0-9]*|[0-9]+))([eE][-+]?[0-9]+)?)|∞|Infinity)/, r = new RegExp("^\\s*\\{(\\??[A-Za-z0-9_]*)\\:"), n = new RegExp("^\\s*([a-zA-Z₀₁₂₃₄₅₆₇₈₉⬚])"), o = new RegExp("^\\s*(sqrt|root|Solution)"), i = new RegExp("^\\s*([α-ωΑ-Ω])"), u = new RegExp('^\\s*"([^"]+)"'), c = new RegExp("^\\s*\\\\text{(.*?)}"), y = /^\s*(\[\.|\.\]|\{\.|\.\}|>=|<=|\*\*|[-–+×*/÷=><();,^{}[\]|_±⁰¹²³⁴⁵⁶⁷⁸⁹ⁿ:≥≤√])/, b = new RegExp("^\\s*(\\\\[^A-Za-z0-9\\s]|\\\\[A-Za-z]+)"), v = new RegExp("^(\\s+)"), f = [], q = e.length;
  for (let l = 0; l < q; ) {
    const h = e.substring(l);
    let a;
    if (a = t.exec(h))
      f.push({
        type: "symbol",
        value: a[1].replace(/\\text\{[Uu]ndefined\}/, "UNDEFINED"),
        from: l + (a[0].length - a[1].length),
        to: l + a[0].length
      }), l += a[0].length;
    else if (a = s.exec(h))
      f.push({
        type: "number",
        value: a[1].replace("∞", "Infinity"),
        from: l + (a[0].length - a[1].length),
        to: l + a[0].length
      }), l += a[0].length;
    else if (a = r.exec(h))
      f.push({ type: "operator", value: "{", from: l, to: l + 1 }), f.push({ type: "name", value: a[1], from: l + 1, to: l + a[1].length + 1 }), f.push({
        type: "operator",
        value: ":",
        from: l + a[0].length - 1,
        to: l + a[0].length
      }), l += a[0].length;
    else if (a = o.exec(h))
      f.push({
        type: "operator",
        value: a[1],
        // will be something like "sin"
        from: l + (a[0].length - a[1].length),
        to: l + a[0].length
      }), l += a[0].length;
    else if (a = n.exec(h))
      f.push({
        type: "name",
        value: a[1],
        from: l + (a[0].length - a[1].length),
        to: l + a[0].length
      }), l += a[0].length;
    else if (a = i.exec(h))
      f.push({
        type: "name",
        value: a[1],
        from: l + (a[0].length - a[1].length),
        to: l + a[0].length
      }), l += a[0].length;
    else if (a = u.exec(h))
      f.push({
        type: "name",
        value: a[1],
        from: l + (a[0].length - a[1].length),
        to: l + a[0].length
      }), l += a[0].length;
    else if (a = c.exec(h)) {
      const I = l + a.index + a[0].indexOf(a[1]);
      f.push({ type: "name", value: a[1], from: I, to: I + a[1].length }), l += a[0].length;
    } else if (a = y.exec(h))
      f.push({
        type: "operator",
        value: a[1],
        from: l + (a[0].length - a[1].length),
        to: l + a[0].length
      }), l += a[0].length;
    else if (a = b.exec(h))
      f.push({
        type: "latex",
        value: a[1],
        from: l + (a[0].length - a[1].length),
        to: l + a[0].length
      }), l += a[0].length;
    else if (a = v.exec(h))
      l += a[0].length;
    else
      throw "can't tokenize '" + h + "'";
  }
  return f.filter((l) => l.value !== "\\left" && l.value !== "\\right");
}
function p(e, t) {
  throw { name: "SyntaxError", message: e, object: t };
}
class z {
  constructor(t, s) {
    g(this, "id");
    g(this, "value");
    g(this, "lbp");
    this.id = this.value = t, this.lbp = s;
  }
}
class V extends z {
  constructor() {
    super(...arguments);
    g(this, "nud", () => p(`Undefined nud for ${this.id}`, this));
    g(this, "led", () => p(`Undefined led for ${this.id}`, this));
  }
}
class N {
  constructor(t) {
    g(this, "def", {});
    g(this, "symbolTable");
    this.symbolTable = t;
  }
  find_or_define(t) {
    let s = this.def[t.value];
    return s && typeof s != "function" || (s = Object.create(this.symbolTable["(name)"]), s.lpb = 0, this.def[t.value] = s), s;
  }
}
class Z {
  constructor(t) {
    g(this, "symbolTable", {});
    g(this, "token", null);
    g(this, "tokens", []);
    g(this, "token_nr", 0);
    g(this, "scope", new N(this.symbolTable));
    this.registerSymbol("(end)"), t.forEach((s) => s(this));
  }
  /// Read and return the next token.
  /// If `id` is passed, the method will throw an error if the current token
  /// has not the same (expected) id. If `optional` is passed, the method will
  /// only advance to the next token if the current token has the same id as
  /// the passed one. In case the id differs, the method just returns without
  /// doing anything.
  advance(t, s = !1) {
    var c;
    if (t && ((c = this.token) == null ? void 0 : c.id) !== t) {
      if (s)
        return;
      p("Expected '" + t + "'.", this.token);
    }
    if (this.token_nr >= this.tokens.length) {
      this.token = this.symbolTable["(end)"];
      return;
    }
    const r = this.tokens[this.token_nr];
    this.token_nr += 1;
    const n = r.value, o = r.type;
    let i;
    o === "name" ? i = this.scope.find_or_define(r) : o === "operator" ? (i = this.symbolTable[n], i || p(`Unknown operator "${n}".`, r)) : o === "latex" ? (i = this.symbolTable[n], i || p(`Unknown latex command "${n}".`, r)) : o === "number" ? i = this.symbolTable["(number)"] : o === "symbol" ? i = this.symbolTable[n] ?? this.symbolTable["(symbol)"] : p("Unexpected token.", r), i || p("Couldn't find token.", r);
    const u = Object.create(i);
    return u.from = r.from, u.to = r.to, u.value = n, u.type = o, this.token = u, this.token;
  }
  /// If a token is passed as second parameter, no initial advance() is called.
  expression(t, s = null) {
    if (!this.token)
      throw new Error("Parser error: Missing token.");
    let r = s || this.token;
    s || this.advance();
    let n = r.nud();
    for (; t < this.token.lbp; )
      r = this.token, this.advance(), n = r.led(n);
    return n;
  }
  /// Setup the symbol table (usually before the actual parsing).
  registerSymbol(t, s = 0) {
    let r = this.symbolTable[t];
    return r ? s > r.lbp && (r.lbp = s) : (r = new V(t, s), this.symbolTable[t] = r), r;
  }
  parse(t) {
    if (this.tokens = H(t), this.tokens.length == 0)
      throw "Error: No tokens to parse.";
    this.scope = new N(this.symbolTable), this.token_nr = 0, this.advance();
    const s = this.expression(0);
    return this.advance("(end)"), s;
  }
}
const P = 5, J = 6, $ = 10, S = 20, m = 25, E = 30, k = 40, W = {
  registerSum(e) {
    const t = e.registerSymbol("+", $);
    t.nud = () => ({
      type: "Plus",
      args: [e.expression(E)]
    }), t.led = d(e, "Sum");
    for (const s of ["-", "–"]) {
      const r = e.registerSymbol(s, $);
      r.nud = () => ({
        type: "Minus",
        args: [e.expression(E)]
      }), r.led = d(e, "Sum", "Minus");
    }
    for (const s of ["\\pm", "±"]) {
      const r = e.registerSymbol(s, $);
      r.nud = () => ({
        type: "PlusMinus",
        args: [e.expression(E)]
      }), r.led = d(e, "Sum", "PlusMinus");
    }
  },
  registerTimes(e) {
    for (const t of ["*", "\\cdot", "\\times", "×"])
      e.registerSymbol(t, S).led = d(e, "Product");
    for (const t of [":", "\\div", "/"])
      e.registerSymbol(t, S).led = d(
        e,
        "Product",
        "DivideBy"
      );
  },
  registerNumber(e) {
    const t = e.registerSymbol("(number)", m);
    t.nud = function() {
      return { type: "Number", value: this.value };
    }, t.led = d(e, "ImplicitProduct");
    const s = e.registerSymbol("\\overline", 100);
    s.led = function(r) {
      const n = e.expression(100);
      return {
        type: "Number",
        value: `${r.value}[${n.value}]`
      };
    };
  },
  registerVariable(e) {
    const t = e.registerSymbol("(name)", m);
    t.nud = function() {
      return { type: "Variable", value: this.value };
    }, t.led = d(e, "ImplicitProduct");
  },
  registerSymbols(e) {
    e.registerSymbol("(symbol)"), e.registerSymbol("UNDEFINED", m).nud = () => ({
      type: "UNDEFINED"
    }), e.registerSymbol("\\infty", m).nud = () => ({
      type: "INFINITY"
    }), e.registerSymbol("\\mathbb{R}", m).nud = () => ({
      type: "REALS"
    }), e.registerSymbol("\\emptyset", m).nud = () => ({
      type: "FiniteSet",
      args: []
    });
  },
  registerSolution(e) {
    const t = e.registerSymbol("\\in", J);
    t.led = (s) => {
      const r = e.expression(0);
      return { type: "Solution", args: [s, r] };
    };
  },
  registerEquation(e) {
    for (const [t, s] of [
      ["=", "Equation"],
      ["<", "LessThan"],
      ["≤", "LessThanEqual"],
      ["\\leq", "LessThanEqual"],
      [">", "GreaterThan"],
      ["≥", "GreaterThanEqual"],
      ["\\geq", "GreaterThanEqual"]
    ])
      e.registerSymbol(t, P).led = (r) => ({ type: s, args: [r, e.expression(P)] });
  },
  registerBrackets(e) {
    function t(s, r, n) {
      const o = e.registerSymbol(s, m);
      e.registerSymbol(r), o.nud = () => {
        const i = e.expression(0);
        return e.advance(r), n && (i.decorators ? i.decorators.push(n) : i.decorators = [n]), i;
      }, o.led = d(e, "ImplicitProduct");
    }
    t("\\{", "\\}", "CurlyBracket"), t("[", "]", "SquareBracket"), t("(", ")", "RoundBracket"), t("{", "}");
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
    }, s = (n) => {
      const o = e.expression(100), i = e.expression(100);
      return n.type === "Number" && Number.isInteger(+n.value) && o.type === "Number" && Number.isInteger(+o.value) && i.type === "Number" && Number.isInteger(+i.value) ? { type: "MixedNumber", args: [n, o, i] } : {
        type: "ImplicitProduct",
        args: [n, { type: "Fraction", args: [o, i] }]
      };
    };
    ["\\frac", "\\dfrac", "\\tfrac"].forEach((n) => {
      const o = e.registerSymbol(n, S);
      o.nud = t, o.led = s;
    });
  },
  registerExponent(e) {
    const t = e.registerSymbol("^", k);
    t.led = function(s) {
      const r = e.expression(k - 1);
      return { type: "Power", args: [s, r] };
    };
  },
  registerRoots(e) {
    const t = e.registerSymbol("\\sqrt", m);
    t.nud = function() {
      if (e.advance("[", !0)) {
        const s = e.expression(0);
        return e.advance("]"), {
          type: "Root",
          args: [e.expression(1 / 0), s]
        };
      } else
        return { type: "SquareRoot", args: [e.expression(1 / 0)] };
    }, t.led = t.led = d(e, "ImplicitProduct");
  }
};
function d(e, t, s, r) {
  return function(n) {
    var u, c;
    const o = t === "Sum" ? $ : t === "ImplicitProduct" ? m : S;
    let i = t === "ImplicitProduct" ? e.expression(o, this) : e.expression(o);
    if (s && (i = { type: s, args: [i] }), r) {
      const y = r(n, i);
      if (y)
        return y;
    }
    return t === "Sum" && !s && !((u = i.decorators) != null && u.length) && (i.type === "Minus" || i.type === "PlusMinus") && (i.decorators = ["MissingBracket"]), n.type === t && !((c = n.decorators) != null && c.length) ? { type: t, args: [...n.args || [], i] } : { type: t, args: [n, i] };
  };
}
function U(e, t = ".") {
  return "args" in e ? {
    ...e,
    path: t,
    args: e.args.map((s, r) => U(s, `${t}/${r}`))
  } : { ...e, path: t };
}
const Q = new Z(Object.values(W));
function X(e) {
  return U(Q.parse(e));
}
function w(e) {
  const t = (r) => w(r), s = (r) => K(r, e.decorators);
  switch (e.type) {
    case "Number":
      return s(e.value);
    case "Variable":
      return s(e.value);
    case "Sum":
      return s(
        e.args.map(
          (r, n) => {
            var o, i;
            return r.type === "Minus" && !((o = r.decorators) != null && o.length) ? (
              // binary minus
              `-${t(r.args[0])}`
            ) : r.type === "PlusMinus" && !((i = r.decorators) != null && i.length) ? (
              // binary ±
              `+/-${t(r.args[0])}`
            ) : (
              // binary plus
              n === 0 ? t(r) : `+${t(r)}`
            );
          }
        ).join("")
      );
    case "Plus":
      return s(`+${t(e.args[0])}`);
    case "Minus":
      return s(`-${t(e.args[0])}`);
    case "PlusMinus":
      return s(`+/-${t(e.args[0])}`);
    case "Product":
      return s(
        e.args.map((r, n) => n == 0 || r.type === "DivideBy" ? t(r) : `*${t(r)}`).join("")
      );
    case "ImplicitProduct":
      return s(e.args.map((r) => t(r)).join(" "));
    case "DivideBy":
      return `:${t(e.args[0])}`;
    case "Fraction":
      return s(`[${t(e.args[0])} / ${t(e.args[1])}]`);
    case "MixedNumber":
      return s(`[${t(e.args[0])} ${t(e.args[1])} / ${t(e.args[2])}]`);
    case "Power":
      return s(`[${t(e.args[0])} ^ ${t(e.args[1])}]`);
    case "SquareRoot":
      return s(`sqrt[${t(e.args[0])}]`);
    case "Root":
      return s(`root[${t(e.args[0])}, ${t(e.args[1])}]`);
    case "Equation":
      return s(`${t(e.args[0])} = ${t(e.args[1])}`);
    case "UNDEFINED":
      return s("UNDEFINED");
    case "INFINITY":
      return s("INFINITY");
    case "REALS":
      return s("REALS");
    case "LessThan":
      return s(`${t(e.args[0])} < ${t(e.args[1])}`);
    case "GreaterThan":
      return s(`${t(e.args[0])} > ${t(e.args[1])}`);
    case "LessThanEqual":
      return s(`${t(e.args[0])} <= ${t(e.args[1])}`);
    case "GreaterThanEqual":
      return s(`${t(e.args[0])} >= ${t(e.args[1])}`);
    case "Solution":
      return s(`Solution[${t(e.args[0])}, ${t(e.args[1])}]`);
    case "FiniteSet":
      return s(`{${e.args.map((r) => t(r)).join(", ")}}`);
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
  return t ? t.reduce((s, r) => r === "RoundBracket" ? `(${s})` : r === "SquareBracket" ? `[.${s}.]` : r === "CurlyBracket" ? `{.${s}.}` : s, e) : e;
}
function re(e) {
  return (t, s) => {
    const r = e[t.path];
    return r ? `{\\color{${r}}${s}}` : s;
  };
}
function se(e, t, s = !0) {
  const r = {}, n = {};
  s && (e = e.filter((o) => o.type !== "Shift"));
  for (const [o, i] of e.entries()) {
    const u = t[o % t.length];
    for (const c of i.fromPaths)
      r[c] = u;
    for (const c of i.toPaths)
      n[c] = u;
  }
  return [r, n];
}
function ne(e, t) {
  return G(R(e), t);
}
function oe(e) {
  return w(X(e));
}
function _(e) {
  return e.length === 0 ? "." : `./${e.join("/")}`;
}
function ae(e) {
  return e === "." ? [] : (e.startsWith("./") && (e = e.substring(2)), e.split("/").map((t) => +t));
}
function ie(e, t, s, r = !0) {
  function n(o, i, u = 0) {
    var y;
    if (s.length === u)
      return r ? {
        ...T(i, _(s)),
        ...(y = o.decorators) != null && y.length ? { decorators: o.decorators.slice() } : null
      } : T(i, _(s));
    const c = s[u];
    if (!("args" in o) || c >= o.args.length)
      throw new Error("Invalid path");
    return {
      ...o,
      args: o.args.map((b, v) => v === c ? n(b, i, u + 1) : b)
    };
  }
  return n(e, t);
}
function T(e, t = "") {
  return {
    ...e,
    path: t + e.path.slice(1),
    ..."args" in e ? { args: e.args.map((s) => T(s, t)) } : null
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
  O as treeToJson,
  G as treeToLatex,
  w as treeToSolver
};
