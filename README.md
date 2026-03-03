# LelMenus - 乐联菜单插件

一个基于 SpongeAPI 的菜单插件，支持动态命令注册、丰富的占位符系统和自定义 NBT 数据。

## 📋 目录

- [命令](#命令)
- [功能特性](#功能特性)
- [配置文件格式](#配置文件格式)
- [支持的 NBT 格式](#支持的 nbt 格式)
- [支持的 MiniPlaceholders](#支持的 miniplaceholders)

---

## 命令

### `/menu [菜单名]`
- **权限**: `lelmenu.use`
- **描述**: 打开指定的菜单
- **用法**: 
  - `/menu` - 打开主菜单
  - `/menu shop` - 打开商店菜单

### `/menureload`
- **权限**: `lelmenu.reload`
- **描述**: 重新加载所有菜单配置并刷新命令映射
- **用法**: `/menureload`

### 动态菜单命令
- **权限**: `lelmenu.use`
- **描述**: 通过配置文件中的 `open_command` 自动注册的快捷命令
- **示例**: 
  - 配置文件中设置 `open_command = "cd"` → 可使用 `/cd` 打开对应菜单
  - 配置文件中设置 `open_command = "shop"` → 可使用 `/shop` 打开对应菜单

---

## 功能特性

### ✨ 核心功能
- **动态命令注册**: 根据配置文件自动注册菜单打开命令
- **热重载支持**: 使用 `/menureload` 无需重启服务器即可更新配置
- **丰富的占位符系统**: 支持玩家信息、服务器状态、经济系统等
- **NBT 数据支持**: 支持多种 NBT 数据类型和复杂结构
- **物品要求检测**: 支持检查玩家物品数量、类型等
- **权限控制**: 每个菜单项都可以设置独立的权限要求

### 🔧 高级功能
- **多菜单支持**: 可以创建多个不同的菜单文件
- **条件显示**: 根据玩家状态动态显示/隐藏菜单项
- **点击事件**: 支持左键、右键点击执行不同命令
- **物品 NBT**: 支持自定义物品的 NBT 数据
- **排行榜集成**: 支持经济系统财富排行榜

---

## 配置文件格式

### 基础配置

```hocon
# 菜单行数 (1-6)
rows = 3

# 打开此菜单的命令 (可选，设置为 null 禁用)
open_command = "main"

# 是否注册该命令 (true/false)
register_command = true

# 菜单更新间隔 (秒)
update_interval = 5

# 菜单标题 (支持颜色代码和占位符)
menu_title = "&c&l乐联&7-> &d服务菜单"

# 是否启用甜味模式 (视觉效果)
sweet = true
```

### 物品配置

```hocon
items {
  # 单个槽位配置
  "10" {
    # 物品材质 (支持 Minecraft ID)
    material = "minecraft:beacon"
    
    # 显示名称 (支持颜色代码和占位符)
    display_name = "&c主城"
    
    # 物品描述 (lore)
    lore = [
      "&9店员 技能学习",
      "&7地标位置/warp zc"
    ]
    
    # 槽位编号
    slot = 10
    
    # 物品数量
    count = 1
    
    # 附魔列表 (格式："附魔 ID;等级")
    enchantments = [
      "minecraft:binding_curse;1"
    ]
    
    # 是否隐藏附魔
    hide_enchantments = true
    
    # 左键点击执行的命令
    left_click_commands = [
      "[close]",
      "[player] warp zc"
    ]
    
    # 右键点击执行的命令
    right_click_commands = [
      "[console] say Hello"
    ]
    
    # 右键点击要求
    right_click_requirement {
      requirements {
        "check_permission" {
          type = "permission"
          input = "%player_has_perm%"
          output = "true"
        }
      }
    }
    
    # 显示要求 (只有满足条件才显示)
    view_requirement {
      requirements {
        "check_level" {
          type = "number >="
          input = "%player_level%"
          output = "10"
        }
      }
    }
    
    # 优先级 (数字越大越优先显示)
    priority = 1
    
    # NBT 数据 (见下方 NBT 格式说明)
    nbt_string {
      "CustomModelData" = "12345"
    }
  }
  
  # 多个槽位配置 (边框等)
  "border" {
    material = "yellow_stained_glass_pane"
    display_name = "&7装饰"
    slots = [0, 8, 17, 26]  # 多个槽位
    priority = 0
  }
}
```

### 特殊命令格式

- `[close]` - 关闭菜单
- `[player] 命令` - 以玩家身份执行命令
- `[console] 命令` - 以控制台身份执行命令
- `[message] 消息` - 发送消息给玩家

---

### View Requirement - 视图要求系统

View Requirement 用于控制菜单项的显示条件，只有满足所有条件时才会显示该物品。

#### 配置格式

```hocon
# 基础格式
requirements {
  "condition_name" {
    type = "条件类型"
    input = "输入值 (支持占位符)"
    output = "期望值"
  }
}

# 完整示例
items {
  "10" {
    material = "beacon"
    display_name = "&c主城"
    slot = 10
    
    # 直接在物品配置中添加 requirements
    requirements {
      "level_check" {
        type = "number >="
        input = "%player_level%"
        output = "10"
      }
    }
  }
}
```

**注意**: `requirements` 直接放在物品配置中，不需要 `view_requirement` 或 `right_click_requirement` 嵌套。

### 支持的比较类型
数值比较：

number > 或 number greater

number >= 或 number greater or equals

number < 或 number less

number <= 或 number less or equals

number equals

字符串比较：

string equals

string contains

string starts with

string ends with
#### 数值比较

| 类型 | 别名 | 说明 | 示例 |
|------|------|------|------|
| `number >` | `number greater` | 大于 | `input="<player_level>", output="10"` |
| `number >=` | `number greater or equals` | 大于等于 | `input="<player_exp_total>", output="100"` |
| `number <` | `number less` | 小于 | `input="<player_health>", output="5"` |
| `number <=` | `number less or equals` | 小于等于 | `input="<player_food>", output="20"` |
| `number equals` | - | 等于 | `input="<player_level>", output="50"` |

##### 字符串比较

| 类型 | 说明 | 示例 |
|------|------|------|
| `string equals` | 字符串相等 | `input="<player_world>", output="world"` |
| `string contains` | 字符串包含 | `input="<player_name>", output="admin"` |
| `string starts with` | 字符串开头 | `input="<player_uuid>", output="abc"` |
| `string ends with` | 字符串结尾 | `input="<player_uuid>", output="123"` |

#### 配置示例

##### 等级要求
```hocon
items {
  "10" {
    material = "beacon"
    # 直接在物品配置中添加 requirements
    requirements {
      "level_check" {
        type = "number >="
        input = "<player_level>"
        output = "10"
      }
    }
  }
}
```
只有玩家等级 >= 10 时才显示

##### 生命值要求
```hocon
items {
  "10" {
    material = "diamond_sword"
    requirements {
      "health_check" {
        type = "number >"
        input = "<player_health>"
        output = "5"
      }
    }
  }
}
```
只有玩家生命值 > 5 时才显示

##### 世界限制
```hocon
items {
  "10" {
    material = "grass_block"
    requirements {
      "world_check" {
        type = "string equals"
        input = "<player_world>"
        output = "world"
      }
    }
  }
}
```
只有在主世界时才显示

##### 多重条件
```hocon
items {
  "10" {
    material = "beacon"
    requirements {
      "level_check" {
        type = "number >="
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
必须同时满足等级 >= 10 **且** 生命值 > 5 才显示

#### Right Click Requirement - 右键点击要求

右键点击要求的配置方式相同，只需在命令配置中添加：

```hocon
items {
  "10" {
    material = "beacon"
    left_click_commands = ["[player] cmd"]
    right_click_commands = ["[player] admin_cmd"]
    
    # 右键点击也需要满足条件
    requirements {
      "permission_check" {
        type = "string equals"
        input = "<player_has_perm_lelmenu_admin>"
        output = "true"
      }
    }
  }
}
```

**说明**: 同一个物品只能有一组 `requirements`，它会同时影响显示和点击行为。

#### 注意事项

1. **占位符解析**: `input` 和 `output` 都支持 MiniPlaceholders 占位符
2. **多重条件**: 所有条件必须全部满足（AND 逻辑）
3. **错误处理**: 如果条件配置错误，默认显示物品
4. **性能优化**: 复杂的条件判断可能会有轻微性能影响

---

## 支持的 NBT 格式

### 1. 简单 NBT 类型

#### nbt_string (字符串类型)
```hocon
nbt_string {
  "CustomModelData" = "12345"
  "display_name" = "神器"
}
```

#### nbt_int (整数类型)
```hocon
nbt_int {
  "Damage" = 0
  "RepairCost" = 1
}
```

#### nbt_double (双精度浮点数)
```hocon
nbt_double {
  "explosion_power" = 1.5
}
```

#### nbt_float (浮点数)
```hocon
nbt_float {
  "speed" = 0.5f
}
```

### 2. 复杂 NBT 结构 (nbt_form)

支持完整的 HOCON 格式，可以定义复杂的嵌套结构：

```hocon
nbt_form {
  # 组件格式 (推荐用于 Minecraft 1.20.5+)
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
  
  # 传统 NBT 标签格式
  tag {
    # 简单值
    "Damage" = 0
    "Unbreakable" = true
    
    # 列表
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
    
    # 嵌套结构
    "EntityTag" {
      "id" = "minecraft:armor_stand"
      "CustomName" = "{\"text\":\"守卫者\"}"
      "Invulnerable" = true
    }
    
    # 数组
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

### 3. NBT 数据注意事项

- **数据类型后缀**:
  - `s` = short (短整型)
  - `b` = byte (字节型)
  - `l` = long (长整型)
  - `f` = float (浮点型)
  - `d` = double (双精度)
  - `B` = byte array (字节数组)
  - `I` = int array (整数数组)
  - `L` = long array (长整数组)

- **占位符支持**: NBT 字符串值中可以使用占位符
- **视图同步**: NBT 数据会在物品创建时自动应用

---

## 支持的 MiniPlaceholders

### 👤 Player 占位符 (玩家相关)

#### 基本信息
- `%player_uuid%` - 玩家 UUID
- `%player_name%` - 玩家名字
- `%player_prefix%` - 玩家前缀
- `%player_suffix%` - 玩家后缀

#### 位置和方向
- `%player_world%` - 所在世界名称
- `%player_x%` - X 坐标
- `%player_y%` - Y 坐标
- `%player_z%` - Z 坐标
- `%player_direction%` - 面向方向 (南/西南/西/西北/北/东北/东/东南)

#### 状态信息
- `%player_health%` - 生命值
- `%player_max_health%` - 最大生命值
- `%player_food%` - 饱食度
- `%player_saturation%` - 饱和度
- `%player_can_fly%` - 能否飞行
- `%player_flying%` - 是否正在飞行
- `%player_fly_speed%` - 飞行速度
- `%player_walk_speed%` - 行走速度

#### 游戏信息
- `%player_gamemode%` - 游戏模式
- `%player_level%` - 经验等级
- `%player_exp_total%` - 总经验值
- `%player_exp%` - 当前等级经验
- `%player_exp_to_next%` - 升级到下一级所需经验
- `%player_ping%` - 网络延迟
- `%player_language%` - 客户端语言

#### 空气值
- `%player_max_air%` - 最大空气值
- `%player_remaining_air%` - 剩余空气值

#### 手持物品
- `%player_item_in_main_hand%` - 主手物品名称
- `%player_item_in_off_hand%` - 副手物品名称

#### 时间统计
- `%player_time_played_seconds%` - 游戏时间 (秒)
- `%player_time_played_minutes%` - 游戏时间 (分钟)
- `%player_time_played_hours%` - 游戏时间 (小时)
- `%player_time_played_days%` - 游戏时间 (天)
- `%player_time_played%` - 游戏时间 (格式化输出，如 "1 d 2 h 30 m")
- `%player_first_join%` - 首次加入时间

### 🖥️ Server 占位符 (服务器相关)

#### 基本信息
- `%server_online%` - 在线玩家数
- `%server_max_players%` - 最大玩家数
- `%server_unique_players%` - 唯一玩家数 (去重)
- `%server_motd%` - 服务器描述 (MOTD)

#### 性能信息
- `%server_tps%` - 每秒刻数 (TPS)
- `%server_ram_used%` - 已用内存 (MB)
- `%server_ram_free%` - 可用内存 (MB)
- `%server_ram_total%` - 总内存 (MB)
- `%server_ram_max%` - 最大内存 (MB)
- `%server_cores%` - CPU 核心数

#### 运行时间
- `%server_uptime%` - 运行时间百分比
- `%server_uptime_total%` - 总运行时间
- `%server_time_world%` - 世界时间戳

### 💰 Economy 占位符 (经济系统)

#### 个人经济
- `%economy_balance%` - 玩家余额 (原始数值)
- `%economy_balance_formatted%` - 格式化后的余额 (带货币符号)
- `%economy_currency_name%` - 货币名称
- `%economy_currency_symbol%` - 货币符号

#### 财富排行榜
- `%economy_baltop_1%` ~ `%economy_baltop_5%` - 富豪榜前 5 名玩家名字
- `%economy_baltop_1_balance%` ~ `%economy_baltop_5_balance%` - 富豪榜前 5 名玩家余额

### 📊 其他占位符

#### 全局占位符
- `%server_hello%` - 你好 (测试用)
- `%server_server_name%` - 服务器名称 (自定义)

---

## 配置示例

### 完整的主菜单配置

```hocon
rows = 3
open_command = "main"
register_command = true
update_interval = 5
menu_title = "&c&l乐联&7-> &d服务菜单"
sweet = true

items {
  # 背景
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
  
  # 主城入口
  "spawn" {
    material = "beacon"
    display_name = "&c&l主城"
    lore = [
      "",
      "&9● 店员技能学习",
      "&7● 地标位置 /warp zc",
      "",
      "&e点击传送"
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
  
  # 商店
  "shop" {
    material = "emerald"
    display_name = "&a&l商店"
    lore = [
      "",
      "&7点击进入商店",
      "&7购买各种物品",
      "",
      "&e点击打开"
    ]
    slot = 13
    left_click_commands = [
      "[close]",
      "[player] shop gui"
    ]
    priority = 2
  }
  
  # 个人信息
  "info" {
    material = "book"
    display_name = "&b&l个人信息"
    lore = [
      "",
      "&7你的名字：%player_name%",
      "&7你的生命：%player_health%/%player_max_health%",
      "&7你的余额：%economy_balance_formatted%",
      "",
      "&e点击查看"
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

## 常见问题

### Q: 命令不工作怎么办？
A: 检查以下几点：
1. 确保配置文件中有 `open_command` 字段
2. 确保 `register_command = true`
3. 使用 `/menureload` 重新加载配置
4. 检查权限 `lelmenu.use`

### Q: 占位符不显示？
A: 确保安装了 MiniPlaceholders 插件，并且占位符格式正确

### Q: NBT 数据不生效？
A: 检查 NBT 格式是否正确，确保数据类型匹配

---

## 依赖

- [SpongeAPI](https://www.spongepowered.org/) - 核心 API
- [MiniPlaceholders](https://github.com/MiniPlaceholders/MiniPlaceholders) - 占位符支持
- CommandPack (可选) - 经济系统支持

---

## 支持与反馈

如有问题或建议，请提交 Issue 或联系开发者。

**版本**: 1.0.0  
**作者**: LelMC Team  
**许可**: MIT License
