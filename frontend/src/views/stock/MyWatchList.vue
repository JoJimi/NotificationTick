<!-- src/views/stock/MyWatchList.vue -->
<template>
  <el-card class="mt16">
    <template #header>
      <div class="header-row">
        <span>내 관심종목</span>
      </div>
    </template>

    <el-table
        :data="rows"
        v-loading="loading"
        empty-text="관심종목이 없습니다."
        @row-click="goDetail"
        style="width: 100%"
        header-cell-class-name="table-header"
    >
      <el-table-column prop="symbol" label="심볼" width="120" />
      <el-table-column prop="name" label="종목명" min-width="220" />
      <el-table-column prop="market" label="시장" width="120" />
      <el-table-column prop="watchCount" label="관심수" width="120" />
      <el-table-column label="" width="160">
        <template #default="{ row }">
          <el-button size="small" @click.stop="goDetail(row)">상세</el-button>
          <el-button size="small" text type="danger" @click.stop="unwatch(row)">해제</el-button>
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
import { fetchMyWatchList, removeWatch } from '@/api/watchlist';

const route = useRoute();
const router = useRouter();

const page = ref(Number(route.query.wpage || 1));
const size = ref(Number(route.query.wsize || 20));

const loading = ref(false);
const pageData = ref({ content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 });

const rows = computed(() => pageData.value.content || []);
const totalElements = computed(() => pageData.value.totalElements || 0);

async function load() {
  loading.value = true;
  try {
    const data = await fetchMyWatchList({ page: page.value, size: size.value });
    pageData.value = data;
  } catch (e) {
    ElMessage.error('관심종목을 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
}

function syncQuery() {
  router.replace({
    path: '/profile',
    query: { ...route.query, wpage: String(page.value), wsize: String(size.value) },
  });
}

function onPageChange(p) { page.value = p; syncQuery(); }
function onSizeChange(s) { size.value = s; page.value = 1; syncQuery(); }

function goDetail(row) { router.push(`/stocks/${encodeURIComponent(row.symbol)}`); }

async function unwatch(row) {
  try {
    await removeWatch(row.symbol);
    ElMessage.success('관심 해제되었습니다.');
    load();
  } catch (e) {
    ElMessage.error('해제에 실패했습니다.');
  }
}

watch(() => route.query, () => load());
onMounted(load);
</script>

<style scoped>
.mt16{ margin-top:16px }
.header-row{ display:flex; justify-content:space-between; align-items:center }
.pager{ display:flex; justify-content:flex-end; margin-top:12px }
.table-header{ font-weight:700 }
</style>
