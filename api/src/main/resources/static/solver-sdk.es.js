var j = Object.defineProperty;
var C = (e, t, n) => t in e ? j(e, t, { enumerable: !0, configurable: !0, writable: !0, value: n }) : e[t] = n;
var g = (e, t, n) => (C(e, typeof t != "symbol" ? t + "" : t, n), n);
const O = "https://solver.geogebra.net/main/api/v1.0-alpha0", F = {
  headers: { "Content-Type": "application/json" }
}, M = {
  curriculum: "",
  precision: 3
};
class A {
  constructor(t = O, n = F, r = M) {
    g(this, "baseUrl");
    g(this, "defaultHeaders");
    g(this, "defaultContext");
    this.baseUrl = t, this.defaultHeaders = n, this.defaultContext = r;
  }
  /** Get a transformation for each applicable plan. */
  async selectPlans(t, n = "latex", r = this.defaultContext) {
    return fetch(`${this.baseUrl}/selectPlans`, {
      ...this.defaultHeaders,
      method: "POST",
      body: JSON.stringify({ input: t, format: n, context: r })
    }).then((s) => s.json());
  }
  /* Apply plan to input and return the transformation. */
  async applyPlan(t, n, r = "latex", s = this.defaultContext) {
    return fetch(`${this.baseUrl}/plans/${n}/apply`, {
      ...this.defaultHeaders,
      method: "POST",
      body: JSON.stringify({ input: t, format: r, context: s })
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
const te = new A();
function H(e) {
  const t = new RegExp("^\\s*([0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?|∞|Infinity)"), n = new RegExp("^\\s*\\{(\\??[A-Za-z0-9_]*)\\:"), r = new RegExp("^\\s*([a-zA-Z₀₁₂₃₄₅₆₇₈₉⬚])"), s = new RegExp("^\\s*(sqrt|root)"), o = new RegExp("^\\s*([α-ωΑ-Ω])"), l = new RegExp('^\\s*"([^"]+)"'), u = new RegExp("^\\s*\\\\text{(.*?)}"), c = new RegExp(
    "^\\s*(\\[\\.|\\.\\]|\\{\\.|\\.\\}|>=|<=|\\*\\*|[-\\–+*/÷=><();,^\\{\\}\\[\\]\\|_±⁰¹²³⁴⁵⁶⁷⁸⁹ⁿ:≥≤√])"
  ), p = new RegExp("^\\s*(\\\\[^A-Za-z0-9\\s]|\\\\[A-Za-z]+)"), $ = new RegExp("^(\\s+)"), f = [], D = e.length;
  for (let a = 0; a < D; ) {
    const d = e.substring(a);
    let i;
    if (i = t.exec(d))
      f.push({
        type: "number",
        value: i[1].replace("∞", "Infinity"),
        from: a + (i[0].length - i[1].length),
        to: a + i[0].length
      }), a += i[0].length;
    else if (i = n.exec(d))
      f.push({ type: "operator", value: "{", from: a, to: a + 1 }), f.push({ type: "name", value: i[1], from: a + 1, to: a + i[1].length + 1 }), f.push({
        type: "operator",
        value: ":",
        from: a + i[0].length - 1,
        to: a + i[0].length
      }), a += i[0].length;
    else if (i = s.exec(d))
      f.push({
        type: "operator",
        value: i[1],
        // will be something like "sin"
        from: a + (i[0].length - i[1].length),
        to: a + i[0].length
      }), a += i[0].length;
    else if (i = r.exec(d))
      f.push({
        type: "name",
        value: i[1],
        from: a + (i[0].length - i[1].length),
        to: a + i[0].length
      }), a += i[0].length;
    else if (i = o.exec(d))
      f.push({
        type: "name",
        value: i[1],
        from: a + (i[0].length - i[1].length),
        to: a + i[0].length
      }), a += i[0].length;
    else if (i = l.exec(d))
      f.push({
        type: "name",
        value: i[1],
        from: a + (i[0].length - i[1].length),
        to: a + i[0].length
      }), a += i[0].length;
    else if (i = u.exec(d)) {
      const E = a + i.index + i[0].indexOf(i[1]);
      f.push({ type: "name", value: i[1], from: E, to: E + i[1].length }), a += i[0].length;
    } else if (i = c.exec(d))
      f.push({
        type: "operator",
        value: i[1],
        from: a + (i[0].length - i[1].length),
        to: a + i[0].length
      }), a += i[0].length;
    else if (i = p.exec(d))
      f.push({
        type: "latex",
        value: i[1],
        from: a + (i[0].length - i[1].length),
        to: a + i[0].length
      }), a += i[0].length;
    else if (i = $.exec(d))
      a += i[0].length;
    else
      throw "can't tokenize '" + d + "'";
  }
  return f.filter((a) => a.value !== "\\left" && a.value !== "\\right");
}
function b(e, t) {
  throw { name: "SyntaxError", message: e, object: t };
}
class z {
  constructor(t, n) {
    g(this, "id");
    g(this, "value");
    g(this, "lbp");
    this.id = this.value = t, this.lbp = n;
  }
}
class V extends z {
  constructor() {
    super(...arguments);
    g(this, "nud", () => b(`Undefined nud for ${this.id}`, this));
    g(this, "led", () => b(`Undefined led for ${this.id}`, this));
  }
}
class k {
  constructor(t) {
    g(this, "def", {});
    g(this, "symbolTable");
    this.symbolTable = t;
  }
  find_or_define(t) {
    let n = this.def[t.value];
    return n && typeof n != "function" || (n = Object.create(this.symbolTable["(name)"]), n.lpb = 0, this.def[t.value] = n), n;
  }
}
class R {
  constructor(t) {
    g(this, "symbolTable", {});
    g(this, "token", null);
    g(this, "tokens", []);
    g(this, "token_nr", 0);
    g(this, "scope", new k(this.symbolTable));
    this.registerSymbol("(end)"), t.forEach((n) => n(this));
  }
  /// Read and return the next token.
  /// If `id` is passed, the method will throw an error if the current token
  /// has not the same (expected) id. If `optional` is passed, the method will
  /// only advance to the next token if the current token has the same id as
  /// the passed one. In case the id differs, the method just returns without
  /// doing anything.
  advance(t, n = !1) {
    var c;
    if (t && ((c = this.token) == null ? void 0 : c.id) !== t) {
      if (n)
        return;
      b("Expected '" + t + "'.", this.token);
    }
    if (this.token_nr >= this.tokens.length) {
      this.token = this.symbolTable["(end)"];
      return;
    }
    const r = this.tokens[this.token_nr];
    this.token_nr += 1;
    const s = r.value, o = r.type;
    let l;
    o === "name" ? l = this.scope.find_or_define(r) : o === "operator" ? (l = this.symbolTable[s], l || b(`Unknown operator "${s}".`, r)) : o === "latex" ? (l = this.symbolTable[s], l || b(`Unknown latex command "${s}".`, r)) : o === "number" ? l = this.symbolTable["(number)"] : b("Unexpected token.", r), l || b("Couldn't find token.", r);
    const u = Object.create(l);
    return u.from = r.from, u.to = r.to, u.value = s, u.type = o, this.token = u, this.token;
  }
  /// If a token is passed as second parameter, no initial advance() is called.
  expression(t, n = null) {
    if (!this.token)
      throw new Error("Parser error: Missing token.");
    let r = n || this.token;
    n || this.advance();
    let s = r.nud();
    for (; t < this.token.lbp; )
      r = this.token, this.advance(), s = r.led(s);
    return s;
  }
  /// Setup the symbol table (usually before the actual parsing).
  registerSymbol(t, n = 0) {
    let r = this.symbolTable[t];
    return r ? n > r.lbp && (r.lbp = n) : (r = new V(t, n), this.symbolTable[t] = r), r;
  }
  parse(t) {
    if (this.tokens = H(t), this.tokens.length == 0)
      throw "Error: No tokens to parse.";
    this.scope = new k(this.symbolTable), this.token_nr = 0, this.advance();
    const n = this.expression(0);
    return this.advance("(end)"), n;
  }
}
const x = 10, m = 20, I = 30, Y = {
  registerBasicOperators(e) {
    function t(n, r, s) {
      const o = n.registerSymbol(r, x);
      o.nud = () => s + n.expression(x), o.led = (l) => l + s + n.expression(x);
    }
    [
      { from: "+", to: "+" },
      { from: "-", to: "-" },
      { from: "–", to: "-" },
      { from: "×", to: "*" },
      { from: "\\times", to: "*" },
      { from: "\\cdot", to: "*" },
      { from: "*", to: "*" },
      { from: "\\div", to: ":" },
      { from: "÷", to: ":" },
      { from: ":", to: ":" },
      { from: "=", to: "=" }
    ].forEach(({ from: n, to: r }) => {
      t(e, n, r);
    });
  },
  registerNumber(e) {
    const t = e.registerSymbol("(number)", m);
    t.nud = function() {
      return `${this.value}`;
    }, t.led = function(r) {
      return `${r} ${e.expression(m, this)}`;
    };
    const n = e.registerSymbol("\\overline", 100);
    n.led = function(r) {
      const s = e.expression(100);
      return `${r}[${s}]`;
    };
  },
  registerVariable(e) {
    const t = e.registerSymbol("(name)", m);
    t.nud = function() {
      return `${this.value}`;
    }, t.led = function(n) {
      return `${n} ${e.expression(m, this)}`;
    };
  },
  registerBrackets(e) {
    function t(n, r, s, o = n, l = r) {
      const u = e.registerSymbol(n, m);
      u.nud = function() {
        const c = e.expression(0);
        return e.advance(r), s ? `${o}${c}${l}` : c;
      }, u.led = function(c) {
        return `${c} ${u.nud()}`;
      }, e.registerSymbol(r);
    }
    t("{", "}", !1), t("\\{", "\\}", !0, "{.", ".}"), t("\\lbrace", "\\rbrace", !0, "{.", ".}"), t("(", ")", !0), t("[", "]", !0, "[.", ".]");
  },
  registerFraction(e) {
    const t = () => {
      const s = e.expression(100), o = e.expression(100);
      return `[${s} / ${o}]`;
    }, n = (s) => {
      const o = e.expression(100), l = e.expression(100);
      return Number.isInteger(+s) && Number.isInteger(+o) && Number.isInteger(+l) ? `[${s} ${o} / ${l}]` : `${s} [${o} / ${l}]`;
    };
    ["\\frac", "\\dfrac", "\\tfrac"].forEach((s) => {
      const o = e.registerSymbol(s, m);
      o.nud = t, o.led = n;
    });
  },
  registerExponent(e) {
    const t = e.registerSymbol("^", I);
    t.led = function(n) {
      const r = e.expression(I - 1);
      return `[${n} ^ ${r}]`;
    };
  },
  registerRoots(e) {
    const t = e.registerSymbol("\\sqrt", m);
    t.nud = function() {
      if (e.advance("[", !0)) {
        const n = e.expression(0);
        return e.advance("]"), `root[${e.expression(1 / 0)} , ${n}]`;
      } else
        return `sqrt[${e.expression(1 / 0)}]`;
    }, t.led = function(n) {
      return `${n} ${t.nud()}`;
    };
  }
}, Z = new R(Object.values(Y)), re = (e) => Z.parse(e);
function B(e, t = ".") {
  const [n, ...r] = e;
  let s, o = [];
  typeof n == "string" ? s = n : [s, ...o] = n;
  let l;
  if (r.length === 0 && s !== "FiniteSet") {
    const u = s;
    u.match(/^[+\-0-9]/) ? l = { type: "Number", value: u, path: t } : u === "UNDEFINED" ? l = { type: "UNDEFINED", path: t } : u === "INFINITY" ? l = { type: "INFINITY", path: t } : u === "Reals" ? l = { type: "REALS", path: t } : l = { type: "Variable", value: u, path: t };
  } else
    l = {
      type: s,
      args: r.map((u, c) => B(u, `${t}/${c}`)),
      path: t
    };
  return o.length && (l.decorators = o), l;
}
function G(e) {
  var t;
  return e.type === "Number" || e.type === "Variable" ? (t = e.decorators) != null && t.length ? [[e.value, ...e.decorators]] : [e.value] : [
    e.decorators ? [e.type, ...e.decorators] : e.type,
    ...e.args.map((n) => G(n))
  ];
}
const _ = 5, S = 10, T = 20, y = 25, N = 30, J = {
  registerSum(e) {
    const t = e.registerSymbol("+", S);
    t.nud = () => ({
      type: "Plus",
      args: [e.expression(N)]
    }), t.led = h(e, "Sum");
    const n = e.registerSymbol("-", S);
    n.nud = () => ({
      type: "Minus",
      args: [e.expression(N)]
    }), n.led = h(e, "Sum", "Minus");
  },
  registerTimes(e) {
    e.registerSymbol("*", T).led = h(e, "Product"), e.registerSymbol(":", T).led = h(
      e,
      "Product",
      "DivideBy"
    );
  },
  registerNumber(e) {
    const t = e.registerSymbol("(number)", y);
    t.nud = function() {
      return { type: "Number", value: this.value };
    }, t.led = h(e, "ImplicitProduct", void 0, (n, r) => {
      var s, o;
      if (r.type === "Number" && n.type === "Number" && !((s = n.decorators) != null && s.length) && !((o = r.decorators) != null && o.length)) {
        e.advance("/");
        const l = e.expression(0);
        return {
          type: "MixedNumber",
          args: [n, r, l]
        };
      } else
        return null;
    });
  },
  registerVariable(e) {
    const t = e.registerSymbol("(name)", y);
    t.nud = function() {
      return { type: "Variable", value: this.value };
    }, t.led = h(e, "ImplicitProduct");
  },
  registerEquation(e) {
    const t = e.registerSymbol("=", _);
    t.led = (n) => ({ type: "Equation", args: [n, e.expression(_)] });
  },
  registerBrackets(e) {
    function t(n, r, s) {
      const o = e.registerSymbol(n, y);
      e.registerSymbol(r), o.nud = () => {
        const l = e.expression(0);
        return e.advance(r), s && (l.decorators ? l.decorators.push(s) : l.decorators = [s]), l;
      }, o.led = h(e, "ImplicitProduct");
    }
    t("{.", ".}", "CurlyBracket"), t("[.", ".]", "SquareBracket"), t("(", ")", "RoundBracket"), t("[", "]");
  },
  registerFraction(e) {
    const t = e.registerSymbol("/", 1);
    t.led = (n) => {
      const r = e.expression(0);
      return { type: "Fraction", args: [n, r] };
    };
  },
  registerExponent(e) {
    const t = e.registerSymbol("^", 1);
    t.led = function(n) {
      const r = e.expression(0);
      return { type: "Power", args: [n, r] };
    };
  },
  registerRoots(e) {
    const t = e.registerSymbol("sqrt", y);
    t.nud = () => {
      e.advance("[");
      const r = e.expression(0);
      return e.advance("]"), { type: "SquareRoot", args: [r] };
    }, t.led = h(e, "ImplicitProduct");
    const n = e.registerSymbol("root", y);
    n.nud = () => {
      e.advance("[");
      const r = e.expression(0);
      e.advance(",");
      const s = e.expression(0);
      return e.advance("]"), {
        type: "Root",
        args: [r, s]
      };
    }, n.led = h(e, "ImplicitProduct"), e.registerSymbol(",");
  }
};
function h(e, t, n, r) {
  return function(s) {
    var u;
    const o = t === "Sum" ? S : t === "ImplicitProduct" ? y : T;
    let l = t === "ImplicitProduct" ? e.expression(o, this) : e.expression(o);
    if (n && (l = { type: n, args: [l] }), r) {
      const c = r(s, l);
      if (c)
        return c;
    }
    return s.type === t && !((u = s.decorators) != null && u.length) ? { type: t, args: [...s.args || [], l] } : { type: t, args: [s, l] };
  };
}
function U(e, t = ".") {
  return "args" in e ? {
    ...e,
    path: t,
    args: e.args.map((n, r) => U(n, `${t}/${r}`))
  } : { ...e, path: t };
}
const W = new R(Object.values(J));
function Q(e) {
  return U(W.parse(e));
}
const X = {
  mulSymbol: " \\times ",
  divSymbol: " \\div "
};
function q(e, t, n) {
  return L(
    e,
    { ...X, ...t },
    n || ((r, s) => s)
  );
}
function L(e, t, n) {
  const r = (o) => L(o, t, n), s = (o) => n(e, v(o, e.decorators));
  switch (e.type) {
    case "Number":
      return n(e, v(K(e), e.decorators));
    case "Variable":
      return n(e, v(e.value, e.decorators));
    case "Sum":
      return s(
        e.args.map(
          (o, l) => {
            var u;
            return o.type === "Minus" && !((u = o.decorators) != null && u.length) ? (
              // binary minus
              `-${r(o.args[0])}`
            ) : (
              // binary plus
              l === 0 ? r(o) : `+${r(o)}`
            );
          }
        ).join("")
      );
    case "Plus":
      return s(`+${r(e.args[0])}`);
    case "Minus":
      return s(`-${r(e.args[0])}`);
    case "Product":
      return s(
        e.args.map(
          (o, l) => l == 0 || o.type === "DivideBy" ? r(o) : `${t.mulSymbol}${r(o)}`
        ).join("")
      );
    case "ImplicitProduct":
      return s(e.args.map((o) => r(o)).join(""));
    case "DivideBy":
      return `${t.divSymbol}${r(e.args[0])}`;
    case "Fraction":
      return s(`\\frac{${r(e.args[0])}}{${r(e.args[1])}}`);
    case "MixedNumber":
      return s(`${r(e.args[0])}\\frac{${r(e.args[1])}}{${r(e.args[2])}}`);
    case "Power":
      return s(`{${r(e.args[0])}}^{${r(e.args[1])}}`);
    case "SquareRoot":
      return s(`\\sqrt{${r(e.args[0])}}`);
    case "Root":
      return s(`\\sqrt[${r(e.args[1])}]{${r(e.args[0])}}`);
    case "Equation":
      return s(`${r(e.args[0])} = ${r(e.args[1])}`);
    case "UNDEFINED":
      return s("\\text{undefined}");
    case "INFINITY":
      return s("\\infty");
    case "REALS":
      return s("\\mathbb{R}");
    case "LessThan":
      return s(`${r(e.args[0])} < ${r(e.args[1])}`);
    case "GreaterThan":
      return s(`${r(e.args[0])} > ${r(e.args[1])}`);
    case "LessThanEqual":
      return s(`${r(e.args[0])} \\leq ${r(e.args[1])}`);
    case "GreaterThanEqual":
      return s(`${r(e.args[0])} \\geq ${r(e.args[1])}`);
    case "Solution":
      return s(`${r(e.args[0])} \\in ${r(e.args[1])}`);
    case "FiniteSet":
      return s(
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
function K(e) {
  const [t, n] = e.value.split("[");
  return n !== void 0 ? `${t}\\overline{${n.slice(0, -1)}}` : t;
}
function v(e, t) {
  return t ? t.reduce((n, r) => r === "RoundBracket" ? `\\left(${n}\\right)` : r === "SquareBracket" ? `\\left[${n}\\right]` : r === "CurlyBracket" ? `\\left\\{${n}\\right\\}` : n, e) : e;
}
function ne(e) {
  return (t, n) => {
    const r = e[t.path];
    return r ? `{\\color{${r}}${n}}` : n;
  };
}
function se(e, t, n = !0) {
  const r = {}, s = {};
  n && (e = e.filter((o) => o.type !== "Shift"));
  for (const [o, l] of e.entries()) {
    const u = t[o % t.length];
    for (const c of l.fromPaths)
      r[c] = u;
    for (const c of l.toPaths)
      s[c] = u;
  }
  return [r, s];
}
function oe(e, t) {
  return q(Q(e), t);
}
function ie(e, t) {
  return q(B(e), t);
}
function w(e) {
  return e.length === 0 ? "." : `./${e.join("/")}`;
}
function le(e) {
  return e === "." ? [] : (e.startsWith("./") && (e = e.substring(2)), e.split("/").map((t) => +t));
}
function ae(e, t, n, r = !0) {
  function s(o, l, u = 0) {
    var p;
    if (n.length === u)
      return r ? {
        ...P(l, w(n)),
        ...(p = o.decorators) != null && p.length ? { decorators: o.decorators.slice() } : null
      } : P(l, w(n));
    const c = n[u];
    if (!("args" in o) || c >= o.args.length)
      throw new Error("Invalid path");
    return {
      ...o,
      args: o.args.map(($, f) => f === c ? s($, l, u + 1) : $)
    };
  }
  return s(e, t);
}
function P(e, t = "") {
  return {
    ...e,
    path: t + e.path.slice(1),
    ..."args" in e ? { args: e.args.map((n) => P(n, t)) } : null
  };
}
export {
  te as api,
  w as arrayToPath,
  ne as coloringTransformer,
  se as createColorMaps,
  ie as jsonToLatex,
  B as jsonToTree,
  re as latexToSolver,
  le as pathToArray,
  oe as solverToLatex,
  Q as solverToTree,
  ae as substituteTree,
  G as treeToJson,
  q as treeToLatex
};
