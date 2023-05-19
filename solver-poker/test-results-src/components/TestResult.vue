<script setup lang="ts">
import type { Ref } from 'vue';
import { computed, ref } from 'vue';
// We would use `jsondiffpatch` instead of `jsondiffpatch-rc` but that has a problem with
// Vite right now. See https://github.com/vitejs/vite/issues/11986#issuecomment-1494484353
import * as jsondiffpatch from 'jsondiffpatch-rc';
import '../json-diff-patch-with-dark-mode.css';
import DiffMatchPatch from 'diff-match-patch';
import stringify from 'json-stable-stringify';
import {
  App_showHelp,
  TestResult_cutThroughStepsFromDiff,
  TestResult_cutThroughStepsFromTransformationSection,
  TestResult_diffLineMode,
  TestResult_diffTheTestSyntax,
  TestResult_diffUsingMergely,
  TestResult_experimentalView1,
  TestResult_greenIsActual,
  TestResult_showLeftAndRightSeparately,
  TestResult_showNonAssertedPropertiesInDiff,
  TestResult_showNonAssertedPropertiesInDiffInLineMode,
  TestResult_trimTransformation,
} from '../local-storage-store';
import type { testResults } from '../test-results.json';
import { generateTestSuggestion } from '../../src/render-test';
import { isThroughStep } from '../../src/util';
import MergelyComponent from '../MergelyComponent.vue';

const props = defineProps<{ testResult: (typeof testResults)[0] }>();

const sortedTransformationJson: Ref<string | null> = computed(() => {
  const transformation = JSON.parse(props.testResult.transformation);
  if (!transformation) return null;
  return stringify(transformation, ({ key: key1 }, { key: key2 }) => {
    // sort the json object's properties
    const index1 = keyOrder.indexOf(key1);
    const index2 = keyOrder.indexOf(key2);
    if (index1 !== -1 && index2 !== -1) return index1 - index2;
    if (index1 !== -1) return -1;
    if (index2 !== -1) return 1;
    return key1.toString().localeCompare(key2.toString());
  });
});
const keyOrder = ['type', 'explanationKey', 'explanation', 'fromExpr', 'steps', 'tasks'];
const assertionTreeTrimmedWithoutThroughSteps = computed(() => {
  const assertionTree = JSON.parse(props.testResult.assertionTree, jsonTrimmer);
  return makeTransformedCopy(assertionTree, trimThroughSteps);
});
const assertionTreeTrimmed = computed(() =>
  JSON.parse(props.testResult.assertionTree, jsonTrimmer),
);
const whatTheTestProbablySaid = computed(() =>
  generateTestSuggestion(JSON.parse(props.testResult.assertionTree), true),
);
const testSuggestion = computed(() => {
  const transformation = JSON.parse(props.testResult.transformation);
  if (transformation === null) return null;
  return generateTestSuggestion(transformation, true);
});
const parsedTransformation = computed(
  () => sortedTransformationJson.value && JSON.parse(sortedTransformationJson.value),
);
const trimmedTransformation = computed(
  () => sortedTransformationJson.value && JSON.parse(sortedTransformationJson.value, jsonTrimmer),
);
const transformationToShowInTransformationSection = computed(() => {
  if (!sortedTransformationJson.value) return null;
  const transformation = TestResult_trimTransformation.value
    ? trimmedTransformation.value
    : parsedTransformation.value;
  if (!TestResult_cutThroughStepsFromTransformationSection.value) return transformation;
  return makeTransformedCopy(transformation, trimThroughSteps);
});
const transformationWithDiffPreferencesApplied = computed(() => {
  if (!sortedTransformationJson.value) return null;
  let transformation = JSON.parse(sortedTransformationJson.value, jsonTrimmer);
  let assertionTree = JSON.parse(props.testResult.assertionTree, jsonTrimmer);
  if (TestResult_cutThroughStepsFromDiff.value) {
    transformation = makeTransformedCopy(transformation, trimThroughSteps);
    assertionTree = makeTransformedCopy(assertionTree, trimThroughSteps);
  }
  if (!TestResult_showNonAssertedPropertiesInDiff.value) {
    deletePropertiesFromTransformationThatAreNotAsserted(assertionTree, transformation);
  }
  return transformation;
});

const listOfExpressions = computed(() => {
  const ret = [] as string[];
  let lastExpr = '';
  traverse(
    trimmedTransformation.value,
    (key, value) => {
      if (key === 'fromExpr' && value !== lastExpr) {
        ret.push(value);
        lastExpr = value;
      }
    },
    (key, value) => {
      if (key === 'toExpr' && value !== lastExpr) {
        ret.push(value);
        lastExpr = value;
      }
    },
  );
  return ret;
});

/** Recursively deletes any properties from `transformation` that are not in
 * `assertionTree`. This does not delete any elements from any arrays. */
