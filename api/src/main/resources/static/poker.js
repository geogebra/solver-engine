/* global renderMathInElement */
// If this fails to resolve or is out of date, then run `npm i`.
import * as ggbSolver from './solver-sdk.es.js';
// You may uncomment the line below during development to enable
// typescript-powered intellisense, but the code will not run properly if it
// remains uncommented.
// import * as ggbSolver from '@geogebra/solver-sdk';

// just for debug convenience
window.ggbSolver = ggbSolver;

// This magic number for the port is dictated in the `poker-dev` script in package.json.
const runningLocallyViaVite = location.port === '4173';
ggbSolver.api.baseUrl = runningLocallyViaVite ? 'http://localhost:8080/api/v1' : './api/v1';
const translationsRootURL = 'https://export-solver.s3.eu-west-1.amazonaws.com';
const mainPokerURL = 'http://solver.geogebra.net/main/poker.html';

// Globally changes the rendering of steps.
let showThroughSteps = false;
let showTrivialSteps = false;

// Show / hide warnings
let hideWarnings = false;

const colorSchemes = {
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
let colorScheme = 'default';

const solutionFormatters = {
  sets: ggbSolver.setsSolutionFormatter,
  simple: ggbSolver.simpleSolutionFormatter,
};
let latexSettings = {
  solutionFormatter: solutionFormatters.simple,
};

// Holds all default translations as a key: translation map
let translationData = {};

const el = (id) => document.getElementById(id);

const isThroughStep = (trans) =>
  trans.steps && trans.steps.length === 1 && trans.steps[0].path === trans.path;

const fetchDefaultTranslations = () =>
  fetch(`${translationsRootURL}/en/method-translations.json`, {
    mode: 'cors',
    cache: 'no-cache',
  }).then((resp) => resp.json());

/******************************************
 * Setting up
 ******************************************/

const initPlans = (plans) => {
  const options = plans
    .sort()
    .map((plan) => /* HTML */ `<option value="${plan}">${plan}</option>`)
    .join('');

  el('plansSelect').innerHTML = /* HTML */ ` <option value="selectPlans">Select Plans</option>
    ${options}`;
  // Default to something simple
  el('plansSelect').value = 'selectPlans';
};

/******************************************
 * Functions to execute plans and render the result
 *******************************************/

const selectPlansOrApplyPlan = async ({ planId, input, ...context }) => {
  const [result, solverFormatResult] =
    planId === 'selectPlans'
      ? await Promise.all([
          ggbSolver.api.selectPlans(input, 'json', context),
          ggbSolver.api.selectPlans(input, 'solver', context),
        ])
      : await Promise.all([
          ggbSolver.api.applyPlan(input, planId, 'json', context),
          ggbSolver.api.applyPlan(input, planId, 'solver', context),
        ]);
  el('source').innerHTML = JSON.stringify(solverFormatResult, null, 4);
  if (result.error !== undefined) {
    console.log(result);
    el('result').innerHTML = /* HTML */ `Error: ${result.error}<br />Message: ${result.message}`;
  } else {
    el('result').innerHTML =
      planId === 'selectPlans'
        ? renderPlanSelections(result, solverFormatResult)
        : renderTransformationAndTest(result, solverFormatResult, 1);
    renderMathInElement(el('result'));
  }
};

/******************************************
 * Top-level rendering functions
 *******************************************/

const findTransformationInSelections = (selections, methodId) => {
  for (let selection of selections) {
    if (selection.metadata.methodId === methodId) {
      return selection.transformation;
    }
  }
};

const isTrivialStep = (transformation) => {
  // currently every tagged step is considered trivial -- subject to change
  return !!transformation.tags;
};

const containsNonTrivialStep = (transformation) => {
  if (!transformation.steps || !transformation.steps.length) {
    return !isTrivialStep(transformation);
  }

  return transformation.steps.some(containsNonTrivialStep);
};

/**
 * @param {ggbSolver.API_SELECT_PLANS_RESPONSE} selections
 * @param {ggbSolver.API_SELECT_PLANS_RESPONSE} testSelections
 */
const renderPlanSelections = (selections, testSelections) => {
  if (!showTrivialSteps && selections) {
    selections = (selections || []).filter((selection) =>
      containsNonTrivialStep(selection.transformation),
    );
  }
  if (!selections || selections.length === 0) {
    return /* HTML */ `<div class="selections">No plans found</div>`;
  }
  return /* HTML */ `
    <div class="selections">
      ${selections.length} plans found
      <ol>
        ${selections
          .map(
            (selection) => /* HTML */ `<li>
              <div class="plan-selection">
                <div class="plan-id">${selection.metadata.methodId}</div>
                ${renderTransformationAndTest(
                  selection.transformation,
                  findTransformationInSelections(testSelections, selection.metadata.methodId),
                )}
              </div>
            </li>`,
          )
          .join('')}
      </ol>
    </div>
  `;
};

/**
 * @param {ggbSolver.Transformation} trans
 * @param {ggbSolver.Transformation} testTrans
 * @param {number} depth
 */
const renderTransformationAndTest = (trans, testTrans, depth = 0) => {
  return /* HTML */ ` ${renderTransformation(trans, depth)} ${renderTest(testTrans)}`;
};

/******************************************
 * Rendering a transformation
 ******************************************/

/**
 * @param {ggbSolver.Transformation} trans
 * @param {number} depth
 */
const renderTransformation = (trans, depth = 0) => {
  const isThrough = isThroughStep(trans);
  if (isThrough && !showThroughSteps) {
    return renderTransformation(trans.steps[0], depth);
  }

  const [fromColoring, toColoring] = createColorMaps(trans);
  const render = (expr, coloring) =>
    ggbSolver.treeToLatex(ggbSolver.jsonToTree(expr, trans.path), latexSettings, coloring);
  return /* HTML */ ` <div class="trans ${isThrough ? 'through-step' : ''}">
    ${trans.planId ? `<div class='plan-id'>${trans.planId}</div>` : ''}
    ${renderExplanation(trans.explanation)}
    <div class="expr">
      ${renderExpression(
        `${render(
          trans.fromExpr,
          fromColoring,
        )} {\\color{#8888ff}\\thickspace\\longmapsto\\thickspace} ${render(
          trans.toExpr,
          toColoring,
        )}`,
      )}
    </div>
    ${renderSteps(trans.steps, depth, depth >= 0 || isThrough)}
    ${renderTasks(trans.tasks, depth, depth >= 0)}
  </div>`;
};

/**
 * @param {ggbSolver.Transformation} trans
 */
const createColorMaps = (trans) => {
  // First deal with the special case that the whole expression is transformed.  In this case there is no need to color
  // the path mapping.
  if (trans.pathMappings.length === 1) {
    const mapping = trans.pathMappings[0];
    if (mapping.type === 'Transform' && mapping.fromPaths[0] === trans.path) {
      return [undefined, undefined];
    }
  }
  // Else proceed with finding appropriate colors for the path mappings.
  const colors = colorSchemes[colorScheme];
  return !colors
    ? [undefined, undefined]
    : ggbSolver.createColorMaps(trans.pathMappings, colors).map(ggbSolver.coloringTransformer);
};

const renderSteps = (steps, depth = 0, open = false) => {
  if (steps === null || steps.length === 0) {
    return '';
  }
  const processedSteps = preprocessSteps(steps);
  return /* HTML */ ` <details class="steps" ${open ? 'open' : ''}>
    <summary>${processedSteps.length} ${processedSteps.length === 1 ? 'step' : 'steps'}</summary>
    <ol>
      ${processedSteps
        .map((step) => /* HTML */ `<li>${renderTransformation(step, depth - 1)}</li>`)
        .join('')}
    </ol>
  </details>`;
};

const renderTasks = (tasks, depth = 0, open = false) => {
  if (tasks === null || tasks.length === 0) {
    return '';
  }
  return /* HTML */ ` <details class="tasks" ${open ? 'open' : ''}>
    <summary>${tasks.length} ${tasks.length === 1 ? 'task' : 'tasks'}</summary>
    <ol>
      ${tasks.map((task) => `<li>${renderTask(task, depth - 1)}</li>`).join('')}
    </ol>
  </details>`;
};

const renderTask = (task, depth = 0) => {
  return /* HTML */ `<div class="task">
    ${renderExplanation(task.explanation)}
    ${!task.steps
      ? renderExpression(task.startExpr)
      : task.steps.length === 1
      ? renderTransformation(task.steps[0], depth - 1, depth >= 0)
      : renderSteps(task.steps, depth - 1, depth >= 0)}
  </div>`;
};

const preprocessSteps = (steps) => {
  if (showTrivialSteps || !steps.some((step) => isTrivialStep(step))) {
    return steps;
  }
  // Trivial steps steps are "collapsed" with the previous step if it exists
  const processedSteps = [];
  let lastStep = null;
  for (const step of steps) {
    if (lastStep !== null && isTrivialStep(step)) {
      lastStep.toExpr = step.toExpr;
    } else {
      lastStep = step;
      processedSteps.push(step);
    }
  }
  return processedSteps;
};

const renderWarning = (content) =>
  /* HTML */ `<div class="warning${hideWarnings ? ' hidden' : ''}">${content}</div>`;

const getExplanationString = (expl) => {
  let explanationString = translationData[expl.key];
  let warnings = [];
  if (!explanationString) {
    warnings.push(`Missing default translation for ${expl.key}`);
    explanationString = `${expl.key}(${[...expl.params.keys()].map((i) => `%${i + 1}`).join()})`;
  }

  for (let [i, param] of expl.params.entries()) {
    // replacing "%1", "%2", ... with the respective rendered expression
    if (explanationString.includes('%' + (i + 1))) {
      explanationString = explanationString.replaceAll(
        '%' + (i + 1),
        renderExpression(param.expression),
      );
    } else {
      warnings.push(
        `Missing %${i + 1} in default translation, should contain ${renderExpression(
          param.expression,
        )}`,
      );
    }
  }
  let unusedPlaceholders = explanationString.match(/%[1-9]/g);
  if (unusedPlaceholders) {
    for (let placeholder of unusedPlaceholders) {
      warnings.push(`Missing parameter for placeholder ${placeholder}`);
    }
  }
  return { explanationString, warnings };
};

const renderExplanation = (expl) => {
  if (!expl) {
    return '';
  }

  const { explanationString, warnings } = getExplanationString(expl);

  return /* HTML */ ` <div class="plan-explanation">
    ${explanationString ? /* HTML */ `<div title="${expl.key}">${explanationString}</div>` : ''}
    ${warnings ? warnings.map(renderWarning).join('') : ''}
  </div>`;
};

const renderExpression = (expr) =>
  `\\(\\displaystyle ${
    typeof expr === 'string' ? expr : ggbSolver.jsonToLatex(expr, latexSettings)
  }\\)`;

/******************************************
 * Rendering a plan test source code.
 ******************************************/

class StringBuilder {
  constructor() {
    this.lines = [];
  }

  addLine(line) {
    this.lines.push(line);
  }

  toString() {
    return this.lines.join('\n');
  }
}

class IndentBuilder {
  constructor(parent, indent = '') {
    this.parent = parent;
    this.indent = indent;
  }

  child(indent = '    ') {
    return new IndentBuilder(this, indent);
  }

  addLine(line) {
    this.parent.addLine(this.indent + line);
    return this;
  }

  do(writeLines) {
    writeLines(this);
  }

  nest(line, writeLines, open = ' {', close = '}') {
    this.addLine(line + open);
    writeLines(this.child());
    this.addLine(close);
  }
}

const renderTest = (trans) => {
  const stringBuilder = new StringBuilder();
  new IndentBuilder(stringBuilder).do(buildTest(trans));
  return /* HTML */ `
    <details>
      <summary>Test Code</summary>
      <pre>${stringBuilder}</pre>
    </details>
  `;
};

const buildTest = (trans) => (builder) => {
  builder.nest('testMethod', (builder) => {
    builder
      .addLine(`method = FILL_ME_IN`)
      .addLine(`inputExpr = "${trans.fromExpr}"`)
      .addLine('')
      .nest('check', buildTestTransformation(trans));
  });
};

const buildTestTransformation = (trans) => (builder) => {
  const throughStep = isThroughStep(trans);
  if (throughStep && !showThroughSteps) {
    builder.do(buildTestTransformation(trans.steps[0]));
    return;
  }
  if (throughStep) {
    builder.addLine('// Through step');
  } else {
    builder.addLine(`fromExpr = "${trans.fromExpr}"`).addLine(`toExpr = "${trans.toExpr}"`);
    if (trans.explanation) {
      builder.do(buildExplanation(trans));
    }
  }
  if (trans.steps) {
    for (let step of trans.steps) {
      builder.addLine('').nest('step', buildTestTransformation(step));
    }
  }
  if (trans.tasks) {
    for (let task of trans.tasks) {
      builder.addLine('').nest('task', buildTestTask(task));
    }
  }
};

const buildTestTask = (task) => (builder) => {
  builder.addLine(`taskId = "${task.taskId}"`);
  builder.addLine(`startExpr = "${task.startExpr}"`);
  if (task.explanation) {
    builder.do(buildExplanation(task));
  }
  if (task.steps) {
    for (let step of task.steps) {
      builder.addLine('').nest('step', buildTestTransformation(step));
    }
  }
};

const buildExplanation = (step) => (builder) => {
  let explanation = step.explanation;
  if (explanation) {
    builder.nest(`explanation`, (builder) => {
      // By convention, the name of the explanation enum in the code is
      // [category]Explanation.[name] given that the key is [category].[name]
      const key = explanation.key.replace('.', 'Explanation.');
      builder.addLine(`key = ${key}`);
    });
  }
};

/******************************************
 * Do initial setup and register event handlers
 ******************************************/

const fetchPlansAndUpdatePage = () =>
  ggbSolver.api.listPlans().then((plans) => {
    initPlans(plans);
    const url = new URL(window.location);
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
      el('plansSelect').value = planId;
    }
    if (input) {
      el('input').value = input;
    }
    if (curriculum) {
      el('curriculumSelect').value = curriculum;
    }
    el('gmFriendlyCheckbox').checked = gmFriendly;
    if (precision) {
      el('precisionSelect').value = precision;
    }
    el('preferDecimals').checked = preferDecimals;
    if (solutionVariable) {
      el('solutionVariable').value = solutionVariable;
    }
    if (planId && input) {
      selectPlansOrApplyPlan({
        planId,
        input,
        curriculum,
        gmFriendly,
        precision: parseInt(precision),
        preferDecimals,
        solutionVariable,
      });
    }
  });

