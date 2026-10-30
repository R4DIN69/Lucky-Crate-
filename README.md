# LuckyCrates - Paper 1.21+ Roulette Spinner Plugin

A high-performance, CS:GO-style crate opening and roulette spinner plugin built natively for Paper/Purpur 1.21.

---

## 🌟 Key Features

1. **CS:GO Roulette Spinner Animation**:
   - 3-row GUI with custom pointer glass panes (Orange pointer above and below the winning slot).
   - Dynamic realistic deceleration curve (starts at high speed, smoothly slows down across 38 shifts).
   - Audio feedback with pitch changes on each shift (`BLOCK_NOTE_BLOCK_PLING`), and celebratory win fanfare (`UI_TOAST_CHALLENGE_COMPLETE` / fireworks / title screen).
   - Fail-safe prize delivery: Even if a player closes their inventory during the spin, the pre-determined winning prize is safely deposited directly into their inventory or dropped at their feet if full.

2. **In-Game Reward Customizer (Creative Mode Left-Click)**:
   - When in **Creative Mode**, simply **left-click** any registered Lucky Crate block.
   - An interactive 54-slot Editor GUI opens.
   - Drag & drop any items into the GUI (including custom enchants, custom names, potions, enchanted books, player heads, etc.).
   - Simply close the GUI and the plugin automatically saves all rewards to `crates.yml`!

3. **Secure PersistentDataContainer Lucky Keys**:
   - Keys use Paper's native `PersistentDataContainer` (PDC) tagging.
   - Players cannot exploit or forge keys on an anvil by renaming regular tripwire hooks.
   - Custom glowing lore, configurable material, and customizable display name.

4. **Commands**:
   - `/place crate` - Target any block within 5 blocks to register it as a Lucky Crate block.
   - `/give <player> luckykey <amount>` - Direct key-giving command (also available via `/luckykey give <player> <amount>` or `/crate key <player> <amount>`).
   - `/crate reload` - Reload configuration and crate files without restarting.
   - `/crate list` - List all placed crate coordinates.

---

## 📦 How to Build the Maven Jar File

### Requirements
- **Java Development Kit (JDK) 21** or higher.
- **Apache Maven 3.8+** (or build inside IntelliJ IDEA / Eclipse / VS Code).

### Building with Command Line
Open a terminal in the project directory (where `pom.xml` is located) and run:
```bash
mvn clean package
```
Once the build completes, the compiled `.jar` file will be located in the `target/` directory:
```
target/LuckyCrates-1.0.0.jar
```

### Building in IntelliJ IDEA
1. Open IntelliJ IDEA and click **Open**.
2. Select the folder containing `pom.xml`.
3. Open the **Maven** tool window on the right side.
4. Expand `LuckyCrates` -> `Lifecycle` -> double-click `package`.
5. Your plugin `.jar` is created in the `target/` folder!

---

## 🚀 Installation & Setup on Your Server

1. Drop the compiled `LuckyCrates-1.0.0.jar` into your server's `plugins/` folder.
2. Restart or start your Paper 1.21+ server.
3. In-game as an Operator / admin:
   - Place a block (such as an Ender Chest, Chest, or custom block).
   - Look at the block and type:
     ```
     /place crate
     ```
   - Switch to Creative Mode (`/gamemode creative`) and **left-click** the crate to open the customizer. Add your desired prize items and close the GUI!
   - Give yourself a Lucky Key:
     ```
     /give <your_name> luckykey 5
     ```
   - Switch to Survival Mode (`/gamemode survival`) and **right-click** the crate to watch the spinner animation!

---

## 📋 Commands & Permissions

| Command | Description | Permission | Default |
| :--- | :--- | :--- | :--- |
| `/place crate` | Sets your targeted block as a Lucky Crate | `luckycrates.admin` | `op` |
| `/give <player> luckykey <amount>` | Gives Lucky Keys to a player | `luckycrates.admin` | `op` |
| `/luckykey give <player> [amount]` | Alias to give Lucky Keys | `luckycrates.admin` | `op` |
| `/crate key <player> [amount]` | Alias to give Lucky Keys | `luckycrates.admin` | `op` |
| `/crate reload` | Reloads `config.yml` and `crates.yml` | `luckycrates.admin` | `op` |
| `/crate list` | Lists all active Lucky Crate locations | `luckycrates.admin` | `op` |
| *Right-Click Crate* | Opens roulette spinner with key | `luckycrates.use` | `true` (all players) |
| *Left-Click Crate (Creative)* | Opens in-game rewards customizer | `luckycrates.admin` | `op` |
