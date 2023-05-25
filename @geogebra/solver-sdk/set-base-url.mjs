import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { argv } from 'node:process';

const baseUrl = argv[2];

console.log('CI_COMMIT_REF_NAME', process.env.CI_COMMIT_REF_NAME);

if (!baseUrl) {
  console.error('Error: Need to pass API url for the SDK as an argument to this script.');
} else {
  const __dirname = fileURLToPath(path.dirname(import.meta.url));
  // do this, but use node instead
  const apiPath = path.join(__dirname, './src/api.ts');
  const api = fs.readFileSync(apiPath, 'utf8');
  const newApi = api.replace(/const BASE_URL = .*/, `const BASE_URL = '${baseUrl}';`);
  fs.writeFileSync(apiPath, newApi, 'utf8');
}
