package methods.equations

import engine.methods.testMethodInX
import methods.angles.AnglesExplanation
import org.junit.jupiter.api.Test

@Suppress("LongMethod", "LargeClass")
class TrigonometricEquationsTest {
    @Test
    fun `test sin(f(x)) = sin(g(y)) trig equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[[/pi/ / 3] - x] = sin[2 x]"

            check {
                fromExpr = "sin[[/pi/ / 3] - x] = sin[2 x]"
                toExpr = "(x = [/pi/ / 9] + [2 k * /pi/ / 3] OR x = [2 /pi/ / 3] + 2 k * /pi/) " +
                    "GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "arcsin[sin[[/pi/ / 3] - x]] = arcsin[sin[2 x]]"
                }

                step {
                    toExpr = "[/pi/ / 3] - x = arcsin[sin[2 x]]"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "[/pi/ / 3] - x = arcsin[sin[2 x]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[/pi/ / 3] - x = /pi/ - arcsin[sin[2 x]]"
                    }

                    task {}
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[[/pi/ / 3] - x] - sin[2 x] = 0"

            check {
                fromExpr = "sin[[/pi/ / 3] - x] - sin[2 x] = 0"
                toExpr = "(x = [/pi/ / 9] + [2 k * /pi/ / 3] OR x = [2 /pi/ / 3] + 2 k * /pi/) " +
                    "GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "sin[[/pi/ / 3] - x] = sin[2 x]"
                }

                step {
                    toExpr = "arcsin[sin[[/pi/ / 3] - x]] = arcsin[sin[2 x]]"
                }

                step {
                    toExpr = "[/pi/ / 3] - x = arcsin[sin[2 x]]"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "[/pi/ / 3] - x = arcsin[sin[2 x]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[/pi/ / 3] - x = /pi/ - arcsin[sin[2 x]]"
                    }

                    task {}
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "-sin[[/pi/ / 3] - x] + sin[2 x] = 0"

            check {
                fromExpr = "-sin[[/pi/ / 3] - x] + sin[2 x] = 0"
                toExpr = "(x = [/pi/ / 9] + [2 k * /pi/ / 3] OR x = [2 /pi/ / 3] + 2 k * /pi/) " +
                    "GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "sin[2 x] = sin[[/pi/ / 3] - x]"
                }

                step {
                    toExpr = "arcsin[sin[2 x]] = arcsin[sin[[/pi/ / 3] - x]]"
                }

                step {
                    toExpr = "2 x = arcsin[sin[[/pi/ / 3] - x]]"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "2 x = arcsin[sin[[/pi/ / 3] - x]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "2 x = /pi/ - arcsin[sin[[/pi/ / 3] - x]]"
                    }

                    task {}
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[[/pi/ / 3] - x] = -sin[2 x]"

            check {
                fromExpr = "sin[[/pi/ / 3] - x] = -sin[2 x]"
                toExpr = "(x = 2 k * /pi/ - [/pi/ / 3] OR x = -[2 /pi/ / 9] + [2 k * /pi/ / 3]) " +
                    "GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "sin[[/pi/ / 3] - x] = sin[-2 x]"
                }

                step {
                    toExpr = "arcsin[sin[[/pi/ / 3] - x]] = arcsin[sin[-2 x]]"
                }

                step {
                    toExpr = "[/pi/ / 3] - x = arcsin[sin[-2 x]]"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "[/pi/ / 3] - x = arcsin[sin[-2 x]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[/pi/ / 3] - x = /pi/ - arcsin[sin[-2 x]]"
                    }

                    task {}
                }
            }
        }
    }

    @Test
    fun `test sin(x) = sin(c) trig equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[2 x] = sin[[2 /pi/ / 7]]"

            check {
                fromExpr = "sin[2 x] = sin[[2 /pi/ / 7]]"
                toExpr = "(x = [/pi/ / 7] + k * /pi/ OR x = [5 /pi/ / 14] + k * /pi/) GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "arcsin[sin[2 x]] = arcsin[sin[[2 /pi/ / 7]]]"
                }

                step {
                    toExpr = "2 x = arcsin[sin[[2 /pi/ / 7]]]"
                }

