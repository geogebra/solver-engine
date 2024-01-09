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
import { onMounted, onUpdated, ref } from 'vue';

defineProps<{ text: string }>();

const rootElement = ref<HTMLSpanElement | null>(null);

const runKatex = () => {
  // Do this after each time after the DOM is updated, because this needs to work with an
  // up-to-date DOM, and change it.
  window.renderMathInElement?.(rootElement.value!);
};
onUpdated(runKatex);
onMounted(runKatex);
</script>

<template>
  <div ref="rootElement">{{ text }}</div>
</template>
