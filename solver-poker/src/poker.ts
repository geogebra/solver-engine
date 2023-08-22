import type { ApiMathFormat, SolverContext, StrategyMap } from '@geogebra/solver-sdk';
import * as solverSDK from '@geogebra/solver-sdk';
import { TransformationJson2 } from '@geogebra/solver-sdk';
import type { ColorScheme, SolutionFormat } from './settings';
import { settings, solutionFormatters } from './settings';
import { renderPlanSelections, renderTransformation } from './render-solution';
import { fetchDefaultTranslations } from './translations';
import type renderMathInElement from 'katex/contrib/auto-render';
import { copyTestCodeToClipboardOnClick, renderTest } from './render-test';

const jsonFormat: ApiMathFormat = 'json2';

declare global {
  interface Window {
    ggbSolver: typeof solverSDK;
    renderMathInElement: typeof renderMathInElement;
  }
}
// just for debug convenience
window.ggbSolver = solverSDK;

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

solverSDK.api.baseUrl = getAPIBaseURL();
const mainPokerURL = 'https://solver.geogebra.net/main/poker/index.html';

let lastResult: { planId?: string; result?: any } = {};

const el = (id: string) => document.getElementById(id);

/******************************************
 * Functions to execute plans and render the result
 *******************************************/

const copyTranslationKeysToClipboardOnClick = () => {
  const copyToClipboard = async (evt: Event) => {
    const textContent = (evt.target as HTMLElement).textContent!;
    await navigator.clipboard.writeText(textContent);
  };
  for (const el of document.querySelectorAll<HTMLElement>('.translation-key')) {
    el.onclick = copyToClipboard;
  }
};

const selectPlansOrApplyPlan = async ({
  planId,
  input,
  ...context
}: { planId: string; input: string } & SolverContext) => {
  const result =
    planId === 'selectPlans'
      ? await solverSDK.api.selectPlans(input, jsonFormat, context)
      : await solverSDK.api.applyPlan(input, planId, jsonFormat, context);
  lastResult = { planId, result: result as TransformationJson2 };
};

/******************************************
 * Do initial setup and register event handlers
 ******************************************/

interface RequestData {
  planId: string;
  input: string;
  curriculum: string;
  gmFriendly: boolean;
  precision: number;
  preferDecimals: boolean;
  solutionVariable: string;
  preferredStrategies: { [category: string]: string };
}

const buildURLString = (startURL: string, data: RequestData) => {
  const url = new URL(startURL);
  url.searchParams.set('plan', data.planId);
  url.searchParams.set('input', data.input);
  if (data.curriculum) {
    url.searchParams.set('curriculum', data.curriculum);
  } else {
    url.searchParams.delete('curriculum');
  }
  if (data.gmFriendly) {
    url.searchParams.set('gmFriendly', '1');
  } else {
    url.searchParams.delete('gmFriendly');
  }
  url.searchParams.set('precision', data.precision.toString());
  if (data.preferDecimals) {
    url.searchParams.set('preferDecimals', '1');
  } else {
    url.searchParams.delete('preferDecimals');
  }
  if (data.solutionVariable) {
    url.searchParams.set('solutionVariable', data.solutionVariable);
  } else {
    url.searchParams.delete('solutionVariable');
  }
  url.searchParams.delete('strategy');
  for (const category in data.preferredStrategies) {
    url.searchParams.append('strategy', `${category}:${data.preferredStrategies[category]}`);
  }
  return url.toString();
};

