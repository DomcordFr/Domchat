# Domchat

Tired of using clunky websites to roleplay, only to find that every app you try is filled with ads, subscription paywalls, and clutter?

Domchat fixes that. It is clean, smooth, and roleplay-first. The interface is built to make you feel like you are chatting with a real person rather than talking to a sterile chatbot widget.

---

## Why Domchat?

### Complete Privacy
When you roleplay on commercial websites or closed apps, you never know who is reading your private sessions. Many platforms log prompts, train models on user inputs, or let staff inspect flagged messages. 

Domchat runs entirely on your phone. It sends data only to the model provider or proxy you choose. There are no tracking scripts, analytics, or central accounts. Because Domchat is open source, anyone can inspect the code to verify that private chats stay on your device.

### Roleplay-First Features
- **SillyTavern Character Cards**: Import Character Card V2 PNGs or JSON cards. Full descriptions, scenarios, greeting messages, and author's notes load instantly.
- **Lorebooks & World Info**: Attach world info to keep long adventures consistent. Entries activate automatically when matching keywords appear in dialogue.
- **Multiple User Personas**: Switch between different roleplay personas. Each persona has its own name, avatar, description, and pronoun settings.
- **Direct Model Connections**: Use your own API keys with Nano-GPT, OpenRouter, Featherless, Claude, OpenAI, DeepSeek, or run offline models with Google LiteRT.
- **Alternate Replies & Branching**: Swipe left or right on any reply to generate alternate takes without losing earlier messages.
- **Natural Voice (TTS)**: Listen to character replies using Fish Audio, ElevenLabs, OpenAI Audio, or free Android system voices.
- **Liquid Glass Design**: Customize wallpaper backgrounds, glass transparency, blur levels, and OLED pure black mode.

---

## Quickstart Guide

### 1. Install the App
Download `Domchat_1.0_stable-arm64-v8a-release.apk` from the [Releases](https://github.com/DomcordFr/Domchat/releases) page and install it on your Android phone.

### 2. Add an AI Provider
1. Open Domchat and tap **Settings** in the bottom navigation bar.
2. Tap **Providers**, then tap the **+** button in the bottom right corner.
3. Select your provider:
   - For **Nano-GPT**: Pick OpenAI compatible, set the base URL to `https://nano-gpt.com/api/v1`, and paste your API key.
   - For **OpenRouter**: Pick OpenRouter, enter your key, and tap the cubes icon to pick models.
   - For **Anthropic** or **OpenAI**: Enter your API key directly.
4. Tap **Save**. Tap the cubes icon to choose which models appear in your chat picker.

### 3. Import a Character
1. Tap the **Characters** tab.
2. Tap the import button in the top right.
3. Select any SillyTavern character card (`.png` with embedded metadata) or `.json` file from your storage.
4. Tap **Start Chat**.

### 4. Set Up Voice Playback (Optional)
1. Go to **Settings > Text to Speech**.
2. Tap **TTS Providers** and add your preferred engine:
   - **Fish Audio**: Select Fish Audio, enter your API key, and provide your voice ID.
   - **ElevenLabs**: Select ElevenLabs, enter your API key, and provide your voice ID.
   - **System TTS**: Uses the free voices already installed on your phone.
3. Tap the speaker icon on any message to listen. A floating glass controller lets you pause, replay, or scrub through the audio.

### 5. Create Your Persona
1. Go to **Settings > Personas**.
2. Tap **Add Persona**.
3. Enter your name, avatar, and background story.
4. Set it as default. The character will address your persona by name in the scene.
