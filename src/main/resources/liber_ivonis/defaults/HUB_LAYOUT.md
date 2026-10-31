# Liber Ivonis Hub layout

The Hub is built from `config/liber_ivonis/hub.json`. The file is read whenever
the Hub opens, so layout and translation changes do not need a game restart.
If the JSON is invalid, the bundled default is shown and the parse error is
written to the game log.

The ModernUI settings page shows the active file paths, layout validation
status, and theme palette. It can open the config/language folders, reload the
files, and open a live Hub preview without leaving the game. In preview mode,
entries that need a loaded player/world remain visible but are disabled.

Translation overrides live in `config/liber_ivonis/lang/<language>.json`.
The lookup order for text in this layout is:

1. The selected language in `config/liber_ivonis/lang`.
2. English (`en_us.json`) in the same directory.
3. Minecraft's loaded language resources.
4. The node's `fallback` value.
5. The original text.

This means a translation key can be edited in the local `lang` directory
without replacing the mod JAR or creating a resource pack.

The old `liber_ivonis-client.toml` is no longer read. Move custom entries and
colours from that file into this JSON document.

## Document

```json
{
  "schemaVersion": 1,
  "theme": {
    "background": "#FF0D0F15",
    "surface": "#FF171A23",
    "surfaceAlt": "#FF2E3547",
    "accent": "#FFF2F5F9",
    "muted": "#FFAEB8C6",
    "outline": "#FF7FD9F5",
    "disabledOutline": "#FF2B3244"
  },
  "layout": {
    "type": "column",
    "children": []
  }
}
```

`layout` may be any supported node. Containers use a `children` array and may
be nested freely (up to 16 levels).

`theme` is optional. It defines the seven palette colours used by node colour
names and by the handbook category/settings pages. Values accept `#RRGGBB`
(opaque) or `#AARRGGBB`. Missing theme colours use the default shown above.

## Nodes

- `column`: vertical `LinearLayout`.
- `row`: horizontal `LinearLayout`.
- `linear`: `LinearLayout`; set `orientation` to `vertical` or `horizontal`.
- `scroll`: scrollable container. Its children are stacked vertically if more
  than one child is supplied. Supports `fillViewport` and `smoothScrolling`.
- `grid`: arranges children into rows with equal-width cells. Set `columns`
  (1-12, default 2) and optional `gap` in dp.
- `text`: a translation key in `text`, plus optional `fallback`, `size`,
  `color`, `textAlign`/`textGravity`, and `maxLines`.
- `entry`: opens a built-in page, item handbook, or custom screen.
- `button`: currently supports `"action": "close"`.
- `spacer`: an empty view, useful in rows and grids.

Common layout properties are available on every node:

```json
{
  "width": "match_parent",
  "height": "wrap_content",
  "weight": 1,
  "margin": [0, 0, 0, 8],
  "padding": [16, 9, 16, 9],
  "gravity": "left|center_vertical",
  "background": "surfaceAlt",
  "minWidth": 0,
  "minHeight": 48,
  "visible": true
}
```

Dimensions accept `match_parent`, `wrap_content`, a dp number, or a string
such as `"24dp"`. `margin` and `padding` accept one, two, or four numbers in
the order left, top, right, bottom. Two numbers mean horizontal, vertical.

Widths and heights passed as a number are dp. A positive `weight` fills the
remaining space on the parent's main axis.

Colors use the palette names `background`, `surface`, `surfaceAlt`, `accent`,
`muted`, `outline`, and `disabledOutline`. Hex colors such as `"#FFAABBCC"`
are also accepted for explicit node colors.

## Entries

An entry must contain exactly one target:

```json
{"type": "entry", "builtin": "ftbquests"}
{"type": "entry", "item": "ageratum:guidebook", "title": "my_pack.guidebook"}
{"type": "entry", "screen": "com.example.ScreenFactory", "method": "create",
 "title": "my_pack.screen", "fallback": "Example screen"}
```

- `builtin`: `ftbquests`, `patchouli`, `modonomicon`, `guideme`, `ageratum`,
  `inventory`, `advancement`, or `controls`.
- `item`: an item registry id. The item name is used when `title` is omitted.
- `screen`: a screen factory class. The method is static and should accept
  supported client objects or no parameters. The class name is used when
  `title` is omitted.

Optional entry properties:

- `title`: translation key or literal label.
- `fallback`: label used when no translation exists.
- `description` and `descriptionFallback`: accessibility description.
- `showUnavailable`: when `true`, an unavailable mod entry is shown disabled.
  The default is `false`.
- `size` and `color`: button typography overrides.

Entries whose required mod is unavailable are omitted unless
`showUnavailable` is enabled.

## Example

```json
{
  "schemaVersion": 1,
  "layout": {
    "type": "column",
    "padding": [24, 22, 24, 18],
    "background": "background",
    "children": [
      {"type": "text", "text": "screen.liber_ivonis.hub", "size": 22},
      {
        "type": "scroll",
        "weight": 1,
        "children": [
          {
            "type": "grid",
            "columns": 3,
            "gap": 8,
            "children": [
              {"type": "entry", "builtin": "ftbquests"},
              {
                "type": "entry",
                "screen": "com.example.ScreenFactory",
                "method": "create",
                "title": "my_pack.screen",
                "fallback": "Example"
              }
            ]
          }
        ]
      },
      {
        "type": "button",
        "action": "close",
        "text": "screen.liber_ivonis.back",
        "width": "match_parent"
      }
    ]
  }
}
```
