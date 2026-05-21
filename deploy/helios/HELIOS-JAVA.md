# Java на Helios (17) и запуск Lab 5

## Роли машин

| Машина | Java | Что запускает |
|--------|------|----------------|
| **Helios** | **17** | `server.jar` (UDP-сервер) + **Java-мост** `UdpTcpBridge` |
| **Arch** | **17 или 21** | `./lab5 build` (сборка) + **GUI** (JavaFX 21) |

`server.jar` собирается с **bytecode 17**. На Helios Gradle не нужен.

---

## Запуск (как при тестировании)

### 1. Arch — сборка и выкладка

```bash
cd /path/to/LAB_5
./scripts/deploy-helios.sh
```

### 2. Helios — UDP-сервер

```bash
cd ~/labuba8/LAB_5
./lab5 stop 5555
./lab5 server-bg s456129 5555
./lab5 status
tail -f deploy/helios/logs/server.log
```

### 3. Arch — туннель + GUI

```bash
export LAB5_REMOTE_DEPLOY='~/labuba8/LAB_5/deploy/helios'
export SSH_PORT=2222
./lab5 remote-gui helios s456129 5555 5555
```

Туннель (внутри `remote-gui`):

1. Helios: `./run-udp-bridge-bg.sh` → Java `tcp-lsn` :15555
2. Arch: `ssh -N -L 15555:127.0.0.1:15555`
3. Arch: Java `udp-lsn` 127.0.0.1:5555 ↔ :15555

---

## Сборка на Arch (если Java 26 по умолчанию)

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./lab5 build
```

---

## Проверка

```bash
# Helios
tail -f deploy/helios/logs/server.log    # Server started on port 5555
tail -f deploy/helios/logs/bridge.log    # TCP connected (tunnel)

# Arch
tail -f logs/bridge.log                  # TCP connected to 127.0.0.1:15555
```
