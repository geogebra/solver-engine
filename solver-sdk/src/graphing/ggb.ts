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
import { Grapher, InvalidGrapherOperation, UnsupportedGraphFeatureError } from './index';
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
  setPointStyle: (objName: string, value: number) => void;
  setLineStyle: (objName: string, value: number) => void;
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
  private coordinateSystem: CoordinateSystem | undefined;

  constructor(ggbAppletApi: GgbAppletApi) {
    this.ggbAppletApi = ggbAppletApi;
    this.varSub = {};
  }

  setCoordinateSystem(coordinateSystem: CoordinateSystem) {
    if (this.coordinateSystem) {
      throw new InvalidGrapherOperation('coordinate system already set');
    }
    this.coordinateSystem = coordinateSystem;
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
    if (this.coordinateSystem === undefined) {
      throw new InvalidGrapherOperation('coordinate system not set');
    }
    switch (graphObject.type) {
      case 'curve2D': {
        const labelFromObject = graphObject.label;
        const exprTree = jsonToTree(graphObject.expression);
        const exprGgb = treeToGgb(exprTree, this.varSub);

        // If there is a label, we use it, otherwise we let the applet create one - but there may be more than one
        // object created, which would mean more than one label.
        const labels = labelFromObject
          ? [labelFromObject]
          : this.evalCommandGetLabels(exprGgb);

        if (labelFromObject) {
          this.ggbAppletApi.evalCommand(`${labelFromObject}:${exprGgb}`);
        }

        for (const label of labels) {
          this.ggbAppletApi.setColor(label, rgbColor[0], rgbColor[1], rgbColor[2]);
          this.setLatexCaption(label, treeToLatex(exprTree));
        }

        break;
      }
      case 'intersection': {
        // Depending on the type of expression one method or the other might produce better results,
        // so we try both and take the better of the two
        const points = this.getPointsFromList(
          `{Intersect(${graphObject.objectLabels.join(
            ', ',
          )}, x(Corner(1)), x(Corner(2)))}`,
        );
        const points2 = this.getPointsFromList(
          `{Intersect(${graphObject.objectLabels.join(', ')})}`,
        );

        let labels;

        if (points2.length > points.length) {
          labels = points2.flatMap((point) => this.evalCommandGetLabels(point));
        } else {
          labels = points.flatMap((point) => this.evalCommandGetLabels(point));
        }

        for (const [index, label] of labels.entries()) {
          this.ggbAppletApi.setPointStyle(label, 1);
          this.ggbAppletApi.setColor(label, 68, 68, 68);
          if (graphObject.projectOntoHorizontalAxis) {
            const projLabel = this.ggbAppletApi.evalCommandGetLabels(`(x(${label}), 0)`);
            const segmentLabel = this.ggbAppletApi.evalCommandGetLabels(
              `Segment(${label}, ${projLabel})`,
            );
            // See https://wiki.geogebra.org/en/SetLineStyle_Command?lang=en
            this.ggbAppletApi.setLineStyle(segmentLabel, 3);
            this.ggbAppletApi.setCaption(
              projLabel,
              `${this.coordinateSystem.horizontalAxis.variable} = %x`,
            );
            this.ggbAppletApi.setLabelStyle(projLabel, 3);
            this.ggbAppletApi.setLabelVisible(projLabel, true);
          }
          if (graphObject.projectOntoVerticalAxis) {
            const projLabel = this.ggbAppletApi.evalCommandGetLabels(`(0, y(${label}))`);
            const segmentLabel = this.ggbAppletApi.evalCommandGetLabels(
              `Segment(${label}, ${projLabel})`,
            );
            // See https://wiki.geogebra.org/en/SetLineStyle_Command?lang=en
            this.ggbAppletApi.setLineStyle(segmentLabel, 3);
            this.ggbAppletApi.setCaption(
              projLabel,
              `${this.coordinateSystem.verticalAxis.variable} = %y`,
            );
            this.ggbAppletApi.setLabelStyle(projLabel, 3);
            this.ggbAppletApi.setLabelVisible(projLabel, true);
          }
          if (graphObject.showLabelWithCoordinates) {
            this.ggbAppletApi.setLabelStyle(label, 2);
            this.ggbAppletApi.setLabelVisible(label, true);
          }
        }
        break;
      }
      default:
        throw new UnsupportedGraphFeatureError(
          'Unsupported graph object type',
          // @ts-ignore
          graphObject.type,
        );
    }
  }

  // The command passed as an argument must return a list
  // Returns an array of strings of format '(x, y)'
  // Invalid points (?, ?) are filtered out
  private getPointsFromList(command: string) {
    const listLabel = this.evalCommandGetLabels(command)[0];

    const values = this.ggbAppletApi.getValueString(listLabel);
    const points = values.split('{')[1].slice(0, -1).replaceAll('),', ');').split(';');

    this.ggbAppletApi.deleteObject(listLabel);

    return points.filter((s) => !s.includes('?')).map((s) => s.trim());
  }

  private evalCommandGetLabels(command: string) {
    return this.ggbAppletApi.evalCommandGetLabels(command).split(',');
  }

  private setLatexCaption(label: string, caption: string) {
    this.ggbAppletApi.setCaption(label, `$${caption}$`);
    this.ggbAppletApi.setLabelStyle(label, 3);
    this.ggbAppletApi.setLabelVisible(label, true);
  }
}
