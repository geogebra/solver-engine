<script setup lang="ts">
import { computed } from 'vue';
import { getExplanationString } from './render-solution';
import type { Metadata } from '@geogebra/solver-sdk';
import { hideWarnings, showTranslationKeys } from './settings';
import KatexRenderedText from './katex-rendered-text.vue';

const props = defineProps<{ metadata: Metadata | null }>();

const explanationInfo = computed(() =>
  props.metadata
    ? getExplanationString(props.metadata)
    : { explanationString: undefined, warnings: undefined },
);

const copyToClipboard = async () => {
  const textContent = props.metadata?.key;
  if (textContent) await navigator.clipboard.writeText(textContent);
};
</script>

<template>
  <div v-if="metadata" class="plan-explanation">
    <div class="translation-key" v-show="showTranslationKeys" @click="copyToClipboard">
      {{ metadata.key }}
    </div>
    <KatexRenderedText
      v-if="explanationInfo.explanationString"
      :title="metadata.key"
      :text="explanationInfo.explanationString"
    ></KatexRenderedText>
    <div v-for="content in explanationInfo.warnings || []" v-show="!hideWarnings" class="warning">
      {{ content }}
    </div>
  </div>
</template>
