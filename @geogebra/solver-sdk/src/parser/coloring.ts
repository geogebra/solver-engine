import { PathMapping } from '../types';
import { ExpressionTree } from './types';

export type ColorMap = { [path: string]: string };

/**
 * Create a ColoringFunction out of a ColorMap, which is a mapping from
 * PathMappings to colors.
 *
 * @param colorMap the ColorMap to create a ColoringFunction out of
 * @returns a TransformerFunction that can be used in treeToLatex
 */
export function coloringTransformer(colorMap: ColorMap) {
  return (
    node: ExpressionTree,
    originalLatex: string,
    parent: ExpressionTree | null = null,
  ) => {
    const color = colorMap[node.path];
    if (!color) return originalLatex;
    let layoutCorrection = '';
    if (
      (parent?.type === 'Sum' || parent?.type === 'Product') &&
      parent?.args[0] !== node
    ) {
      // need this so that binary operators are shown with correct spacing:
      // Example: 1-2 ==> 1{\color{red}{}-2}
      layoutCorrection = '{}';
    }
    return `{\\color{${color}}${layoutCorrection}${originalLatex}}`;
  };
}

/**
 * Create a pair of ColorMap instances for the origin.
 *
 * This is a simple demo implementation. It currently doesn't take situations
 * into account where a term *and* one of it's sub-terms (e.g. 1/2 and 2) are
 * mapped to something. It also doesn't check whether the same term is mapped
 * multiple times. This will result in inconsistent colorings. Eventually, we'll
 * also want to send only a subset of mappings (the significant ones) to this
 * method to avoid too many colors.
 *
 * @param pathMappings list of PathMapping instances to color
 * @param colors list of colors to used to create the ColorMap instances.
 *        The list is cycled through if it is not long enough
 * @param ignoreShiftMappings if you do not want to color path mappings of
 * expressions that do not change, except to shift over
 * @returns two ColorMap instances, one to color the origins of the pathMappings
 *          and the other to color their destinations
 */
export function createColorMaps(
  pathMappings: PathMapping[],
  colors: string[],
  ignoreShiftMappings = true,
): [ColorMap, ColorMap] {
  const fromColorMap: ColorMap = {};
  const toColorMap: ColorMap = {};
  if (ignoreShiftMappings) {
    pathMappings = pathMappings.filter((mapping) => mapping.type !== 'Shift');
  }
  for (const [i, mapping] of pathMappings.entries()) {
    const color = colors[i % colors.length];
    for (const fromPath of mapping.fromPaths) {
      fromColorMap[fromPath] = color;
    }
    for (const toPath of mapping.toPaths) {
      toColorMap[toPath] = color;
    }
  }
  return [fromColorMap, toColorMap];
}
