<!-- src/views/news/NewsBySymbol.vue -->
<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <div class="title">
          <span>뉴스 • </span>
          <el-tag type="info" effect="plain">{{ symbol }}</el-tag>
        </div>
        <div class="actions">
          <el-input
              v-model="inputSymbol"
              placeholder="심볼 입력 (예: 005930)"
              clearable
              @keyup.enter="goSymbol"
              style="width: 220px"
          />
          <el-button type="primary" @click="goSymbol">이동</el-button>
        </div>
      </div>
    </template>

    <!-- 로딩 -->
    <el-skeleton v-if="loading" :rows="6" animated />

    <!-- 본문 -->
    <template v-else>
      <div v-if="items.length === 0" class="empty">
        해당 종목의 뉴스가 없습니다.
      </div>

      <el-table
          v-else
          :data="pagedItems"
          header-cell-class-name="table-header"
          style="width: 100%"
      >
        <el-table-column label="제목" min-width="320">
          <template #default="{ row }">
            <a
                class="title-link"
                :href="row.content"
                target="_blank"
                rel="noopener noreferrer"
                @click.stop
            >
              {{ row.title }}
            </a>
            <el-tag v-if="row.source" size="small" class="ml8" effect="plain">{{ row.source }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="요약" min-width="360">
          <template #default="{ row }">
            <div class="summary" :title="row.summary || ''">
              {{ row.summary || '요약 없음' }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="발행시각" width="160">
          <template #default="{ row }">
            {{ formatDate(row.publishedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="" width="120" align="center">
          <template #default="{ row }">
            <el-button size="small" @click.stop="open(row.content)">열기</el-button>
            <el-button size="small" text @click.stop="copy(row.content)">복사</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager" v-if="items.length > pageSize">
        <el-pagination
            background
            layout="prev, pager, next, sizes, total"
            :page-sizes="[10,20,50]"
            :page-size="pageSize"
            :current-page="page"
            :total="items.length"
            @size-change="onSizeChange"
            @current-change="onPageChange"
        />
      </div>
    </template>
  </el-card>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';
import { fetchNewsBySymbol } from '@/api/news';

const route = useRoute();
const router = useRouter();

const symbol = computed(() => String(route.params.symbol || ''));
const inputSymbol = ref(symbol.value);

const loading = ref(false);
const rawItems = ref([]); // 원본 받아온 데이터

// 최신순 정렬 후 사용
const items = computed(() =>
    [...rawItems.value].sort((a, b) => new Date(b.publishedAt) - new Date(a.publishedAt))
);

// 페이지네이션(클라이언트 사이드)
const page = ref(Number(route.query.page || 1));
const pageSize = ref(Number(route.query.size || 20));
const pagedItems = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return items.value.slice(start, start + pageSize.value);
});

function formatDate(iso) {
  if (!iso) return '-';
  return dayjs(iso).format('YYYY-MM-DD HH:mm');
}

async function load() {
  if (!symbol.value) return;
  loading.value = true;
  try {
    rawItems.value = await fetchNewsBySymbol(symbol.value);
  } catch (e) {
    rawItems.value = [];
    ElMessage.error('뉴스를 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
}

function syncQuery() {
  router.replace({
    path: `/news/${encodeURIComponent(symbol.value)}`,
    query: { page: String(page.value), size: String(pageSize.value) },
  });
}

function onPageChange(p) { page.value = p; syncQuery(); }
function onSizeChange(s) { pageSize.value = s; page.value = 1; syncQuery(); }

function goSymbol() {
  const s = (inputSymbol.value || '').trim();
  if (!s) return;
  page.value = 1;
  router.push({ path: `/news/${encodeURIComponent(s)}`, query: { page: '1', size: String(pageSize.value) } });
}

function open(url) {
  if (!url) return;
  window.open(url, '_blank', 'noopener,noreferrer');
}
async function copy(url) {
  if (!url) return;
  try {
    await navigator.clipboard.writeText(url);
    ElMessage.success('링크를 복사했습니다.');
  } catch (_) {
    ElMessage.warning('복사에 실패했습니다.');
  }
}

watch(() => route.params.symbol, () => {
  inputSymbol.value = symbol.value;
  load();
});
watch(() => route.query, (q) => {
  page.value = Number(q.page || 1);
  pageSize.value = Number(q.size || 20);
});

onMounted(load);
</script>

<style scoped>
.header-row{display:flex;justify-content:space-between;align-items:center}
.title{display:flex;align-items:center;gap:4px}
.actions{display:flex;gap:8px;align-items:center}
.table-header{font-weight:700}
.summary{
  display:-webkit-box;
  -webkit-line-clamp:2;
  -webkit-box-orient:vertical;
  overflow:hidden;
  line-height:1.4;
}
.pager{display:flex;justify-content:flex-end;margin-top:12px}
.ml8{margin-left:8px}
.empty{padding:24px 8px;color:#777}
.title-link{text-decoration:none}
.title-link:hover{text-decoration:underline}
</style>
