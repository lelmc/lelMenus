# LelMenus - lel Chest Menu Plugin

A chest menu plugin based on SpongeAPI 12, inspired by DeluxeMenus functionality and format, with rich placeholder system and custom NBT data support.

---

## 🌏 Language / 语言

- **[🇨🇳 中文版 README](README-zh_CN.md)** - 中文说明文档
- **[🇬🇧 English README](README.md)** - You are here

---

## 📋 Table of Contents

- [Commands](#commands)
- [Features](#features)
- [Configuration Format](#configuration-format)
- [Supported NBT Formats](#supported-nbt-formats)
- [Supported MiniPlaceholders](#supported-miniplaceholders)

---

## Commands

### `/menu [menu_name]`
- **Permission**: `lelmenu.use`
- **Description**: Open a specific menu
- **Usage**: 
  - `/menu` - Open main menu
  - `/menu shop` - Open shop menu

### `/menureload`
- **Permission**: `lelmenu.reload`
- **Description**: Reload all menu configurations and refresh command mappings
- **Usage**: `/menureload`

### Dynamic Menu Commands
- **Permission**: `lelmenu.use`
- **Description**: Shortcut commands automatically registered from `open_command` in configuration files
- **Examples**: 
  - Set `open_command = "cd"` in config → Use `/cd` to open corresponding menu
  - Set `open_command = "shop"` in config → Use `/shop` to open corresponding menu

---

## Features

### ✨ Core Features
- **Dynamic Command Registration**: Automatically register menu opening commands based on configuration
- **Hot Reload Support**: Update configurations without restarting server using `/menureload`
- **Rich Placeholder System**: Support for player info, server status, economy systems, etc.
- **NBT Data Support**: Multiple NBT data types and complex structures
- **Item Requirement Detection**: Check player item quantity, type, etc.
- **Permission Control**: Each menu item can have independent permission requirements

### 🔧 Advanced Features
- **Multi-Menu Support**: Create multiple different menu files
- **Conditional Display**: Dynamically show/hide menu items based on player state
- **Click Events**: Support different commands for left-click and right-click
- **Item NBT**: Custom NBT data for items
- **MiniPlaceholders Integration**: Support for economy system placeholders

---

## Configuration Format

### Basic Configuration

```hocon
# Menu rows (1-6)
rows = 3

# Command to open this menu (optional, delete if not needed)
open_command = "main"

# Menu update interval (seconds), will update slots with "update=true" (optional, delete if not needed)
update_interval = 5

# Menu title (supports color codes and placeholders)
menu_title = "&c&lLel&7-> &dService Menu"

# Supports multiple conditions, below is a permission check example
requirements {
  "check_permission" { #This can be named arbitrarily
    type = "condition type"            # "permission"
    input = "input value (supports placeholders)"  # "lelmc.user"
    output = "expected value"  # Expected true/false. If true, players with this permission can open. If false, players without this permission can open.
  }
}
```

### Item Configuration

```hocon
items {
  # Single slot configuration
  "10" {
    # Item material (supports Minecraft IDs)
    material = "minecraft:beacon"
    
    # Display name (supports color codes and placeholders)
    display_name = "&cMain City"
    
    # Item lore
    lore = [
      "&9Staff skill learning",
      "&7Landmark location /warp zc"
    ]
    
    # Slot number
    slot = 10
    
    # Item count (optional, delete if not needed)
    count = 1
    
    # Enchantment list (format: "enchantment_id;level") (optional)
    enchantments = [
      "minecraft:binding_curse;1"
    ]
    
    # Hide enchantments (optional)
    hide_enchantments = true
    
    # Left click commands (optional)
    left_click_commands = [
      "[close]",
      "[player] warp zc"
    ]
    
    # Right click commands (optional)
    right_click_commands = [
      "[console] say Hello"
    ]
    
    shift_left_click_commands=[
      "[close]",
      "[message] shift_left_click_commands"
    ]
    
    shift_right_click_commands=[
      "[close]",
      "[message] shift_right_click_commands"
    ]
    
    middle_click_requirement=[
      "[close]",
      "[message] middle_click_requirement"
    ]
    
    # View requirements (only shows when conditions met) (optional)
    requirements {
      "check_level" {
        type = ">="
        input = "%player_level%"
        output = "10"
      }
    }
    
    # Priority (higher number = higher priority) (optional)
    priority = 1
    
    # NBT data (see NBT format section below) (optional)
    nbt_string {
      "CustomModelData" = "12345"
    }
    # This slot will be updated
    update=true
  }
  
  # Multiple slots configuration (borders, etc.)
  "border" {
    material = "yellow_stained_glass_pane"
    display_name = "&7Decoration"
    slots = [0, 8, 17, 26]  # Multiple slots
    priority = 0
  }
}
```

### Special Command Formats

- `[close]` - Close menu
- `[refresh]` - Refresh the current menu
- `[delay;50]` - Delay 50 ticks before executing the commands that follow
- `[player] command` - Execute command as player
- `[console] command` - Execute command as console
- `[message] message` - Send message to player

---

## Requirements - View Requirement System

Requirements control menu item display conditions. Items only show when all conditions are met.

### Configuration Format

```hocon
# Basic format
requirements {
  "condition_name" {
    type = "condition_type"
    input = "input_value (supports placeholders)"
    output = "expected_value"
  }
}

# Complete example
items {
  "10" {
    material = "beacon"
    display_name = "&cMain City"
    slot = 10
    
    # Add requirements directly in item configuration
    requirements {
      "level_check" {
        type = ">="
        input = "<player_level>"
        output = "10"
      }
    }
  }
}
```

### Supported Comparison Types

##### Permission Check
| 类型           | 说明   | 示例                                   |
|--------------|------|--------------------------------------|
| `permission` | Check permission | `input="lelmenu.user" , output=true` |

#### Numeric Comparisons

| Type | Description       | Example |
|-----|-------------------|---------|
| `>` | Greater than      | `input="<player_level>", output="10"` |
| `>=` | Greater or equals | `input="<player_exp_total>", output="100"` |
| `<` | Less than         | `input="<player_health>", output="5"` |
| `<=` | Less or equals    | `input="<player_food>", output="20"` |
| `==` | Equals            | `input="<player_level>", output="50"` |

#### String Comparisons

| Type | Description | Example |
|------|-------------|---------|
| `equals` | String equals | `input="<player_world>", output="world"` |
| `!=`          | String does not contain | `input="<player_name>", output="admin"`  |
| `contains` | String contains | `input="<player_name>", output="admin"` |
| `starts with` | String starts with | `input="<player_uuid>", output="abc"` |
| `ends with` | String ends with | `input="<player_uuid>", output="123"` |

### Configuration Examples

#### Level Requirement
```hocon
items {
  "10" {
    material = "beacon"
    # Add requirements directly in item configuration
    requirements {
      "level_check" {
        type = ">="
        input = "<player_level>"
        output = "10"
      }
    }
  }
}
```
Only shows when player level >= 10

#### Health Requirement
```hocon
items {
  "10" {
    material = "diamond_sword"
    requirements {
      "health_check" {
        type = ">"
        input = "<player_health>"
        output = "5"
      }
    }
  }
}
```
Only shows when player health > 5

#### World Restriction
```hocon
items {
  "10" {
    material = "grass_block"
    requirements {
      "world_check" {
        type = "equals"
        input = "<player_world>"
        output = "world"
      }
    }
  }
}
```
Only shows when in overworld

#### Multiple Conditions
```hocon
items {
  "10" {
    material = "beacon"
    requirements {
      "level_check" {
        type = ">="
        input = "<player_level>"
        output = "10"
      }
      "health_check" {
        type = "number >"
        input = "<player_health>"
        output = "5"
      }
    }
  }
}
```
Must satisfy level >= 10 **AND** health > 5 to show

#### Right Click Requirement

Same configuration method, add to command configuration:

```hocon
items {
  "10" {
    material = "beacon"
    left_click_commands = ["[player] cmd"]
    right_click_commands = ["[player] admin_cmd"]
    
    # Right click also needs to meet conditions
    requirements {
      "permission_check" {
        type = "equals"
        input = "<player_has_perm_lelmenu_admin>"
        output = "true"
      }
    }
  }
}
```

**Note**: Each item can only have one set of `requirements`, which affects both display and click behavior.

### Important Notes

1. **Placeholder Parsing**: Both `input` and `output` support MiniPlaceholders placeholders
2. **Multiple Conditions**: All conditions must be satisfied (AND logic)
3. **Error Handling**: If condition configuration is incorrect, item shows by default
4. **Performance Optimization**: Complex condition checks may have slight performance impact

---

## Supported NBT Formats

### 1. Simple NBT Types

#### nbt_string (String Type)
```hocon
nbt_string {
  "CustomModelData" = "12345"
  "display_name" = "Divine Weapon"
}
```

#### nbt_int (Integer Type)
```hocon
nbt_int {
  "Damage" = 0
  "RepairCost" = 1
}
```

#### nbt_double (Double Type)
```hocon
nbt_double {
  "explosion_power" = 1.5
}
```

#### nbt_float (Float Type)
```hocon
nbt_float {
  "speed" = 0.5f
}
```

### 2. Complex NBT Structures (nbt_form)

Supports complete HOCON format for defining complex nested structures:

```hocon
nbt_form {
  # Component format (recommended for Minecraft 1.20.5+)
  components {
    "minecraft:custom_data" {
      "lelmenu": {
        "id" = "special_item"
        "power" = 100
        "skills" = [
          "fireball",
          "lightning"
        ]
      }
    }
    "minecraft:enchantments" {
      levels {
        "minecraft:sharpness" = 5
        "minecraft:fire_aspect" = 2
      }
    }
    "minecraft:attribute_modifiers" {
      modifiers = [
        {
          type = "minecraft:attack_damage"
          id = "base_attack"
          amount = 10.0
          operation = "add_value"
        }
      ]
    }
  }
  
  # Traditional NBT tag format
  tag {
    # Simple values
    "Damage" = 0
    "Unbreakable" = true
    
    # Lists
    "ench" = [
      {
        id = 16s
        lvl = 5s
      },
      {
        id = 20s
        lvl = 2s
      }
    ]
    
    # Nested structures
    "EntityTag" {
      "id" = "minecraft:armor_stand"
      "CustomName" = "{\"text\":\"Guardian\"}"
      "Invulnerable" = true
    }
    
    # Arrays
    "SkullOwner" {
      Id = [I; 12345678, 12345678, 12345678, 12345678]
      Properties {
        textures = [
          {
            Value = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv...\"}}}"
          }
        ]
      }
    }
  }
}
```

### 3. NBT Data Notes

- **Data Type Suffixes**:
  - `s` = short
  - `b` = byte
  - `l` = long
  - `f` = float
  - `d` = double
  - `B` = byte array
  - `I` = int array
  - `L` = long array

- **Placeholder Support**: NBT string values can use placeholders
- **View Synchronization**: NBT data is automatically applied when item is created

---

## Supported MiniPlaceholders

### 👤 Player Placeholders

#### Basic Info
- `<player_uuid>` - Player UUID
- `<player_name>` - Player name
- `<player_prefix>` - Player prefix
- `<player_suffix>` - Player suffix

#### Location and Direction
- `<player_world>` - Current world name
- `<player_x>` - X coordinate
- `<player_y>` - Y coordinate
- `<player_z>` - Z coordinate
- `<player_direction>` - Facing direction (South/Southwest/West/Northwest/North/Northeast/East/Southeast)

#### Status Info
- `<player_health>` - Health
- `<player_max_health>` - Max health
- `<player_food>` - Food level
- `<player_saturation>` - Saturation
- `<player_can_fly>` - Can fly
- `<player_flying>` - Is flying
- `<player_fly_speed>` - Fly speed
- `<player_walk_speed>` - Walk speed

#### Game Info
- `<player_gamemode>` - Game mode
- `<player_level>` - Experience level
- `<player_exp_total>` - Total experience
- `<player_exp>` - Current level experience
- `<player_exp_to_next>` - Experience needed for next level
- `<player_ping>` - Network ping
- `<player_language>` - Client language

#### Air Values
- `<player_max_air>` - Max air
- `<player_remaining_air>` - Remaining air

#### Held Items
- `<player_hand_main>` - Main hand item name
- `<player_hand_off>` - Off hand item name

#### Time Statistics
- `<player_time_seconds>` - Play time (seconds)
- `<player_time_minutes>` - Play time (minutes)
- `<player_time_hours>` - Play time (hours)
- `<player_time_days>` - Play time (days)
- `<player_time_played>` - Play time (formatted, e.g., "1 d 2 h 30 m")
- `<player_first_join>` - First join time

### 🖥️ Server Placeholders

#### Basic Info
- `<server_online>` - Online player count
- `<server_max_players>` - Max player count
- `<server_unique_players>` - Unique player count
- `<server_motd>` - Server MOTD

#### Performance Info
- `<server_tps>` - Ticks per second (TPS)
- `<server_ram_used>` - Used memory (MB)
- `<server_ram_free>` - Free memory (MB)
- `<server_ram_total>` - Total memory (MB)
- `<server_ram_max>` - Max memory (MB)
- `<server_cores>` - CPU cores

#### Uptime
- `<server_uptime>` - Uptime percentage
- `<server_uptime_total>` - Total uptime
- `<server_time_world>` - World timestamp

### 💰 Economy Placeholders

#### Personal Economy
- `<economy_balance>` - Player balance (raw value)
- `<economy_balance_formatted>` - Formatted balance (with currency symbol)
- `<economy_currency_name>` - Currency name
- `<economy_currency_symbol>` - Currency symbol

#### Wealth Leaderboard
- `<economy_baltop_1>` ~ `<economy_baltop_5>` - Top 5 richest players names
- `<economy_baltop_1_balance>` ~ `<economy_baltop_5_balance>` - Top 5 richest players balances

### 📊 Other Placeholders

#### Global Placeholders
- `<server_hello>` - Hello (test)
- `<server_server_name>` - Server name (custom)

---

## Configuration Example

### Complete Main Menu Configuration

```hocon
rows = 3
open_command = "main"
register_command = true
update_interval = 5
menu_title = "&c&lLel&7-> &dService Menu"
sweet = true

items {
  # Background
  "background" {
    material = "white_stained_glass_pane"
    display_name = " "
    slots = [
      0, 1, 2, 3, 4, 5, 6, 7, 8,
      9, 10, 11, 12, 13, 14, 15, 16, 17,
      18, 19, 20, 21, 22, 23, 24, 25, 26
    ]
    priority = 0
  }
  
  # Main City Entry
  "spawn" {
    material = "beacon"
    display_name = "&c&lMain City"
    lore = [
      "",
      "&9● Staff skill learning",
      "&7● Landmark location /warp zc",
      "",
      "&eClick to teleport"
    ]
    slot = 11
    enchantments = ["minecraft:binding_curse;1"]
    hide_enchantments = true
    left_click_commands = [
      "[close]",
      "[player] warp zc"
    ]
    priority = 2
  }
  
  # Shop
  "shop" {
    material = "emerald"
    display_name = "&a&lShop"
    lore = [
      "",
      "&7Click to enter shop",
      "&7Buy various items",
      "",
      "&eClick to open"
    ]
    slot = 13
    left_click_commands = [
      "[close]",
      "[player] shop gui"
    ]
    priority = 2
  }
  
  # Personal Info
  "info" {
    material = "book"
    display_name = "&b&lPersonal Info"
    lore = [
      "",
      "&7Your name: <player_name>",
      "&7Your health: <player_health>/<player_max_health>",
      "&7Your balance: <economy_balance_formatted>",
      "",
      "&eClick to view"
    ]
    slot = 15
    left_click_commands = [
      "[close]",
      "[player] info"
    ]
    priority = 2
  }
}
```

---

## FAQ

### Q: Commands not working?
A: Check the following:
1. Ensure `open_command` field exists in configuration
2. Use `/menureload` to reload configurations
3. Check permission `lelmenu.use`

### Q: Placeholders not displaying?
A: Ensure MiniPlaceholders plugin is installed and placeholder format is correct

### Q: NBT data not working?
A: Check NBT format is correct and data types match

---

## Dependencies

- [SpongeAPI](https://www.spongepowered.org/) - Core API
- [MiniPlaceholders](https://github.com/MiniPlaceholders/MiniPlaceholders) - Placeholder support
- CommandPack (optional) - Economy system support

---

## Support and Feedback

If you have any questions or suggestions, please submit an Issue or contact the developer.

**Version**: 1.0.0  
**Author**: LelMC Team  
**License**: MIT License
