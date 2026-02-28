# IDEA Git SSH 配置指南

## 当前状态

✅ Git Remote 已配置为 HTTPS：
```
origin  https://github.com/wuyouke/demo-ai.git (fetch)
origin  https://github.com/wuyouke/demo-ai.git (push)
```

✅ SSH 密钥已加载到系统：
- `github_demo_ai` (ED25519)
- `id_rsa` (RSA)

## IDEA SSH 配置步骤

### 方案 1：HTTPS（推荐 - 当前配置）

这是当前的配置方式，IDEA 会提示输入 GitHub 凭证：

1. **首次使用时**，IDEA 会要求输入：
   - Username: `wuyouke`
   - Password: 使用 GitHub Personal Access Token（不是密码）

2. **生成 GitHub Token**：
   - 访问 https://github.com/settings/tokens
   - 点击 "Generate new token"
   - 选择 `repo` 权限
   - 复制 Token

3. **在 IDEA 中配置**：
   - `Preferences` → `Version Control` → `GitHub`
   - 点击 `+` 登录
   - 选择 "Login with Token"
   - 粘贴 Token

**优点**：简单、安全、支持二因素认证

---

### 方案 2：SSH（如需使用）

如果想使用 SSH 连接（需要额外配置）：

#### 步骤 1：修改 Remote URL

```bash
git remote set-url origin git@github.com:wuyouke/demo-ai.git
```

#### 步骤 2：IDEA 中配置 SSH

**Mac 用户**：

1. 打开 `Preferences` → `Tools` → `SSH Configurations`
2. 点击 `+` 添加配置
3. 设置如下：
   ```
   Host: github.com
   User: git
   Port: 22
   Auth type: Key pair
   Private key file: ~/.ssh/github_demo_ai
   Known hosts file: ~/.ssh/known_hosts
   ```
4. 点击 "Test Connection" 验证

**如果失败**，改为配置多个密钥：
   - 首先尝试 `~/.ssh/github_demo_ai`
   - 然后尝试 `~/.ssh/id_rsa`
   - 最后尝试 `~/.ssh/id_ed25519`

#### 步骤 3：确保系统 SSH 配置正确

编辑 `~/.ssh/config`，添加：

```
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/github_demo_ai
    IdentityFile ~/.ssh/id_rsa
    IdentityFile ~/.ssh/id_ed25519
    AddKeysToAgent yes
    IdentitiesOnly yes
```

#### 步骤 4：启动 SSH Agent

每次启动终端时运行：

```bash
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/github_demo_ai
ssh-add ~/.ssh/id_rsa
```

或者添加到 `~/.zshrc` 或 `~/.bash_profile`：

```bash
if [ -z "$SSH_AUTH_SOCK" ]; then
    eval "$(ssh-agent -s)"
    ssh-add ~/.ssh/github_demo_ai 2>/dev/null
    ssh-add ~/.ssh/id_rsa 2>/dev/null
fi
```

---

## 推荐方案

**推荐使用 HTTPS + Token**（当前配置）

原因：
- ✅ 更安全（Token 可随时撤销）
- ✅ 支持 GitHub 两步验证
- ✅ 配置简单
- ✅ 跨平台兼容性好

---

## 常见问题

### Q1: IDEA 仍然提示输入密钥？

**A**: 这是正常的 HTTPS Token 流程。按照方案 1 配置 GitHub Token 即可。

### Q2: 如何在 IDEA 中查看当前 Git 配置？

**A**:
- 打开 `Preferences` → `Version Control` → `Git`
- 查看当前的 Git executable 路径和 SSH 配置

### Q3: 如何测试 SSH 连接？

**A**: 在终端运行：
```bash
ssh -T git@github.com
```

成功时会显示：
```
Hi wuyouke! You've successfully authenticated, but GitHub does not provide shell access.
```

### Q4: 需要同时配置 SSH 和 HTTPS 吗？

**A**: 不需要。选一个用就行。推荐 HTTPS。

---

## 已加载的 SSH 密钥

当前系统已加载以下密钥：

```
256 SHA256:WIhBZbAv+1T5de0DC/EQycbjC9efVJYYlIp1FcnP3NI wuyouke@demo-ai (ED25519)
3072 SHA256:S0lGVQQAQfQw8BDqo9/eq/7Ce++0zooTuDIUMPztwGI wuyouke@MBP-JRQ56QVQ7R-0057.local (RSA)
```

---

## 快速命令参考

```bash
# 查看当前 Remote 配置
git remote -v

# 切换到 HTTPS
git remote set-url origin https://github.com/wuyouke/demo-ai.git

# 切换到 SSH
git remote set-url origin git@github.com:wuyouke/demo-ai.git

# 测试 GitHub 连接
ssh -T git@github.com

# 加载 SSH 密钥
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/github_demo_ai
ssh-add ~/.ssh/id_rsa

# 查看已加载的密钥
ssh-add -l
```

---

## 最后一步：在 IDEA 中使用

1. **打开项目**在 IDEA 中
2. **使用 Git 操作**：
   - `Git` → `Pull` / `Push` / `Commit`
   - 首次会提示输入 GitHub 凭证
   - 凭证会被保存，之后自动使用

---

**配置完成！你现在可以在 IDEA 中无缝使用 Git 了。** ✅

