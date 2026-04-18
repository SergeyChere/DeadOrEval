# Branching Strategy

## Branch Structure

```
main      — production ready, protected
review    — community reviewed, protected  
dev       — active development, open
```

## Flow

```
contributor fork → dev → review → main
```

## Rules

### `dev`
- Open for all contributors
- Fork the repository and open a PR against `dev`
- No approval required

### `review`
- Requires **2 approvals** from community members
- All conversations must be resolved
- Stale approvals dismissed on new commits
- Squash merge only

### `main`
- Requires **approval from @SergeyChere only**
- Requires review from Code Owners
- All conversations must be resolved
- Squash merge only
- Force push blocked

## How to Contribute

```bash
# 1. Fork the repository
# 2. Clone your fork
git clone https://github.com/YOUR_USERNAME/DeadOrEval.git

# 3. Create a branch from dev
git checkout dev
git checkout -b feat/your-feature

# 4. Make your changes

# 5. Push to your fork
git push origin feat/your-feature

# 6. Open a PR against dev
```

## Commit Convention

```
feat(module):     new feature
fix(module):      bug fix
docs(module):     documentation
refactor(module): code refactor
test(module):     tests
```