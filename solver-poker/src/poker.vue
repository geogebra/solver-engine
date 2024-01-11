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
import type {
  ApiMathFormat,
  GraphResponseJson,
  GraphResponseSolver,
  PlanSelectionJson,
  PlanSelectionSolver,
  ServerErrorResponse,
  SolverContext,
  TransformationJson,
  TransformationSolver,
} from '@geogebra/solver-sdk';
import * as solverSdk from '@geogebra/solver-sdk';
import { GgbAppletGrapher } from '@geogebra/solver-sdk';

import {
  colorScheme,
  demoMode,
  hideWarnings,
  jsonFormat,
  params,
  presets,
  settings,
  showCosmeticSteps,
  showInvisibleChangeSteps,
  showPedanticSteps,
  showThroughSteps,
  showTranslationKeys,
  solutionFormat,
} from './settings';
import TransformationComponent from './transformation-component.vue';
import PlanSelections from './plan-selections.vue';
import TestSuggestion from './test-suggestion.vue';
import { fetchDefaultTranslations, translationsFetched } from './translations';
import { computed, onMounted, reactive, ref, watch, watchEffect } from 'vue';
import { computedAsync } from '@vueuse/core';
import GgbApplet, { GgbAppletApi } from './GgbApplet.vue';

const jsonFormatSpecifier: ApiMathFormat = 'json2';

const mainPokerURL = 'https://solver.geogebra.net/main/poker/index.html';

const plans = computedAsync(() => solverSdk.api.listPlans());
const versionInfo = computedAsync(() => solverSdk.api.versionInfo());
const strategies = computedAsync(() => solverSdk.api.listStrategies());

/** Map of strategy category to preferred strategy. If no value is set, then nothing was
 * selected for that category. */
const mapOfCategoryToSelectedStrategy = reactive<{
  [category: string]: string;
}>({});
// populate that map with the values from the URL
for (const strategyChoice of [params.strategy || []].flat()) {
  const [category, choice] = strategyChoice.split(':');
  mapOfCategoryToSelectedStrategy[category] = choice;
}
// watcher updates the URL when that map changes
watch(
  mapOfCategoryToSelectedStrategy,
  () => {
    params.strategy = Object.entries(mapOfCategoryToSelectedStrategy)
      .filter(([_, strategy]) => strategy)
      .map(([category, strategy]) => `${category}:${strategy}`);
  },
  { deep: true },
);

const strategySectionOpen = ref<boolean>(!!params.strategy);
const responseSourceDetailsOpen = ref<boolean>(false);

const textInTheMathInputTextbox = ref<string>(params.input);
/** Change this to reactively trigger re-querying Solver, since the solver backend might
 * have changed. */
const reactivityTriggerForQueryingSolver = ref({});

const submitForm = () => {
  // This will trigger re-querying Solver, because the `computedAsync`s that query the
  // Solver reactively depend on `params.input`
  params.input = textInTheMathInputTextbox.value;
  // Re-query solver even if `params.input` didn't change.
  reactivityTriggerForQueryingSolver.value = {};
};

const solverContext = computed(() => {
  const ret: SolverContext = {};
  if (params.precision) {
    ret.precision = parseInt(params.precision);
  }
  if (params.solutionVariable) {
    ret.solutionVariable = params.solutionVariable;
  }
  if (params.preset) {
    ret.presets = [params.preset];
  }
  const preferredStrategies = { ...mapOfCategoryToSelectedStrategy };
  console.log(preferredStrategies);
  for (const category in preferredStrategies) {
    if (preferredStrategies[category] === '') {
      delete preferredStrategies[category];
    }
  }
  if (Object.keys(preferredStrategies).length > 0) {
    ret.preferredStrategies = preferredStrategies;
  }

  ret.settings = {};
  for (const setting of settings.value || []) {
    if (params[setting.name]) {
      ret.settings[setting.name] = params[setting.name];
    }
  }

  return ret;
});

const resultJsonFormat = computedAsync<
  TransformationJson | PlanSelectionJson[] | ServerErrorResponse | undefined
