<!-- src/views/portfolio/PortfolioDetails.vue -->
<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span>포트폴리오 상세</span>
        <div class="actions">
          <el-button @click="$router.back()">뒤로</el-button>
          <el-button type="primary" @click="editQuick" :disabled="!portfolio">빠른 수정</el-button>
          <el-button type="danger" text @click="remove" :disabled="!portfolio">삭제</el-button>
        </div>
      </div>
    </template>

    <el-skeleton v-if="loading" :rows="6" animated />
    <template v-else>
      <template v-if="portfolio">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ portfolio.id }}</el-descriptions-item>
          <el-descriptions-item label="이름">{{ portfolio.name }}</el-descriptions-item>
          <el-descriptions-item label="설명" :span="2">{{ portfolio.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="생성">{{ fmt(portfolio.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="수정">{{ fmt(portfolio.updatedAt) }}</el-descriptions-item>
        </el-descriptions>

        <h3 class="sub">거래 내역</h3>
        <el-table :data="txRows" empty-text="거래 내역이 없습니다." style="width:100%">
          <el-table-column prop="_symbol" label="심볼" width="120" />
          <el-table-column prop="_name" label="종목명" min-width="160" />
          <el-table-column prop="_type" label="유형" width="100" />
          <el-table-column prop="_qty" label="수량" width="100" />
          <el-table-column prop="_price" label="가격" width="120" />
          <el-table-column prop="_time" label="체결시각" width="170" />
          <el-table-column label="원본" min-width="220">
            <template #default="{ row }">
              <span class="json">{{ stringify(row._raw) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <div v-else>데이터가 없습니다.</div>
    </template>

    <!-- 빠른 수정 다이얼로그 -->
    <el-dialog v-model="dialog" title="포트폴리오 수정" width="520">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="88px" @submit.prevent>
        <el-form-item label="이름" prop="name">
          <el-input v-model="form.name" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="설명" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog=false">취소</el-button>
        <el-button type="primary" :loading="saving" @click="save">저장</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import { fetchPortfolioDetails, updatePortfolio, deletePortfolio } from '@/api/portfolio';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const portfolio = ref(null);

function fmt(t){ return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'; }
function stringify(obj){ try { return JSON.stringify(obj, null, 0); } catch { return ''; } }

// 거래 필드 유연 매핑
function mapTx(tx) {
  const sym = tx.symbol || tx.stockSymbol || tx.ticker || tx.stock?.symbol || '-';
  const name = tx.stockName || tx.name || tx.stock?.name || '-';
  const type = tx.type || tx.side || '-';
  const qty = tx.quantity ?? tx.qty ?? '-';
  const price = tx.price ?? tx.fillPrice ?? tx.executedPrice ?? '-';
  const time = tx.executedAt || tx.transactedAt || tx.createdAt || null;
  return {
    _symbol: String(sym ?? '-'),
    _name: String(name ?? '-'),
    _type: String(type ?? '-'),
    _qty: qty,
    _price: price,
    _time: time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-',
    _raw: tx,
  };
}
const txRows = computed(() => (portfolio.value?.transaction || []).map(mapTx));

async function load(id) {
  if (!id) return;
  loading.value = true;
  try {
    portfolio.value = await fetchPortfolioDetails(id);
  } catch {
    ElMessage.error('상세 정보를 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
}

onMounted(() => load(route.params.portfolioId));
watch(() => route.params.portfolioId, (id) => load(id));

// 빠른 수정
const dialog = ref(false);
const formRef = ref();
const form = ref({ name: '', description: '' });
const rules = { name: [{ required: true, message: '이름을 입력하세요.', trigger: 'blur' }, { min: 2, message: '2자 이상', trigger: 'blur' }] };
const saving = ref(false);

function editQuick() {
  if (!portfolio.value) return;
  form.value = { name: portfolio.value.name || '', description: portfolio.value.description || '' };
  dialog.value = true;
}

async function save() {
  try { await formRef.value.validate(); } catch { return; }
  saving.value = true;
  try {
    const updated = await updatePortfolio(portfolio.value.id, { ...form.value });
    portfolio.value = { ...portfolio.value, ...updated };
    ElMessage.success('수정되었습니다.');
    dialog.value = false;
  } catch {
    ElMessage.error('수정에 실패했습니다.');
  } finally {
    saving.value = false;
  }
}

// 삭제
async function remove() {
  if (!portfolio.value) return;
  try {
    await ElMessageBox.confirm('포트폴리오를 삭제할까요?', '삭제 확인', {
      confirmButtonText: '삭제',
      cancelButtonText: '취소',
      type: 'warning',
    });
  } catch { return; }

  try {
    await deletePortfolio(portfolio.value.id);
    ElMessage.success('삭제되었습니다.');
    router.replace('/profile');
  } catch {
    ElMessage.error('삭제에 실패했습니다.');
  }
}
</script>

<style scoped>
.header-row{display:flex;justify-content:space-between;align-items:center}
.actions{display:flex;gap:8px;align-items:center}
.sub{margin:18px 0 10px 0}
.json{font-family:ui-monospace, SFMono-Regular, Menlo, monospace; font-size:12px; color:#666}
</style>
