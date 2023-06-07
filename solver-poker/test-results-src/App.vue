<script setup lang="ts">
// This file uses <script setup> syntax. See
// https://vuejs.org/api/sfc-script-setup.html#script-setup for more info about how to use
// that.

import { watchEffect } from 'vue';
import take from 'lodash/take';
import { useTimeAgo } from '@vueuse/core';
import TestResult from './components/TestResult.vue';
// These .json files are generated when you run the tests.
import { testResults, dateGenerated } from './test-results';
import testsRunning from './tests-running.json';
import {
  App_countOfTestsToRender,
  App_showHelp,
  TestResult_showLeftAndRightSeparately,
  TestResult_trimTransformation,
  TestResult_greenIsActual,
  TestResult_showNonAssertedPropertiesInDiff,
  TestResult_showNonAssertedPropertiesInDiffInLineMode,
  TestResult_experimentalView1,
  TestResult_diffUsingMergely,
  TestResult_diffTheTestSyntax,
  TestResult_cutThroughStepsFromDiff,
  TestResult_diffLineMode,
} from './local-storage-store';

const timeSinceResultsGenerated = useTimeAgo(new Date(dateGenerated));
const failedTests = testResults.filter((result) => !result.passed);
const passedTests = testResults.filter((result) => result.passed);

watchEffect(() => {
  if (App_showHelp.value) {
    document.body.classList.add('show-help');
  } else {
    document.body.classList.remove('show-help');
  }
});

// Just for ease of debugging with the browser console
window['testResults'] = testResults;
</script>

<template>
  <header>
    <label for="countOfTestsToRender" style="margin-right: 0">Count of results to render </label>
    <input v-model.number="App_countOfTestsToRender" id="countOfTestsToRender" />
    <input type="checkbox" v-model="App_showHelp" id="showHelp" />
    <label for="showHelp">
      Show help
      <span class="help-text">(disable to remove clutter) </span>
    </label>
    <input type="checkbox" v-model="TestResult_trimTransformation" id="trimTransformation" />
    <label for="trimTransformation">
      Trim transformation
      <span class="help-text"
        >(Expand the "Transformation" section to see the results of toggling this.)
      </span>
    </label>
    <template v-if="failedTests.length > 0">
      <input
        type="checkbox"
        v-model="TestResult_showLeftAndRightSeparately"
        id="showLeftAndRightSeparately"
      />
      <label for="showLeftAndRightSeparately"> Show Actual and Expected separately </label>
      <input type="checkbox" v-model="TestResult_greenIsActual" id="greenIsActual" />
      <label for="greenIsActual">
        Actual is green
        <span class="help-text"
          >(or on the right in the case of Mergely). Expected is red or left.
        </span>
        <template v-if="!App_showHelp"> or on the right </template>
      </label>
      <input type="checkbox" v-model="TestResult_diffUsingMergely" id="diffUsingMergely" />
      <label for="diffUsingMergely"> Diff using Mergely </label>
      <input type="checkbox" v-model="TestResult_diffTheTestSyntax" id="diffTheTestSyntax" />
      <label for="diffTheTestSyntax"> Diff the test syntax </label>
      <template v-if="!TestResult_diffTheTestSyntax">
        <input type="checkbox" v-model="TestResult_diffLineMode" id="diffLineMode" />
        <label for="diffLineMode" disabled>
          Diff the ┌└ view
          <span class="help-text"
            >(In the diff, ┌ will mean <code>fromExpr</code> and └ will mean <code>toExpr</code>)
          </span>
        </label>
        <template v-if="TestResult_diffLineMode">
          <input
            type="checkbox"
            v-model="TestResult_showNonAssertedPropertiesInDiffInLineMode"
            id="showNonAssertedPropertiesInDiffInLineMode"
          />
          <label for="showNonAssertedPropertiesInDiffInLineMode">
            Show non-asserted properties in ┌└ diff
            <span class="help-text"
              >(In the diff, show properties on the actual transformation even if those properties
              are not also defined/asserted in the assertion tree)
            </span>
          </label>
          <input type="checkbox" v-model="TestResult_experimentalView1" id="experimentalView1" />
          <label for="experimentalView1">
            Experimental ┌└ View 1
            <span class="help-text"
              >(Try toggling this if the diff doesn't look helpful. Sometimes it makes a difference)
            </span>
          </label>
        </template>
      </template>
      <template v-if="!TestResult_diffTheTestSyntax && !TestResult_diffLineMode">
        <input
          type="checkbox"
          v-model="TestResult_cutThroughStepsFromDiff"
          id="cutThroughStepsFromDiff"
        />
        <label for="cutThroughStepsFromDiff"> Do not show through steps in diff </label>
        <input
          type="checkbox"
          v-model="TestResult_showNonAssertedPropertiesInDiff"
          id="showNonAssertedPropertiesInDiff"
        />
        <label for="showNonAssertedPropertiesInDiff">
          Show non-asserted properties in diff
          <span class="help-text"
            >(In the diff, show properties on the actual transformation even if those properties are
            not also defined/asserted in the assertion tree)
          </span>
        </label>
      </template>
    </template>
    <hr style="border: none; border-bottom: 1px solid; opacity: 0.18" />
  </header>
  <span class="deemphasize">Last updated </span>
  <span :class="timeSinceResultsGenerated === 'just now' ? '' : 'deemphasize'">{{
    timeSinceResultsGenerated
  }}</span>
  <span class="deemphasize">. </span>
  <div class="help-text">
    Timestamp may show unexpected results because of gradle caching/restoring the .json files. Only
    the plan tests in the <code>methods</code> package are shown.
  </div>
  <h1>
    {{ failedTests.length }} failed tests<template v-if="testsRunning">. Rerunning tests…</template>
  </h1>
  <TestResult
    v-for="testResult in take(failedTests, App_countOfTestsToRender, undefined)"
    :test-result="testResult"
  />
  <hr />
  <h1>{{ passedTests.length }} passed tests</h1>
  <TestResult
    v-for="testResult in take(passedTests, App_countOfTestsToRender, undefined)"
    :test-result="testResult"
  />
  <p class="deemphasize">
    The following message is required to be included, in order to use the Mergely diff library on
    this specific page:
    <br />
    This software is a Combined Work using Mergely and is covered by the MPL 1.1 license. For the
    full license, see http://www.mergely.com/license.
  </p>
</template>

<style scoped>
.deemphasize {
  opacity: var(--deemphasized-opacity);
}
header {
  position: sticky;
  top: 0;
  background-color: var(--background-color);
  z-index: 101;
}
label,
input:not([type='checkbox']) {
  margin-right: 1em;
}
</style>

<!-- Since this `<style>` tag doesn't say `scoped` like the other `<style>` tag, its will
affect the whole page, not just this component. -->
<style>
:root {
  --deemphasized-opacity: 0.4;
}
.help-text {
  opacity: var(--deemphasized-opacity);
  /* hides the text */
  font-size: 0px;
  transition: font-size 0.3s ease-in-out;
}
.show-help .help-text {
  /* shows the text */
  font-size: 0.8em;
}
</style>