window.onload = () => {
  const mathInput = el('input') as HTMLInputElement;
  const curriculumSelect = el('curriculumSelect') as HTMLSelectElement;
  const planSelect = el('plansSelect') as HTMLSelectElement;
  const strategyDetails = el('strategyDetails') as HTMLDetailsElement;
  let strategySelects: HTMLSelectElement[] = [];
  const precisionSelect = el('precisionSelect') as HTMLSelectElement;
  const preferDecimalsCheckbox = el('preferDecimals') as HTMLInputElement;
  const gmFriendlyCheckbox = el('gmFriendlyCheckbox') as HTMLInputElement;
  const solutionVariableInput = el('solutionVariable') as HTMLInputElement;

  const inputForm = el('form') as HTMLFormElement;
  const submitToMainButton = el('submitToMain') as HTMLButtonElement;

  const showThroughStepsCheckbox = el('showThroughSteps') as HTMLInputElement;
  const showRearrangementStepsCheckbox = el('showRearrangementSteps') as HTMLInputElement;
  const showPedanticStepsCheckbox = el('showPedanticSteps') as HTMLInputElement;
  const showCosmeticStepsCheckbox = el('showCosmeticSteps') as HTMLInputElement;
  const showInvisibleChangeStepsCheckbox = el('showInvisibleChangeSteps') as HTMLInputElement;
  const colorSchemeSelect = el('colorScheme') as HTMLSelectElement;
  const solutionFormatSelect = el('solutionFormat') as HTMLSelectElement;
  const showTranslationKeysCheckbox = el('showTranslationKeys') as HTMLInputElement;
  const hideWarningsCheckbox = el('hideWarnings') as HTMLInputElement;

  const resultElement = el('result') as HTMLElement;
  const sourceElement = el('source') as HTMLElement;

  /******************************************
   * Setting up
   ******************************************/

  const initPlans = (plans: string[]) => {
    const options = plans
      .sort()
      .map((plan) => /* HTML */ `<option value="${plan}">${plan}</option>`)
      .join('');

    planSelect.innerHTML = /* HTML */ ` <option value="selectPlans">Select Plans</option>
      ${options}`;
    // Default to something simple
    planSelect.value = 'selectPlans';
  };

  const initStrategies = (strategies: StrategyMap) => {
    strategyDetails.innerHTML =
      `<summary>Strategies</summary>` +
      Object.entries(strategies)
        .map(([category, strategyList]) => {
          const categoryName = category.replace(/(.)([A-Z])/g, '$1 $2').toLowerCase();

          return /* HTML */ `<p>
            <label for="${category}Select">
              ${categoryName.charAt(0).toUpperCase() + categoryName.slice(1)}
            </label>
            <select id="${category}Select" name="${category}">
              <option name="-" selected>-</option>
              ${strategyList
                .map(
                  (strategy) =>
                    /* HTML */ `<option value="${strategy.strategy}">${strategy.strategy}</option>`,
                )
                .join('')}
            </select>
          </p>`;
        })
        .join('');

    strategySelects = Object.keys(strategies).map(
      (category) => el(`${category}Select`) as HTMLSelectElement,
    );
  };

  const displayLastResult = () => {
    const { planId, result } = lastResult;
    sourceElement.innerHTML = JSON.stringify(result, null, 4);
    console.log({ planId, result });
    if (planId === undefined || result === undefined) {
      return;
    }
    if ('error' in result) {
      resultElement.innerHTML = /* HTML */ `Error: ${result.error}<br />Message: ${result.message}`;
    } else {
      resultElement.innerHTML =
        planId === 'selectPlans'
          ? renderPlanSelections(result)
          : `${renderTransformation(result, 1)} ${renderTest(result, planId)}`;
      window.renderMathInElement(resultElement);
      copyTranslationKeysToClipboardOnClick();
      copyTestCodeToClipboardOnClick();
    }
  };

  const getRequestDataFromForm = (): RequestData => {
    const preferredStrategies: { [category: string]: string } = {};
    for (const strategySelect of strategySelects) {
      if (strategySelect.value !== '-') {
        preferredStrategies[strategySelect.name] = strategySelect.value;
      }
    }

    return {
      planId: planSelect.value,
      input: mathInput.value,
      curriculum: curriculumSelect.value,
      /** GM stands for Graspable Math */
      gmFriendly: gmFriendlyCheckbox.checked,
      precision: parseInt(precisionSelect.value),
      preferDecimals: preferDecimalsCheckbox.checked,
      solutionVariable: solutionVariableInput.value,
      preferredStrategies,
    };
  };

  const fetchPlansAndUpdatePage = () =>
    Promise.all([solverSDK.api.listPlans(), solverSDK.api.listStrategies()]).then(
      ([plans, strategies]) => {
        initPlans(plans);
        initStrategies(strategies);
        const url = new URL(window.location.toString());
        const planId = url.searchParams.get('plan');
        const input = url.searchParams.get('input');
        const curriculum = url.searchParams.get('curriculum');
        const gmFriendly = url.searchParams.get('gmFriendly') === '1';
        const precision = url.searchParams.get('precision');
        // Before 2/23/2023, `preferDecimals` may have been set to "true" instead of "1", so
        // we should keep this backwards-compatibility logic here, for a while.
        const temp = url.searchParams.get('preferDecimals');
        const preferDecimals = temp === '1' || temp === 'true';
        const solutionVariable = url.searchParams.get('solutionVariable');
        if (planId) {
          planSelect.value = planId;
        }
        if (input) {
          mathInput.value = input;
        }
        if (curriculum) {
          curriculumSelect.value = curriculum;
        }
        gmFriendlyCheckbox.checked = gmFriendly;
        if (precision) {
          precisionSelect.value = precision;
        }
        preferDecimalsCheckbox.checked = preferDecimals;
        if (solutionVariable) {
          solutionVariableInput.value = solutionVariable;
        }
        for (const strategyChoice of url.searchParams.getAll('strategy')) {
          const [category, choice] = strategyChoice.split(':');
          (el(`${category}Select`) as HTMLSelectElement).value = choice;
        }
        if (planId && input) {
          selectPlansOrApplyPlan({
            planId,
            input,
            curriculum: curriculum || undefined,
            gmFriendly,
            precision: precision ? parseInt(precision) : undefined,
            preferDecimals,
            solutionVariable: solutionVariable || undefined,
          }).then(displayLastResult);
        }
      },
    );

  fetchDefaultTranslations().then(fetchPlansAndUpdatePage);

  solverSDK.api.versionInfo().then((info) => {
    el('version-info')!.innerHTML = info.commit
      ? /* HTML */ `commit
          <a href="https://git.geogebra.org/solver-team/solver-engine/-/commit/${info.commit}"
            >${info.commit.substring(0, 8)}
          </a> `
      : 'no commit info';

    if (info.deploymentName === 'main') {
      el('submitToMain')!.remove();
    } else if (info.deploymentName) {
      if (/^PLUT-\d+$/i.test(info.deploymentName)) {
        el('title')!.innerHTML = /* HTML */ `
          Solver Poker
          <a href="https://geogebra-jira.atlassian.net/browse/${info.deploymentName.toUpperCase()}"
            >${info.deploymentName.toUpperCase()}
          </a>
        `;
      } else {
        el('title')!.innerHTML = `Solver Poker (${info.deploymentName})`;
      }
      document.title = `${info.deploymentName} Solver Poker`;
    }
  });

  inputForm.onsubmit = (evt) => {
    evt.preventDefault();
    const data = getRequestDataFromForm();
    const urlString = buildURLString(window.location.toString(), data);
    history.pushState({ url: urlString }, '', urlString);
    selectPlansOrApplyPlan(data).then(displayLastResult);
  };

  submitToMainButton.onclick = () => {
    const data = getRequestDataFromForm();
    const urlString = buildURLString(mainPokerURL, data);
    window.open(urlString, '_blank');
  };

  const optionsChanged = () => {
    const data = getRequestDataFromForm();
    const urlString = buildURLString(window.location.toString(), data);
    history.replaceState({ url: urlString }, '', urlString);
    if (data.input !== '') {
      selectPlansOrApplyPlan(data).then(displayLastResult);
    }
  };

  const displayOptionsChanged = () => {
    Object.assign(settings, {
      showThroughSteps: showThroughStepsCheckbox.checked,
      showRearrangementSteps: showRearrangementStepsCheckbox.checked,
      showPedanticSteps: showPedanticStepsCheckbox.checked,
      showCosmeticSteps: showCosmeticStepsCheckbox.checked,
      showInvisibleChangeSteps: showInvisibleChangeStepsCheckbox.checked,
      selectedColorScheme: colorSchemeSelect.value as ColorScheme,
      showTranslationKeys: showTranslationKeysCheckbox.checked,
      latexSettings: {
        ...settings.latexSettings,
        solutionFormatter: solutionFormatters[solutionFormatSelect.value as SolutionFormat],
      },
    });

    displayLastResult();
  };

  curriculumSelect.onchange = optionsChanged;
  planSelect.onchange = optionsChanged;
  precisionSelect.onchange = optionsChanged;
  preferDecimalsCheckbox.onchange = optionsChanged;
  gmFriendlyCheckbox.onchange = optionsChanged;

  showThroughStepsCheckbox.onchange = displayOptionsChanged;
  showRearrangementStepsCheckbox.onchange = displayOptionsChanged;
  showPedanticStepsCheckbox.onchange = displayOptionsChanged;
  showCosmeticStepsCheckbox.onchange = displayOptionsChanged;
  showInvisibleChangeStepsCheckbox.onchange = displayOptionsChanged;
  colorSchemeSelect.onchange = displayOptionsChanged;
  solutionFormatSelect.onchange = displayOptionsChanged;

  showTranslationKeysCheckbox.onchange = () => {
    settings.showTranslationKeys = showTranslationKeysCheckbox.checked;
    for (const el of document.getElementsByClassName('translation-key')) {
      el.classList.toggle('hidden', !settings.showTranslationKeys);
    }
  };

  hideWarningsCheckbox.onchange = () => {
    settings.hideWarnings = hideWarningsCheckbox.checked;
    for (const el of document.getElementsByClassName('warning')) {
      el.classList.toggle('hidden', settings.hideWarnings);
    }
  };

  document.onkeydown = (evt) => {
    if (evt.ctrlKey && evt.code === 'KeyD') {
      for (const el of document.getElementsByClassName('hide-in-demo-mode')) {
        el.classList.toggle('hidden');
      }
    }
  };

  window.onpopstate = fetchPlansAndUpdatePage;
};
