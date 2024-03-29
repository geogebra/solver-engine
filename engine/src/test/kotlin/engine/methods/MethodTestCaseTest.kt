/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package engine.methods

import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.UnsignedIntegerPattern
import engine.steps.metadata.FixedKeyMetadataMaker
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

class DummyKey(override val keyName: String) : MetadataKey

class MethodTestCaseTest {
    private val testRuleMetadataKey = DummyKey("Dummy.rule")
    private val testPlanMetadataKey = DummyKey("Dummy.plan")
    private val testRuleSkillMetadataKey1 = DummyKey("Dummy.skill1")
    private val testRuleSkillMetadataKey2 = DummyKey("Dummy.skill2")
    private val testRuleSkillMetadataKey3 = DummyKey("Dummy.skill3")

    private val testRule = object : RunnerMethod {
        override val name = "TestName"
        override val runner = rule {
            val pattern = UnsignedIntegerPattern()

            onPattern(pattern) {
                ruleResult(
                    toExpr = transformTo(pattern, integerOp(pattern) { it + it }),
                    explanation = metadata(testRuleMetadataKey),
                    skills = listOf(
                        metadata(testRuleSkillMetadataKey1, move(pattern)),
                        metadata(testRuleSkillMetadataKey2),
                    ),
                )
            }
        }
    }

    private val testPlan = run {
        val pattern = AnyPattern()

        Plan(
            pattern = pattern,
            resultPattern = AnyPattern(),
            stepsProducer = steps { repeat(3) { apply(testRule) } },
            explanationMaker = FixedKeyMetadataMaker(testPlanMetadataKey) { listOf(move(pattern)) },
        )
    }

    private val testTaskSet = taskSet {
        val ptn = AnyPattern()
        pattern = ptn
        explanation = testPlanMetadataKey

        val applyTestRuleTwice = steps {
            apply(testRule)
            apply(testRule)
        }

        tasks {
            val task1 = task(
                startExpr = get(ptn),
                explanation = metadata(testPlanMetadataKey, get(ptn)),
            )
            task(
                startExpr = task1.result,
                stepsProducer = applyTestRuleTwice,
                explanation = metadata(testPlanMetadataKey, task1.result),
            )
            allTasks()
        }
    }

    @Test
    fun testsFromExprCorrectly() {
        testMethod {
            inputExpr = "1"
            method = testPlan

            check {
                step { }
                step { fromExpr = "2" }
                step { }
            }
        }

        assertFails {
            testMethod {
                inputExpr = "1"
                method = testPlan

                check {
                    step { }
                    step { fromExpr = "3" }
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
                step { toExpr = "4" }
                step { }
            }
        }

        assertFails {
            testMethod {
                inputExpr = "1"
                method = testPlan

                check {
                    step { }
                    step { toExpr = "3" }
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
                    transform {
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

    @Test
    fun testCircularSteps() {
        testMethod {
            inputExpr = "0"
            method = testPlan

            check {
                noTransformation()
            }
        }
    }

    @Test
    fun testsTasksCorrectly() {
        testMethod {
            inputExpr = "1"
            method = testTaskSet

            check {
                task {
                    taskId = "#1"
                    startExpr = "1"
                    noStep()
                }

                task {
                    taskId = "#2"
                    startExpr = "1"

                    step {}
                    step {}
                }
            }
        }

        assertFails {
            testMethod {
                inputExpr = "1"
                method = testTaskSet

                check {
                    task {
                        startExpr = "1"
                        noStep()
                    }

                    task {
                        startExpr = "1"

                        step {}
                        // Missing: step {}
                    }
                }
            }
        }
    }
}
