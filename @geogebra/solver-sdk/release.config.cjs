module.exports = {
  branches: [
    // release new SDK package with @latest when on "release" branch
    'release',
    // release new SDK package with @beta tag when on "staging" branch
    { name: 'staging', channel: 'beta' },
    // release new SDK package with @alpha tag when on "main" branch
    { name: 'main', channel: 'alpha' },
    // do a pre-release of SDK package with @plut-xyz tag on "plut-xyz-*" branches
    { name: 'plut-609-new-sdk-versioning', prerelease: 'plut-609', channel: 'plut-609' },
  ],
  // {
  //   "name": "(plut-[0-9]+)*.",
  //   "prerelease": "${name.replace(/(plut-[0-9]+).*/, '$1')}",
  //   "channel": "${name.replace(/(plut-[0-9]+).*/, '$1')}"
  // },
  plugins: [
    '@semantic-release/commit-analyzer',
    '@semantic-release/release-notes-generator',
    '@semantic-release/gitlab',
    '@semantic-release/npm',
    [
      '@semantic-release/git',
      {
        assets: ['package.json'],
        message:
          'chore(release): ${nextRelease.version} [skip ci]\n\n${nextRelease.notes}',
      },
    ],
  ],
};
