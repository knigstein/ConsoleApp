# Lab 5 Deployment Guide (Helios + local GUI)

## Target topology

- Helios runs PostgreSQL-backed UDP server.
- Helios can run console client for diagnostics.
- **Recommended:** JavaFX GUI also runs on Helios with `ssh -Y`, talking to `localhost` (avoids blocked inbound UDP from the internet).
- Optional: run GUI on a laptop and point it at Helios (often fails if UDP is filtered).

## Requirements

- Java 17+ (`java -version`)
- Bash
- `unzip` (needed only when wrapper fallback is used)
- Optional system `gradle` (preferred on Helios to avoid wrapper downloads)

## Important upload rule

Deploy scripts resolve paths relative to repository root. Upload the whole project tree, not only `deploy/helios/*`.

```bash
scp -r LAB_5 <login>@helios:~/
```

Or archive the full project and extract to any directory that keeps the original root structure.

## Build (recommended: on Arch/Linux, not on Helios)

**Helios has Java 17 only** — run prebuilt JARs. Full Gradle build needs OpenJFX and is unreliable on FreeBSD.

On **Arch** (from repository root):

```bash
./lab5 build
# or: ./scripts/deploy-helios.sh   # build + scp server.jar to Helios
```

On **Helios** — verify artifacts only (after upload or `deploy-helios.sh`):

```bash
cd ~/LAB_5/deploy/helios
chmod +x build.sh run-server.sh run-server-bg.sh run-client.sh helios-stack.sh init-db.sh
./build.sh verify
# or server-only: ./build.sh server
```

If `dist/server*.jar` is already present, `./build.sh` skips Gradle automatically on FreeBSD.

Do **not** expect `./build.sh` to compile from sources on Helios unless you set `LAB5_FORCE_HELIOS_BUILD=1` (usually fails on JavaFX).

## Start server on Helios

```bash
cd ~/LAB_5/deploy/helios
./run-server.sh <db_login> 5555
```

- DB URL by default is `jdbc:postgresql://pg:5432/studs`.
- You can override DB endpoint via env vars before launch:
  - `LAB5_DB_HOST`
  - `LAB5_DB_PORT`
  - `LAB5_DB_NAME`
- `db_login` is required.
- Port defaults to `5555` if omitted.

If this is the first deployment (or you need full schema reset), initialize DB manually:

```bash
cd ~/LAB_5/deploy/helios
chmod +x init-db.sh
./init-db.sh <db_login>
```

## Start console client on Helios (optional)

```bash
cd ~/LAB_5/deploy/helios
./run-client.sh localhost 5555
```

This client is intended for quick diagnostics only.

## Canonical Helios: server + GUI on the same host (recommended)

UDP from the public internet to Helios is often blocked; run the JavaFX GUI **on Helios** and forward the display (X11):

```bash
ssh -Y <login>@helios.se.ifmo.ru
cd ~/LAB_5/deploy/helios
./helios-stack.sh all <db_login> 5555
```

This starts the server in the background, then the GUI connecting to `localhost`. Floor images are shipped under `deploy/helios/Corpus/` by `distHelios`.

- Stop background server: `./helios-stack.sh stop`
- Status: `./helios-stack.sh status`

## GUI from your laptop (optional; UDP may be blocked)

From project root (Gradle launcher):

```bash
./run-gui-client.sh real <helios_host> 5555
```

Mock UI without server:

```bash
./run-gui-client.sh mock
```

From deploy directory (JavaFX module-path):

```bash
cd ~/LAB_5/deploy/helios
./run-gui-client.sh real <helios_host> 5555
```

## End-to-end verification checklist

1. Register user in GUI.
2. Login in GUI.
3. Add/update/remove object in GUI.
4. Verify object appears/disappears in map and table.
5. Re-open collection (`show` in console client or wait polling) and ensure state is consistent.

## Notes

- GUI is expected to run on local Linux/Windows/macOS. Running JavaFX on Helios (FreeBSD) is not a required path.
- `execute_script` in GUI input is intentionally rejected by server command handler.
- Server does not support XML `save` flow in DB mode.

## Troubleshooting

- `Project directory is not part of the build`:
  run scripts from `~/LAB_5/deploy/helios` and keep repository layout intact.
- `curl SSL_connect` during wrapper download:
  install/use system `gradle` on Helios.
- `dist/server.jar not found`:
  run `./build.sh` first.
- `Address already in use`:
  start server with another port, e.g. `./run-server.sh <db_login> 5556`.
