The files in this directory were taken
from [the openapi-generator](https://github.com/OpenAPITools/openapi-generator/tree/v7.2.0/modules/openapi-generator/src/main/resources/kotlin-spring)
repository and customized.

The customization consists of allowing for async requests so we can interrupt the thread if it takes too long.

If the version of openapi-generator is upgraded, these files need to be copied again and the same customization applied.
To make it clear what the customizations are, we should have one separate commit on the main branch whose sole changeset
is adding files from the repository as they are. Further commits can then customize them.

