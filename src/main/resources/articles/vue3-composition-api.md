# Vue 3 Composition API 最佳实践

Vue 3 的 Composition API 为我们提供了更灵活、更可组合的代码组织方式。本文将分享一些实用的最佳实践。

## 为什么选择 Composition API？

### 更好的逻辑复用

**Options API 的问题：**

```javascript
// 难以复用的 Options API
export default {
  data() {
    return {
      mouseX: 0,
      mouseY: 0
    }
  },
  mounted() {
    window.addEventListener('mousemove', this.handleMouseMove)
  },
  beforeUnmount() {
    window.removeEventListener('mousemove', this.handleMouseMove)
  },
  methods: {
    handleMouseMove(e) {
      this.mouseX = e.pageX
      this.mouseY = e.pageY
    }
  }
}
```

**Composition API 的优势：**

```javascript
// 可复用的 Composable
import { ref, onMounted, onUnmounted } from 'vue'

export function useMouse() {
  const x = ref(0)
  const y = ref(0)

  function update(event) {
    x.value = event.pageX
    y.value = event.pageY
  }

  onMounted(() => window.addEventListener('mousemove', update))
  onUnmounted(() => window.removeEventListener('mousemove', update))

  return { x, y }
}
```

使用：

```vue
<script setup>
import { useMouse } from '@/composables/useMouse'

const { x, y } = useMouse()
</script>

<template>
  <div>鼠标位置：{{ x }}, {{ y }}</div>
</template>
```

## 核心概念

### ref 与 reactive

**ref - 适合基本类型：**

```javascript
import { ref } from 'vue'

const count = ref(0)
const message = ref('Hello')

// 访问需要 .value
console.log(count.value) // 0
count.value++

// 模板中自动解包
// <div>{{ count }}</div>
```

**reactive - 适合对象：**

```javascript
import { reactive } from 'vue'

const state = reactive({
  count: 0,
  message: 'Hello',
  user: {
    name: 'John',
    age: 30
  }
})

// 直接访问
console.log(state.count) // 0
state.count++
```

**选择建议：**

- 基本类型：使用 `ref`
- 对象：使用 `reactive`
- 混合场景：统一使用 `ref` + 对象

### computed 计算属性

```javascript
import { ref, computed } from 'vue'

const firstName = ref('John')
const lastName = ref('Doe')

// 只读计算属性
const fullName = computed(() => {
  return `${firstName.value} ${lastName.value}`
})

// 可写计算属性
const fullNameWritable = computed({
  get() {
    return `${firstName.value} ${lastName.value}`
  },
  set(value) {
    [firstName.value, lastName.value] = value.split(' ')
  }
})
```

### watch 与 watchEffect

**watch - 精确控制：**

```javascript
import { ref, watch } from 'vue'

const question = ref('')
const answer = ref('')

// 监听单个源
watch(question, async (newQuestion, oldQuestion) => {
  if (newQuestion.includes('?')) {
    answer.value = 'Thinking...'
    answer.value = await fetchAnswer(newQuestion)
  }
})

// 监听多个源
watch([firstName, lastName], ([newFirst, newLast], [oldFirst, oldLast]) => {
  // ...
})

// 深度监听
watch(
  () => state.user,
  (newUser) => console.log(newUser),
  { deep: true }
)
```

**watchEffect - 自动追踪：**

```javascript
import { ref, watchEffect } from 'vue'

const count = ref(0)
const double = ref(0)

// 自动追踪依赖
watchEffect(() => {
  double.value = count.value * 2
  console.log(`count: ${count.value}, double: ${double.value}`)
})
```

## 常用 Composables

### useAsyncData

```javascript
import { ref } from 'vue'

export function useAsyncData(fetchFn) {
  const data = ref(null)
  const error = ref(null)
  const loading = ref(false)

  async function execute(...args) {
    loading.value = true
    error.value = null
    try {
      data.value = await fetchFn(...args)
    } catch (e) {
      error.value = e
    } finally {
      loading.value = false
    }
  }

  return {
    data,
    error,
    loading,
    execute
  }
}
```

使用：

