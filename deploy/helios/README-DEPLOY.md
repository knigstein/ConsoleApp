# Lab 5 Deployment Guide for FreeBSD (helios)

## Requirements

- **Java 17+** — check with `java -version`
  - On FreeBSD: `pkg install openjdk17` or `openjdk21`
- **Bash** — usually pre-installed on FreeBSD

## Quick Start

### 1. Upload to helios

```bash
scp -r deploy/helios/* your_login@helios:~/lab5/
```

Or upload the archive:

```bash
scp lab5-deploy-helios.tar.gz your_login@helios:~/
ssh your_login@helios
mkdir -p lab5 && cd lab5
tar xzf ~/lab5-deploy-helios.tar.gz
```

### 2. Build on helios

```bash
cd ~/lab5
chmod +x build.sh run-server.sh run-client.sh
./build.sh
```

### 3. Run the Server

```bash
./run-server.sh data.xml 5555
```

The server will:
- Load the collection from `data.xml`
- Listen on UDP port `5555`
- Accept client connections
- Local commands: type `save` or `exit` in the server console

### 4. Run the Client (another terminal)

```bash
./run-client.sh localhost 5555
```

Or from another machine:

```bash
./run-client.sh <helios_hostname> 5555
```

## Available Client Commands

| Command | Description |
|---------|-------------|
| `help` | Show available commands |
| `info` | Show collection info |
| `show` | Display all elements |
| `add` | Add new element |
| `update <id>` | Update element by ID |
| `remove_by_id <id>` | Remove element by ID |
| `remove_first` | Remove first element |
| `clear` | Clear collection |
| `add_if_min` | Add if smaller than smallest |
| `remove_lower` | Remove all smaller than given |
| `filter_contains_name <substring>` | Filter by name |
| `filter_greater_than_semester_enum <SEMESTER>` | Filter by semester |
| `print_field_descending_group_admin` | Print group admins descending |
| `execute_script <file>` | Run script file |
| `exit` | Exit client |

## Server-Only Commands

- `save` — save collection to XML (type in server console)
- `exit` — shutdown server (saves collection first)

## Notes for FreeBSD

- UDP ports > 1024 don't require root
- If firewall is enabled: check with `sudo pfctl -s rules`
- Java home on FreeBSD: `/usr/local/openjdk17/`

## Troubleshooting

**"Unable to access jarfile"** — run `./build.sh` first

**"Address already in use"** — another process uses the port, try different port:
```bash
./run-server.sh data.xml 5556
```

**"Command not found: java"** — install OpenJDK:
```bash
pkg install openjdk17
```
