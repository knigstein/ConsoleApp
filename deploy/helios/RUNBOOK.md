# Lab 5 — запуск и остановка (Helios + Arch, UDP-туннель)

**Java на Helios = 17** (сборка на Arch, bytecode 17). Подробно: [HELIOS-JAVA.md](HELIOS-JAVA.md)

Пути по умолчанию:
- **Helios:** `~/labuba8/LAB_5`
- **Arch:** `/home/forallxyz/ITMO_ED/Java/LAB_5` (или ваш клон)
- **SSH:** `Host helios` → `helios.se.ifmo.ru:2222`, пользователь `s456129`
- **БД:** логин PostgreSQL = `s456129`, порт UDP = `5555`

---

## A. Полная остановка (с нуля перед новым запуском)

### На Helios

```bash
ssh helios
cd ~/labuba8/LAB_5

./lab5 stop 5555 2>/dev/null || true
# (останавливает ServerMain и освобождает UDP 5555)

sockstat -4 -u -l | grep 5555
ps aux | grep -E 'ServerMain|run-udp-bridge|UdpTcpBridge' | grep -v grep
# если порт всё ещё занят: kill <PID> из sockstat

rm -f deploy/helios/logs/server.pid
```

### На Arch

```bash
cd /home/forallxyz/ITMO_ED/Java/LAB_5

./scripts/udp-tunnel-helios.sh stop 2>/dev/null || true

pkill -f 'socat.*5555' 2>/dev/null || true
pkill -f 'socat.*15555' 2>/dev/null || true
pkill -f 'UdpTcpBridge' 2>/dev/null || true
pkill -f 'GuiClientApp' 2>/dev/null || true
```

---

## B. Запуск (рабочий сценарий)

### 1. Helios — только UDP-сервер

```bash
ssh helios
cd ~/labuba8/LAB_5

./lab5 check server
./lab5 server-bg s456129 5555

./lab5 status
tail -5 deploy/helios/logs/server.log
# ожидается: Server started on port 5555
```

Сессию SSH можно закрыть — сервер в фоне.

### 2. Arch — пересборка и выкладка `server.jar` (после правок моста/клиента)

```bash
cd /home/forallxyz/ITMO_ED/Java/LAB_5
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./lab5 build

scp -P 2222 deploy/helios/dist/server.jar helios:~/labuba8/LAB_5/deploy/helios/dist/
```

На Helios перезапустите мост (если туннель уже был): `pkill -f UdpTcpBridge; ./deploy/helios/run-udp-bridge-bg.sh`

### 3. Arch — туннель + GUI

```bash
cd /home/forallxyz/ITMO_ED/Java/LAB_5

export LAB5_REMOTE_DEPLOY='~/labuba8/LAB_5/deploy/helios'
export SSH_PORT=2222

# нужен вход по SSH-ключу (BatchMode), иначе remote-gui не поднимет туннель:
ssh -o BatchMode=yes helios 'echo OK'

./lab5 remote-gui helios s456129 5555 5555
```

При входе на Helios в `deploy/helios/logs/server.log` должна появиться строка `Request req-...` — если её нет, запрос не дошёл до UDP-сервера (мост/SSH).

При закрытии окна GUI туннель останавливается автоматически.

### 4. Ручной туннель (если remote-gui не работает)

**Helios:**

```bash
cd ~/labuba8/LAB_5/deploy/helios
./run-udp-bridge-bg.sh 15555 5555
```

**Arch — терминал A (SSH, держать открытым):**

```bash
ssh -p 2222 -N -L 15555:127.0.0.1:15555 -o ExitOnForwardFailure=yes helios
```

**Arch — терминал B (локальный Java-мост + GUI):**

```bash
cd /home/forallxyz/ITMO_ED/Java/LAB_5
java -cp deploy/helios/dist/server.jar server.UdpTcpBridge udp-lsn 127.0.0.1 5555 127.0.0.1 15555 &
./lab5 mock 127.0.0.1 5555
```

---

## C. Корректное завершение работы

| Что остановить | Где | Команда |
|----------------|-----|---------|
| GUI | Arch | закрыть окно или `pkill -f GuiClientApp` |
| UDP-туннель | Arch | `Ctrl+C` в терминале remote-gui / ручного ssh, или `./scripts/udp-tunnel-helios.sh stop` |
| Сервер | Helios | `cd ~/labuba8/LAB_5 && ./lab5 stop` |

Порядок: **сначала Arch (GUI + туннель), потом Helios (сервер)**.

---

## D. Проверка и диагностика

```bash
# Helios — сервер жив?
ssh helios 'cd ~/labuba8/LAB_5 && ./lab5 status'
ssh helios 'tail -20 ~/labuba8/LAB_5/deploy/helios/logs/server.log'

# Arch — туннель?
./scripts/udp-tunnel-helios.sh status

# Helios — порт занят?
ssh helios 'sockstat -4 -l | grep 5555'
```

---

## E. Сборка и архив (только на Arch, при изменении кода)

```bash
cd /home/forallxyz/ITMO_ED/Java/LAB_5
./scripts/package-helios-prebuilt.sh
scp -P 2222 ../LAB_5-helios-*.tar.gz helios:~/
```

На Helios после нового архива:

```bash
cd ~
tar -xzf LAB_5-helios-*.tar.gz   # или распаковать в labuba8
cd ~/labuba8/LAB_5
chmod +x lab5 deploy/helios/*.sh scripts/*.sh
```

---

## F. Сообщение «сервер недоступен после 3 попыток»

Частая причина при `remote-gui`: многопакетный ответ (`show`, `add`) через туннель.
Нужны **свежие** `server.jar` / `gui-client.jar` с length-framing в `UdpTcpBridge` и увеличенными таймаутами клиента.

Локально пересобрать и обновить jar на Helios:

```bash
cd /home/forallxyz/ITMO_ED/Java/LAB_5
gradle distHelios
scp -P 2222 deploy/helios/dist/server.jar helios:~/labuba8/LAB_5/deploy/helios/dist/
```

На Arch для GUI — тот же `distHelios` (локальный `gui-client.jar` + `server.jar` для моста).

Туннель должен писать: `local: java-udp-lsn`, `remote: java-tcp-lsn` (не socat + java).

После сбоя: **полная остановка (A) → запуск (B)**.
