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
        <span v-if="!showCosmeticSteps && isCosmeticTransformation(step)" class="note">
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
