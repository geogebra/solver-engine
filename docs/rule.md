The **Rule** is the simplest transformation producer. It creates a
transformation with explanation and potentially with associated
skills, but with no substeps. It transforms the input which
matches its pattern into an equivalent expression, but it does
not know when it should apply.

Adding new rules:

- check if a rule which does the transformation you want
  already exists (or one which can be generalized to achieve it)
- decide on which category the new rule fits into
- choose a good descriptive name for the new rule (a rule is an
  action, so the name should start with a verb)
- add the explanation key, make sure that the names of the rule
  and key match
- implement the rule!