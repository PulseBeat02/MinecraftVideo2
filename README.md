# **Minecraft Video Read Me File**

**Version:** 0.0.1

**Author:** BananaPuncher714

------------------------------------------------------------------------------------------------------------------------------------------------------------------

**Plugin Setup**

- If you are on **Windows or Mac** :

  1) Install VLC here: [https://www.videolan.org/vlc/index.html](https://www.videolan.org/vlc/index.html).

  2) Install the Minecraft Video Plugin, and put it into your Plugins Folder.

  3) Run the server accordingly.

- If you are on **Linux or Unix** :

  1) Run the following command to download the VLC repository onto your machine: **sudo add-apt-repository &quot;deb http://archive.ubuntu.com/ubuntu $(lsb\_release -sc) universe**.

  2) After that, run updates to your Linux by running **sudo apt-get update** command.

  3) Finally, push updates to your computer by running the **sudo apt-get install command**.

  4) Install the Minecraft Video Plugin, and put it into your Plugins Folder.

  5) Run the server accordingly.

------------------------------------------------------------------------------------------------------------------------------------------------------------------

**Command Usage**

- **/video load**
  - /video load [Video ID] [Youtube URL]
    - Loads a video on the specified video ID with the Youtube URL that was provided.
  - /video load [Video ID] [MRL]
    - Loads a video on the specified video ID with the MRL that was provided. (Example: video.mkv)

- **/video play**
  - /video play [Video ID]
    - Plays the video (if stopped or loaded) for the specified Video ID.

- **/video stop**
  - /video stop [Video ID]
    - Stops the video (if currently playing) for the specified Video ID.

- **/video list**
  - /video list …
    - Lists all videos that have been loaded by the plugin.

- **/video delete**
  - /video delete [Video ID]
    - Deletes the video if exists in the list (see the list by typing /video list)
