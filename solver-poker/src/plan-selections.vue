<script setup lang="ts">
import { computed } from 'vue';
import type { PlanSelectionJson } from '@geogebra/solver-sdk';
import { containsNonTrivialStep } from './util';
import TransformationComponent from './transformation-component.vue';
import TestSuggestion from './test-suggestion.vue';

const props = defineProps<{ solverResponse: PlanSelectionJson[] }>();
const selections = computed(() =>
  props.solverResponse.filter((selection) => containsNonTrivialStep(selection.transformation)),
);
</script>

<template>
  <div v-if="!selections || selections.length === 0" class="selections">No plans found</div>
  <div v-else class="selections">
    {{ selections.length }} plans found
    <ol>
      <li v-for="selection in selections">
        <div class="plan-selection">
          <div class="plan-id">{{ selection.metadata.methodId }}</div>
          <TransformationComponent
            :transformation="selection.transformation"
            :depth="0"
          ></TransformationComponent>
          <TestSuggestion
            :transformation="selection.transformation"
            :methodId="selection.metadata.methodId"
          ></TestSuggestion>
        </div>
      </li>
    </ol>
  </div>
</template>
