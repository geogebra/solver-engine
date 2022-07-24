const el = id => document.getElementById(id);

let translationData = {}
const loadDefaultTranslations = () => fetch("./DefaultTranslations.json")
    .then(function (resp) {
        return resp.json();
    })
    .then(function (data) {
        translationData = data
    })

const initPlans = (plans) => {
    const options = plans
        .sort()
        .map(plan => `<option value='${plan}'>${plan}</option>`)
        .join('');

    el("plansSelect").innerHTML = `
        <option value="selectPlans">Select Plans</option>
        ${options}`;
    // Default to something simple
    el("plansSelect").value = "selectPlans";
};

const fetchPlans = () =>
    fetch("/api/v1.0-alpha0/plans/")
        .then(response => response.json())
        .then(initPlans);

const selectPlansOrApplyPlan = (planId, input, curriculum) => {
    if (planId === "selectPlans") {
        return selectPlans(input, curriculum);
    } else {
        return applyPlan(planId, input, curriculum);
    }
}

const applyPlan = async (planId, input, curriculum = "") => {
    const response = await fetch(`/api/v1.0-alpha0/plans/${planId}/apply`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            "input": input,
            "format": "latex",
            "curriculum": curriculum,
        })
    });
    const result = await response.json();
    if (result.error !== undefined) {
        console.log(result);
        el("result").innerHTML = `Error: ${result.error}<br/>Message: ${result.message}`;
    } else {
        console.log(result);
        el("result").innerHTML = renderTransformation(result, 1);
        renderMathInElement(el("result"));
    }
};

const selectPlans = async (input, curriculum = "") => {
    const response = await fetch(`/api/v1.0-alpha0/selectPlans`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            "input": input,
            "format": "latex",
            "curriculum": curriculum,
        })
    });
    const result = await response.json();
    if (result.error !== undefined) {
        console.log(result);
        el("result").innerHTML = `Error: ${result.error}<br/>Message: ${result.message}`;
    } else {
        console.log(result);
        el("result").innerHTML = renderPlanSelections(result);
        renderMathInElement(el("result"));
    }
}

const renderPlanSelections = (selections) => {
    if (!selections || selections.length === 0) {
        return `<div class="selections">No plans found</div>`;
    }
    return `
    <div class ="selections">${selections.length} plans found
        <ol>
            ${selections.map(selection => `<li>${renderPlanSelection(selection)}</li>`).join('')}
        </ol>
    </div>
    `;
}

const renderPlanSelection = (selection) => {
    return `
    <div class="plan-selection">
        <div class="plan-id">${selection.metadata.methodId}</div>
        ${renderTransformation(selection.transformation)}
    </div>`;
}

const renderTransformation = (trans, depth = 0) => {
    if (trans.steps && trans.steps.length === 1 && trans.steps[0].path === trans.path) {
        return renderTransformation(trans.steps[0], depth);
    }
    return `
    <div class="trans">
        ${trans.planId ? `<div class="plan-id">${trans.planId}</div>` : ""}
        ${renderExplanation(trans.explanation)}
        <div>${renderExpression(`${trans.fromExpr} {\\color{#8888ff}\\thickspace\\longmapsto\\thickspace} ${trans.toExpr}`)}</div>
        ${renderSteps(trans.steps, depth)}
    </div>`;
};

const renderSteps = (steps, depth = 0) => {
    if (steps === null || steps.length === 0) {
        return '';
    }
    return `
    <details class="steps" ${depth > 0 ? "open" : ""}>
        <summary>${steps.length} steps</summary>
        <ol>
            ${steps.map(step => `<li>${renderTransformation(step, depth - 1)}</li>`).join('')}
        </ol>
    </details>`;
};

const renderWarning = (content) => `<div class="warning">${content}</div>`

const getExplanationString = (expl) => {
    let explanationString = translationData[expl.key];
    let warnings = [];
    if (!explanationString) {
        warnings.push(`Missing default translation for ${expl.key}`)
        explanationString = `${expl.key}(${[...expl.params.keys()].map(i => `%${i + 1}`).join()})`
    }

    for (let [i, param] of expl.params.entries()) {
        // replacing "%1", "%2", ... with the respective rendered expression
        if (explanationString.includes("%" + (i + 1))) {
            explanationString = explanationString.replace("%" + (i + 1), renderExpression(param.expression))
        } else {
            warnings.push(`Missing %${i + 1} in default translation, should contain ${renderExpression(param.expression)}`);
        }
    }
    let unusedPlaceholders = explanationString.match(/%[1-9]/g)
    if (unusedPlaceholders) {
        for (let placeholder of unusedPlaceholders) {
            warnings.push(`Missing parameter for placeholder ${placeholder}`)
        }
    }
    return {explanationString, warnings}
}

const renderExplanation = expl => {
    if (!expl) {
        return '';
    }

    const {explanationString, warnings} = getExplanationString(expl)

    return `
    <div>
        ${explanationString ? `<div title="${expl.key}">${explanationString}</div>` : ""}
        ${warnings ? warnings.map(renderWarning).join("") : ""}
    </div>`;
}

const renderExpression = expr => `\\(\\displaystyle${expr}\\)`;

window.onload = () => {
    loadDefaultTranslations()
    fetchPlans().then(() => {
        const url = new URL(window.location);
        const planId = url.searchParams.get("plan")
        const input = url.searchParams.get("input")
        const curriculum = url.searchParams.get("curriculum")
        if (planId) {
            el("plansSelect").value = planId;
        }
        if (input) {
            el("input").value = input;
        }
        if (curriculum) {
            el("curriculumSelect").value = curriculum
        }
        if (planId && input) {
            selectPlansOrApplyPlan(planId, input, curriculum);
        }
    });

    el("form").onsubmit = (evt) => {
        evt.preventDefault();
        const planId = el("plansSelect").value;
        const input = el("input").value;
        const curriculum = el("curriculumSelect").value;
        const url = new URL(window.location);
        url.searchParams.set("plan", planId);
        url.searchParams.set("input", input);
        const urlString = url.toString();
        history.replaceState({url: urlString}, null, urlString);
        selectPlansOrApplyPlan(planId, input, curriculum);
    }
};
