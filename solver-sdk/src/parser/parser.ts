/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

import type { Token } from './tokenizer';
import { tokenize } from './tokenizer';

// Adapted from:
//   From Top Down Operator Precedence
//   http://javascript.crockford.com/tdop/index.html
//   Douglas Crockford
//   2010-06-26

function error(message: string, object: any = undefined): never {
  throw new Error(
    `SyntaxError: ${message}${
      object === undefined
        ? ''
        : `
${JSON.stringify(object, null, 2)}`
    }`,
  );
}

export abstract class ParserSymbol<T> {
  id: string;
  value: string;
  lbp: number;

  constructor(id: string, lbp: number) {
    this.id = this.value = id;
    this.lbp = lbp;
  }

  abstract nud: () => T;
  abstract led: (left: T) => T;
}

class ParserSymbolBase<T> extends ParserSymbol<T> {
  nud = () => error(`Undefined nud for ${this.id}`, this);
  led = () => error(`Undefined led for ${this.id}`, this);
}

class Scope<T> {
  def: { [tokenType: string]: any } = {};
  symbolTable: { [key: string]: ParserSymbol<T> };

  constructor(symbolTable: { [key: string]: ParserSymbol<T> }) {
    this.symbolTable = symbolTable;
  }

  find_or_define(tok: Token) {
    let o = this.def[tok.value];
    //if (!o || typeof o == 'function') o = symbolTable[tok.value];
    if (o && typeof o !== 'function') return o; // check !== function, because {} has e.g. 'toString'
    o = Object.create(this.symbolTable['(name)']);
    o.lpb = 0;
    this.def[tok.value] = o;
    return o;
  }
}

/// T is the return type of the nud() and led() functions of the ParserSymbols.
export class Parser<T> {
  symbolTable: { [key: string]: ParserSymbol<T> } = {};
  token: (ParserSymbol<T> & Partial<Token>) | null = null;
  tokens: Token[] = [];
  token_nr = 0;
  scope: Scope<T> = new Scope(this.symbolTable);
  balancingTokenIds: string[] = [];

  constructor(symbols: ((parser: Parser<T>) => void)[]) {
    this.registerSymbol('(end)');
    symbols.forEach((symbol) => symbol(this));
  }

  error(message: string, object: any = undefined): never {
    error(message, object);
  }

  /// Read and return the next token.
  /// If `id` is passed, the method will throw an error if the current token
  /// has not the same (expected) id. If `optional` is passed, the method will
  /// only advance to the next token if the current token has the same id as
  /// the passed one. In case the id differs, the method just returns without
  /// doing anything.
  advance(id?: string, optional = false) {
    if (id && this.token?.id !== id) {
      if (optional) return;
      else this.error("Expected '" + id + "'.", this.token);
    }

    if (this.token_nr >= this.tokens.length) {
      this.token = this.symbolTable['(end)'];
      return;
    }

    const t = this.tokens[this.token_nr];
    this.token_nr += 1;

    const v = t.value;
    const a = t.type;

    let o: ParserSymbol<T>;
    if (a === 'name') {
      o = this.scope.find_or_define(t);
    } else if (a === 'operator') {
      o = this.symbolTable[v];
      if (!o) error(`Unknown operator "${v}".`, t);
    } else if (a === 'latex') {
      o = this.symbolTable[v];
      if (!o) error(`Unknown latex command "${v}".`, t);
    } else if (a === 'number') {
      o = this.symbolTable['(number)'];
    } else if (a === 'symbol') {
      o = this.symbolTable[v] ?? this.symbolTable['(symbol)'];
    } else {
      this.error('Unexpected token.', t);
    }
    if (!o) this.error("Couldn't find token. Testing semantic-release", t);

    const token = Object.create(o);
    token.from = t.from;
    token.to = t.to;
    token.value = v;
    token.type = a;
    this.token = token;
    return this.token;
  }

  /// If a token is passed as second parameter, no initial advance() is called.
  expression(rbp: number, _token: ParserSymbol<T> | null = null): T {
    if (!this.token) this.error('Parser error: Missing token.');
    let t = _token || this.token;
    if (!_token) this.advance();
    let left = t!.nud();
    while (rbp < this.token.lbp && !this.currentTokenIsBalancing()) {
      t = this.token;
      this.advance();
      left = t.led(left);
    }
    return left;
  }

  currentTokenIsBalancing(): boolean {
    const sz = this.balancingTokenIds.length;
    return sz > 0 && this.balancingTokenIds[sz - 1] === this.token?.id;
  }

  /// Parse a balanced subexpression, expecting a token with id balancingId to close the subexpression
  balancedExpression(balancingId: string) {
    this.balancingTokenIds.push(balancingId);
    const expr = this.expression(0);
    this.balancingTokenIds.pop();
    this.advance(balancingId);
    return expr;
  }

  /// Setup the symbol table (usually before the actual parsing).
  registerSymbol(id: string, bp = 0) {
    let s = this.symbolTable[id];
    if (s) {
      if (bp > s.lbp) s.lbp = bp;
    } else {
      s = new ParserSymbolBase(id, bp);
      this.symbolTable[id] = s;
    }
    return s;
  }

  parse(source: string) {
    this.tokens = tokenize(source);
    if (this.tokens.length === 0) throw 'Error: No tokens to parse.';
    this.scope = new Scope(this.symbolTable);
    this.token_nr = 0;
    this.advance();
    const e = this.expression(0);
    this.advance('(end)');
    return e;
  }
}
