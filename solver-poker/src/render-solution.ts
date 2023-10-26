import * as solverSdk from '@geogebra/solver-sdk';
import type {
  ExpressionTree,
  MathJson,
  Metadata,
  Transformation,
  TransformationJson,
  LatexTransformer,
} from '@geogebra/solver-sdk';
import { colorScheme, colorSchemes, latexSettings } from './settings';
import { translationData } from './translations';

const removeOuterBrackets = (expression: ExpressionTree) => ({ ...expression, decorators: [] });

export const renderExpressionMapping = (
  trans: TransformationJson,
  fromColoring?: LatexTransformer,
  toColoring?: LatexTransformer,
) => {
  const fromTree = removeOuterBrackets(solverSdk.jsonToTree(trans.fromExpr, trans.path));
  const toTree = removeOuterBrackets(solverSdk.jsonToTree(trans.toExpr, trans.path));
  const fromLatex = solverSdk.treeToLatex(fromTree, latexSettings.value, fromColoring);
  if (toTree.type === 'Void') {
    return renderExpression(fromLatex);
  } else {
    const toLatex = solverSdk.treeToLatex(toTree, latexSettings.value, toColoring);
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

export const getExplanationString = (expl: Metadata) => {
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
  const unusedPlaceholders = explanationString.match(/%[1-9]/g);
  if (unusedPlaceholders) {
    for (const placeholder of unusedPlaceholders) {
      warnings.push(`Missing parameter for placeholder ${placeholder}`);
    }
  }
  return { explanationString, warnings };
};

export const renderExpression = (expr: MathJson | string) =>
  `\\(\\displaystyle ${
    typeof expr === 'string' ? expr : solverSdk.jsonToLatex(expr, latexSettings.value)
  }\\)`;
