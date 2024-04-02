module.exports = {
  branches: [
    // release new SDK package with @latest when on "release" branch
    'release',

    // We set staging and main to do pre-releases instead of full releases,
    // because semantic-release enforces that the versions numbers are
    // release <= staging <= main otherwise. This would conflict with our
    // requirement to do a new SDK release after merging into staging and
    // release branches as the SDK needs to point to a different backend
    // URL.

    // pre-release new SDK package with @staging tag when on "staging" branch
    { name: 'staging', channel: 'staging', prerelease: 'staging' },
    // pre-release new SDK package with @main tag when on "main" branch
    { name: 'main', channel: 'main', prerelease: 'main' },

    // We don't do pre-releases on feature branches, because rebasing them on
    // main leads to lost git-tags and semantic-release will try to re-create
    // existing git-tags and fail.
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
    [
      '@semantic-release/exec',
      {
        publishCmd: [
          'echo "${nextRelease.version}" >> version.env',
          'echo "$(cat ./solver-sdk/license.txt ./solver-sdk/dist/solver-sdk.es.js)" > ./solver-sdk/dist/solver-sdk.es.js',
          'echo "$(cat ./solver-sdk/license.txt ./solver-sdk/dist/solver-sdk.umd.js)" > ./solver-sdk/dist/solver-sdk.umd.js',
          'aws s3 sync ./dist s3://$SDK_BUCKET/solver-sdk/$(cat version.env)/ --exclude="*" --include="solver-sdk.*.js"',
        ].join(' && '),
      },
    ],
  ],
};
