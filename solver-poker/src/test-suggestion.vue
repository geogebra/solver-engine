<script setup lang="ts">
import { computed } from 'vue';
import type { TransformationJson } from '@geogebra/solver-sdk';
import { generateTestSuggestion } from './render-test';
import { demoMode } from './settings';

const props = defineProps<{
  transformation: TransformationJson;
  methodId: string;
}>();

const testSuggestion = computed(() => generateTestSuggestion(props.transformation, props.methodId));

const copyToClipboard = async () => {
  const text = testSuggestion.value;
  if (text) await navigator.clipboard.writeText(text);
};
</script>

<template>
  <details v-show="!demoMode" class="test-code">
    <summary>Test Code <button @click="copyToClipboard">Copy</button></summary>
    <pre>{{ testSuggestion }}</pre>
  </details>
</template>
