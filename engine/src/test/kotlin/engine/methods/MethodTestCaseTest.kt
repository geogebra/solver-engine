package engine.methods

import engine.methods.stepsproducers.Pipeline
import engine.methods.stepsproducers.PipelineItem
import engine.patterns.AnyPattern
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.MetadataMaker
import engine.steps.metadata.metadata
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

class DummyKey : MetadataKey {
    override val keyName = "dummy"
}

class MethodTestCaseTest {

    private val testRuleMetadataKey = DummyKey()
    private val testPlanMetadataKey = DummyKey()
    private val testRuleSkillMetadataKey1 = DummyKey()
    private val testRuleSkillMetadataKey2 = DummyKey()
    private val testRuleSkillMetadataKey3 = DummyKey()

    private val testRule = object : RunnerMethod {
        override val name = "TestName"
        override val runner = rule {
            val pattern = AnyPattern()

            onPattern(pattern) {
                TransformationResult(
                    toExpr = move(pattern),
                    explanation = metadata(testRuleMetadataKey),
                    skills = listOf(
                        metadata(testRuleSkillMetadataKey1, move(pattern)),
                        metadata(testRuleSkillMetadataKey2)
                    )
                )
            }
        }
    }

    private val testPlan = run {
        val pattern = AnyPattern()

        RegularPlan(
            pattern = pattern,
            resultPattern = AnyPattern(),
            stepsProducer = Pipeline((1..3).map { PipelineItem(testRule) }),
            explanationMaker = MetadataMaker(testPlanMetadataKey) { listOf(move(pattern)) }
        )
    }

    @Test
    fun testsFromExprCorrectly() {
        testMethod {
            inputExpr = "1"
            method = testPlan

            check {
                step { }
                step { fromExpr = "1" }
                step { }
            }
        }

        assertFails {
            testMethod {
                inputExpr = "1"
                method = testPlan

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
        testMethod {
            inputExpr = "1"
            method = testPlan

            check {
                step { }
                step { toExpr = "1" }
                step { }
            }
        }

        assertFails {
            testMethod {
                inputExpr = "1"
                method = testPlan

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
            testMethod {
                inputExpr = "1"
                method = testPlan

                check {
                    step { }
                    step { }
                }
            }
        }

        testMethod {
            inputExpr = "1"
            method = testPlan

            check {
                step { }
                step { }
                step { }
            }
        }

        assertFails {
            testMethod {
                inputExpr = "1"
                method = testPlan

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
        testMethod {
            inputExpr = "1"
            method = testPlan

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
            testMethod {
                inputExpr = "1"
                method = testPlan

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
        testMethod {
            inputExpr = "1"
            method = testPlan

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
            testMethod {
                inputExpr = "1"
                method = testPlan

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
        testMethod {
            inputExpr = "1"
            method = testPlan

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
            testMethod {
                inputExpr = "1"
                method = testPlan

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
        testMethod {
            inputExpr = "1"
            method = testPlan

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
            testMethod {
                inputExpr = "1"
                method = testPlan

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
