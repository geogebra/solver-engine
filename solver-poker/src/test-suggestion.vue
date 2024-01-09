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
    <summary>
      Test Code
      <button @click="copyToClipboard">Copy</button>
    </summary>
    <pre>{{ testSuggestion }}</pre>
  </details>
</template>
