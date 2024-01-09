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

import type { ExpressionTree, LatexTransformer } from '@geogebra/solver-sdk';
import { jsonToTree, MathJson, treeToLatex as sdkTreeToLatex } from '@geogebra/solver-sdk';
import { latexSettings } from './settings.ts';
import { mathWords } from './translations.ts';

/**
 * Renders an expression to LaTeX, using the global latex settings and math words.  Use this
 * instead of the SDK function in poker.
 * @param expr expression to render
 * @param transformer optional transformer (usually coloring transformer)
 */
export const treeToLatex = (expr: ExpressionTree, transformer?: LatexTransformer) =>
  sdkTreeToLatex(expr, latexSettings.value, transformer, mathWords.value);

/**
 * Convenience function that compiles the MathJSON expression and renders it to LaTeX.  Use this
 * instead of the SDK function in poker as it applies the global settings and math words.
 * @param json MathJSON representation of the expression
 * @param transformer optional transformer
 */
export const jsonToLatex = (json: MathJson, transformer?: LatexTransformer) =>
  treeToLatex(jsonToTree(json), transformer);