>(
  async () => {
    const input = params.input;
    if (!input) return undefined;
    // Read this value, to make this `computedAsync` reactively depend on its value.
    reactivityTriggerForQueryingSolver.value;
    const plan = params.plan;
    const ret =
      plan === 'selectPlans'
        ? await solverSdk.api.selectPlans(input, jsonFormatSpecifier, solverContext.value)
        : await solverSdk.api.applyPlan(input, plan, jsonFormatSpecifier, solverContext.value);
    console.log('Fetched result in JSON format:', ret);
    return ret;
  },
  undefined,
  // Invalid math syntax errors are not thrown errors, so they are not handled by this
  { onError: (error) => console.error(error) },
);

const graphSolverFormat = computedAsync<GraphResponseSolver | ServerErrorResponse>(async () => {
  const input = params.input;
  const ret = await solverSdk.api.createGraph(input, 'solver', solverContext.value);
  return ret;
});

const graphJsonFormat = computedAsync<GraphResponseJson | ServerErrorResponse>(async () => {
  const input = params.input;
  const ret = await solverSdk.api.createGraph(input, 'json2', solverContext.value);
  return ret;
});

/** Use this instead of `params.plan === 'selectPlans'` because `resultJsonFormat` might
 * be the wrong format, temporarily, while we are fetching a new format. */
const resultJsonFormatIsAListOfPlans = computed(() => {
  return Array.isArray(resultJsonFormat.value);
});

const resultSolverFormat = computed(() => {
  // We don't want to query Solver for the result in Solver format, unless the user
  // clicked on the things in the UI that would require showing the result in Solver
  // format.
  if (!jsonFormat.value && responseSourceDetailsOpen.value) {
    return resultSolverFormat_Helper.value;
  }
  return undefined;
});

const resultSolverFormat_Helper = computedAsync<
  TransformationSolver | PlanSelectionSolver[] | ServerErrorResponse | undefined
>(
  async () => {
    // Read this value, to make this `computedAsync` reactively depend on its value.
    reactivityTriggerForQueryingSolver.value;
    const ret =
      params.plan === 'selectPlans'
        ? await solverSdk.api.selectPlans(params.input, 'solver', solverContext.value)
        : await solverSdk.api.applyPlan(params.input, params.plan, 'solver', solverContext.value);
    console.log('Fetched result in Solver format:', ret);
    return ret;
  },
  undefined,
  { lazy: true, onError: (error) => console.error(error) },
);

fetchDefaultTranslations();

/** This is a convenience mode for local development. When enabled, this makes it so that
 * you don't have to press enter to submit the form.
 *
 * You can enable this by creating a solver-poker/.env.local file that has
 * `VITE_AUTO_SUBMISSION_MODE=true` in it. Then run Poker locally (rebuilding first, if
 * you are not using `npm run poker-dev`) */
