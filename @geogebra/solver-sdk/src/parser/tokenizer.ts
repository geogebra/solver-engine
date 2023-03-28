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
  const r_symbols = /^\s*(UNDEFINED|REALS|\\text\{[Uu]ndefined\}|\\mathbb\{R\})/;
  const r_number = /^\s*((([0-9]*(\.[0-9]*|[0-9]+))([eE][-+]?[0-9]+)?)|∞|Infinity)/;
  const r_match = new RegExp('^\\s*\\{(\\??[A-Za-z0-9_]*)\\:'); // named regexp match
  const r_name = new RegExp('^\\s*([a-zA-Z₀₁₂₃₄₅₆₇₈₉⬚])'); // single-character names
  const r_func = new RegExp('^\\s*(sqrt|root|Solution)');
  const r_greek = new RegExp('^\\s*([α-ωΑ-Ω])'); // single-character greek letters
  const r_long_name = new RegExp('^\\s*"([^"]+)"'); // multi-character strings "..."
  const r_long_name_latex = new RegExp('^\\s*\\\\text{(.*?)}'); // multi-character strings "\\text{...}"
  const r_operator =
    /^\s*(\[\.|\.\]|\{\.|\.\}|>=|<=|\*\*|[-–+×*/÷=><();,^{}[\]|_±⁰¹²³⁴⁵⁶⁷⁸⁹ⁿ:≥≤√])/;
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
        value: m[1].replace(/\\text\{[Uu]ndefined\}/, 'UNDEFINED'),
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