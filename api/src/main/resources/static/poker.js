/* global renderMathInElement */

const apiRoot = "./api/v1.0-alpha0";
const translationsRootURL = "https://export-solver.s3.eu-west-1.amazonaws.com";
const mainPokerURL = "http://solver.geogebra.net/main/poker.html";

// Globally changes the rendering of steps.
let showThroughSteps = false;
let showRearrangements = false;

// Show / hide warnings
let hideWarnings = false;

// Holds all default translations as a key: translation map
let translationData = {};

const el = (id) => document.getElementById(id);

const isThroughStep = (trans) =>
    trans.steps && trans.steps.length === 1 && trans.steps[0].path === trans.path;

/******************************************
 * API access
 ******************************************/

const requestApplyPlan = async (planId, input, context, format = "latex") => {
    const response = await fetch(`${apiRoot}/plans/${planId}/apply`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ input, format, context }),
    });
    return await response.json();
};

const requestSelectPlans = async (input, context, format = "latex") => {
    const response = await fetch(`${apiRoot}/selectPlans`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ input, format, context }),
    });
    return await response.json();
};

const fetchPlans = () => fetch(`${apiRoot}/plans/`).then((response) => response.json());

const fetchDefaultTranslations = () =>
    fetch(`${translationsRootURL}/en/method-translations.json`, {
        mode: "cors",
        cache: "no-cache",
    }).then((resp) => resp.json());

const fetchVersionInfo = () => fetch(`${apiRoot}/versionInfo`).then((resp) => resp.json());

/******************************************
 * Setting up
 ******************************************/

const initPlans = (plans) => {
    const options = plans
        .sort()
        .map((plan) => /* HTML */ `<option value="${plan}">${plan}</option>`)
        .join("");

    el("plansSelect").innerHTML = /* HTML */ ` <option value="selectPlans">Select Plans</option>
        ${options}`;
    // Default to something simple
    el("plansSelect").value = "selectPlans";
};

/******************************************
 * Functions to execute plans and render the result
 *******************************************/

const selectPlansOrApplyPlan = ({ planId, input, ...context }) => {
    if (planId === "selectPlans") {
        return selectPlans(input, context);
    } else {
        return applyPlan(planId, input, context);
    }
};

const applyPlan = async (planId, input, context) => {
    const result = await requestApplyPlan(planId, input, context);
    el("source").innerHTML = JSON.stringify(result, null, 4);
    if (result.error !== undefined) {
        console.log(result);
        el("result").innerHTML = /* HTML */ `Error: ${result.error}<b />Message: ${result.message}`;
    } else {
        const solverResult = await requestApplyPlan(planId, input, context, "solver");
        el("result").innerHTML = renderTransformationAndTest(result, solverResult, 1);
        renderMathInElement(el("result"));
    }
};

