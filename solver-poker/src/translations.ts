// Holds all default translations as a key: translation map
export let translationData: Record<string, string> = {};

const translationsRootURL = 'https://export-solver.s3.eu-west-1.amazonaws.com';

export const fetchDefaultTranslations = () =>
  fetch(`${translationsRootURL}/en/method-translations.json`, {
    mode: 'cors',
    cache: 'no-cache',
  })
    .then((resp) => resp.json())
    .then((jsonResp) => {
      translationData = jsonResp;
    });
