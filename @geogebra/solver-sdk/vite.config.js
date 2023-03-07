import path from 'path';
import { defineConfig } from 'vite';

import dns from 'dns';
// print address as localhost, not 127.0.0.1 because this is what the solver API
// currently allows
dns.setDefaultResultOrder('verbatim');

export default defineConfig({
  build: {
    lib: {
      entry: path.resolve(__dirname, 'src/index.ts'),
      name: 'ggbSolver',
      fileName: (format) => `solver-sdk.${format}.js`,
    },
    outDir: 'dist',
  },
});
