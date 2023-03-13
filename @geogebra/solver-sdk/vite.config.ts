import path from 'path';
import { defineConfig, normalizePath } from 'vite';
import { viteStaticCopy } from 'vite-plugin-static-copy';

import dns from 'dns';
// print address as localhost, not 127.0.0.1 because this is what the solver API
// currently allows
dns.setDefaultResultOrder('verbatim');

export default defineConfig({
  build: {
    lib: {
      entry: toAbsolutePath('src/index.ts'),
      name: 'ggbSolver',
      fileName: (format) => `solver-sdk.${format}.js`,
    },
    outDir: 'dist',
  },
  plugins: [
    // Copy the solver-sdk bundle into the place where Poker can use it, after every time
    // that vite builds it.
    viteStaticCopy({
      targets: [
        {
          src: toAbsolutePath('dist/solver-sdk.es.js'),
          dest: toAbsolutePath('../../api/src/main/resources/static'),
        },
      ],
    }),
  ],
});

function toAbsolutePath(relativePath: string) {
  // `normalizePath()` is used because
  // https://github.com/sapphi-red/vite-plugin-static-copy/blob/main/README.md#usage
  // suggested it.
  return normalizePath(path.resolve(__dirname, relativePath));
}
