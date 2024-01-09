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
            :method-id="selection.metadata.methodId"
          ></TestSuggestion>
        </div>
      </li>
    </ol>
  </div>
</template>
