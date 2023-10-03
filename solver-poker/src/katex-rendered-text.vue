<script setup lang="ts">
import { onMounted, onUpdated, ref } from 'vue';

defineProps<{ text: string }>();

const rootElement = ref<HTMLSpanElement | null>(null);

const runKatex = () => {
  // Do this after each time after the DOM is updated, because this needs to work with an
  // up-to-date DOM, and change it.
  window.renderMathInElement?.(rootElement.value!);
};
onUpdated(runKatex);
onMounted(runKatex);
</script>

<template>
  <div ref="rootElement">{{ text }}</div>
</template>
