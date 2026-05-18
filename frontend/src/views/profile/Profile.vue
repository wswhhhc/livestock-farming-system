<template>
  <div class="page-container">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="header-text">
          <h1 class="page-title">个人资料</h1>
          <p class="page-subtitle">管理您的账户信息和安全设置</p>
        </div>
      </div>
    </div>

    <div class="profile-grid">
      <!-- 个人信息卡片 -->
      <el-card class="profile-card glass-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <el-icon :size="20"><InfoFilled /></el-icon>
            <span>个人信息</span>
          </div>
        </template>
        <div class="profile-avatar-section">
          <div class="profile-avatar">{{ userInitial }}</div>
          <div class="profile-meta">
            <div class="profile-name">{{ profile.realName || profile.username }}</div>
            <div class="profile-role-tag">
              <el-tag :type="profile.role === 2 ? 'danger' : 'success'" size="small" effect="plain">
                {{ profile.roleName }}
              </el-tag>
            </div>
            <div class="profile-joined">注册时间：{{ profile.createTime }}</div>
          </div>
        </div>
      </el-card>

      <!-- 编辑资料卡片 -->
      <el-card class="profile-card glass-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <el-icon :size="20"><Edit /></el-icon>
            <span>编辑资料</span>
          </div>
        </template>
        <el-form :model="profileForm" label-width="80px" class="profile-form">
          <el-form-item label="用户名">
            <el-input :model-value="profile.username" disabled />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSaveProfile">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 修改密码卡片 -->
      <el-card class="profile-card glass-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <el-icon :size="20"><Lock /></el-icon>
            <span>修改密码</span>
          </div>
        </template>
        <el-form :model="pwdForm" label-width="100px" class="profile-form">
          <el-form-item label="原密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="changingPwd" @click="handleChangePassword">确认修改</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, changePassword } from '../../api/user'

const profile = reactive({
  username: '',
  realName: '',
  phone: '',
  role: 1,
  roleName: '',
  createTime: ''
})

const profileForm = reactive({
  realName: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const saving = ref(false)
const changingPwd = ref(false)

const userInitial = computed(() => {
  const name = profile.realName || profile.username
  return name ? name.charAt(0).toUpperCase() : 'U'
})

function formatTime(time) {
  if (!time) return ''
  return time.substring(0, 10)
}

onMounted(async () => {
  try {
    const data = await getProfile()
    Object.assign(profile, {
      ...data,
      createTime: formatTime(data.createTime)
    })
    profileForm.realName = data.realName || ''
    profileForm.phone = data.phone || ''
  } catch {
    // handled by http interceptor
  }
})

async function handleSaveProfile() {
  saving.value = true
  try {
    await updateProfile({
      realName: profileForm.realName,
      phone: profileForm.phone
    })
    profile.realName = profileForm.realName
    profile.phone = profileForm.phone
    ElMessage.success('资料更新成功')
  } catch {
    // handled by http interceptor
  } finally {
    saving.value = false
  }
}

async function handleChangePassword() {
  if (!pwdForm.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!pwdForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  changingPwd.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch {
    // handled by http interceptor
  } finally {
    changingPwd.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 52px;
  height: 52px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  backdrop-filter: blur(12px);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.3px;
}

.page-subtitle {
  font-size: 13px;
  color: var(--text-tertiary);
  margin: 0;
}

.profile-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--glass-border-strong);
  background: var(--glass-bg-strong);
  backdrop-filter: blur(20px);
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.profile-avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 8px 0;
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: #fff;
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 16px rgba(93, 140, 93, 0.3);
}

.profile-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.profile-role-tag {
  display: flex;
}

.profile-joined {
  font-size: 13px;
  color: var(--text-tertiary);
}

.profile-form {
  max-width: 420px;
}
</style>
