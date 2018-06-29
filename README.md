# What is *"Planet"*
*"Planet"* is my yet another `libgdx` game prototype where spaceshit flies around procedurally generated 3D planet. 

# Downloads
 * Desktop JAR - https://github.com/afomins/planet/releases/download/v0.1.0/planet-v0.1.0.jar
 * Android APK - https://github.com/afomins/planet/releases/download/v0.1.0/planet-v0.1.0.apk
 
# How it looks
Following GIFs illustrate how "*Planet*" looks:

|  Flying above planet | Scaling and rotating around spaceship |
| --|--|
| <img src="https://github.com/afomins/planet/blob/master/assets-raw/planet-000.gif" width="300"> | <img src="https://github.com/afomins/planet/blob/master/assets-raw/planet-001.gif" width="300"> |

|  Flying above South America | Shooting bullets |
| --|--|
| <img src="https://github.com/afomins/planet/blob/master/assets-raw/planet-002.gif" width="300"> | <img src="https://github.com/afomins/planet/blob/master/assets-raw/planet-003.gif" width="300"> |

# Implementation details
 * `libgdx` was used as platform independent framework
 * Spaceship model was siletly stolen from `libgdx` tutorials
 * Planet sphere model was procedurally generated from **icosphere** as described in this tutorial -> http://blog.andreaskahler.com/2009/06/creating-icosphere-mesh-in-code.html
 * Following [**heightmap**](https://github.com/afomins/planet/blob/master/android/assets/earth_height_map.png) was used to modify height of icosphere's vertices to generate mountains
 * Following [**colormap**](https://github.com/afomins/planet/blob/master/android/assets/earth_surface_map.png) was used to adjust color of icosphere's vertices to separate land from see
 
# How to run Desktop app
 * Clone `git@github.com:afomins/planet.git` and import *Gradle* project it into your favorite IDE
 * Allow *Gradle* to download dependent packages and update project files
 * Run `com.matalok.planet.desktop.DesktopLauncher` class
