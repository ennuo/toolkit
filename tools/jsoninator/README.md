# Jsoninator

CLI Java utility for converting resources between JSON and binary formats.

Supports (de)serialization of common resources, including but not limited to .plan, .bin, .mat,
.anim, etc

## Notes

Most common revisions for deploy, lbp1, lbp2, and lbp3 should be fully compatible.

## Usage

```bash
# Convert a resource to JSON
java -jar jsoninator.jar pal_fishsticks_4575.plan fishsticks.json

# Convert back to a resource
java -jar jsoninator.jar fishsticks.json pal_fishsticks_4575.plan
```