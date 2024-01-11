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
import { Grapher, UnsupportedGraphFeatureError } from './index';
import { treeToGgb, treeToLatex } from '../renderer';
import { jsonToTree } from '../parser';

/**
 * A partial definition of hte ggb applet API, containing the operations required to implement GgbAppletGrapher.
 */
export type GgbAppletApi = {
  getAllObjectNames: (type?: string) => string[];
  getVisible: (objName: string) => boolean;
  setVisible: (objName: string, visible: boolean) => void;
  getObjectType: (objName: string) => string;
  isMoveable: (objName: string) => boolean;
  getColor: (objName: string) => string;
  getXML: (objName: string) => string;
  getValue: (objName: string) => number;
  setValue: (objName: string, value: number) => void;
  getValueString: (objName: string) => string;
  evalCommand: (command: string) => boolean;
  evalCommandGetLabels: (command: string) => string;
  deleteObject: (objName: string) => void;
  newConstruction: () => void;
  // NAME = 0, NAME_VALUE = 1, VALUE = 2 and (from GeoGebra 3.2) CAPTION = 3
  setLabelStyle: (objName: string, style: number) => void;
  setLabelVisible: (objName: string, visible: boolean) => void;
  setAxisLabels: (
    viewNumber: number,
    xAxis: string,
    yAxis: string,
    zAxis: string,
  ) => void;
  setCoordSystem: (xmin: number, xmax: number, ymin: number, ymax: number) => void;
  setColor: (objName: string, red: number, green: number, blue: number) => void;
  setCaption: (objName: string, caption: string) => void;
  showAllObjects: () => void;
  setBase64: (construction: string, callback?: () => void) => void;
  getBase64: () => string;
  registerUpdateListener: (onUpdate: (name: string) => void) => void;
  registerClientListener: (onUpdate: (content: any) => void) => void;
};

/**
 * Implements the Grapher interface to draw graphs on a ggb applet instance.  The applet must already be
 * configured.
 */
export class GgbAppletGrapher implements Grapher {
  private readonly ggbAppletApi: GgbAppletApi;
  private readonly varSub: Record<string, string>;

  constructor(ggbAppletApi: GgbAppletApi) {
    this.ggbAppletApi = ggbAppletApi;
    this.varSub = {};
  }

  setCoordinateSystem(coordinateSystem: CoordinateSystem) {
    switch (coordinateSystem.type) {
      case 'Cartesian2D': {
        this.ggbAppletApi.setCoordSystem(
          coordinateSystem.horizontalAxis.minValue,
          coordinateSystem.horizontalAxis.maxValue,
          coordinateSystem.verticalAxis.minValue,
          coordinateSystem.verticalAxis.maxValue,
        );
        this.ggbAppletApi.setAxisLabels(
          1,
          coordinateSystem.horizontalAxis.label,
          coordinateSystem.verticalAxis.label,
          '',
        );
        this.varSub[coordinateSystem.horizontalAxis.variable] = 'x';
        this.varSub[coordinateSystem.verticalAxis.variable] = 'y';
        break;
      }
      default:
        throw new UnsupportedGraphFeatureError(
          'Unsupported coordinate system type',
          coordinateSystem.type,
        );
    }
  }

  drawObject(
    graphObject: GraphObject<MathJson>,
    rgbColor: [number, number, number] = [200, 20, 20],
  ) {
    switch (graphObject.type) {
      case 'curve2D': {
        const labelFromObject = graphObject.label;
        const exprTree = jsonToTree(graphObject.expression);
        const exprGgb = treeToGgb(exprTree, this.varSub);

        // If there is a label, we use it, otherwise we let the applet create one - but there may be more than one
        // object created, which would mean more than one label.
        const labels = labelFromObject
          ? [labelFromObject]
          : this.ggbAppletApi.evalCommandGetLabels(exprGgb).split(',');

        if (labelFromObject) {
          this.ggbAppletApi.evalCommand(`${labelFromObject}:${exprGgb}`);
        }

        for (const label of labels) {
          this.ggbAppletApi.setColor(label, rgbColor[0], rgbColor[1], rgbColor[2]);
          this.ggbAppletApi.setCaption(label, `$${treeToLatex(exprTree)}$`);
          this.ggbAppletApi.setLabelStyle(label, 3);
          this.ggbAppletApi.setLabelVisible(label, true);
        }

        break;
      }
      default:
        throw new UnsupportedGraphFeatureError(
          'Unsupported graph object type',
          graphObject.type,
        );
    }
  }
}
