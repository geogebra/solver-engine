import { ref } from 'vue';

// Holds all default translations as a key: translation map
export const translationData = ref<Record<string, string>>({});
export const translationsFetched = ref(false);

const translationsRootURL = 'https://export-solver.s3.eu-west-1.amazonaws.com';

export const fetchDefaultTranslations = () =>
  fetch(`${translationsRootURL}/en/method-translations.json`, {
    mode: 'cors',
    cache: 'no-cache',
  })
    .then((resp) => resp.json())
    .then((jsonResp) => {
      translationData.value = jsonResp;
      translationsFetched.value = true;
    });
