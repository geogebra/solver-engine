import { Metadata, Task, Transformation } from '@geogebra/solver-sdk';
import { isThroughStep } from './util';
import { settings } from './settings';

export const renderTest = (trans: Transformation) => /* HTML */ `
  <details>
    <summary>Test Code <button class="copy-test-code-button">Copy</button></summary>
    <pre>${generateTestSuggestion(trans)}</pre>
  </details>
`;

export const copyTestCodeToClipboardOnClick = () => {
  const copyToClipboard = async (evt: Event) => {
    const testCode = (evt.target as HTMLElement)?.parentElement?.nextElementSibling?.textContent;
    if (testCode) {
      await navigator.clipboard.writeText(testCode);
    }
  };
  for (const el of document.querySelectorAll<HTMLElement>('.copy-test-code-button')) {
    el.onclick = copyToClipboard;
  }
};

export function generateTestSuggestion(trans: Transformation, methodId = 'FILL_ME_IN'): string {
  lastExpressionPrinted = '';
  const stringBuilder = new StringBuilder();
  new IndentBuilder(stringBuilder).do(buildTest(trans, methodId));
  return stringBuilder.toString();
}

class StringBuilder {
  lines: string[];

  constructor() {
    this.lines = [];
  }

  addLine(line: string) {
    this.lines.push(line);
  }

  toString() {
    return this.lines.join('\n');
  }
}

let lastExpressionPrinted = '';

class IndentBuilder {
  parent: StringBuilder | IndentBuilder;
  indent: string;

  constructor(parent: StringBuilder | IndentBuilder, indent = '') {
    this.parent = parent;
    this.indent = indent;
  }

  child(indent = '    ') {
    return new IndentBuilder(this, indent);
  }

  addLine(line: string) {
    this.parent.addLine(this.indent + line);
    return this;
  }

  do(writeLines: (builder: IndentBuilder) => void) {
    writeLines(this);
  }

  nest(line: string, writeLines: (builder: IndentBuilder) => void, open = ' {', close = '}') {
    this.addLine(line + open);
    writeLines(this.child());
    this.addLine(close);
  }
}

const buildTest = (trans: Transformation, methodId: string) => (builder: IndentBuilder) => {
  builder.nest('testMethod', (builder) => {
    builder.addLine(`method = ${methodId}`);
    const fromExpr = trans?.fromExpr.toString();
    builder.addLine(`inputExpr = "${fromExpr}"`);
    lastExpressionPrinted = fromExpr;
    builder.nest('check', buildTestTransformation(trans));
  });
};

const buildTestTransformation = (trans: Transformation) => (builder: IndentBuilder) => {
  if (!trans) return;
  const throughStep = isThroughStep(trans);
  if (throughStep && !settings.showThroughSteps) {
    builder.do(buildTestTransformation(trans.steps[0]));
    return;
  }
  if (throughStep) {
    builder.addLine('// Through step');
  } else {
    const fromExpr = trans.fromExpr?.toString();
    if (fromExpr !== lastExpressionPrinted && fromExpr !== undefined) {
      builder.addLine(`fromExpr = "${fromExpr}"`);
      lastExpressionPrinted = fromExpr;
    }
    if (trans.explanation) {
      builder.do(buildExplanation(trans.explanation));
    }
  }
  if (trans.steps) {
    for (const step of trans.steps) {
      builder.nest('step', buildTestTransformation(step));
    }
  }
  if (!throughStep) {
    const toExpr = trans.toExpr?.toString();
    if (toExpr !== lastExpressionPrinted && toExpr !== undefined) {
      builder.addLine(`toExpr = "${toExpr}"`);
      lastExpressionPrinted = toExpr;
    }
  }
  if (trans.tasks) {
    for (const task of trans.tasks) {
      builder.addLine('').nest('task', buildTestTask(task));
    }
  }
};

const buildTestTask = (task: Task) => (builder: IndentBuilder) => {
  builder.addLine(`taskId = "${task.taskId}"`);
  builder.addLine(`startExpr = "${task.startExpr}"`);
  if (task.explanation) {
    builder.do(buildExplanation(task.explanation));
  }
  if (task.steps) {
    for (const step of task.steps) {
      builder.addLine('').nest('step', buildTestTransformation(step));
    }
  }
};

const buildExplanation = (explanation?: Metadata) => (builder: IndentBuilder) => {
  if (explanation) {
    builder.nest(`explanation`, (builder) => {
      // By convention, the name of the explanation enum in the code is
      // [category]Explanation.[name] given that the key is [category].[name]
      const key = explanation.key.replace('.', 'Explanation.');
      builder.addLine(`key = ${key}`);
    });
  }
};
