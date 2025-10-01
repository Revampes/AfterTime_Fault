# Custom Cape Instructions

## How to Use Custom Capes

1. **Enable the module**: Go to your mod's config menu and enable "Custom Cape" under the Render category.

2. **Add your cape files**: Place your cape image files in the mod's config directory:
   ```
   config/ratallofyou/capes/
   ```

3. **Supported formats**:
   - **PNG files**: For static (non-animated) capes
   - **GIF files**: For animated capes

4. **Recommended specifications**:
   - **Size**: 64x32 pixels (standard Minecraft cape dimensions)
   - **Format**: PNG for static capes, GIF for animated capes
   - **File naming**: Any name is fine, the mod will use the first cape file it finds alphabetically

## File Location

The mod will automatically create the capes directory at:
```
config/ratallofyou/capes/
```

This is integrated with your mod's configuration system, making it easy to manage cape files alongside other mod settings.

## File Priority

The mod will automatically load the first cape file it finds in the config directory (alphabetically). If you have multiple cape files, make sure the one you want to use comes first alphabetically.

## Features

- **Static Capes**: PNG files for non-animated capes
- **Animated Capes**: GIF files with frame-by-frame animation
- **Hot Reload**: Change cape files without restarting Minecraft
- **Config Integration**: Files stored in mod's config directory
- **Automatic Detection**: Supports both PNG and GIF formats

## Troubleshooting

- Check the Minecraft console/logs for cape loading messages
- Make sure the cape file is not corrupted
- Verify the file format is supported (PNG/GIF only)
- The cape will only appear when the "Custom Cape" module is enabled in the config
- Cape files must be placed in: `config/ratallofyou/capes/`

## Example Directory Structure

```
config/
└── ratallofyou/
    ├── Config.cfg
    ├── FHK.cfg
    └── capes/
        ├── my_awesome_cape.png     ← Static cape
        ├── animated_cape.gif       ← Animated cape
        └── README.md              ← This file
```

The mod will use `animated_cape.gif` in this example (comes first alphabetically).
