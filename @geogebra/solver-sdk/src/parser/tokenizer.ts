/**
 * Analyses the passed string and returns an array of tokens
 */

export type Token = {
  type: 'name' | 'number' | 'operator' | 'latex' | 'symbol';
  value: string | number;
  from: number; // index of first character
  to: number; // index of last character+1
};

/// a latex command is either a backslash plus a single non-alpha-numeric character
/// like \, or is a backslash plus a string of alphabetic characters
export function tokenize(str: string): Token[] {
  const r_symbols =
    /^\s*(\/undefined\/|\/reals\/|\\text\{[Uu]ndefined\}|\\mathbb\{R\}|ℯ|\\mathrm\{e\}|ί|\\mathrm{i}|\\iota|\\pi(?:\{\})?)/;
  const r_number = /^\s*((([0-9]*(\.[0-9]*|[0-9]+))([eE][-+]?[0-9]+)?)|∞|Infinity)/;
  const r_match = new RegExp('^\\s*\\{(\\??[A-Za-z0-9_]*)\\:'); // named regexp match
  const r_name = new RegExp('^\\s*([a-zA-Z₀₁₂₃₄₅₆₇₈₉⬚])'); // single-character names
  const r_func = new RegExp('^\\s*(sqrt|root|Solution)');
  // this currently handles:
  // * {\mathrm{sin}} (or any other trigonometric function)
  // * {{\mathrm{\mathrm{ln}}}\left(x\right)} (nested natural-log)
  const r_nested_mathrm_with_braces = new RegExp(
    '^\\s*{\\\\mathrm{(\\\\mathrm{[^}]+}|[^}]+)}}',
  );
  // this doesn't include the opening and closing `{}` pair around it
  // we only want to include selected functions, as we \mathrm{2} shouldn't be tokenize here
  const r_trig_and_log_mathrm = new RegExp(
    '^\\s*\\\\mathrm{(' +
      'sin|cos|tan|cot|csc|sec|' + // Trigonometric functions
      'arcsin|arccos|arctan|arccot|arccsc|arcsec|' + // inverse trig functions
      'sinh|cosh|tanh|coth|csch|sech|' + // Hyperbolic functions
      'arsinh|arcosh|artanh|arcoth|arcsch|arsech|' + // inverse Hyperbolic functions
      'ln|log' + // Logarithmic functions
      ')}',
  );
  const r_greek = new RegExp('^\\s*([α-ωΑ-Ω])'); // single-character greek letters

  // we continue to have "iota" and "pi" here,
  // though they are currently tokenized as "symbols"
  // not as "variables"
  const r_greek_latex_cmds = new RegExp(
    '^\\s*(\\\\(alpha|beta|gamma|delta|epsilon|zeta|eta|theta|iota|kappa|lambda|mu|nu|xi|omicron|pi|rho|sigma|tau|upsilon|phi|chi|psi|omega|Alpha|Beta|Gamma|Delta|Epsilon|Zeta|Eta|Theta|Iota|Kappa|Lambda|Mu|Nu|Xi|Omicron|Pi|Rho|Sigma|Tau|Upsilon|Phi|Chi|Psi|Omega)(?:\\{\\})?)',
  ); // greek alphabet with optional `{}` at the end (as returned by the keyboard)
  const r_long_name = new RegExp('^\\s*"([^"]+)"'); // multi-character strings "..."
  const r_long_name_latex = new RegExp('^\\s*\\\\text{(.*?)}'); // multi-character strings "\\text{...}"
  const r_operator =
    /^\s*(\[\.|\.\]|\{\.|\.\}|>=|<=|\*\*|[-–+×*/÷=><();,^{}[\]|_±⁰¹²³⁴⁵⁶⁷⁸⁹ⁿ:≥≤√]|\\left\||\\right\||\|)/;
  const r_latex_command = new RegExp('^\\s*(\\\\[^A-Za-z0-9\\s]|\\\\[A-Za-z]+)');
  const r_whitespace = new RegExp('^(\\s+)');

  const tokens: Token[] = [];
  const l = str.length;

  for (let i = 0; i < l; ) {
    // this isn't super performant, but using lastIndex to make the regexp start
    // searching at a certain point does not work in combination with aligning
    // the regexp to the start of the string using ^...
    const s = str.substring(i);
    let m;
    if ((m = r_whitespace.exec(s))) {
      i += m[0].length;
    } else if ((m = r_symbols.exec(s))) {
      tokens.push({
        type: 'symbol',
        value: m[1].replace(/\\text\{[Uu]ndefined\}/, '/undefined/').replace(/\{\}$/, ''),
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_number.exec(s))) {
      // the token is a number
      tokens.push({
        type: 'number',
        value: m[1].replace('∞', 'Infinity'),
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_match.exec(s))) {
      // the token is a named regular expression
      tokens.push({ type: 'operator', value: '{', from: i, to: i + 1 });
      tokens.push({ type: 'name', value: m[1], from: i + 1, to: i + m[1].length + 1 });
      tokens.push({
        type: 'operator',
        value: ':',
        from: i + m[0].length - 1,
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_func.exec(s))) {
      // the token is a function
      tokens.push({
        type: 'operator',
        value: m[1], // will be something like "sin"
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_name.exec(s))) {
      // the token is a name
      tokens.push({
        type: 'name',
        value: m[1],
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_greek.exec(s))) {
      // the token is a greek letter
      tokens.push({
        type: 'name',
        value: m[1],
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_greek_latex_cmds.exec(s))) {
      tokens.push({
        type: 'name',
        value: m[1].replace(/\{\}$/, ''),
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_long_name.exec(s))) {
      // the token is a long name
      tokens.push({
        type: 'name',
        value: m[1],
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_long_name_latex.exec(s))) {
      // the token is a long name based on a latex \\text element
      const from = i + m.index + m[0].indexOf(m[1]);
      tokens.push({ type: 'name', value: m[1], from: from, to: from + m[1].length });
      i += m[0].length;
    } else if ((m = r_nested_mathrm_with_braces.exec(s))) {
      // the token is a trigonometric function using \\mathrm
      tokens.push({
        type: 'latex',
        value: m[0],
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_trig_and_log_mathrm.exec(s))) {
      // the token is a trigonometric function using \\mathrm
      tokens.push({
        type: 'latex',
        value: m[0],
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_operator.exec(s))) {
      // the token is an operator
      tokens.push({
        type: 'operator',
        value: m[1],
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else if ((m = r_latex_command.exec(s))) {
      // the token is an operator (based on a latex command)
      tokens.push({
        type: 'latex',
        value: m[1],
        from: i + (m[0].length - m[1].length),
        to: i + m[0].length,
      });
      i += m[0].length;
    } else {
      throw "can't tokenize '" + s + "'";
    }
  }
  return tokens.filter((t) => t.value !== '\\left' && t.value !== '\\right');
}
