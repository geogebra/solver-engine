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

import type {
  ExpressionTree,
  LatexTransformer,
  MathJson,
  Metadata,
  Transformation,
  TransformationJson,
} from '@geogebra/solver-sdk';
import * as solverSdk from '@geogebra/solver-sdk';
import { MappedExpression } from '@geogebra/solver-sdk';
import { colorScheme, colorSchemes } from './settings';
import { translationData } from './translations';
import { jsonToLatex, treeToLatex } from './render-math.ts';

const removeOuterBrackets = (expression: ExpressionTree) => ({ ...expression, decorators: [] });

export const renderExpressionMapping = (
  trans: TransformationJson,
  fromColoring?: LatexTransformer,
  toColoring?: LatexTransformer,
) => {
  const fromTree = removeOuterBrackets(solverSdk.jsonToTree(trans.fromExpr, trans.path));
  const toTree = removeOuterBrackets(solverSdk.jsonToTree(trans.toExpr, trans.path));
  const fromLatex = treeToLatex(fromTree, fromColoring);
  if (toTree.type === 'Void') {
    return renderExpression(fromLatex);
  } else {
    const toLatex = treeToLatex(toTree, toColoring);
    return renderExpression(
      `${fromLatex} {\\color{#8888ff}\\thickspace\\longmapsto\\thickspace} ${toLatex}`,
    );
  }
};

export const createColorMaps = (
  trans: Transformation,
): [LatexTransformer | undefined, LatexTransformer | undefined] => {
  // First deal with the special case that the whole expression is transformed.  In this case there is no need to color
  // the path mapping.
  if (trans.pathMappings.length === 1) {
    const mapping = trans.pathMappings[0];
    if (mapping.type === 'Transform' && mapping.fromPaths[0] === trans.path) {
      return [undefined, undefined];
    }
  }
  // Else proceed with finding appropriate colors for the path mappings.
  const colors = colorSchemes[colorScheme.value];
  if (colors) {
    const [fromCM, toCM] = solverSdk.createColorMaps(trans.pathMappings, colors);
    return [solverSdk.coloringTransformer(fromCM), solverSdk.coloringTransformer(toCM)];
  } else {
    return [undefined, undefined];
  }
};

export const getExplanationString = (expl: Metadata, formula?: MappedExpression) => {
  let explanationString = translationData.value[expl.key];
  const warnings = [];

  const parameters = expl.params || [];
  if (!explanationString) {
    warnings.push(`Missing default translation for ${expl.key}`);
    explanationString = `${expl.key}(${[...parameters.keys()].map((i) => `%${i + 1}`).join()})`;
  }

  for (const [i, param] of parameters.entries()) {
    // replacing "%1", "%2", ... with the respective rendered expression
    if (explanationString.includes('%' + (i + 1))) {
      explanationString = explanationString.replaceAll(
        '%' + (i + 1),
        renderExpression(param.expression),
      );
    } else {
      warnings.push(
        `Missing %${i + 1} in default translation, should contain ${renderExpression(
          param.expression,
        )}`,
      );
    }
  }

  if (formula) {
    if (explanationString.includes('%f')) {
      explanationString = explanationString.replaceAll('%f', renderExpression(formula.expression));
    } else {
      warnings.push(
        `Missing %f in default translation, should contain ${renderExpression(formula.expression)}`,
      );
    }
  }

  const unusedPlaceholders = explanationString.match(/%([1-9]|f)/g);
  if (unusedPlaceholders) {
    for (const placeholder of unusedPlaceholders) {
      warnings.push(`Missing parameter for placeholder ${placeholder}`);
    }
  }
  return { explanationString, warnings };
};

export const renderExpression = (expr: MathJson | string) =>
  `\\(\\displaystyle ${typeof expr === 'string' ? expr : jsonToLatex(expr)}\\)`;
