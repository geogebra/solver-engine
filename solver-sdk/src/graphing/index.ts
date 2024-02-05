/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
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

import { CoordinateSystem, GraphObject, MathJson } from '../types';

export { GgbAppletGrapher } from './ggb';

/**
 * An interface for drawing graphs as returned by the solver graph API.  Unsupported features should throw an
 * UnsupportedGraphFeatureError error.
 */
export interface Grapher {
  /**
   * Set the coordinate system of the grapher.  This should be done first.
   * @param coordinateSystem the coordinate system as returned by the solver graph API
   */
  setCoordinateSystem(coordinateSystem: CoordinateSystem): void;

  /**
   * Draw an object (e.g. curve) on the graph.  This should be done after the coordinate system has been set.
   * @param graphObject the object as provided by the solver graph API
   * @param rgbColor what color to draw the object in, in [R, G, B] format
   */
  drawObject(
    graphObject: GraphObject<MathJson>,
    rgbColor: [number, number, number],
  ): void;
}

/**
 * Error thrown by a Grapher implementation when it doesn't know how to draw a certain type of object on the graph.
 */
export class UnsupportedGraphFeatureError extends Error {
  constructor(message = '', ...args: any) {
    super(message, ...args);
  }
}

/**
 * Error thrown by a Grapher implementation when an operation is done at the wrong time (e.g. setCoordinateSystem
 * called twice, or drawObject called before setCoordinateSystem
 */

export class InvalidGrapherOperation extends Error {
  constructor(message = '', ...args: any) {
    super(message, ...args);
  }
}
