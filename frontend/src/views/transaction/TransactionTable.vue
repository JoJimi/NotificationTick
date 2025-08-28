<!-- src/views/transaction/TransactionTable.vue -->
<template>
  <el-card class="mt16">
    <template #header>
      <div class="header-row">
        <span>거래 내역</span>
        <div class="actions">
          <el-input v-model="search" placeholder="심볼 검색 (예: AAPL)" clearable @keyup.enter="applySearch" style="width: 200px" />
          <el-button @click="applySearch">검색</el-button>
          <el-button type="primary" @click="openCreate">새 거래</el-button>
          <el-button @click="load" :loading="loading">새로고침</el-button>
        </div>
      </div>
    </template>

    <el-table
        :data="pagedRows"
        v-loading="loading"
        empty-text="거래 내역이 없습니다."
        style="width:100%"
        header-cell-class-name="table-header"
        @row-dblclick="openEdit"
    >
      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="stockSymbol" label="심볼" width="120">
        <template #default="{ row }">
          <el-tag type="info" effect="plain">{{ row.stockSymbol }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tradeType" label="유형" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.tradeType === 'BUY' ? 'success' : 'danger'">{{ row.tradeType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="수량" width="110" align="right" />
      <el-table-column prop="price" label="가격" width="140" align="right">
        <template #default="{ row }">{{ money(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="총액" width="160" align="right">
        <template #default="{ row }">{{ money(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="생성" width="170">
        <template #default="{ row }">{{ dt(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="수정" width="170">
        <template #default="{ row }">{{ dt(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="" width="230" align="right">
        <template #default="{ row }">
          <el-button size="small" @click.stop="openEdit(row)">수정</el-button>
          <el-button size="small" type="danger" text @click.stop="onDelete(row)">삭제</el-button>
          <el-dropdown>
            <el-button size="small" text>더보기</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push(`/news/${row.stockSymbol}`)">뉴스 보기</el-dropdown-item>
                <el-dropdown-item @click="$router.push(`/stocks/${row.stockSymbol}`)">종목 상세</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager" v-if="filteredRows.length > pageSize">
      <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :page-sizes="[10,20,50,100]"
          :page-size="pageSize"
          :current-page="page"
          :total="filteredRows.length"
          @size-change="onSizeChange"
          @current-change="onPageChange"
      />
    </div>

    <!-- 생성/수정 다이얼로그 -->
    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '새 거래' : '거래 수정'" width="520">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" @submit.prevent>
        <el-form-item label="심볼" prop="stockSymbol">
          <el-input v-model="form.stockSymbol" :disabled="dialog.mode==='edit'" placeholder="예: AAPL / 005930" />
        </el-form-item>
        <el-form-item label="유형" prop="tradeType">
          <el-select v-model="form.tradeType" placeholder="선택">
            <el-option label="BUY (매수)" value="BUY" />
            <el-option label="SELL (매도)" value="SELL" />
          </el-select>
        </el-form-item>
        <el-form-item label="수량" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :step="1" :precision="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="가격" prop="price">
          <el-input-number v-model="form.price" :min="0.0001" :step="0.01" :precision="4" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible=false">취소</el-button>
        <el-button type="primary" :loading="saving" @click="submit">저장</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import { fetchTransactions, createTransaction, updateTransaction, deleteTransaction } from '@/api/transactions';

const props = defineProps({
  portfolioId: { type: [String, Number], required: true },
});

const router = useRouter();

const loading = ref(false);
const list = ref([]);
const search = ref('');
const page = ref(1);
const pageSize = ref(20);

const moneyFmt = new Intl.NumberFormat('ko-KR', { maximumFractionDigits: 6 });
const money = (v) => (v == null ? '-' : moneyFmt.format(Number(v)));
const dt = (t) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-');

const rows = computed(() => [...list.value].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
const filteredRows = computed(() => {
  const q = (search.value || '').trim().toUpperCase();
  if (!q) return rows.value;
  return rows.value.filter(r => String(r.stockSymbol || '').toUpperCase().includes(q));
});
const pagedRows = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredRows.value.slice(start, start + pageSize.value);
});

function onPageChange(p){ page.value = p; }
function onSizeChange(s){ pageSize.value = s; page.value = 1; }
function applySearch(){ page.value = 1; }

async function load() {
  if (!props.portfolioId) return;
  loading.value = true;
  try {
    list.value = await fetchTransactions(props.portfolioId);
  } catch {
    ElMessage.error('거래 내역을 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
}

onMounted(load);
watch(() => props.portfolioId, load);

// ====== Dialog / Form ======
const dialog = ref({ visible: false, mode: 'create', targetId: null });
const formRef = ref();
const form = ref({ stockSymbol: '', tradeType: '', quantity: 1, price: 0.01 });
const rules = {
  stockSymbol: [{ required: true, message: '심볼을 입력하세요.', trigger: 'blur' }],
  tradeType: [{ required: true, message: '유형을 선택하세요.', trigger: 'change' }],
  quantity: [{ required: true, type: 'number', min: 1, message: '1 이상 정수', trigger: 'change' }],
  price: [{ required: true, type: 'number', min: 0.0001, message: '0보다 큰 가격', trigger: 'change' }],
};

function openCreate() {
  dialog.value = { visible: true, mode: 'create', targetId: null };
  form.value = { stockSymbol: '', tradeType: 'BUY', quantity: 1, price: 0.01 };
}

function openEdit(row) {
  dialog.value = { visible: true, mode: 'edit', targetId: row.id };
  form.value = {
    stockSymbol: row.stockSymbol, // 수정 시 비활성화
    tradeType: row.tradeType,
    quantity: row.quantity,
    price: Number(row.price),
  };
}

const saving = ref(false);
async function submit() {
  try { await formRef.value.validate(); } catch { return; }
  saving.value = true;
  try {
    if (dialog.value.mode === 'create') {
      await createTransaction(props.portfolioId, {
        stockSymbol: form.value.stockSymbol.trim(),
        tradeType: form.value.tradeType,
        quantity: Number(form.value.quantity),
        price: Number(form.value.price),
      });
      ElMessage.success('거래가 추가되었습니다.');
    } else {
      await updateTransaction(props.portfolioId, dialog.value.targetId, {
        tradeType: form.value.tradeType,
        quantity: Number(form.value.quantity),
        price: Number(form.value.price),
      });
      ElMessage.success('거래가 수정되었습니다.');
    }
    dialog.value.visible = false;
    await load();
  } catch {
    ElMessage.error('저장에 실패했습니다.');
  } finally {
    saving.value = false;
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`이 거래를 삭제할까요? [${row.stockSymbol} ${row.tradeType}]`, '삭제 확인', {
      confirmButtonText: '삭제',
      cancelButtonText: '취소',
      type: 'warning',
    });
  } catch { return; }
  try {
    await deleteTransaction(props.portfolioId, row.id);
    ElMessage.success('삭제되었습니다.');
    await load();
  } catch {
    ElMessage.error('삭제에 실패했습니다.');
  }
}
</script>

<style scoped>
.mt16{ margin-top:16px }
.header-row{ display:flex; justify-content:space-between; align-items:center }
.actions{ display:flex; gap:8px; align-items:center }
.pager{ display:flex; justify-content:flex-end; margin-top:12px }
.table-header{ font-weight:700 }
</style>
