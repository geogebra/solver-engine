### Before moving to Verify, check that

- [ ] Translation keys have been exported if there are new ones
- [ ] Entries were added to the `Changelog.md` file (SDK changes should be of the form `SDK: ...`)
- [ ] If relevant, JIRA ticket description was updated with test inputs and new keys

### When merging to main, check that

- [ ] If the MR contains changes to the SDK, the merge commit message is prefixed so that a SDK release will be
  triggered on main (e.g. `feat:` or `fix:`) 