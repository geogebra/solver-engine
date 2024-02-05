<!--
  - Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
  - This file is part of GeoGebra
  -
  - The GeoGebra source code is licensed to you under the terms of the
  - GNU General Public License (version 3 or later)
  - as published by the Free Software Foundation,
  - the current text of which can be found via this link:
  - https://www.gnu.org/licenses/gpl.html ("GPL")
  - Attribution (as required by the GPL) should take the form of (at least)
  - a mention of our name, an appropriate copyright notice
  - and a link to our website located at https://www.geogebra.org
  -
  - For further details, please see https://www.geogebra.org/license
  -
  -->

<template>
  <div :class="id">
    <div :id="id"></div>
  </div>
</template>

<script setup lang="ts">
import { nanoid } from 'nanoid';
import { onMounted } from 'vue';

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
  setAxisLabels: (viewNumber: number, xAxis: string, yAxis: string, zAxis: string) => void;
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

const id = nanoid();
let ggbApplet: GgbAppletApi | undefined;

const props = defineProps<{
  materialId?: string;
  construction?: string;
  settings?: Record<string, any>;
}>();
const emit = defineEmits<{
  (event: 'onLoad', api: GgbAppletApi): void;
  (event: 'onChange', name: string, api: GgbAppletApi): void;
  (event: 'onClientEvent', content: any, api: GgbAppletApi): void;
}>();

// https://wiki.geogebra.org/en/Reference:GeoGebra_App_Parameters
const parameters = {
  id: id,
  scaleContainerClass: id,
  showResetIcon: false,
  enableLabelDrags: false,
  enableShiftDragZoom: false,
  enableRightClick: false,
  errorDialogsActive: false,
  appletOnLoad: function (api: GgbAppletApi) {
    ggbApplet = api;
    // @ts-ignore
    window.ggbApplet = api;
    ggbApplet.registerUpdateListener(onGgbUpdate);
    ggbApplet.registerClientListener(onGgbClientEvent);
    emit('onLoad', api);
  },
  allowUpscale: true,
  showFullscreenButton: false,
  ...props.settings,
};

if (props.materialId) {
  if (props.construction) {
    // @ts-ignore
    parameters.ggbBase64 = props.construction;
  } else {
    // @ts-ignore
    parameters.material_id = props.materialId;
  }
}

const applet = new GGBApplet(parameters, true);
applet.setHTML5Codebase('https://www.geogebra.org/apps/5.2.819.0/web3d/');

function onGgbUpdate(name: string) {
  emit('onChange', name, ggbApplet!);
}

function onGgbClientEvent(content: GgbAppletApi) {
  emit('onClientEvent', content, ggbApplet!);
}

onMounted(() => {
  applet.inject(id);
});

defineExpose({
  ggbApplet,
});
</script>

<script lang="ts">
declare const GGBApplet: any;
</script>
