// Globally changes the rendering of steps.
import * as solverSDK from '@geogebra/solver-sdk';
import { LatexSettings } from '@geogebra/solver-sdk';

export interface Settings {
  showThroughSteps: boolean;
  showPedanticSteps: boolean;
  showCosmeticSteps: boolean;
  showInvisibleChangeSteps: boolean;
  showTranslationKeys: boolean;
  hideWarnings: boolean;

  selectedColorScheme: ColorScheme;

  latexSettings: LatexSettings;
}

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
  sets: solverSDK.setsSolutionFormatter,
  simple: solverSDK.simpleSolutionFormatter,
};

export type SolutionFormat = keyof typeof solutionFormatters;

export const settings: Settings = {
  showThroughSteps: false,
  showPedanticSteps: false,
  showCosmeticSteps: false,
  showTranslationKeys: false,
  showInvisibleChangeSteps: false,
  hideWarnings: false,
  selectedColorScheme: 'default',
  latexSettings: {
    solutionFormatter: solutionFormatters.simple,
  },
};
