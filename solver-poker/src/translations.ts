/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

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
