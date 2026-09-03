# Liber Ivonis 伊波恩之书

NeoForge 1.21.1 的多模组界面入口。右键使用伊波恩之书打开暗色 Screen Hub。

## 设计与兼容

- ModernUI NeoForge `3.13.0.1`（Curse Maven `8206075`）是必需依赖，项目使用其 Arc3D 渲染运行时。
- Hub 核心不直接链接可选模组类；FTB Quests、Patchouli、Modonomicon、GuideME 与藿香在模组存在时显示。
- 手册入口优先调用手册物品自身的客户端 `use` 行为，避免绑定私有 Screen 类。
- 容器类 GUI 仍必须由目标模组创建并同步菜单，Hub 不绕过服务端校验。

## 配置

配置文件：`config/liber_ivonis-client.toml`

```toml
[hub]
showUnavailable = false
builtinEntries = ["ftbquests", "patchouli|key:examplemod.category.adventure", "inventory"]
entryOrder = ["patchouli", "item:examplemod:guide_book", "custom:com.example.client.ExampleScreen", "inventory"]

[handbooks]
handbookItems = ["examplemod:guide_book|示例手册|key:examplemod.category.adventure"]

[customScreens]
customScreens = ["我的界面|com.example.client.ExampleScreen|create|key:examplemod.category.tools"]
```

自定义界面名称支持翻译键：将第一个字段写成 `key:examplemod.screen.title`；不带 `key:` 时按普通文字显示。

自定义屏幕方法必须是静态方法，返回 `net.minecraft.client.gui.screens.Screen`。默认尝试的方法名为 `create`，也支持 `open` 与 `getScreen`。
