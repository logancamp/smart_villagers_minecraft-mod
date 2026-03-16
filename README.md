# Smart Villagers

Description
This is a minecraft mod that allows for local running of llms for all villager interactions with a side chat upon viewing the trade window. Villagers can talk, argue and adjust or negotiate prices. Note price changes only occur after reopening the trade window, the villager will tell you.

Requirements
- Minecraft 1.21.11
- Fabric Loader
- Fabric API
- Ollama

Setup
1. Install Fabric Loader
2. Install Fabric API
3. Put smart-villagers-0.1.0.jar into your Minecraft mods folder
4. Install Ollama
5. Run: ollama run phi3
6. Run the SmartVillagersAI python file
7. Launch Minecraft through fabric as you would for any mod

Notes
- The AI helper must stay running while Minecraft is open.
- Villager trade updates appear after reopening the trade menu.