```vue
<script setup>
import { useAsyncData } from '@/composables/useAsyncData'
import { fetchUser } from '@/api'

const { data: user, loading, error, execute } = useAsyncData(fetchUser)

// 组件挂载时加载
onMounted(() => execute(123))
</script>
```

### useLocalStorage

```javascript
import { ref, watch } from 'vue'

export function useLocalStorage(key, defaultValue) {
  const data = ref(defaultValue)

  // 初始化
  const stored = localStorage.getItem(key)
  if (stored) {
    data.value = JSON.parse(stored)
  }

  // 同步到 localStorage
  watch(data, (newValue) => {
    localStorage.setItem(key, JSON.stringify(newValue))
  }, { deep: true })

  return data
}
```

### useDebounce

```javascript
import { ref, watch } from 'vue'

export function useDebounce(value, delay = 300) {
  const debouncedValue = ref(value.value)
  let timeout

  watch(value, (newValue) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      debouncedValue.value = newValue
    }, delay)
  })

  return debouncedValue
}
```

## 最佳实践

### 1. 组织代码结构

```javascript
<script setup>
// 1. 导入
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 2. Composables
const router = useRouter()
const userStore = useUserStore()

// 3. 响应式状态
const count = ref(0)
const message = ref('')

// 4. 计算属性
const doubleCount = computed(() => count.value * 2)

// 5. 方法
function increment() {
  count.value++
}

// 6. 生命周期
onMounted(() => {
  console.log('Component mounted')
})
</script>
```

### 2. 类型安全（TypeScript）

```typescript
import { ref, Ref } from 'vue'

interface User {
  id: number
  name: string
  email: string
}

// 明确类型
const user = ref<User>({
  id: 1,
  name: 'John',
  email: 'john@example.com'
})

// Composable 类型
export function useUser(): {
  user: Ref<User | null>
  loading: Ref<boolean>
  fetchUser: (id: number) => Promise<void>
} {
  const user = ref<User | null>(null)
  const loading = ref(false)

  async function fetchUser(id: number) {
    loading.value = true
    try {
      user.value = await api.getUser(id)
    } finally {
      loading.value = false
    }
  }

  return { user, loading, fetchUser }
}
```

### 3. 组件拆分

```vue
<!-- ParentComponent.vue -->
<script setup>
import { ref } from 'vue'
import ChildComponent from './ChildComponent.vue'

const items = ref([1, 2, 3])

function addItem() {
  items.value.push(items.value.length + 1)
}
</script>

<template>
  <div>
    <button @click="addItem">Add Item</button>
    <ChildComponent
      v-for="item in items"
      :key="item"
      :item="item"
    />
  </div>
</template>

<!-- ChildComponent.vue -->
<script setup>
defineProps<{
  item: number
}>()

const emit = defineEmits<{
  remove: [id: number]
}>()
</script>
```

### 4. 提供/注入

```javascript
// 父组件
import { provide, ref } from 'vue'

const theme = ref('light')
provide('theme', theme)

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
}
provide('toggleTheme', toggleTheme)

// 子组件
import { inject } from 'vue'

const theme = inject('theme')
const toggleTheme = inject('toggleTheme')
```

## 性能优化

### 1. 使用 shallowRef

```javascript
import { shallowRef } from 'vue'

// 大型对象，只需要整体替换
const bigData = shallowRef({
  // 大量数据
})

// 触发更新
bigData.value = newData
```

### 2. 懒加载

```javascript
import { ref, onMounted } from 'vue'

const HeavyComponent = ref(null)

onMounted(async () => {
  HeavyComponent.value = (await import('./HeavyComponent.vue')).default
})
```

### 3. computed 缓存

```javascript
// 昂贵的计算
const expensiveResult = computed(() => {
  // 只在依赖变化时重新计算
  return heavyCalculation(data.value)
})
```

## 总结

Composition API 的优势：

- ✅ 更好的逻辑复用
- ✅ 更清晰的代码组织
- ✅ 更好的类型推导
- ✅ 更小的打包体积（Tree-shaking）

记住这些关键点：

1. 优先使用 `<script setup>`
2. 合理使用 ref 和 reactive
3. 提取可复用的 Composables
4. 注意 TypeScript 类型
5. 适时进行性能优化

Happy coding with Vue 3! 🎉
