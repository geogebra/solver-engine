<!--
  - Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
  - This file is part of GeoGebra
  -
  - The GeoGebra source code is licensed to you under the terms of the
  - GNU General Public License (version 3 or later)
  - as published by the Free Software Foundation,
  - the current text of which can be found via this link:
  - https://www.gnu.org/licenses/gpl.html ("GPL")
  - Attribution (as required by the GPL) should take the form of (at least)
  - a mention of our name, an appropriate copyright notice
  - and a link to our website located at https://www.geogebra.org
  -
  - For further details, please see https://www.geogebra.org/license
  -
  -->

<script setup lang="ts">
import { computed } from 'vue';
import { getExplanationString } from './render-solution';
import type { MappedExpression, Metadata } from '@geogebra/solver-sdk';
import { hideWarnings, showTranslationKeys } from './settings';
import KatexRenderedText from './katex-rendered-text.vue';

const props = defineProps<{
  metadata: Metadata | null;
  formula?: MappedExpression;
}>();

const explanationInfo = computed(() =>
  props.metadata
    ? getExplanationString(props.metadata, props.formula)
    : { explanationString: undefined, warnings: undefined },
);

const copyToClipboard = async () => {
  const textContent = props.metadata?.key;
  if (textContent) await navigator.clipboard.writeText(textContent);
};
</script>

<template>
  <div v-if="metadata" class="plan-explanation">
    <div v-show="showTranslationKeys" class="translation-key" @click="copyToClipboard">
      {{ metadata.key }}
    </div>
    <KatexRenderedText
      v-if="explanationInfo.explanationString"
      :title="metadata.key"
      :text="explanationInfo.explanationString"
    />
    <KatexRenderedText
      v-for="warning in explanationInfo.warnings || []"
      v-show="!hideWarnings"
      :text="warning"
      class="warning"
    />
  </div>
</template>