const autoSubmissionMode =
  (import.meta as TodoFigureOutType).env.VITE_AUTO_SUBMISSION_MODE === 'true' &&
  (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1');

if (autoSubmissionMode) {
  watch(textInTheMathInputTextbox, () => submitForm());
}

type TodoFigureOutType = any;

// Set `document.title`
const stopChangeTitleWatcher = watchEffect(() => {
  const info = versionInfo.value;
  if (info) {
    stopChangeTitleWatcher();
    const { deploymentName } = info;
    if (deploymentName && deploymentName !== 'main') {
      document.title = `${deploymentName} Solver Poker`;
    }
  }
});

const submitToMainButtonClicked = () => {
  const url = new URL(mainPokerURL);
  url.search = location.search;
  window.open(url.toString(), '_blank');
};

const camelCaseToLabel = (category: string) => {
  const categoryName = category.replace(/(.)([A-Z])/g, '$1 $2').toLowerCase();
  return categoryName.charAt(0).toUpperCase() + categoryName.slice(1);
};

const inputSyntaxHelpSection = ref<HTMLDetailsElement | null>(null);

let applet = ref<GgbAppletApi | undefined>(undefined);

function onLoad(api: GgbAppletApi) {
  console.log('Defining applet');
  applet.value = api;
}

watch([graphJsonFormat, applet], () => {
  // Redraw the graph
  const graph = graphJsonFormat.value;
  const ggbApi = applet.value;
  if (!ggbApi || !graph) {
    return;
  }
  ggbApi.newConstruction();
  const grapher = new GgbAppletGrapher(ggbApi);

  if ('error' in graph) {
    return;
  }
  grapher.setCoordinateSystem(graph.coordinateSystem);
  const colors: Array<[number, number, number]> = [
    [200, 20, 20],
    [10, 120, 10],
    [10, 10, 100],
  ];
  for (const [i, obj] of graph.objects.entries()) {
    grapher.drawObject(obj, colors[i % colors.length]);
  }
  // applet.value.showAllObjects();
});

onMounted(() => {
  window.renderMathInElement?.(inputSyntaxHelpSection.value!);
});
</script>

<template>
  <form v-show="!demoMode" class="display-options">
    <h3>Engine Options</h3>
    <label for="presets">Preset</label>
    <select id="presets" v-model="params.preset">
      <option name="-" value="" selected>-</option>
      <option v-for="preset in presets" :value="preset.name">
        {{ preset.name }}
      </option>
    </select>
    <br />
    <template v-for="setting of settings ?? []" :key="setting">
      <select :id="setting.name" v-model="params[setting.name]">
        <option name="-" value="" selected>-</option>
        <option v-for="value in setting.values" :value="value">
          {{ value }}
        </option>
      </select>
      <label :for="setting.name">{{ camelCaseToLabel(setting.name) }}</label>
      <br />
    </template>
    <h3>Display Options</h3>
    <label for="colorScheme">Colors</label>
    <select id="colorScheme" v-model="colorScheme">
      <option value="default">Default</option>
      <option value="primary">Primary colors</option>
      <option value="none">No coloring</option>
      <option value="blue">Just blue</option>
    </select>
    <br />
    <label for="solutionFormat">Solution format</label>
    <select id="solutionFormat" v-model="solutionFormat">
      <option value="simple">Simple</option>
      <option value="sets">Sets</option>
    </select>
    <br />
    <input id="showThroughSteps" v-model="showThroughSteps" type="checkbox" />
    <label for="showThroughSteps">Show through steps</label>
    <br />
    <input id="hideWarnings" v-model="hideWarnings" type="checkbox" />
    <label for="hideWarnings">Hide warnings</label>
    <br />
    <input id="showPedanticSteps" v-model="showPedanticSteps" type="checkbox" />
    <label for="showPedanticSteps">Show pedantic steps</label>
    <br />
    <input id="showCosmeticSteps" v-model="showCosmeticSteps" type="checkbox" />
    <label for="showCosmeticSteps">Show cosmetic steps</label>
    <br />
    <input id="showInvisibleChangeSteps" v-model="showInvisibleChangeSteps" type="checkbox" />
    <label for="showInvisibleChangeSteps">Show InvisibleChange steps</label>
    <br />
    <input id="showTranslationKeys" v-model="showTranslationKeys" type="checkbox" />
    <label for="showTranslationKeys">Show translation keys</label>
  </form>

  <div class="version-info">
    <template v-if="!versionInfo">fetching commit...</template>
    <template v-else-if="versionInfo.commit">
      commit
      <a href="https://git.geogebra.org/solver-team/solver-engine/-/commit/${info.commit}">{{
        versionInfo.commit.substring(0, 8)
      }}</a>
    </template>
    <template v-else>no commit info</template>
  </div>
  <h1 contenteditable="true">
    <template v-if="!versionInfo?.deploymentName || versionInfo.deploymentName === 'main'"
      >Solver Poker
    </template>
    <template v-else-if="/^PLUT-\d+$/i.test(versionInfo.deploymentName)"
      >Solver Poker
      <a
        :href="`https://geogebra-jira.atlassian.net/browse/${versionInfo.deploymentName.toUpperCase()}`"
        >{{ versionInfo.deploymentName.toUpperCase() }}
      </a>
    </template>
    <template v-else>Solver Poker ({{ versionInfo.deploymentName }})</template>
  </h1>
  <details v-show="!demoMode" ref="inputSyntaxHelpSection">
    <summary>Input syntax</summary>
    <ul>
      <li>recurring decimals: \(10.3\overline{401}\) is <code>10.3[401]</code>,</li>
      <li>arithmetic operators \(+, -, \times, \div\) are <code>+, -, *, :</code>,</li>
      <li>\(\alpha + \Omega\) is <code>\alpha + \Omega</code></li>
      <li>\(x_{12} - \beta_n\) is <code>x_12 - \beta_n</code></li>
      <li>\(x^y\) is <code>[x ^ y]</code>,</li>
      <li>\(\frac{x}{y}\) is <code>[x / y]</code>,</li>
      <li>\(2\frac{1}{3}\) is <code>[2 1/3]</code>,</li>
      <li>\(\sqrt{x}\) is <code>sqrt[x]</code>,</li>
      <li>\(\sqrt[3]{12}\) is <code>root[12, 3]</code>.</li>
      <li>\(\left|3x^2 + 2\right|\) is <code>abs[3 [x ^ 2] + 2]</code></li>
      <li>natural log: \(\ln x\) is <code>ln x</code></li>
      <li>base-10 log: \(\log x\) is <code>log x</code></li>
      <li>base-b log: \(\log_b x\) is <code>log[b] x</code></li>
      <li>
        mathematical constants \(e\), \(\pi\), and \(i\) are <code>/e/</code>, <code>/pi/</code>,
        and <code>/i/</code>
      </li>
      <li>
        supported trigonometric functions: <code>sin</code>, <code>cos</code>, <code>tan</code>,
        <code>sec</code>, <code>csc</code>, <code>cot</code>, and their inverse (e.g.
        <code>arcsin</code>), hyperbolic (e.g. <code>sinh</code>) and hyperbolic inverse (e.g.
        <code>asinh</code>) functions; the argument is written directly after the function name, e.g
        <code>sin x</code> for \(\sin x\) or <code>asinh(x + 1)</code> for \(\textrm{asinh}(x + 1)\)
      </li>
      <li>
        <code>x + &lt;. -y + z .&gt;</code> is another way of writing \(x-y+z\), but it separates
        the \(-y+z\) into a subexpression called a partial sum. You should never need to input
        partial sums, possibly unless you are trying to test the back end.
      </li>
      <li>\(\frac{\mathrm{d} \sin x}{\mathrm{d} x}\) is <code>diff[sin x / x]</code></li>
      <li>
        \(\frac{\partial^2 \sin x \cos y}{\partial x \partial y}\) is
        <code>[diff ^ 2][sin x cos y / x y]</code>
      </li>
      <li>\(\int 3x + 1 \mathrm{d}x\) is <code>prim[3x + 1, x]</code></li>
      <li>
        \(\int_{-\infty}^0 e^{-x^2} \mathrm{d}x\) is
        <code>int[-/infinity/, 0, [/e/ ^ -[x^2]], x]</code>
      </li>
      <li>the vector \(\begin{pmatrix}1 \\ 2 \\ 3\end{pmatrix}\) is <code>vec[1, 2, 3]</code></li>
      <li>
        the matrix \(\begin{pmatrix}1 & 2 \\ 3 & 4\end{pmatrix}\) is <code>mat[1, 2; 3, 4]</code>
      </li>
    </ul>
    <p>Example: \(\frac{1}{\sqrt{1 - x^2}}\) is <code>[1 / sqrt[1 - [x^2]]]</code>.</p>
  </details>

  <!-- The `.prevent` prevents that thing where the browser automatically tries to do a
  page navigation when a form is submitted. -->
  <form v-show="!demoMode" @submit.prevent="submitForm">
    <p>
      <label for="precisionSelect">Precision</label>
      <select id="precisionSelect" v-model="params.precision">
        <option value="2">2 d.p.</option>
        <option value="3" selected>3 d.p.</option>
        <option value="4">4 d.p.</option>
        <option value="5">5 d.p.</option>
        <option value="6">6 d.p.</option>
      </select>
      <label for="solutionVariable">Solution variable</label>
      <input id="solutionVariable" v-model="params.solutionVariable" type="text" size="1" />
    </p>
    <p>
      <label for="plansSelect">Plan</label>
      <select id="plansSelect" v-model="params.plan">
        <option value="selectPlans">Select Plans</option>
        <option v-for="plan in plans" :value="plan">{{ plan }}</option>
      </select>
    </p>
    <details :open="strategySectionOpen">
      <summary>Strategies</summary>
      <p v-for="[category, strategyList] in Object.entries(strategies ?? {})" :key="category">
        <label :for="`${category}Select`">
          {{ camelCaseToLabel(category) }}
        </label>
        <select
          :id="`${category}Select`"
          v-model="mapOfCategoryToSelectedStrategy[category]"
          :name="category"
        >
          <option name="-" value="" selected>-</option>
          <option v-for="strategy in strategyList" :value="strategy.strategy">
            {{ strategy.strategy }}
          </option>
        </select>
      </p>
    </details>
    <p>
      <label for="input">Input</label>
      <input
        id="input"
        v-model="textInTheMathInputTextbox"
        type="text"
        placeholder="Expression"
        size="30"
      />
      <input v-if="!autoSubmissionMode" type="submit" value="Submit" />
      <input
        v-if="versionInfo && versionInfo.deploymentName !== 'main'"
        type="button"
        value="Submit to main"
        @click="submitToMainButtonClicked"
      />
    </p>
  </form>

  <details open>
    <summary>Response</summary>
    <p v-if="translationsFetched">
      <template v-if="!params.input"></template>
      <template v-else-if="!resultJsonFormat">Fetching…</template>
      <template v-else-if="'error' in resultJsonFormat">
        Error: {{ resultJsonFormat.error }}<br />
        Message: {{ resultJsonFormat.message }}
      </template>
      <PlanSelections
        v-else-if="resultJsonFormatIsAListOfPlans"
        :solver-response="(resultJsonFormat as PlanSelectionJson[])"
      ></PlanSelections>
      <template v-else>
        <TransformationComponent
          :transformation="(resultJsonFormat as TransformationJson)"
          :depth="1"
        ></TransformationComponent>
        <TestSuggestion
          :transformation="(resultJsonFormat as TransformationJson)"
          :method-id="params.plan"
        ></TestSuggestion>
      </template>
    </p>
  </details>

  <details
    v-show="!demoMode"
    :open="responseSourceDetailsOpen"
    @toggle="responseSourceDetailsOpen = !responseSourceDetailsOpen"
  >
    <summary>Response Source</summary>
    <input id="jsonFormatCheckbox" v-model="jsonFormat" type="checkbox" />
    <label for="jsonFormatCheckbox">JSON Format</label>
    <pre id="source">{{
      JSON.stringify(jsonFormat ? resultJsonFormat : resultSolverFormat, null, 4)
    }}</pre>
  </details>
  <details>
    <summary>Graph</summary>
    <div id="applet">
      <GgbApplet
        :material-id="''"
        :settings="{
          appName: 'classic',
          showAlgebraInput: false,
          perspective: 'G',
          height: 600,
          width: 600,
          enableLabelDrags: true,
          enableShiftDragZoom: true,
          scaleContainerClass: null, // If we scale, firefox thinks the scale is 0
          allowUpscale: false, // We want width and height to really be 600
        }"
        @onLoad="onLoad"
      />
    </div>
  </details>
  <details v-show="!demoMode">
    <summary>Graph Source</summary>
    <pre>{{ JSON.stringify(graphSolverFormat, null, 4) }}</pre>
  </details>
