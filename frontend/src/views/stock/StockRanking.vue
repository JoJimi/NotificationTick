<!-- src/views/StockRanking.vue -->
<template>
  <el-card>
    <template #header>종목 랭킹 (관심수 DESC)</template>

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
      <el-table-column prop="watchCount" label="관심수" width="120" />
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
import { fetchStockRanking } from '@/api/stock';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const page = ref(Number(route.query.page || 1));
const size = ref(Number(route.query.size || 20));

const pageData = ref({
  content: [],
  number: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
});

const rows = computed(() => pageData.value.content || []);
const totalElements = computed(() => pageData.value.totalElements || 0);

async function load() {
  loading.value = true;
  try {
    const data = await fetchStockRanking({ page: page.value, size: size.value });
    pageData.value = data;
  } catch (e) {
    ElMessage.error('랭킹을 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
}

function syncQuery() {
  router.replace({
    path: '/stocks/ranking',
    query: { page: String(page.value), size: String(size.value) },
  });
}

function onPageChange(p) { page.value = p; syncQuery(); }
function onSizeChange(s) { size.value = s; page.value = 1; syncQuery(); }

function goDetail(row) {
  router.push(`/stocks/${encodeURIComponent(row.symbol)}`);
}

watch(() => route.query, () => load());
onMounted(load);
</script>

<style scoped>
.pager{display:flex;justify-content:flex-end;margin-top:12px}
</style>
