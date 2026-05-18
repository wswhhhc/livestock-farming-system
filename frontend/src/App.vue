<template>
  <router-view v-slot="{ Component }">
    <transition name="app" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<style>
.app-enter-active {
  animation: appIn 0.4s ease-out;
}
.app-leave-active {
  animation: appOut 0.3s ease-in;
}

@keyframes appIn {
  0% { opacity: 0; transform: scale(0.98); }
  100% { opacity: 1; transform: scale(1); }
}

@keyframes appOut {
  0% { opacity: 1; transform: scale(1); }
  100% { opacity: 0; transform: scale(0.98); }
}
</style>
