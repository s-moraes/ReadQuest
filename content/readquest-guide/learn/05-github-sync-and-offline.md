# GitHub Sync & Offline Architecture

ReadQuest is engineered with an **offline-first** design philosophy. All learning content, manifests, image assets, and user progress live locally on your device, requiring an internet connection only when synchronizing updates from a remote repository.

---

## How Remote Sync Works

In the **Settings** screen, the **Sync Settings** card allows you to connect ReadQuest to any public or accessible GitHub repository containing valid course material.

```text
+-------------------------------------------------------------+
| Content Sync & Repository                                   |
+-------------------------------------------------------------+
| Repo URL: [ https://github.com/s-moraes/ReadQuest          ] |
| Status:   Last synced Today at 17:45                        |
|                                                             |
| [ Sync Content from Web ]                                   |
+-------------------------------------------------------------+
```

When you tap **Sync Content from Web**, the engine performs the following automated steps:

```text
[ User initiates Sync ]
           |
           v
[ GitHubUrlResolver ] ---> Normalizes tree/blob/raw URLs
           |
           v
[ Connects to main / master branch ]
           |
           v
[ Fetches content/catalog.json ] ---> Enumerates subjects
           |
           v
[ Downloads each subject.json & assets ]
           |
           v
[ Writes to local app storage ] ---> Atomic JSON commit
```

---

## Supported GitHub URL Formats

The built-in `GitHubUrlResolver` automatically resolves multiple URL patterns into raw CDN endpoints:

| Input URL Format | Resolution Strategy |
| :--- | :--- |
| `https://github.com/user/repo` | Resolves to `https://raw.githubusercontent.com/user/repo/main/` with fallback to `/master/`. |
| `https://github.com/user/repo/tree/v2.0` | Resolves to `https://raw.githubusercontent.com/user/repo/v2.0/`. |
| `https://raw.githubusercontent.com/...` | Used directly without alteration. |

---

## Offline Independence & Storage Safety

- **Atomic Storage:** Manifests and progress records are saved using atomic rename techniques (`AtomicJsonStore`). A power failure or abrupt app termination will never corrupt your study files.
- **Image Caching:** All diagrams referenced in `assets/` are pre-downloaded to your device storage, ensuring you can review system diagrams on flights or in offline environments.
- **Progress Preservation:** Syncing updated material preserves your existing completed topic IDs and quiz scores unless you explicitly choose to reset them in Settings.
