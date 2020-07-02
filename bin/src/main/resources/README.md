# **Minecraft Video**
## **About**
Minecraft Video is a Bukkit plugin that intends to allow server owners to play videos on item frames, as well as automatically extracting, packaging, and sending the video's sound to clients via resource packs.

### Dependencies:
Minecraft Video requires several things to run:
- VLC
- An additional open port to service client resource pack requests
Optional:
- Python 2.6/2.7/3.2 for the youtube downloader

### Authors:
- BananaPuncher714 - Creator, Developer
- jetp250 - Developer

## **Usage**
A 5x5 map canvas is required, ranging from 0-24, left to right, top to bottom. You can then use the commands to play videos.

### Commands:
- /video
  - play <id> - Plays the specified video with the id
  - stop - Stops the currently playing video
  - list - Lists all loaded videos
  - load <id> <mrl> - Loads a video with <id> at the given mrl. The mrl can be a url, or a file relative to the /plugins/MinecraftVideo folder
  - delete <id> - Deletes a video with the given id

## **How it works:**
Minecraft Video uses VLCJ to decode video files and converts them to the 124 minecraft color palette. It then splits it into map packets which get sent to the client. The sound is extracted from the video file and converted to vorbis. It is then packaged into a resource pack automatically and sent to the client.

## **Credits**
A big shoutout to jetp250 for his help with optimizing the image utility used for converting colors. Without it, MinecraftVideo would not be able to play anything larger than 256x192 videos!