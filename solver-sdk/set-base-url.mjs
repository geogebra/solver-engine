/** This script is called by the CI script to adjust the API base URL the SDK is
 * using. To use this script, pass the new URL as the first argument. */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { argv } from 'node:process';

const baseUrl = argv[2];

if (!baseUrl) {
  console.error('Error: Need to pass API url for the SDK as an argument to this script.');
} else {
  console.log('Setting API url to', baseUrl);
  const __dirname = fileURLToPath(path.dirname(import.meta.url));
  // do this, but use node instead
  const apiPath = path.join(__dirname, './src/api.ts');
  const api = fs.readFileSync(apiPath, 'utf8');
  const newApi = api.replace(/const BASE_URL = .*/, `const BASE_URL = '${baseUrl}';`);
  fs.writeFileSync(apiPath, newApi, 'utf8');
}
