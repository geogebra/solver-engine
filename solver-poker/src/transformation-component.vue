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
    <Explanation :metadata="transformation.explanation" :formula="transformation.formula" />
    <KatexRenderedText
      class="expr"
      :text="renderExpressionMapping(transformation, colorMaps.fromColoring, colorMaps.toColoring)"
    />
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
