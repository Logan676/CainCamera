# CainCamera Overview
CainCamera is a comprehensive open source Android app that integrates a beauty camera, image editing and short video editing. This project mainly demonstrates the implementation of the beauty camera and short video editor. The image editing module is not yet finished.

I created this project to learn how to implement real-time beautification, dynamic filters, dynamic stickers, makeup, photo capture, segmented video recording with deletion, image editing, short video editing and composition. The features completed so far include:

## 1. Beauty Camera
- Real-time beauty and whitening
- Dynamic filters
- Dynamic stickers
- Photo capture and segmented video recording with deletion
- Face reshaping such as slim face, bright eyes and teeth whitening
### Note
Due to missing makeup materials this module only demonstrates the general workflow.

## 2. Short Video Editing
- UI inspired by Douyin (TikTok). The editing page mimics the Douyin style.
- Custom FFmpeg-based video player wrapped in a MediaPlayer-like interface. It supports fast seeking, speed adjustment and real-time effect preview. This player is mainly for short video editing preview and is still buggy.
- Simple trimming based on remuxing. Playback speed processing is not implemented yet.
- Real-time effects such as flash, hallucination, zoom, shake, soul out-of-body filters and all split screen effects. Time effects require modifications to the player and are not yet implemented.
### Note
A non-linear editing SDK is under development.

## Update Log
**2021-04-05**: Upgraded CameraX to `1.0.0-rc03`. Basic face detection and beauty features are adapted, but some CameraX functions are still missing. After finishing the non-linear editing SDK I plan to integrate [MediaPipe](https://github.com/google/mediapipe) to replace the current face SDK.

# About Face SDK Verification
The face keypoint SDK uses a trial version of Face++ and has a limited number of daily uses. Please register your own key at [Face++](https://www.faceplusplus.com/) and bind your package name before use. Mainland users should register at [https://www.faceplusplus.com.cn/](https://www.faceplusplus.com.cn/). Registration steps:
[Face++ SDK Registration](https://github.com/CainKernel/CainCamera/blob/master/document/introduction/facepp_registration.md)

For more questions about Face++ SDK please ask on the official GitHub:
[MegviiFacepp-Android-SDK](https://github.com/FacePlusPlus/MegviiFacepp-Android-SDK)

# Library Introduction
- **cameralibrary**: Camera library including render thread and engine.
- **facedetectlibrary**: Face++ keypoint SDK library used with `landmarklibrary`.
- **filterlibrary**: Contains filters and resource utilities.
- **imagelibrary**: Image editing library. Currently only provides filter processing and saving.
- **landmarklibrary**: Normalized key point processing for filters and stickers.
- **medialibrary**: Short video editing library providing a real-time preview player, audio trimmer and video composer in C++. Audio/video trimming and composition are still under development.
- **pickerlibrary**: Media selector for choosing images and videos.
- **utilslibrary**: Shared utilities for bitmap, file and string handling.
- **videolibrary**: Planned video editing library. Not yet implemented.

# CainCamera Screenshots
## Animated Stickers and Filters
![Sticker and filter](https://github.com/CainKernel/CainCamera/blob/master/screenshot/sticker_and_filter.jpg)

![Dynamic filter](https://github.com/CainKernel/CainCamera/blob/master/screenshot/dynamic_filter.jpg)

## Face Beautification and Reshaping
![Beauty face](https://github.com/CainKernel/CainCamera/blob/master/screenshot/beauty_face.jpg)

![Face reshape](https://github.com/CainKernel/CainCamera/blob/master/screenshot/face_reshape.jpg)

## Makeup Function
*Note: Only demonstrates using masks because materials are missing.*

![Makeup](https://github.com/CainKernel/CainCamera/blob/master/screenshot/makeup.jpg)

## Media Library Scanning
![Media scan](https://github.com/CainKernel/CainCamera/blob/master/screenshot/media_scan.jpg)

## Image Editing Page
*Note: The image editor does not include all features yet.*

![Image edit](https://github.com/CainKernel/CainCamera/blob/master/screenshot/image_edit.jpg)

# CainCamera Reference Projects
[grafika](https://github.com/google/grafika)

[GPUImage](https://github.com/CyberAgent/android-gpuimage)

[MagicCamera](https://github.com/wuhaoyu1990/MagicCamera)

[AudioVideoRecordingSample](https://github.com/saki4510t/AudioVideoRecordingSample)

# "Android Beauty Camera Development Series"
[Chapter 1 Camera Preview with OpenGL ES](https://www.jianshu.com/p/dabc6be45d2e)

[Chapter 2 Recording Video with OpenGL ES](https://www.jianshu.com/p/d5fe577170cd)

[Chapter 3 Adding Filters to Camera](https://www.jianshu.com/p/f7629254f7f0)

[Chapter 4 Dynamic Stickers](https://www.jianshu.com/p/122bedf3a17e)

[Chapter 5 Customized Beautify Effects](https://www.jianshu.com/p/3334a3af331f)

[Chapter 6 Customized Makeup Effects](https://www.jianshu.com/p/bc0d0db2893b)

# "Android FFmpeg Player Development"
[Chapter 0 Common Base Classes](https://www.jianshu.com/p/9003caa6683f)

[Chapter 1 Player Initialization and Demuxing](https://www.jianshu.com/p/95dc19217847)

[Chapter 2 Audio and Video Decoders](https://www.jianshu.com/p/8de0fc796ef9)

[Chapter 3 Audio Output - OpenSLES](https://www.jianshu.com/p/9b41212c71a5)

[Chapter 4 Audio Resample and Tempo/Pitch](https://www.jianshu.com/p/4af5d16ac017)

[Chapter 5 Video Synchronization and Rendering](https://www.jianshu.com/p/f8ba3ceac687)

# Contact
email: <cain.huang@outlook.com>

blog: [cain_huang](http://www.jianshu.com/u/fd6f2b25d0f4)

# License
```
Copyright 2018 cain.huang@outlook.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
