import { ref } from 'vue';
import type { MathWords, TranslationsMap } from '@geogebra/solver-sdk';
import { defaultMathWords, fetchTranslationsMap, getMathWords } from '@geogebra/solver-sdk';

// Holds all default translations as a key: translation map
export const translationData = ref<TranslationsMap>({});
export const mathWords = ref<MathWords>(defaultMathWords);
export const translationsFetched = ref(false);

export const fetchDefaultTranslations = () =>
  fetchTranslationsMap().then((map) => {
    translationData.value = map;
    mathWords.value = getMathWords(map);
    translationsFetched.value = true;
  });
