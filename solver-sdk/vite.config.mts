import path from 'path';
import { normalizePath } from 'vite';
import { defineConfig } from 'vitest/config';

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
    pool: 'threads',
    fileParallelism: false,
    maxWorkers: 1,
  },
});

function toAbsolutePath(relativePath: string) {
  // `normalizePath()` is used because
  // https://github.com/sapphi-red/vite-plugin-static-copy/blob/main/README.md#usage
  // suggested it.
  return normalizePath(path.resolve(import.meta.dirname, relativePath));
}
