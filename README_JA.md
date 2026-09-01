# GrowthTools

[![Build](https://github.com/yakekusolsu/GrowthTool/actions/workflows/build.yml/badge.svg)](https://github.com/yakekusolsu/GrowthTool/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java 21](https://img.shields.io/badge/Java-21-orange)
![Paper 1.21.x](https://img.shields.io/badge/Paper-1.21.x-blue)

[English](README.md)

GrowthToolsは、Minecraftのツールや武器へアイテム単位のLevel、EXP、解放Abilityを追加するPaper向けOSSプラグインです。**0.7.0-alpha.1**はpre-releaseです。本番導入前にバックアップを取得し、[既知の問題](docs/known-issues.md)を確認してください。

## 機能

- PDCに保存するTool UUID、Level、累積EXP、data version、Lore
- Pickaxe、Axe、Shovel、Hoe、Fishing Rod、Bowへの対応
- Block、釣果、成立したBow Damageによる設定可能なEXP
- 安全上限付きVein Miner、Area Break、Auto Smelt、Experience Boost
- SQLite Registry／Audit Log／再起動後も維持されるPlaced Block保護
- Repair、Duplicate検出、明示的ID再生成、診断、管理Command
- 所有者付きAbility登録と公開Eventを持つ実験的Addon API v1
- PlaceholderAPI、Vault、WorldGuard、mcMMO、Jobs Reborn、Geyser、Floodgateの任意・分離Adapter

任意連携の検証範囲は[互換性matrix](docs/compatibility.md)へscenario単位で記録しています。Paper 1.21.11ではWorldGuard、PlaceholderAPI、mcMMO、Jobs、Geyser、Floodgateの記載範囲をplayer QA済みです。未記載の構成はmanualのままです。より広い互換試験、API安定化、ToolごとのAbility選択／Skill Tree設計は今後の予定であり、実装済みではありません。

## 対応環境

- Paper 1.21.4 build 232とPaper 1.21.10 build 130はserver runtime test済み
- Paper 1.21.11は互換性matrix記載範囲で実player QA済み
- その他のPaper 1.21.xは互換を想定しますがruntime未検証
- Paper 1.21.x以外は非対応
- Java 21

## インストールとQuick start

1. Releaseから`GrowthTools-0.7.0-alpha.1.jar`を取得するか、sourceからbuildします。
2. Paper serverの`plugins`へ配置します。任意Pluginは必須ではありません。
3. Serverを起動し、consoleで正常なenableを確認します。
4. OPで`/gt give <player> pickaxe`を実行し、手に持って`/gt inspect`を確認します。
5. `plugins/GrowthTools/config.yml`を確認し、安全な変更後に`/gt reload`します。

詳細は[Installation](docs/installation.md)、[設定](docs/configuration.md)、[Migration](docs/migration.md)、[Privacy](docs/privacy.md)を参照してください。

## Commands

| Command | 目的 |
| --- | --- |
| `/growthtools`, `/gt`, `/gt version` | Help／version |
| `/gt give <player> <type>` | GrowthTool作成 |
| `/gt inspect` | 手持ちItemの調査 |
| `/gt reload` | config／messagesの検証・reload |
| `/gt debug tool|registry|database`, `/gt debug add-level <levels>` | 管理診断・手持ちToolのLevel加算 |
| `/gt repair`, `/gt regenerate-id` | 保守的repair／明示的UUID交換 |
| `/gt ability list|info|debug` | Ability Registry調査 |
| `/gt integrations` | 任意連携health表示 |
| `/gt doctor [export]` | 診断またはprivacy-safe report出力 |

Typeは`pickaxe`、`axe`、`shovel`、`hoe`、`fishing_rod`、`bow`です。詳細は[管理Command](docs/admin-commands.md)を参照してください。

## Permissions

`growthtools.command`のみ全員が利用できます。管理権限`growthtools.admin.reload`、`.give`、`.inspect`、`.debug`、`.repair`、`.regenerateid`、`.ability`、`.integrations`、`.doctor`はすべてdefault OPです。

## Dataと安全設計

portableな正本はPDCであり、Loreは表示専用です。SQLiteは観測情報とPlaced Block保護を保存しますが、正常なPDCを上書きしません。SQLは専用single-thread executorで処理します。DB初期化に失敗した場合、PDC Toolの参照を継続し、Registry依存機能とBlock EXPを安全側へdegradeします。Ability追加破壊は上限を持ち、未load／設置済み／保護済みBlockを除外して通常の耐久処理を使います。

## Developer API

Addonは`GrowthTools-api-0.7.0-alpha.1.jar`を`compileOnly`で利用し、`depend: [GrowthTools]`を宣言して`GrowthToolsProvider.get()`からAPI v1を取得します。0.x中は実験的です。[API guide](docs/api.md)、[version policy](docs/api-versioning.md)、[Ability API](docs/ability-api.md)、[example addon](examples/growthtools-example-addon/README.md)を参照してください。

## SourceからBuild

```shell
./gradlew build
./gradlew releaseBuild
```

Windowsでは`gradlew.bat`を使います。`build`はunit／MockBukkit integration testとAPI／JAR監査を実行します。`releaseBuild`はcleanな配布artifactを検証します。成果物は`build/libs/`です。`./gradlew runServer`はPaper 1.21.11のmanual QA profileを起動します。Minecraft EULAの承諾はserver operatorが行ってください。

## Roadmap

- Phase 1〜6: 基盤、成長、Gameplay EXP、SQLite／Registry、4 Ability、Addon APIと任意連携境界
- Phase 7: Release監査、API baseline、再現可能archive、実Paper起動QA、failure isolation、公開文書
- Phase 7.5: disposable player QA kit、2 versionのPaper smoke、任意Plugin lifecycle matrix、SQLite lock degradation、初回Git import準備
- [Manual QA](docs/manual-qa.md)に残るplayer／failure／任意連携edge matrixの継続
- 将来: API安定化、設計済みAbility選択／Skill Tree

MySQL、Redis、cross-server同期、Web dashboard、GUIは含みません。

## ContributionとSecurity

[CONTRIBUTING.md](CONTRIBUTING.md)を確認してください。脆弱性はpublic Issueではなく、[SECURITY.md](SECURITY.md)のprivate手順で報告してください。

## License

[MIT License](LICENSE)です。bundleする第三者componentは[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)に記載します。
