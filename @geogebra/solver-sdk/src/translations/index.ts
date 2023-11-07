import { defaultMathWords, MathWords } from '../renderer';

const mathWordCategory = 'MathWord';
const defaultTranslationsRootURL = 'https://export-solver.s3.eu-west-1.amazonaws.com';

export type TranslationsMap = Record<string, string>;

/**
 * Fetch the translation map from the location containing translations for the given locale and return it.
 * @param locale optional code for the locale to fetch (default 'en')
 * @param translationsRootURL optional root URL of translation files (defaults to the official one)
 * @param mode optional request mode (default 'cors')
 * @param cache optional request cache mode (default 'no-cache' for safety, can probably be relaxed)
 */
export async function fetchTranslationsMap(
  locale = 'en',
  translationsRootURL = defaultTranslationsRootURL,
  mode: RequestMode = 'cors',
  cache: RequestCache = 'no-cache',
): Promise<TranslationsMap> {
  const rawResp = await fetch(
    `${translationsRootURL}/${locale}/method-translations.json`,
    {
      mode,
      cache,
    },
  );
  return await rawResp.json();
}

/**
 * Build a MathWords instance from the given translation map and return it.  This instance is suitable for passing to
 * functions rendering math expressions. @param map
 */
export function getMathWords(map: TranslationsMap): MathWords {
  const words = { ...defaultMathWords };
  let key: keyof MathWords;
  for (key in words) {
    const translationKey = `${mathWordCategory}.${key}`;
    if (translationKey in map) {
      words[key] = map[translationKey];
    }
  }
  return words;
}
