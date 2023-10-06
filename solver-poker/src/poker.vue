<script setup lang="ts">
import type {
  ApiMathFormat,
  SolverContext,
  StrategyMap,
  PlanSelectionJson,
  PlanSelectionSolver,
  ServerErrorResponse,
  TransformationJson,
  TransformationSolver,
} from '@geogebra/solver-sdk';
import * as solverSDK from '@geogebra/solver-sdk';
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

let lastResult:
  | {
      planId: 'selectPlans';
      result: PlanSelectionJson[];
      resultSolverFormat?: PlanSelectionSolver[];
    }
  | {
      planId: Exclude<string, 'selectPlans'>;
      result: TransformationJson;
      resultSolverFormat?: TransformationSolver;
    }
  | { planId?: undefined; result?: undefined; resultSolverFormat?: undefined }
  | {
      planId: string;
      result: ServerErrorResponse;
      resultSolverFormat?: ServerErrorResponse;
    } = {};

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
  const showPedanticStepsCheckbox = el('showPedanticSteps') as HTMLInputElement;
  const showCosmeticStepsCheckbox = el('showCosmeticSteps') as HTMLInputElement;
  const showInvisibleChangeStepsCheckbox = el('showInvisibleChangeSteps') as HTMLInputElement;
  const colorSchemeSelect = el('colorScheme') as HTMLSelectElement;
  const solutionFormatSelect = el('solutionFormat') as HTMLSelectElement;
  const showTranslationKeysCheckbox = el('showTranslationKeys') as HTMLInputElement;
  const hideWarningsCheckbox = el('hideWarnings') as HTMLInputElement;

  const resultElement = el('result') as HTMLElement;
  const sourceElement = el('source') as HTMLElement;

  const jsonFormatCheckbox = el('jsonFormatCheckbox') as HTMLInputElement;
  const responseSourceDetails = el('responseSourceDetails') as HTMLDetailsElement;

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

  const selectPlansOrApplyPlan = async ({
    planId,
    input,
    ...context
  }: { planId: string; input: string } & SolverContext) => {
    if (planId === 'selectPlans') {
      const result = await solverSDK.api.selectPlans(input, jsonFormat, context);
      lastResult = { planId, result };
    } else {
      const result = await solverSDK.api.applyPlan(input, planId, jsonFormat, context);
      lastResult = { planId, result };
    }

    if (!jsonFormatCheckbox.checked && responseSourceDetails.open) {
      lastResult.resultSolverFormat =
        planId === 'selectPlans'
          ? await solverSDK.api.selectPlans(input, 'solver', context)
          : await solverSDK.api.applyPlan(input, planId, 'solver', context);
    }
  };

  const displayLastResult = () => {
    const { planId, result, resultSolverFormat } = lastResult;
    sourceElement.innerHTML = JSON.stringify(
      jsonFormatCheckbox.checked ? result : resultSolverFormat,
      null,
      4,
    );
    console.log(lastResult);
    if (planId === undefined || result === undefined) {
      return;
    }
    if ('error' in result) {
      resultElement.innerHTML = /* HTML */ `Error: ${result.error}<br />Message: ${result.message}`;
    } else {
      resultElement.innerHTML =
        planId === 'selectPlans'
          ? renderPlanSelections(result as PlanSelectionJson[])
          : `${renderTransformation(result as TransformationJson, 1)} ${renderTest(
              result as TransformationJson,
              planId,
            )}`;
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
  jsonFormatCheckbox.onchange = optionsChanged;
  responseSourceDetails.ontoggle = optionsChanged;

  showThroughStepsCheckbox.onchange = displayOptionsChanged;
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
</script>

<template>
  <form class="display-options hide-in-demo-mode">
    <h3>Display Options</h3>
    <label for="colorScheme">Colors</label>
    <select id="colorScheme">
      <option value="default">Default</option>
      <option value="primary">Primary colors</option>
      <option value="none">No coloring</option>
      <option value="blue">Just blue</option>
    </select>
    <br />
    <label for="solutionFormat">Solution format</label>
    <select id="solutionFormat">
      <option value="simple">Simple</option>
      <option value="sets">Sets</option>
    </select>
    <br />
    <input id="showThroughSteps" type="checkbox" />
    <label for="showThroughSteps">Show through steps</label>
    <br />
    <input id="hideWarnings" type="checkbox" />
    <label for="hideWarnings">Hide warnings</label>
    <br />
    <input id="showPedanticSteps" type="checkbox" />
    <label for="showPedanticSteps">Show pedantic steps</label>
    <br />
    <input id="showCosmeticSteps" type="checkbox" />
    <label for="showCosmeticSteps">Show cosmetic steps</label>
    <br />
    <input id="showInvisibleChangeSteps" type="checkbox" />
    <label for="showInvisibleChangeSteps">Show InvisibleChange steps</label>
    <br />
    <input id="showTranslationKeys" type="checkbox" />
    <label for="showTranslationKeys">Show translation keys</label>
  </form>

  <div id="version-info" class="version-info">fetching commit...</div>
  <h1 contenteditable="true" id="title">Solver Poker</h1>
  <details class="hide-in-demo-mode">
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

  <form id="form" class="hide-in-demo-mode">
    <p>
      <label for="curriculumSelect">Curriculum</label>
      <select id="curriculumSelect">
        <option value="" selected>Default</option>
        <option value="US">US</option>
        <option value="EU">EU</option>
      </select>
      <label for="precisionSelect">Precision</label>
      <select id="precisionSelect">
        <option value="2">2 d.p.</option>
        <option value="3" selected>3 d.p.</option>
        <option value="4">4 d.p.</option>
        <option value="5">5 d.p.</option>
        <option value="6">6 d.p.</option>
      </select>
      <label for="solutionVariable">Solution variable</label>
      <input type="text" id="solutionVariable" value="x" size="1" />
      <input id="preferDecimals" type="checkbox" />
      <label for="preferDecimals">Prefer decimals</label>
      <!-- GM stands for Graspable Math -->
      <input id="gmFriendlyCheckbox" type="checkbox" />
      <label for="gmFriendlyCheckbox">GM friendly</label>
    </p>
    <p>
      <label for="plansSelect">Plan</label>
      <select id="plansSelect"></select>
    </p>
    <details id="strategyDetails">
      <summary>Strategies</summary>
    </details>
    <p>
      <label for="input">Input</label>
      <input type="text" id="input" placeholder="Expression" size="30" />
      <input type="submit" id="submit" value="Submit" />
      <input type="button" id="submitToMain" value="Submit to main" />
    </p>
  </form>

  <p id="result"></p>
  <details id="responseSourceDetails" class="hide-in-demo-mode">
    <summary>Response Source</summary>
    <input id="jsonFormatCheckbox" type="checkbox" />
    <label for="jsonFormatCheckbox">JSON Format</label>
    <pre id="source"></pre>
  </details>
</template>

<style>
.plan-id {
  color: blue;
  font-family: monospace;
}

.display-options {
  float: right;
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

.hidden {
  display: none;
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