const selectPlans = async (input, context) => {
    const result = await requestSelectPlans(input, context);
    el("source").innerHTML = JSON.stringify(result, null, 4);
    if (result.error !== undefined) {
        console.log(result);
        el("result").innerHTML = /* HTML */ `Error: ${result.error}<br />Message: ${result.message}`;
    } else {
        console.log(result);
        const testResult = await requestSelectPlans(input, context, "solver");
        el("result").innerHTML = renderPlanSelections(result, testResult);
        renderMathInElement(el("result"));
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

const renderPlanSelections = (selections, testSelections) => {
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
                            ${renderPlanSelection(
                                selection,
                                findTransformationInSelections(
                                    testSelections,
                                    selection.metadata.methodId
                                )
                            )}
                        </li>`
                    )
                    .join("")}
            </ol>
        </div>
    `;
};

const renderPlanSelection = (selection, testTransformation) => {
    return /* HTML */ ` <div class="plan-selection">
        <div class="plan-id">${selection.metadata.methodId}</div>
        ${renderTransformationAndTest(selection.transformation, testTransformation)}
    </div>`;
};

const renderTransformationAndTest = (trans, testTrans, depth = 0) => {
    return /* HTML */ ` ${renderTransformation(trans, depth)} ${renderTest(testTrans)}`;
};

/******************************************
 * Rendering a transformation
 ******************************************/

const renderTransformation = (trans, depth = 0) => {
    const isThrough = isThroughStep(trans);
    if (isThrough && !showThroughSteps) {
        return renderTransformation(trans.steps[0], depth);
    }
    return /* HTML */ ` <div class="trans ${isThrough ? "through-step" : ""}">
        ${trans.planId ? `<div class="plan-id">${trans.planId}</div>` : ""}
        ${renderExplanation(trans.explanation)}
        <div class="expr">
            ${renderExpression(
                `${trans.fromExpr} {\\color{#8888ff}\\thickspace\\longmapsto\\thickspace} ${trans.toExpr}`
            )}
        </div>
        ${renderSteps(trans.steps, depth, depth >= 0 || isThrough)}
        ${renderTasks(trans.tasks, depth, depth >= 0)}
    </div>`;
};

const renderSteps = (steps, depth = 0, open = false) => {
    if (steps === null || steps.length === 0) {
        return "";
    }
    const processedSteps = preprocessSteps(steps);
    return /* HTML */ ` <details class="steps" ${open ? "open" : ""}>
        <summary>
            ${processedSteps.length} ${processedSteps.length === 1 ? "step" : "steps"}
        </summary>
        <ol>
            ${processedSteps
                .map((step) => /* HTML */ `<li>${renderTransformation(step, depth - 1)}</li>`)
                .join("")}
        </ol>
    </details>`;
};

const renderTasks = (tasks, depth = 0, open = false) => {
    if (tasks === null || tasks.length === 0) {
        return "";
    }
    return /* HTML */ ` <details class="tasks" ${open ? "open" : ""}>
        <summary>${tasks.length} ${tasks.length === 1 ? "task" : "tasks"}</summary>
        <ol>
            ${tasks.map((task) => `<li>${renderTask(task, depth - 1)}</li>`).join("")}
        </ol>
    </details>`;
};

const renderTask = (task, depth = 0) => {
    return /* HTML */ `<div class="task">
        Task ${task.taskId}: ${renderExplanation(task.explanation)}
        ${!task.steps
            ? renderExpression(task.startExpr)
            : task.steps.length === 1
            ? renderTransformation(task.steps[0], depth - 1, depth >= 0)
            : renderSteps(task.steps, depth - 1, depth >= 0)}
    </div>`;
};

const preprocessSteps = (steps) => {
    if (showRearrangements || !steps.some((step) => step.type === "Rearrangement")) {
        return steps;
    }
    // Rearrangement steps are "collapsed" with the previous step if it exists
    const processedSteps = [];
    let lastStep = null;
    for (const step of steps) {
        if (lastStep !== null && step.type === "Rearrangement") {
            lastStep.toExpr = step.toExpr;
        } else {
            lastStep = step;
            processedSteps.push(step);
        }
    }
    return processedSteps;
};

const renderWarning = (content) =>
    /* HTML */ `<div class="warning${hideWarnings ? " hidden" : ""}">${content}</div>`;

const getExplanationString = (expl) => {
    let explanationString = translationData[expl.key];
    let warnings = [];
    if (!explanationString) {
        warnings.push(`Missing default translation for ${expl.key}`);
        explanationString = `${expl.key}(${[...expl.params.keys()]
            .map((i) => `%${i + 1}`)
            .join()})`;
    }

    for (let [i, param] of expl.params.entries()) {
        // replacing "%1", "%2", ... with the respective rendered expression
        if (explanationString.includes("%" + (i + 1))) {
            explanationString = explanationString.replaceAll(
                "%" + (i + 1),
                renderExpression(param.expression)
            );
        } else {
            warnings.push(
                `Missing %${i + 1} in default translation, should contain ${renderExpression(
                    param.expression
                )}`
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
        return "";
    }

    const { explanationString, warnings } = getExplanationString(expl);

    return /* HTML */ ` <div class="plan-explanation">
        ${explanationString ? /* HTML */ `<div title="${expl.key}">${explanationString}</div>` : ""}
        ${warnings ? warnings.map(renderWarning).join("") : ""}
    </div>`;
};

const renderExpression = (expr) => `\\(\\displaystyle ${expr}\\)`;

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
        return this.lines.join("\n");
    }
}

class IndentBuilder {
    constructor(parent, indent = "") {
        this.parent = parent;
        this.indent = indent;
    }

    child(indent = "    ") {
        return new IndentBuilder(this, indent);
    }

    addLine(line) {
        this.parent.addLine(this.indent + line);
        return this;
    }

    do(writeLines) {
        writeLines(this);
    }

    nest(line, writeLines, open = " {", close = "}") {
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
    builder.nest("testMethod", (builder) => {
        builder
            .addLine(`method = FILL_ME_IN`)
            .addLine(`inputExpr = "${trans.fromExpr}"`)
            .addLine("")
            .nest("check", buildTestTransformation(trans));
    });
};

const buildTestTransformation = (trans) => (builder) => {
    const throughStep = isThroughStep(trans);
    if (throughStep && !showThroughSteps) {
        builder.do(buildTestTransformation(trans.steps[0]));
        return;
    }
    if (throughStep) {
        builder.addLine("// Through step");
    } else {
        builder.addLine(`fromExpr = "${trans.fromExpr}"`).addLine(`toExpr = "${trans.toExpr}"`);
        if (trans.explanation) {
            builder.do(buildExplanation(trans));
        }
    }
    if (trans.steps) {
        for (let step of trans.steps) {
            builder.addLine("").nest("step", buildTestTransformation(step));
        }
    }
    if (trans.tasks) {
        for (let task of trans.tasks) {
            builder.addLine("").nest("task", buildTestTask(task));
        }
    }
};

const buildTestTask = (task) => (builder) => {
    builder.addLine(`taskId = "${task.taskId}"`);
    if (task.explanation) {
        builder.do(buildExplanation(task));
    }
    if (task.steps) {
        for (let step of task.steps) {
            builder.addLine("").nest("step", buildTestTransformation(step));
        }
    }
};

const buildExplanation = (step) => (builder) => {
    let explanation = step.explanation;
    if (explanation) {
        builder.nest(`explanation`, (builder) => {
            // By convention, the name of the explanation enum in the code is
            // [category]Explanation.[name] given that the key is [category].[name]
            const key = explanation.key.replace(".", "Explanation.");
            builder.addLine(`key = ${key}`);
        });
    }
};

/******************************************
 * Do initial setup and register event handlers
 ******************************************/

const fetchPlansAndUpdatePage = () =>
    fetchPlans().then((plans) => {
        initPlans(plans);
        const url = new URL(window.location);
        const planId = url.searchParams.get("plan");
        const input = url.searchParams.get("input");
        const curriculum = url.searchParams.get("curriculum");
        const precision = url.searchParams.get("precision");
        const preferDecimals = url.searchParams.get("preferDecimals");
        const solutionVariable = url.searchParams.get("solutionVariable");
        if (planId) {
            el("plansSelect").value = planId;
        }
        if (input) {
            el("input").value = input;
        }
        if (curriculum) {
            el("curriculumSelect").value = curriculum;
        }
        if (precision) {
            el("precisionSelect").value = precision;
        }
        if (preferDecimals) {
            el("preferDecimals").checked = true;
        }
        if (solutionVariable) {
            el("solutionVariable").value = solutionVariable;
        }
        if (planId && input) {
            selectPlansOrApplyPlan({
                planId,
                input,
                curriculum,
                precision: parseInt(precision),
                preferDecimals: preferDecimals === "true",
                solutionVariable,
            });
        }
    });

const getRequestDataFromForm = () => ({
    planId: el("plansSelect").value,
    input: el("input").value,
    curriculum: el("curriculumSelect").value,
    precision: parseInt(el("precisionSelect").value),
    preferDecimals: el("preferDecimals").checked,
    solutionVariable: el("solutionVariable").value,
});

const buildURLString = (startURL, data) => {
    const url = new URL(startURL);
    url.searchParams.set("plan", data.planId);
    url.searchParams.set("input", data.input);
    if (data.curriculum) {
        url.searchParams.set("curriculum", data.curriculum);
    } else {
        url.searchParams.delete("curriculum");
    }
    url.searchParams.set("precision", data.precision.toString());
    if (data.preferDecimals) {
        url.searchParams.set("preferDecimals", "true");
    } else {
        url.searchParams.delete("preferDecimals");
    }
    url.searchParams.set("solutionVariable", data.solutionVariable);
    return url.toString();
};

window.onload = () => {
    fetchDefaultTranslations().then((translations) => {
        console.log("Test translation:", translations.Test);
        translationData = translations;
        fetchPlansAndUpdatePage();
    });

    fetchVersionInfo().then((info) => {
        el("version-info").innerHTML = info.commit
            ? /* HTML */ `commit
                  <a
                      href="https://git.geogebra.org/solver-team/solver-engine/-/commit/${info.commit}"
                      >${info.commit.substring(0, 8)}
                  </a> `
            : "no commit info";

        if (info.deploymentName === "main") {
            el("submitToMain").remove();
        } else if (info.deploymentName) {
            if (/^PLUT-\d+$/i.test(info.deploymentName)) {
                el("title").innerHTML = /* HTML */ `
                    Solver Poker
                    <a
                        href="https://geogebra-jira.atlassian.net/browse/${info.deploymentName.toUpperCase()}"
                        >${info.deploymentName.toUpperCase()}
                    </a>
                `;
            } else {
                el("title").innerHTML = `Solver Poker (${info.deploymentName})`;
            }
            document.title = `${info.deploymentName} Solver Poker`;
        }
    });

    el("form").onsubmit = (evt) => {
        evt.preventDefault();
        const data = getRequestDataFromForm();
        const urlString = buildURLString(window.location, data);
        history.pushState({ url: urlString }, null, urlString);
        selectPlansOrApplyPlan(data);
    };

    el("submitToMain").onclick = () => {
        const data = getRequestDataFromForm();
        const urlString = buildURLString(mainPokerURL, data);
        window.open(urlString, "_blank");
    };

    el("showThroughSteps").onchange = (evt) => {
        showThroughSteps = evt.target.checked;
        const data = getRequestDataFromForm();
        if (data.input !== "") {
            selectPlansOrApplyPlan(data);
        }
    };

    el("showRearrangements").onchange = (evt) => {
        showRearrangements = evt.target.checked;
        const data = getRequestDataFromForm();
        if (data.input !== "") {
            selectPlansOrApplyPlan(data);
        }
    };

    el("hideWarnings").onchange = (evt) => {
        hideWarnings = evt.target.checked;
        for (const el of document.getElementsByClassName("warning")) {
            el.classList.toggle("hidden", hideWarnings);
        }
    };
};

window.onpopstate = fetchPlansAndUpdatePage;
