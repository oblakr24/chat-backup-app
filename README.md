# Chat Backup
<b>An app used to list, export and import your SMS conversations.</b>

This is a tech-demonstration app showcasing the latest Android tech stack while solving a real-world use-case.

## Features:
1. Fully offline
2. No ads or payments
3. Listing of current conversations and messages
4. Search with highlighting, select, delete and export functionalities for conversations
5. Import a previously exported file to preview and (selectively) save the messages to the device
6. Open architecture allowing for future extensions and other formats
7. Togglable dark-light theming

## Tech stack

This is a single-activity app fully built with Jetpack Compose screens.
Each screen relies on a navigation route usd by Compose Navigation and a Hilt-instantiated viewmodel.
A screen depends on a single UI state model to make it completely reliant on the viewmodel-defined source of truth.
The UI state is constructed using Molecule in the viewmodel from smaller pieces of state; this way the architecture is simpler, more controllable and more extensible.

The sharing (exporting and importing of app-generated files) is delegated to system utilities, so the security and privacy is guaranteed by the system and the control is given to the user on how to share.

Specific dependencies:

- Jetpack Compose and Compose Navigation: UI
- Hilt: Dependency injection
- Molecule: using of Compose Compiler in the VM layer for reactive state construction
- KotlinX serialization for serialization and deserialization of models into and from files
- Extended Material icons for vector images
- Accompanist Permissions for Composable permission handling
- DataStore for persisting user preferences

## FAQ
<b>How do you use this app?</b>
1. Use the app on any phone to export the selected messages
2. Share the messages to another device
3. Use the app on another device to open the file
4. Click the "Save" button on selected conversations to save them to the device.
<b>What if I want a different export/import format?</b>
The app currently does not support other formats. However, you can submit an issue request on github or contribute.
<b>Does this app send any of my data?</b>
No - this app does not communicate with any servers.
It is up to you how to share the exported file.