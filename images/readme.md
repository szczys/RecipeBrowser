# App Icon Guide

This directory holds the app icon source files. The current revision uses the following for foreground and background:

* recipe-browser-icon-morecrust-shadow_nonstop.svg
* picnic-resized-blue.svg

For some reason the gradient form the inkscape file throws an error when imported to Android Studio (resulting in the shadow not being shows). The solution to this is to run the source fiel (without the _nonstop addendum in the filename) through the following program:
https://github.com/s3r6/svg-non-stop/
