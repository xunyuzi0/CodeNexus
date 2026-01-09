<script setup>
import { ref } from 'vue'
import axios from 'axios'

// 1. 定义变量
const code = ref(`public class Main {
    public static void main(String[] args) {
        System.out.println("Hello Docker World!");
        System.out.println("This is from Vue + SpringBoot");
    }
}`)
const result = ref('')
const loading = ref(false)

// 2. 提交代码的方法
const submitCode = async () => {
  loading.value = true
  result.value = '运行中...'

  try {
    // 发送 POST 请求给后端
    const res = await axios.post('http://localhost:8080/api/code/run', {
      code: code.value,
      language: 'java', // 暂时没用，先传着
    })

    // 拿到结果
    result.value = res.data
    // 简单粗暴：弹窗显示
    alert('运行成功！\n' + res.data)
  } catch (error) {
    result.value = '运行出错: ' + error.message
    alert('出错了！')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container">
    <h1>CodeNexus 极简测试版</h1>

    <textarea v-model="code" class="code-editor" spellcheck="false"></textarea>

    <div class="btn-box">
      <button @click="submitCode" :disabled="loading" class="run-btn">
        {{ loading ? '运行中...' : '运行代码 (Run)' }}
      </button>
    </div>

    <div class="output-box">
      <h3>运行结果：</h3>
      <pre>{{ result }}</pre>
    </div>
  </div>
</template>

<style scoped>
.container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.code-editor {
  width: 100%;
  height: 200px;
  background-color: #1e1e1e;
  color: #d4d4d4;
  font-family: 'Consolas', monospace;
  font-size: 16px;
  padding: 10px;
  border-radius: 8px;
  border: none;
  outline: none;
  resize: vertical;
}
.btn-box {
  margin: 20px 0;
  text-align: right;
}
.run-btn {
  padding: 10px 20px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
.run-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
.output-box {
  background-color: #f4f4f4;
  padding: 15px;
  border-radius: 8px;
  border-left: 5px solid #42b983;
}
pre {
  white-space: pre-wrap; /* 保留换行 */
}
</style>
