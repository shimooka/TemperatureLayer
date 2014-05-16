Temperature Layer
==
[Temperature Layer] is the simple Android application which displays battery temperature. The temperature will be displayed on the transparent layer, so it does not become obstructive.

Temperature Layer is available at [Google Play].

The source code (eclipse project format) is also available on [GitHub](https://github.com/shimooka/TemperatureLayer) under [Apache License, Version 2.0][Apache].

Feature
--
- Android2.0 or later
- The battery temperature will be displayed on the transparent layer
- Automatic start on boot
- Temperature notification
- Show/Hide icon on the status bar (**Android 4.2+**)
- Celsius (°C) or Fahrenheit (°F)
- Display temperature where you want (on a status bar is possible)
- Choose your favorite font in device
- Customize text size and text color with transparency
- High temperature alert with notification sound and vibration
- English and Japanese translation
- ... and for FREE !

About display position
--

- Touch 'Position' menu in settings, enter '**edit**' mode
- In the edit mode, the temperature text color will be displayed in **red**
- **Swipe** to move the temperature text
- **Two-finger touch** to exit edit mode

Limitation in edit mode
--
If swipe up and up, the temperature text will go under the status bar (and you will not see the text). This is NOT a bug. When you exit edit mode, the text will display collectly.

Screen capture
--------------
![All screen of Temperature Layer](capture.png)

Acknowledgement
---------------
This application use following libraries. Thanks for author's great works !

- [Android Color Picker]
- [FontPreference dialog for Android] (modified version)

Releases
--------
- 2014/05/16 ver.1.0.3 - New display position, show/hide icon on the status bar, fixed bugs
- 2014/05/09 ver.1.0.2 - added high temperature alert
- 2013/07/26 ver.1.0.1 - added font chooser and notification
- 2013/06/26 ver.1.0.0 - first stable release
- 2013/06/25 ver.0.9.3 - display on a status bar is possible
- 2013/06/24 ver.0.9.2 - internal refactored
- 2013/06/19 ver.0.9.1 - initial beta release

License
-------
Copyright &copy; 2013,2014 Hideyuki SHIMOOKA &lt;shimooka@doyouphp.jp&gt;
Licensed under the [Apache License, Version 2.0][Apache]

[Apache]: http://www.apache.org/licenses/LICENSE-2.0
[Android Color Picker]: https://code.google.com/p/android-color-picker/
[Temperature Layer]: https://play.google.com/store/apps/details?id=jp.doyouphp.android.temperaturelayer
[Google Play]: https://play.google.com/store/apps/details?id=jp.doyouphp.android.temperaturelayer
[FontPreference dialog for Android]: http://www.ulduzsoft.com/2012/01/fontpreference-dialog-for-android/
