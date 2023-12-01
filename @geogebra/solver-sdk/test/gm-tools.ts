import * as path from 'path';
import requireFromUrl from 'require-from-url/sync';
import { config } from 'dotenv';
import { JSDOM } from 'jsdom';
import { expect, it } from 'vitest';

export async function loadGmEnvironment() {
  global.it = it;
  // Make it so values like `process.env.GMATH_PATH` are populated from the `.env` file, if
  // you have one, which you don't need to.
  config({ path: path.resolve(__dirname, '../.env') });
  expect(typeof document).to.not.equal(
    'undefined',
    'This failure is caused by not running vitest with the `--environment jsdom` option. If you ran the command from the @graspable/solver-sdk directory, then this is done for you automatically, via the config in @geogebra/solver-sdk/vite.config.ts . Otherwise, you can just add `--environment jsdom` onto the command you ran. `jsdom` is a tool that can stub things like `document` into the nodejs environment. `document` is needed by the GM library.',
  );
  const dom = new JSDOM(
    /* HTML */ ` <script>
        window.gmathPromise = new Promise((resolve, reject) => {
          window.gmathPromiseResolve = resolve;
        });
      </script>
      <script src="${process.env.GMATH_PATH ??
        'https://graspablemath.com/shared/libs/gmath-dist/gmath-3.0.2.min.js'}"></script>
      <script>
        window.gmathPromiseResolve(window.gmath);
      </script>`,
    { runScripts: 'dangerously', resources: 'usable', url: import.meta.url },
  );
  const gmath = await dom.window.gmathPromise;
  global.gmath = gmath;
  let when;
  const { WHEN_FOR_PATH } = process.env;
  if (WHEN_FOR_PATH) {
    when = (await import(WHEN_FOR_PATH)).when;
  } else {
    when = requireFromUrl(
      'https://graspablemath.com/shared/libs/gmath-dist/when-for-actions-latest.js',
    ).when;
  }
  return { gmath, when };
}
