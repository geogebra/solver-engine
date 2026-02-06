/*
 * Copyright (c) 2026 GeoGebra GmbH, office@geogebra.org
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

import engine.methods.testMethodInX
import methods.equations.EquationsPlans
import org.junit.jupiter.api.Test

class QuadraticTrigonometricEquationsTest {
    @Test
    fun `test complete equation with two solutions`() { // PLUT-1084
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 [cos ^ 2][x] + 3 cos[x] + 1 = 0"

            check {
                toExpr = "SetSolution[x: {-[2 /pi/ / 3] + 2 k * /pi/, /pi/ + 2 k * /pi/, [2 /pi/ / 3] + 2 k * /pi/}]" +
                    " GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "2 [t ^ 2] + 3 t + 1 = 0 AND t = cos[x]"
                }

                step {
                    toExpr = "SetSolution[t: {-1, -[1 / 2]}] AND t = cos[x]"
                }

                step {
                    task {
                        startExpr = "cos[x] = -1"
                    }

                    task {
                        startExpr = "cos[x] = -[1 / 2]"
                    }

                    task {}
                }
            }
        }

        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 [cos ^ 2][x] + 3 cos[2 /pi/ + x] + 1 = 0"

            check {
                toExpr = "SetSolution[x: {-[2 /pi/ / 3] + 2 k * /pi/, /pi/ + 2 k * /pi/, [2 /pi/ / 3] + 2 k * /pi/}]" +
                    " GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "2 [cos ^ 2][x] + 3 cos[x] + 1 = 0"
                }

                step {
                    toExpr = "2 [t ^ 2] + 3 t + 1 = 0 AND t = cos[x]"
                }

                step {
                    toExpr = "SetSolution[t: {-1, -[1 / 2]}] AND t = cos[x]"
                }

                step {
                    task {
                        startExpr = "cos[x] = -1"
                    }

                    task {
                        startExpr = "cos[x] = -[1 / 2]"
                    }

                    task {}
                }
            }
        }
    }

    @Test
    fun `test complete equation with no solution`() { // PLUT-1084
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "13 [sin ^ 2][x] + 48 sin[x] + 45 = 0"

            check {
                toExpr = "Contradiction[t: t = [-48 +/- sqrt[-36] / 26]]"

                step {
                    toExpr = "13 [t ^ 2] + 48 t + 45 = 0 AND t = sin[x]"
                }

                step {
                    toExpr = "Contradiction[t: t = [-48 +/- sqrt[-36] / 26]] AND t = sin[x]"
                }

                step {}
            }
        }
    }

    @Test
    fun `test complete equation with one solution`() { // PLUT-1084
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[tan ^ 2][x] - 2 sqrt[3] * tan[x] + 3 = 0"

            check {
                toExpr = "SetSolution[x: {[/pi/ / 3] + k * /pi/}] GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "[t ^ 2] - 2 sqrt[3] t + 3 = 0 AND t = tan[x]"
                }

                step {
                    toExpr = "SetSolution[t: {sqrt[3]}] AND t = tan[x]"
                }

                step {
                    toExpr = "SetSolution[x: {[/pi/ / 3] + k * /pi/}] GIVEN SetSolution[k: /integers/]"

                    task {
                        startExpr = "tan[x] = sqrt[3]"
                    }
                }
            }
        }
    }

    @Test
    fun `test pure equation with no solution`() { // PLUT-1090
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 [cos ^ 2][x] + 1 = 0"

            check {
                toExpr = "Contradiction[x: [cos ^ 2][x] = -[1 / 2]]"

                step {
                    toExpr = "2 [cos ^ 2][x] = -1"
                }

                step {
                    toExpr = "[cos ^ 2][x] = -[1 / 2]"
                }

                step {
                    toExpr = "Contradiction[x: [cos ^ 2][x] = -[1 / 2]]"
                }
            }
        }
    }

    @Test
    fun `test pure equation with real solutions`() { // PLUT-1090
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 [sin ^ 2][x] - 1 = 0"

            check {
                toExpr = "SetSolution[x: {[/pi/ / 4] + [k * /pi/ / 2]}] GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "2 [sin ^ 2][x] = 1"
                }

                step {
                    toExpr = "[sin ^ 2][x] = [1 / 2]"
                }

                step {
                    toExpr = "sin[x] = +/-[sqrt[2] / 2]"
                }

                step {
                    toExpr = "sin[x] = -[sqrt[2] / 2] OR sin[x] = [sqrt[2] / 2]"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "sin[x] = -[sqrt[2] / 2]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "sin[x] = [sqrt[2] / 2]"
                    }

                    task {
                        taskId = "#3"
                        startExpr = "SetSolution[x: {-[/pi/ / 4] + 2 k * /pi/, [/pi/ / 4] + 2 k * /pi/," +
                            " [3 /pi/ / 4] + 2 k * /pi/, [5 /pi/ / 4] + 2 k * /pi/}] GIVEN SetSolution[k: /integers/]"
                    }
                }

                step {}
            }
        }
    }

    @Test
    fun `test pure equation using roots method`() { // PLUT-1090
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "3 [tan ^ 2][x] = 9"

            check {
                toExpr = "SetSolution[x: {-[/pi/ / 3] + k * /pi/, [/pi/ / 3] + k * /pi/}] " +
                    "GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "[tan ^ 2][x] = 3"
                }

                step {
                    toExpr = "tan[x] = +/-sqrt[3]"
                }

                step {
                    toExpr = "tan[x] = -sqrt[3] OR tan[x] = sqrt[3]"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "tan[x] = -sqrt[3]"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "tan[x] = sqrt[3]"
                    }

                    task {}
                }
            }
        }
    }

    @Test
    fun `test incomplete equation with one solution`() { // PLUT-1085
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 [sin ^ 2][x] = -4 sin[x]"

            check {
                fromExpr = "2 [sin ^ 2][x] = -4 sin[x]"
                toExpr = "SetSolution[x: {k * /pi/}] GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "2 [sin ^ 2][x] + 4 sin[x] = 0"
                }

                step {
                    toExpr = "2 sin[x] (sin[x] + 2) = 0"
                }

                step {
                    toExpr = "sin[x] (sin[x] + 2) = 0"
                }

                step {
                    toExpr = "sin[x] = 0 OR sin[x] + 2 = 0"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "sin[x] = 0"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "sin[x] + 2 = 0"
                    }

                    task {}
                }
            }
        }
    }

    @Test
    fun `test incomplete equation with two solutions`() { // PLUT-1085
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "2 [cos ^ 2][x] - cos[x] = 0"

            check {
                toExpr = "SetSolution[x: {-[/pi/ / 3] + 2 k * /pi/, [/pi/ / 2] + k * /pi/, [/pi/ / 3] + 2 k * /pi/}]" +
                    " GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "cos[x] (2 cos[x] - 1) = 0"
                }

                step {
                    toExpr = "cos[x] = 0 OR 2 cos[x] - 1 = 0"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "cos[x] = 0"

                        step {
                            toExpr = "SetSolution[x: {[/pi/ / 2] + k * /pi/}] GIVEN SetSolution[k: /integers/]"
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "2 cos[x] - 1 = 0"

                        step {
                            toExpr = "SetSolution[x: {-[/pi/ / 3] + 2 k * /pi/, [/pi/ / 3] + 2 k * /pi/}]" +
                                " GIVEN SetSolution[k: /integers/]"
                        }
                    }

                    task {}
                }
            }
        }
    }

    @Test
    fun `test solution by factoring`() { // PLUT-1084
        testMethodInX {
            method = EquationsPlans.SolveEquation
            inputExpr = "[sin ^ 2][x] = [1 / 2] sin[degree[ 180 ] - x]"

            check {
                toExpr = "SetSolution[x: {[/pi/ / 6] + 2 k * /pi/, [5 /pi/ / 6] + 2 k * /pi/, k * /pi/}] " +
                    "GIVEN SetSolution[k: /integers/]"

                step {
                    toExpr = "[sin ^ 2][x] - [1 / 2] sin[degree[ 180 ] - x] = 0"
                }

                step {
                    toExpr = "[sin ^ 2][x] - [1 / 2] sin[x] = 0"
                }

                step {
                    toExpr = "sin[x] (sin[x] - [1 / 2]) = 0"
                }

                step {
                    toExpr = "sin[x] = 0 OR sin[x] - [1 / 2] = 0"
                }

                step {
                    task {
                        taskId = "#1"
                        startExpr = "sin[x] = 0"
                    }

                    task {
                        taskId = "#2"
                        startExpr = "sin[x] - [1 / 2] = 0"
                    }

                    task {}
                }
            }
        }
    }
}
