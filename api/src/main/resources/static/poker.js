const el = id => document.getElementById(id);

const initPlans = (plans) => {
    el("plansSelect").innerHTML = plans
        .sort()
        .map(plan => `<option value='${plan}'}>${plan}</option>`)
        .join('');
    // Default to something simple
    el("plansSelect").value = "SimplifyArithmeticExpression";
};

const fetchPlans = () =>
    fetch("/api/v1.0-alpha0/plans/")
        .then(response => response.json())
        .then(initPlans);


const applyPlan = async (planId, input) => {
    const response = await fetch(`/api/v1.0-alpha0/plans/${planId}/apply`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            "input": input,
            "format": "latex"
        })
    })
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

const renderTransformation = (trans, depth = 0) => {
    if (trans.steps && trans.steps.length === 1 && trans.steps[0].path === trans.path) {
        return renderTransformation(trans.steps[0], depth);
    }
    return `
    <div class="trans">
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

const renderExplanation = expl => {
    if (!expl) {
        return '';
    }
    return `
    <div>
        explanation: ${expl.key}
        ${expl.params && expl.params.length > 0 ? expl.params.map(p => renderExpression(p.expression)).join() : ''}
    </div>
    `
}

const renderExpression = expr => `\\(\\displaystyle${expr}\\)`;

window.onload = () => {
    fetchPlans().then(() => {
        const url = new URL(window.location);
        const planId = url.searchParams.get("plan")
        const input = url.searchParams.get("input")
        if (planId) {
            el("plansSelect").value = planId;
        }
        if (input) {
            el("input").value = input;
        }
        if (planId && input) {
            applyPlan(planId, input);
        }
    });

    el("form").onsubmit = (evt) => {
        evt.preventDefault();
        const planId = el("plansSelect").value;
        const input = el("input").value;
        const url = new URL(window.location);
        url.searchParams.set("plan", planId);
        url.searchParams.set("input", input);
        const urlString = url.toString();
        history.replaceState({url: urlString}, null, urlString);
        applyPlan(planId, input);
    }
};
