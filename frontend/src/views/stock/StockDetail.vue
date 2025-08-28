<!-- src/views/StockDetail.vue -->
<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span>종목 상세</span>
        <el-button @click="$router.back()">뒤로</el-button>
        <div class="btns">
          <el-button @click="$router.back()">뒤로</el-button>
          <el-button type="primary" :disabled="!stock" @click="$router.push(`/news/${stock.symbol}`)">관련 뉴스</el-button>
        </div>
      </div>
    </template>

    <el-skeleton v-if="loading" :rows="6" animated />
    <template v-else>
      <el-descriptions title="" :column="2" border v-if="stock">
        <el-descriptions-item label="ID">{{ stock.id }}</el-descriptions-item>
        <el-descriptions-item label="심볼">{{ stock.symbol }}</el-descriptions-item>
        <el-descriptions-item label="종목명">{{ stock.name }}</el-descriptions-item>
        <el-descriptions-item label="시장">{{ stock.market }}</el-descriptions-item>
        <el-descriptions-item label="ISIN">{{ stock.isin }}</el-descriptions-item>
        <el-descriptions-item label="관심수">{{ stock.watchCount }}</el-descriptions-item>
      </el-descriptions>
      <div v-else>데이터가 없습니다.</div>
    </template>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { fetchStockBySymbol } from '@/api/stock';

const route = useRoute();
const loading = ref(false);
const stock = ref(null);

async function load(symbol) {
  if (!symbol) return;
  loading.value = true;
  try {
    stock.value = await fetchStockBySymbol(symbol);
  } catch (e) {
    ElMessage.error('상세 정보를 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
}

onMounted(() => load(route.params.symbol));
watch(() => route.params.symbol, (s) => load(s));
</script>

<style scoped>
.header-row{display:flex;justify-content:space-between;align-items:center}
.btns{display:flex;gap:8px;align-items:center}
</style>
