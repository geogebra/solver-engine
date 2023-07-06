module.exports = {
  branches: [
    // release new SDK package with @latest when on "release" branch
    'release',
    // release new SDK package with @beta tag when on "staging" branch and add the "-staging"
    // suffix to version
    { name: 'staging', channel: 'beta', prerelease: 'staging' },
    // release new SDK package with @alpha tag when on "main" branch and add the "-alpha"
    // suffix to version
    { name: 'main', channel: 'alpha', prerelease: 'alpha' },
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
