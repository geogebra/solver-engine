module.exports = {
  branches: [
    // release new SDK package with @latest when on "release" branch
    'release',
    // pre-release new SDK package with @staging tag when on "staging" branch
    { name: 'staging', channel: 'staging', prerelease: 'staging' },
    // pre-release new SDK package with @main tag when on "main" branch
    { name: 'main', channel: 'main', prerelease: 'main' },
    // do a pre-release of SDK package with @plut-xyz tag on "plut-xyz-*" branches
    {
      // this uses [glob notation](https://github.com/micromatch/micromatch#matching-features)
      name: 'plut-+([0-9])-*',
      // version a.b.c-plut-xyz.0
      // this uses [lodash template notation](https://lodash.com/docs/4.17.15#template)
      prerelease: "${name.replace(/(plut-[0-9]+).*/, '$1')}",
      // @plut-xyz tag
      // this uses [lodash template notation](https://lodash.com/docs/4.17.15#template)
      channel: "${name.replace(/(plut-[0-9]+).*/, '$1')}",
    },
  ],
  plugins: [
    '@semantic-release/commit-analyzer',
    '@semantic-release/gitlab',
    '@semantic-release/npm',
    [
      '@semantic-release/git',
      {
        assets: ['package.json'],
        message: 'chore(release): ${nextRelease.version} [skip ci]',
      },
    ],
  ],
};
