/** Takes a solver path of the format like this './2/1' and turns it into an
 * array like [2,1]. */
export function pathToArray(path: string): number[] {
  if (path === '.') return [];
  if (path.startsWith('./')) path = path.substring(2);
  return path.split('/').map((el) => +el);
}
