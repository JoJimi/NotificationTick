<!-- src/views/stock/StockList.vue -->
<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span>종목 목록</span>
        <div class="actions">
          <el-input
              v-model="keyword"
              placeholder="심볼/이름/ISIN 검색"
              clearable
              @keyup.enter="applySearch"
              style="width: 260px"
          />
          <el-button type="primary" @click="applySearch">검색</el-button>
        </div>
      </div>
    </template>

    <el-table
        :data="rows"
        v-loading="loading"
        empty-text="데이터가 없습니다."
        @row-click="goDetail"
        style="width: 100%"
        header-cell-class-name="table-header"
    >
      <el-table-column prop="symbol" label="심볼" width="120" />
      <el-table-column prop="name" label="종목명" min-width="200" />
      <el-table-column prop="market" label="시장" width="120" />
      <el-table-column prop="isin" label="ISIN" min-width="200" />

      <!-- 추가: 등락률/거래량 -->
      <el-table-column label="등락률(%)" width="120">
        <template #default="{ row }">
          <span>{{ row.changeRate != null ? Number(row.changeRate).toFixed(2) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="거래량" width="140">
        <template #default="{ row }">
          <span>{{ row.volume != null ? Number(row.volume).toLocaleString() : '-' }}</span>
        </template>
      </el-table-column>

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
import { fetchStocks } from '@/api/stock';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const page = ref(Number(route.query.page || 1));
const size = ref(Number(route.query.size || 20));
const keyword = ref(String(route.query.keyword || ''));

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
    const data = await fetchStocks({ keyword: keyword.value, page: page.value, size: size.value });
    pageData.value = data;
  } catch (e) {
    ElMessage.error('목록을 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
}

function syncQuery() {
  router.replace({
    path: '/stocks',
    query: {
      page: String(page.value),
      size: String(size.value),
      ...(keyword.value ? { keyword: keyword.value } : {}),
    },
  });
}

function applySearch() { page.value = 1; syncQuery(); }
function onPageChange(p) { page.value = p; syncQuery(); }
function onSizeChange(s) { size.value = s; page.value = 1; syncQuery(); }

function goDetail(row) {
  router.push(`/stocks/${encodeURIComponent(row.symbol)}`);
}

watch(
    () => route.query,
    (q) => {
      page.value = Number(q.page || 1);
      size.value = Number(q.size || 20);
      keyword.value = String(q.keyword || '');
      load();
    }
);

onMounted(load);
</script>

<style scoped>
.header-row{display:flex;justify-content:space-between;align-items:center}
.actions{display:flex;gap:8px;align-items:center}
.pager{display:flex;justify-content:flex-end;margin-top:12px}
.table-header{font-weight:700}
</style>
