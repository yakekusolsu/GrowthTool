# Bedrock compatibility

Phase 6 abilities activate from ordinary gameplay events and do not require Java-only keys.
Floodgate detection is isolated behind `BedrockPlayerService.isBedrockPlayer(UUID)` and returns
false when Floodgate is unavailable. Geyser presence is reported for diagnostics; it does not
change progression or deny features. Bedrock players therefore receive the same automatic
level, EXP, and ability behavior as Java players.

A real Bedrock client QA session passed GrowthTool use, Block EXP, Vein Miner, Area Break,
fishing, and bow through the tested Geyser/Floodgate profile. This is scenario-specific evidence,
not a claim for every protocol translator version or configuration.

Future manual abilities must offer a command, inventory interaction, or another cross-platform
input. Swap-hand, middle-click, or an unsupported keybind must never be the sole activation path.
