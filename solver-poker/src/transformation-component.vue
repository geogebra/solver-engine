<script setup lang="ts">
import { onUpdated, ref, onMounted, computed } from 'vue';
import { isThroughStep } from './util';
import type { TransformationJson } from '@geogebra/solver-sdk';
import { createColorMaps, renderExpressionMapping } from './render-solution';
import { showThroughSteps } from './settings';
import Explanation from './explanation.vue';
import StepsComponent from './steps-component.vue';
import TasksComponent from './tasks-component.vue';
import KatexRenderedText from './katex-rendered-text.vue';

const props = defineProps<{
  transformation: TransformationJson;
  depth: number;
}>();

const colorMaps = computed(() => {
  const [fromColoring, toColoring] = createColorMaps(props.transformation);
  return { fromColoring, toColoring };
});
const isThrough = computed(() => isThroughStep(props.transformation));
const alternatives = computed(() => props.transformation.alternatives || []);
</script>

<template>
  <TransformationComponent
    v-if="!showThroughSteps && isThrough"
    :transformation="transformation.steps![0]"
    :depth="depth"
  />
  <div v-else :class="`trans ${isThrough ? 'through-step' : ''}`">
    <Explanation :metadata="transformation.explanation" />
    <KatexRenderedText
      class="expr"
      :text="renderExpressionMapping(transformation, colorMaps.fromColoring, colorMaps.toColoring)"
    ></KatexRenderedText>
    <StepsComponent :steps="transformation.steps" :depth="depth" :open="depth >= 0 || isThrough" />
    <TasksComponent :tasks="transformation.tasks" :depth="depth" :open="depth >= 0" />

    <!-- Alternatives section -->

    <details v-if="alternatives.length > 0" class="alternatives">
      <summary>
        {{
          alternatives.length === 1
            ? 'alternative method'
            : alternatives.length + ' alternative methods'
        }}
      </summary>
      <ol>
        <li v-for="alt in alternatives">
          <Explanation :metadata="alt.explanation" />
          <StepsComponent :steps="alt.steps" :depth="depth" :open="false" />
        </li>
      </ol>
    </details>
  </div>
</template>
