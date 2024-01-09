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

<script setup lang="ts">
import type { TaskJson } from '@geogebra/solver-sdk';
import TaskComponent from './task-component.vue';

const props = defineProps<{
  tasks?: TaskJson[];
  depth: number;
  open: boolean;
}>();
</script>

<template>
  <details v-if="tasks && tasks.length" class="tasks" :open="open">
    <summary>{{ tasks.length }} {{ tasks.length === 1 ? 'task' : 'tasks' }}</summary>
    <ol>
      <li v-for="task in tasks">
        <TaskComponent :task="task" :depth="depth - 1" />
      </li>
    </ol>
  </details>
</template>
