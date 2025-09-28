<!-- src/views/stock/StockDetail.vue -->
<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span>종목 상세</span>
        <div class="btns">
          <el-button-group>
            <el-button @click="$router.back()">뒤로</el-button>
            <el-button :type="watching ? 'success' : 'info'" :disabled="!stock" @click="onToggle">
              {{ watching ? '관심 해제' : '관심 등록' }} ({{ count }})
            </el-button>
            <el-button type="primary" :disabled="!stock" @click="$router.push(`/news/${stock.symbol}`)">관련 뉴스</el-button>
          </el-button-group>
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

        <!-- 추가: 등락률/거래량 -->
        <el-descriptions-item label="등락률(%)">
          {{ stock.changeRate != null ? Number(stock.changeRate).toFixed(2) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="거래량">
          {{ stock.volume != null ? Number(stock.volume).toLocaleString() : '-' }}
        </el-descriptions-item>

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
import { getWatchState, toggleWatch } from '@/api/watchlist';

const route = useRoute();
const loading = ref(false);
const stock = ref(null);

const watching = ref(false);
const count = ref(0);

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
  await loadWatch(symbol);
}

async function loadWatch(symbol) {
  try {
    const res = await getWatchState(symbol);
    watching.value = !!res.watching;
    count.value = Number(res.watchCount || 0);
    if (stock.value) stock.value.watchCount = count.value; // 화면 동기화
  } catch (_) {
    // 비로그인/권한 문제 등은 토글 시 에러 메시지로 처리
  }
}

async function onToggle() {
  if (!stock.value) return;
  try {
    const res = await toggleWatch(stock.value.symbol);
    watching.value = !!res.watching;
    count.value = Number(res.watchCount || 0);
    if (stock.value) stock.value.watchCount = count.value;
    ElMessage.success(watching.value ? '관심 등록되었습니다.' : '관심 해제되었습니다.');
  } catch (e) {
    ElMessage.error('처리에 실패했습니다.');
  }
}

onMounted(() => load(route.params.symbol));
watch(() => route.params.symbol, (s) => load(s));
</script>

<style scoped>
.header-row{display:flex;justify-content:space-between;align-items:center}
.btns{display:flex;gap:8px;align-items:center}
</style>
