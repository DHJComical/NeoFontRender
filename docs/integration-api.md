# Neo Font Render integration API

NFR API version 2 exposes TOML storage and client settings-page registration. Integrations should
declare NFR as a required or optional compile-time dependency as appropriate.

## TOML configuration

Dedicated files are the default and recommended mode:

```java
NfrConfigFile config = NfrConfigApi.builder("examplemod")
        .fileName("examplemod.toml")
        .open();
config.define("feature.enabled", true, "Enable the feature.");
boolean enabled = config.getBoolean("feature.enabled", true);
config.set("feature.enabled", false).save();
```

An integration may explicitly store values in the NFR main file instead:

```java
NfrConfigFile config = NfrConfigApi.builder("examplemod")
        .storage(NfrConfigStorage.NFR_MAIN)
        .open();
```

Those keys are isolated below `extensions.examplemod` in `neofontrender.toml`. The Modern Tooltips
addon intentionally uses `config/neofontrender-modern-tooltips.toml`, not the main file.

## Embedded settings pages

Register an `NfrSettingsPage` during client initialization:

```java
NfrSettingsPageRegistry.register(new ExampleSettingsPage());
```

Each opening of the NFR settings screen calls `createSession()`. The returned session builds its
view with NFR's shared controls and receives `apply()` or `cancel()` from the common footer. Keep
editable values in the session (or snapshot runtime values) so Cancel can restore them. Page ids
must be namespaced, such as `examplemod:settings`.

## Arc3D

NFR distributes Arc3D Core as an unrelocated contained library. Integrations can use the original
`icyllis.arc3d.*` packages or the stable, explicitly named `neofontrender.api.arc3d.Arc3DApi`
entry point. Do not bundle a second Arc3D copy when NFR is a required dependency.
