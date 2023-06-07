<script setup lang="ts">
import { ref, watchEffect, onMounted } from 'vue';
import uniqueId from 'lodash/uniqueId';

declare const Mergely;

const props = defineProps<{ leftText: string; rightText: string }>();

const rootDomElement = ref(null as HTMLElement | null);
const mergely = ref(null as typeof Mergely | null);
onMounted(() => {
  const domElement = rootDomElement.value!;
  // Need to give it a unique id or else there will be errors when there are more than
  // one.
  const id = `mergely${uniqueId()}`;
  domElement.id = id;
  mergely.value = new Mergely(`#${id}`, {
    ignorews: true,
    license: 'mpl-separate-notice',
    line_numbers: false,
    // We use the parent element because `domElement` has the 'light-mode' class, so it's white.
    bgcolor: getComputedStyle(domElement.parentElement!).getPropertyValue('--background-color'),
  });
});
watchEffect(() => {
  if (!mergely.value) return;
  mergely.value.lhs(props.leftText);
  mergely.value.rhs(props.rightText);
});
</script>

<template>
  <div ref="rootDomElement" class="light-mode" style="height: 75vh; width: 100%"></div>
</template>
