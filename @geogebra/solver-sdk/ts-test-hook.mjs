/**
 * Overrides the tsconfig used for the app.
 * In the test environment we need some tweaks.
 */

import { register } from 'ts-node';
register({
  files: true,
  transpileOnly: true,
  project: './test/tsconfig.json',
});
