# Deploying a Team Study Hub on GitHub

In engineering organizations, onboarding new hires, standardizing cloud infrastructure patterns, or preparing teams for Kubernetes certifications requires structured study. ReadQuest allows companies and study groups to maintain a living, version-controlled syllabus on GitHub.

---

## Architecture Overview

By maintaining a dedicated GitHub repository (e.g., `github.com/my-org/backend-academy`), teams gain:

1. **Pull Request Peer Review:** Senior engineers review course updates and quiz additions using standard Git workflows.
2. **Zero Hosting Infrastructure:** GitHub’s raw content CDN (`raw.githubusercontent.com`) serves as a serverless content backend.
3. **Instant Mobile Distribution:** Team members paste the repository URL into ReadQuest Settings and hit Sync.

```text
[ Engineering Team ]
        |
        v (git push / PR merge)
[ GitHub Repository: my-org/backend-academy ]
        |
        v (Raw CDN)
[ ReadQuest Android Client ]
        |
        v (Offline Storage)
[ Engineer Studying Offline on Commute ]
```

---

## Step-by-Step Deployment Guide

### Step 1: Initialize the Directory
Create a new Git repository with a `content/` root directory:

```bash
mkdir -p backend-academy/content/k8s-mastery/{assets,learn,extra,quiz}
cd backend-academy
```

### Step 2: Publish `catalog.json`
Define your courses in `content/catalog.json`:

```json
{
  "version": 1,
  "subjects": [
    {
      "id": "k8s-mastery",
      "title": "Kubernetes in Production",
      "description": "Pods, Services, Ingress, and real-world failure modes.",
      "version": 1,
      "courseJsonPath": "content/k8s-mastery/subject.json"
    }
  ]
}
```

### Step 3: Configure Mobile Clients
Instruct your engineers to:
1. Open ReadQuest and go to **Settings**.
2. Paste `https://github.com/my-org/backend-academy` into **Repository URL**.
3. Tap **Sync Content from Web**.

Your custom training materials, interactive diagrams, and quizzes will immediately populate their mobile devices.
