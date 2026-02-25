# Git工作流最佳实践

在团队协作中，规范的Git工作流能大幅提升开发效率。本文分享常用的Git工作流和最佳实践。

## 常见工作流模式

### 1. Git Flow

经典的分支模型，适合版本发布明确的项目：

```
master (生产)
  ↑
release (预发布)
  ↑
develop (开发)
  ↑
feature/* (功能分支)
```

**分支说明：**

- `master`: 生产环境，只接受合并
- `develop`: 开发分支，日常开发的基础
- `feature/*`: 功能分支，开发新功能
- `release/*`: 发布分支，准备发布
- `hotfix/*`: 紧急修复分支

**工作流程：**

```bash
# 1. 从develop创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/user-login

# 2. 开发功能
git add .
git commit -m "feat: implement user login"

# 3. 合并到develop
git checkout develop
git merge feature/user-login

# 4. 创建发布分支
git checkout -b release/v1.1.0

# 5. 发布到master
git checkout master
git merge release/v1.1.0
git tag v1.1.0

# 6. 同步到develop
git checkout develop
git merge release/v1.1.0
```

### 2. GitHub Flow

简化的工作流，适合持续部署：

```
main (生产)
  ↑
feature/* (功能分支)
```

**流程：**

```bash
# 1. 从main创建分支
git checkout main
git pull
git checkout -b feature/add-comments

# 2. 开发并提交
git add .
git commit -m "feat: add comment feature"

# 3. 推送并创建PR
git push origin feature/add-comments

# 4. Code Review后合并到main
# 5. 自动部署到生产环境
```

### 3. GitLab Flow

结合了Git Flow和GitHub Flow的优点：

```
production (生产)
  ↑
pre-production (预生产)
  ↑
main (主分支)
  ↑
feature/* (功能分支)
```

## 提交规范

### Commit Message 格式

采用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type 类型：**

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档变更
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具变更

**示例：**

```bash
# 新功能
git commit -m "feat(auth): add JWT authentication"

# 修复bug
git commit -m "fix(api): handle null pointer in user service"

# 文档更新
git commit -m "docs: update API documentation"

# 重构
git commit -m "refactor(user): extract validation logic"

# 破坏性变更
git commit -m "feat(api)!: change response format

BREAKING CHANGE: API response now includes metadata field"
```

## 分支管理

### 分支命名规范

```bash
# 功能分支
feature/user-authentication
feature/article-comment

# 修复分支
fix/login-error
fix/memory-leak

# 文档分支
docs/api-guide
docs/readme-update

# 重构分支
refactor/user-service
refactor/database-layer
```

### 分支保护规则

```yaml
# .github/workflows/branch-protection.yml
main:
  required_status_checks:
    - tests
    - lint
  required_reviews: 2
  no_force_push: true
  no_delete: true

develop:
  required_status_checks:
    - tests
  required_reviews: 1
```

## 常用操作

### 合并策略

**1. Merge (保留历史)**

```bash
git checkout main
git merge feature/user-login
```

**2. Squash (压缩提交)**

```bash
git checkout main
git merge --squash feature/user-login
git commit -m "feat: add user login feature"
```

**3. Rebase (线性历史)**

```bash
git checkout feature/user-login
git rebase main
git checkout main
git merge feature/user-login
```

### 冲突解决

```bash
# 1. 拉取最新代码
git fetch origin
git rebase origin/main

# 2. 解决冲突
# 编辑冲突文件
git add .
git rebase --continue

# 3. 如果需要放弃
git rebase --abort
```

### 撤销操作

```bash
# 撤销最后一次提交（保留更改）
git reset --soft HEAD~1

# 撤销最后一次提交（丢弃更改）
git reset --hard HEAD~1

# 撤销某个文件的更改
git checkout -- file.txt

# 修改最后一次提交
git commit --amend
```

## 协作规范

### 1. Pull Request 流程

```bash
# 1. 创建功能分支
git checkout -b feature/new-feature

# 2. 开发并提交
git add .
git commit -m "feat: implement new feature"

# 3. 推送到远程
git push origin feature/new-feature

# 4. 在GitHub/GitLab创建PR
# 5. 等待Code Review
# 6. 修改反馈
# 7. 合并PR
```

### 2. Code Review检查项

- [ ] 代码风格符合规范
- [ ] 功能实现正确
- [ ] 有适当的测试
- [ ] 没有安全隐患
- [ ] 性能可接受
- [ ] 文档已更新

### 3. PR描述模板

```markdown
## 变更类型
- [ ] 新功能
- [ ] Bug修复
- [ ] 重构
- [ ] 文档更新

## 变更说明
<!-- 描述本次PR的主要变更 -->

## 测试说明
<!-- 如何测试这些变更 -->

## 截图
<!-- 如果有UI变更，提供截图 -->

## 相关Issue
Closes #123
```

## 最佳实践

### 1. 提交粒度

✅ **好的提交：**
```bash
git commit -m "feat: add user registration form"
git commit -m "feat: add email validation"
git commit -m "feat: add password strength check"
```

❌ **不好的提交：**
```bash
git commit -m "add everything for user module"
```

### 2. 及时同步

```bash
# 每天开始工作前
git checkout main
git pull origin main

# 定期同步feature分支
git checkout feature/xxx
git rebase main
```

### 3. 使用.gitignore

```gitignore
# IDE
.idea/
.vscode/
*.iml

# 构建产物
target/
dist/
build/

# 依赖
node_modules/
vendor/

# 环境配置
.env
.env.local

# 日志
*.log

# 系统文件
.DS_Store
Thumbs.db
```

### 4. 使用Git Hooks

```bash
# .git/hooks/pre-commit
#!/bin/sh
npm run lint
npm run test
```

## 工具推荐

1. **GUI工具**
   - SourceTree
   - GitKraken
   - GitHub Desktop

2. **命令行增强**
   - Oh My Zsh (git插件)
   - tig
   - lazygit

3. **辅助工具**
   - commitizen (规范提交)
   - husky (Git Hooks)
   - conventional-changelog (生成changelog)

## 总结

好的Git工作流能够：

- ✅ 提高团队协作效率
- ✅ 降低代码冲突
- ✅ 便于版本管理
- ✅ 方便问题追溯

选择适合团队的工作流，坚持执行，持续改进！

---

你的团队使用什么工作流？欢迎评论交流！
