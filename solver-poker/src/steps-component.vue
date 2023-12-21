<script setup lang="ts">
import { computed } from 'vue';
import type { TransformationJson } from '@geogebra/solver-sdk';
import { showCosmeticSteps, showInvisibleChangeSteps, showPedanticSteps } from './settings';
import { isCosmeticTransformation, isInvisibleChangeStep, isPedanticTransformation } from './util';
import TransformationComponent from './transformation-component.vue';

const props = defineProps<{
  steps?: TransformationJson[];
  depth: number;
  open: boolean;
}>();

const filteredSteps = computed(() => {
  if (showInvisibleChangeSteps.value) {
    return props.steps || [];
  }
  return (
    props.steps?.filter(
      (step) =>
        !isInvisibleChangeStep(step) &&
        (!isPedanticTransformation(step) || showPedanticSteps.value),
    ) || []
  );
});
</script>

<template>
  <details v-if="steps && steps.length > 0" class="steps" :open="open">
    <summary>
      {{ filteredSteps.length }} {{ filteredSteps.length === 1 ? 'step' : 'steps' }}
    </summary>
    <ol>
      <template v-for="step in filteredSteps">
        <span class="note" v-if="!showCosmeticSteps && isCosmeticTransformation(step)">
          The expression has been rewritten in a normalized form
        </span>
        <li v-else>
          <TransformationComponent
            :transformation="step"
            :depth="depth - 1"
          ></TransformationComponent>
        </li>
      </template>
    </ol>
  </details>
</template>
