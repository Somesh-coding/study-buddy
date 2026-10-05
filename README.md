# Study Buddy // Retro AI

A local-first AI study companion built for the Hacktoberfest 2026 **Build for a Friend** challenge.

## What it does

- Import a PDF and extract its text.
- Write/edit study notes.
- Store notes, practice results, and study plans in the browser's `localStorage` — **no application database**.
- Ask Study Buddy questions grounded in your notes.
- Generate multiple-choice practice questions.
- Track practice accuracy locally.
- Generate a study plan and mark tasks complete locally.
- Retro CRT/pixel UI designed for a small, focused study workflow.

## Architecture

```text
React + Vite
    |
    | same-origin /api
    v
Spring Boot 3.5
    |
    +--> Apache PDFBox (PDF text extraction)
    |
    +--> lightweight in-memory context ranking (no vector DB)
    |
    +--> Hugging Face Inference Providers
             |
             +--> Qwen/Qwen2.5-7B-Instruct-1M (open-weight)
```

The browser owns the persistent study state. The backend does not persist notes or practice history.

## Run locally

### 1. Requirements

- Java 21
- Maven 3.9+
- Node.js 22+
- A Hugging Face token with **Inference Providers** permission for real AI calls.

Spring Boot 3.5 supports Java 17 through 25, so use Java 21 for this project rather than Java 26.

### 2. Start the backend

From `backend/`:

```bash
mvn spring-boot:run
```

Set your token first if using PowerShell:

```powershell
$env:HF_TOKEN="hf_your_token_here"
```

### 3. Start React

From `frontend/`:

```bash
npm install
npm run dev
```

Open `http://localhost:5173`.

### 4. Build one deployable JAR

```bash
cd frontend
npm install
npm run build
```

Copy the generated `frontend/dist` contents into `backend/src/main/resources/static/`, then:

```bash
cd ../backend
mvn clean package
java -jar target/study-buddy-1.0.0.jar
```

For deployment, use the included Dockerfile so this copy step happens automatically.

## Deploy to Render

1. Push this repository to GitHub.
2. Create a **New → Web Service** on Render.
3. Connect the repository.
4. Select **Docker** as the runtime.
5. Render will build the root `Dockerfile`.
6. Add the secret environment variable:
   - `HF_TOKEN` = your Hugging Face fine-grained token with Inference Providers permission.
7. Optional model variable:
   - `HF_MODEL=Qwen/Qwen2.5-7B-Instruct-1M:fastest`
8. Deploy.

The Docker image listens on Render's `PORT` value and the health check is `/api/health`.

## Important privacy design

There is no database. Notes and practice history are stored in browser `localStorage`.
PDF text is extracted in the backend only for the current request and is not written to disk/database.

The Hugging Face token is only used by Spring Boot and is never exposed to React.

## Hacktoberfest story

The project is built for a real friend who wants a simple way to study from their own material. The open approach matters because the app can use an open-weight model and keep the application architecture model-swappable instead of locking the project to one closed AI API.

For the DEV submission, document the friend's real problem, show the working demo, link the repository, explain the open model choice, and optionally embed a DevRelay agent session.
