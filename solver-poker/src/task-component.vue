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
import { renderExpression } from './render-solution';
import { latexSettings, showThroughSteps } from './settings';
import type { TaskJson } from '@geogebra/solver-sdk';
import * as solverSdk from '@geogebra/solver-sdk';
import { jsonToTree } from '@geogebra/solver-sdk';
import Explanation from './explanation.vue';
import TasksComponent from './tasks-component.vue';
import StepsComponent from './steps-component.vue';
import KatexRenderedText from './katex-rendered-text.vue';
import { treeToLatex } from './render-math.ts';

const props = defineProps<{
  task: TaskJson;
  depth: number;
}>();

const startExprTree = computed(() => solverSdk.jsonToTree(props.task.startExpr));

const rendering = computed(() => {
  const fromExpr = jsonToTree(props.task.startExpr);
  const toExpr = jsonToTree(props.task.steps![props.task.steps!.length - 1].toExpr);

  return (
    solverSdk.specialTransformationLatex(fromExpr, toExpr, latexSettings.value) ||
    `${treeToLatex(fromExpr)}
      {\\color{#8888ff}\\thickspace\\longmapsto\\thickspace}
      ${treeToLatex(toExpr)}`
  );
});
</script>

<template>
  <div>
    <Explanation :metadata="task.explanation" />
    <template v-if="task.steps">
      <KatexRenderedText class="expr" :text="renderExpression(rendering)"></KatexRenderedText>
      <TasksComponent
        v-if="
          !showThroughSteps &&
          task.steps.length === 1 &&
          task.steps[0].tasks &&
          task.steps[0].explanation.key === task.explanation?.key
        "
        :tasks="task.steps[0].tasks"
        :depth="depth - 1"
        :open="depth >= 0"
      />
      <StepsComponent v-else :steps="task.steps" :depth="depth - 1" :open="depth >= 0" />
    </template>
    <KatexRenderedText
      v-else-if="startExprTree.type !== 'Void'"
      :text="renderExpression(treeToLatex(startExprTree))"
    ></KatexRenderedText>
  </div>
</template>
