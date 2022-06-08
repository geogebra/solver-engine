package plans

import engine.expressionmakers.move
import engine.patterns.AnyPattern
import engine.plans.Pipeline
import engine.plans.PipelineItem
import engine.plans.Plan
import engine.rules.Rule
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.makeMetadata
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

class PlanTestCaseTest {

    private val testRuleMetadataKey = object : MetadataKey {}
    private val testPlanMetadataKey = object : MetadataKey {}
    private val testRuleSkillMetadataKey1 = object : MetadataKey {}
    private val testRuleSkillMetadataKey2 = object : MetadataKey {}
    private val testRuleSkillMetadataKey3 = object : MetadataKey {}

    private val testRule = run {
        val pattern = AnyPattern()

        Rule(
            pattern = pattern,
            resultMaker = move(pattern),
            explanationMaker = makeMetadata(testRuleMetadataKey),
            skillMakers = listOf(
                makeMetadata(testRuleSkillMetadataKey1, move(pattern)),
                makeMetadata(testRuleSkillMetadataKey2),
            ),
        )
    }

    private val testPlan = run {
        val pattern = AnyPattern()

        Plan(
            ownPattern = pattern,
            stepsProducer = Pipeline((1..3).map { PipelineItem(testRule) }),
            explanationMaker = makeMetadata(testPlanMetadataKey, move(pattern)),
        )
    }

    @Test
    fun testsFromExprCorrectly() {
        testPlan {
            inputExpr = "1"
            plan = testPlan

            check {
                step { }
                step { fromExpr = "1" }
                step { }
            }
        }

        assertFails {
            testPlan {
                inputExpr = "1"
                plan = testPlan

                check {
                    step { }
                    step { toExpr = "2" }
                    step { }
                }
            }
        }
    }

    @Test
    fun testsToExprCorrectly() {
        testPlan {
            inputExpr = "1"
            plan = testPlan

            check {
                step { }
                step { toExpr = "1" }
                step { }
            }
        }

        assertFails {
            testPlan {
                inputExpr = "1"
                plan = testPlan

                check {
                    step { }
                    step { toExpr = "2" }
                    step { }
                }
            }
        }
    }

    @Test
    fun testsNumberOfStepsCorrectly() {
        assertFails {
            testPlan {
                inputExpr = "1"
                plan = testPlan

                check {
                    step { }
                    step { }
                }
            }
        }

        testPlan {
            inputExpr = "1"
            plan = testPlan

            check {
                step { }
                step { }
                step { }
            }
        }

        assertFails {
            testPlan {
                inputExpr = "1"
                plan = testPlan

                check {
                    step { }
                    step { }
                    step { }
                    step { }
                }
            }
        }
    }

    @Test
    fun testsPathMappingCorrectly() {
        testPlan {
            inputExpr = "1"
            plan = testPlan

            check {
                step {
                    move {
                        fromPaths(".")
                        toPaths(".")
                    }
                }

                step { }
                step { }
            }
        }

        assertFails {
            testPlan {
                inputExpr = "1"
                plan = testPlan

                check {
                    step {
                        move {
                            fromPaths(".")
                            toPaths("./1")
                        }
                    }

                    step { }
                    step { }
                }
            }
        }
    }

    @Test
    fun testsExplanationKeyCorrectly() {
        testPlan {
            inputExpr = "1"
            plan = testPlan

            check {
                explanation {
                    key = testPlanMetadataKey
                }

                step {
                    explanation {
                        key = testRuleMetadataKey
                    }
                }

                step { }
                step { }
            }
        }

        assertFails {
            testPlan {
                inputExpr = "1"
                plan = testPlan

                check {
                    explanation {
                        key = testPlanMetadataKey
                    }

                    step {
                        explanation {
                            key = testPlanMetadataKey
                        }
                    }

                    step { }
                    step { }
                }
            }
        }
    }

    @Test
    fun testsExplanationPathMappingCorrectly() {
        testPlan {
            inputExpr = "1"
            plan = testPlan

            check {
                explanation {
                    key = testPlanMetadataKey
                    param {
                        move {
                            fromPaths(".")
                            toPaths(".")
                        }
                    }
                }
            }
        }

        assertFails {
            testPlan {
                inputExpr = "1"
                plan = testPlan

                check {
                    explanation {
                        key = testPlanMetadataKey
                        param {
                            move {
                                fromPaths(".")
                                toPaths("./1")
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testsSkillsCorrectly() {
        testPlan {
            inputExpr = "1"
            plan = testPlan

            check {
                step {
                    skill {
                        key = testRuleSkillMetadataKey1

                        param {
                            move {
                                fromPaths(".")
                                toPaths(".")
                            }
                        }
                    }

                    skill {
                        key = testRuleSkillMetadataKey2
                    }
                }

                step { }
                step { }
            }
        }

        assertFails {
            testPlan {
                inputExpr = "1"
                plan = testPlan

                check {
                    step {
                        skill {
                            key = testRuleSkillMetadataKey1
                        }

                        skill {
                            key = testRuleSkillMetadataKey3
                        }
                    }

                    step { }
                    step { }
                }
            }
        }
    }
}