import { useLocalStorage } from '@vueuse/core';

// This file provides reactive variables that will be persisted in local storage.
//
// Since this is global store, please prefix the names of the variables you write with
// something appropriate.

export const App_countOfTestsToRender = storedVar('App_countOfTestsToRender', 10);
export const App_showHelp = storedVar('App_showHelp', false);
export const TestResult_greenIsActual = storedVar('TestResult_greenIsActual', true);
export const TestResult_showLeftAndRightSeparately = storedVar(
  'TestResult_showLeftAndRightSeparately',
  false,
);
export const TestResult_trimTransformation = storedVar('TestResult_trimTransformation', true);
export const TestResult_diffUsingMergely = storedVar('TestResult_diffUsingMergely', false);
export const TestResult_diffTheTestSyntax = storedVar('TestResult_diffTheTestSyntax', false);
export const TestResult_cutThroughStepsFromDiff = storedVar(
  'TestResult_cutThroughStepsFromDiff',
  true,
);
export const TestResult_cutThroughStepsFromTransformationSection = storedVar(
  'TestResult_cutThroughStepsFromTransformationSection',
  false,
);
export const TestResult_diffLineMode = storedVar('TestResult_diffLineMode', false);
export const TestResult_showNonAssertedPropertiesInDiff = storedVar(
  'TestResult_showNonAssertedPropertiesInDiff',
  false,
);
export const TestResult_showNonAssertedPropertiesInDiffInLineMode = storedVar(
  'TestResult_showNonAssertedPropertiesInDiffInLineMode',
  false,
);
export const TestResult_experimentalView1 = storedVar('TestResult_experimentalView1', false);

function storedVar<T>(key, defaultValue: T) {
  return useLocalStorage(key, defaultValue, { writeDefaults: false });
}