function deletePropertiesFromTransformationThatAreNotAsserted(assertionTree, transformation) {
  if (!assertionTree) return;
  if (!transformation) return;
  if (typeof assertionTree !== 'object') return;
  if (Array.isArray(assertionTree)) {
    if (transformation && !Array.isArray(transformation)) {
      throw new Error(
        'if assertionTree is an array, then transformation must be an array too. There must be something wrong with the test framework code to make an invalid tree, like this.',
      );
    }
    assertionTree.forEach((_, index) => {
      deletePropertiesFromTransformationThatAreNotAsserted(
        assertionTree[index],
        transformation[index],
      );
    });
    return;
  }
  Object.keys(transformation).forEach((key) => {
    if (!(key in assertionTree)) {
      delete transformation[key];
    } else {
      deletePropertiesFromTransformationThatAreNotAsserted(assertionTree[key], transformation[key]);
    }
  });
}

function jsonTrimmer(key: string, value) {
  if (value === null) return value;
  delete value.mappedParams;
  delete value.gmAction;
  delete value.type;
  delete value.skills;
  delete value.wasIgnoredDuringTestAsAThroughStep;
  delete value.tags;
  delete value.path;
  delete value.pathMappings;
  if (value.explanation) {
    const explanationKey = value.explanation.key;
    delete value.explanation;
    return { explanationKey, ...value };
  }
  return value;
}

function trimThroughSteps(key: string, value) {
  if (isThroughStep(value)) {
    return value.steps[0];
  }
  return value;
}

function makeTransformedCopy(parsedJson, transform) {
  return JSON.parse(JSON.stringify(parsedJson, transform));
}

const assertionList = computed(() =>
  treeToList(JSON.parse(props.testResult.assertionTree, jsonTrimmer)).join('\n'),
);
const transformationPropertyList = computed(() =>
  treeToList(
    TestResult_showNonAssertedPropertiesInDiffInLineMode.value
      ? trimmedTransformation.value
      : transformationWithDiffPreferencesApplied.value,
  ).join('\n'),
);

const originalLeft = computed(() => {
  if (TestResult_diffTheTestSyntax.value) return whatTheTestProbablySaid.value;
  if (TestResult_diffLineMode.value) return assertionList.value;
  return TestResult_cutThroughStepsFromDiff.value
    ? assertionTreeTrimmedWithoutThroughSteps.value
    : assertionTreeTrimmed.value;
});
const originalRight = computed(() => {
  if (TestResult_diffTheTestSyntax.value) return testSuggestion.value;
  if (TestResult_diffLineMode.value) return transformationPropertyList.value;
  return transformationWithDiffPreferencesApplied.value;
});
const left = computed(() =>
  TestResult_greenIsActual.value ? originalLeft.value : originalRight.value,
);
const right = computed(() =>
  TestResult_greenIsActual.value ? originalRight.value : originalLeft.value,
);
const leftText = computed(() => {
  const value = left.value || '';
  if (typeof value === 'string') return value;
  return JSON.stringify(value, null, 2).replace(/,\n/g, '\n');
});
const rightText = computed(() => {
  const value = right.value || '';
  if (typeof value === 'string') return value;
  return JSON.stringify(value, null, 2).replace(/,\n/g, '\n');
});

const diffHtml = computed(() => {
  if (typeof (left.value || right.value) === 'string') {
    const diffMp = new DiffMatchPatch();
    const diff = diffMp.diff_main(leftText.value, rightText.value);
    diffMp.diff_cleanupSemantic(diff);
    return diffMp
      .diff_prettyHtml(diff)
      .replace(/&para;/g, '')
      .replace(/style="background:#e6ffe6;"/g, '')
      .replace(/style="background:#ffe6e6;"/g, '');
  } else {
    const delta = jsondiffpatch.diff(left.value, right.value);
    if (!delta) return '';
    return jsondiffpatch.formatters.html.format(delta, left.value);
  }
});

function treeToList(tree: any) {
  const list: string[] = [];
  traverse(
    tree,
    (key, value) => {
      if (TestResult_experimentalView1.value) {
        if (!(value instanceof Object) || Array.isArray(value)) return;
        list.push(`E   ${value.explanationKey ?? ''}`);
        list.push(`┌     ${value.fromExpr ?? ''}`);
      } else {
        if (key === 'explanationKey') {
          list.push(`E   ${value ?? ''}`);
        }
        if (key === 'fromExpr') {
          list.push(`┌     ${value}`);
        }
      }
    },
    (key, value) => {
      if (TestResult_experimentalView1.value) {
        if (!(value instanceof Object) || Array.isArray(value)) return;
        list.push(`└     ${value.toExpr ?? ''}`);
      } else {
        if (key === 'toExpr') {
          list.push(`└     ${value}`);
        }
      }
    },
  );
  return list.map((line) => line.trim());
}