</template>

<style>
input,
select {
  margin-left: 4px;
}

select {
  margin-right: 4px;
}

.display-options {
  float: right;
}

.display-options > input[type='checkbox'] {
  margin-left: 0;
}

.plan-id {
  color: blue;
  font-family: monospace;
}

.translation-key {
  font-family: monospace;
  color: darkgreen;
  cursor: copy;
}

.note {
  color: gray;
  font-size: small;
}

.warning {
  color: #ac0000;
}

/* Separations between plan steps / sub-steps */

details.steps > ol,
details.tasks > ol {
  margin: 0 0 0 0;
  border-left: thin solid lightgray;
  padding-right: 0;
}

details.steps > ol > li,
details.tasks > ol > li {
  border-bottom: thin solid lightgray;
  padding: 5px 0;
  margin: 5px 0;
}

details.steps > ol > li:last-child,
details.tasks > ol > li:last-child {
  border-bottom: 0;
  padding-bottom: 0;
  margin-bottom: 0;
}

details.alternatives > summary {
  color: blue;
}

/* Separations between selected plans */
.selections > ol > li {
  border-bottom: thin solid black;
  padding: 6px 0;
  margin: 6px 0;
}

.selections > ol > li:last-child {
  border-bottom: 0;
  padding-bottom: 0;
  margin-bottom: 0;
}

/* Separate the Test Code section from the solution */
.test-code {
  padding-top: 20px;
}

.through-step > .expr,
.through-step > .plan-id,
.through-step > .plan-explanation {
  color: lightgray;
}

.version-info {
  position: fixed;
  bottom: 0;
  right: 0;
  background-color: white;
  color: dimgrey;
  padding: 4px;
  font-family: monospace;
  font-size: small;
}

summary {
  /* to avoid expanding the details view when clicking on the whitespace next to the summary */
  width: fit-content;
}
</style>
