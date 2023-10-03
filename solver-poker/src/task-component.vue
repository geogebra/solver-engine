<script setup lang="ts">
import { onUpdated, ref, computed } from 'vue';
import { renderExpression } from './render-solution';
import { showThroughSteps } from './settings';
import * as solverSdk from '@geogebra/solver-sdk';
import type { TaskJson } from '@geogebra/solver-sdk';
import { jsonToTree } from '@geogebra/solver-sdk';
import { latexSettings } from './settings';
import Explanation from './explanation.vue';
import TasksComponent from './tasks-component.vue';
import StepsComponent from './steps-component.vue';
import KatexRenderedText from './katex-rendered-text.vue';

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
    `${solverSdk.treeToLatex(fromExpr, latexSettings.value)}
      {\\color{#8888ff}\\thickspace\\longmapsto\\thickspace}
      ${solverSdk.treeToLatex(toExpr, latexSettings.value)}`
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
      :text="renderExpression(solverSdk.treeToLatex(startExprTree, latexSettings))"
    ></KatexRenderedText>
  </div>
</template>
