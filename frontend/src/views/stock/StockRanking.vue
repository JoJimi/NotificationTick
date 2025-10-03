<!-- src/views/stock/StockRanking.vue -->
<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span>종목 랭킹</span>
      </div>
    </template>

    <el-tabs v-model="by" @tab-change="onTabChange">
      <el-tab-pane label="관심수" name="watch" />
      <el-tab-pane label="등락률" name="change" />
      <el-tab-pane label="거래량" name="volume" />
    </el-tabs>

    <el-table
        :data="rows"
        v-loading="loading"
        empty-text="데이터가 없습니다."
        @row-click="goDetail"
        style="width: 100%"
    >
      <el-table-column type="index" label="#" width="70" />
      <el-table-column prop="symbol" label="심볼" width="120" />
      <el-table-column prop="name" label="종목명" min-width="200" />
      <el-table-column prop="market" label="시장" width="120" />

      <!-- 모드별 컬럼 -->
      <el-table-column
          v-if="by === 'watch'"
          prop="watchCount"
          label="관심수"
          width="120"
      />
      <el-table-column
          v-else-if="by === 'change'"
          label="등락률(%)"
          width="140"
      >
        <template #default="{ row }">
          <span>{{ formatChange(row.changeRate) }}</span>
        </template>
      </el-table-column>
      <el-table-column
          v-else
          label="거래량"
          width="160"
      >
        <template #default="{ row }">
          <span>{{ formatNumber(row.volume) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="" width="120">
        <template #default="{ row }">
          <el-button size="small" @click.stop="goDetail(row)">상세</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :page-sizes="[10,20,50,100]"
          :page-size="size"
          :current-page="page"
          :total="totalElements"
          @size-change="onSizeChange"
          @current-change="onPageChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  fetchStockRanking,
  fetchStockRankingByChangeRate,
  fetchStockRankingByVolume,
} from '@/api/stock';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const page = ref(Number(route.query.page || 1));
const size = ref(Number(route.query.size || 20));
const by = ref(['watch', 'change', 'volume'].includes(String(route.query.by)) ? String(route.query.by) : 'watch');

const pageData = ref({
  content: [],
  number: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
});

const rows = computed(() => pageData.value.content || []);
const totalElements = computed(() => pageData.value.totalElements || 0);

function syncQuery() {
  router.replace({
    path: '/stocks/ranking',
    query: { page: String(page.value), size: String(size.value), by: by.value },
  });
}

function onTabChange() {
  page.value = 1;
  syncQuery();
}

function onPageChange(p) { page.value = p; syncQuery(); }
function onSizeChange(s) { size.value = s; page.value = 1; syncQuery(); }

function goDetail(row) {
  router.push(`/stocks/${encodeURIComponent(row.symbol)}`);
}

function formatNumber(n) {
  if (n == null) return '-';
  try { return Number(n).toLocaleString(); } catch { return String(n); }
}
function formatChange(v) {
  if (v == null) return '-';
  const num = Number(v);
  return isNaN(num) ? '-' : num.toFixed(2);
}

async function load() {
  loading.value = true;
  try {
    let data;
    if (by.value === 'change') {
      data = await fetchStockRankingByChangeRate({ page: page.value, size: size.value });
    } else if (by.value === 'volume') {
      data = await fetchStockRankingByVolume({ page: page.value, size: size.value });
    } else {
      data = await fetchStockRanking({ page: page.value, size: size.value });
    }
    pageData.value = data;
  } catch (e) {
    ElMessage.error('랭킹을 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
}

watch(
    () => route.query,
    (q) => {
      page.value = Number(q.page || 1);
      size.value = Number(q.size || 20);
      by.value = ['watch', 'change', 'volume'].includes(String(q.by)) ? String(q.by) : 'watch';
      load();
    }
);

onMounted(load);
</script>

<style scoped>
.header-row{display:flex;justify-content:space-between;align-items:center}
.pager{display:flex;justify-content:flex-end;margin-top:12px}
</style>
