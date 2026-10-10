// Vite 개발 서버와 Electron을 함께 실행하는 개발용 러너이다.
// 추가 npm 의존성 없이 Node.js 내장 모듈만 사용한다.
import { spawn } from 'node:child_process';
import { createRequire } from 'node:module';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const require = createRequire(import.meta.url);
const rootDir = path.dirname(fileURLToPath(new URL('../package.json', import.meta.url)));

const VITE_PORT = 5173;
const DEV_SERVER_URL = `http://localhost:${VITE_PORT}`;
const ELECTRON_ENTRY = path.join(rootDir, 'dist-electron', 'main.js');

const children = new Set();

function run(command, args, options = {}) {
  const child = spawn(command, args, {
    cwd: rootDir,
    stdio: 'inherit',
    shell: false,
    ...options,
  });
  children.add(child);
  child.on('exit', () => {
    children.delete(child);
  });
  return child;
}

function runNode(scriptArgs, env = process.env) {
  return run(process.execPath, scriptArgs, { env });
}

function waitForDevServer(url, retries = 60, intervalMs = 500) {
  return new Promise((resolve, reject) => {
    let attempt = 0;
    const timer = setInterval(async () => {
      attempt += 1;
      try {
        const res = await fetch(url);
        if (res.ok || res.status < 500) {
          clearInterval(timer);
          resolve();
          return;
        }
      } catch {
        // 서버가 아직 준비되지 않음. 다음 시도에서 재확인한다.
      }
      if (attempt >= retries) {
        clearInterval(timer);
        reject(new Error(`Vite 개발 서버(${url})가 제한 시간 안에 준비되지 않았습니다.`));
      }
    }, intervalMs);
  });
}

function runCommand(command, args) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, { cwd: rootDir, stdio: 'inherit', shell: false });
    child.on('exit', (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`명령 실패: ${command} ${args.join(' ')} (exit code ${code})`));
      }
    });
    child.on('error', reject);
  });
}

function shutdown(exitCode = 0) {
  for (const child of children) {
    if (!child.killed) {
      child.kill();
    }
  }
  process.exit(exitCode);
}

process.on('SIGINT', () => shutdown(0));
process.on('SIGTERM', () => shutdown(0));

const viteBin = path.join(rootDir, 'node_modules', 'vite', 'bin', 'vite.js');
const tscBin = path.join(rootDir, 'node_modules', 'typescript', 'bin', 'tsc');
const electronCli = require.resolve('electron/cli.js');

const vite = runNode([viteBin, '--port', String(VITE_PORT), '--strictPort']);

try {
  await waitForDevServer(DEV_SERVER_URL);
} catch (error) {
  console.error(error instanceof Error ? error.message : error);
  vite.kill();
  process.exit(1);
}

try {
  await runCommand(process.execPath, [tscBin, '-p', 'tsconfig.electron.json']);
} catch (error) {
  console.error(error instanceof Error ? error.message : error);
  vite.kill();
  process.exit(1);
}

const electron = runNode([electronCli, ELECTRON_ENTRY], {
  ...process.env,
  VITE_DEV_SERVER_URL: DEV_SERVER_URL,
});

electron.on('exit', (code) => {
  vite.kill();
  process.exit(code ?? 0);
});
