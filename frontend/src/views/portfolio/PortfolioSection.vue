<!-- src/views/portfolio/PortfolioSection.vue -->
<template>
  <el-card class="mt16">
    <template #header>
      <div class="header-row">
        <span>내 포트폴리오</span>
        <div class="actions">
          <el-button-group>
            <el-button type="primary" @click="openCreate">새 포트폴리오</el-button>
            <el-button @click="load" :loading="loading">새로고침</el-button>
          </el-button-group>
        </div>
      </div>
    </template>

    <el-table
        :data="rows"
        v-loading="loading"
        empty-text="포트폴리오가 없습니다."
        style="width: 100%"
        header-cell-class-name="table-header"
    >
      <el-table-column prop="name" label="이름" min-width="220" />
      <el-table-column prop="description" label="설명" min-width="320">
        <template #default="{ row }">
          <div class="ellipsis" :title="row.description || ''">{{ row.description || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="생성/수정" width="210">
        <template #default="{ row }">
          <div class="meta">
            <span>{{ fmt(row.createdAt) }}</span>
            <span class="sep">•</span>
            <span>{{ fmt(row.updatedAt) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="" width="220" align="right">
        <template #default="{ row }">
          <el-button size="small" @click="goDetails(row)">상세</el-button>
          <el-button size="small" @click="openEdit(row)">수정</el-button>
          <el-button size="small" type="danger" text @click="onDelete(row)">삭제</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 생성/수정 다이얼로그 -->
    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '새 포트폴리오' : '포트폴리오 수정'" width="520">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="88px" @submit.prevent>
        <el-form-item label="이름" prop="name">
          <el-input v-model="form.name" maxlength="60" show-word-limit placeholder="예: 테크장기투자" />
        </el-form-item>
        <el-form-item label="설명" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="예: AI·클라우드 중심으로 5년 이상 홀딩"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialog.visible = false">취소</el-button>
        <el-button type="primary" :loading="saving" @click="submit">저장</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchMyPortfolios, createPortfolio, updatePortfolio, deletePortfolio } from '@/api/portfolio';

const router = useRouter();

const loading = ref(false);
const list = ref([]);
const rows = computed(() =>
    [...list.value].sort((a, b) => new Date(b.updatedAt || b.createdAt) - new Date(a.updatedAt || a.createdAt))
);

function fmt(t) { return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'; }

async function load() {
  loading.value = true;
  try {
    list.value = await fetchMyPortfolios();
  } catch {
    ElMessage.error('포트폴리오를 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
}

const dialog = ref({ visible: false, mode: 'create', targetId: null });
const formRef = ref();
const form = ref({ name: '', description: '' });
const rules = {
  name: [
    { required: true, message: '이름을 입력하세요.', trigger: 'blur' },
    { min: 2, message: '2자 이상 입력하세요.', trigger: 'blur' },
  ],
};

function openCreate() {
  dialog.value = { visible: true, mode: 'create', targetId: null };
  form.value = { name: '', description: '' };
}

function openEdit(row) {
  dialog.value = { visible: true, mode: 'edit', targetId: row.id };
  form.value = { name: row.name || '', description: row.description || '' };
}

const saving = ref(false);
async function submit() {
  try {
    await formRef.value.validate();
  } catch { return; }

  saving.value = true;
  try {
    if (dialog.value.mode === 'create') {
      await createPortfolio({ ...form.value });
      ElMessage.success('포트폴리오가 생성되었습니다.');
    } else {
      await updatePortfolio(dialog.value.targetId, { ...form.value });
      ElMessage.success('포트폴리오가 수정되었습니다.');
    }
    dialog.value.visible = false;
    load();
  } catch {
    ElMessage.error('저장에 실패했습니다.');
  } finally {
    saving.value = false;
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`포트폴리오 "${row.name}"을(를) 삭제할까요?`, '삭제 확인', {
      confirmButtonText: '삭제',
      cancelButtonText: '취소',
      type: 'warning',
    });
  } catch { return; }

  try {
    await deletePortfolio(row.id);
    ElMessage.success('삭제되었습니다.');
    load();
  } catch {
    ElMessage.error('삭제에 실패했습니다.');
  }
}

function goDetails(row) {
  router.push(`/portfolios/${encodeURIComponent(row.id)}`);
}

onMounted(load);
</script>

<style scoped>
.mt16{ margin-top:16px }
.header-row{ display:flex; justify-content:space-between; align-items:center }
.actions{ display:flex; gap:8px; align-items:center }
.table-header{ font-weight:700 }
.meta{ display:flex; gap:6px; color:#777; font-size:12px }
.sep{ opacity:.6 }
.ellipsis{ white-space:nowrap; overflow:hidden; text-overflow:ellipsis }
</style>
