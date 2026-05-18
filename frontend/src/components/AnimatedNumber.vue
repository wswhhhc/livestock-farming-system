<template>
  <span>{{ displayText }}</span>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  value: { type: Number, default: null },
  duration: { type: Number, default: 800 },
  decimal: { type: Number, default: 0 },
  prefix: { type: String, default: '' },
  suffix: { type: String, default: '' },
  fallback: { type: String, default: '-' }
})

const displayText = ref(props.fallback)
let frameId = null

function animateValue() {
  cancelAnimationFrame(frameId)

  if (props.value == null) {
    displayText.value = props.fallback
    return
  }

  const from = 0
  const to = Number(props.value)
  const start = performance.now()

  function tick(now) {
    const elapsed = now - start
    const progress = Math.min(elapsed / props.duration, 1)
    const eased = progress === 1 ? 1 : 1 - Math.pow(1 - progress, 3)
    const current = from + (to - from) * eased

    if (props.decimal > 0) {
      displayText.value = props.prefix + current.toLocaleString('zh-CN', { minimumFractionDigits: props.decimal, maximumFractionDigits: props.decimal }) + props.suffix
    } else {
      displayText.value = props.prefix + Math.round(current).toLocaleString('zh-CN') + props.suffix
    }

    if (progress < 1) {
      frameId = requestAnimationFrame(tick)
    }
  }

  frameId = requestAnimationFrame(tick)
}

watch(() => props.value, animateValue, { immediate: true })

onBeforeUnmount(() => {
  cancelAnimationFrame(frameId)
})
</script>
