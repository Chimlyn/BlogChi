<template>
  <div class="pagination" v-if="pages > 1">
    <button class="btn btn-sm" :disabled="page <= 1" @click="go(page - 1)">上一页</button>
    <span>{{ page }} / {{ pages }}</span>
    <button class="btn btn-sm" :disabled="page >= pages" @click="go(page + 1)">下一页</button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: { type: Number, required: true },
  size: { type: Number, required: true },
  total: { type: Number, required: true }
})

const emit = defineEmits(['change'])

const pages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))

function go(target) {
  if (target < 1 || target > pages.value) return
  emit('change', target)
}
</script>
