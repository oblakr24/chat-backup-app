# Chat Backup
<b>An fully-featured SMS app with ability to export and import your SMS conversations.</b>

This is a tech-demonstration app showcasing the latest Android tech stack while solving a real-world use-case.

## Features:
1. Fully offline
2. Send, receive SMS messages
3. Quick reply to an SMS
4. No ads or payments
5. Listing of current conversations and messages
6. Search with highlighting, select, delete and export functionalities for conversations
7. Import a previously exported file to preview and (selectively) save the messages to the device
8. Open architecture allowing for future extensions and other formats
9. Togglable dark-light theming

## Video

https://user-images.githubusercontent.com/32245831/221344724-336c23ea-c49e-4ca9-9392-284b9ed760f4.mov

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
</br>
1. Use it as your default SMS app
2. Use the app on any phone to export the selected messages
3. Share the messages to another device
4. Use the app on another device to open the file
5. Click the "Save" button on selected conversations to save them to the device.
</br>
<b>What if I want a different export/import format?</b>
</br>
The app currently does not support other formats. However, you can submit an issue request on github or contribute.
</br>
<b>Does this app send any of my data?</b>
</br>
No - this app does not communicate with any servers.
It is up to you how to share the exported file.


## Screenshots

<b>SMS Browsing & Search</b>

<p align="center">
  <img src="https://user-images.githubusercontent.com/32245831/221346215-c8fd26b6-0804-4422-a714-f2fbe3f53957.png" width="270" height="570">
  <img src="https://user-images.githubusercontent.com/32245831/221346211-a51e034a-a146-477f-9e7d-5b99483fcf25.png" width="270" height="570">
  <img src="https://user-images.githubusercontent.com/32245831/221346312-514253cc-4041-4798-87a2-de04083b72ff.png" width="270" height="570">
</p>

<b>Dark Mode</b>

<p align="center">
  <img src="https://user-images.githubusercontent.com/32245831/221346292-feacc6d6-66d2-46d8-b6e5-65e952003830.png" width="270" height="570">
  <img src="https://user-images.githubusercontent.com/32245831/221346295-61eb898d-46de-49b5-aec7-d5406406701a.png" width="270" height="570">
</p>

<b>Conversation</b>

<p align="center">
  <img src="https://user-images.githubusercontent.com/32245831/221346339-62391c22-b76e-451f-8be4-ba14526dd2d0.png" width="270" height="570">
  <img src="https://user-images.githubusercontent.com/32245831/221346821-5dc7d7b5-4069-4ee9-b6d2-749a17e4b63b.png" width="270" height="570">
</p>

<b>Compose</b>

<p align="center">
  <img src="https://user-images.githubusercontent.com/32245831/221346366-8f779fb1-98c7-4528-8bfb-d26c805e63ae.png" width="270" height="570">
  <img src="https://user-images.githubusercontent.com/32245831/221346718-5a931a40-9387-4e9a-857b-e835675d870c.png" width="270" height="570">
</p>
