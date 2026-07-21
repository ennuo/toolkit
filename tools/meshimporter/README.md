# Mesh Importer CLI

## Usage:

```bash
java -jar meshimporter.java -i <input> <...options> -o <output>
```

```
Arguments:
        --input, -i <path>
        Specifies the glTF 2.0 Binary (.glb) source file to import

        --output, -o <path>
        Path to write built model file to

        --map, -m <...overrides>
        Specifies material override to existing game resources, pairs are separated by commas in the format of material_name=resource
        For example, a single override would be "Material.001=g916",
        while multiple overrides would be "Material.001=g916,Material.002=g916"

        --skeleton, -s <skeleton>
        Specifies the skeleton to use for a costume model
        Supported values:
        sackboy, oddsock, swoop, small_toggle, big_toggle

        --categories, -c <...categories>
        Specifies the categories used by a costume, list is separated by commas
        Supported values:
        beard, feet, eyes, glasses, mouth, moustache, nose
        hair, head, neck, torso, legs, hands, waist

        --regions, -r <..regions>
        Specifies the regions to hide when equipping a costume piece, list is separated by commas
        Built-in regions for Sackboy:
        _scalp, _brow, _torso, _torso1, _pants, _shorts, _socks, _zip, _zippull
        _armr, _arml, _sleever, _sleevel, _legs, _glover, _glovel, _eyes

        --plan, -p <path>
        Specifies the path to build a costume piece plan file to
```