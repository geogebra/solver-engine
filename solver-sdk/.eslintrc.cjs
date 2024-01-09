module.exports = {
  env: {
    node: true,
  },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'eslint-config-prettier',
  ],
  rules: {
    // override/add rules settings here, such as:
    '@typescript-eslint/no-non-null-assertion': 0,
    '@typescript-eslint/no-explicit-any': 0,
    '@typescript-eslint/ban-ts-comment': 0,
    // the 'error' part of the line below isn't the point, but the
    // `{ checkLoops: false }` is used in the code
    'no-constant-condition': ['error', { checkLoops: false }],
  },
};
