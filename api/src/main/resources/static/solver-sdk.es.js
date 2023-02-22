var F = Object.defineProperty;
var L = (e, t, s) => t in e ? F(e, t, { enumerable: !0, configurable: !0, writable: !0, value: s }) : e[t] = s;
var g = (e, t, s) => (L(e, typeof t != "symbol" ? t + "" : t, s), s);
const B = "https://solver.geogebra.net/main/api/v1", M = {
  headers: { "Content-Type": "application/json" }
}, j = {
  curriculum: "",
  precision: 3
};
class C {
  constructor(t = B, s = M, r = j) {
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
    }).then((u) => u.json());
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
const te = new C();
function U(e, t = ".") {
  const [s, ...r] = e;
  let n, u = [];
  typeof s == "string" ? n = s : [n, ...u] = s;
  let i;
  if (r.length === 0 && n !== "FiniteSet") {
    const a = n;
    a.match(/^[+\-0-9]/) ? i = { type: "Number", value: a, path: t } : a === "UNDEFINED" ? i = { type: "UNDEFINED", path: t } : a === "INFINITY" ? i = { type: "INFINITY", path: t } : a === "REALS" ? i = { type: "REALS", path: t } : i = { type: "Variable", value: a, path: t };
  } else
    i = {
      type: n,
      args: r.map((a, c) => U(a, `${t}/${c}`)),
      path: t
    };
  return u.length && (i.decorators = u), i;
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
  return $(
    e,
    { ...A, ...t },
    s || ((r, n) => n)
  );
}
function $(e, t, s) {
  const r = (u) => $(u, t, s), n = (u) => s(e, E(u, e.decorators));
  switch (e.type) {
    case "Number":
      return s(e, E(Y(e), e.decorators));
    case "Variable":
      return s(e, E(e.value, e.decorators));
    case "Sum":
      return n(
        e.args.map(
          (a, c) => {
            var d, p;
            return a.type === "Minus" && !((d = a.decorators) != null && d.length) ? (
              // binary minus
              `-${r(a.args[0])}`
            ) : a.type === "PlusMinus" && !((p = a.decorators) != null && p.length) ? (
              // binary ±
              `\\pm ${r(a.args[0])}`
            ) : (
              // binary plus
              c === 0 ? r(a) : `+${r(a)}`
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
          (a, c) => c == 0 || a.type === "DivideBy" ? r(a) : `${t.mulSymbol}${r(a)}`
        ).join("")
      );
    case "ImplicitProduct":
      return n(e.args.map((a) => r(a)).join(""));
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
      return n(`\\sqrt[{${r(e.args[1])}}]{${r(e.args[0])}}`);
    case "Equation":
      return t.align ? n(`${r(e.args[0])} & = & ${r(e.args[1])}`) : n(`${r(e.args[0])} = ${r(e.args[1])}`);
    case "EquationSystem":
      const u = { ...t, align: !0 };
      return n(
        `\\left\\{\\begin{array}{rcl}
` + e.args.map((a) => "  " + $(a, u, s) + `\\\\
`).join("") + "\\end{array}\\right."
      );
    case "EquationUnion":
      const i = { ...t, align: !1 };
      return n(
        e.args.map((a) => $(a, i, s)).join(", ")
      );
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
        e.args.length === 0 ? "\\emptyset" : `\\left\\{${e.args.map((a) => r(a)).join(", ")}\\right\\}`
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
function E(e, t) {
  return t ? t.reduce((s, r) => r === "RoundBracket" ? `\\left(${s}\\right)` : r === "SquareBracket" ? `\\left[${s}\\right]` : r === "CurlyBracket" ? `\\left\\{${s}\\right\\}` : s, e) : e;
}
function H(e) {
  const t = /^\s*(UNDEFINED|REALS|\\text\{[Uu]ndefined\}|\\mathbb\{R\})/, s = /^\s*((([0-9]*(\.[0-9]*|[0-9]+))([eE][-+]?[0-9]+)?)|∞|Infinity)/, r = new RegExp("^\\s*\\{(\\??[A-Za-z0-9_]*)\\:"), n = new RegExp("^\\s*([a-zA-Z₀₁₂₃₄₅₆₇₈₉⬚])"), u = new RegExp("^\\s*(sqrt|root|Solution)"), i = new RegExp("^\\s*([α-ωΑ-Ω])"), a = new RegExp('^\\s*"([^"]+)"'), c = new RegExp("^\\s*\\\\text{(.*?)}"), d = /^\s*(\[\.|\.\]|\{\.|\.\}|>=|<=|\*\*|[-–+×*/÷=><();,^{}[\]|_±⁰¹²³⁴⁵⁶⁷⁸⁹ⁿ:≥≤√])/, p = new RegExp("^\\s*(\\\\[^A-Za-z0-9\\s]|\\\\[A-Za-z]+)"), x = new RegExp("^(\\s+)"), f = [], w = e.length;
  for (let l = 0; l < w; ) {
    const h = e.substring(l);
    let o;
    if (o = x.exec(h))
      l += o[0].length;
    else if (o = t.exec(h))
      f.push({
        type: "symbol",
        value: o[1].replace(/\\text\{[Uu]ndefined\}/, "UNDEFINED"),
        from: l + (o[0].length - o[1].length),
        to: l + o[0].length
      }), l += o[0].length;
    else if (o = s.exec(h))
      f.push({
        type: "number",
        value: o[1].replace("∞", "Infinity"),
        from: l + (o[0].length - o[1].length),
        to: l + o[0].length
      }), l += o[0].length;
    else if (o = r.exec(h))
      f.push({ type: "operator", value: "{", from: l, to: l + 1 }), f.push({ type: "name", value: o[1], from: l + 1, to: l + o[1].length + 1 }), f.push({
        type: "operator",
        value: ":",
        from: l + o[0].length - 1,
        to: l + o[0].length
      }), l += o[0].length;
    else if (o = u.exec(h))
      f.push({
        type: "operator",
        value: o[1],
        // will be something like "sin"
        from: l + (o[0].length - o[1].length),
        to: l + o[0].length
      }), l += o[0].length;
    else if (o = n.exec(h))
      f.push({
        type: "name",
        value: o[1],
        from: l + (o[0].length - o[1].length),
        to: l + o[0].length
      }), l += o[0].length;
    else if (o = i.exec(h))
      f.push({
        type: "name",
        value: o[1],
        from: l + (o[0].length - o[1].length),
        to: l + o[0].length
      }), l += o[0].length;
    else if (o = a.exec(h))
      f.push({
        type: "name",
        value: o[1],
        from: l + (o[0].length - o[1].length),
        to: l + o[0].length
      }), l += o[0].length;
    else if (o = c.exec(h)) {
      const N = l + o.index + o[0].indexOf(o[1]);
      f.push({ type: "name", value: o[1], from: N, to: N + o[1].length }), l += o[0].length;
    } else if (o = d.exec(h))
      f.push({
        type: "operator",
        value: o[1],
        from: l + (o[0].length - o[1].length),
        to: l + o[0].length
      }), l += o[0].length;
    else if (o = p.exec(h))
      f.push({
        type: "latex",
        value: o[1],
        from: l + (o[0].length - o[1].length),
        to: l + o[0].length
      }), l += o[0].length;
    else
      throw "can't tokenize '" + h + "'";
  }
  return f.filter((l) => l.value !== "\\left" && l.value !== "\\right");
}
function b(e, t) {
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
    g(this, "nud", () => b(`Undefined nud for ${this.id}`, this));
    g(this, "led", () => b(`Undefined led for ${this.id}`, this));
  }
}
class P {
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
    g(this, "scope", new P(this.symbolTable));
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
      b("Expected '" + t + "'.", this.token);
    }
    if (this.token_nr >= this.tokens.length) {
      this.token = this.symbolTable["(end)"];
      return;
    }
    const r = this.tokens[this.token_nr];
    this.token_nr += 1;
    const n = r.value, u = r.type;
    let i;
    u === "name" ? i = this.scope.find_or_define(r) : u === "operator" ? (i = this.symbolTable[n], i || b(`Unknown operator "${n}".`, r)) : u === "latex" ? (i = this.symbolTable[n], i || b(`Unknown latex command "${n}".`, r)) : u === "number" ? i = this.symbolTable["(number)"] : u === "symbol" ? i = this.symbolTable[n] ?? this.symbolTable["(symbol)"] : b("Unexpected token.", r), i || b("Couldn't find token.", r);
    const a = Object.create(i);
    return a.from = r.from, a.to = r.to, a.value = n, a.type = u, this.token = a, this.token;
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
    this.scope = new P(this.symbolTable), this.token_nr = 0, this.advance();
    const s = this.expression(0);
    return this.advance("(end)"), s;
  }
}
const k = 5, J = 6, S = 10, v = 20, y = 25, T = 30, _ = 40, W = {
  registerSum(e) {
    const t = e.registerSymbol("+", S);
    t.nud = () => ({
      type: "Plus",
      args: [e.expression(T)]
    }), t.led = m(e, "Sum");
    for (const s of ["-", "–"]) {
      const r = e.registerSymbol(s, S);
      r.nud = () => ({
        type: "Minus",
        args: [e.expression(T)]
      }), r.led = m(e, "Sum", "Minus");
    }
    for (const s of ["\\pm", "±"]) {
      const r = e.registerSymbol(s, S);
      r.nud = () => ({
        type: "PlusMinus",
        args: [e.expression(T)]
      }), r.led = m(e, "Sum", "PlusMinus");
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
    const t = e.registerSymbol("(number)", y);
    t.nud = function() {
      return { type: "Number", value: this.value };
    }, t.led = m(e, "ImplicitProduct");
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
    const t = e.registerSymbol("(name)", y);
    t.nud = function() {
      return { type: "Variable", value: this.value };
    }, t.led = m(e, "ImplicitProduct");
  },
  registerSymbols(e) {
    e.registerSymbol("(symbol)"), e.registerSymbol("UNDEFINED", y).nud = () => ({
      type: "UNDEFINED"
    }), e.registerSymbol("\\infty", y).nud = () => ({
      type: "INFINITY"
    }), e.registerSymbol("\\mathbb{R}", y).nud = () => ({
      type: "REALS"
    }), e.registerSymbol("\\emptyset", y).nud = () => ({
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
      e.registerSymbol(t, k).led = (r) => ({ type: s, args: [r, e.expression(k)] });
  },
  registerBrackets(e) {
    function t(s, r, n, u = !1) {
      const i = e.registerSymbol(s, y);
      e.registerSymbol(r), i.nud = () => {
        if (u && e.advance(r, !0))
          return e.expression(0);
        const a = e.expression(0);
        return e.advance(r), n && (a.decorators ? a.decorators.push(n) : a.decorators = [n]), a;
      }, i.led = m(e, "ImplicitProduct");
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
      const n = e.expression(100), u = e.expression(100);
      return { type: "Fraction", args: [n, u] };
    }, s = (n) => {
      const u = e.expression(100), i = e.expression(100);
      return n.type === "Number" && Number.isInteger(+n.value) && u.type === "Number" && Number.isInteger(+u.value) && i.type === "Number" && Number.isInteger(+i.value) ? { type: "MixedNumber", args: [n, u, i] } : {
        type: "ImplicitProduct",
        args: [n, { type: "Fraction", args: [u, i] }]
      };
    };
    ["\\frac", "\\dfrac", "\\tfrac"].forEach((n) => {
      const u = e.registerSymbol(n, v);
      u.nud = t, u.led = s;
    });
  },
  registerExponent(e) {
    const t = e.registerSymbol("^", _);
    t.led = function(s) {
      const r = e.expression(_ - 1);
      return { type: "Power", args: [s, r] };
    };
  },
  registerRoots(e) {
    const t = e.registerSymbol("\\sqrt", y);
    t.nud = function() {
      if (e.advance("[", !0)) {
        const s = e.expression(0);
        return e.advance("]"), {
          type: "Root",
          args: [e.expression(1 / 0), s]
        };
      } else
        return { type: "SquareRoot", args: [e.expression(1 / 0)] };
    }, t.led = t.led = m(e, "ImplicitProduct");
  }
};
function m(e, t, s, r) {
  return function(n) {
    var a, c;
    const u = t === "Sum" ? S : t === "ImplicitProduct" ? y : v;
    let i = t === "ImplicitProduct" ? e.expression(u, this) : e.expression(u);
    if (s && (i = { type: s, args: [i] }), r) {
      const d = r(n, i);
      if (d)
        return d;
    }
    return t === "Sum" && !s && !((a = i.decorators) != null && a.length) && (i.type === "Minus" || i.type === "PlusMinus") && (i.decorators = ["MissingBracket"]), n.type === t && !((c = n.decorators) != null && c.length) ? { type: t, args: [...n.args || [], i] } : { type: t, args: [n, i] };
  };
}
function q(e, t = ".") {
  return "args" in e ? {
    ...e,
    path: t,
    args: e.args.map((s, r) => q(s, `${t}/${r}`))
  } : { ...e, path: t };
}
const Q = new Z(Object.values(W));
function X(e) {
  return q(Q.parse(e));
}
function D(e) {
  const t = (r) => D(r), s = (r) => K(r, e.decorators);
  switch (e.type) {
    case "Number":
      return s(e.value);
    case "Variable":
      return s(e.value);
    case "Sum":
      return s(
        e.args.map(
          (r, n) => {
            var u, i;
            return r.type === "Minus" && !((u = r.decorators) != null && u.length) ? (
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
    case "EquationSystem":
      return s(e.args.map((r) => t(r)).join(", "));
    case "EquationUnion":
      return s(e.args.map((r) => t(r)).join(" OR "));
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
  s && (e = e.filter((u) => u.type !== "Shift"));
  for (const [u, i] of e.entries()) {
    const a = t[u % t.length];
    for (const c of i.fromPaths)
      r[c] = a;
    for (const c of i.toPaths)
      n[c] = a;
  }
  return [r, n];
}
function ne(e, t) {
  return G(U(e), t);
}
function oe(e) {
  return D(X(e));
}
function R(e) {
  return e.length === 0 ? "." : `./${e.join("/")}`;
}
function ae(e) {
  return e === "." ? [] : (e.startsWith("./") && (e = e.substring(2)), e.split("/").map((t) => +t));
}
function ie(e, t, s, r = !0) {
  function n(u, i, a = 0) {
    var d;
    if (s.length === a)
      return r ? {
        ...I(i, R(s)),
        ...(d = u.decorators) != null && d.length ? { decorators: u.decorators.slice() } : null
      } : I(i, R(s));
    const c = s[a];
    if (!("args" in u) || c >= u.args.length)
      throw new Error("Invalid path");
    return {
      ...u,
      args: u.args.map((p, x) => x === c ? n(p, i, a + 1) : p)
    };
  }
  return n(e, t);
}
function I(e, t = "") {
  return {
    ...e,
    path: t + e.path.slice(1),
    ..."args" in e ? { args: e.args.map((s) => I(s, t)) } : null
  };
}
export {
  te as api,
  R as arrayToPath,
  re as coloringTransformer,
  se as createColorMaps,
  ne as jsonToLatex,
  U as jsonToTree,
  oe as latexToSolver,
  X as latexToTree,
  ae as pathToArray,
  ie as substituteTree,
  O as treeToJson,
  G as treeToLatex,
  D as treeToSolver
};