function traverse(obj, callbackBeforeRecurse, callbackAfterRecurse) {
  for (const key in obj) {
    callbackBeforeRecurse(key, obj[key], obj);
    if (obj[key] !== null && typeof obj[key] === 'object') {
      traverse(obj[key], callbackBeforeRecurse, callbackAfterRecurse);
    }
    callbackAfterRecurse(key, obj[key], obj);
  }
}

/** These three variables are unnecessary, but they decrease the page load time by making
 * it so we don't add stuff to the DOM that is hidden anyway. (Blocking time goes from
 * roughly .9 seconds to .24 seconds, when rendering all the tests, as of 2023-04-21). */
const transformationOpen = ref(false);
const suggestionOpen = ref(false);
const assertionTreeOpen = ref(false);
</script>

<template>
  <div>{{ testResult.testClassName }}.{{ testResult.testName }}</div>
  {{ testResult.methodId }}
  <div
    :style="{
      'border-color': testResult.passed ? 'rgba(46, 160, 67, 0.25)' : 'rgba(248, 81, 73, 0.2)',
    }"
    class="card"
  >
    <details @toggle="transformationOpen = !transformationOpen">
      <summary>Transformation</summary>
      <input
        type="checkbox"
        v-model="TestResult_cutThroughStepsFromTransformationSection"
        id="cutThroughStepsFromTransformationSection"
      />
      <label for="cutThroughStepsFromTransformationSection">Do not show through steps here </label>
      <div class="help-text">The "actual" output:</div>
      <pre v-if="transformationOpen">{{ transformationToShowInTransformationSection }}</pre>
    </details>
    <br />
    <details @toggle="suggestionOpen = !suggestionOpen">
      <summary>Test Code Suggestion</summary>
      <pre v-if="suggestionOpen">{{
        parsedTransformation &&
        generateTestSuggestion(parsedTransformation, true, testResult.methodId)
      }}</pre>
      <hr class="minor" />
    </details>
    <br />
    <details @toggle="assertionTreeOpen = !assertionTreeOpen">
      <summary>Assertion Tree</summary>
      <div class="help-text">What the test asserted:</div>
      <template v-if="assertionTreeOpen">
        <pre>{{
          TestResult_trimTransformation
            ? JSON.parse(testResult.assertionTree, jsonTrimmer)
            : JSON.parse(testResult.assertionTree)
        }}</pre>
        <hr class="minor" />
      </template>
    </details>
    <div class="help-text">The actual steps:</div>
    <div style="white-space: pre">{{ listOfExpressions.join('\n') }}</div>
    <template v-if="!testResult.passed">
      <hr v-if="!App_showHelp" class="minor" />
      <template v-if="TestResult_showLeftAndRightSeparately">
        <span class="help-text"> What is shown in the diff, but separate: </span>
        <div class="monospace red">{{ leftText }}</div>
        <div class="monospace green">{{ rightText }}</div>
        <hr class="minor" />
      </template>
      <div class="help-text">The diff:</div>
      <MergelyComponent
        v-if="TestResult_diffUsingMergely"
        :leftText="leftText"
        :rightText="rightText"
      />
      <div v-else class="monospace" v-html="diffHtml"></div>
    </template>
    <template v-if="testResult.failureMessage">
      <hr class="minor" />
      <div class="help-text">Error message:</div>
      <code style="white-space: pre">{{ testResult.failureMessage }}</code>
    </template>
    <!-- <div class="monospace red">{{ leftText }}</div>
    <div class="monospace green">{{ rightText }}</div>
    <div class="monospace" v-html="diffHtml"></div>
    <hr />
    <MergelyComponent
      v-if="TestResult_diffUsingMergely"
      :leftText="leftText"
      :rightText="rightText"
    /> -->
  </div>
</template>

<style scoped>
details {
  display: inline;
}

:deep(del) {
  text-decoration: none;
  background-color: var(--highlight-red);
}

:deep(ins) {
  text-decoration: none;
  background-color: var(--highlight-green);
}

.green {
  background-color: var(--highlight-green);
}

.red {
  background-color: var(--highlight-red);
}

.card {
  border-width: 2px;
  border-style: solid;
  padding: 0.5rem;
  border-radius: 0.5rem;
}

.monospace {
  /* font-family: Consolas, 'Courier New', monospace; */
  font-family: 'Bitstream Vera Sans Mono', 'DejaVu Sans Mono', Monaco, Courier, monospace;
  font-size: 14px;
  white-space: pre;
  /* This is was set so that the │ character would match tip to tip with another one that
    is on the next line without gap│. (That is not a normal pipe character.) */
  line-height: 1.21;
}

hr.minor {
  border: 0;
  border-top: 1px solid;
  opacity: 0.18;
  width: max(20%, 8em);
  max-width: 100%;
  margin-left: 0;
}

:deep(.mergely-editor .mergely.ch.d.lhs) {
  text-decoration: none;
}
</style>
