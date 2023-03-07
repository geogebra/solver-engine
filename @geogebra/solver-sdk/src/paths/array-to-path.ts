/** Takes an array of indices and turns it into a solver path string. E.g., [2,1] ==> './2/1'. */
export function arrayToPath(path: number[]): string {
  if (path.length === 0) return '.';
  return `./${path.join('/')}`;
}
