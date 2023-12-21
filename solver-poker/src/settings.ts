// Globally changes the rendering of steps.
import * as solverSdk from '@geogebra/solver-sdk';
import { LatexSettings } from '@geogebra/solver-sdk';
import { computedAsync, useUrlSearchParams } from '@vueuse/core';
import { computed, ref } from 'vue';
import type renderMathInElement from 'katex/contrib/auto-render';

declare global {
  interface Window {
    ggbSolver: typeof solverSdk;
    renderMathInElement: typeof renderMathInElement;
  }
}

const getAPIBaseURL = (): string => {
  // This magic number for the port is dictated in the `poker-dev` script in package.json.
  const runningLocallyViaVite = location.port === '4173';
  if (runningLocallyViaVite) {
    return 'http://localhost:8080/api/v1';
  }
  // Poker can be served at
  // - <base>/poker.html (legacy)
  // - <base>/poker
  // - <base>/poker/index.html
  return location.pathname.replace(/\/(poker\.html|poker\/?|poker\/index\.html)$/, '/api/v1');
};

solverSdk.api.baseUrl = getAPIBaseURL();

// just for debug convenience
window.ggbSolver = solverSdk;

export const colorSchemes = {
  default: [
    'green',
    'purple',
    'red',
    // Orange
    '#E07415',
  ],
  none: null,
  blue: ['blue'],
  primary: ['red', 'green', 'blue'],
};

export type ColorScheme = keyof typeof colorSchemes;

export const solutionFormatters = {
  sets: solverSdk.setsSolutionFormatter,
  simple: solverSdk.simpleSolutionFormatter,
};

export type SolutionFormat = keyof typeof solutionFormatters;

export const colorScheme = ref<ColorScheme>('default');
export const solutionFormat = ref<SolutionFormat>('simple');

export const settings = computedAsync(() => solverSdk.api.listSettings());
export const presets = computedAsync(() => solverSdk.api.listPresets());

export const params = useUrlSearchParams('history', {
  removeFalsyValues: true,
  removeNullishValues: true,
}) as {
  plan: string;
  input: string;
  precision: string;
  solutionVariable: string;
  strategy?: string | string[];
  preset?: string;

  // Properties that only affect how the Solver's response is displayed

  showThroughSteps?: '1' | '';
  hideWarnings?: '1' | '';
  showPedanticSteps?: '1' | '';
  showCosmeticSteps?: '1' | '';
  showInvisibleChangeSteps?: '1' | '';
  showTranslationKeys?: '1' | '';
  jsonFormat?: '1' | '';
} & {
  // settings
  [key: string]: string;
};

params.plan = params.plan || 'selectPlans';
params.input = params.input || '';
params.precision = params.precision || '3';
params.solutionVariable = params.solutionVariable || 'x';

function booleanRefSyncedWithUrlParam(paramName: keyof typeof params) {
  return computed({
    get: () => params[paramName] === '1',
    set: (value) => {
      params[paramName] = value ? '1' : '';
    },
  });
}

export const showThroughSteps = booleanRefSyncedWithUrlParam('showThroughSteps');
export const hideWarnings = booleanRefSyncedWithUrlParam('hideWarnings');
export const showPedanticSteps = booleanRefSyncedWithUrlParam('showPedanticSteps');
export const showCosmeticSteps = booleanRefSyncedWithUrlParam('showCosmeticSteps');
export const showInvisibleChangeSteps = booleanRefSyncedWithUrlParam('showInvisibleChangeSteps');
export const showTranslationKeys = booleanRefSyncedWithUrlParam('showTranslationKeys');
export const jsonFormat = booleanRefSyncedWithUrlParam('jsonFormat');

export const getSettingValue = (settingName: string) => params[settingName] === 'true';

export const setSettingValue = (settingName: string, value: boolean) => {
  params[settingName] = value ? 'true' : '';
};

export const latexSettings = computed<LatexSettings>(() => ({
  solutionFormatter: solutionFormatters[solutionFormat.value as SolutionFormat],
}));

export const demoMode = ref(false);
document.onkeydown = (evt) => {
  if (evt.ctrlKey && evt.code === 'KeyD') {
    demoMode.value = !demoMode.value;
  }
};