                step {
                    toExpr =
                        "(x = [/pi/ / 7] + k * /pi/ OR x = [5 /pi/ / 14] + k * /pi/) GIVEN SetSolution[k: /integers/]"

                    task {
                        taskId = "#1"
                        startExpr = "2 x = arcsin[sin[[2 /pi/ / 7]]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "2 x = /pi/ - arcsin[sin[[2 /pi/ / 7]]]"
                    }

                    task { }
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[2 x] + sin[[2 /pi/ / 7]] = 0"

            check {
                fromExpr = "sin[2 x] + sin[[2 /pi/ / 7]] = 0"
                toExpr =
                    "(x = -[/pi/ / 7] + k * /pi/ OR x = [9 /pi/ / 14] + k * /pi/) GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "sin[2 x] = -sin[[2 /pi/ / 7]]"
                }

                step {
                    toExpr = "sin[2 x] = sin[-[2 /pi/ / 7]]"
                }

                step {
                    toExpr = "arcsin[sin[2 x]] = arcsin[sin[-[2 /pi/ / 7]]]"
                }

                step {
                    toExpr = "2 x = arcsin[sin[-[2 /pi/ / 7]]]"
                }

                step {
                    toExpr =
                        "(x = -[/pi/ / 7] + k * /pi/ OR x = [9 /pi/ / 14] + k * /pi/) GIVEN SetSolution[k: /integers/]"

                    task {
                        taskId = "#1"
                        startExpr = "2 x = arcsin[sin[-[2 /pi/ / 7]]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "2 x = /pi/ - arcsin[sin[-[2 /pi/ / 7]]]"
                    }

                    task {}
                }
            }
        }
    }

    @Test
    fun `test sin(x) = c trig equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[x] = [1 / 2]"

            check {
                fromExpr = "sin[x] = [1 / 2]"
                toExpr = "(x = [/pi/ / 6] + 2 k * /pi/ OR x = [5 /pi/ / 6] + 2 k * /pi/) " +
                    "GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "arcsin[sin[x]] = arcsin[[1 / 2]]"
                }

                step {
                    toExpr = "x = arcsin[[1 / 2]]"
                }

                step {
                    toExpr = "(x = [/pi/ / 6] + 2 k * /pi/ OR x = [5 /pi/ / 6] + 2 k * /pi/) " +
                        "GIVEN SetSolution[k: /integers/]"

                    task {
                        taskId = "#1"
                        startExpr = "x = arcsin[[1 / 2]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x = /pi/ - arcsin[[1 / 2]]"
                    }

                    task {
                        taskId = "#3"
                        startExpr = "(x = [/pi/ / 6] + 2 k * /pi/ OR x = [5 /pi/ / 6] + 2 k * /pi/) " +
                            "GIVEN SetSolution[k: /integers/]"
                        explanation {
                            key = EquationsExplanation.CollectSolutions
                        }
                    }
                }
            }
        }
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[x] - [1 / 2] = 0"

            check {
                fromExpr = "sin[x] - [1 / 2] = 0"
                toExpr = "( x = [/pi/ / 6] + 2 k * /pi/ OR x = [5 /pi/ / 6] + 2 k * /pi/ )" +
                    "GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "sin[x] = [1 / 2]"
                }

                step {
                    toExpr = "arcsin[sin[x]] = arcsin[[1 / 2]]"
                }

                step {
                    toExpr = "x = arcsin[[1 / 2]]"
                }

                step {
                    toExpr = "(x = [/pi/ / 6] + 2 k * /pi/ OR x = [5 /pi/ / 6] + 2 k * /pi/) " +
                        "GIVEN SetSolution[k: /integers/]"

                    task {
                        taskId = "#1"
                        startExpr = "x = arcsin[[1 / 2]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x = /pi/ - arcsin[[1 / 2]]"
                    }

                    task {
                        taskId = "#3"
                        startExpr = "(x = [/pi/ / 6] + 2 k * /pi/ OR x = [5 /pi/ / 6] + 2 k * /pi/) " +
                            "GIVEN SetSolution[k: /integers/]"
                        explanation {
                            key = EquationsExplanation.CollectSolutions
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `test sin(f(x)) = c trig equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[x + 3] = [1 / 4] + [2 / 8]"

            check {
                fromExpr = "sin[x + 3] = [1 / 4] + [2 / 8]"
                toExpr = "(x = [/pi/ / 6] + 2 k * /pi/ - 3 OR x = [5 /pi/ / 6] + 2 k * /pi/ - 3) " +
                    "GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "sin[x + 3] = [1 / 2]"
                }

                step {
                    toExpr = "arcsin[sin[x + 3]] = arcsin[[1 / 2]]"
                }

                step {
                    toExpr = "x + 3 = arcsin[[1 / 2]]"
                }

                step {
                    toExpr = "(x = [/pi/ / 6] + 2 k * /pi/ - 3 OR x = [5 /pi/ / 6] + 2 k * /pi/ - 3)" +
                        " GIVEN SetSolution[k: /integers/]"

                    task {
                        taskId = "#1"
                        startExpr = "x + 3 = arcsin[[1 / 2]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x + 3 = /pi/ - arcsin[[1 / 2]]"
                    }

                    task {
                        taskId = "#3"
                        startExpr = "(x = [/pi/ / 6] + 2 k * /pi/ - 3 OR x = [5 /pi/ / 6] + 2 k * /pi/ - 3) " +
                            "GIVEN SetSolution[k: /integers/]"
                    }
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[x + 3] - [1 / 4] = [2 / 8]"

            check {
                toExpr = "(x = [/pi/ / 6] + 2 k * /pi/ - 3 OR x = [5 /pi/ / 6] + 2 k * /pi/ - 3) " +
                    "GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "sin[x + 3] - [1 / 4] = [1 / 4]"
                }

                step {
                    toExpr = "sin[x + 3] = [1 / 2]"
                }

                step {
                    toExpr = "arcsin[sin[x + 3]] = arcsin[[1 / 2]]"
                }

                step {
                    toExpr = "x + 3 = arcsin[[1 / 2]]"
                }

                step {
                    toExpr = "(x = [/pi/ / 6] + 2 k * /pi/ - 3 OR x = [5 /pi/ / 6] + 2 k * /pi/ - 3) " +
                        "GIVEN SetSolution[k: /integers/]"

                    task {
                        taskId = "#1"
                        startExpr = "x + 3 = arcsin[[1 / 2]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x + 3 = /pi/ - arcsin[[1 / 2]]"
                    }

                    task {
                        taskId = "#3"
                        startExpr = "(x = [/pi/ / 6] + 2 k * /pi/ - 3 OR x = [5 /pi/ / 6] + 2 k * /pi/ - 3) " +
                            "GIVEN SetSolution[k: /integers/]"
                    }
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[x] = [2 / 5]"

            check {
                toExpr = "(x = arcsin[[2 / 5]] + 2 k * /pi/ OR x = /pi/ - arcsin[[2 / 5]] + 2 k * /pi/) " +
                    "GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "arcsin[sin[x]] = arcsin[[2 / 5]]"
                }

                step {
                    toExpr = "x = arcsin[[2 / 5]]"
                }

                step {
                    toExpr = "(x = arcsin[[2 / 5]] + 2 k * /pi/ OR x = /pi/ - arcsin[[2 / 5]] + 2 k * /pi/) " +
                        "GIVEN SetSolution[k: /integers/]"

                    task {
                        taskId = "#1"
                        startExpr = "x = arcsin[[2 / 5]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x = /pi/ - arcsin[[2 / 5]]"
                    }

                    task {
                        taskId = "#3"
                        startExpr = "(x = arcsin[[2 / 5]] + 2 k * /pi/ OR x = /pi/ - arcsin[[2 / 5]] + 2 k * /pi/) " +
                            "GIVEN SetSolution[k: /integers/]"
                    }
                }
            }
        }
    }

    @Test
    fun `test sin(f(x)) = 0`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[2 x] = 0"

            check {
                step {
                    toExpr = "arcsin[sin[2 x]] = arcsin[0]"
                }

                step {
                    toExpr = "2 x = arcsin[0]"
                }

                step {
                    toExpr = "2 x = k * /pi/ GIVEN SetSolution[k: /integers/]"
                }

                step {
                    toExpr = "x = [k * /pi/ / 2] GIVEN SetSolution[k: /integers/]"
                }
            }
        }
    }

    @Test
    fun `test sin(f(x)) = ± 1`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[2 x] = 1"

            check {
                step {
                    toExpr = "arcsin[sin[2 x]] = arcsin[1]"
                }

                step {
                    toExpr = "2 x = arcsin[1]"
                }

                step {
                    toExpr = "2 x = [/pi/ / 2]"
                }

                step {
                    toExpr = "2 x = [/pi/ / 2] + 2 k * /pi/ GIVEN SetSolution[k: /integers/]"
                }

                step {
                    toExpr = "x = [[/pi/ / 2] + 2 k * /pi/ / 2] GIVEN SetSolution[k: /integers/]"
                }

                step {
                    toExpr = "x = [[/pi/ / 2] / 2] + [2 k * /pi/ / 2] GIVEN SetSolution[k: /integers/]"
                }

                step {
                    toExpr = "x = [/pi/ / 4] + k * /pi/ GIVEN SetSolution[k: /integers/]"
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[2 x] = -1"

            check {
                step {
                    toExpr = "arcsin[sin[2 x]] = arcsin[-1]"
                }

                step {
                    toExpr = "2 x = arcsin[-1]"
                }

                step {
                    toExpr = "2 x = -[/pi/ / 2]"
                }

                step {
                    toExpr = "2 x = -[/pi/ / 2] + 2 k * /pi/ GIVEN SetSolution[k: /integers/]"
                }

                step {
                    toExpr = "x = [-[/pi/ / 2] + 2 k * /pi/ / 2] GIVEN SetSolution[k: /integers/]"
                }

                step {
                    toExpr = "x = [-[/pi/ / 2] / 2] + [2 k * /pi/ / 2] GIVEN SetSolution[k: /integers/]"
                }

                step {
                    toExpr = "x = -[/pi/ / 4] + k * /pi/ GIVEN SetSolution[k: /integers/]"
                }
            }
        }
    }

    @Test
    fun `test cos(f(x)) = cos(g(x)) trig equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "cos[[/pi/ / 3] - x] = cos[2 x]"

            check {
                fromExpr = "cos[[/pi/ / 3] - x] = cos[2 x]"
                toExpr = "(x = [/pi/ / 9] + [2 k * /pi/ / 3] OR x = 2 k * /pi/ - [/pi/ / 3]) " +
                    "GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "arccos[cos[[/pi/ / 3] - x]] = arccos[cos[2 x]]"
                }

                step {
                    toExpr = "[/pi/ / 3] - x = arccos[cos[2 x]]"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "[/pi/ / 3] - x = arccos[cos[2 x]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[/pi/ / 3] - x = -arccos[cos[2 x]]"
                    }

                    task {}
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "cos[[/pi/ / 3] - x] = -cos[2 x]"

            check {
                fromExpr = "cos[[/pi/ / 3] - x] = -cos[2 x]"
                toExpr = "(x = [2 /pi/ / 3] + 2 k * /pi/ OR x = [4 /pi/ / 9] + [2 k * /pi/ / 3]) " +
                    "GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "cos[[/pi/ / 3] - x] = cos[/pi/ - 2 x]"
                }

                step {
                    toExpr = "arccos[cos[[/pi/ / 3] - x]] = arccos[cos[/pi/ - 2 x]]"
                }

                step {
                    toExpr = "[/pi/ / 3] - x = arccos[cos[/pi/ - 2 x]]"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "[/pi/ / 3] - x = arccos[cos[/pi/ - 2 x]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[/pi/ / 3] - x = -arccos[cos[/pi/ - 2 x]]"
                    }

                    task {}
                }
            }
        }
    }

    @Test
    fun `test cos(x) = cos(c) trig equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "cos[2 x] = cos[degree[ 35 ]]"

            check {
                fromExpr = "cos[2 x] = cos[degree[ 35 ]]"
                toExpr =
                    "(x = [7 /pi/ / 72] + k * /pi/ OR x = -[7 /pi/ / 72] + k * /pi/) GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "arccos[cos[2 x]] = arccos[cos[degree[ 35 ]]]"
                }

                step {
                    toExpr = "2 x = arccos[cos[degree[ 35 ]]]"
                }

                step {
                    toExpr = "(x = [7 /pi/ / 72] + k * /pi/ OR x = -[7 /pi/ / 72] + k * /pi/) " +
                        "GIVEN SetSolution[k: /integers/]"

                    task {
                        taskId = "#1"
                        startExpr = "2 x = arccos[cos[degree[ 35 ]]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "2 x = -arccos[cos[degree[ 35 ]]]"
                    }

                    task {}
                }
            }
        }
    }

    @Test
    fun `test cos(f(x)) = c trig equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "cos[x + [/pi/ / 8]] + [sqrt[2] / 2] = sqrt[2]"

            check {
                fromExpr = "cos[x + [/pi/ / 8]] + [sqrt[2] / 2] = sqrt[2]"
                toExpr = "(x = [/pi/ / 8] + 2 k * /pi/ OR x = -[3 /pi/ / 8] + 2 k * /pi/) " +
                    "GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "cos[x + [/pi/ / 8]] = [sqrt[2] / 2]"
                }

                step {
                    toExpr = "arccos[cos[x + [/pi/ / 8]]] = arccos[[sqrt[2] / 2]]"
                }

                step {
                    toExpr = "x + [/pi/ / 8] = arccos[[sqrt[2] / 2]]"
                }

                step {
                    toExpr = "(x = [/pi/ / 8] + 2 k * /pi/ OR x = -[3 /pi/ / 8] + 2 k * /pi/) " +
                        "GIVEN SetSolution[k: /integers/]"

                    task {
                        taskId = "#1"
                        startExpr = "x + [/pi/ / 8] = arccos[[sqrt[2] / 2]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x + [/pi/ / 8] = -arccos[[sqrt[2] / 2]]"
                    }

                    task {
                        taskId = "#3"
                        startExpr = "(x = [/pi/ / 8] + 2 k * /pi/ OR x = -[3 /pi/ / 8] + 2 k * /pi/) " +
                            "GIVEN SetSolution[k: /integers/]"
                    }
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "cos[3 x - [/pi/ / 6]] = -[sqrt[3] / 2]"

            check {
                toExpr = "(x = [/pi/ / 3] + [2 k * /pi/ / 3] OR x = -[2 /pi/ / 9] + [2 k * /pi/ / 3]) " +
                    "GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "arccos[cos[3 x - [/pi/ / 6]]] = arccos[-[sqrt[3] / 2]]"
                }

                step {
                    toExpr = "3 x - [/pi/ / 6] = arccos[-[sqrt[3] / 2]]"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "3 x - [/pi/ / 6] = arccos[-[sqrt[3] / 2]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "3 x - [/pi/ / 6] = -arccos[-[sqrt[3] / 2]]"
                    }

                    task {}
                }
            }
        }
    }

    @Test
    fun `test cos(f(x)) = 0 equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "cos[2 x] = 0"

            check {
                fromExpr = "cos[2 x] = 0"
                toExpr = "x = [/pi/ / 4] + [k * /pi/ / 2] GIVEN SetSolution[k: /integers/]"
                explanation {
                    key = EquationsExplanation.SolveTrigonometricEquation
                }

                step {
                    toExpr = "arccos[cos[2 x]] = arccos[0]"
                }

                step {
                    toExpr = "2 x = arccos[0]"
                }

                step {
                    toExpr = "2 x = [/pi/ / 2] + k * /pi/ GIVEN SetSolution[k: /integers/]"
                }

                step {
                    toExpr = "x = [[/pi/ / 2] + k * /pi/ / 2] GIVEN SetSolution[k: /integers/]"
                }

                step {
                    toExpr = "x = [[/pi/ / 2] / 2] + [k * /pi/ / 2] GIVEN SetSolution[k: /integers/]"
                }

                step {}
            }
        }
    }

    @Test
    fun `test cos(f(x)) = cos(g(x)) with only one valid solution`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "cos[x + [5 /pi/ / 6]] = -cos[x - [/pi/ / 3]]"

            check {
                toExpr = "x = [/pi/ / 4] + k * /pi/"

                step {
                    toExpr = "cos[x + [5 /pi/ / 6]] = cos[/pi/ - (x - [/pi/ / 3])]"
                }

                step {
                    toExpr = "arccos[cos[x + [5 /pi/ / 6]]] = arccos[cos[/pi/ - (x - [/pi/ / 3])]]"
                }

                step {
                    toExpr = "x + [5 /pi/ / 6] = arccos[cos[/pi/ - (x - [/pi/ / 3])]]"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "x + [5 /pi/ / 6] = arccos[cos[/pi/ - (x - [/pi/ / 3])]]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "x + [5 /pi/ / 6] = -arccos[cos[/pi/ - (x - [/pi/ / 3])]]"
                    }

                    task {}
                }
            }
        }
    }

    @Test
    fun `test impossible trig equation`() {
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "sin[2 x] = 2"

            check {
                fromExpr = "sin[2 x] = 2"
                toExpr = "Contradiction[x: sin[2 x] = 2]"
                explanation {
                    key = AnglesExplanation.ExtractSolutionFromImpossibleSineEquation
                }
            }
        }
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "cos[2 x] = 2"

            check {
                fromExpr = "cos[2 x] = 2"
                toExpr = "Contradiction[x: cos[2 x] = 2]"
                explanation {
                    key = AnglesExplanation.ExtractSolutionFromImpossibleSineEquation
                }
            }
        }
    }
}
