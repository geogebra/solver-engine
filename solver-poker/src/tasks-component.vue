<script setup lang="ts">
import type { TaskJson } from '@geogebra/solver-sdk';
import TaskComponent from './task-component.vue';

const props = defineProps<{
  tasks: TaskJson[] | null;
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
