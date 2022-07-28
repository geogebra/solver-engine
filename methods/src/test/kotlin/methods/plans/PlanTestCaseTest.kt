package methods.plans

import engine.expressionmakers.move
import engine.methods.Plan
import engine.methods.Rule
import engine.methods.stepsproducers.Pipeline
import engine.methods.stepsproducers.PipelineItem
import engine.patterns.AnyPattern
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.makeMetadata
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

class DummyKey : MetadataKey {
    override val keyName = "dummy"
}

class PlanTestCaseTest {

    private val testRuleMetadataKey = DummyKey()
    private val testPlanMetadataKey = DummyKey()
    private val testRuleSkillMetadataKey1 = DummyKey()
    private val testRuleSkillMetadataKey2 = DummyKey()
    private val testRuleSkillMetadataKey3 = DummyKey()

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
            pattern = pattern,
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
