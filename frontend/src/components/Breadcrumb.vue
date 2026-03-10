<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const breadcrumbs = ref<{ title: string }[]>([])

const updateBreadcrumbs = () => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  breadcrumbs.value = matched.map(item => ({
    title: item.meta.title as string
  }))
}

watch(() => route.path, updateBreadcrumbs, { immediate: true })
</script>

<style scoped>
.el-breadcrumb {
  line-height: 60px;
}
</style>
