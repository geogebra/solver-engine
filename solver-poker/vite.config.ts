import path from 'path';
import { defineConfig, normalizePath } from 'vite';
import vue from '@vitejs/plugin-vue';

import dns from 'dns';
// print address as localhost, not 127.0.0.1 because this is what the solver API
// currently allows
dns.setDefaultResultOrder('verbatim');

export default defineConfig({
  plugins: [vue()],
  build: {
    outDir: toAbsolutePath('../api/src/main/resources/static/poker'),
    assetsDir: '.',
    emptyOutDir: true,
  },
  base: './',
});

function toAbsolutePath(relativePath: string) {
  // `normalizePath()` is used because
  // https://github.com/sapphi-red/vite-plugin-static-copy/blob/main/README.md#usage
  // suggested it.
  return normalizePath(path.resolve(__dirname, relativePath));
}
