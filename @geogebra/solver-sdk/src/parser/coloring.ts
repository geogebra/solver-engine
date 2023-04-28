import { PathMapping } from '../types';
import { ColorLatexTransformer, LatexTransformer } from './tree-to-latex';

/**
 * An object that maps path strings to color values. The ColorMap
 * type is a dictionary where keys are path strings and values
 * are color strings.
 */
export type ColorMap = { [path: string]: string };

/**
 * This function creates and returns an instance of the ColorLatexTransformer
 * class, which implements the LatexTransformer interface. The `colorMap`
 * parameter is an object that maps path strings to color values. The resulting
 * LatexTransformer object can be used to transform expressions in an
 * expression tree by applying colors to specific nodes in the tree.
 *
 * @param colorMap (required): A ColorMap object
 * @param defaultTextColor (optional, default="black"): default text color to use
 * @returns A LatexTransformer object that can be used to transform
 * expression trees with colors applied to specific nodes.
 */
export function coloringTransformer(
  colorMap: ColorMap,
  defaultTextColor = 'black',
): LatexTransformer {
  return new ColorLatexTransformer(colorMap, defaultTextColor);
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
