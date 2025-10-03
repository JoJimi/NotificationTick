<!-- src/components/NavButton.vue -->
<template>
  <el-button
      :type="type"
      :size="size"
      :plain="plain"
      :text="text"
      :round="round"
      :disabled="disabled"
      @click="go"
  >
    <slot />
  </el-button>
</template>

<script setup>
import { useRouter } from 'vue-router';

const props = defineProps({
  to: { type: [String, Object], required: true },
  type: { type: String, default: 'default' }, // primary / success / info / warning / danger / default
  size: { type: String, default: 'default' }, // small / default / large
  plain: { type: Boolean, default: false },
  text: { type: Boolean, default: false },
  round: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  replace: { type: Boolean, default: false },
});
const router = useRouter();
function go() {
  props.replace ? router.replace(props.to) : router.push(props.to);
}
</script>
