import { Metadata, Task, Transformation } from '@geogebra/solver-sdk';
import { isThroughStep } from './util';
import { settings } from './settings';

export const renderTest = (trans: Transformation) => {
  const stringBuilder = new StringBuilder();
  new IndentBuilder(stringBuilder).do(buildTest(trans));
  return /* HTML */ `
    <details>
      <summary>Test Code</summary>
      <pre>${stringBuilder}</pre>
    </details>
  `;
};

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

const buildTest = (trans: Transformation) => (builder: IndentBuilder) => {
  builder.nest('testMethod', (builder) => {
    builder
      .addLine(`method = FILL_ME_IN`)
      .addLine(`inputExpr = "${trans.fromExpr}"`)
      .addLine('')
      .nest('check', buildTestTransformation(trans));
  });
};

const buildTestTransformation = (trans: Transformation) => (builder: IndentBuilder) => {
  const throughStep = isThroughStep(trans);
  if (throughStep && !settings.showThroughSteps) {
    builder.do(buildTestTransformation(trans.steps![0]));
    return;
  }
  if (throughStep) {
    builder.addLine('// Through step');
  } else {
    builder.addLine(`fromExpr = "${trans.fromExpr}"`).addLine(`toExpr = "${trans.toExpr}"`);
    if (trans.explanation) {
      builder.do(buildExplanation(trans.explanation));
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

const buildTestTask = (task: Task) => (builder: IndentBuilder) => {
  builder.addLine(`taskId = "${task.taskId}"`);
  builder.addLine(`startExpr = "${task.startExpr}"`);
  if (task.explanation) {
    builder.do(buildExplanation(task.explanation));
  }
  if (task.steps) {
    for (let step of task.steps) {
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
