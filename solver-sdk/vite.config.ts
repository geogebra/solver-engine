/// <reference types="vitest" />
import path from 'path';
import { defineConfig, normalizePath } from 'vite';

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
  test: {
    include: ['test/**/*.test.ts'],
    // Needed to run the gmath library in a node environment
    environment: 'jsdom',
    reporters: ['dot'],
    singleThread: true,
  },
});

function toAbsolutePath(relativePath: string) {
  // `normalizePath()` is used because
  // https://github.com/sapphi-red/vite-plugin-static-copy/blob/main/README.md#usage
  // suggested it.
  return normalizePath(path.resolve(__dirname, relativePath));
}