const getRequestDataFromForm = () => ({
  planId: el('plansSelect').value,
  input: el('input').value,
  curriculum: el('curriculumSelect').value,
  /** GM stands for Graspable Math */
  gmFriendly: el('gmFriendlyCheckbox').checked,
  precision: parseInt(el('precisionSelect').value),
  preferDecimals: el('preferDecimals').checked,
  solutionVariable: el('solutionVariable').value,
});

const buildURLString = (startURL, data) => {
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
  url.searchParams.set('solutionVariable', data.solutionVariable);
  return url.toString();
};

window.onload = () => {
  fetchDefaultTranslations().then((translations) => {
    console.log('Test translation:', translations.Test);
    translationData = translations;
    fetchPlansAndUpdatePage();
  });

  ggbSolver.api.versionInfo().then((info) => {
    el('version-info').innerHTML = info.commit
      ? /* HTML */ `commit
          <a href="https://git.geogebra.org/solver-team/solver-engine/-/commit/${info.commit}"
            >${info.commit.substring(0, 8)}
          </a> `
      : 'no commit info';

    if (info.deploymentName === 'main') {
      el('submitToMain').remove();
    } else if (info.deploymentName) {
      if (/^PLUT-\d+$/i.test(info.deploymentName)) {
        el('title').innerHTML = /* HTML */ `
          Solver Poker
          <a href="https://geogebra-jira.atlassian.net/browse/${info.deploymentName.toUpperCase()}"
            >${info.deploymentName.toUpperCase()}
          </a>
        `;
      } else {
        el('title').innerHTML = `Solver Poker (${info.deploymentName})`;
      }
      document.title = `${info.deploymentName} Solver Poker`;
    }
  });

  el('form').onsubmit = (evt) => {
    evt.preventDefault();
    const data = getRequestDataFromForm();
    const urlString = buildURLString(window.location, data);
    history.pushState({ url: urlString }, null, urlString);
    selectPlansOrApplyPlan(data);
  };

  el('submitToMain').onclick = () => {
    const data = getRequestDataFromForm();
    const urlString = buildURLString(mainPokerURL, data);
    window.open(urlString, '_blank');
  };

  const optionsChanged = () => {
    showThroughSteps = el('showThroughSteps').checked;
    showTrivialSteps = el('showTrivialSteps').checked;
    colorScheme = el('colorScheme').value;
    latexSettings.solutionFormatter = solutionFormatters[el('solutionFormat').value];

    const data = getRequestDataFromForm();
    const urlString = buildURLString(window.location, data);
    history.replaceState({ url: urlString }, null, urlString);
    if (data.input !== '') {
      selectPlansOrApplyPlan(data);
    }
  };

  el('curriculumSelect').onchange = optionsChanged;
  el('plansSelect').onchange = optionsChanged;
  el('precisionSelect').onchange = optionsChanged;
  el('preferDecimals').onchange = optionsChanged;
  el('gmFriendlyCheckbox').onchange = optionsChanged;
  el('showThroughSteps').onchange = optionsChanged;
  el('showTrivialSteps').onchange = optionsChanged;
  el('colorScheme').onchange = optionsChanged;
  el('solutionFormat').onchange = optionsChanged;

  el('hideWarnings').onchange = (evt) => {
    hideWarnings = evt.target.checked;
    for (const el of document.getElementsByClassName('warning')) {
      el.classList.toggle('hidden', hideWarnings);
    }
  };
};

window.onpopstate = fetchPlansAndUpdatePage;
